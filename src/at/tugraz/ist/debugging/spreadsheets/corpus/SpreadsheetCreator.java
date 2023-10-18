package at.tugraz.ist.debugging.spreadsheets.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.util.datastructures.Pair;
import at.tugraz.ist.util.fileManipulation.Directory;

public class SpreadsheetCreator {

	public enum FormulaType {
		ADD, AND, MAX, MIN, MINUS, MOD, NOT, OR, SUM
	}

	public static String propertiesDirectory = "SpreadsheetsLinear"
			+ File.separator + "configurationfiles";
	public static final String propertiesFileEnding = ".properties";
	public static String spreadsheetsDirectory = "SpreadsheetsLinear"
			+ File.separator + "spreadsheets";
	public static final String spreadsheetsFileEnding = ".xlsx";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Directory.makeDirectory(spreadsheetsDirectory);
		Directory.makeDirectory(propertiesDirectory);
		for (int i = 1; i <= 100; i++) {
			new SpreadsheetCreator("linear_" + i, i);
		}

	}

	private CellStyle faultyCellStyle = null;

	private int formulaCellsColumnIndex = 1;
	private int formulaCellsCounter = 1;

	private int formulaCellsSheetIndex = 0;
	private CellStyle headingsCellStyle = null;
	private int inputCellsColumnIndex = 0;
	private int inputCellsCounter = 1;

	// private Map<String, Cell> nodePoiCellMap = new HashMap<String, Cell>();

	private int inputCellsSheetIndex = 0;

	private CellStyle outputCellStyleCorrect = null;
	private CellStyle outputCellStyleWrong = null;
	Map<Integer, Row> rows = new HashMap<Integer, Row>();
	private Sheet sheet = null;

	private Workbook workbook = null;

	public SpreadsheetCreator(String spreadsheetName, int numberOfFormulas) {

		workbook = new XSSFWorkbook();
		setWorkbookStyle();
		sheet = workbook.createSheet("Sheet1");
		formatSheet();

		defineHeadings();
		Pair<Coords, Integer> outputVariable = linear(numberOfFormulas);
		Coords output = outputVariable.getFirst();

		Map<Coords, String> wrongOutputCellExpectedValue = new HashMap<Coords, String>();
		Integer fakeExpectedValue = outputVariable.getSecond() == 0 ? 1 : 0;
		wrongOutputCellExpectedValue.put(output, fakeExpectedValue.toString());

		saveSpreadsheetToFile(spreadsheetName);
		createPropertiesFile(spreadsheetName, wrongOutputCellExpectedValue);

	}

	public void createPropertiesFile(String excelSheet,
			Map<Coords, String> wrongOutputCellExpectedValue) {
		SpreadsheetProperties property = new SpreadsheetProperties();
		property.setExcelSheetName(spreadsheetsDirectory + File.separator
				+ excelSheet + spreadsheetsFileEnding);
		// property.setCorrectOutputCells(correctOutputCell);
		property.setIncorrectOutputCellsWithExpectedValue(wrongOutputCellExpectedValue);
		/*
		 * Map<Coordinates, String> faultMapping = new HashMap<Coordinates,
		 * String>(); for(String fault: tc.faults){ Cell c =
		 * nodePoiCellMap.get(fault); faultMapping.put(new Coordinates(0,
		 * c.getRowIndex(), c.getColumnIndex()), "UNKNOWN"); }
		 * property.setFaultMapping(faultMapping);
		 */
		property.saveToFile(propertiesDirectory + File.separator + excelSheet
				+ propertiesFileEnding);
	}

	protected void defineHeadings() {

		Row row = getRow(0);
		Cell header = row.createCell(0);
		header.setCellValue("Input");
		header.setCellStyle(headingsCellStyle);
		header = row.createCell(1);
		header.setCellValue("Formulas");
		header.setCellStyle(headingsCellStyle);
	}

	/**
	 * Creates a spreadsheet that compute Fibonacci number
	 * 
	 * @param maxNumber
	 * @return Coordinates of the last formula cell, Computed Output value
	 */
	protected Pair<Coords, Integer> fibonacci(int maxNumber) {
		Coords cell = getNextFreeInputCell();
		Row row = getRow(cell.getRow() - 1);
		Cell c = row.createCell(cell.getColumn());
		c.setCellValue(0);
		Coords nMinusTwo = cell;

		cell = getNextFreeInputCell();
		row = getRow(cell.getRow() - 1);
		c = row.createCell(cell.getColumn());
		c.setCellValue(1);
		Coords nMinusOne = cell;

		Integer computedValue = 1;
		Integer nMinusTwoValue = 0;
		Integer nMinusOneValue = 1;

		for (int i = 0; i < maxNumber; i++) {
			cell = getNextFreeFormulaCell();
			row = getRow(cell.getRow() - 1);
			c = row.createCell(cell.getColumn());

			List<Coords> refCells = new ArrayList<Coords>();
			refCells.add(nMinusTwo);
			refCells.add(nMinusOne);
			String formula = getFormula(FormulaType.ADD, refCells);
			c.setCellFormula(formula);

			nMinusTwo = nMinusOne;
			nMinusOne = new Coords(0, c.getRowIndex(), c.getColumnIndex());

			computedValue = nMinusOneValue + nMinusTwoValue;
			nMinusTwoValue = nMinusOneValue;
			nMinusOneValue = computedValue;
		}
		c.setCellStyle(outputCellStyleWrong);
		return new Pair<Coords, Integer>(nMinusOne, computedValue);
	}

	protected void formatSheet() {
		for (int i = 0; i < 10; i++) {
			sheet.setColumnWidth(i, 4000);
		}
	}

	private String getBinaryFormula(String op, List<Coords> referencedCells) {
		String formula = "";
		for (Coords c : referencedCells) {
			formula += c.getPOIStringWithSheetPrefix() + op;
		}
		formula = formula.substring(0, formula.length() - 1);
		return formula;
	}

	private String getFormula(FormulaType formulaType,
			List<Coords> referencedCells) {
		String formula = "";

		switch (formulaType) {
		case MIN:
			formula += "MIN(";
			formula += getFormulaCellsAsString(referencedCells);
			formula += ")";
			break;

		case MAX:
			formula += "MAX(";
			formula += getFormulaCellsAsString(referencedCells);
			formula += ")";
			break;

		case MOD:
			if (referencedCells.size() != 2) {
				System.err.println("Wrong size of inputs for MOD!");
				return null;
			}

			formula += "MOD(";
			formula += getFormulaCellsAsString(referencedCells);
			formula += ")";
			break;

		case SUM:
			formula += "SUM(";
			formula += getFormulaCellsAsString(referencedCells);
			formula += ")";
			break;

		case AND:
			formula += "AND(";
			formula += getFormulaCellsAsString(referencedCells);
			formula += ")";
			break;

		case OR:
			formula += "OR(";
			formula += getFormulaCellsAsString(referencedCells);
			formula += ")";
			break;

		case NOT:
			if (referencedCells.size() != 1) {
				System.err.println("Wrong size of inputs for NOT!");
			}

			formula += "NOT(";
			formula += getFormulaCellsAsString(referencedCells);
			formula += ")";
			break;

		case ADD:
			formula = getBinaryFormula("+", referencedCells);
			break;

		case MINUS:
			formula = getBinaryFormula("-", referencedCells);
			break;

		default:
			System.err.println("not implemented: " + formulaType.name());
			break;
		}
		return formula;
	}

	private String getFormulaCellsAsString(List<Coords> referencedCells) {
		String formulaCells = "";
		for (Coords cell : referencedCells) {
			formulaCells += cell.getPOIStringWithSheetPrefix() + ",";
		}
		formulaCells = formulaCells.substring(0, formulaCells.length() - 1);
		return formulaCells;
	}

	protected Coords getNextFreeFormulaCell() {
		return new Coords(formulaCellsSheetIndex, formulaCellsCounter++,
				formulaCellsColumnIndex);
	}

	protected Coords getNextFreeInputCell() {
		return new Coords(inputCellsSheetIndex, inputCellsCounter++,
				inputCellsColumnIndex);
	}

	protected Row getRow(int rowIndex) {
		Row row = null;
		if (rows.containsKey(rowIndex)) {
			row = rows.get(rowIndex);
		} else {
			row = sheet.createRow((short) rowIndex);
			rows.put(rowIndex, row);
		}
		return row;

	}

	/**
	 * Creates a spreadsheet with maxNumber formulas. The compute value of each
	 * cell is equal to the number of formulas
	 * 
	 * @param maxNumber
	 * @return Coordinates of the last formula cell, Computed Output value
	 */
	protected Pair<Coords, Integer> linear(int maxNumber) {
		Coords cell = getNextFreeInputCell();
		Row row = getRow(cell.getRow() - 1);
		Cell c = row.createCell(cell.getColumn());
		c.setCellValue(0);
		Coords lastCell = cell;

		cell = getNextFreeInputCell();
		row = getRow(cell.getRow() - 1);
		c = row.createCell(cell.getColumn());
		c.setCellValue(1);
		Coords oneCell = cell;

		Integer computedValue = 1;

		for (int i = 0; i < maxNumber; i++) {
			cell = getNextFreeFormulaCell();
			row = getRow(cell.getRow() - 1);
			c = row.createCell(cell.getColumn());

			List<Coords> refCells = new ArrayList<Coords>();
			String formula = "";
			refCells.add(oneCell);
			refCells.add(lastCell);
			formula = getFormula(FormulaType.ADD, refCells);
			computedValue++;

			c.setCellFormula(formula);
			lastCell = new Coords(0, c.getRowIndex(), c.getColumnIndex());
		}
		c.setCellStyle(outputCellStyleWrong);
		return new Pair<Coords, Integer>(lastCell, computedValue);
	}

	/**
	 * Creates a spreadsheet with maxNumber formulas. The compute value of each
	 * cell is either 0 or 1.
	 * 
	 * @param maxNumber
	 * @return Coordinates of the last formula cell, Computed Output value
	 */
	protected Pair<Coords, Integer> plusAndMinus(int maxNumber) {
		Coords cell = getNextFreeInputCell();
		Row row = getRow(cell.getRow() - 1);
		Cell c = row.createCell(cell.getColumn());
		c.setCellValue(1);
		Coords oneCell = cell;

		cell = getNextFreeInputCell();
		row = getRow(cell.getRow() - 1);
		c = row.createCell(cell.getColumn());
		c.setCellValue(1);
		Coords additionalOneCell = cell;
		Coords lastCell = additionalOneCell;

		Integer computedValue = 1;

		for (int i = 0; i < maxNumber; i++) {
			cell = getNextFreeFormulaCell();
			row = getRow(cell.getRow() - 1);
			c = row.createCell(cell.getColumn());

			List<Coords> refCells = new ArrayList<Coords>();
			String formula = "";
			if (i % 2 == 1) {
				refCells.add(oneCell);
				refCells.add(lastCell);
				formula = getFormula(FormulaType.ADD, refCells);
				computedValue = 1;
			} else {
				refCells.add(oneCell);
				refCells.add(lastCell);
				formula = getFormula(FormulaType.MINUS, refCells);
				computedValue = 0;
			}

			c.setCellFormula(formula);
			lastCell = new Coords(0, c.getRowIndex(), c.getColumnIndex());
		}
		c.setCellStyle(outputCellStyleWrong);
		return new Pair<Coords, Integer>(lastCell, computedValue);
	}

	private void saveSpreadsheetToFile(String sheetName) {
		FileOutputStream fileOut;
		String fileName = spreadsheetsDirectory + File.separator + sheetName
				+ spreadsheetsFileEnding;
		try {
			fileOut = new FileOutputStream(fileName);
			workbook.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			System.err.println("Shit!!!!");
			System.err.println(e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Shit!!!!");
			System.err.println(e.toString());
			e.printStackTrace();
		}
	}

	protected void setWorkbookStyle() {
		headingsCellStyle = workbook.createCellStyle();
		Font fontBold = workbook.createFont();
		fontBold.setBold(true);
		headingsCellStyle.setFont(fontBold);

		faultyCellStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		fontBold.setBold(true);
		font.setColor(IndexedColors.RED.getIndex());
		faultyCellStyle.setFont(font);
		faultyCellStyle.setBorderBottom(BorderStyle.THICK);
		faultyCellStyle.setBottomBorderColor(IndexedColors.RED.getIndex());
		faultyCellStyle.setBorderLeft(BorderStyle.THICK);
		faultyCellStyle.setLeftBorderColor(IndexedColors.RED.getIndex());
		faultyCellStyle.setBorderRight(BorderStyle.THICK);
		faultyCellStyle.setRightBorderColor(IndexedColors.RED.getIndex());
		faultyCellStyle.setBorderTop(BorderStyle.THICK);
		faultyCellStyle.setTopBorderColor(IndexedColors.RED.getIndex());

		outputCellStyleCorrect = workbook.createCellStyle();
		outputCellStyleCorrect.setFillForegroundColor(IndexedColors.LIGHT_GREEN
				.getIndex());
		outputCellStyleCorrect.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		outputCellStyleWrong = workbook.createCellStyle();
		outputCellStyleWrong.setFillForegroundColor(IndexedColors.RED
				.getIndex());
		outputCellStyleWrong.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}

}
