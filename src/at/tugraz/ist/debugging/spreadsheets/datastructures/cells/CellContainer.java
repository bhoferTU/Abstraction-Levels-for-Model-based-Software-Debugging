package at.tugraz.ist.debugging.spreadsheets.datastructures.cells;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.util.POIReader;
import at.tugraz.ist.util.debugging.Writer;

/**
 * Workbook representative class which includes a map of cells and provides a
 * factory method to load a given Excel workbook.
 * 
 */
public class CellContainer implements ICellContainer {

    private Map<Coords, Cell> cells;
	private Set<Coords> formulaCells;
	private Set<Coords> outputCells;
	private Set<Coords> inputCells;
    
  


	/**
	 * Private Class to manage reading info from spreadsheet
	 */
	private POIReader reader;

	/**
	 * for superclasses
	 */
	protected CellContainer() {
		init();
	}
    
	public static CellContainer create(String excelSheetName) {
		CellContainer c = new CellContainer();
		
		try {
			if (excelSheetName != null)
				c.populate(excelSheetName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public void addEmptyCell(Coords coords, Cell cell){
		cells.put(coords, cell);
		inputCells.add(coords);
	}

	public boolean isPopulated() {
		if (reader != null)
			return reader.isReady();
		return false;
	}
	
	/**
	 * Initializes the set of input and output cells
	 */
	private void calculateInOutCells() {
		// search input cells
		Set<Coords> referenced_cells = new HashSet<Coords>();

		for (Cell entry : getCells()) {
			Set<Coords> references = entry.getReferences(false);

			if (references.isEmpty())
				inputCells.add(entry.getCoords());
			if (entry.isFormulaCell())
				formulaCells.add(entry.getCoords());

			referenced_cells.addAll(references);
		}

		// search output cells
		for (Cell entry : getCells()) {

			if (referenced_cells.contains(entry.getCoords()) == false && entry.isFormulaCell()) {
				outputCells.add(entry.getCoords());
			}
		}

		// input AND output: when no references to others (constant, formula)
		// and not referenced by other formula cells (--> not output, not input
		// - NEUTRAL)

		// remove cells which are in- and output
//		Iterator<Coords> iter = inputCells.iterator();

	
//		while (iter.hasNext()) {
//			Coords coords = iter.next();
//			if (outputCells.contains(coords)) {
//				outputCells.remove(coords);
//				iter.remove();
//			} else
//				inputs.add(cells.get(coords));
//		}
	}

	/**
	 * Dumps the string representation of the cell container
	 * 
	 * @param extended
	 *            Detailled information
	 */
	public void dumpInfo(boolean extended) {
		calculateInOutCells();
		Writer.printLine();
		Writer.println("                                CellContainer Information                                          ");
		Writer.println("------------------------------------------------------------------------------------------------------");

		Map<Long, Cell> cellMap = reader.getCells();

		Writer.println(String.format("Amount of registered cells:\t%d",
				cellMap.size()));
		if (extended) {
			List<Long> fingerprints = new ArrayList<Long>(cellMap.keySet());
			Collections.sort(fingerprints);
			for (long fingerprint : fingerprints) {
				Cell cell = cellMap.get(fingerprint);
				Object result = cell.evaluate();
				Writer.println(String
						.format("%1$-2s%2$-1s: %3$-40s => Result: %4$-20s (type: %5$-1s)",
								cell.getCoords().getUserString(),
								inputCells.contains(cell.getCoords()) ? "<"
										: outputCells.contains(cell.getCoords()) ? ">"
												: " ", cell.getExpression(),
								result, result != null ? result.getClass()
										.getName() : "unknown"));
			}
		}
		Writer.printLine();
	}

	// -------------------------------------------------
	/**
	 * single get cell classes
	 */

	/**
	 * returns cell by its Coordinates (consisting of sheet index, column and row)
	 * 
	 * @param coords
	 *            are the coordinates to the wanted cell
	 * @return returns cell if found, else null
	 */
	public Cell getCell(Coords coords) {
		return cells.get(coords);
	}
	
	/**
	 * return an ICell or null if not contained
	 */
	@Override
	public ICell getICell(Coords coord) {
		return cells.get(coord);
	}


	// ------------------------------------------------------
	/**
	 * getter for the collection
	 */
	
	/**
	 * returns Collection of all stored cells
	 * 
	 * @return Collection of all stored cells
	 */
	public Collection<Cell> getCells() {
		return reader.getCells().values();
	}

	/**
	 * 
	 * @return Set of Coordinates that point to Formula Cells (including output
	 *         cells)
	 */
	@Override
	public Set<Coords> getFormulaCoords() {
		if (formulaCells == null || formulaCells.size() <= 0)
			calculateInOutCells();

		return formulaCells; 
	}

	/**
	 * 
	 * @return Set of Coordinates that point to Input Cells
	 */
	@Override
	public Set<Coords> getInputCoords() {
		if (inputCells == null || inputCells.size() <= 0)
			calculateInOutCells();
		return inputCells;
	}

	/**
	 * 
	 * @return Set of Coordinates that point to Output Cells
	 */
	@Override
	public Set<Coords> getOutputCoords() {
		if (outputCells == null || outputCells.size() <= 0)
			calculateInOutCells();

		return outputCells;
	}

	/**
	 * 
	 * @return some information about the workbook such as the worksheet names
	 *         and the active worksheet
	 */
	public WorkbookInfo getWBInfo() {
		return reader.getWBInfo();
	}

	private void init() {
		cells = new HashMap<>();

		inputCells = new HashSet<>();
		outputCells = new HashSet<>();
		formulaCells = new HashSet<>();
	}

	/**
	 * Determines whether a given cell is an input cell
	 * 
	 * @param cell
	 * @return
	 */
	public boolean isInputCell(Cell cell) {
		if (getInputCoords() == null)
			throw new InvalidOperationException(
					"Cell container: List of inputs cells is not initialized");
		return getInputCoords().contains(cell.getCoords());
	}

	/**
	 * 
	 * @param filePath
	 *            of excel file
	 * @param silent
	 *            defines if cell container should be printed
	 * @return created cell container
	 */
	@Override
	public void populate(String filepath) {
		reader = POIReader.create(filepath, this);
		init();

		for (Cell cell : reader.getCells().values())
			cells.put(cell.getCoords(), cell);
	}


	/**
	 * creates a workbook
	 * 
	 * @param filePath
	 *            of excel file
	 * @return workbook on succeed, else null
	 */
	public static Workbook getWorkbook(String filePath) {
		Workbook wb;
		try {
			wb = new XSSFWorkbook(new FileInputStream(filePath));
		} catch (Exception e) {
			try {
				wb = new HSSFWorkbook(new FileInputStream(filePath));
			} catch (Exception e2) {
				Writer.print(e2.getMessage(), true);
				Writer.printStackTrace(e2);
				return null;
			}
		}
		return wb;
	}
	//* GEt the no. of formula cells for a spreadsheet
	
	public int getNoFormulaCells(){
		int no=this.getFormulaCoords().size();
		return no;
		
	}

}
