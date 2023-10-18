package at.tugraz.ist.debugging.spreadsheets.expressions.constants;

import java.math.BigDecimal;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import choco.kernel.model.variables.real.RealExpressionVariable;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Represents a real constant
 * 
 */
public class DoubleConstant extends ConstExpression {
	private double value;

	public DoubleConstant(BigDecimal value) {
		this.value = value.doubleValue();
	}

	public DoubleConstant(double value) {
		this.value = value;
	}

	@Override
	public Double evaluate() {
		return value;
	}


	@Override
	public RealExpressionVariable getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		// return new RealConstantVariable(value);
		throw new UnsupportedOperationException("Real not supported for Choco");
	}


	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		throw new UnsupportedOperationException("Real not supported in Minion");
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		return info.getCodeGenerator().getValue(value);
	}

	@Override
	public String getTypeAsString() {
		return "double";
	}

	@Override
	public String getValueAsString() {
		return Double.toString(value);
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		return info.getContext().MkReal(Double.toString(value));
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}
}
