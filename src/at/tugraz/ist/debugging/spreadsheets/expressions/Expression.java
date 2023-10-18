package at.tugraz.ist.debugging.spreadsheets.expressions;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3DependencyBasedModelGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import choco.IPretty;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Angi
 */
public abstract class Expression implements IConstraintExpression{

    protected IConstraintExpression[] operands;
    
    public Expression()
    {
        
    }
    
    public Expression(IConstraintExpression operand1,
			IConstraintExpression operand2)
    {
        operands = new IConstraintExpression[2];
        operands[0] = operand1;
        operands[1] = operand2;
    }
    public Expression(IConstraintExpression[] operands)
    {
        this.operands = operands;
    }
    
    @Override
    public abstract Set<IConstraintExpression> getConditionalExpressions();

    @Override
    public abstract Object evaluate();

    @Override
    public abstract IPretty getChocoConstraint(ChocoConstraintStrategyGenerationInformation info);

    @Override
    public abstract MinionExpressionConstraints getMinionValueConstraints(
            MinionConstraintStrategyGenerationInformation info);

    @Override
    public abstract String getSMTConstraint(SMTConstraintStrategyGenerationInformation info);

    @Override
    public abstract Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst);

    @Override
    public abstract Set<Coords> getReferences(boolean dynamic);
    
    @Override
    public abstract int getNumberOperations();

    @Override
    public abstract Boolean isEquivalencePossible();
    
    
    /**
	 * Generates a Z3 expression hierarchy which represents the current expression. 
     * This is done by recursive calls of the same function.
	 * 
	 * @param info
	 *            Z3 code generation information
	 * @return
	 * @throws Z3Exception
	 */
    @Override
	public Expr getZ3Constraint(Z3ConstraintStrategyGenerationInformation info)
			throws Z3Exception
    {
        if(info instanceof Z3ValueBasedModelGenerationInformation)
            return getZ3ValueConstraint((Z3ValueBasedModelGenerationInformation)info);
        
        if(info instanceof Z3DependencyBasedModelGenerationInformation)
            return getZ3DependencyConstraint((Z3DependencyBasedModelGenerationInformation)info);
        
        return null;
    }
    
    /**
	 * Generates a Z3 expression for the value model hierarchy which represents 
     * the current expression. 
     * This is done by recursive calls of the same function.
	 * 
	 * @param info
	 *            Z3 code generation information
	 * @return
	 * @throws Z3Exception
	 */
	protected abstract Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception;

    /**
	 * Generates a Z3 expression for the dependency model hierarchy which 
     * represents the current expression. 
     * This is done by recursive calls of the same function.
	 * 
	 * @param info
	 *            Z3 code generation information
	 * @return
	 * @throws Z3Exception
	 */
	protected Expr getZ3DependencyConstraint(Z3DependencyBasedModelGenerationInformation info)
			throws Z3Exception
    {
        Context ctx = info.getContext();
        List<BoolExpr> expressions = new ArrayList<BoolExpr>();
        for(IConstraintExpression expr : operands)
        {
            for(Cell cell : expr.getReferencedCells(false, true))
            {
                Expr expression = info.getVariables().get(cell);
                if(expression != null)
                    expressions.add((BoolExpr)expression);
            }
        }
        
        if(expressions.size()>0) {
            if(expressions.size() == 1)
                return expressions.get(0);
            
            BoolExpr[] boolExpressions = new BoolExpr[expressions.size()];
            for(int i = 0; i<expressions.size(); i++)
                boolExpressions[i] = expressions.get(i);
            
            return ctx.MkAnd(boolExpressions);
        } else {
            boolean allConstants = true;
            for(IConstraintExpression expr : operands)
                if(!(expr instanceof ConstExpression) && !(expr instanceof BinaryNumericOperation))
                    allConstants = false;
            if(allConstants)
                return ctx.MkBool(true);
            else
                return null;
        }
    }
    
}
