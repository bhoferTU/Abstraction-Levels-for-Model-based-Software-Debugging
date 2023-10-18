package at.tugraz.ist.debugging.spreadsheets.expressions.constants;

import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import choco.Choco;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Represents a boolean constant
 * 
 */
public class BoolConstant extends ConstExpression {
	private boolean value;

	public BoolConstant(boolean value) {
		this.value = value;
	}

	@Override
	public Boolean evaluate() {
		return value;
	}

	@Override
	public IntegerExpressionVariable getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		return this.value ? Choco.ONE : Choco.ZERO;
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			String result = info.getNextAuxiliaryVariable(Domain.INT3);
			String stringValue = "1";
			return MinionConstraints.getConstantDefinition(stringValue, result,
					Domain.INT3);
		}
		if (info.getModelGranularity() == EModelGranularity.Dependency) {
			String result = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
			String stringValue = "1";
			return MinionConstraints.getConstantDefinition(stringValue, result,
					Domain.BOOLEAN);
		}
		String result = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
		String stringValue = (value) ? "1" : "0";
		return MinionConstraints.getConstantDefinition(stringValue, result,
				Domain.INTEGER);
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		return info.getCodeGenerator().getValue(value);
	}

	@Override
	public String getTypeAsString() {
		return "bool";
	}

	@Override
	public String getValueAsString() {
		return Boolean.toString(value);
	}

    @Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		return info.getContext().MkBool(value);
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

}
