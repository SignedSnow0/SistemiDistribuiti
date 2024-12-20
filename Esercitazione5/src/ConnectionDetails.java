import java.io.Serializable;
import java.net.InetAddress;

public class ConnectionDetails implements Serializable {
    public ConnectionDetails(InetAddress ip, int port, String[] files) {
        this.ip = ip;
        this.port = port;
        this.files = files;
    }

    public InetAddress GetIp() { return ip; }
    public int GetPort() { return port; }
    public String[] GetFiles() { return files; }

    private final InetAddress ip;
    private final int port;
    private final String[] files;
}
