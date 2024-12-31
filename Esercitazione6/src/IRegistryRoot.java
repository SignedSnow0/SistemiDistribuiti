import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRegistryRoot extends Remote {
    void Register(char firstLetter, char lastLetter, IRemoteRegistry registry) throws RemoteException;
}
