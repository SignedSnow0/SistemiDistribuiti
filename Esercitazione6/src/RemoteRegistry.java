import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RemoteRegistry extends UnicastRemoteObject implements IRemoteRegistry {
    public RemoteRegistry(char firstLetter, char lastLetter) throws RemoteException {
        super();
        this.firstLetter = firstLetter;
        this.lastLetter = lastLetter;
    }

    @Override
    public void Register(String name, IServer endpoint) {
        var lowerName = name.toLowerCase();
        if (lowerName.charAt(0) < firstLetter || lowerName.charAt(0) > lastLetter) {
            return;
        }

        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(lowerName)) {
                endpoints.get(i).add(endpoint);
                return;
            }
        }

        var list = new ArrayList<IServer>();
        list.add(endpoint);

        names.add(lowerName);
        endpoints.add(list);
    }

    @Override
    public List<IServer> Lookup(String name) {
        var lowerName = name.toLowerCase();
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(lowerName)) {
                return endpoints.get(i);
            }
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) throws RemoteException {
        if (args.length != 2) {
            System.out.println("Usage: java RemoteRegistry <firstLetter> <lastLetter>");
            System.exit(1);
        }

        var firstLetter = args[0].charAt(0);
        var lastLetter = args[1].charAt(0);

        var subRegistry = new RemoteRegistry(firstLetter, lastLetter);

        try {
            IRegistryRoot registry = (IRegistryRoot) Naming.lookup(REGISTRY_URL);

            registry.Register(firstLetter, lastLetter, subRegistry);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println("Failed to register server");
            System.exit(1);
        }
    }

    private final List<String> names = new ArrayList<>();
    private final List<List<IServer>> endpoints = new ArrayList<>();
    private final char firstLetter;
    private final char lastLetter;

    private static final String REGISTRY_URL = "rmi://localhost:1099/Registry";
}
