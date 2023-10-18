package at.tugraz.ist.debugging.modelbased.smt;

import java.io.IOException;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.Strategy;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator.SortType;
import at.tugraz.ist.debugging.modelbased.smt.parser.SolverOutputParser;
import at.tugraz.ist.debugging.modelbased.smt.parser.Z3Parser;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.util.debugging.Writer;

public abstract class SMTConstraintStrategy extends ConstraintStrategy {
	public static final String Z3_PATH = "z3.exe";
	/**
	 * abnormal variables
	 */
	protected String[] abnormals;

	protected SMTCodeGenerator generator;

	/**
	 * SMT code generator wrapper
	 */
	protected SMTConstraintStrategyGenerationInformation info;

	protected SolverOutputParser parser;
	/**
	 * weight variable (upper bound needed for AtMost constraint)
	 */
	protected String weightVariable;
	protected Process solverInstance;

	public SMTConstraintStrategy() {
	}

	/**
	 * Creates the variables for each cell of the cell container including the
	 * cell variable as well as the abnormal variable for non-input cells
	 * 
	 * @param cellContainer
	 * @param info
	 * @throws IOException
	 */
	protected void createVariables(CellContainer cellContainer,
			SMTConstraintStrategyGenerationInformation info) throws IOException {
		for (Cell cell : cellContainer.getCells()) {
			// ignore cell if it is not in the cone
			if (ConstraintStrategyConfiguration.useCones() && !cones.contains(cell))
				continue;

			Object value = cell.evaluate();
			String cellPosition = cell.getCoords().getConstraintString();
			String cellVariableName = VAR_CELL_NAME_PREFIX + cellPosition;

			boolean supported = true;
			if (value instanceof Boolean) {
				info.addVariable(cell, cellVariableName, SortType.Bool);
			} else if (value instanceof Integer) {
				info.addVariable(cell, cellVariableName, SortType.Int);
			} else if (value instanceof Double) {
				info.addVariable(cell, cellVariableName, SortType.Real);
			} else if (value instanceof String) {
				if (ConstraintStrategyConfiguration.useStrings())
					info.addVariable(cell, cellVariableName, SortType.Int);
				else
					supported = false;
			}

			else if (value == null) {
				// RESTRICTION: null cells are mapped to integers
				info.addVariable(cell, cellVariableName, SortType.Int);
				Writer.println(String
						.format("Warning (Constraint strategy): Cell %s is mapped to an integer variable",
								cell.getCoords().getUserString()));
			}
			// throw new InvalidOperationException(String.format(
			else
				throw new InvalidOperationException(String.format(
						"Cell %s: Result type '%s' is not supported",
						cell.getCoords().getUserString(), value.getClass().getName()));

			if (supported && !cellContainer.isInputCell(cell)) {
				String abnormalCellVariablePrefix = VAR_CELL_NOTABNORMAL_PREFIX
						+ cellPosition;
				// add abnormal variable
				info.addAbnormalVariable(cell, abnormalCellVariablePrefix);
			}
		}
	}

	@Override
	protected void initialize(CellContainer cellContainer,
			Set<String> wrongCells) {
		try {
            Strategy strategy = ConstraintStrategyConfiguration.getStrategy();
            switch(strategy.getSolver())
            {
                case Z3:
                    initializeZ3();
                    break;
                default:
                    throw new UnsupportedOperationException("Solver not supported!");
                
            }
		} catch (IOException e) {
			throw new RuntimeException(
					"SMT: Error while initializing pipe to process");
		}
	}
    
    private void initializeZ3() throws IOException
    {
        solverInstance = Runtime.getRuntime().exec( new String[] { 
            Z3_PATH, "-smt2", "-in" });
        generator = new SMTCodeGenerator(solverInstance.getOutputStream());
        parser = new Z3Parser(solverInstance.getInputStream());
    }
}
