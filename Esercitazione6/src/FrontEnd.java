import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FrontEnd extends UnicastRemoteObject implements IFrontEnd, IRegistryRoot {
    public FrontEnd() throws RemoteException {
        super();
    }

    @Override
    public void Register(String name, IServer endpoint) throws RemoteException {
        var c = name.toLowerCase().charAt(0);
        for (int i = 0; i < firstLetters.size(); i++) {
            if (c >= firstLetters.get(i) && c <= lastLetters.get(i)) {
                registries.get(i).Register(name, endpoint);
            }
        }
    }

    @Override
    public List<IServer> Lookup(String name) throws RemoteException {
        var c = name.toLowerCase().charAt(0);
        for (int i = 0; i < firstLetters.size(); i++) {
            if (c >= firstLetters.get(i) && c <= lastLetters.get(i)) {
                return registries.get(i).Lookup(name);
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void Register(char firstLetter, char lastLetter, IRemoteRegistry registry) {
        firstLetters.add(firstLetter);
        lastLetters.add(lastLetter);
        registries.add(registry);
    }

    public static void main(String[] args) {
        try {
            var frontEnd = new FrontEnd();
            Naming.rebind(REGISTRY_URL, frontEnd);
        } catch (Exception e) {
            System.out.println("Failed to start front end");
            System.exit(1);
        }
    }

    private final List<Character> firstLetters = new ArrayList<>();
    private final List<Character> lastLetters = new ArrayList<>();
    private final List<IRemoteRegistry> registries = new ArrayList<>();

    private static final String REGISTRY_URL = "rmi://localhost:1099/Registry";
}
