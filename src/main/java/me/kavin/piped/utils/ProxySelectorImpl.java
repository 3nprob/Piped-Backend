package me.kavin.piped.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class ProxySelectorImpl extends ProxySelector {
    private final ArrayList<Proxy> noProxy = new ArrayList<Proxy>();
    private final ArrayList<Proxy> externalProxy = new ArrayList<Proxy>();

    public ProxySelectorImpl(String externalProxyHost, Integer externalProxyPort) {
        noProxy.add(Proxy.NO_PROXY);
        externalProxy.add(
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress(externalProxyHost, externalProxyPort))
        );
    }

    @Override
    public List<Proxy> select(final URI uri) {
        if (uri.getScheme().equals("socket")) {
            return this.noProxy;
        }
        return this.externalProxy;
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
        System.err.println("Unable to reach uri " + uri + " via proxy: " + ioe.getMessage());
    }
}

