import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Client <dsIP> <dsPort>");
            System.exit(1);
        }

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        var buffer = new byte[1024];
        var packet = new DatagramPacket(buffer, buffer.length);

        String[] files;
        try {
            packet.setAddress(InetAddress.getByName(args[0]));
            packet.setPort(Integer.parseInt(args[1]));

            packet.setData("ls".getBytes(StandardCharsets.UTF_8));
            socket.send(packet);

            packet.setData(buffer);
            socket.receive(packet);

            var response = new String(packet.getData(), StandardCharsets.UTF_8).trim();
            files = response.split("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (files.length == 0) {
            System.out.println("No files found");
            System.exit(1);
        }

        System.out.println("Files disponibili:");
        for (int i = 0; i < files.length; i++) {
            System.out.print("\t" + i + 1 + ": ");
            System.out.println(files[i]);
        }
    }

    private static DatagramSocket socket;
}
