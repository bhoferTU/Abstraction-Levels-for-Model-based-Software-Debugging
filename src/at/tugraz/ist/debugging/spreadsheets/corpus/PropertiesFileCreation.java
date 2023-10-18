package at.tugraz.ist.debugging.spreadsheets.corpus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICellContainer;
import at.tugraz.ist.util.datastructures.Pair;

public class PropertiesFileCreation {

	public enum FaultTypes {
		AOR, // Continuous Range Shrinking
		CRP, // Reference Replacement
		CRR, // Arithmetic Operator Replacement
		CRS, // Relational Operator Replacement
		FFR, // Constants Replacement
		FRC, // Constants for Reference Replacement
		RFR, // Formula Replacement with Constant
		ROR, // Formula Function Replacement
		UKN // Unknown
	}

	private static boolean allowDoubles = true;
	private static int created = 0;
	private static List<Coords> differentFormulas;
	private static List<Coords> differentValues;
	private static int exception = 0;

	private static Map<Coords, String> expectedValues;
	private static Map<Coords, FaultTypes> faults;

	private static Set<Coords> outputCells;

	public static boolean containsWrongOutputCell(String originalFile, Workbook wb2) throws Exception {
		ICellContainer cells = CellContainer.create(originalFile);
		Set<Coords> outputCells = cells.getOutputCoords();
		Workbook wb = getWorkbook(originalFile).getFirst();

		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			Sheet sheet2 = wb2.getSheetAt(i);
			Iterator<Row> rowIterator = sheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Row row2 = sheet2.getRow(row.getRowNum());

				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					Cell cell2 = row2.getCell(cell.getColumnIndex());
					if (cell.getCellTypeEnum() != CellType.FORMULA)
						continue;

					Coords dCell = getCoordsForCell(cell);
					if (outputCells.contains(dCell) == false)
						continue;

					if (cell.getCachedFormulaResultTypeEnum() == CellType.NUMERIC
							&& cell2.getCachedFormulaResultTypeEnum() == CellType.NUMERIC) {
						if (!equal(cell.getNumericCellValue(), cell2.getNumericCellValue())) {
							return true;
						}
					}

					else if (cell.getCachedFormulaResultTypeEnum() == CellType.STRING
							&& cell2.getCachedFormulaResultTypeEnum() == CellType.STRING) {
						if (!cell.getStringCellValue().equalsIgnoreCase(cell2.getStringCellValue())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean createPropertiesFile(String propertiesFile, String originalExcelFile,
			String faultyExcelFile) {

		if (findDifferences(originalExcelFile, faultyExcelFile) == false){
			System.out.println("No differences in files "+originalExcelFile+", "+faultyExcelFile);
			return false;
		}
			

		File file = new File(propertiesFile);
		BufferedWriter fW = null;

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			// String filepath = faultyExcelFile.replace("\\", "\\\\");
			// filepath = filepath.substring(32, filepath.length());
			fW = new BufferedWriter(new FileWriter(propertiesFile, false));

			fW.write("# simple configuration file\n");
			fW.write("# notation: worksheet!column!row, e.g. 0!A!3\n");
			fW.write("#           worksheet starts with 0\n");
			fW.write("#           column starts with A (see Excel notation)\n");
			fW.write("#           row starts with 1\n\n");
			fW.write("EXCEL_SHEET=" + getRelativeExcelFilePath(faultyExcelFile, propertiesFile) + "\n\n");
			// fW.write("EXCEL_SHEET=" + faultyExcelFile.replace("\\", "\\\\") +
			// "\n\n");
			int correct = 0;
			int incorrect = 0;

			for (Coords cell : outputCells) {
				if (!hasFaultyValue(cell)) {
					fW.write("CORRECT_OUTPUT_" + (++correct) + "=" + cell.getCSVString() + "\n");
				}
			}

			fW.write("\n");

			for (Coords cell : differentValues) {
				fW.write("INCORRECT_OUTPUT_" + (++incorrect) + "=" + cell.getCSVString() + "\n");
				if (expectedValues.get(cell) != null) {
					Double value = Double.parseDouble(expectedValues.get(cell));
					if (equal(value, new Double(value.intValue())))
						fW.write("INCORRECT_OUTCELL_EXPECTED_VALUE_" + incorrect + "="
								+ new Double(Double.parseDouble(expectedValues.get(cell))).intValue() + "\n");
					else if (allowDoubles)
						fW.write("INCORRECT_OUTCELL_EXPECTED_VALUE_" + incorrect + "=" + expectedValues.get(cell)
								+ "\n");
					else {
						fW.flush();
						fW.close();
						throw new Exception("no doubles are allowed");
					}
				}
			}
			fW.write("\n");

			int faulty = 0;
			for (Coords cell : faults.keySet()) {

				fW.write("FAULTY_CELLS_" + (++faulty) + "=" + cell.getCSVString() + "\n");
				fW.write("FAULT_TYPE_" + faulty + "=" + faults.get(cell) + "\n");
			}

			fW.flush();
			fW.close();
			created++;
			return true;

		} catch (Exception e) {
			// System.err.println(e.getMessage());
			// e.printStackTrace();
			exception++;
			return false;
		}

	}

	private static String getRelativeExcelFilePath(String absolutePath, String absolutePosition) {
		// System.out.println(absolutePath);
		// System.out.println(absolutePosition);

		String[] pathTokens = absolutePath.split("\\\\");
		String[] pathTokens2 = absolutePosition.split("\\\\");
		String path = "";
		int i = 0;
		for (i = 0; i < pathTokens.length; i++) {
			if (!pathTokens[i].equalsIgnoreCase(pathTokens2[i])) {
				break;
			}
		}
		for (int j = 0; j < pathTokens2.length - i - 1; j++) {
			path += "..\\\\";
		}
		for (int j = i; j < pathTokens.length; j++) {
			path += pathTokens[j];
			if (j < pathTokens.length - 1)
				path += "\\\\";
		}

		// System.out.println(path);
		// System.out.println("**************************");
		return path;
	}

	public static boolean createPropertiesFile(String propertiesFile, String originalExcelFile, String faultyExcelFile,
			Map<Coords, FaultTypes> faults) {
		PropertiesFileCreation.faults = faults;
		return createPropertiesFile(propertiesFile, originalExcelFile, faultyExcelFile);
	}

	public static boolean createPropertiesFile(String propertiesFile, String originalExcelFile, String faultyExcelFile,
			Map<Coords, String> faults, boolean tableNameCorrected) {
		Map<Coords, FaultTypes> converted = new HashMap<Coords, FaultTypes>();
		for (Entry<Coords, String> entry : faults.entrySet())
			converted.put(entry.getKey(), FaultTypes.valueOf(entry.getValue()));
		return createPropertiesFile(propertiesFile, originalExcelFile, faultyExcelFile, converted);
	}

	private static boolean equal(double num1, double num2) {
		double diff = num1 - num2;
		diff = Math.abs(diff);
		if (diff < 0.1)
			return true;
		return false;
	}

	private static boolean findDifferences(String originalExcelFile, String faultyExcelFile) {
		try {

			Pair<Workbook, FormulaParsingWorkbook> workbooks1 = getWorkbook(originalExcelFile);
			Workbook wb = workbooks1.getFirst();
			Pair<Workbook, FormulaParsingWorkbook> workbooks2 = getWorkbook(faultyExcelFile);
			Workbook wb2 = workbooks2.getFirst();

			boolean fillFaults = false;
			differentFormulas = new ArrayList<Coords>();
			differentValues = new ArrayList<Coords>();
			expectedValues = new HashMap<Coords, String>();
			if (faults == null) {
				faults = new HashMap<Coords, FaultTypes>();
				fillFaults = true;
			}

			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
			evaluator.evaluateAll();
			FormulaEvaluator evaluator2 = wb2.getCreationHelper().createFormulaEvaluator();
			evaluator2.evaluateAll();
			
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				Sheet sheet = wb.getSheetAt(i);
				Sheet sheet2 = wb2.getSheetAt(i);
				Iterator<Row> rowIterator = sheet.rowIterator();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Row row2 = sheet2.getRow(row.getRowNum());

					Iterator<Cell> cellIterator = row.cellIterator();
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						Cell cell2 = row2.getCell(cell.getColumnIndex());
						switch (cell.getCellTypeEnum()) {
						case FORMULA:
							Coords dCell = getCoordsForCell(cell);
							switch (evaluator.evaluateFormulaCell(cell)) {
							case Cell.CELL_TYPE_NUMERIC:
								if (!equal(cell.getNumericCellValue(), cell2.getNumericCellValue())) {
									differentValues.add(dCell);
									expectedValues.put(dCell, Double.toString(cell.getNumericCellValue()));
								}
								break;

							case Cell.CELL_TYPE_STRING:

								if (cell.getCachedFormulaResultTypeEnum() == CellType.STRING) {
									if (!cell.getStringCellValue().equalsIgnoreCase(cell2.getStringCellValue())) {
										differentValues.add(dCell);
										expectedValues.put(dCell, cell.getStringCellValue());
									}
								}
								break;

							}

							if (!cell.getCellFormula().equalsIgnoreCase(cell2.getCellFormula())) {
								differentFormulas.add(dCell);
								if (fillFaults)
									faults.put(dCell, FaultTypes.UKN);
								/*
								 * else if (faults.get(dCell) == null) throw new
								 * Exception("error in cell " + faultyExcelFile
								 * + ":" + dCell + " is not defined in faults");
								 * } else if (faults.get(dCell) != null) { throw
								 * new Exception("error in cell " +
								 * faultyExcelFile + ":" + dCell +
								 * " does not exists");
								 */
							}

						default:

						}
					}
				}
			}

			ICellContainer cells = CellContainer.create(faultyExcelFile);
			outputCells = cells.getOutputCoords();
			List<Coords> differentOutputValues = new ArrayList<Coords>();
			for (Coords cell : differentValues) {
				if (outputCells.contains(cell)) {
					differentOutputValues.add(cell);
				}
			}
			differentValues = differentOutputValues;

			if (differentFormulas.size() == 0) {
				// System.err.println("# No difference found for file " +
				// originalExcelFile);
				// for (Coordinates fault : faults.keySet())
				// System.err.println(" " + fault + " (" +
				// faults.get(fault) + ")");
				return false;
			}

			if (differentValues.size() == 0) {
				// System.err.println("# No wrong output values for file " +
				// originalExcelFile);
				// for (Coordinates fault : faults.keySet())
				// System.err.println(" " + fault + " (" +
				// faults.get(fault) + ")");
				return false;
			}

			return true;

		} catch (Exception e) {
			exception++;
			// System.err.println("Exception while reading file " +
			// originalExcelFile);
			// System.err.println(e.getMessage());
			// e.printStackTrace();
			return false;
		}
	}

	private static Coords getCoordsForCell(Cell cell) {

		return new Coords(cell.getSheet().getWorkbook().getSheetIndex(cell.getSheet().getSheetName()), // worksheet
																										// index
				cell.getRowIndex(), cell.getColumnIndex()); // row and column
															// index
	}

	public static Pair<Workbook, FormulaParsingWorkbook> getWorkbook(String file)
			throws InvalidFormatException, IOException {

		try {
			FileInputStream fip = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(fip);
			fip = new FileInputStream(file);
			FormulaParsingWorkbook fpw = null;

			if (file.endsWith(".xlsx")) {
				XSSFWorkbook xWb = new XSSFWorkbook(fip);
				fpw = XSSFEvaluationWorkbook.create(xWb);
			} else if (file.endsWith(".xls")) {
				HSSFWorkbook hWb = new HSSFWorkbook(fip);
				fpw = HSSFEvaluationWorkbook.create(hWb);
			} else {
				System.err.println("# File format not supported for file " + file);
			}
			return new Pair<Workbook, FormulaParsingWorkbook>(wb, fpw);
		} catch (Exception e) {
			System.err.println("HOBI: " + e.toString());
			return null;
		}

	}

	private static Boolean hasFaultyValue(Coords cell) {
		return differentValues.contains(cell);
		/*
		 * //no longer necessary, since Coords can be compared. for (Coords
		 * faultyCell : differentValues) { if
		 * (faultyCell.getCSVString().equalsIgnoreCase(cell.getCSVString()))
		 * return true; } return false;
		 */
	}

	public static void main(String[] args) {

		File baseDir = new File("test\\oldFiles\\");
		try {
			for (File file : baseDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File fileName) {
					String path = fileName.getPath();
					return path.contains("_Fault") && path.endsWith(".properties") == false;
				}
			})) {
				String faultyExcelFile = file.getPath();
				String originalExcelFile = faultyExcelFile.replaceAll("_Fault\\d*", "");
				String propertiesFile = faultyExcelFile.replaceAll("xlsx?$", "properties");

				Map<Coords, FaultTypes> faults = null;// FaultDatabase.get(faultyExcelFile);
				// TODO: why was this needed?
				// CoordsToCellMap.getInstance().populate(originalExcelFile);
				PropertiesFileCreation.createPropertiesFile(propertiesFile, originalExcelFile, faultyExcelFile, faults);
				System.out.println("generate " + propertiesFile);
			}
			System.out.println("\ndone");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printStat() {
		System.out.println("");
		System.out.println("Exceptions:     " + exception);
		System.out.println("Created:        " + created);
	}
}
