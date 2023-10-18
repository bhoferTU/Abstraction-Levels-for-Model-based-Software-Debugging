package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import java.util.ArrayList;
import java.util.List;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;
import com.microsoft.z3.Expr;

import com.microsoft.z3.Z3Exception;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Implementation of the Excel AVG function
 * 
 */
class CountFunction extends ShellFunction {

	public CountFunction(IConstraintExpression[] expressions) {
		super(Function.COUNT_FUNCTION_NAME, expressions);
	}

	@Override
	public Object evaluate() {
        
		int count = 0;
        List<Object> values = new ArrayList<>();

		for (IConstraintExpression expression : operands) {
            if(expression instanceof CellReference)
                for(IConstraintExpression refExpression : ((CellReference) expression)
							.getReferencedExpressions())
                    values.add(refExpression.evaluate());
            else
                values.add(expression.evaluate());
		}

        for(Object value : values)
            if(value instanceof Double || value instanceof Integer || value instanceof Boolean)
                count++;
        
		return count;
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
        //TODO: Implement me
        throw new NotImplementedException("Function: count -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: count -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: count -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
        //TODO: Implement me
        throw new NotImplementedException("Function: count -> get...Constraint not implemented");
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
		return super.toString(COUNT_FUNCTION_NAME, operands);
	}
}