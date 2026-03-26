
public class ProxyStatus {

    private final String proxy;
    private ProxyStatuses status;
    private String message;

    protected ProxyStatus(String proxy, ProxyStatuses status) {
        this.proxy = proxy;
        this.status = status;
    }

    protected ProxyStatus(String proxy, ProxyStatuses status, String message) {
        this.proxy = proxy;
        this.status = status;
        this.message = message;
    }

    public String getProxy() {
        return proxy;
    }

    public ProxyStatuses getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(ProxyStatuses status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        if (message != null) {
            return String.format("%s - %s (%s)", proxy, status, message);
        } else {
            return String.format("%s - %s", proxy, status);
        }
    }
}
