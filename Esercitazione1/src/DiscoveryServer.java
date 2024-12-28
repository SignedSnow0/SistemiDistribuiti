import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryServer {

    /**
     * @param args client port, row swap port
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("DiscoveryServer <client port> <row_swap port>");
            System.exit(1);
        }

        servers = new ArrayList<>();

        try {
            clientSocket = new DatagramSocket(Integer.parseInt(args[0]));
            rowSwapSocket = new DatagramSocket(Integer.parseInt(args[1]));
        } catch (SocketException | NumberFormatException e) {
            System.out.println("Error creating socket");
            System.exit(1);
        }

        var clientThread = new Thread(() -> {
            while(true) {
                var buffer = new byte[1024];
                var packet = new DatagramPacket(buffer, buffer.length);
                String request;
                try {
                    packet.setData(buffer);
                    clientSocket.receive(packet);
                    request = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                } catch (IOException e) { continue; }

                var response = "Unknown request";
                if (request.equals("ls")) {
                    var responseBuilder = new StringBuilder();
                    for (var server : servers) {
                        responseBuilder.append(server.file());
                        responseBuilder.append(";");
                        responseBuilder.append(server.port());
                        responseBuilder.append("\n");
                    }
                    response = responseBuilder.toString();
                }

                try {
                    packet.setData(response.getBytes());
                    clientSocket.send(packet);
                } catch (IOException _) { }
            }
        });
        clientThread.start();

        var rowSwapThread = new Thread(() -> {
            while(true) {
                var buffer = new byte[1024];
                var packet = new DatagramPacket(buffer, buffer.length);
                String newFile;
                try {
                    packet.setData(buffer);
                    rowSwapSocket.receive(packet);
                    newFile = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                } catch (IOException e) {
                    continue;
                }

                boolean duplicate = false;
                for (var entry : servers) {
                    if (entry.file().equals(newFile)) {
                        duplicate = true;
                        break;
                    }

                    if (entry.ip().equals(packet.getAddress()) && entry.port() == packet.getPort()) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) { continue; }

                servers.add(new ServerEntry(newFile, packet.getAddress(), packet.getPort()));
            }
        });
        rowSwapThread.start();
    }

    /**
     * List of registered servers
     */
    private static List<ServerEntry> servers;

    /**
     * Client interface socket
     */
    private static DatagramSocket clientSocket;
    /**
     * RowSwapServer socket
     */
    private static DatagramSocket rowSwapSocket;
}
