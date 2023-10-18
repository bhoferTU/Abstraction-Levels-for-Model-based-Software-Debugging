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
import com.microsoft.z3.ArithExpr;
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
public class RankFunction extends ShellFunction {

    public RankFunction(IConstraintExpression[] expressions) {
		super(Function.RANK_FUNCTION_NAME, expressions);
	}
    
    @Override
	public Object evaluate() {
        double value = (double)operands[0].evaluate();
        int order = 0;
        if(operands.length == 3)
            order = ((Double)operands[2].evaluate()).intValue();
        
        List<IConstraintExpression> references = new ArrayList<IConstraintExpression>();
        List<RankExpression> sortedList = new ArrayList<RankExpression>();
        if (operands[1] instanceof CellReference) {
            references.addAll(((CellReference) operands[1]).getReferencedExpressions());
        } else
            references.add(operands[1]);
        
        for(IConstraintExpression expr : references)
            if(expr.evaluate() instanceof Integer || expr.evaluate() instanceof Double)
                sortedList.add(new RankExpression(expr));
        
        int count = 1;    
        if(order == 1) {
            Collections.sort(sortedList);
            for(RankExpression expr : sortedList) {
                if(value <= expr.getValue())
                    return (double)count;
                count++;
            }
        } else {
            Collections.sort(sortedList, Collections.reverseOrder());
            for(RankExpression expr : sortedList) {
                if(value >= expr.getValue())
                    return (double)count;
                count++;
            }
        }
        return 0.0;
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
        ArithExpr value = (ArithExpr)operands[0].getZ3Constraint(info);
        int order = 0;
        if(operands.length == 3)
            order = ((Double)operands[2].evaluate()).intValue();
        
        List<IConstraintExpression> references = new ArrayList<IConstraintExpression>();
        List<RankExpression> sortedList = new ArrayList<RankExpression>();
        if (operands[1] instanceof CellReference) {
            references.addAll(((CellReference) operands[1]).getReferencedExpressions());
        } else
            references.add(operands[1]);
        
        for(IConstraintExpression expr : references)
            if(expr.evaluate() instanceof Integer || expr.evaluate() instanceof Double)
                sortedList.add(new RankExpression(expr));
        
        int count = sortedList.size();
        Context ctx = info.getContext();
        Expr constraint;
        if(order == 1) {
            Collections.sort(sortedList);
            constraint = ctx.MkITE(ctx.MkLe(value, 
                (ArithExpr)sortedList.get(count-1).getExpression().getZ3Constraint(info)), 
                ctx.MkReal(count), ctx.MkReal(0));
        
            for(count = count-1; count > 0; count--) {
                constraint = ctx.MkITE(ctx.MkLe(value, 
                    (ArithExpr)sortedList.get(count-1).getExpression().getZ3Constraint(info)), 
                    ctx.MkReal(count), constraint);
            }
        } else {
            Collections.sort(sortedList, Collections.reverseOrder());
            constraint = ctx.MkITE(ctx.MkGe(value, 
                (ArithExpr)sortedList.get(count-1).getExpression().getZ3Constraint(info)), 
                ctx.MkReal(count), ctx.MkReal(0));
        
            for(count = count-1; count > 0; count--) {
                constraint = ctx.MkITE(ctx.MkGe(value, 
                    (ArithExpr)sortedList.get(count-1).getExpression().getZ3Constraint(info)), 
                    ctx.MkReal(count), constraint);
            }
        }

        return constraint;
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
		return super.toString(RANK_FUNCTION_NAME, operands);
	}
    
    public class RankExpression implements Comparable<Object>{
        private IConstraintExpression expr;
        private double value;
        
        public RankExpression(IConstraintExpression expr) {
            this.expr = expr;
            this.value = (double)expr.evaluate();
        }
        
        public IConstraintExpression getExpression() { return expr; }
        public double getValue() { return value; }

        @Override
        public int compareTo(Object t) {
            RankExpression compareToObject = (RankExpression)t;
            if(value < compareToObject.getValue())
                return -1;
            else if (value == compareToObject.getValue())
                return 0;
            else
                return 1;  
        }
    }
    
}
