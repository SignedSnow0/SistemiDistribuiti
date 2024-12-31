import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("FileServer <port>");
            System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("FileServer <port>");
            System.exit(1);
        }

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
        } catch (Exception e) {
            System.err.println("Error creating socket");
            System.exit(1);
        }

        try {
            while (true) {
                try {
                    var client = serverSocket.accept();
                    client.setSoTimeout(30000);

                    new FileThread(client).start();
                } catch (Exception e) {
                    System.err.println("Error accepting connection");
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error executing server");
            System.exit(1);
        }
    }

    private static ServerSocket serverSocket;
}