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
import choco.kernel.model.variables.integer.IntegerConstantVariable;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Represents a string constant expression
 * 
 */
public class StringConstant extends ConstExpression {
	private String value;

	public StringConstant(String value) {
		this.value = value;
	}

	@Override
	public String evaluate() {
		return value;
	}

	@Override
	public IntegerConstantVariable getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		return Choco.constant(info.mapString(value));
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			throw new InvalidOperationException("NullExpression not implemented for Comparision model!");
		}
		if (info.getModelGranularity() == EModelGranularity.Dependency) {
			throw new InvalidOperationException("NullExpression not implemented for DBM!");
		}
		String result = info.getNextAuxiliaryVariable(Domain.INTEGER);
		return MinionConstraints
				.getEQUALConstraint(Integer.toString(info.mapString(value)),
						result, Domain.INTEGER);
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		return info.getCodeGenerator().getValue(info.addStringMapping(value));
	}

	@Override
	public String getTypeAsString() {
		return "string";
	}

	@Override
	public String getValueAsString() {
		return value;
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		return info.getContext().MkInt(info.addStringMapping(value));
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}
}
