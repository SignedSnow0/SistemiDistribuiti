/* FileUtility.java */

import java.io.*;

public class FileUtility {
	/**
	 * Nota: sorgente e destinazione devono essere correttamente aperti e chiusi
	 * da chi invoca questa funzione.
	 *  
	 */
	static protected void trasferisci_a_byte_file_binario(DataInputStream src, DataOutputStream dest) throws IOException {
		// ciclo di lettura da sorgente e scrittura su destinazione
	    int buffer;    
	    try {
	    	// esco dal ciclo all lettura di un valore negativo -> EOF
	    	// N.B.: la funzione consuma l'EOF
	    	while ((buffer=src.read()) >= 0) {
	    		dest.write(buffer);
	    	}
	    	dest.flush();
	    }
	    catch (EOFException e) {
	    	System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    }
	}

	static protected void InviaFileSuRete(File src, DataOutputStream dest) throws IOException {
		try (var file = new FileInputStream(src)) {
			dest.writeLong(src.length());

			int buffer;
			while ((buffer = file.read()) >= 0) {
				dest.write(buffer);
			}
			dest.flush();
		}
	}

	static protected void RiceviFileDaRete(DataInputStream src, File dest) throws IOException {
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