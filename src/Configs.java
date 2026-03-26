
public class Configs implements RequestsConfigs {

    private final boolean doPrintSettings;
    private final int connectTimeout;
    private final int threads;
    private final int proxiesLimit;
    private final boolean doPrintAllProxies;
    private final String currentIpAPIAddress;
    private final String geoIpAPIAddress;
    private final boolean isProxyUrlToList;
    private final String proxyUrl;

    public boolean getDoPrintSettings() {
        return doPrintSettings;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getThreads() {
        return threads;
    }

    public int getProxiesLimit() {
        return proxiesLimit;
    }

    public boolean getDoPrintAllProxies() {
        return doPrintAllProxies;
    }

    @Override
    public String getCurrentIpAPIAddress() {
        return currentIpAPIAddress;
    }

    @Override
    public String getGeoIpAPIAddress() {
        return geoIpAPIAddress;
    }

    public boolean isProxyUrlToList() {
        return isProxyUrlToList;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public Configs(Builder builder) {
        this.doPrintSettings = builder.doPrintSettings;
        this.connectTimeout = builder.connectTimeout;
        this.threads = builder.threads;
        this.proxiesLimit = builder.proxiesLimit;
        this.doPrintAllProxies = builder.doPrintAllProxies;
        this.currentIpAPIAddress = builder.currentIpAPIAddress;
        this.geoIpAPIAddress = builder.geoIpAPIAddress;
        this.isProxyUrlToList = builder.isProxyUrlToList;
        this.proxyUrl = builder.proxyUrl;
    }

    public static class Builder {

        private boolean doPrintSettings = Boolean.parseBoolean(System.getProperty("printSettings", "false"));
        private int connectTimeout = Integer.parseInt(System.getProperty("timeout", "5"));
        private int threads = Integer.parseInt(System.getProperty("threads", "5"));
        private int proxiesLimit = Integer.parseInt(System.getProperty("proxiesLimit", "-1"));
        private boolean doPrintAllProxies = Boolean.parseBoolean(System.getProperty("printAll", "false"));
        private String currentIpAPIAddress = System.getProperty("currentIpAPIAddress", "https://api.ipify.org/");
        private String geoIpAPIAddress = System.getProperty("geoIpAPIAddress", "https://ipinfo.io/");
        private boolean isProxyUrlToList = false;
        private String proxyUrl = "";

        public Configs build(String[] args) {
            checkArgs(args);
            this.proxyUrl = args[0];
            if (args[0].endsWith(".txt")) {
                this.isProxyUrlToList = true;
            }
            return new Configs(this);
        }
    }

    public static void checkArgs(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("ERROR! Too many arguments.");
        } else if (args.length == 0) {
            throw new IllegalArgumentException("ERROR! There is no proxy address argument.");
        }
    }
}
