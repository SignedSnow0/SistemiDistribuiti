import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class FtpClient {
    public static void main(String[] args) {
        try {
            var server = (IFtpServer)Naming.lookup("//localhost:1099/FtpServer");

            String dir;
            do {
                System.out.print("Inserire cartella da ricevere: ");
                dir = System.console().readLine();

                var details = server.GetDirActive(dir);
                System.out.println("Connessione: " + details.GetIp() + ":" + details.GetPort());
                System.out.println("Cartelle disponibili:");
                for (var file : details.GetFiles()) {
                    System.out.println("\t" + file);
                }


                try (var socket = new Socket(details.GetIp(), details.GetPort())){
                    var inputStream = socket.getInputStream();
                    var outputStream = socket.getOutputStream();


                } catch (IOException e) {

                }

            } while (dir != null);
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
