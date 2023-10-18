package at.tugraz.ist.debugging.spreadsheets.configuration.algorithm;

import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICellContainer;

public class SpreadsheetInput implements IAlgorithmInput {

	/**
	 * mapping to all cells in the spreadsheet, accessible via coordinates
	 */
	CellContainer cells;

	/**
	 * information about cells (expected/unexpected, faulty)
	 */
	SpreadsheetProperties properties;
	
	/**
	 * this flag can be set to true if any settings/config/properties have changed
	 * if this flag is true, we will reinitialize the data. 
	 */
	protected boolean needsInit = false;

	public SpreadsheetInput(SpreadsheetProperties properties,
			CellContainer cells) {
		setup(properties, cells);
	}

	@Override
	public void changeProperties(SpreadsheetProperties prop) {
		if (cells == null) {
			// create a new CellContainer, as none was given
			cells = CellContainer.create(prop.getExcelSheetPath());
		}

		this.properties = prop;
		needsInit = true;
	}

	@Override
	public ICellContainer getCells() {
		return cells;
	}

	@Override
	public SpreadsheetProperties getProperties() {
		return properties;
	}

	protected void init() {

	}

	/**
	 * saves Properties and CellContainer to this instance and initializes any
	 * additional configurations as given in the implementations of this
	 * interface
	 * 
	 * @param prop
	 * @param cells
	 */
	private void setup(SpreadsheetProperties prop, CellContainer cellContainer) {
		this.properties = prop;
		this.cells = cellContainer;
		if (cells == null)
			cells = CellContainer.create(properties.getExcelSheetPath());
		else if (!cells.isPopulated())
			try {
				cells.populate(properties.getExcelSheetPath());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		needsInit = true;
	}

}
