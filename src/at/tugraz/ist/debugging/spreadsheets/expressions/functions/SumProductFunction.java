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
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.ExpressionReturnType;
import choco.IPretty;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Angi
 */
public class SumProductFunction extends ShellFunction {

    public SumProductFunction(IConstraintExpression[] expressions) {
		super(Function.SUMPRODUCT_FUNCTION_NAME, expressions);
	}
    
    @Override
	public Object evaluate() {
        
        BigDecimal doubleProduct = new BigDecimal(1);
		int intProduct = 1;
        List<BigDecimal> doubleResults = new LinkedList<>();
        List<Integer> intResults = new LinkedList<>();
		ExpressionReturnType returnType = ExpressionReturnType.Integer;
		
		List<List<IConstraintExpression>> flatExpr = new LinkedList<>();
		try {
			for (IConstraintExpression expression : operands) {
				List<IConstraintExpression> productList = new ArrayList<>();
				// evaluate all results
				if (expression instanceof CellReference) {
					// evaluate referenced cells
					productList.addAll(((CellReference) expression).getReferencedExpressions());

				} else {
					// evaluate formula
					productList.add(expression);
				}
                flatExpr.add(productList);
			}

            for(List<IConstraintExpression> list : flatExpr){
                for (IConstraintExpression expr : list) {
                    Object result = expr.evaluate();
                    if (returnType == ExpressionReturnType.Integer) {
                        if (result instanceof Integer)
                            intProduct *= (Integer) result;
                        else if (result instanceof Double) {
                            doubleProduct = new BigDecimal(intProduct).multiply((new BigDecimal(
                                    (Double) result)));
                            returnType = ExpressionReturnType.Double;
                        }
                    } else if (returnType == ExpressionReturnType.Double) {
                        if (result instanceof Integer)
                            doubleProduct = doubleProduct.multiply(new BigDecimal(
                                    (Integer) result));
                        else if (result instanceof Double)
                            doubleProduct = doubleProduct.multiply(new BigDecimal(
                                    (Double) result));
                    }
                }
                intResults.add(intProduct);
                doubleResults.add(doubleProduct);
            }
            
            if (returnType == ExpressionReturnType.Integer)
            {
                int intSum = 0;
                for(int product : intResults)
                    intSum += product;
                
                return intSum;
            }
            
            BigDecimal doubleSum = new BigDecimal(0);
            for(BigDecimal product : doubleResults)
                doubleSum = doubleSum.add(product);
            
            return Double.valueOf(doubleSum.toPlainString());
            
		} catch (Exception e) {
			throw new EvaluationException("Error during calculating product", e);
		}
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
        //TODO: Implement me
        throw new NotImplementedException("Function: sumproduct -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: sumproduct -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: sumproduct -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {		
		List<List<IConstraintExpression>> flatExpr = new LinkedList<>();
        for (IConstraintExpression expression : operands) {
            List<IConstraintExpression> productList = new ArrayList<>();
            if (expression instanceof CellReference) {
                productList.addAll(((CellReference) expression).getReferencedExpressions());
            } else {
                productList.add(expression);
            }
            flatExpr.add(productList);
        }

        ArithExpr[] summandsArray = new ArithExpr[flatExpr.size()];
        for(int i = 0; i < flatExpr.size(); i++) {
            List<IConstraintExpression> list = flatExpr.get(i);
            ArithExpr[] multiplicandsArray = new ArithExpr[list.size()];
            for(int j = 0; j < list.size(); j++)
                multiplicandsArray[j] = (ArithExpr)list.get(j).getZ3Constraint(info);

            summandsArray[i] = info.getContext().MkMul(multiplicandsArray);
        }
        
        return info.getContext().MkAdd(summandsArray);
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
		return super.toString(SUMPRODUCT_FUNCTION_NAME, operands);
	}
}
