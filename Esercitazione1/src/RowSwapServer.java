import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class RowSwapServer {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("RowSwapServer <discovery_server IP> <discovery_server port> <port> <file>");
            System.exit(1);
        }

        try{
            socket = new DatagramSocket(Integer.parseInt(args[2]));

            var buffer = new byte[1024];
            var packet = new DatagramPacket(buffer, buffer.length);
            packet.setData(args[3].getBytes());

            packet.setAddress(InetAddress.getByName(args[0]));
            packet.setPort(Integer.parseInt(args[1]));

            socket.send(packet);
        } catch (NumberFormatException | IOException e) {
            System.out.println("Errore registering to discovery server");
            System.exit(1);
        }

        while (true) {
            var buffer = new byte[1024];
            var packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                continue;
            }

            var request = new String(packet.getData(), StandardCharsets.UTF_8).trim();
            var tokens = request.split(";");

            if (tokens.length != 2) {
                continue;
            }

            int firstIndex, secondIndex;
            try {
                firstIndex = Integer.parseInt(tokens[0]);
                secondIndex = Integer.parseInt(tokens[1]);

                if (firstIndex > secondIndex) {
                    int temp = secondIndex;
                    secondIndex = firstIndex;
                    firstIndex = temp;
                }
            } catch (NumberFormatException e) {
                continue;
            }

            try {
                var file = new File(args[3]);
                var tmpFile = new File(args[3].concat(".tmp"));
                var reader = new BufferedReader(new FileReader(file));
                int i = 0;
                String line, firstLine = null, secondLine = null;
                while ((line = reader.readLine()) != null) {
                    if (i == firstIndex) {
                        firstLine = line;
                    } else if (i == secondIndex) {
                        secondLine = line;
                    }

                    i++;
                }

                reader.close();
                if (firstLine == null || secondLine == null) {
                    continue;
                }
                reader = new BufferedReader(new FileReader(file));

                i = 0;
                var tmpWriter = new BufferedWriter(new FileWriter(tmpFile));
                while ((line = reader.readLine()) != null) {
                    if (i < firstIndex) {
                        tmpWriter.write(line);
                    } else if (i == firstIndex) {
                        tmpWriter.write(secondLine);
                    } else if (i < secondIndex) {
                        tmpWriter.write(line);
                    } else if (i == secondIndex) {
                        tmpWriter.write(firstLine);
                    } else {
                        tmpWriter.write(line);
                    }

                    tmpWriter.newLine();

                    i++;
                }

                reader.close();
                tmpWriter.close();

                file.delete();
                tmpFile.renameTo(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static DatagramSocket socket;
}
