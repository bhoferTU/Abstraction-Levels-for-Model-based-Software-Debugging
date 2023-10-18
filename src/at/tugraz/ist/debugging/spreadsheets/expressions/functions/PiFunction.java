/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Angi
 */
public class PiFunction extends ShellFunction {

	public PiFunction(IConstraintExpression[] expressions) {
		super(Function.PI_FUNCTION_NAME, expressions);
	}

	@Override
	public Object evaluate() {

        return Math.PI;
	}

	@Override
	public IPretty getChocoConstraint(ChocoConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: pi -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: pi -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(SMTConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: pi -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
        Double pi = Math.PI;
		return info.getContext().MkReal(pi.toString());
	}

	@Override
	public Boolean isEquivalencePossible() {
		return true;
	}

	@Override
	public String toString() {
        return super.toString(PI_FUNCTION_NAME, operands);
	}
}
