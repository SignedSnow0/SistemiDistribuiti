import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
    public String getName() throws RemoteException;
}
