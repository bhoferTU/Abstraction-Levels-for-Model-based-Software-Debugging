package at.tugraz.ist.debugging.spreadsheets.expressions;

import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;

/**
 * Base class for constant and complex expressions which are combined in a tree
 * structure and/or associated with a certain cell instance
 * 
 */
public interface IExpression {

	/**
	 * Determines the set of cells which are directly referred by the current
	 * expression, so any Cell-References that occur in the formula.
	 * 
	 * 
	 * @param dynamic
	 *            if true, only the cell references that are in the evaluated
	 *            subexpressions are returned. Any references that only occur in
	 *            branches of the expression that is not executed due to
	 *            evaluation are ignored.
	 * @param constants
	 *            if true, cells with constant values (input cells) are also
	 *            returned, otherwise only formula cells are returned
	 * 
	 * @return a set of cells that are directly referenced by this expression
	 */
	public Set<Cell> getReferencedCells(boolean dynamic, boolean constants);

	/**
	 * Determines the set of coordinates which are directly referred by the
	 * current expression, so any Cell-References that occur in the formula.
	 * This can be called even before all cells are added to the container as it
	 * is non-recursive and has no dependency to {@link Cell}.
	 * 
	 * @param dynamic
	 *            if true, only the cell references that are in the evaluated
	 *            subexpressions are returned. Any references that only occur in
	 *            branches of the expression that is not executed due to
	 *            evaluation are ignored.
	 * 
	 * @return a set of Coords that are directly referenced by this expression
	 */
	public Set<Coords> getReferences(boolean dynamic);

	/**
	 * 
	 * @return
	 */
	public int getNumberOperations();

	/**
	 * 
	 * @return true when the input could be derived from the output
	 */
	public Boolean isEquivalencePossible();

	/**
	 * is the subexpression expressed as a poi-formula
	 */
	public void setStringExpression(String expr);

	public String getFormula();

	// public void setContext(FormulaParsingWorkbook wb, int ws);

	/**
	 * 
	 * @return
	 */
	@Override
	public String toString();

}
