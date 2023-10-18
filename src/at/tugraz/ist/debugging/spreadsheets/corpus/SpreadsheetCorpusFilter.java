package at.tugraz.ist.debugging.spreadsheets.corpus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import at.tugraz.ist.util.fileManipulation.Directory;

/**
 * This class is used to filter a spreadsheet corpus. Small spreadsheets
 * (concering the number of formulas) and spreadsheets with the wrong format
 * (e.g. Excel 5.0) are filtered out.
 * 
 * @author bhofer
 * 
 */
public class SpreadsheetCorpusFilter {
	private static String destDirectory = "sampleSpreadsheets" + File.separator
			+ "TO_USE";

	private static List<String> files = new ArrayList<String>();
	private static int formulaSizeBound = 5;
	private static String logFile = "EUSES_evaluation.txt";
	private static String sourceDirectory = "sampleSpreadsheets";

	private static final String helpText = "Spreadsheet Corpus filtering program \n"
			+ "--> filters out spreadsheets with the wrong format and too small spreadsheets \n\n"
			+ "USAGE: \n"
			+ "  -help        --> prints this help message\n"
			+ "  -src=dir     --> sets the source directory\n"
			+ "  -dest=dir    --> sets the destination directory\n"
			+ "  -log=file    --> sets the logging file\n"
			+ "  -filter=num  --> spreadsheets containing up to 'num' formulas are sorted out\n\n"
			+ "DEFAULT VALUES: \n"
			+ "  src="
			+ sourceDirectory
			+ "\n"
			+ "  dest="
			+ destDirectory
			+ "\n"
			+ "  log="
			+ logFile
			+ "\n"
			+ "  filter=" + formulaSizeBound + "\n";

	private static Map<String, Integer> tooLessFormulas = new HashMap<String, Integer>();
	private static Map<String, Integer> toProcess = new HashMap<String, Integer>();

	private static List<String> wrongFormat = new ArrayList<String>();

	private static void copyFiles() {
		for (String fileName : toProcess.keySet()) {
			String split = File.separator;
			if (split.equalsIgnoreCase("\\"))
				split += split;
			String[] parts = fileName.split(split);
			String newFileName = destDirectory + File.separator
					+ parts[parts.length - 3] + File.separator
					+ parts[parts.length - 1];
			try {
				FileUtils.copyFile(new File(fileName), new File(newFileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		if (args.length == 0) {
			setUpTOUSEfolder();
			return;
		}
		if (args[0].equalsIgnoreCase("-help") || args[0].equalsIgnoreCase("-h")) {
			System.out.println(helpText);
			return;
		}
		for (int i = 0; i < args.length; i++) {
			String prefix = args[i].substring(1, args[i].indexOf("="));
			String suffix = args[i].substring(args[i].indexOf("=") + 1);
			switch (prefix) {
			case "src":
				sourceDirectory = suffix;
				continue;
			case "dest":
				destDirectory = suffix;
				continue;
			case "log":
				logFile = suffix;
				continue;
			case "filter":
				formulaSizeBound = Integer.parseInt(suffix);
				continue;

			default:
				System.err.println("Option " + prefix + " unknown!");
				System.out.println(helpText);
				break;
			}
		}

		setUpTOUSEfolder();

	}

	private static void saveResults() {
		String fileName = logFile;
		File file = new File(fileName);
		BufferedWriter fW = null;

		try {
			if (!file.exists()) {
				file.createNewFile();

			}
			fW = new BufferedWriter(new FileWriter(fileName, false));

			fW.write("WRONG FORMAT (" + wrongFormat.size() + ")\n");
			for (String str : wrongFormat) {
				fW.write(str + "\n");
			}

			fW.write("\nTO LESS FORMULAS (" + tooLessFormulas.size() + ")\n");
			for (String str : tooLessFormulas.keySet()) {
				fW.write(str + " (" + tooLessFormulas.get(str) + ")\n");
			}

			fW.write("\nCOULD BE USED FOR FURHTER PROCESSING ("
					+ toProcess.size() + ")\n");
			for (String str : toProcess.keySet()) {
				fW.write(str + " (" + toProcess.get(str) + ")\n");
			}
			fW.flush();

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private static void setUpTOUSEfolder() {
		files = Directory.getFilesRecursively(sourceDirectory, ".xls");
		files.addAll(Directory.getFilesRecursively(sourceDirectory, ".xlsx"));
		FileInputStream fip = null;

		for (String file : files) {

			try {
				fip = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(fip);
				int formulaCounter = 0;
				for (int i = 0; i < wb.getNumberOfSheets(); i++) {
					Sheet sheet = wb.getSheetAt(i);

					Iterator<Row> rowIterator = sheet.rowIterator();
					while (rowIterator.hasNext()) {

						Row row = rowIterator.next();
						if (row == null) {
							continue;
						}

						Iterator<Cell> cellIterator = row.cellIterator();

						while (cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							if (cell == null)
								continue;

							if (cell.getCellTypeEnum() == CellType.FORMULA) {
								formulaCounter++;
							}

						}

					}
				}
				if (formulaCounter > formulaSizeBound) {
					toProcess.put(file, formulaCounter);
				} else {
					tooLessFormulas.put(file, formulaCounter);
				}
			} catch (OldExcelFormatException e) {
				wrongFormat.add(file);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				wrongFormat.add(file);
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println(file);
				wrongFormat.add(file);
				if (fip != null) {
					try {
						fip.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				e.printStackTrace();
			}
		}
		saveResults();
		copyFiles();
	}

}
