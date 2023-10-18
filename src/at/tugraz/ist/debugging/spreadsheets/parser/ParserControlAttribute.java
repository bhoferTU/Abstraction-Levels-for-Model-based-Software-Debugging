package at.tugraz.ist.debugging.spreadsheets.parser;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * Expression needed by the parser to temporarily store information about POI
 * ATTR tokens
 * 
 */
public class ParserControlAttribute implements IConstraintExpression {

	public enum Type {
		ATTR
	}

	private IConstraintExpression[] expressions;
	private Type type;

	public ParserControlAttribute(Type type, IConstraintExpression[] expressions) {
		this.type = type;
		this.expressions = expressions;
	}

	@Override
	public Object evaluate() {
		throw new InvalidOperationException(
				"Cannot evaluate control attribute expression");
	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		throw new InvalidOperationException(
				"Cannot determine referenced cells for control attribute expression");
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(
				"Parser control attribute cannot be converted to a constraint");
	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		throw new InvalidOperationException(
				"Cannot determine conditional expressions for control attribute expression");
	}

	public IConstraintExpression[] getExpressions() {
		return this.expressions;
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(
				"Parser control attribute cannot be converted to a constraint");
	}

	@Override
	public int getNumberOperations() {
		return 0;
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		throw new InvalidOperationException(
				"Cannot determine referenced cells for control attribute expression");
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(
				"Parser control attribute cannot be converted to a constraint");
	}

	@Override
	public Expr getZ3Constraint(Z3ConstraintStrategyGenerationInformation info)
			throws Z3Exception {
		throw new InvalidOperationException(
				"Parser control attribute cannot be converted to a constraint");
	}

	@Override
	public Boolean isEquivalencePossible() {
		throw new InvalidOperationException(
				"Parser control attribute cannot be converted to a constraint");
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
		return String.format("ParserControl attribute of type '%s'",
				type.toString());
	}

}
