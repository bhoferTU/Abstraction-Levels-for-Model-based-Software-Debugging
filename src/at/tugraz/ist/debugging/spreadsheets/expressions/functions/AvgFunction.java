package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
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
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.ExpressionReturnType;
import at.tugraz.ist.util.MathUtils;
import choco.Choco;
import choco.IPretty;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealExpressionVariable;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Z3Exception;

/**
 * Implementation of the Excel AVG function
 * 
 */
class AvgFunction extends ShellFunction {

	public AvgFunction(IConstraintExpression[] expressions) {
		super(Function.AVG_FUNCTION_NAME, expressions);
	}

	@Override
	public Object evaluate() {
		double doubleSum = 0;
		int intSum = 0;
		ExpressionReturnType returnType = ExpressionReturnType.Integer;
		int cellCount = 0;

		List<IConstraintExpression> flatExpr = new LinkedList<IConstraintExpression>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				flatExpr.addAll(((CellReference) expression)
						.getReferencedExpressions());
			} else {
				flatExpr.add(expression);
			}
		}

		for (IConstraintExpression expr : flatExpr) {
			Object result = expr.evaluate();
			if (returnType == ExpressionReturnType.Integer) {
				if (result instanceof Integer)
					intSum += (Integer) result;
				else if (result instanceof Double) {
					doubleSum = intSum + (Double) result;
					returnType = ExpressionReturnType.Double;
				} else {
					// do not count the cell value
					cellCount -= 1;
				}
			} else if (returnType == ExpressionReturnType.Double) {
				if (result instanceof Integer)
					doubleSum = doubleSum + (Integer) result;
				else if (result instanceof Double)
					doubleSum = doubleSum + (Double) result;
				else {
					// do not count the cell value
					cellCount -= 1;
				}
			}
			cellCount++;
		}

		if (returnType == ExpressionReturnType.Integer)
			return MathUtils.Divide(intSum, cellCount);
		return MathUtils.Divide(doubleSum, cellCount);
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		List<IntegerExpressionVariable> integerExpressions = new ArrayList<IntegerExpressionVariable>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				for (Cell cell : ((CellReference) expression)
						.getDirectlyReferencedCells()) {
					Variable var = info.getVariables().get(cell);
					if (var instanceof IntegerExpressionVariable)
						integerExpressions.add((IntegerExpressionVariable) var);
					else if (var instanceof RealExpressionVariable)
						throw new InvalidOperationException(
								"Sum of real values is currently not supported");
					else if (var == null) {
						throw new InvalidOperationException(String.format(
								"Sum: The variable for cell %s does not exist",
								cell.getCoords().getUserString()));

					}
				}
			} else {
				IPretty var = expression.getChocoConstraint(info);
				if (var instanceof RealExpressionVariable)
					throw new InvalidOperationException(
							"Sum of real values is currently not supported");
				if (var instanceof IntegerExpressionVariable)
					integerExpressions.add((IntegerExpressionVariable) var);
			}

		}
		IntegerExpressionVariable[] summandVariables = new IntegerExpressionVariable[integerExpressions
				.size()];
		summandVariables = integerExpressions.toArray(summandVariables);

		IntegerVariable sumAuxVar = info.addAuxIntegerConstraint(Choco
				.sum(summandVariables));
		return info.addAuxIntDiv(sumAuxVar, new IntegerConstantVariable(
				summandVariables.length));
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			throw new InvalidOperationException("AvgFunction not implemented for Comparision model!");
		}
		
		String auxVar1 = info.getNextAuxiliaryVariable(Domain.INTEGER);
		String auxVar2 = info.getNextAuxiliaryVariable(Domain.INTEGER);
		MinionExpressionConstraints constraints = new MinionExpressionConstraints(
				new HashSet<String>(), auxVar2, Domain.INTEGER);
		Set<String> referencedCells = new HashSet<String>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				for (Cell referredCell : ((CellReference) expression)
						.getDirectlyReferencedCells()) {
					referencedCells.add(referredCell.getCoords().getMinionString());
				}
			} else {

				MinionExpressionConstraints constraints1 = expression
						.getMinionValueConstraints(info);
				constraints.addConstraints(constraints1);
				referencedCells.add(constraints1.getVarname());
			}

		}
		MinionExpressionConstraints constraints2 = MinionConstraints
				.getAVGConstraints(referencedCells, auxVar1, auxVar2);
		constraints2.addConstraints(constraints);
		return constraints2;
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {

		List<String> smtExpressions = new ArrayList<String>();
		List<SortType> sortTypes = new ArrayList<SortType>();
		for (IConstraintExpression summand : operands) {
			if (summand instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) summand)
						.getDirectlyReferencedCells()) {
					SortType sortType = info.determineSort(cell.evaluate());
					if (sortType == SortType.Int || sortType == SortType.Real) {
						smtExpressions.add(info.getVariables().get(cell));
						sortTypes.add(sortType);
					}
				}
			} else {
				SortType sortType = info.determineSort(summand.evaluate());
				if (sortType == SortType.Int || sortType == SortType.Real) {
					smtExpressions.add(summand.getSMTConstraint(info));
					sortTypes.add(sortType);
				}
			}
		}
		String[] summandsArray = new String[smtExpressions.size()];
		summandsArray = smtExpressions.toArray(summandsArray);

		// convert all participants to the sort type corresponding to the
		// resulting value
		SortType targetType = info.determineSort(evaluate());

		for (int i = 0; i < summandsArray.length; i++) {
			info.getCodeGenerator().toSortType(summandsArray[i],
					sortTypes.get(i), targetType);
		}

		return info.getCodeGenerator().div(
				info.getCodeGenerator().sum(summandsArray),
				info.getCodeGenerator().getValue(summandsArray.length));
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		List<ArithExpr> expressionList = new ArrayList<ArithExpr>();
		for (IConstraintExpression summand : operands) {
			if (summand instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) summand)
						.getDirectlyReferencedCells()) {
					Expr expression = info.getVariables().get(cell);
					if (expression instanceof IntExpr
							|| expression instanceof RealExpr) {
						expressionList.add((ArithExpr) expression);
					}
				}
			} else {
				Expr expression = summand.getZ3Constraint(info);
				if (expression instanceof IntExpr
						|| expression instanceof RealExpr) {
					expressionList.add((ArithExpr) expression);
				}
			}
		}

		// convert all participants to the sort type corresponding to the
		// resulting value
		SortType targetType = info.determineSort(evaluate());
		ArithExpr[] summandsArray = new ArithExpr[expressionList.size()];
		summandsArray = expressionList.toArray(summandsArray);
		for (int i = 0; i < summandsArray.length; i++) {
			info.toSortType(summandsArray[i], targetType);
		}

		return info.getContext().MkDiv(info.getContext().MkAdd(summandsArray),
				info.getContext().MkInt(summandsArray.length));
	}
    
	@Override
	public Boolean isEquivalencePossible() {
		for (IConstraintExpression expression : operands) {
			if (!expression.isEquivalencePossible()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return super.toString(AVG_FUNCTION_NAME, operands);
	}
}