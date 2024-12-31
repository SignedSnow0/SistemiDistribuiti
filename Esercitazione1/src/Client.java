import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Client <discovery_server IP> <discovery_server port>");
            System.exit(1);
        }

        var buffer = new byte[1024];
        var packet = new DatagramPacket(buffer, buffer.length);

        try {
            socket = new DatagramSocket();

            packet.setAddress(InetAddress.getByName(args[0]));
            packet.setPort(Integer.parseInt(args[1]));
        } catch (SocketException | NumberFormatException | UnknownHostException e) {
            System.out.println("Error parsing input arguments");
            System.exit(1);
        }

        var reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Select an option ([ls] to show files, [swap <port> <i> <j>] to swap lines): ");
        String line = reader.readLine();
        while (line != null) {
            if (line.equals("ls")) {
                String[] files;
                try {
                    packet.setData(line.getBytes(StandardCharsets.UTF_8));
                    socket.send(packet);

                    packet.setData(buffer);
                    socket.receive(packet);

                    var response = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                    files = response.split("\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Available files: ");
                for (int i = 0; i < files.length; i++) {
                    System.out.print("\t" + i + 1 + ": ");
                    System.out.println(files[i]);
                }
            }
            else if (line.startsWith("swap")) {
                var tokens = line.split(" ");
                if (tokens.length != 4) {
                    System.out.println("Invalid command");
                    continue;
                }

                int port, firstIndex, secondIndex;
                try {
                    port = Integer.parseInt(tokens[1]);
                    firstIndex = Integer.parseInt(tokens[2]);
                    secondIndex = Integer.parseInt(tokens[3]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid command");
                    continue;
                }

                var request = firstIndex + ";" + secondIndex;
                try {
                    packet.setPort(port);
                    packet.setData(request.getBytes(StandardCharsets.UTF_8));
                    socket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                System.out.println("Invalid command");
            }


            System.out.println("Select an option ([ls] to show files, [swap <port> <i> <j>] to swap lines): ");
            line = reader.readLine();
        }
    }

    private static DatagramSocket socket;
}
