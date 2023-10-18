package at.tugraz.ist.debugging.modelbased.z3api;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.DoubleConstant;
import at.tugraz.ist.util.debugging.Writer;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Z3Exception;
import java.util.Map;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * Information class which is needed during the generation of the value based 
 * Z3 clauses which represent the cells.
 */
public class Z3ValueBasedModelGenerationInformation extends
		Z3ConstraintStrategyGenerationInformation {
    
	/**
	 * Blocking clause structure
	 * 
	 */
	public enum Avoidance {
		/**
		 * Block the model as is (including all notabnormal assignments)
		 */
		All,
		/**
		 * Include false notabnormal assignments in the blocking clause only
		 */
		FalseOnly,
		/**
		 * Include true notabnormal assignments in the blocking clause only
		 */
		TrueOnly
	}

	public Z3ValueBasedModelGenerationInformation(Context ctx,
			Solver solver, Set<Cell> cone, boolean useCones, EModelGranularity modelGranularity) {
		super(ctx, solver, cone, useCones, modelGranularity);
	}
    
    /**
	 * Adds a cell assignment assertion to the solver
	 * 
     * This method distincts between input-, error dependent and normal cells.
     * The assertion is structured as follows.
     * - Input and normal cells: cellVariable = expression 
     * - error dependent cells: non-abnormalVariable => cellVariable = expression
	 * 
	 * @param cell
	 * @param expr
	 * @throws Z3Exception
	 */
    @Override
	public void addCellAssignment(Cell cell, Expr expr) throws Z3Exception {
		if (notabnormalVariablesMap.containsKey(cell)) {
			solver.Assert(ctx.MkImplies(notabnormalVariablesMap.get(cell),
					ctx.MkEq(variables.get(cell), expr)));
		} else
			solver.Assert(ctx.MkEq(variables.get(cell), expr));
	}
    
    @Override
    public void addConstraintsForOutputCells(Cell currentCell, 
            Map.Entry<Coords, ConstExpression> entry) throws Z3Exception {

        //+/- epsilon
        if(entry.getValue().evaluate() instanceof Double)
        {
            //excel rounds up to the 13 decimal place (so epsilon has
            //also 13 decimal places). To compensate for this inaccuracy
            //we allow the expected value to be between +/- epsilon.
            double epsilon = 0.0000000000005;
            double value = (double)entry.getValue().evaluate();

            solver.Assert(ctx.MkLe((ArithExpr)getVariables().get(currentCell), 
                    (ArithExpr)new DoubleConstant(value + epsilon).getZ3Constraint(this)));
            solver.Assert(ctx.MkGe((ArithExpr)getVariables().get(currentCell), 
                    (ArithExpr)new DoubleConstant(value - epsilon).getZ3Constraint(this)));
            return;
        }

        solver.Assert(ctx.MkEq(getVariables().get(currentCell), 
                entry.getValue().getZ3Constraint(this)));
    }
	
    @Override
	public void createVariables(CellContainer cellContainer,
			boolean useStrings, String varPrefix) throws Z3Exception {
		for (Cell cell : cellContainer.getCells()) {
			// ignore cells which are not in the cone
			if (useCones && !cone.contains(cell))
				continue;

			Object value = cell.evaluate();
            Set<Cell> references = cell.getExpression().getReferencedCells(true, true);
			String cellPosition = cell.getCoords().getConstraintString();
			String cellVariableName = varPrefix
					+ ConstraintStrategy.VAR_CELL_NAME_PREFIX + cellPosition;

            for(Cell reference : references)
                if(!cellContainer.getCells().contains(reference))
                    createNullVariable(reference, useStrings, varPrefix);
            
			boolean supported = true;
			if (value instanceof Boolean) {
				addVariable(cell, ctx.MkBoolConst(cellVariableName));
			//} else if (value instanceof Integer) {
			//	addVariable(cell, ctx.MkIntConst(cellVariableName));
			} else if (value instanceof Double || value instanceof Integer) {
				addVariable(cell, ctx.MkRealConst(cellVariableName));
			} else if (value instanceof String) {
				if (useStrings)
					addVariable(cell, ctx.MkIntConst(cellVariableName));
				else
					supported = false;
			}
			else if (value == null) {
				// RESTRICTION: null cells are mapped to integers
				createNullVariable(cell, useStrings, varPrefix);
				Writer.println(String
						.format("Warning (Constraint strategy): Cell %s is mapped to an integer variable",
								cell.getCoords().getUserString()));
			}
			else
				throw new InvalidOperationException(String.format(
						"Cell %s: Result type '%s' is not supported",
						cell.getCoords().getUserString(), value.getClass().getName()));

			if (supported && cone.contains(cell)) {
				String notabnormalCellVariableName = varPrefix
						+ ConstraintStrategy.VAR_CELL_NOTABNORMAL_PREFIX
						+ cellPosition;
				// add notabnormal variable
				addNotabnormalVariable(cell,
						ctx.MkBoolConst(notabnormalCellVariableName));
			}
		}
	}
}
