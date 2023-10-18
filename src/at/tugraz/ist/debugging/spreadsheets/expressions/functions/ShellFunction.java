package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.IExpression;
import choco.IPretty;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.util.HashSet;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * A Shell Function for all Functions, only really implements the IExpression
 * interface however
 * 
 * All constraint related functions from the IConstraint(Expression) interfaces
 * throw an exception
 * 
 * @author egetzner
 * 
 */
public class ShellFunction extends Function implements IExpression {

    protected String formula;

	protected ShellFunction(IConstraintExpression[] operands) {
        super(operands);
	}

	public ShellFunction(String name, IConstraintExpression[] operands) {
		super(name, operands);
	}

	@Override
	public Object evaluate() {
		throw new InvalidOperationException(String.format(
				"IConstraintExpression not implemented for function '%s'",
				functionName));
	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		Set<Cell> cells = new HashSet<Cell>();
		for (IConstraintExpression summand : operands)
			cells.addAll(summand.getReferencedCells(dynamic, faultyConst));
		return cells;
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(String.format(
				"IConstraintExpression not implemented for function '%s'",
				functionName));
	}

	/**
	 * if (and company) will have to override this function
	 */
	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		Set<IConstraintExpression> conditionalExpressions = new HashSet<IConstraintExpression>();
		for (IConstraintExpression expr : operands) {
			conditionalExpressions.addAll(expr.getConditionalExpressions());
		}
		return conditionalExpressions;
	}
    
    public String getFormula()
	{
		return this.formula;
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(String.format(
				"IConstraintExpression not implemented for function '%s'",
				functionName));
	}

	@Override
	public int getNumberOperations() {
		int sum = 1;

		for (IConstraintExpression operand : operands)
			sum += operand.getNumberOperations();

		return sum;
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		Set<Coords> cells = new HashSet<Coords>();
		for (IConstraintExpression summand : operands)
			cells.addAll(summand.getReferences(false));
		return cells;
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException(String.format(
				"IConstraintExpression not implemented for function '%s'",
				functionName));
	}
    
    @Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		throw new InvalidOperationException(String.format(
				"IConstraintExpression not implemented for function '%s'",
				functionName));
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

    public void setStringExpression(String expression)
	{
		this.formula = expression;
	}
}
