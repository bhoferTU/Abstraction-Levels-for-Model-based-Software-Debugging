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
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import choco.IPretty;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Angi
 */
public class VarFunction extends ShellFunction {

    public VarFunction(IConstraintExpression[] expressions) {
		super(Function.VAR_FUNCTION_NAME, expressions);
	}
    
    @Override
	public Object evaluate() {
        MathContext mc = new MathContext(15, RoundingMode.HALF_UP);
        BigDecimal power;
        BigDecimal result = new BigDecimal(0.0);
        List<IConstraintExpression> differenceList = new ArrayList<>();
		try {
			for (IConstraintExpression expression : operands) {
				if (expression instanceof CellReference) {
					differenceList.addAll(((CellReference) expression).getReferencedExpressions());
				} else
					differenceList.add(expression);
			}
            
            IConstraintExpression[] differenceArray = new IConstraintExpression[differenceList.size()];
            for(int i = 0; i < differenceList.size(); i++)
                differenceArray[i] = differenceList.get(i);
            
            AvgFunction avgFunction = new AvgFunction(differenceArray);
            double average = (double)avgFunction.evaluate();
                    
            for(IConstraintExpression expr : differenceList) {    
                power = new BigDecimal(0);
                Object evalExpr = expr.evaluate();
                if(evalExpr instanceof Integer || evalExpr instanceof Double)
                {
                    power =  new BigDecimal((Double)evalExpr - average);
                    power = power.pow(2);
                    result = result.add(power);
                }
            }

            result = result.divide(new BigDecimal((double)differenceList.size()), mc);
            return Double.valueOf(result.toPlainString());
            
		} catch (Exception e) {
			throw new EvaluationException("Error during calculating var", e);
        }
    }

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
        //TODO: Implement me
        throw new NotImplementedException("Function: var -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: var -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: var -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
        List<IConstraintExpression> differenceList = new ArrayList<>();
        for (IConstraintExpression expression : operands) {
            if (expression instanceof CellReference) {
                differenceList.addAll(((CellReference) expression).getReferencedExpressions());
            } else
                differenceList.add(expression);
        }
        
        IConstraintExpression[] differenceArray = new IConstraintExpression[differenceList.size()];
        for(int i = 0; i < differenceList.size(); i++)
            differenceArray[i] = differenceList.get(i);
        
        AvgFunction avgFunction = new AvgFunction(differenceArray);
        ArithExpr avg = (ArithExpr)avgFunction.getZ3ValueConstraint(info);
        
        ArithExpr[] powArray = new ArithExpr[differenceList.size()];
        int i = 0;
        for(IConstraintExpression expr : differenceList)
        {
            ArithExpr arithExpr = (ArithExpr)expr.getZ3Constraint(info);
            arithExpr = info.getContext().MkSub(new ArithExpr[] {arithExpr, avg});
            powArray[i] = info.getContext().MkPower(arithExpr, info.getContext().MkInt(2));
            i++;
        }
        
        ArithExpr sum = info.getContext().MkAdd(powArray);
        return info.getContext().MkDiv(sum, info.getContext().MkInt(differenceList.size()));
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
		return super.toString(VAR_FUNCTION_NAME, operands);
	}
}
