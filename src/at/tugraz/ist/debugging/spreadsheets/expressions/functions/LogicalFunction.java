package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator.SortType;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Implementation of logical functions
 * 
 */
class LogicalFunction extends ShellFunction {
	/**
	 * Supported logical functions
	 */
	public enum LogicalFunctionType {
		And, Or
	};

	private LogicalFunctionType functionType;

	public LogicalFunction(IConstraintExpression[] expressions, LogicalFunctionType functionType) {
		super(expressions);
		this.functionType = functionType;

		if (this.functionType == LogicalFunctionType.And)
			functionName = Function.AND_FUNCTION_NAME;
		else
			functionName = Function.OR_FUNCTION_NAME;
	}

	/**
	 * Converts a given object to a boolean value
	 * 
	 * This method behaves as Excel: An integer or real value is true if it is
	 * not equal to 0.
	 * 
	 * @param value
	 * @return
	 */
	private Boolean convertToBool(Object value) {
		if (value == null)
			return null;

		if (value instanceof Boolean)
			return (Boolean) value;

		if (value instanceof Integer)
			return (Integer) value != 0;

		if (value instanceof Double)
			return (Double) value != 0;

		return null;
	}

	@Override
	public Object evaluate() {
		List<Boolean> results = new LinkedList<Boolean>();
		List<IConstraintExpression> flatExpr = getFlattedExpressions(operands);

		for (IConstraintExpression expr : flatExpr) {
			Object res = expr.evaluate();
			Boolean bRes = convertToBool(res);
			if (bRes != null)
				results.add(bRes);
		}

		switch (functionType) {
		case And:
			return !results.contains(Boolean.FALSE);
		case Or:
			return results.contains(Boolean.TRUE);
		default:
			throw new RuntimeException("Unknown boolean operation");
		}
	}

	@Override
	public IPretty getChocoConstraint(ChocoConstraintStrategyGenerationInformation info) {
		// RESTRICTION: not supported yet
		throw new InvalidOperationException("Not supported");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(MinionConstraintStrategyGenerationInformation info) {
		String auxVar = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
		MinionExpressionConstraints constraints = new MinionExpressionConstraints(new HashSet<String>(), auxVar,
				Domain.BOOLEAN);
		Set<String> referencedCells = new HashSet<String>();
		for (IConstraintExpression expression : operands) {

			// expressions that are evalualted must not be of the type Boolean!
			/*
			 * if(!(expression.evaluate() instanceof Boolean)){ throw new
			 * RuntimeException( String.format(
			 * "AND/OR: cannot determine not() of type '%s'",
			 * expression.evaluate().getClass().getName())); }
			 */
			if (expression instanceof CellReference) {
				for (Cell referredCell : ((CellReference) expression).getDirectlyReferencedCells()) {
					referencedCells.add(referredCell.getCoords().getMinionString());
				}
			} else {

				MinionExpressionConstraints constraints1 = expression.getMinionValueConstraints(info);
				constraints.addConstraints(constraints1);
				referencedCells.add(constraints1.getVarname());
			}

		}
		MinionExpressionConstraints constraints2;
		switch (functionType) {
		case And:
			constraints2 = MinionConstraints.getANDConstraint(referencedCells, auxVar);
			break;
		case Or:
			constraints2 = MinionConstraints.getORConstraint(referencedCells, auxVar);
			break;
		default:
			throw new InvalidOperationException("Minion constraint: Unknown operation");
		}
		constraints2.addConstraints(constraints);
		return constraints2;
	}

	@Override
	public String getSMTConstraint(SMTConstraintStrategyGenerationInformation info) {
		List<String> boolExpressionsList = new ArrayList<String>();

		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				for (Cell referencedCell : ((CellReference) expression).getDirectlyReferencedCells()) {
					Object value = referencedCell.evaluate();
					if (isParticipatingValue(value)) {
						boolExpressionsList.add(info.getCodeGenerator().toSortType(
								info.getVariables().get(referencedCell), info.determineSort(value), SortType.Bool));
					}

				}
			} else {
				Object value = expression.evaluate();
				if (isParticipatingValue(value)) {
					boolExpressionsList.add(info.getCodeGenerator().toSortType(expression.getSMTConstraint(info),
							info.determineSort(value), SortType.Bool));
				}
			}
		}

		String[] boolExpressions = new String[boolExpressionsList.size()];
		boolExpressions = boolExpressionsList.toArray(boolExpressions);
		switch (functionType) {
		case And:
			return info.getCodeGenerator().and(boolExpressions);
		case Or:
			return info.getCodeGenerator().or(boolExpressions);
		default:
			throw new RuntimeException("SMT constraint: Invalid logical operation");

		}
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info) throws Z3Exception {

		List<BoolExpr> boolExpressionsList = new ArrayList<BoolExpr>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				for (Cell referencedCell : ((CellReference) expression).getDirectlyReferencedCells()) {
					Object value = referencedCell.evaluate();
					if (isParticipatingValue(value)) {
						boolExpressionsList.add(
								(BoolExpr) info.toSortType(info.getVariables().get(referencedCell), SortType.Bool));
					}
				}
			} else {
				Object value = expression.evaluate();
				if (isParticipatingValue(value)) {
					boolExpressionsList
							.add((BoolExpr) info.toSortType(expression.getZ3Constraint(info), SortType.Bool));
				}
			}
		}

		BoolExpr[] boolExpressions = new BoolExpr[boolExpressionsList.size()];
		boolExpressions = boolExpressionsList.toArray(boolExpressions);

		switch (functionType) {
		case And:
			return info.getContext().MkAnd(boolExpressions);
		case Or:
			return info.getContext().MkOr(boolExpressions);
		default:
			throw new InvalidOperationException("Z3 constraint: Unknown operation");
		}
	}

	@Override
	public Boolean isEquivalencePossible() {
		switch (functionType) {
		case And:
			if ((Boolean) this.operands[0].evaluate() == true && (Boolean) this.operands[1].evaluate() == true)
				if (this.operands[0].isEquivalencePossible() && this.operands[1].isEquivalencePossible())
					return true;
			return false;

		case Or:
			if(this.operands[0] instanceof LogicalFunction && this.operands[1] instanceof LogicalFunction){
				if(((LogicalFunction)this.operands[0]).functionType == LogicalFunctionType.And && ((LogicalFunction)this.operands[1]).functionType == LogicalFunctionType.And) //Checking for XOR
					return true;
			}
			if ((Boolean) this.operands[0].evaluate() == false && (Boolean) this.operands[1].evaluate() == false) {
				if (this.operands[0].isEquivalencePossible() && this.operands[1].isEquivalencePossible())
					return true;
			}
			return false;

		default:
			return false;
		}
	}

	/**
	 * Determines whether a given value influences the function result or if it
	 * is ignored (e.g. String)
	 * 
	 * @param value
	 * @return
	 */
	private boolean isParticipatingValue(Object value) {
		return (value instanceof Integer) || (value instanceof Double) || (value instanceof Boolean);
	}

	@Override
	public String toString() {
		return super.toString(functionType == LogicalFunctionType.And ? AND_FUNCTION_NAME : OR_FUNCTION_NAME, operands);

	}

}