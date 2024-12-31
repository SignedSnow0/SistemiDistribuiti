import java.io.*;
import java.net.*;

class FileThread extends Thread {
	public FileThread(Socket clientSocket) throws IOException {
		inStream = new DataInputStream(clientSocket.getInputStream());
		outStream = new DataOutputStream(clientSocket.getOutputStream());
	}

	public void run() {
		try {
			while (true) {
				var request = inStream.readUTF();
				var tokens = request.split(" ");
				if (tokens.length != 2) {
					continue;
				}

				if (tokens[0].equals("mput")) {
					try {
						var file = new File(tokens[1]);
						if (file.exists()) {
							outStream.writeUTF("already exists");
							return;
						}

						outStream.writeUTF("continue");
						FileUtility.ReceiveFile(inStream, file);

						outStream.writeUTF("success");
					} catch (IOException e) {
						outStream.writeUTF("error");
					}
				} else if (tokens[0].equals("mget")) {
					var dir = new File(tokens[1]);
					if (!dir.exists()) {
						outStream.writeUTF("does not exist");
						continue;
					}

					for (File file : dir.listFiles()) {
						if (file.isDirectory()) {
							continue;
						}

						outStream.writeUTF(file.getName());
						if (!inStream.readUTF().equals("continue")) {
							break;
						}

						FileUtility.SendFile(file, outStream);

						if (!inStream.readUTF().equals("continue")) {
							break;
						}
					}
					outStream.writeUTF("stop");
				}
			}
		} catch (IOException e) { }
	}

	private final DataInputStream inStream;
	private final DataOutputStream outStream;
}


