import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class FtpClient {
    public static void main(String[] args) throws IOException {
        IFtpServer server = null;
        try {
            server = (IFtpServer) Naming.lookup("//localhost:1099/FtpServer");
        } catch (Exception e) {
            System.out.println("Cannot find server in registry");
            System.exit(1);
        }

        String dir;
        var stdIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Select a folder to receive");
        while ((dir = stdIn.readLine()) != null) {
            System.out.println("A to get in active mode, P to get in passive mode");
            var mode = stdIn.readLine();
            if (mode.equals("A")) {
                var details = server.GetDirActive(dir);

                try (var socket = new Socket(details.GetIp(), details.GetPort())){
                    var in = new DataInputStream(socket.getInputStream());
                    var out = new DataOutputStream(socket.getOutputStream());

                    for (var fileName : details.GetFiles()) {
                        var file = new File(fileName);
                        if (file.exists()) {
                            continue;
                        }

                        out.writeUTF(fileName);
                        var response = in.readUTF();
                        if (!response.equals("continue")) {
                            continue;
                        }

                        FileUtility.ReceiveFile(in, file);
                    }
                    out.writeUTF("stop");
                } catch (IOException e) {
                    System.out.println("Cannot connect to server");
                    System.exit(1);
                }
            } else if (mode.equals("P")) {
                try (var socket = new ServerSocket()) {
                    socket.bind(null);

                    var files = server.GetDirPassive(dir, socket.getInetAddress(), socket.getLocalPort());
                    if (files.length == 0) {
                        System.out.println("No files to receive");
                        continue;
                    }

                    var serverSocket = socket.accept();
                    serverSocket.setSoTimeout(30000);

                    var in = new DataInputStream(serverSocket.getInputStream());
                    var out = new DataOutputStream(serverSocket.getOutputStream());

                    for (var fileName : files) {
                        var file = new File(fileName);
                        if (file.exists()) {
                            continue;
                        }

                        out.writeUTF(fileName);
                        var response = in.readUTF();
                        if (!response.equals("continue")) {
                            continue;
                        }

                        FileUtility.ReceiveFile(in, file);
                    }
                    out.writeUTF("stop");
                } catch (IOException e) {
                    System.out.println("Cannot connect to server");
                    System.exit(1);
                }
            }

            System.out.println("Select a folder to receive");
        }
    }
}
