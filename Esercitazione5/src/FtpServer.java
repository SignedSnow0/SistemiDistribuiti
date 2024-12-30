import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FtpServer extends UnicastRemoteObject implements IFtpServer {
    public FtpServer() throws RemoteException { super(); }

    @Override
    public GetDirResponse GetDirActive(String path) throws RemoteException {
        try {
            var folder = new File(path);
            if (!folder.isDirectory()) {
                return null;
            }

            List<String> files = new ArrayList<>();
            for (var file : folder.listFiles()) {
                if (file.isFile()) {
                    files.add(file.getName());
                }
            }

            var server = new FileServerActive(path);
            new Thread(server).start();

            return new GetDirResponse(server.GetIp(), server.GetPort(), files.toArray(new String[files.size()]));
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String[] GetDirPassive(String path, InetAddress ip, int port) throws RemoteException {
        try {
            var folder = new File(path);
            if (!folder.isDirectory()) {
                return null;
            }

            List<String> files = new ArrayList<>();
            for (var file : folder.listFiles()) {
                if (file.isFile()) {
                    files.add(file.getName());
                }
            }

            var server = new FileServerPassive(path, ip, port);
            new Thread(server).start();

            return files.toArray(new String[files.size()]);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            var server = new FtpServer();

            Naming.rebind("//localhost:1099/FtpServer", server);
        } catch (RemoteException | MalformedURLException e) {
            System.err.println("Remote exception: " + e.getMessage());
            System.exit(1);
        }
    }
}
