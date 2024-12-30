import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;

public class FileServerActive implements Runnable {
    public FileServerActive(String dir) throws IOException, IllegalArgumentException {
        this.dir = new File(dir);
        if (!this.dir.isDirectory()) {
            throw new IllegalArgumentException("dir must be an existing directory!");
        }       
        
        socket = new ServerSocket();
        socket.bind(null);
    }

    public InetAddress GetIp() { return socket.getInetAddress(); }
    public int GetPort() { return socket.getLocalPort(); }

    @Override
    public void run() {
        try {
            var client = socket.accept();
            client.setSoTimeout(30000);

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
    private final ServerSocket socket;
}
