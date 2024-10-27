import java.net.InetAddress;

public record ServerEntry(String file, InetAddress ip, int port) { }

