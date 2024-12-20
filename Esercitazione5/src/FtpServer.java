import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class FtpServer extends UnicastRemoteObject implements IFtpServer {
    public FtpServer() throws RemoteException { super(); }

    @Override
    public ConnectionDetails GetDirActive(String path) throws RemoteException {
        try {
            var server = new FileServer(path);
           
            var files = Arrays.stream(new File(path)
                .listFiles())
                .filter(File::isFile)
                .map(File::getName)
                .toArray(String[]::new);

            for (var file : files) {
                System.out.println(file);
            }

            new Thread(server).start();

            System.out.println("Fine rmi");

            return new ConnectionDetails(server.GetIp(), server.GetPort(), files);
        } catch (IOException | IllegalArgumentException e) {
            throw new RemoteException();
        }
    }

    @Override
    public String[] GetDirPassive(String path, String ip, int port) throws RemoteException {
        return null;
    }

    public static void main(String[] args) {
        try {
            var server = new FtpServer();

            Naming.rebind("//localhost:1099/FtpServer", server);
        } catch (RemoteException | MalformedURLException e) {
            System.err.println("Remote exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
