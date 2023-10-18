package at.tugraz.ist.debugging.modelbased.z3api;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Z3Exception;
import java.util.Map;
import java.util.Set;

/**
 * Information class which is needed during the generation of the dependency 
 * based Z3 clauses which represent the cells.
 */
public class Z3DependencyBasedModelGenerationInformation extends 
        Z3ConstraintStrategyGenerationInformation {

    public Z3DependencyBasedModelGenerationInformation(Context ctx, Solver solver, 
            Set<Cell> cone, boolean useCones, EModelGranularity modelGranularity) {
        super(ctx, solver, cone, useCones, modelGranularity);
    }

    /**
	 * Adds a cell assignment assertion to the solver
	 * 
	 * This method distincts between input-, error dependent and normal cells.
     * The assertion is structured as follows.
     * - Input and normal cells: cellVariable <-> expression
     * - error dependent cells: non-abnormalVariable v cellVariable <-> expression
	 * 
	 * @param cell
	 * @param expr
	 * @throws Z3Exception
	 */
    @Override
	public void addCellAssignment(Cell cell, Expr expr) throws Z3Exception {
        if(expr == null)
            return;
        if (notabnormalVariablesMap.containsKey(cell) || normalVariables.contains(cell))
        {
            BoolExpr expression;
            if(getModelGranularity() == EModelGranularity.Sophisticated &&
                    cell.getExpression().isEquivalencePossible())
                expression = getBiImpliesExpression(expr, variables.get(cell));
            else
                expression = getImpliesExpression(expr, variables.get(cell));

            if(notabnormalVariablesMap.containsKey(cell))
                expression = getImpliesExpression(notabnormalVariablesMap.get(cell), expression);
            
            solver.Assert(expression);
		} 
        else
            solver.Assert(ctx.MkEq(variables.get(cell), expr));
	}
    
    private BoolExpr getImpliesExpression(Expr expr, Expr currentExpr) throws Z3Exception
    {
        return ctx.MkImplies((BoolExpr)expr, (BoolExpr)currentExpr);
    }
    
    private BoolExpr getBiImpliesExpression(Expr expr, Expr currentExpr) throws Z3Exception
    {
        return ctx.MkEq(expr, currentExpr);
    }
    
    @Override
    public void createVariables(CellContainer cellContainer, boolean useStrings, 
            String varPrefix) throws Z3Exception {
        
        for(Cell cell : cellContainer.getCells())
        {
            if(useCones && !cone.contains(cell))
                continue;
            
            Object value = cell.evaluate();
            if(value instanceof String && !useStrings)
                continue;
            
            Set<Cell> references = cell.getExpression().getReferencedCells(true, true);
            for(Cell reference : references)
                if(reference.evaluate() == null)
                    createNullVariable(reference, useStrings, varPrefix);
         
            
            
            String cellPosition = cell.getCoords().getConstraintString();
			String cellVariableName = varPrefix
					+ ConstraintStrategy.VAR_CELL_NAME_PREFIX + cellPosition;
            
            addVariable(cell, ctx.MkBoolConst(cellVariableName));
            
            if (cone.contains(cell)) {
				String notabnormalCellVariableName = varPrefix
						+ ConstraintStrategy.VAR_CELL_NOTABNORMAL_PREFIX
						+ cellPosition;

				addNotabnormalVariable(cell,
						ctx.MkBoolConst(notabnormalCellVariableName));
			}
        }
    }            

    @Override
    public void addConstraintsForOutputCells(Cell currentCell, 
            Map.Entry<Coords, ConstExpression> entry) throws Z3Exception {

        boolean value = entry.getValue().getValueAsString().equals("1.0");

        solver.Assert(ctx.MkEq(getVariables().get(currentCell), ctx.MkBool(value)));
    }     
}
