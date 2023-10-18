package at.tugraz.ist.debugging.spreadsheets.datastructures.cells;

import java.util.Set;

import org.apache.poi.ss.usermodel.CellValue;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.IExpression;

/***
 * 
 * interface for all cells, which have coordinates, are in or out cells and are
 * referenced or not.
 * 
 * @author egetzner
 * 
 */
public interface ICell {

	/**
	 * 
	 * @return the evaluated value of the cell (evaluation of a formula or the
	 *         constant value)
	 */
	public CellValue getCellValue();

	/**
	 * 
	 * @param dynamic
	 *            if true, cells that referenced only in neglected
	 *            subexpressions (not executed due to a branching) are not
	 *            considered for the result
	 * @param includeConst TODO
	 * 
	 * @return Set of Cell-Coordinates that are recursively referenced in the
	 *         Formula and subsequently referenced cells.
	 */
	public Set<Coords> getCone(boolean dynamic, boolean includeConst);

	/**
	 * Note on AreaTokens: all Coordinates that are encompassed by the AreaToken
	 * are returned e.g.: A1:A4 will result in [0!A!1, 0!A!2, 0!A!3, 0!A!4]
	 * 
	 * @param dynamic
	 *            if false, this function returns all cells that are referenced
	 *            in this cells expression, an empty set if the expression is
	 *            constant.
	 * 
	 *            if true, references that occur only in neglected branches
	 *            (that are not executed due to a conditional expression like
	 *            IF) are not contained in the set.
	 * 
	 * @return Set of Cell-Coordinates that are directly referenced in the
	 *         Formula
	 */
	public Set<Coords> getReferences(boolean dynamic);

	/**
	 * 
	 * @return exact Coordinates of the Cell in the workbook
	 */
	public Coords getCoords();

	/**
	 * 
	 * @return the expression that describes a cell (formula or constant)
	 */
	public IExpression getExpression();

	/**
	 * 
	 * @return
	 */
	public String getFormulaString();

	/**
	 * 
	 * @return true if the expression is complex (formula), false if constant
	 */
	public boolean isFormulaCell();
	
	
	public Integer getRecursiveNumberOfPossibleCoincidentialCorrectness(ICellContainer cellContainer);

}
