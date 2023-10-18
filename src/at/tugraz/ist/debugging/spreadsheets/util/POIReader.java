package at.tugraz.ist.debugging.spreadsheets.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.WorkbookInfo;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.NullExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.functions.Function;
import at.tugraz.ist.debugging.spreadsheets.parser.ErrorCellException;
import at.tugraz.ist.debugging.spreadsheets.parser.Parser;
import at.tugraz.ist.debugging.spreadsheets.parser.ParsingException;
import at.tugraz.ist.util.debugging.Writer;

/**
 * Workbook representative class which includes a map of cells and provides a
 * factory method to load a given Excel workbook.
 * 
 */
public class POIReader {

	/**
	 * Poi Type of Excelfile, which can be HSSF or XSSF
	 */
	public enum ExcelFileType {
		HSSF, XSSF
	};

	/**
	 * Status of the current cell container instance
	 * 
	 */
	public enum Mode {
		/**
		 * Cell container is ready to be used
		 */
		Initialized,
		/**
		 * Workbook is not yet loaded completely
		 */
		Initializing
	}

	/**
	 * Specifies type of excel program
	 */
	public enum ProgramType {
		/**
		 * excel file contains basic functions like SUM, AVG,...
		 */
		FUNCTION,
		/**
		 * excel file uses If
		 */
		IF,
		/**
		 * excel file contains only simple arithmetic and text
		 */
		SIMPLE
	}

	/**
	 * Factory method which loads an Excel workbook given its filepath
	 * 
	 * @param filePath
	 *            of excel file
	 * @param silent
	 *            defines if cell container should be printed
	 * @return created cell container
	 */
	public static POIReader create(String filePath, CellContainer container) {
		Workbook wb;
		FormulaParsingWorkbook ewb;
		ExcelFileType type;
		try {
			wb = new XSSFWorkbook(new FileInputStream(filePath));
			ewb = XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);
			type = ExcelFileType.XSSF;
		} catch (Exception e) {
			try {
				wb = new HSSFWorkbook(new FileInputStream(filePath));
				ewb = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);
				type = ExcelFileType.HSSF;
			} catch (Exception e2) {
				Writer.print(e2.getMessage(), true);
				Writer.printStackTrace(e2);
				// return null;
				throw new RuntimeException(
						"Error during cell container creation", e2);
			}
		}

		// iterate one time to find out the maximum amount of columns
		int minColumn = Integer.MAX_VALUE;
		int maxColumn = Integer.MIN_VALUE;
		int minRow = Integer.MAX_VALUE;
		int maxRow = Integer.MIN_VALUE;

		List<String> wsnames = new ArrayList<String>();

		int sheets = wb.getNumberOfSheets();
		for (int s = 0; s < sheets; ++s) {
			Sheet sheet = wb.getSheetAt(s);

			wsnames.add(s, sheet.getSheetName());

			if (sheet.getFirstRowNum() < minRow)
				minRow = sheet.getFirstRowNum();
			if (sheet.getLastRowNum() > maxRow)
				maxRow = sheet.getLastRowNum();

			for (int r = sheet.getFirstRowNum(); r < sheet.getLastRowNum(); ++r) {
				Row row = sheet.getRow(r);
				if (row != null) {
					if (row.getFirstCellNum() < minColumn)
						minColumn = row.getFirstCellNum();
					if (row.getLastCellNum() > maxColumn)
						maxColumn = row.getLastCellNum();
				}
			}
		}

		POIReader poiReader = new POIReader(maxColumn + 1, maxRow + 1);
		poiReader.excelFileType = type;
		
		poiReader.getWBInfo().setWSNames(wsnames);
		poiReader.getWBInfo().setActiveWS(wb.getActiveSheetIndex());
		poiReader.getWBInfo().setEvaluationWB((EvaluationWorkbook) ewb);
		poiReader.getWBInfo().setWB((FormulaRenderingWorkbook)ewb);
		poiReader.getWBInfo().setWb(wb);

		FormulaEvaluator evaluator = wb.getCreationHelper()
				.createFormulaEvaluator();
		poiReader.getWBInfo().setFormulaEvaluator(evaluator);

		
		Parser parser = new Parser(container, poiReader.getWBInfo());

		for (int s = 0; s < sheets; ++s) {
			// initialize a new cell container

			Sheet sheet = wb.getSheetAt(s);
			
			//each sheet has a tmpCell, which is empty and should not be displayed. 
			//it is used to calculate temporary formulas for the evaluation of conditions

			//poiReader.getWBInfo().setTmpCell(s,Cell c);
			
			org.apache.poi.ss.usermodel.Cell evalCell = null;
			
			
			for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); ++r) {
				Row row = sheet.getRow(r);
				if (row == null || row.getLastCellNum() < 0)
					continue;
				
				for (int c = row.getFirstCellNum(); c <= row.getLastCellNum(); ++c) {

					org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
					
					//check if cell is not null
					if (cell == null)
					{
						if (evalCell == null && c >= 0 && c < 256)
						{
							//save this cell as the temporary eval cell
							evalCell = row.createCell(c);
							poiReader.getWBInfo().setEvalCell(s,evalCell);
						}
					    continue;
					}
					
					//evaluate cell to get the *value*
					
					CellValue cellValue=null;
					try{
					cellValue = evaluator.evaluate(cell);
					if (cellValue == null)
						continue;
					}catch(Exception e){
						System.err.println("Error evaluating cell "+e.toString());
						continue;
					}
	
					IConstraintExpression expression = null;
					Ptg[] ptg = null;
					String formula = "";
					
					Coords coords = new Coords(s, r, c);

					
					try {
												
						if (cell.getCellTypeEnum() == CellType.FORMULA) {
							
							//if the cell contains a formula
							formula = cell.getCellFormula();
//							System.out.println(coords.getPOIStringWithSheetPrefix()+": "+formula);
							//parse the formula
							ptg = FormulaParser.parse(cell.getCellFormula(),
									ewb, FormulaType.CELL, s);
							
							for (int i = 0; i < ptg.length; ++i)
								if (ptg[i] instanceof FuncVarPtg
										&& ((FuncVarPtg) ptg[i])
												.getName()
												.equals(Function.IF_FUNCTION_NAME))
									poiReader.programType = ProgramType.IF;
								else if ((ptg[i] instanceof FuncVarPtg || ptg[i] instanceof FuncPtg)
										&& poiReader.programType != ProgramType.IF)
									poiReader.programType = ProgramType.FUNCTION;
							
							
							expression = parser.parse(ptg, s);
					
						} else {
							formula = cellValue.getStringValue();
							expression = ConstExpression.create(cellValue);
						}
						Cell analyzed_cell = new Cell(coords, cellValue,
								expression, formula);
						poiReader.registerCell(analyzed_cell);
					} catch (ParsingException e) {
						throw e.setCell(coords).setPtgs(ptg);
					} catch (ErrorCellException e) {
						Cell analyzed_cell = new Cell(coords,cellValue,
								new NullExpression(), formula);
						poiReader.errorCells.add(analyzed_cell);

						poiReader.registerCell(analyzed_cell);
					} /*catch (InvalidOperationException e)
					{
						throw new ParsingException(e.getMessage()).setCell(coords);
					}*/
				}
				
				if (evalCell == null)
				{
					if (row.getFirstCellNum() > 0)
						evalCell = row.createCell(row.getFirstCellNum()-1);
					else if (row.getLastCellNum() < 255)
						evalCell = row.createCell(row.getLastCellNum()+1);
				
					if (evalCell != null)
						poiReader.getWBInfo().setEvalCell(s,evalCell);
				}
			}
			
			if (evalCell == null)
			{
				int rownum = -1;
				
				if (sheet.getFirstRowNum() > 0)
					rownum = sheet.getFirstRowNum()-1;
				else 
					rownum = sheet.getLastRowNum()+1;
				
				Row row = sheet.createRow(rownum);
				evalCell = row.createCell(0);
				
				if (evalCell != null)
					poiReader.getWBInfo().setEvalCell(s,evalCell);
			}
		}
		poiReader.setReady();
		return poiReader;
	}


	/**
	 * Mapping of cell fingerprint to the particular cell instance
	 */
	private Map<Long, Cell> cellMap = new HashMap<Long, Cell>();

	/**
	 * maximum number of columns, needed for fingerprint
	 */
	private int currentMaxColumns;

	/**
	 * maximum number of rows, needed for fingerprint
	 */
	private int currentMaxRows;

	/**
	 * mode of cell container
	 */
	private Mode currentMode;

	private Set<Cell> errorCells = new HashSet<Cell>();

	/**
	 * type of excel file (HSSF or XSSF)
	 */
	private ExcelFileType excelFileType;

	/**
	 * type of exel program (SIMPLE, FUNCTION or IF)
	 */
	private ProgramType programType = ProgramType.SIMPLE;

	/**
	 * Set of fingerprints representing cells which were not already added, but
	 * are needed by already added cells
	 */
	private Set<Long> referredFutureCells = new HashSet<Long>();

	private WorkbookInfo wbInfo;
	

	/**
	 * creates a CellContainer.create with given parameters
	 * 
	 * @param maxColumns
	 *            defines maximum number of columns in excel file
	 * @param maxRows
	 *            defines maximum number of rows in excel file
	 */
	private POIReader(int maxColumns, int maxRows) {
		this.currentMode = Mode.Initializing;
		this.wbInfo = new WorkbookInfo(maxColumns, maxRows);
	}

	/**
	 * returns cell by its position
	 * 
	 * @param column
	 *            of wanted cell
	 * @param row
	 *            of wanted cell
	 * @param index
	 *            of wanted cell
	 * @return cell if found, else null
	 */
	public Cell getCell(int column, int row, int index) {
		long fingerprint = getCellFingerprint(column, row, index);
		Cell cell = cellMap.get(fingerprint);
		if (cell != null)
			return cell;

		// behaviour depending on mode
		if (currentMode == Mode.Initializing) {
			if (this.currentMaxRows > row || this.currentMaxRows == row
					&& this.currentMaxColumns > column) {
				// we refer a cell which is inside the cell table built so far
				// here we assume that the table is built row-wise
				cell = new Cell(column, row, index);
				registerCell(cell);
				return cell;
			} else {
				referredFutureCells.add(fingerprint);
				return null;
			}
		}
		throw new InvalidOperationException(String.format(
				"Cannot find cell for column: %d, row: %d, index: %d", column,
				row, index));
	}
	
	public void registerIfPossible(int column, int row, int index)
	{
		long fingerprint = getCellFingerprint(column, row, index);
		Cell cell = cellMap.get(fingerprint);
		if (cell != null)
			return;

		// behaviour depending on mode
		if (currentMode == Mode.Initializing) {
			if (this.currentMaxRows > row || this.currentMaxRows == row
					&& this.currentMaxColumns > column) {
				// we refer a cell which is inside the cell table built so far
				// here we assume that the table is built row-wise
				cell = new Cell(column, row, index);
				registerCell(cell);
			} else {
				referredFutureCells.add(fingerprint);
			}
		}
	}

	/**
	 * Calculates a cell's fingerprint given its integer position representation
	 * 
	 * @param column
	 * @param row
	 * @param index
	 * @return Fingerprint value
	 */
	private long getCellFingerprint(int column, int row, int index) {
		return ((this.wbInfo.getMaxRows() * column + row) * 100 + index);
	}

	/**
	 * Calculates the fingerprint given the cell's Excel name representation
	 * 
	 * @param cellName
	 *            Name of the desired cell
	 * @return
	 */
	private long getCellFingerprint(String cellName) {
		cellName = cellName.toUpperCase();
		int pos = 0;
		int index = 0;
		if (cellName.startsWith("SHEET")) {
			index = Integer
					.valueOf(cellName.substring(5, cellName.indexOf("!"))) - 1;
			cellName = cellName.substring(cellName.indexOf("!") + 1);
		}
		for (; pos < cellName.length(); ++pos)
			if (Character.isDigit(cellName.getBytes()[pos]))
				break;
		int row = Integer.valueOf(cellName.substring(pos)) - 1;
		int col = 0;
		for (int i = 0; i < pos; ++i)
			col = 26 * col + cellName.getBytes()[i] - 'A' + 1;
		col--;
		return getCellFingerprint(col, row, index);
	}

	/**
	 * Calculates the cell's position (sheet index, column, row) given its
	 * fingerprint
	 * 
	 * @param fingerprint
	 * @return
	 */
	private int[] getCellPosition(long fingerprint) {
		int[] position = new int[3];
		position[2] = (int) (fingerprint % 100);
		fingerprint /= 100;
		position[0] = (int) (fingerprint / this.wbInfo.getMaxRows());
		position[1] = (int) (fingerprint % this.wbInfo.getMaxRows());
		return position;
	}

	/**
	 * returns Collection of all stored cells
	 * 
	 * @return Collection of all stored cells
	 */
	public Map<Long, Cell> getCells() {
		return this.cellMap;
	}

	/**
	 * returns all error cells.
	 * 
	 * @return all error cells
	 */
	public Set<Cell> getErrorCells() {
		return errorCells;
	}

	/**
	 * returns type of excel file (HSSF or XSSF)
	 * 
	 * @return
	 */
	public ExcelFileType getExcelFileType() {
		return excelFileType;
	}

	/**
	 * returns type of excel program (SIMPLE, FUNCTION, IF)
	 * 
	 * @return type of excel program
	 */
	public ProgramType getProgramType() {
		return programType;
	}

	public WorkbookInfo getWBInfo() {
		return wbInfo;
	}

	/**
	 * returns true if initialization of cell container is finished
	 * 
	 * @return true if initialization of cell container is finished, else false
	 */
	public boolean isReady() {
		return currentMode == Mode.Initialized;
	}

	/**
	 * Registers a cell with the cell container
	 * 
	 * @param cell
	 *            which should be registered
	 */
	private void registerCell(Cell cell) {
		int column = cell.getCoords().getColumn();
		//as getRow returns with an index starting with 1, we need to adjust for fingerprint
		int row = cell.getCoords().getRow()-1;
		int index = cell.getCoords().getWorksheet();
		
		long fingerprint = getCellFingerprint(column, row, index);
		// Writer.println(String.format("Row: %s Col: %s FP: %s", row,
		// column,
		// fingerprint));

		// update information about latest inserted cell
		if (row > currentMaxRows) {
			// if we entered a new row we reset maxcolumns in order
			// to avoid that future referenced cells in the same row
			// are created immediately
			currentMaxColumns = column;
			currentMaxRows = row;
		} else {
			// if we do not entered a new row we just update maxcolumns
			currentMaxColumns = column > currentMaxColumns ? column
					: currentMaxColumns;
		}

		if (cellMap.containsKey(fingerprint)
				&& !(cellMap.get(fingerprint).getExpression() instanceof NullExpression))
		{
			Coords coords = cell.getCoords();
			throw new InvalidOperationException(
					String.format(
							"Cannot register cell twice (column: %d, row: %d, index: %d)",
							coords.getColumn(), coords.getRow(), coords.getWorksheet()));
		}
		cellMap.put(fingerprint, cell);

		// update refered future cells
		if (referredFutureCells.contains(fingerprint))
			referredFutureCells.remove(fingerprint);
		
		// trigger registration of referred future cells
		//TODO: if we have trouble with null references, check here
		Set<Coords> refs = cell.getReferences(false);
		for (Coords c : refs)
		{
			int col = c.getColumn();
			//as getRow returns with an index starting with 1, we need to adjust for fingerprint
			int r = c.getRow()-1;
			int i = c.getWorksheet();

			registerIfPossible(col,r,i);
		}
	}

	/**
	 * Applies the given values to the corresponding cells
	 * 
	 * @param values
	 *            Cell name to value mapping which contains the new cell values
	 */
	public void resetCellValues(Map<String, ConstExpression> values) {
		for (Map.Entry<String, ConstExpression> value : values.entrySet()) {
			long fingerprint = getCellFingerprint(value.getKey());
			Cell cell = cellMap.get(fingerprint);
			if (cell == null)
				continue;
			cell.setExpression(value.getValue());
		}
	}

	/**
	 * Sets the mode to initialized after adding referred cells which are not
	 * already available (i.e. referred null cells)
	 */
	private void setReady() {
		for (long fingerprint : referredFutureCells) {
			int[] position = getCellPosition(fingerprint);
			cellMap.put(fingerprint, new Cell(position[0], position[1],
					position[2]));
		}
		referredFutureCells.clear();
		currentMode = Mode.Initialized;
	}
}
