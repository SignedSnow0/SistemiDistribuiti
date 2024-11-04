// PutFileServer Concorrente

import java.io.*;
import java.net.*;

// Thread lanciato per ogni richiesta accettata
// versione per il trasferimento di file binari
class FileThread extends Thread {
	private final Socket clientSocket;
	private final DataInputStream inSock;
	private final DataOutputStream outSock;

	/**
	 * Constructor
	 * @param clientSocket
	 */
	public FileThread(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		inSock = new DataInputStream(clientSocket.getInputStream());
		outSock = new DataOutputStream(clientSocket.getOutputStream());
	}

	public void run() {
		try {
			while (true) {
				var request = inSock.readUTF();
				var tokens = request.split(" ");
				if (tokens.length != 2) {
					continue;
				}

				if (tokens[0].equals("mput")) {
					try {
						MPut(tokens[1]);

						outSock.writeUTF("Successo");
					} catch (IOException e) {
						outSock.writeUTF("Errore");
					}

				} else if (tokens[0].equals("mget")) {
					MGet(tokens[1]);
				}
			}
		} catch (IOException e) {

		}
	} // run

	private void MGet(String path) throws IOException {
		var dir = new File(path);
		if (!dir.exists()) {
			outSock.writeUTF("stop");
		}

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}

			outSock.writeUTF(file.getName());

			FileUtility.InviaFileSuRete(file, outSock);

			if (!inSock.readUTF().equals("continue")) {
				break;
			}
		}
	}

	private void MPut(String path) throws IOException {
		var file = new File(path);
		if (file.isFile()) {
			outSock.writeUTF("salta file");
			return;
		}

		outSock.writeUTF("attiva");
		FileUtility.RiceviFileDaRete(inSock, file);
	}
} // PutFileServerThread class


