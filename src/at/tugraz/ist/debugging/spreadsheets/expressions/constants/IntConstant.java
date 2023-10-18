package at.tugraz.ist.debugging.spreadsheets.expressions.constants;

import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import choco.IPretty;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Represents an integer constant
 * 
 */
public class IntConstant extends ConstExpression {
	private int value;

	public IntConstant(int value) {
		this.value = value;
	}

	@Override
	public Integer evaluate() {
		return value;
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		return new IntegerConstantVariable(value);
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			return MinionConstraints.getFakeConstantDefintion(
					"1", Domain.INT3);
		}
		if (info.getModelGranularity() == EModelGranularity.Dependency) {
			return MinionConstraints.getFakeConstantDefintion(
					"1", Domain.BOOLEAN);
		}
		return MinionConstraints.getFakeConstantDefintion(
				Integer.toString(value), Domain.INTEGER);
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		return info.getCodeGenerator().getValue(value);
	}

	@Override
	public String getTypeAsString() {
		return "int";
	}

	@Override
	public String getValueAsString() {
		return Integer.toString(value);
	}

    @Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		return info.getContext().MkInt(value);
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}
}
