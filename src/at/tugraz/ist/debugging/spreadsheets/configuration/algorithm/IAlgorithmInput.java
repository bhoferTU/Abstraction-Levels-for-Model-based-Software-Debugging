package at.tugraz.ist.debugging.spreadsheets.configuration.algorithm;

import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICellContainer;

public interface IAlgorithmInput {

	/**
	 * swaps out the SpreadsheetProperties, if any data (In/Out/Expected or
	 * Faulty) has changed and reinitializes any additional configuration that
	 * is dependant on this
	 * 
	 * @param prop
	 *            the new properties file
	 */
	public void changeProperties(SpreadsheetProperties prop);

	/**
	 * getter for the ICellContainer class, mapping all Cells, their Formula
	 * Expressions and Values to their Location
	 * 
	 * @return
	 */
	ICellContainer getCells();

	/**
	 * getter for SpreadsheetProperties, with additional info regarding correct
	 * and incorrect output cells as well as faulty cells if given.
	 * 
	 * @return
	 */
	SpreadsheetProperties getProperties();

}
