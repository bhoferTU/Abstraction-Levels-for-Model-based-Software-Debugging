package at.tugraz.ist.debugging.modelbased;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.SpreadsheetCell;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.NullExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.IntConstant;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import choco.Choco;
import choco.IPretty;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.usermodel.CellValue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Excel cell
 * 
 */
public class Cell extends SpreadsheetCell {

    public Cell(){}
    
	/**
	 * Creates a null cell, i.e. a cell including a NullExpression instance
	 * 
	 * @param column
	 * @param row
	 * @param index
	 */
	public Cell(int column, int row, int index) {
		super(new Coords(index, row, column), null, new NullExpression(), "");
	}

	/**
	 * 
	 * @param c column
	 * @param r row
	 * @param s sheet index
	 * @param cellValue
	 * @param expression
	 * @param cellFormula
	 *            string representation of the formula
	 */
	public Cell(Coords coords, CellValue cellValue,
			IConstraintExpression expression, String cellFormula) {
		super(coords, cellValue, expression, cellFormula);

	}

	/**
	 * Triggers the info instance to add the cell's SMT representation
	 * 
	 * @param info
	 *            SMT code generation instance
	 * @throws IOException
	 *             An error happened while generating the SMT code
	 */
	public void addSMTConstraint(SMTConstraintStrategyGenerationInformation info)
			throws IOException {
		info.addCellAssignment(this, expression.getSMTConstraint(info));

	}

	/**
	 * Triggers the info instance to add the cell's Z3 representation
	 * 
	 * @param info
	 *            Z3 wrapper instance
	 * @throws Z3Exception
	 *             An error happened while adding the cell's Z3 representation
	 */
	public void addZ3Constraint(Z3ConstraintStrategyGenerationInformation info)
			throws Z3Exception {
        info.addCellAssignment(this, expression.getZ3Constraint(info));
	}
    
    public BoolExpr getZ3Constraint(Z3ConstraintStrategyGenerationInformation info) throws Z3Exception
    {
        return info.getCellAssignment(this, expression.getZ3Constraint(info));
    }

	/**
	 * Evaluates the expression tree and returns an object representing the
	 * result. The object type is determined dynamically by the expression's
	 * evaluation method, and can be Bool, Integer, Double, String.
	 * 
	 * @return Result object
	 */
	public Object evaluate() {
		if (this.expression == null)
			throw new InvalidOperationException(
					"Cannot evaluate cell since there is no expression available.");

		try {
			Object result = expression.evaluate();
			return result;
		} catch (EvaluationException e) {
			throw e.setCell(getCoords()).setExpression(
					expression.toString());
		}
	}



	/**
	 * Returns the Choco constraint which represents the current cell
	 * 
	 * This function returns an eq constraint which does not include the
	 * abnormal variable (i.e. cell = expression).
	 * 
	 * @param info
	 *            Info parameter
	 * @return
	 */
	public Constraint getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		IPretty expressionConstraint;
		try {
			expressionConstraint = expression.getChocoConstraint(info);
		} catch (EvaluationException e) {
			throw e.setCell(getCoords());
		}
		Variable cellVariable = info.getVariables().get(this);

		if (expressionConstraint instanceof IntegerExpressionVariable
				&& cellVariable instanceof IntegerVariable) {
			return Choco.eq((IntegerVariable) cellVariable,
					(IntegerExpressionVariable) expressionConstraint);
		}
		throw new InvalidOperationException(
				"Cannot create cell constraint because of unknown variable or expression constraint type");

	}

	/**
	 * Looks for conditional expressions in the expression tree and returns the
	 * set of all found expressions
	 * 
	 * @return
	 */
	public Set<IConstraintExpression> getConditionalExpressions() {
		if (expression != null)
			return expression.getConditionalExpressions();
		return new HashSet<IConstraintExpression>();

	}
	
	/**
	 * Returns a set of cells which are needed in order to determine the
	 * evaluation result for each possible path through the expression tree
	 * 
	 * @return
	 */
	public Set<Cell> getAllReferencesRecursive() {
		
		Set<Cell> referenced = new HashSet<Cell>();
		
		//recursive
		for (Cell cell : expression.getReferencedCells(false, true))
		{
			referenced.add(cell);
			referenced.addAll(cell.getAllReferencesRecursive());
		}
		return referenced;
	}
	
	/*private MinionExpressionConstraints getDependencyBasedConstraints(
			MinionConstraintStrategyGenerationInformation info,
			Boolean sophisticated) {
		String cellName = getCoords().getMinionString();
		if (expression instanceof ConstExpression) {
			return MinionConstraints.getConstantDefinition("1", cellName,
					Domain.BOOLEAN);
		} else {
			Integer abnormalIndex = info.getNextAbnormalVariableIndex(this);
			String tmpVar = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
			Set<String> referencedCells = new HashSet<String>();
			for (Coords c : expression.getReferences(false)) { //FIXME: which cells are needed here? recursive yes/no?
				referencedCells.add(c.getMinionString());
			}
			MinionExpressionConstraints constraints = MinionConstraints
					.getMINConstraint(referencedCells, tmpVar);

			if (sophisticated && expression.isEquivalencePossible()) {
				constraints.addConstraints(MinionConstraints
						.getEqualReifyConstraint(cellName, "1", tmpVar));
			} else {
				constraints.addConstraints(MinionConstraints
						.getEqualImplyConstraint(cellName, "1", tmpVar));
			}
			return MinionConstraints.surroundConstraintsWithAbnormalClause(
					constraints, abnormalIndex, cellName);
		}

	}
	*/
	@Override
	public IConstraintExpression getExpression() {
		if (expression == null)
			throw new InvalidOperationException(
					"Cannot retrieve expression for cell since it is not set");
		return expression;
	}

	public MinionExpressionConstraints getMinionConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		EModelGranularity granularity = info.getModelGranularity();
		switch (granularity) {
		//case Simple:
		//	return getDependencyBasedConstraints(info, false);
		//case Sophisticated:
		//	return getDependencyBasedConstraints(info, true);
		case Dependency: 
			return getDependencyBasedConstraints(info);
		case Value:
			return getValueBasedConstraints(info);
		case Comparison:
			return getComparisonConstraints(info);
		default:;
			System.err.println("ModelGranularity " + granularity
					+ " not supported!");
			return null;
		}

	}
	/**
	 * @author inica 2022 new FUNCTION for the DBM -  
	 * getDependencyBasedConstraints(info) - makes use of TABLES Constraints and TUPLES
	 * NO min Constraint, NO sophisticated, NO reifyimply
	 */
private MinionExpressionConstraints getDependencyBasedConstraints(MinionConstraintStrategyGenerationInformation info) {
		
		try{
			String cellName = getCoords().getMinionString();
			if(expression instanceof IntConstant){
				return MinionConstraints.getConstantDefinition("1", cellName,
						Domain.BOOLEAN);
			}
			
			Integer abnormalIndex = info.getNextAbnormalVariableIndex(this);
			
			MinionExpressionConstraints constraints = expression
					.getMinionValueConstraints(info);
			
			if (constraints.isConstant()) {
				Set<String> tcConstraints = constraints.getConstraintsTC();
				Set<String> newTcConstraints = new HashSet<String>();
				for (String tc : tcConstraints) {
					newTcConstraints.add(tc.replace(constraints.getVarname(),
							cellName));
				}
				info.deleteVariable(constraints.getVarname());
				return new MinionExpressionConstraints(new HashSet<String>(),
						newTcConstraints, cellName, constraints.getResultType());
			}

			Set<String> constraintSet = constraints.getConstraints();
			Set<String> newConstraints = new HashSet<String>();
			if (info.isAuxhiliaryVariable(constraints.getVarname())) {
				for (String tc : constraintSet) {
					newConstraints.add(tc.replace(constraints.getVarname(),
							cellName));
				}
				info.deleteVariable(constraints.getVarname());

			} else {
				newConstraints = constraintSet;
				newConstraints.addAll(MinionConstraints.getEQUALDTableConstraints(abnormalIndex, constraints.getVarname(), cellName).getConstraints());
			}
			constraints = new MinionExpressionConstraints(newConstraints,
					constraints.getConstraintsTC(), cellName,
					constraints.getResultType());
			return constraints;
		}catch (EvaluationException e) {
			throw e.setCell(getCoords());
		}
	}
	private MinionExpressionConstraints getComparisonConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		try{
			String cellName = getCoords().getMinionString();
			if(expression instanceof IntConstant){
				return MinionConstraints.getConstantDefinition("1", cellName,
						Domain.INT3);
			}
			
			Integer abnormalIndex = info.getNextAbnormalVariableIndex(this);
			
			MinionExpressionConstraints constraints = expression
					.getMinionValueConstraints(info);
			
			if (constraints.isConstant()) {
				Set<String> tcConstraints = constraints.getConstraintsTC();
				Set<String> newTcConstraints = new HashSet<String>();
				for (String tc : tcConstraints) {
					newTcConstraints.add(tc.replace(constraints.getVarname(),
							cellName));
				}
				info.deleteVariable(constraints.getVarname());
				return new MinionExpressionConstraints(new HashSet<String>(),
						newTcConstraints, cellName, constraints.getResultType());
			}

			Set<String> constraintSet = constraints.getConstraints();
			Set<String> newConstraints = new HashSet<String>();
			if (info.isAuxhiliaryVariable(constraints.getVarname())) {
				for (String tc : constraintSet) {
					newConstraints.add(tc.replace(constraints.getVarname(),
							cellName));
				}
				info.deleteVariable(constraints.getVarname());

			} else {
				newConstraints = constraintSet;
				newConstraints.addAll(MinionConstraints.getEQUALTableConstraints(abnormalIndex, constraints.getVarname(), cellName).getConstraints());
			}
			constraints = new MinionExpressionConstraints(newConstraints,
					constraints.getConstraintsTC(), cellName,
					constraints.getResultType());
			return constraints;
		}catch (EvaluationException e) {
			throw e.setCell(getCoords());
		}
	}

	private MinionExpressionConstraints getValueBasedConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		try {
			String cellName = getCoords().getMinionString();
			if (expression instanceof IntConstant) {
				return MinionConstraints.getConstantDefinition(
						((IntConstant) expression).getValueAsString(),
						cellName, Domain.INTEGER);
			}
			if (expression instanceof NullExpression){
				return MinionConstraints.getConstantDefinition(
						"0",
						cellName, Domain.INTEGER);
			}
			MinionExpressionConstraints constraints = expression
					.getMinionValueConstraints(info);
			if (constraints.isConstant()) {
				Set<String> tcConstraints = constraints.getConstraintsTC();
				Set<String> newTcConstraints = new HashSet<String>();
				for (String tc : tcConstraints) {
					newTcConstraints.add(tc.replace(constraints.getVarname(),
							cellName));
				}
				info.deleteVariable(constraints.getVarname());
				return new MinionExpressionConstraints(new HashSet<String>(),
						newTcConstraints, cellName, constraints.getResultType());
			}

			Integer abnormalIndex = info.getNextAbnormalVariableIndex(this);
			Set<String> constraintSet = constraints.getConstraints();
			Set<String> newConstraints = new HashSet<String>();
			if (info.isAuxhiliaryVariable(constraints.getVarname())) {
				for (String tc : constraintSet) {
					newConstraints.add(tc.replace(constraints.getVarname(),
							cellName));
				}
				info.deleteVariable(constraints.getVarname());

			} else {
				newConstraints = constraintSet;
				newConstraints.addAll(MinionConstraints.getEQUALConstraint(
						constraints.getVarname(), cellName,
						constraints.getResultType()).getConstraints());
			}
			constraints = new MinionExpressionConstraints(newConstraints,
					constraints.getConstraintsTC(), cellName,
					constraints.getResultType());
			return MinionConstraints.surroundConstraintsWithAbnormalClause(
					constraints, abnormalIndex, cellName);

		} catch (EvaluationException e) {
			throw e.setCell(getCoords());
		}
	}

	public void setExpression(IConstraintExpression expression) {
		super.expression = expression;
		this.expression = expression;
	}

	@Override
	public String toString() {
		return /*"Cell " + */coords.getPOIStringWithSheetPrefix();
	}
	
	
}
