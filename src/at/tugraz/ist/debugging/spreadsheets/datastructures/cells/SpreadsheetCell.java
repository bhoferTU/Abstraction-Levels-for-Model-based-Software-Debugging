package at.tugraz.ist.debugging.spreadsheets.datastructures.cells;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.usermodel.CellValue;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.IExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;

public class SpreadsheetCell implements ICell {

	/**
	 * POI content
	 */
	protected CellValue cellValue;

	/**
	 * Position of the Cell, consisting of worksheet index, row and column
	 */
	protected Coords coords;

	/**
	 * Expression tree
	 */
	protected IConstraintExpression expression;

	/**
	 * String representation of the formula, as it would appear in a spreadsheet
	 * program
	 */
	protected String formula;
	
	Set<Coords> cone = null;
	Boolean coneIsDynamic = null;
	Boolean coneIncludesConst = null;

    public SpreadsheetCell() {}
    
	public SpreadsheetCell(Coords coord, CellValue value,
			IConstraintExpression expr, String cellFormula) {
		this.coords = coord;
		this.cellValue = value;
		this.expression = expr;
		this.formula = cellFormula;

		if (this.expression == null)
			throw new InvalidOperationException(
					"Expression for newly instantiated cell must not be null");

	}

	@Override
	public CellValue getCellValue() {
		return cellValue;
	}

	@Override
	public Set<Coords> getCone(boolean dynamic, boolean includeConst) {
		if(cone!=null && coneIsDynamic==dynamic && coneIncludesConst==includeConst)
			return cone;
		
		coneIsDynamic=dynamic;
		coneIncludesConst=includeConst;
		
		cone = new HashSet<Coords>();
		
		try {
			Set<Cell> references = expression.getReferencedCells(dynamic, includeConst);
		
			for (Cell c : references) {
				
				// don't add this cells coordinates (cone of a constant cell =
				// their own coords), as it cannot be faulty.	
				if (!includeConst && !c.isFormulaCell()) {
					continue;
				}
	
				cone.addAll(c.getCone(dynamic, includeConst));
			}
		} catch (RuntimeException e)
		{
			System.err.println("Error in cell " + this.getCoords() + " value: " + this.getCellValue());
			throw new RuntimeException(e.getMessage());
		}
		cone.add(coords);
		return cone;
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		return expression.getReferences(dynamic);
	}

	@Override
	public Coords getCoords() {
		return coords;
	}

	@Override
	public IExpression getExpression() {
		if (expression == null)
			throw new InvalidOperationException(
					"Cannot retrieve expression for cell since it is not set");
		return expression;
	}

	@Override
	public String getFormulaString() {
		if (formula == null)
			return "";
		return formula;
	}

	@Override
	public boolean isFormulaCell() {
		if (getExpression() instanceof ConstExpression)
			return false;
		return true;
	}

	public static Set<Coords> convertCellsToCoords(Set<Cell> cells) {
		Set<Coords> ret = new HashSet<Coords>();
		for (ICell c : cells)
			ret.add(c.getCoords());
		return ret;
	}
	
	public Integer getRecursiveNumberOfPossibleCoincidentialCorrectness(ICellContainer cellContainer){
		if(recursiveNumberOfPossibleCoincidentalCorrectness==null){
			recursiveNumberOfPossibleCoincidentalCorrectness = getNumberOfPossibleCoincidentialCorrectness();
			for(Coords cell: getReferences(false)){
				recursiveNumberOfPossibleCoincidentalCorrectness +=cellContainer.getICell(cell).getRecursiveNumberOfPossibleCoincidentialCorrectness(cellContainer);
			}
		}
		return recursiveNumberOfPossibleCoincidentalCorrectness;
	}
	
	Integer recursiveNumberOfPossibleCoincidentalCorrectness = null;
	
    public Integer getNumberOfPossibleCoincidentialCorrectness(){
		return expression.isEquivalencePossible()?0:1;
	}
}
