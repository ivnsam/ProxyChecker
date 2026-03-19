import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import javax.net.ssl.SSLHandshakeException;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ProxyChecker {
  boolean doPrintSettings = Boolean.parseBoolean(System.getProperty("printSettings", "false"));
  int connectTimeout = Integer.parseInt(System.getProperty("timeout", "5"));
  int threads = Integer.parseInt(System.getProperty("threads", "5"));
  int proxiesLimit = Integer.parseInt(System.getProperty("proxiesLimit", "-1"));
  boolean doPrintAllProxies = Boolean.parseBoolean(System.getProperty("printAll", "false"));
  String currentIpAPIAddress = System.getProperty("currentIpAPIAddress", "https://api.ipify.org/");
  String geoIpAPIAddress = System.getProperty("geoIpAPIAddress", "https://ipinfo.io/");
  String proxy;

  public void main(String[] args) {
    System.out.println("StatusCheckWithClientAndProxy.");
    System.out.println();
    
    if (doPrintSettings) printSettings(args);

    if (args.length > 0) {
      proxy = args[0];
      if (proxy.equals("--help")) printHelp();
      else if (proxy.endsWith(".txt")) {
        List<String> proxies = downloadProxyList(proxy);
        if (proxiesLimit > 0) proxies = proxies.stream().limit(proxiesLimit).collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<ProxyStatus>> futures = new ArrayList<>();
        for (final String item : proxies) {
          futures.add(executor.submit(() -> {
            return checkProxy(item);
          }));
        }
        executor.shutdown();
        for (Future<ProxyStatus> future : futures) {
          try {
            ProxyStatus proxyStatus = future.get();
            if (doPrintAllProxies || proxyStatus.getStatus() == ProxyStatuses.OK) System.out.println(proxyStatus);
          } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
          }
        }
      } else {
        ProxyStatus proxyStatus = checkProxy(proxy);
        System.out.println(proxyStatus);
      }
    } else {
      System.out.println("ERROR! Proxy address is not set. See help.");
      System.out.println();
      printHelp();
    }
  }

  public ProxyStatus checkProxy(String proxy) {
    ProxyStatus proxyCheckResult = new ProxyStatus(proxy, ProxyStatuses.UNABLE);
    ArrayList<String> parsedProxyAddress = parseProxyAddress(proxy);

    try {
      InetAddress proxyAddress = InetAddress.getByName(parsedProxyAddress.get(1));
      InetSocketAddress proxySocket = new InetSocketAddress(proxyAddress, Integer.parseInt(parsedProxyAddress.get(2)));

      HttpClient client = HttpClient.newBuilder()
        .proxy(ProxySelector.of(proxySocket))
        .connectTimeout(Duration.ofSeconds(connectTimeout))
        .build();

      // Get current IP
      HttpRequest requestCurrentIP = HttpRequest.newBuilder()
        .uri(URI.create(currentIpAPIAddress))
        .build();
      HttpResponse<String> responseCurrentIP = client.send(requestCurrentIP, HttpResponse.BodyHandlers.ofString());
      String currentIP = responseCurrentIP.body();
      int currentIPStatusCode = responseCurrentIP.statusCode();

      // Get current IP country
      if (currentIPStatusCode == 200) {
        HttpRequest requestCurrentCountryByIP = HttpRequest.newBuilder()
          .uri(URI.create(geoIpAPIAddress + currentIP))
          .header("User-Agent", "curl")
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

  private ArrayList<String> parseProxyAddress(String proxyAddress) {
    ArrayList<String> proxySplittedAddress = new ArrayList<>();

    String[] proxySplit = proxyAddress.split("://");
    if (proxySplit.length > 1) {
      proxySplittedAddress.add(proxySplit[0]);
      proxySplit = proxySplit[1].split(":");
    } else {
      System.out.println("WARN! Proxy proto is set to default: http.");
      proxySplittedAddress.add("http");
      proxySplit = proxySplit[0].split(":");
    }

    if (proxySplit.length > 1) {
      proxySplittedAddress.add(proxySplit[0]);
      proxySplittedAddress.add(proxySplit[1]);
    } else {
      System.out.println("ERROR! Proxy address is incorrect.");
      System.out.println();
      printHelp();
      System.exit(1);
    }

    return proxySplittedAddress;
  }

  private List<String> downloadProxyList(String proxyListURL) {
    List<String> proxies = new ArrayList<>();
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(proxyListURL)).build();
    try {
      String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
      proxies = body.lines().toList();
    } catch (IOException | InterruptedException e) {
      System.out.println("ERROR! Proxy list '" + proxyListURL + "' is not accessible.");
    }
    return proxies;
  }

  private void printSettings(String[] args) {
    System.out.println("Connection timeout: " + connectTimeout);
    System.out.println("Current IP service API address: " + currentIpAPIAddress);
    System.out.println("GeoIP service API address: " + geoIpAPIAddress);
    if (args.length > 0 && args[0].endsWith(".txt")) {
      System.out.println("Threads: " + threads);
      System.out.println("Proxies limit: " + proxiesLimit);
      System.out.println("Print all proxies: " + doPrintAllProxies);
    }
    System.out.println();    
  }

  private void printHelp() {
    System.out.println("Checks if a proxy is working.");
    System.out.println("How to use:");
    System.out.println("  java StatusCheckWithClientAndProxy [<proxyProtocol>]://<proxyIP>:<proxyPort>");
    System.out.println("  OR");
    System.out.println("  java StatusCheckWithClientAndProxy <URLWithProxyListInTxt>");
    System.out.println("Additional parameters:");
    System.out.println("  -Dtimeout=<seconds> parameter to set timeout for requests. Default: 5 sec.");
    System.out.println("  -DcurrentIpAPIAddress=<currentIpAPIAddress> parameter to set API address to get current IP. Default: https://api.ipify.org/.");
    System.out.println("  -DgeoIpAPIAddress=<geoIpAPIAddress> parameter to set API address to get country info by current IP. Default: https://ipinfo.io/.");
    System.out.println("  -DprintSettings=<printSettings> parameter to print settings on start. Default: false.");
    System.out.println("Additional parameters for multiple proxies from file:");
    System.out.println("  -Dthreads=<threads> parameter to set amount of threads which are checking proxies. Default: 5.");
    System.out.println("  -DproxiesLimit=<proxiesLimit> parameter to set maximum of proxies to check if the file with proxies is long. Negative = unlimited. Default: -1.");
    System.out.println("  -DprintAllProxies=<printAllProxies> parameter to print results for all proxies. If false - prints only working proxies. Default: false.");
    System.out.println();
  }

  private enum ProxyStatuses {
    OK, TIMEOUT, ERROR, UNABLE
  }

  private class ProxyStatus {
    private String proxy;
    private ProxyStatuses status;
    private String message;

    private ProxyStatus(String proxy, ProxyStatuses status) {
      this.proxy = proxy;
      this.status = status;
    }

    private ProxyStatus(String proxy, ProxyStatuses status, String message) {
      this.proxy = proxy;
      this.status = status;
      this.message = message;
    }

    public String getProxy() { return proxy; }
    public ProxyStatuses getStatus() { return status; }
    public String getMessage() { return message; }

    public void setStatus(ProxyStatuses status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
      if (message != null) return String.format("%s - %s (%s)", proxy, status, message);
      else return String.format("%s - %s", proxy, status);
    }
  }
}