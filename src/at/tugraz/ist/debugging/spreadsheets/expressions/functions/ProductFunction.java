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
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.ExpressionReturnType;
import choco.IPretty;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
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
public class ProductFunction extends ShellFunction {

    public ProductFunction(IConstraintExpression[] expressions) {
		super(Function.PRODUCT_FUNCTION_NAME, expressions);
	}
    
    @Override
	public Object evaluate() {
        
        BigDecimal doubleProduct = new BigDecimal(1);
		int intProduct = 1;
		ExpressionReturnType returnType = ExpressionReturnType.Integer;

		List<Object> results = new LinkedList<>();
		List<IConstraintExpression> flatExpr = new LinkedList<>();
		try {
			for (IConstraintExpression expression : operands) {
				results.clear();
				// evaluate all results
				if (expression instanceof CellReference) {
					// evaluate referenced cells
					flatExpr.addAll(((CellReference) expression).getReferencedExpressions());

				} else {
					// evaluate formula
					flatExpr.add(expression);
				}
			}

			for (IConstraintExpression expr : flatExpr) {
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
		} catch (Exception e) {
			throw new EvaluationException("Error during calculating product", e);
		}

		if (returnType == ExpressionReturnType.Integer)
			return intProduct;
		return Double.valueOf(doubleProduct.toPlainString());
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
        //TODO: Implement me
        throw new NotImplementedException("Function: product -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: product -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: product -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
        		List<ArithExpr> expressions = new ArrayList<>();
		for (IConstraintExpression multiplicand : operands) {
			if (multiplicand instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) multiplicand).getDirectlyReferencedCells()) {
					Expr expression = info.getVariables().get(cell);
					if (expression instanceof IntExpr
							|| expression instanceof RealExpr) {
						expressions.add((ArithExpr) expression);
					}
				}
			} else {
				Expr expression = multiplicand.getZ3Constraint(info);
				if (expression instanceof IntExpr
						|| expression instanceof RealExpr) {
					expressions.add((ArithExpr) expression);
				}
			}
		}

		// convert all participants to the sort type corresponding to the
		// resulting value
		SMTCodeGenerator.SortType targetType = info.determineSort(evaluate());
		ArithExpr[] multiplicandsArray = new ArithExpr[expressions.size()];
		multiplicandsArray = expressions.toArray(multiplicandsArray);
		for (int i = 0; i < multiplicandsArray.length; i++) {
			info.toSortType(multiplicandsArray[i], targetType);
		}
		return info.getContext().MkMul(multiplicandsArray);
	}
    
	@Override
	public Boolean isEquivalencePossible() {
		for (IConstraintExpression expression : operands)
            if(expression instanceof CellReference){
                for(Cell cell : ((CellReference)expression).getDirectlyReferencedCells()){
                    if(cell.evaluate() instanceof Double && ((Double)cell.evaluate()).equals(0.0) ||
                       cell.evaluate() instanceof Integer && ((int)cell.evaluate()) == 0)
                        return false;
                }
            } else if(expression.evaluate() instanceof Double && ((Double)expression.evaluate()).equals(0.0) ||
                      expression.evaluate() instanceof Integer && ((int)expression.evaluate()) == 0)
                        return false;
        return true;
	}

	@Override
	public String toString() {
		return super.toString(PRODUCT_FUNCTION_NAME, operands);
	}
}
