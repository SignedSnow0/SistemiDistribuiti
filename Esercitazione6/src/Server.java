import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements IServer {
    public Server(String name) throws RemoteException {
        super();
        this.name = name;
    }

    @Override
    public String getName() throws RemoteException { return name; }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Server <category> <name>");
            System.exit(1);
        }

        try {
            var registry = (IFrontEnd)Naming.lookup(REGISTRY_URL);
            var endpoint = new Server(args[1]);

            registry.Register(args[0], endpoint);
        } catch (Exception e) {
            System.out.println("Failed to register server");
            System.exit(1);
        }
    }

    private final String name;

    private static final String REGISTRY_URL = "rmi://localhost:1099/Registry";
}
