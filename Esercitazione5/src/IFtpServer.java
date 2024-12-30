import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFtpServer extends Remote {
    GetDirResponse GetDirActive(String dir) throws RemoteException;
    String[] GetDirPassive(String dir, InetAddress ip, int port) throws RemoteException;
}
