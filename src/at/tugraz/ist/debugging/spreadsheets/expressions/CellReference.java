package at.tugraz.ist.debugging.spreadsheets.expressions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.usermodel.CellValue;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.exceptions.CoordinatesException;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import choco.IPretty;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Represents a cell reference
 * 
 * A cell reference could either be a single cell reference (e.g. A3) or a range
 * reference (e.g. A3:B25)
 * 
 */
public class CellReference implements IConstraintExpression {

	/**
	 * Associated cell container
	 */
	private final CellContainer cellContainer;

	/**
	 * Set of all cells which are referred directly (NOT recursive)
	 */
	private Set<Cell> staticReferencedCells;

	private Set<Coords> directlyReferencedCoords;

	/**
	 * First referenced Coordinates
	 */
	private Coords firstCoords;
	
	/**
	 * Last referenced Coordinates
	 */
	private Coords lastCoords;


	/**
	 * constructor
	 * 
	 * @param firstColumn
	 *            is index of first column of area
	 * @param firstRow
	 *            is index of first row of area
	 * @param lastColumn
	 *            is index of last column of area
	 * @param lastRow
	 *            is index of last row of area
	 * @param index
	 *            of sheet of area
	 * @param cellContainer
	 *            containing referenced cells
	 */
	public CellReference(int firstColumn, int firstRow, int lastColumn,
			int lastRow, int index, CellContainer cellContainer) throws CoordinatesException {
		this.cellContainer = cellContainer;
		
		this.firstCoords = Coords.createCoordsSafely(index,firstRow,firstColumn);
		this.lastCoords = Coords.createCoordsSafely(index,lastRow,lastColumn);
		
	}

	/**
	 * constructor
	 * 
	 * @param column
	 *            of cell
	 * @param row
	 *            of cell
	 * @param index
	 *            of sheet of cell
	 * @param cellContainer
	 *            containing referenced cell
	 */
	public CellReference(int column, int row, int index,
			CellContainer cellContainer) throws CoordinatesException {
		this.cellContainer = cellContainer;
		this.firstCoords = Coords.createCoordsSafely(index,row,column);
		this.lastCoords = null;
		
	}

	@Override
	public Object evaluate() {
		Set<Cell> tmpReferencedCells = getDirectlyReferencedCells();
		
		if (IsRangeReference() || tmpReferencedCells.size() != 1)
			throw new EvaluationException("Cannot evaluate referenced cell");
		return tmpReferencedCells.iterator().next().evaluate();
	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean includeConstants) {
		if (cellContainer == null)
			throw new InvalidOperationException(
					"Cell container is not known to cell reference");

		Set<Cell> tmpReferences = new HashSet<Cell>();
		
		for (Coords coords : getReferences(dynamic)) {
//			System.out.println(coords);
			Cell cell = cellContainer.getCell(coords);

			if (cell == null){ //Cell is not initialized, because it is empty
				cell = new Cell(coords, new CellValue(0),new NullExpression(),"");
				cellContainer.addEmptyCell(coords, cell);
			}

			if (!includeConstants && !cell.isFormulaCell())
				continue;
			
			tmpReferences.add(cell);
		}
		
		return tmpReferences;
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		Set<Cell> tmpReferencedCells = getDirectlyReferencedCells();
		if (tmpReferencedCells.size() == 1)
			return info.getVariables()
					.get(tmpReferencedCells.iterator().next());
		throw new InvalidOperationException(
				"Cannot create constraint for referenced cell range");
	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		return new HashSet<IConstraintExpression>();
	}

	/**
	 * @return
	 */
	public Set<Cell> getDirectlyReferencedCells() {
		if (staticReferencedCells == null)
			staticReferencedCells = getReferencedCells(false, true);
		return staticReferencedCells;
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		Set<Cell> tmpReferencedCells = getDirectlyReferencedCells();
		if (tmpReferencedCells.size() == 1) {
			String varName = tmpReferencedCells.iterator().next()
					.getCoords().getMinionString();
			return new MinionExpressionConstraints(new HashSet<String>(),
					new HashSet<String>(), varName,
					info.getVariableDomain(varName));
		}

		throw new InvalidOperationException(
				"Cannot create constraint for referenced cell range");
	}

	@Override
	public int getNumberOperations() {
		return 0;
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		if (directlyReferencedCoords == null){
			
			directlyReferencedCoords = new HashSet<Coords>();
			
			if (IsSingleReference())
				directlyReferencedCoords.add(firstCoords);
			else
			{
				int index = firstCoords.getWorksheet();

				for (int row = firstCoords.getRow(); row <= lastCoords.getRow(); row++)
				{
					for (int col = firstCoords.getColumn(); col <= lastCoords.getColumn(); col++)
						directlyReferencedCoords.add(new Coords(index,row-1,col));
				}
			}
			
		}
		return directlyReferencedCoords;
	}

	/**
	 * @return List expressions which are contained by directly referred cells
	 */
	public List<IConstraintExpression> getReferencedExpressions() {
		List<IConstraintExpression> referencedExpressions = new ArrayList<IConstraintExpression>();
		Set<Cell> tmpReferencedCells = getDirectlyReferencedCells();
		for (Cell referencedCell : tmpReferencedCells) {
			referencedExpressions.add(referencedCell.getExpression());
		}
		return referencedExpressions;
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		Set<Cell> tmpReferencedCells = getDirectlyReferencedCells();
		if (tmpReferencedCells.size() == 1)
			return info.getVariables()
					.get(tmpReferencedCells.iterator().next());
		throw new InvalidOperationException(
				"Cannot create constraint for referenced cell range");
	}

	@Override
	public Expr getZ3Constraint(Z3ConstraintStrategyGenerationInformation info)
			throws Z3Exception {
		Set<Cell> tmpReferencedCells = getDirectlyReferencedCells();
		if (tmpReferencedCells.size() == 1)
			return info.getVariables()
					.get(tmpReferencedCells.iterator().next());
		throw new InvalidOperationException(
				"Cannot create constraint for referenced cell range");
	}

	@Override
	public Boolean isEquivalencePossible() {
		return true;
	}

	/**
	 * @return Cell reference refers multiple cells
	 */
	public boolean IsRangeReference() {
		return !IsSingleReference();
	}

	/**
	 * @return Cell reference only refers one single cell
	 */
	public boolean IsSingleReference() {
		return (lastCoords == null);
	}
	
	String formula;
	
	public void setStringExpression(String expression)
	{
		this.formula = expression;
	}
	
	public String getFormula()
	{
		return this.formula;
	}

	@Override
	public String toString() {
		String stringVal = firstCoords.getPOIStringWithSheetPrefix();
		if (IsRangeReference()) {
			stringVal += ":"
					+ lastCoords.getPOIStringWithSheetPrefix();
		}
		return stringVal;
	}

}
