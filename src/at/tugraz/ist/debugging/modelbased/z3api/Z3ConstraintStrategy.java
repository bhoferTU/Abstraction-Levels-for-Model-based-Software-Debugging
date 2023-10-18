package at.tugraz.ist.debugging.modelbased.z3api;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyResult;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpanMeasurement;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * Abstract base class for constraint debugging strategies which use the Z3 Java
 * API
 * 
 */
public abstract class Z3ConstraintStrategy extends ConstraintStrategy {

    protected TimeSpan runtime;
    protected List<List<Cell>> mcs;
    protected Z3ConstraintStrategyGenerationInformation info;
    protected TimeSpanMeasurement measurement;
    protected BoolExpr[] notabnormals;
    protected IntExpr weightVariable;
    
	/**
	 * Z3 context
	 */
	protected Context ctx;

	/**
	 * Z3 solver
	 */
	protected Solver solver;

	public Z3ConstraintStrategy() throws Z3Exception {
	}

	@Override
	protected void initialize(CellContainer cellContainer,
			Set<String> wrongCells) {
		try {
			HashMap<String, String> cfg = new HashMap<>();
			cfg.put("model", "true");
			cfg.put("unsat-core", "true");
			ctx = new Context(cfg);
			solver = ctx.MkSolver("QF_NRA");
		} catch (Z3Exception e) {
			throw new RuntimeException("Z3: Error while initializing context");
		}
	}
    
    public void initializeConstraints(CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues)
    {        
        switch(ConstraintStrategyConfiguration.getModelGranularity())
        {
            case Value:
                info = new Z3ValueBasedModelGenerationInformation(ctx, solver, 
                        cones, ConstraintStrategyConfiguration.useCones(), 
                        ConstraintStrategyConfiguration.getModelGranularity());
                break;
            case Simple:
            case Sophisticated:
                info = new Z3DependencyBasedModelGenerationInformation(ctx, 
                        solver, cones, ConstraintStrategyConfiguration.useCones(), 
                        ConstraintStrategyConfiguration.getModelGranularity());
                break;
            default:
                throw new AssertionError(ConstraintStrategyConfiguration.getModelGranularity().name());
        }
        runtime = new TimeSpan(0, ConstraintStrategyConfiguration.getPrecision());
        measurement = new TimeSpanMeasurement(ConstraintStrategyConfiguration.getPrecision());

        try
        {
        // STACK FRAME 1
        // create variables
        info.createVariables(cellContainer, ConstraintStrategyConfiguration.useStrings());
        info.initializeInputVariables(cellContainer);
        info.initializeNormalVariables(cellContainer);
        Writer.println(info.toString());

        // add constraints for notAbnormal cells
        for (Cell cell : info.getNotabnormalVariablesMap().keySet()) {
            cell.addZ3Constraint(info);
        }
        //add weight function; at least one notabnormal must be set
        //(otherwise we would not gain anything from a satisfied run)
        notabnormals = info.getNotabnormalVariables();
        weightVariable = info.addWeightFunction(info.getInvertedNotabnormals());
        solver.Push();

        // add formula constraints for correct cells
        for(Cell cell : info.getNormalCells())
                cell.addZ3Constraint(info);
        solver.Push();

        // add constraints for input cells
        for (Cell cell : info.getInputCells())
            if(!cones.contains(cell))
                cell.addZ3Constraint(info);
        solver.Push();

        // STACK FRAME 2
        // add value constraints for correct cells and for observed wrong cells
        for (Map.Entry<Coords, ConstExpression> entry : referenceValues.entrySet()) {
            Cell currentCell = cellContainer.getCell(entry.getKey());
            if (currentCell == null)
                throw new InvalidOperationException("ConstraintStrategy: Output cell must not be null");
            if (entry.getValue() != null)
                info.addConstraintsForOutputCells(currentCell, entry);
        }

        numConstraints = solver.NumAssertions();
        
        } catch (Z3Exception e) {
			throw new RuntimeException(e);
		}
    }
    
    public abstract ConstraintStrategyResult runSolvingAlgorithm();
    
    @Override
    public ConstraintStrategyResult solveConstraints(
			CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues)
    {
        initializeConstraints(cellContainer, referenceValues);
        return runSolvingAlgorithm();
    }
    
    
    @Override
    protected ConstraintStrategyResult checkSolution(ConstraintStrategyResult result, 
            CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues) throws Z3Exception
    {
        Status satisfiability;
        measurement = new TimeSpanMeasurement(ConstraintStrategyConfiguration.getPrecision());
        runtime = new TimeSpan(0, ConstraintStrategyConfiguration.getPrecision());
        runtime.add(result.getRuntime());
        
        mcs = result.getHighPriorityDiagnoses();
        mcs.addAll(result.getLowPriorityDiagnoses());
        List<List<Cell>> highPriorityMCS = new ArrayList<>();
        List<List<Cell>> lowPriorityMCS = new ArrayList<>();
        
        Z3ValueBasedModelGenerationInformation CheckSolutionInfo = new 
            Z3ValueBasedModelGenerationInformation(ctx, solver, cones, 
                ConstraintStrategyConfiguration.useCones(), EModelGranularity.Value);
        
        solver.Reset();
        
        // create variables
        CheckSolutionInfo.createVariables(cellContainer, ConstraintStrategyConfiguration.useStrings());
        CheckSolutionInfo.initializeInputVariables(cellContainer);
        CheckSolutionInfo.initializeNormalVariables(cellContainer);
        
        // add constraints for correct cells
        for(Cell cell : CheckSolutionInfo.getNormalCells())
            cell.addZ3Constraint(CheckSolutionInfo);

        // add constraints for input cells
        for (Cell cell : CheckSolutionInfo.getInputCells())
            cell.addZ3Constraint(CheckSolutionInfo);

        // add constraints for observed wrong cells
        for (Map.Entry<Coords, ConstExpression> entry : referenceValues.entrySet()) {
            Cell currentCell = cellContainer.getCell(entry.getKey());
            if (currentCell == null)
                throw new InvalidOperationException(
                        "ConstraintStrategy: Output cell must not be null");
            if (entry.getValue() != null)
                CheckSolutionInfo.addConstraintsForOutputCells(currentCell, entry);
        }
        solver.Push();

        Map<Cell, BoolExpr> constraintSet = new HashMap<>(); 
        // create constraints for notAbnormal cells
        for(Cell cell : CheckSolutionInfo.getNotabnormalVariablesMap().keySet())
            constraintSet.put(cell, cell.getZ3Constraint(CheckSolutionInfo));
        
        //verify diagnoses
        Map<Cell, BoolExpr> tempRemoval = new HashMap<>();
        for(List<Cell> correctionSet : mcs)
        {
            for(Cell correctionCell : correctionSet)
            {
                tempRemoval.put(correctionCell, constraintSet.get(correctionCell));
                constraintSet.remove(correctionCell);
            }
            for(BoolExpr expression : constraintSet.values())
            {
                solver.Assert(expression);
            }
            solver.Push();
            
            //System.out.println(solver.toString());
            measurement.start();
            satisfiability = solver.Check();
            runtime.add(measurement.stop());
            
            if(satisfiability == Status.SATISFIABLE)
                highPriorityMCS.add(correctionSet);
            else
                lowPriorityMCS.add(correctionSet);
            
            solver.Pop(2);
            solver.Push();

            constraintSet.putAll(tempRemoval);
            tempRemoval.clear();
        }
        ConstraintStrategyResult finalResult = new ConstraintStrategyResult(highPriorityMCS, lowPriorityMCS); 
        finalResult.setRuntimeSolving(result.getRuntimeSolving());
        finalResult.setRuntimeValidating(runtime);
        return finalResult;
    }
}
