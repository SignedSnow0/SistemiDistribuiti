import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFtpServer extends Remote {
    ConnectionDetails GetDirActive(String dir) throws RemoteException;
    String[] GetDirPassive(String dir, String ip, int port) throws RemoteException;
}
