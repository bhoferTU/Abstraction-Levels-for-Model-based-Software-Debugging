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
import choco.IPretty;
import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Angi
 */
public class SmallFunction extends ShellFunction {

    public SmallFunction(IConstraintExpression[] expressions) {
		super(Function.SMALL_FUNCTION_NAME, expressions);
	}
    
    @Override
	public Object evaluate() {
        int count = ((Double)operands[1].evaluate()).intValue();
        List<IConstraintExpression> references = new ArrayList<IConstraintExpression>();
        List<SmallExpression> sortedList = new ArrayList<SmallExpression>();
        if (operands[0] instanceof CellReference) {
            references.addAll(((CellReference) operands[0]).getReferencedExpressions());
        } else
            references.add(operands[0]);
        
        for(IConstraintExpression expr : references)
            if(expr.evaluate() instanceof Integer || expr.evaluate() instanceof Double)
                sortedList.add(new SmallExpression(expr));
           
        Collections.sort(sortedList);
        return sortedList.get(count-1).value;
    }

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
        //TODO: Implement me
        throw new NotImplementedException("Function: rank -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: rank -> get...Constraint not implemented");
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info)  {
        //TODO: Implement me
        throw new NotImplementedException("Function: rank -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
        Double count = (Double)operands[1].evaluate()-1;
        
        List<IConstraintExpression> references = new ArrayList<IConstraintExpression>();
        List<SmallExpression> sortedList = new ArrayList<SmallExpression>();
        if (operands[0] instanceof CellReference) {
            references.addAll(((CellReference) operands[0]).getReferencedExpressions());
        } else
            references.add(operands[0]);
        
        for(IConstraintExpression expr : references)
            if(expr.evaluate() instanceof Integer || expr.evaluate() instanceof Double)
                sortedList.add(new SmallExpression(expr));
        
        Collections.sort(sortedList);
        
        Context ctx = info.getContext();
        
        Double rand = Math.random();
        ArrayExpr array = ctx.MkArrayConst("array" + rand.toString(), ctx.RealSort(), ctx.RealSort());
        int i = 0;
        for(SmallExpression expr : sortedList){
            Double value = expr.getValue();
            ctx.MkStore(array, ctx.MkInt(i), ctx.MkReal(value.toString()));
            i++;
        }
        return ctx.MkSelect(array, ctx.MkReal(count.toString()));
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
		return super.toString(SMALL_FUNCTION_NAME, operands);
	}
    
    public class SmallExpression implements Comparable<Object>{
        private IConstraintExpression expr;
        private double value;
        
        public SmallExpression(IConstraintExpression expr) {
            this.expr = expr;
            this.value = (double)expr.evaluate();
        }
        
        public IConstraintExpression getExpression() { return expr; }
        public double getValue() { return value; }

        @Override
        public int compareTo(Object t) {
            SmallExpression compareToObject = (SmallExpression)t;
            if(value < compareToObject.getValue())
                return -1;
            else if (value == compareToObject.getValue())
                return 0;
            else
                return 1;  
        }
    }
    
}
