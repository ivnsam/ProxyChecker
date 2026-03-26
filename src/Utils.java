
public class Utils {

    public static void printSettings(Configs configs) {
        System.out.println("Connection timeout: " + configs.getConnectTimeout());
        System.out.println("Current IP service API address: " + configs.getCurrentIpAPIAddress());
        System.out.println("GeoIP service API address: " + configs.getGeoIpAPIAddress());
        if (configs.isProxyUrlToList()) {
            System.out.println("Threads: " + configs.getThreads());
            System.out.println("Proxies limit: " + configs.getProxiesLimit());
            System.out.println("Print all proxies: " + configs.getDoPrintAllProxies());
        }
        System.out.println();
    }

    public static void printHelp() {
        System.out.println("Checks if a proxy is working.");
        System.out.println("How to use:");
        System.out.println("  java ProxyChecker [<proxyProtocol>]://<proxyIP>:<proxyPort>");
        System.out.println("  OR");
        System.out.println("  java ProxyChecker <URLWithProxyListInTxt>");
        System.out.println("Additional parameters:");
        System.out.println("  -Dtimeout=<seconds> parameter to set timeout for requests. Default: 5 sec.");
        System.out.println("  -DcurrentIpAPIAddress=<currentIpAPIAddress> parameter to set API address to get current IP. Default: https://api.ipify.org/.");
        System.out.println("  -DgeoIpAPIAddress=<geoIpAPIAddress> parameter to set API address to get country info by current IP. Default: https://ipinfo.io/.");
        System.out.println("  -DprintSettings=<printSettings> parameter to print settings on start. Default: false.");
        System.out.println("Additional parameters for multiple proxies from file:");
        System.out.println("  -Dthreads=<threads> parameter to set amount of threads which are checking proxies. Default: 5.");
        System.out.println("  -DproxiesLimit=<proxiesLimit> parameter to set maximum of proxies to check if the file with proxies is long. Negative = unlimited. Default: -1.");
        System.out.println("  -DprintAll=<printAll> parameter to print results for all proxies. If false - prints only working proxies. Default: false.");
        System.out.println();
    }
}
