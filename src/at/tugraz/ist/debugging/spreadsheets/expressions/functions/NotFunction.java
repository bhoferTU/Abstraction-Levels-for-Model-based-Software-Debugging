package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;
import com.microsoft.z3.BoolExpr;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;


/**
 * Implementation of the Excel NOT function
 * 
 */
class NotFunction extends ShellFunction {
	IConstraintExpression expression;

	public NotFunction(IConstraintExpression expression) {
		super(Function.NOT_FUNCTION_NAME, new IConstraintExpression[] { expression });
		this.expression = expression;
	}

	@Override
	public Object evaluate() {
		Object value = expression.evaluate();
        if(value instanceof Double) {
            if(((Double)value).equals(1.0))
                value = true;
            else if(((Double)value).equals(0.0))
                value = false;
        }
		if (value instanceof Boolean)
			return !((Boolean) value);

		throw new RuntimeException(String.format(
				"NOT: cannot determine not() of type '%s'", value.getClass()
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
		String auxVar = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
		MinionExpressionConstraints constraints = expression
				.getMinionValueConstraints(info);
		MinionExpressionConstraints constraints1 = MinionConstraints
				.getNOTConstraint(constraints.getVarname(), auxVar);
		constraints1.addConstraints(constraints);
		return constraints1;
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException("Not supported");

	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		return info.getContext().MkNot((BoolExpr)expression.getZ3Constraint(info));
	}

	@Override
	public Boolean isEquivalencePossible() {
		return expression.isEquivalencePossible();
	}

	@Override
	public String toString() {
		return super.toString(NOT_FUNCTION_NAME,
				new IConstraintExpression[] { expression });
	}

}