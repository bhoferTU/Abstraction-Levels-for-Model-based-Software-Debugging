/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Angi
 */
public class PowerFunction extends ShellFunction {
IConstraintExpression base;
	IConstraintExpression exponent;

	public PowerFunction(IConstraintExpression base,
			IConstraintExpression exponent) {
		super(Function.POWER_FUNCTION_NAME, new IConstraintExpression[] { base, exponent });
		this.base = base;
        this.exponent = exponent;
		
	}

	@Override
	public Object evaluate() {

        Object resultBase = base.evaluate();
		Object resultExponent = exponent.evaluate();
		

		if (resultExponent instanceof Integer
				&& resultBase instanceof Integer)
			return (int)Math.pow((int)resultBase, (int)resultExponent);
		else if (resultExponent instanceof Double
				&& resultBase instanceof Double)
            return (double)Math.pow((double)resultBase, (double)resultExponent);
        else
			throw new RuntimeException(String.format("POW: cannot determine pow() of type '%s' and '%s'",
					resultExponent.getClass().getName(), 
                    resultBase.getClass().getName()));
	}

	@Override
	public IPretty getChocoConstraint(ChocoConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: power -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: power -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(SMTConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: power -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
        
		List<ArithExpr> expressions = new ArrayList<>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) expression).getDirectlyReferencedCells()) {
					Expr reference = info.getVariables().get(cell);
					if (reference instanceof IntExpr
							|| reference instanceof RealExpr) {
						expressions.add((ArithExpr) reference);
					}
				}
			} else {
				Expr reference = expression.getZ3Constraint(info);
				if (reference instanceof IntExpr
						|| reference instanceof RealExpr) {
					expressions.add((ArithExpr) reference);
				}
			}
		}

		// convert all participants to the sort type corresponding to the
		// resulting value
		SMTCodeGenerator.SortType targetType = info.determineSort(evaluate());
		ArithExpr[] expressionsArray = new ArithExpr[expressions.size()];
		expressionsArray = expressions.toArray(expressionsArray);
		for (int i = 0; i < expressionsArray.length; i++) {
			info.toSortType(expressionsArray[i], targetType);
		}
        if(expressionsArray.length != 2)
            throw new RuntimeException("Function: power() only supported for two arguments");
		return info.getContext().MkPower(expressionsArray[0], expressionsArray[1]);
	}

	@Override
	public Boolean isEquivalencePossible() {
        if (base.evaluate().toString().equals("0") || 
            base.evaluate().toString().equals("1") || 
            exponent.evaluate().toString().equals("0"))
            return false;

		return true;
	}

	@Override
	public String toString() {
		IConstraintExpression[] expression = { exponent, base };
		return super.toString(POWER_FUNCTION_NAME, expression);
	}
}
