package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator.SortType;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * Implementation of the Excel ABS function
 * 
 */
class AbsFunction extends ShellFunction {
	IConstraintExpression expression;

	public AbsFunction(IConstraintExpression expression) {
		super(Function.ABS_FUNCTION_NAME, new IConstraintExpression[] { expression });
		this.expression = expression;
	}

	@Override
	public Object evaluate() {
		Object value = expression.evaluate();
		if (value instanceof Integer) {
			Integer intVal = (Integer) value;
			return intVal < 0 ? -intVal : intVal;
		} else if (value instanceof Double) {
			Double dVal = (Double) value;
			return dVal < 0 ? -dVal : dVal;
		} else if (value instanceof Boolean) {
			Boolean bVal = (Boolean) value;
			return bVal ? 1 : 0;
		}
		throw new RuntimeException(String.format(
				"ABS: cannot determine abs() of type '%s'", value.getClass()
						.getName()));
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException("Not supported");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			throw new InvalidOperationException("AbsFunction not implemented for Comparision model!");
		}
		
		String auxVar = info.getNextAuxiliaryVariable(Domain.INTEGER);
		MinionExpressionConstraints constraints = expression
				.getMinionValueConstraints(info);
		MinionExpressionConstraints constraints1 = MinionConstraints
				.getABSOLUTConstraint(constraints.getVarname(), auxVar);
		constraints1.addConstraints(constraints);
		return constraints1;
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {

		String smtExpression = expression.getSMTConstraint(info);
		Object value = expression.evaluate();
		if (value instanceof Boolean)
			info.getCodeGenerator().toSortType(smtExpression,
					info.determineSort(value), SortType.Int);
		return info.getCodeGenerator().abs(smtExpression);

	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		Expr smtExpression = expression.getZ3Constraint(info);
		if (smtExpression instanceof BoolExpr)
			smtExpression = info.toSortType(smtExpression, SortType.Int);
		if (!(smtExpression instanceof ArithExpr))
			throw new RuntimeException(
					String.format(
							"ABS: cannot apply abs() to non-arithmetic value (type '%s')",
							smtExpression.getClass().getName()));
        
        Context ctx = info.getContext();
        ArithExpr expr = (ArithExpr)smtExpression;
        
        return (Expr) ctx.MkITE(ctx.MkLt(expr, ctx.MkInt(0)),
				ctx.MkUnaryMinus(expr), expr);
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

	@Override
	public String toString() {
		return super.toString(ABS_FUNCTION_NAME,
				new IConstraintExpression[] { expression });
	}

}