import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class FileClient {
    public static void main(String[] args) {
        try {
            var address = InetAddress.getByName(args[0]);
            var port = Integer.parseInt(args[1]);

            socket = new Socket(address, port);

            inSocket = new DataInputStream(socket.getInputStream());
            outSocket = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Errore parsing argomenti di ingresso");
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("PutFileClient Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure scegli azione:");

        try {
            String line = null;
            while ((line = stdIn.readLine()) != null) {
                var tokens = line.split(" ");
                if (tokens.length != 2) {
                    continue;
                }

                if (tokens[0].equals("mget")) {
                    MGet(tokens[1]);
                } else if (tokens[0].equals("mput")) {
                    MPut(tokens[1]);
                }
            }
        } catch (IOException e) {

        }
    }

    private static void MGet(String path) throws IOException {
        var folder = new File(path);
        if (folder.isDirectory()) {
            return;
        }

        if (!folder.mkdir()) {
            return;
        }

        outSocket.writeUTF("mget " + folder.getName());

        var newFile = inSocket.readUTF();
        while (!newFile.equals("stop")) {
            var file = new File(folder.getAbsoluteFile() + "/" + newFile);
            file.createNewFile();

            FileUtility.RiceviFileDaRete(inSocket, file);

            outSocket.writeUTF("continue");

            newFile = inSocket.readUTF();
        }
    }

    private static void MPut(String path) throws IOException {
        var folder = new File(path);
        if (!folder.isDirectory()) {
            return;
        }

        for (var file : folder.listFiles()) {
            if (!file.isFile()) {
                continue;
            }

            outSocket.writeUTF("mput " + file.getName());

            var response = inSocket.readUTF();
            if (!response.equals("attiva")) {
                continue;
            }

            //outSocket.writeLong(file.length());
            FileUtility.InviaFileSuRete(file, outSocket);

            response = inSocket.readUTF();
        }
    }



    private static Socket socket;
    private static DataInputStream inSocket;
    private static DataOutputStream outSocket;
}
