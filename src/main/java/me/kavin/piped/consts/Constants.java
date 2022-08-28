package me.kavin.piped.consts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kavin.piped.utils.PageMixin;
import me.kavin.piped.utils.ProxySelectorImpl;
import me.kavin.piped.utils.RequestUtils;
import me.kavin.piped.utils.resp.ListLinkHandlerMixin;
import okhttp3.OkHttpClient;
import okhttp3.brotli.BrotliInterceptor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.ContentCountry;

import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class Constants {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0";

    public static final int PORT;
    public static final String HTTP_WORKERS;

    public static final String PROXY_PART;

    public static final String IMAGE_PROXY_PART;

    public static final String CAPTCHA_BASE_URL, CAPTCHA_API_KEY;

    public static final StreamingService YOUTUBE_SERVICE;

    public static final String PUBLIC_URL;

    public static final String PUBSUB_URL;

    public static final String PUBSUB_HUB_URL;

    public static final String HTTP_PROXY;

    public static final String FRONTEND_URL;

    public static final OkHttpClient h2client;
    public static final OkHttpClient h2_no_redir_client;

    public static final boolean COMPROMISED_PASSWORD_CHECK;

    public static final boolean DISABLE_REGISTRATION;

    public static final int FEED_RETENTION;

    public static final boolean DISABLE_TIMERS;

    public static final String RYD_PROXY_URL;

    public static final List<String> SPONSORBLOCK_SERVERS;

    public static final boolean DISABLE_RYD;

    public static final boolean DISABLE_SERVER;

    public static final boolean DISABLE_LBRY;

    public static final int SUBSCRIPTIONS_EXPIRY;

    public static final String SENTRY_DSN;

    public static final String MATRIX_ROOM = "#piped-events:matrix.org";

    public static final String MATRIX_SERVER;

    public static final String MATRIX_TOKEN;

    public static final String GEO_RESTRICTION_CHECKER_URL;

    public static final String YOUTUBE_COUNTRY;

    public static final String VERSION;

    public static final ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(Page.class, PageMixin.class)
            .addMixIn(ListLinkHandler.class, ListLinkHandlerMixin.class)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    public static final Object2ObjectOpenHashMap<String, String> hibernateProperties = new Object2ObjectOpenHashMap<>();

    public static final ObjectNode frontendProperties = mapper.createObjectNode();

    static {
        Properties prop = new Properties();
        try {
            YOUTUBE_SERVICE = NewPipe.getService(0);

            if (new File("config.properties").exists()) {
                prop.load(new FileReader("config.properties"));
            }

            PORT = Integer.parseInt(prop.getProperty("PORT", "6000"));
            HTTP_WORKERS = getProperty(prop, "HTTP_WORKERS",
                    String.valueOf(Runtime.getRuntime().availableProcessors()));
            PROXY_PART = getProperty(prop, "PROXY_PART");
            IMAGE_PROXY_PART = getProperty(prop, "IMAGE_PROXY_PART", PROXY_PART);
            CAPTCHA_BASE_URL = getProperty(prop, "CAPTCHA_BASE_URL");
            CAPTCHA_API_KEY = getProperty(prop, "CAPTCHA_API_KEY");
            PUBLIC_URL = getProperty(prop, "API_URL");
            PUBSUB_URL = getProperty(prop, "PUBSUB_URL", PUBLIC_URL);
            PUBSUB_HUB_URL = getProperty(prop, "PUBSUB_HUB_URL", "https://pubsubhubbub.appspot.com/subscribe");
            HTTP_PROXY = getProperty(prop, "HTTP_PROXY");
            FRONTEND_URL = getProperty(prop, "FRONTEND_URL", "https://piped.video");
            COMPROMISED_PASSWORD_CHECK = Boolean.parseBoolean(getProperty(prop, "COMPROMISED_PASSWORD_CHECK", "true"));
            DISABLE_REGISTRATION = Boolean.parseBoolean(getProperty(prop, "DISABLE_REGISTRATION", "false"));
            FEED_RETENTION = Integer.parseInt(getProperty(prop, "FEED_RETENTION", "30"));
            DISABLE_TIMERS = Boolean.parseBoolean(getProperty(prop, "DISABLE_TIMERS", "false"));
            RYD_PROXY_URL = getProperty(prop, "RYD_PROXY_URL", "https://ryd-proxy.kavin.rocks");
            SPONSORBLOCK_SERVERS = List.of(getProperty(prop, "SPONSORBLOCK_SERVERS", "https://sponsor.ajay.app,https://sponsorblock.kavin.rocks")
                    .split(","));
            DISABLE_RYD = Boolean.parseBoolean(getProperty(prop, "DISABLE_RYD", "false"));
            DISABLE_SERVER = Boolean.parseBoolean(getProperty(prop, "DISABLE_SERVER", "false"));
            DISABLE_LBRY = Boolean.parseBoolean(getProperty(prop, "DISABLE_LBRY", "false"));
            SUBSCRIPTIONS_EXPIRY = Integer.parseInt(getProperty(prop, "SUBSCRIPTIONS_EXPIRY", "30"));
            SENTRY_DSN = getProperty(prop, "SENTRY_DSN", "");
            System.getenv().forEach((key, value) -> {
                if (key.startsWith("hibernate"))
                    hibernateProperties.put(key, value);
            });
            MATRIX_SERVER = getProperty(prop, "MATRIX_SERVER", "https://matrix-client.matrix.org");
            MATRIX_TOKEN = getProperty(prop, "MATRIX_TOKEN");
            GEO_RESTRICTION_CHECKER_URL = getProperty(prop, "GEO_RESTRICTION_CHECKER_URL");
            prop.forEach((_key, _value) -> {
                String key = String.valueOf(_key), value = String.valueOf(_value);
                if (key.startsWith("hibernate"))
                    hibernateProperties.put(key, value);
                else if (key.startsWith("frontend."))
                    frontendProperties.put(StringUtils.substringAfter(key, "frontend."), value);
            });
            frontendProperties.put("imageProxyUrl", IMAGE_PROXY_PART);
            frontendProperties.putArray("countries").addAll(
                    YOUTUBE_SERVICE.getSupportedCountries().stream().map(ContentCountry::getCountryCode)
                            .map(JsonNodeFactory.instance::textNode).toList()
            );

            // transform hibernate properties for legacy configurations
            hibernateProperties.replace("hibernate.dialect",
                    "org.hibernate.dialect.PostgreSQL10Dialect",
                    "org.hibernate.dialect.PostgreSQLDialect"
            );

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .addInterceptor(BrotliInterceptor.INSTANCE);
            OkHttpClient.Builder builder_noredir = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .addInterceptor(BrotliInterceptor.INSTANCE);
            if (!StringUtils.isEmpty(HTTP_PROXY) && HTTP_PROXY.contains(":")) {
                String host = StringUtils.substringBefore(HTTP_PROXY, ":");
                String port = StringUtils.substringAfter(HTTP_PROXY, ":");
                ProxySelectorImpl ps = new ProxySelectorImpl(host, Integer.parseInt(port));
                ProxySelector.setDefault(ps);
                final Proxy proxy  = new Proxy(
                    Proxy.Type.HTTP,
                    new InetSocketAddress(host, Integer.parseInt(port))
                );
                builder.proxy(proxy);
                builder_noredir.proxy(proxy);
            }
            h2client = builder.build();
            h2_no_redir_client = builder_noredir.build();
            String temp = null;
            try {
                var html = RequestUtils.sendGet("https://www.youtube.com/");
                var regex = Pattern.compile("GL\":\"([A-Z]{2})\"", Pattern.MULTILINE);
                var matcher = regex.matcher(html);
                if (matcher.find()) {
                    temp = matcher.group(1);
                }
            } catch (Exception ignored) {
                System.err.println("Failed to get country from YouTube!");
            }
            YOUTUBE_COUNTRY = temp;
            VERSION = new File("VERSION").exists() ?
                    IOUtils.toString(new FileReader("VERSION")) :
                    "unknown";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getProperty(final Properties prop, String key) {
        return getProperty(prop, key, null);
    }

    private static String getProperty(final Properties prop, String key, String def) {

        final String envVal = System.getenv(key);

        if (envVal != null)
            return envVal;

        return prop.getProperty(key, def);
    }
}
