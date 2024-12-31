import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRemoteRegistry extends Remote {
    void Register(String name, IServer endpoint) throws RemoteException;

    List<IServer> Lookup(String name) throws RemoteException;
}
