package at.tugraz.ist.debugging.spreadsheets.configuration.algorithm;

import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;

public class ModelConfig extends SpreadsheetInput {

	public ModelConfig(SpreadsheetProperties properties, CellContainer cells) {
		super(properties, cells);
	}

	@Override
	public CellContainer getCells() {
		return cells;
	}
}
