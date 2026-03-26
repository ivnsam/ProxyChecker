
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) {
        System.out.println("StatusCheckWithClientAndProxy.");
        System.out.println();

        Configs configs;

        try {
            Configs.checkArgs(args);
            if (args[0].equals("--help") || args[0].equals("-h")) {
                Utils.printHelp();
                return;
            }
            configs = new Configs.Builder().build(args);
        } catch (IllegalArgumentException e) {
            Utils.printHelp();
            System.out.println(e.getMessage());
            return;
        }

        if (configs.getDoPrintSettings()) {
            Utils.printSettings(configs);
        }

        if (configs.isProxyUrlToList()) {
            List<String> proxies = ProxyListLoader.download(configs.getProxyUrl());
            if (configs.getProxiesLimit() >= 0) {
                proxies = proxies.stream().limit(configs.getProxiesLimit()).collect(Collectors.toList());
            }

            ExecutorService executor = Executors.newFixedThreadPool(configs.getThreads());
            List<Future<ProxyStatus>> futures = new ArrayList<>();
            for (final String item : proxies) {
                futures.add(executor.submit(() -> {
                    return ProxyChecker.checkProxy(item, configs);
                }));
            }
            executor.shutdown();
            try {
                executor.awaitTermination((configs.getConnectTimeout() * 3), TimeUnit.SECONDS);
                for (Future<ProxyStatus> future : futures) {
                    ProxyStatus proxyStatus = future.get();
                    if (configs.getDoPrintAllProxies() || proxyStatus.getStatus() == ProxyStatuses.OK) {
                        System.out.println(proxyStatus);
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                Utils.printHelp();
                System.out.println(e.getMessage());
            }
        } else {
            try {
                ProxyStatus proxyStatus = ProxyChecker.checkProxy(configs.getProxyUrl(), configs);
                System.out.println(proxyStatus);
            } catch (IllegalArgumentException e) {
                Utils.printHelp();
                System.out.println(e.getMessage());
                return;
            }
        }
    }
}
