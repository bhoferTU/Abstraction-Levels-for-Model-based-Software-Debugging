package at.tugraz.ist.debugging.spreadsheets.datastructures;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.util.CellAddress;

import at.tugraz.ist.debugging.spreadsheets.exceptions.CoordinatesException;
import at.tugraz.ist.debugging.spreadsheets.util.PoiExtensions;

/**
 * 
 * this class maps the location of a cell in the spreadsheet to sortable
 * coordinates
 * 
 * @author egetzner
 * 
 */
public class Coords {

	private static int compare(int o1, int o2) {
		return (o1 == o2) ? 0 : (o1 < o2) ? -1 : 1;
	}

	public static Set<String> convertCoordsSet(Set<Coords> set) {
		Set<String> strings = new HashSet<String>(set.size());

		for (Coords c : set) {
			strings.add(c.getConstraintString());
		}
		return strings;
	}


	private String columnName;

	private final int row, column, worksheet;

	/**
	 * Constructor for usage in with POI Coordinates (as they start from 0)
	 * other usage discouraged
	 * 
	 * each element corresponds to a number, starting from 0.
	 * 
	 * @param wsStartingWithZero
	 *            the worksheet-index
	 * @param rowStartingWithZero
	 *            the row index, starting from 0
	 * @param colStartingWithZero
	 *            the column index, starting from 0, 'A' == 0;
	 */
	public Coords(int wsStartingWithZero, int rowStartingWithZero,
			int colStartingWithZero) {
		this.row = rowStartingWithZero + 1;
		this.column = colStartingWithZero;
		this.worksheet = wsStartingWithZero;
		this.columnName = null;
	}

	public static Coords createCoordsSafely(int wsStartingWithZero, int rowStartingWithZero,
			int colStartingWithZero) throws CoordinatesException
	{
		Coords tmp = new Coords(wsStartingWithZero,rowStartingWithZero,colStartingWithZero);
		if (wsStartingWithZero < 0 ||
				rowStartingWithZero < 0 ||
				colStartingWithZero < 0)
			throw new CoordinatesException("faulty coordinates: " + tmp);
		return tmp;
	}
	
	/**
	 * usage recommended
	 * 
	 * Constructor for creating Coordinates from String the Format is:
	 * worksheet!column!row, worksheet starting from 0, column as a String of
	 * Uppersize Letters, row starting with 1
	 * 
	 * @param str
	 *            of the format:
	 */
	public Coords(String str) throws CoordinatesException {
			String[] rep = str.split("!");
		if (rep.length < 3)
			throw new CoordinatesException(
					"String Representation wrong format, string: " + str);

		worksheet = Integer.parseInt(rep[0]);
		columnName = rep[1];
		column = PoiExtensions.getColumnIndex(columnName);
		row = Integer.parseInt(rep[2]);
	}
	
	public Coords(CellAddress cellAddress){
		worksheet = 0;
		column = cellAddress.getColumn();
		columnName = PoiExtensions.getColumnString(column);
		row = cellAddress.getRow();
	}

	int compareTo(Coords o) {
		int ws_comp = compare(worksheet, o.getWorksheet());
		if (ws_comp == 0) {
			int row_comp = compare(row, o.getRow());
			if (row_comp == 0)
				return compare(column, o.getColumn());
			return row_comp;
		}
		return ws_comp;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Coords))
			return false;
		if (this == obj)
			return true;
		return (compareTo((Coords) obj) == 0);
	}

	/**
	 * 
	 * @return the column index, starting from 0
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * 
	 * @return the excel type column name, with Letters.
	 */
	public String getColumnName() {
		if (columnName == null)
			return PoiExtensions.getColumnString(column);
		else
			return columnName;
	}

	/**
	 * 
	 * @return the row index, starting from 1!
	 */
	public int getRow() {
		return row;
	}

	/**
	 * 
	 * @return worksheet index, starting from 0
	 */
	public int getWorksheet() {
		return worksheet;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	// / ---------------------------------------------------------------
	// different kinds of String representations

	/**
	 * 
	 * @return Excel-Type representation of Coordinates with Sheet prefix (e.g.:
	 *         Sheet1!A1)
	 */
	public String getPOIStringWithSheetPrefix() {
		return "Sheet" + (worksheet + 1) + "!" + getColumnName() + row;
	}

	/**
	 * 
	 * @return Excel-Type representation of Coordinates (e.g.: 0!A1)
	 */
	public String getShortString() {
		return worksheet + "!" + getColumnName() + row;
	}

	/**
	 * Returns the CSV string representation of the cell's position, each value
	 * is separated by a !
	 * 
	 * @return
	 */
	public String getCSVString() {
		return worksheet + "!" + getColumnName() + "!" + row;
	}

	/**
	 * 
	 * @return a String representation without !, for minion
	 */
	public String getMinionString() {
		return "Sheet" + (worksheet + 1) + "_" + getColumnName() + row;
	}
	
	
	/**
	 * 
	 * @return a String that can be used to identify cells in ConstraintBased approaches
	 */
	public String getConstraintString() {
		return getPOIStringWithSheetPrefix();
	}
	
	/**
	 * 
	 * @return a String that is easily readable by humans of the format (0!A2), 
	 * with the worksheet index separated by !, starting with 0
	 */
	public String getUserString() {
		return getShortString();
	}

	/**
	 * Returns the CSV string representation of the cell's position each value
	 * is separated by a !
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return getCSVString();
	}

}
