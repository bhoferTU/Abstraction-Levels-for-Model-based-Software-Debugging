package at.tugraz.ist.debugging.spreadsheets.expressions;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ConstraintStrategyGenerationInformation;
import choco.IPretty;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public interface IConstraint {
	/**
	 * Evaluates the expression (which also leads to the evaluation of
	 * subexpressions in complex expressions). The resulting object's type is
	 * determined dynamically with respect to the operand types. All expression
	 * evaluation functions aim to behave like the Excel internal evaluation.
	 * 
	 * @return Object representing the evaluation result
	 */
	public Object evaluate();

	/**
	 * Generates the Choco constraint representation of a the current
	 * expression.
	 * 
	 * This method recursively calls subexpressions' getChocoConstraint() method
	 * and returns a constraint or an IntegerExpression object.
	 * 
	 * @param info
	 *            Choco generation information which includes all variable
	 *            mappings etc.
	 * @return
	 */
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info);

	/**
	 * Generates the Minion constraint representation of the current expression.
	 * The underlying model is based on concrete values: Possibly created
	 * auxiliary variables are added to the info object.
	 * 
	 * @param info
	 *            Minion generation information which includes all variable
	 *            mappings etc.
	 * @return
	 */
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info);

	/**
	 * Generates an string which represents the current expression in the SMT
	 * language. This is done by recursive calls of the same function.
	 * 
	 * @param info
	 *            SMT code generation information
	 * @return
	 */
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info);
    
    /**
	 * Generates a Z3 expression hierarchy which represents the current expression. 
     * This is done by recursive calls of the same function.
	 * 
	 * @param info
	 *            Z3 code generation information
	 * @return
	 * @throws Z3Exception
	 */
	public Expr getZ3Constraint(Z3ConstraintStrategyGenerationInformation info)
			throws Z3Exception;
}
