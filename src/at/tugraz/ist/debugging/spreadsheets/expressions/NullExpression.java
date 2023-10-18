package at.tugraz.ist.debugging.spreadsheets.expressions;

import java.util.HashSet;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import choco.Choco;
import choco.IPretty;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Represents the expression of an empty cell
 * 
 */
public class NullExpression extends ConstExpression {
	public NullExpression() {
	}

	@Override
	public Object evaluate() {
		//return null;
        if(ConstraintStrategyConfiguration.getStrategy()!=null && ConstraintStrategyConfiguration.getStrategy().getSolver() == ESolver.Z3)
            return 0.0;
        else
            return 0;
	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		return new HashSet<Cell>();
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		return Choco.ZERO;
	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		return new HashSet<IConstraintExpression>();
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			String auxVar = info.getNextAuxiliaryVariable(Domain.INT3);
			return MinionConstraints.getEQUALConstraint("1", auxVar, Domain.INT3);
		}
		
		String auxVar = info.getNextAuxiliaryVariable(Domain.INTEGER);
		return MinionConstraints
				.getEQUALConstraint("0", auxVar, Domain.INTEGER);
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		return new HashSet<>();
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		return info.getCodeGenerator().getValue(0);
	}

	@Override
	public String getTypeAsString() {
		return "int";
	}

	@Override
	public String getValueAsString() {
		return "13";
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		return info.getContext().MkReal(0);
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

	@Override
	public String toString() {
		return "<null-expression>";
	}
}
