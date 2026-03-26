
import java.util.ArrayList;

public class ProxyAddressParser {

    public static ArrayList<String> parse(String proxyAddress) {
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
            Utils.printHelp();
            throw new IllegalArgumentException("ERROR! Invalid proxy list address.");
        }

        return proxySplittedAddress;
    }
}
