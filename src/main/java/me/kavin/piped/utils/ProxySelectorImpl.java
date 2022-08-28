package me.kavin.piped.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ProxySelectorImpl extends ProxySelector {


    // private String proxyHost;
    // private Integer proxyPort;
    private final Set<URI> noProxiedUris = new HashSet<>();
    private final ArrayList<Proxy> noProxy = new ArrayList<Proxy>();
    private final ArrayList<Proxy> externalProxy = new ArrayList<Proxy>();
    private final ArrayList<Proxy> unknownProxy = new ArrayList<Proxy>();
    // private final ProxySelector delegate;

    public ProxySelectorImpl(String externalProxyHost, Integer externalProxyPort) {
        // this.proxyHost = proxyHost;
        // this.proxyPort = proxyPort;
        noProxy.add(Proxy.NO_PROXY);
        externalProxy.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
            externalProxyHost, externalProxyPort)));

        // this.delegate = delegate;
    }

    @Override
    public List<Proxy> select(final URI uri) {
        if (uri.getScheme().equals("socket")  || noProxiedUris.contains(uri)) {
            System.err.println(uri.getScheme() + "### NOPROXY: " + uri);
            return this.noProxy;
        }
        /// todo: whitelist domains
        System.err.println(uri.getScheme() + "### YESPROXY: " + uri);
        return this.externalProxy;

//        final InetSocketAddress proxyAddress = InetSocketAddress
//            .createUnresolved(proxyHost, proxyPort);
//        if (uri.scheme == "socket://"  || noProxiedUris.contains(uri)) {
//            System.err.println(uri.scheme + "### NOPROXY: " + uri);
//            return Collections.singletonList(new Proxy(Type.HTTP, proxyAddress));
//        }
//        System.err.println(uri.scheme + "### YESPROXY:" + uri);
//        return Collections.singletonList(new Proxy(Type.HTTP, proxyAddress));
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
        System.err.println("Unable to reach uri " + uri + " via proxy: " + ioe.getMessage());
    }

    public void addNoProxyUri(URI uri) {
        noProxiedUris.add(uri);
    }
}

