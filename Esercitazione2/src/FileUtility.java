import java.io.*;

public class FileUtility {
	static protected void SendFile(File src, DataOutputStream dest) throws IOException {
		try (var file = new FileInputStream(src)) {
			dest.writeLong(src.length());

			int buffer;
			while ((buffer = file.read()) >= 0) {
				dest.write(buffer);
			}
			dest.flush();
		}
	}

	static protected void ReceiveFile(DataInputStream src, File dest) throws IOException {
		try (var file = new FileOutputStream(dest)) {
			var size = src.readLong();

			int read = 0;
			while (read < size) {
				file.write(src.read());
				read++;
			}
		}
	}
}
