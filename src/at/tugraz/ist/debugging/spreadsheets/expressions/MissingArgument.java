package at.tugraz.ist.debugging.spreadsheets.expressions;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import choco.IPretty;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Represents a POI missing argument token and is needed by the parser
 * 
 */
public class MissingArgument extends Expression {

	@Override
	public Object evaluate() {
		return 0;
		// throw new
		// InvalidOperationException("Evaluating a missing argument is not supported");
	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		return new HashSet<Cell>();
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(
				"MissingArgument cannot be converted to constraint");
	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		return new HashSet<IConstraintExpression>();
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(
				"MissingArgument cannot be converted to constraint");
	}

	@Override
	public int getNumberOperations() {
		return 0;
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		return new HashSet<Coords>();
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(
				"MissingArgument cannot be converted to constraint");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		throw new InvalidOperationException(
				"MissingArgument cannot be converted to constraint");
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

	String formula;
	
	public void setStringExpression(String expression)
	{
		this.formula = expression;
	}
	
	public String getFormula()
	{
		return this.formula;
	}
	
	@Override
	public String toString() {
		return "<missing argument>";
	}

}
