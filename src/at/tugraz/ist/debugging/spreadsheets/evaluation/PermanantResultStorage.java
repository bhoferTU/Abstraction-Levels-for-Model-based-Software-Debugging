package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PermanantResultStorage {

	public static BufferedWriter setUp(String header, String fileName,
			boolean append) {
		File file = new File(fileName);
		BufferedWriter fW = null;
		boolean created = false;
		try {
			if (!file.exists()) {
				file.createNewFile();
				created = true;
			}
			fW = new BufferedWriter(new FileWriter(fileName, append));
			if (created && header != null) {
				fW.write(header);
				fW.flush();
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return fW;
	}

	private String fileName = "";

	BufferedWriter fileWriter = null;

	public PermanantResultStorage() {
		super();
	}

	public PermanantResultStorage(String fileName) {
		super();
		this.fileName = fileName;
	}

	@Override
	public void finalize() {
		try {
			if (fileWriter != null) {
				fileWriter.close();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void storeResult(Result result) {
		try {

			if (fileWriter == null) {
				fileWriter = PermanantResultStorage.setUp(
						Result.getColumnHeader(), fileName, true);
			}

			fileWriter.write(result.toString());
			fileWriter.flush();

		} catch (IOException ex) {
			System.err
					.println("Error when creating results file / writing to file! "+result.toString());
		}

	}
}
