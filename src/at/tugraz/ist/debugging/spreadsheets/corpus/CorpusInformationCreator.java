package at.tugraz.ist.debugging.spreadsheets.corpus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICellContainer;
import at.tugraz.ist.debugging.spreadsheets.util.CorpusLocation;
import at.tugraz.ist.util.fileManipulation.Directory;

public class CorpusInformationCreator {

	//private static String PATH = "Benchmarks\\Configuration_files";
	private static String PATH = CorpusLocation.PATH_ISCAS85+CorpusLocation.config;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {


		String resultsFile = "SpreadsheetInfo_ISCAS85_new.csv";
		File file = new File(resultsFile);
		
		if (file.exists())
		{
			System.out.println("File already exists!");
			return;
		}

		List<String> files = Directory.getFilesRecursively(PATH, ".properties");
		
		BufferedWriter writer = null;
		boolean fileExists = false;
		if (file.exists()) {
			fileExists = true;
		}

		try {
			writer = new BufferedWriter(new FileWriter(resultsFile, true));
			if (!fileExists) {
				writer.write(SpreadsheetInformation.getHeader());
			}

			for (String propertiesFile : files) {
				System.out.println(propertiesFile);
//				if(!propertiesFile.contains("DB_Admin"))
//					continue;
				
				if (propertiesFile
						.contains("hw8_1FAULTS_FAULTVERSION1.properties")
						|| propertiesFile
								.contains("hw8_1FAULTS_FAULTVERSION4.properties"))
					continue;
				SpreadsheetProperties properties = new SpreadsheetProperties(
						propertiesFile);
				ICellContainer container = CellContainer.create(properties
						.getExcelSheetPath());

				SpreadsheetInformation info = new SpreadsheetInformation(
						properties, container);
				writer.write(info.toString());
				writer.flush();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpreadsheetPropertiesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// Silent catch
				e.printStackTrace();
			}

		}

	}

}
