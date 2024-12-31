import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) throws IOException {
        IFrontEnd registry = null;
        try {
            registry = (IFrontEnd) Naming.lookup(REGISTRY_URL);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println("Failed to find server");
            System.exit(1);
        }

        var in = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter a name to lookup: ");
        var line = in.readLine();
        while (line != null) {
            var servers = registry.Lookup(line);
            if (servers.isEmpty()) {
                System.out.println("No servers found");
            } else {
                System.out.println("Servers found:");
                for (var server : servers) {
                    System.out.println(server.getName());
                }
            }

            System.out.println("Enter a name to lookup: ");
            line = in.readLine();
        }
    }

    private static final String REGISTRY_URL = "rmi://localhost:1099/Registry";
}
