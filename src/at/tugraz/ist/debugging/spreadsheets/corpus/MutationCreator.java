package at.tugraz.ist.debugging.spreadsheets.corpus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AddPtg;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.DividePtg;
import org.apache.poi.ss.formula.ptg.EqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterEqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterThanPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.LessEqualPtg;
import org.apache.poi.ss.formula.ptg.LessThanPtg;
import org.apache.poi.ss.formula.ptg.MultiplyPtg;
import org.apache.poi.ss.formula.ptg.NotEqualPtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.formula.ptg.ScalarConstantPtg;
import org.apache.poi.ss.formula.ptg.SubtractPtg;
import org.apache.poi.ss.formula.ptg.UnaryMinusPtg;
import org.apache.poi.ss.formula.ptg.UnaryPlusPtg;
import org.apache.poi.ss.formula.ptg.ValueOperatorPtg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.exceptions.CoordinatesException;
import at.tugraz.ist.debugging.spreadsheets.util.PoiExtensions;
import at.tugraz.ist.util.IO.OutputConfigurator;
import at.tugraz.ist.util.datastructures.Pair;
import at.tugraz.ist.util.fileManipulation.Directory;

public class MutationCreator {

	private static CellStyle faultyCellStyle = null;
	private static CellStyle outputCellStyleCorrect = null;
	private static CellStyle outputCellStyleWrong = null;

	// public static String category = "homework";

	public static String configDir = "Benchmarks" + File.separator + "Iulia" + File.separator + "SEEDED"
			+ File.separator + "PropertiesFiles" + File.separator;
	// "configurationFiles" + File.separator
	// + category + File.separator;
	public static String destDir = "Benchmarks" + File.separator + "Iulia" + File.separator + "SEEDED" + File.separator
	// "sampleSpreadsheets" + File.separator
	// + "TO_USE" + File.separator + category + File.separator
			+ "ExcelFiles" + File.separator;
	public static Integer faultsPerVersion = 3;

	public static String logFile = "MutationCreation_homework.log";
	public static Integer numberOfFaultyVersions = 5;

	public static faultNumberMode faultNumMode = faultNumberMode.UPTO;

	public enum faultNumberMode {
		EXACT, UPTO
	};

	// public static int minValue = -2000;
	// public static int maxValue = 5000;
	// private static int minFunc = -1;
	// private static int maxFunc = -1;
	// public static boolean allowDoubles = true;

	public static String sourceDir = "Benchmarks" + File.separator + "Iulia" + File.separator + "SumCircExcelFiles"
			+ File.separator;
	// "sampleSpreadsheets" + File.separator
	// + "TO_USE" + File.separator + category + File.separator;

	private static final String helpText = "spreadsheet mutating program \n" + "--> mutates the given spreadsheets \n\n"
			+ "USAGE: \n" + "  -help          --> prints this help message\n"
			+ "  -src=dir       --> sets the source directory\n"
			+ "  -dest=dir      --> sets the destination directory for the generated mutants\n"
			+ "  -config=dir    --> sets the destination directory for the generated properties files\n"
			+ "  -log=file      --> sets the logging file\n"
			+ "  -versions=num  --> number of versions that should be created\n"
			+ "  -faults=num    --> number of faults per version\n\n" + "DEFAULT VALUES: \n" + "  src=" + sourceDir
			+ "\n" + "  dest=" + destDir + "\n" + "  config=" + configDir + "\n" + "  log=" + logFile + "\n"
			+ "  versions=" + numberOfFaultyVersions + "\n" + "   faults=" + faultsPerVersion + "\n"
			+ " faultNumerMode=" + faultNumMode;

	public static void createFaultyVersions(String originalFile, int numberFaultyVersions, int faultsPerVersion) {
		File dir = new File(configDir);
		if (!dir.exists() || dir.isDirectory()) {
			dir.mkdirs();
		}

		for (int i = 1; i <= numberFaultyVersions; i++) {
			System.out.println("** VERSION: " + i);
			String faultFile = destDir;
			faultFile += originalFile.substring(originalFile.lastIndexOf(File.separator) + 1,
					originalFile.indexOf("."));
			faultFile += "_" + faultsPerVersion + "FAULTS";
			faultFile += "_FAULTVERSION";
			faultFile += i + originalFile.substring(originalFile.indexOf("."));
			boolean success = false;
			int counter = 0;
			while (!success && counter < 100) {
				Map<Coords, String> faults = seedFault(originalFile, faultFile, faultsPerVersion);
				if (faults == null)
					break;

				String fileName = faultFile.substring(faultFile.lastIndexOf(File.separator) + 1, faultFile.indexOf('.'))
						+ ".properties";

				String propertiesFile = configDir + fileName;

				success = PropertiesFileCreation.createPropertiesFile(propertiesFile, originalFile, faultFile, faults,
						false);
				counter++;
			}
		}
	}

	private static String getName(Cell cell) {
		return cell.getSheet().getWorkbook().getSheetIndex(cell.getSheet()) + "!"
				+ PoiExtensions.getColumnString(cell.getColumnIndex()) + "!" + (cell.getRowIndex() + 1);
		// return cell.getSheet().getWorkbook().getSheetIndex(cell.getSheet())
		// + "!" + cell.getRowIndex() + "!" + cell.getColumnIndex();
		// return
		// cell.getSheet().getWorkbook().getSheetIndex(cell.getSheet().getSheetName())
		// + "!" + XlsColConverter.numberToLabel(cell.getColumnIndex()) + "!" +
		// (cell.getRowIndex()+1);
	}

	public static Pair<Workbook, FormulaParsingWorkbook> getWorkbook(String file)
			throws InvalidFormatException, IOException {

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
			System.err.println("not suppored file format");
		}

		faultyCellStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setColor(IndexedColors.RED.getIndex());
		font.setBold(true);
		faultyCellStyle.setFont(font);

		outputCellStyleCorrect = wb.createCellStyle();
		outputCellStyleCorrect.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		outputCellStyleCorrect.setFillPattern(CellStyle.SOLID_FOREGROUND);

		outputCellStyleWrong = wb.createCellStyle();
		outputCellStyleWrong.setFillForegroundColor(IndexedColors.RED.getIndex());
		outputCellStyleWrong.setFillPattern(CellStyle.SOLID_FOREGROUND);

		return new Pair<Workbook, FormulaParsingWorkbook>(wb, fpw);
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String prefix = args[i].substring(1, args[i].indexOf("="));
			String suffix = args[i].substring(args[i].indexOf("=") + 1);
			switch (prefix) {
			case "help":
				System.out.println(helpText);
				continue;
			case "src":
				sourceDir = suffix;
				continue;
			case "dest":
				destDir = suffix;
				continue;
			case "config":
				configDir = suffix;
				continue;
			case "log":
				logFile = suffix;
				continue;
			case "versions":
				numberOfFaultyVersions = Integer.parseInt(suffix);
				continue;
			case "faults":
				faultsPerVersion = Integer.parseInt(suffix);
				continue;
			default:
				System.err.println("Option " + prefix + " unknown!");
				System.out.println(helpText);
				break;
			}

		}

		OutputConfigurator.setOutputAndErrorStreamToFile(logFile);
		List<String> files = Directory.getFilesRecursively(sourceDir, ".xls");
		files.addAll(Directory.getFilesRecursively(sourceDir, ".xlsx"));

		for (String file : files) {
			System.out.println(file);
			if (faultNumMode == faultNumberMode.UPTO) {
				for (int i = 1; i <= faultsPerVersion; i++) {
					createFaultyVersions(file, numberOfFaultyVersions, i);
				}
			} else
				createFaultyVersions(file, numberOfFaultyVersions, faultsPerVersion);
		}

		PropertiesFileCreation.printStat();
	}

	private static Boolean mutateToken(Map<Coords, String> faultTypes, Cell cell, String cellName, String formula,
			Ptg token, CellContainer cells) {
		boolean changed = false;
		try {

			Random rand = new Random();
			// if (token instanceof AttrPtg || token instanceof FuncVarPtg) {
			// changed = replaceFormulaFunction(token, formula, cell);
			// if (changed)
			//
			// faultTypes.put(new Coords(cellName), "FFR");
			//
			// }
			if (token instanceof RefPtgBase && !(token instanceof Ref3DPtg)) {
				Boolean constant = rand.nextBoolean();
				if (constant) {
					changed = replaceReferenceWithConstant((RefPtgBase) token, formula, cell);
					if (changed)
						faultTypes.put(new Coords(cellName), "CRR");
				} else {
					changed = replaceReferenceWithAnotherReference(((RefPtgBase) token), formula, cell, cells);
					if (changed)
						faultTypes.put(new Coords(cellName), "RFR");
				}
			} else if (token instanceof AreaPtgBase && !(token instanceof Area3DPtg)) {
				changed = shrinkContinuousRange((AreaPtgBase) token, formula, cell);
				if (changed)
					faultTypes.put(new Coords(cellName), "CRS");
			} else if (token instanceof ScalarConstantPtg) {
				changed = replaceConstant(((ScalarConstantPtg) token), formula, cell);
				if (changed)
					faultTypes.put(new Coords(cellName), "CRP");
			} else if (token instanceof ValueOperatorPtg) {
				changed = replaceOperator(((ValueOperatorPtg) token), formula, cell);
				if (changed && (token instanceof AddPtg || token instanceof SubtractPtg || token instanceof MultiplyPtg
						|| token instanceof DividePtg)) {
					faultTypes.put(new Coords(cellName), "AOR");
				} else if (changed) {
					faultTypes.put(new Coords(cellName), "ROR");
				}
			}
			cell.setCellStyle(faultyCellStyle);
		} catch (CoordinatesException e) {
			e.printStackTrace();
		}
		return changed;
	}

	private static Map<Integer, Cell> readCellsFromWorkbook(Workbook wb) {
		Integer counter = 0;
		Map<Integer, Cell> cells = new HashMap<Integer, Cell>();
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			Iterator<Row> rowIterator = sheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch (cell.getCellTypeEnum()) {
					case FORMULA:
						cells.put(counter++, cell);
					default:
						// nothing;
					}
				}
			}
		}
		return cells;
	}

	public static boolean recalculateAllFormulas(Workbook wb) {
		System.out.println("Recalculating all formulas");
		try {
			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
			for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
				Sheet sheet = wb.getSheetAt(sheetNum);
				for (Row r : sheet) {
					for (Cell c : r) {
						evaluator.evaluate(c);
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}

	// CRP
	private static boolean replaceConstant(ScalarConstantPtg token, String formula, Cell cell) {
		// System.out.println("+++ CRP +++");
		Random rand = new Random();
		if (token instanceof IntPtg) {
			Integer value = ((IntPtg) token).getValue();
			Integer newValue = value + rand.nextInt(1000) + 1;
			formula = replaceConstant(value.toString(), newValue.toString(), formula);
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof NumberPtg) {
			Double value = ((NumberPtg) token).getValue();
			Double newValue = value + rand.nextDouble() + 1;
			formula = replaceConstant(value.toString(), newValue.toString(), formula);
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof BoolPtg) {
			Boolean value = ((BoolPtg) token).getValue();
			Boolean newValue = !value;
			formula = replaceConstant(value.toString(), newValue.toString(), formula);
			cell.setCellFormula(formula);
			return true;
		}
		return false;
	}

	protected static String replaceConstant(String oldValue, String newValue, String formula) {
		CharSequence dollar = "$";
		CharSequence dollar2 = "\\$";
		oldValue = oldValue.replace(dollar, dollar2);
		String[] tokens = formula.split(oldValue);
		String result = "";
		int tokenCounter = 0;
		if (tokens.length == 0)
			return newValue;
		for (String token : tokens) {
			tokenCounter++;
			result += token;
			if (token.length() == 0) {
				result += newValue;
			} else {
				char lastChar = token.charAt(token.length() - 1);

				if (tokenCounter == tokens.length && !(formula.endsWith(oldValue)))
					break;
				if ((lastChar >= 'a' && lastChar <= 'z') || (lastChar >= 'A' && lastChar <= 'Z')
						|| (lastChar >= '0' && lastChar <= '9')) {// part of
																	// cell
																	// reference
					result += oldValue;
				} else {
					result += newValue;
				}
			}
		}
		// System.out.println("OLD: " + formula);
		// System.out.println("NEW: " + result);
		// if (formula.equalsIgnoreCase(result)) {
		// System.out.println("No diff");
		// }
		return result;
	}

	// FFR
	private static boolean replaceFormulaFunction(Ptg token, String formula, Cell cell) {
		System.out.println("+++ FFR +++");
		if (formula.contains("SUM")) {
			System.out.println("+++ replace SUM/AVG +++");
			formula = formula.replaceAll("SUM", "AVERAGE");
			cell.setCellFormula(formula);
			return true;
			/*
			 * } else if (formula.contains("COUNT")) { formula =
			 * formula.replaceAll("COUNT", "SUM"); cell.setCellFormula(formula);
			 * return true;
			 */
		} else if (formula.contains("AVERAGE")) {
			System.out.println("+++ replace AVG/SUM +++");
			formula = formula.replaceAll("AVERAGE", "SUM");
			cell.setCellFormula(formula);
			return true;
		} else if (formula.contains("MIN")) {
			System.out.println("+++ replace MIN/MAX +++");
			formula = formula.replaceAll("MIN", "MAX");
			cell.setCellFormula(formula);
			return true;
		} else if (formula.contains("MAX")) {
			System.out.println("+++ replace MAX/MIN +++");
			formula = formula.replaceAll("MAX", "MIN");
			cell.setCellFormula(formula);
			return true;
		}

		return false;
	}

	// AOR and ROR
	private static boolean replaceOperator(ValueOperatorPtg token, String formula, Cell cell) {
		System.out.println("+++ AOR or ROR +++");
		if (token instanceof AddPtg || token instanceof UnaryPlusPtg) {
			// formula = formula.replace('+', '-');
			System.out.println("+++ replace +/* +++");
			formula = formula.replace('+', '*');
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof SubtractPtg || token instanceof UnaryMinusPtg) {
			System.out.println("+++ replace -/+ +++");
			formula = formula.replace('-', '+');
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof MultiplyPtg) {
			// formula = formula.replace('*', '/');
			System.out.println("+++ replace */+ +++");
			formula = formula.replace('*', '+');
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof DividePtg) {
			System.out.println("+++ replace //* +++");
			formula = formula.replace('/', '*');
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof EqualPtg) {
			System.out.println("+++ replace =/<> +++");
			formula = formula.replace("=", "<>");
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof NotEqualPtg) {
			System.out.println("+++ replace <>/= +++");
			formula = formula.replace("<>", "=");
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof GreaterEqualPtg) {
			System.out.println("+++ replace >=/< +++");
			formula = formula.replace(">=", "<");
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof GreaterThanPtg) {
			System.out.println("+++ replace >/< +++");
			formula = formula.replace(">", "<");
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof LessEqualPtg) {
			System.out.println("+++ replace <=/> +++");
			formula = formula.replace("<=", ">");
			cell.setCellFormula(formula);
			return true;
		} else if (token instanceof LessThanPtg) {
			System.out.println("+++ replace </> +++");
			formula = formula.replace("<", ">");
			cell.setCellFormula(formula);
			return true;
		}
		return false;
	}

	// RFR
	private static boolean replaceReferenceWithAnotherReference(RefPtgBase refToken, String formula, Cell cell,
			CellContainer cells) {
		System.out.println("+++ RFR +++");
		Random rand = new Random();
		String oldToken = refToken.toFormulaString();
		Map<Integer,String> nonReferencingFormulaCells = getNonReferencingCells(cells, cell);
		int cellIndex = rand.nextInt(nonReferencingFormulaCells.size());
		String newToken = nonReferencingFormulaCells.get(cellIndex);
		newToken = newToken.substring(newToken.indexOf("!")+1);
		formula = replaceConstant(oldToken, newToken, formula);
		cell.setCellFormula(formula);
		return true;
	}

	private static Map<Integer, String> getNonReferencingCells(CellContainer cells, Cell cell) {
		Map<Integer, String> nonReferencingCells = new HashMap<Integer, String>();
		int counter = 0;
//		try {
			Coords thisCell = new Coords(cell.getAddress());
			for (Coords co : cells.getFormulaCoords()) {
				boolean contained = false;
				at.tugraz.ist.debugging.modelbased.Cell c = cells.getCell(co);
				for(at.tugraz.ist.debugging.modelbased.Cell ref :c.getAllReferencesRecursive()){
					if(ref.equals(thisCell)){
						contained = true;
						break;
					}	
				}
				if(!contained)
					nonReferencingCells.put(counter++, c.toString());
			}
//		} catch (CoordinatesException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return nonReferencingCells;
	}

	// CRR
	private static boolean replaceReferenceWithConstant(RefPtgBase refToken, String formula, Cell cell) {
		System.out.println("+++ CRR +++");
		Random rand = new Random();
		String oldToken = refToken.toFormulaString();
		Integer constInt = rand.nextInt(1000);
		formula = formula.replaceAll(oldToken, constInt.toString());
		cell.setCellFormula(formula);
		return true;
	}

	public static Map<Coords, String> seedFault(String originalFile, String faultyFile, Integer numFaults) {
		try {
			Pair<Workbook, FormulaParsingWorkbook> workbooks1; // = getWorkbook(originalFile);
			Map<Coords, String> faultTypes = new HashMap<Coords, String>();
			Workbook wb=null;// = workbooks1.getFirst();
			FormulaParsingWorkbook fpw;// = workbooks1.getSecond();
			
			System.out.println("Seeding "+numFaults+" faults in file "+faultyFile);

			
			boolean correctFormulas = false;
			while (!correctFormulas) {
				 workbooks1 = getWorkbook(originalFile);
				 wb = workbooks1.getFirst();
				 fpw = workbooks1.getSecond();
				Map<Integer, Cell> cells = readCellsFromWorkbook(wb);
				for (int i = 0; i < numFaults; i++) {
					Boolean changed = false;
					int trials = 0;
					while (!changed && trials < 100) {
						trials++;
						
						String cellName = "";
						try {
							Random rand = new Random();
							Integer random = rand.nextInt(cells.size());
							Cell cell = cells.get(random);
							cellName = getName(cell);
							System.out.println("Try to seed fault in CELL: " + cellName);
							if (faultTypes.containsKey(new Coords(cellName))) {
								changed = false;
								System.out.println("Not changed (because already mutated), used trials "+trials);
								continue;
							}
							FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
							evaluator.evaluateAll();
							if (cell.getCachedFormulaResultTypeEnum() != CellType.NUMERIC) {
								changed = false;
								System.out.println("Not changed (non-numeric cell), used trials "+trials);
								continue;
							}
							String formula = cell.getCellFormula();
							Integer replaceTroughtConstant = rand.nextInt(8);
							if (replaceTroughtConstant == 0) { // FRC
								System.out.println("+++ FRC +++");
								Integer constInt = rand.nextInt(8);
								formula = constInt.toString();
								changed = true;
								faultTypes.put(new Coords(cellName), "FRC");
								cell.setCellFormula(formula);
								continue;
							}
							Ptg[] formulaTokens = FormulaParser.parse(cell.getCellFormula(), fpw, FormulaType.CELL, i);
							Integer position = rand.nextInt(formulaTokens.length);
							Ptg token = formulaTokens[position];
							changed = mutateToken(faultTypes, cell, cellName, formula, token,
									CellContainer.create(originalFile));
						} catch (Exception e) {
							e.printStackTrace();
							changed = false;
						}
						if(!changed)
							System.out.println("Not Changed, used trials "+trials);
						else
							System.out.println("Cell "+cellName+ " changed!");
					}
					if (!changed) {
						System.err.println("No mutuation could be created for file " + faultyFile);
						return null;
					}
				}
				correctFormulas = recalculateAllFormulas(wb);
				System.out.println("Formulas correct: "+correctFormulas);
			}
			writeToOutputFile(faultyFile, wb);
			return faultTypes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// CRS
	private static boolean shrinkContinuousRange(AreaPtgBase areaToken, String formula, Cell cell) {
		System.out.println("+++ CRS +++");
		Random rand = new Random();
		Boolean begin = rand.nextBoolean();
		String oldToken = areaToken.toFormulaString();

		if (areaToken.getFirstColumn() < areaToken.getLastColumn()) {
			if (begin) {
				areaToken.setFirstColumn(areaToken.getFirstColumn() + 1);
			} else {
				areaToken.setLastColumn(areaToken.getLastColumn() - 1);
			}
			formula = formula.replaceAll(oldToken, areaToken.toFormulaString());
			cell.setCellFormula(formula);
			return true;
		} else if (areaToken.getFirstRow() < areaToken.getLastRow()) {
			if (begin) {
				areaToken.setFirstRow(areaToken.getFirstRow() + 1);
			} else {
				areaToken.setLastRow(areaToken.getLastRow() - 1);
			}
			formula = formula.replaceAll(oldToken, areaToken.toFormulaString());
			cell.setCellFormula(formula);
			return true;
		}

		return false;
	}

	private static void writeToOutputFile(String file2, Workbook wb) throws IOException, FileNotFoundException {
		File dir = new File(file2.substring(0, file2.lastIndexOf(File.separator)));
		if (!dir.exists() || dir.isDirectory()) {
			dir.mkdirs();
		}
		File file2_f = new File(file2);
		if (!file2_f.exists())
			file2_f.createNewFile();
		FileOutputStream out = new FileOutputStream(file2);
		wb.write(out);
		out.close();
	}
}
