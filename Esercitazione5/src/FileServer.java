import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class FileServer implements Runnable {
    public FileServer(String dir) throws IOException, IllegalArgumentException {
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
            
            System.out.println("Connesso");
        } catch (IOException e) {

        }

        System.out.println("Fine thread");
    }

    private final File dir;
    private final ServerSocket socket;
}
