
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ProxyListLoader {

    public static List<String> download(String proxyListURL) {
        List<String> proxies = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(proxyListURL))
                .build();
        try {
            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            proxies = body.lines().toList();
        } catch (IOException | InterruptedException e) {
            System.out.println("ERROR! Proxy list '" + proxyListURL + "' is not accessible.");
        }
        return proxies;
    }
}
