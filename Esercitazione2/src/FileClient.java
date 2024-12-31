import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class FileClient {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Client <server IP> <server port>");
            System.exit(1);
        }

        try {
            var address = InetAddress.getByName(args[0]);
            var port = Integer.parseInt(args[1]);

            socket = new Socket(address, port);

            inSocket = new DataInputStream(socket.getInputStream());
            outSocket = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Client <server IP> <server port>");
            System.exit(1);
        }

        var stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("[mget <dir>] to get a directory | [mput <dir>] to put a directory");

        String line = null;
        while ((line = stdIn.readLine()) != null) {
            var tokens = line.split(" ");
            if (tokens.length != 2) {
                System.out.println("[mget <dir>] to get a directory | [mput <dir>] to put a directory");
                continue;
            }

            if (tokens[0].equals("mget")) {
                var folder = new File(tokens[1]);
                if (folder.isDirectory()) {
                    System.out.println("Directory already exists");
                    continue;
                }

                if (!folder.mkdir()) {
                    System.out.println("Error creating directory");
                    continue;
                }

                outSocket.writeUTF("mget " + folder.getName());
                if (inSocket.readUTF().equals("does not exist")) {
                    System.out.println("Directory does not exist");
                    continue;
                }

                var newFile = inSocket.readUTF();
                while (!newFile.equals("stop")) {
                    var file = new File(folder.getAbsoluteFile() + "/" + newFile);
                    if (!file.createNewFile()) {
                        System.out.println("Error creating file");
                        outSocket.writeUTF("stop");
                        continue;
                    }

                    outSocket.writeUTF("continue");
                    FileUtility.ReceiveFile(inSocket, file);

                    outSocket.writeUTF("continue");

                    newFile = inSocket.readUTF();
                }
            } else if (tokens[0].equals("mput")) {
                var folder = new File(tokens[1]);
                if (!folder.isDirectory()) {
                    System.out.println("Directory does not exist");
                    continue;
                }

                for (var file : folder.listFiles()) {
                    if (!file.isFile()) {
                        continue;
                    }

                    outSocket.writeUTF("mput " + file.getName());
                    if (inSocket.readUTF().equals("already exists")) {
                        System.out.println("File already exists");
                        continue;
                    }

                    var response = inSocket.readUTF();
                    if (!response.equals("continue")) {
                        continue;
                    }

                    FileUtility.SendFile(file, outSocket);

                    response = inSocket.readUTF();
                    if (!response.equals("success")) {
                        System.out.println("Error sending file");
                    }
                }
            }

            System.out.println("[mget <dir>] to get a directory | [mput <dir>] to put a directory");
        }
    }

    private static Socket socket;
    private static DataInputStream inSocket;
    private static DataOutputStream outSocket;
}
