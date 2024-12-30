import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class FileServerPassive implements Runnable {
    public FileServerPassive(String dir, InetAddress ip, int port) throws IOException, IllegalArgumentException {
        this.dir = new File(dir);
        if (!this.dir.isDirectory()) {
            throw new IllegalArgumentException("dir must be an existing directory!");
        }

        clientIp = ip;
        clientPort = port;
    }

    @Override
    public void run() {
        try (var client = new Socket(clientIp, clientPort)) {
            var out = new DataOutputStream(client.getOutputStream());
            var in = new DataInputStream(client.getInputStream());

            var filePath = in.readUTF();
            while (!filePath.equals("stop")) {
                var file = new File(dir + "/" + filePath);
                if (!file.exists()) {
                    out.writeUTF("skip");
                    continue;
                }

                out.writeUTF("continue");

                FileUtility.SendFile(file, out);

                filePath = in.readUTF();
            }
        } catch (IOException e) {
            System.out.println("Failed to connect to client");
        }
    }

    private final File dir;
    private final InetAddress clientIp;
    private final int clientPort;
}