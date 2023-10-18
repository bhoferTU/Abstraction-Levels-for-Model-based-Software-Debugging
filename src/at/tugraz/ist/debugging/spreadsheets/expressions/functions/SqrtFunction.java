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
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import choco.IPretty;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Angi
 */
public class SqrtFunction extends ShellFunction {
    IConstraintExpression expression;
    
	public SqrtFunction(IConstraintExpression expression) {
		super(Function.SQRT_FUNCTION_NAME, new IConstraintExpression[] { expression });
		this.expression = expression;
	}

	@Override
	public Object evaluate() {
        Object result = expression.evaluate();
        if(result instanceof Integer)
            return Math.sqrt(((Integer)result).doubleValue());
        if(result instanceof Double)
            return Math.sqrt((Double)result);
        throw new EvaluationException("Function: SQRT only accepts a number");
	}

	@Override
	public IPretty getChocoConstraint(ChocoConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: sqrt -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: sqrt -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(SMTConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: sqrt -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
        //TODO: Implement me
        throw new NotImplementedException("Function: sqrt -> get...Constraint not implemented");
        
	}

	@Override
	public Boolean isEquivalencePossible() {
		for (IConstraintExpression expression : operands) {
			if (!expression.isEquivalencePossible()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
        return super.toString(PI_FUNCTION_NAME, operands);
	}
}
