
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLHandshakeException;

public class ProxyChecker {

    public static ArrayList<ProxyStatus> checkProxies() {
        ArrayList<ProxyStatus> proxyStatuses = new ArrayList<>();

        return proxyStatuses;
    }

    public static ProxyStatus checkProxy(String proxy, RequestsConfigs configs) {
        ProxyStatus proxyCheckResult = new ProxyStatus(proxy, ProxyStatuses.UNABLE);
        ArrayList<String> parsedProxyAddress = ProxyAddressParser.parse(proxy);
        if (parsedProxyAddress == null) {
            return proxyCheckResult;
        }

        try {
            InetAddress proxyAddress = InetAddress.getByName(parsedProxyAddress.get(1));
            InetSocketAddress proxySocket = new InetSocketAddress(proxyAddress, Integer.parseInt(parsedProxyAddress.get(2)));

            HttpClient client = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(proxySocket))
                    .connectTimeout(Duration.ofSeconds(configs.getConnectTimeout()))
                    .build();

            // Get current IP
            HttpRequest requestCurrentIP = HttpRequest.newBuilder()
                    .uri(URI.create(configs.getCurrentIpAPIAddress()))
                    .timeout(Duration.ofSeconds(configs.getConnectTimeout()))
                    .build();
            HttpResponse<String> responseCurrentIP = client.send(requestCurrentIP, HttpResponse.BodyHandlers.ofString());
            String currentIP = responseCurrentIP.body();
            int currentIPStatusCode = responseCurrentIP.statusCode();

            // Get current IP country
            if (currentIPStatusCode == 200) {
                HttpRequest requestCurrentCountryByIP = HttpRequest.newBuilder()
                        .uri(URI.create(configs.getGeoIpAPIAddress() + currentIP))
                        .header("User-Agent", "curl")
                        .timeout(Duration.ofSeconds(configs.getConnectTimeout()))
                        .build();
                HttpResponse<String> response = client.send(requestCurrentCountryByIP, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    proxyCheckResult.setStatus(ProxyStatuses.OK);
                    String body = response.body();
                    Pattern countryPattern = Pattern.compile("\"country\"\\s*:\\s*\"([^\"]+)\"");
                    Pattern cityPattern = Pattern.compile("\"city\"\\s*:\\s*\"([^\"]+)\"");
                    Matcher countryMatcher = countryPattern.matcher(body);
                    Matcher cityMatcher = cityPattern.matcher(body);
                    String country = countryMatcher.find() ? countryMatcher.group(1) : "";
                    String city = cityMatcher.find() ? cityMatcher.group(1) : "";
                    proxyCheckResult.setMessage(country + "/" + city);
                }
            } else {
                proxyCheckResult.setStatus(ProxyStatuses.ERROR);
                proxyCheckResult.setMessage("UnreachableCurrentIP");
            }

        } catch (SSLHandshakeException e) {
            proxyCheckResult.setStatus(ProxyStatuses.ERROR);
            proxyCheckResult.setMessage(e.getClass().getSimpleName());
        } catch (HttpConnectTimeoutException e) {
            proxyCheckResult.setStatus(ProxyStatuses.TIMEOUT);
            proxyCheckResult.setMessage(e.getClass().getSimpleName());
        } catch (InterruptedException | IOException e) {
            proxyCheckResult.setStatus(ProxyStatuses.ERROR);
            proxyCheckResult.setMessage(e.getClass().getSimpleName());
        }
        return proxyCheckResult;
    }
}
