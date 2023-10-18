package at.tugraz.ist.debugging.modelbased.solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.CellValue;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.IModelBasedStrategy;
import at.tugraz.ist.debugging.modelbased.ModelBasedResult;
import at.tugraz.ist.debugging.modelbased.minion.MinionTimeOutoutException;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.ModelConfig;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;
import com.microsoft.z3.Z3Exception;

/**
 * Base class for constraint based strategies
 * 
 */
public abstract class ConstraintStrategy implements IModelBasedStrategy {

	public static String externalProcess = null;

	/**
	 * Prefix for variables which represent notabnormal flags
	 */
	public static final String VAR_CELL_NOTABNORMAL_PREFIX = "nab";

	/**
	 * Prefix for cell variable names
	 */
	public static final String VAR_CELL_NAME_PREFIX = "var";

	protected Set<Cell> cones;
	protected int numConstraints = 0;

	/**
	 * Returns a string representation for a given diagnosis list
	 * 
	 * @param abnormalAssignments
	 * @return
	 */
	public String abnormalsToString(List<List<Cell>> abnormalAssignments) {
		StringBuilder s = new StringBuilder(String.format(
				"Determined %d solutions", abnormalAssignments.size()));

		for (List<Cell> assignment : abnormalAssignments) {
			s.append("\n");
			s.append("{");
			for (int i = 0; i < assignment.size(); i++) {
				if (i > 0)
					s.append(", ");
				s.append(assignment.get(i).getCoords().getConstraintString());
			}
			s.append("}");
		}
		return s.toString();
	}
    
    /**
     * checks whether dependency-based solutions are real solutions.
     * 
     * @param result diagnoses found by the solving algorithm
     * @param cellContainer cellcontainer
     * @param referenceValues reference values initialized for value-based 
     *                        model debugging
     * @return list of extended solutions.
     * @throws Z3Exception 
     */
    protected ConstraintStrategyResult checkSolution(
            ConstraintStrategyResult result, 
            CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues) throws Z3Exception
    {
        return result;
    }

	/**
	 * Initializes the debugging strategy (creation of solver instances etc.)
	 * 
	 * @param cellContainer
	 * @param wrongCells
	 */
	protected abstract void initialize(CellContainer cellContainer,
			Set<String> wrongCells);

    
    private void initializeConesNoInputCells(CellContainer cellContainer,
			Set<Coords> wrongCells, List<Coords> faultyCells) {
		cones = new HashSet<>();
        Set<Cell> wrongOutputCellsReferences = new HashSet<>();
		for (Coords cellCoords : wrongCells) {
			Cell cell = cellContainer.getCell(cellCoords);
			wrongOutputCellsReferences.add(cell);
			wrongOutputCellsReferences.addAll(cell.getAllReferencesRecursive());
		}
        for(Cell cell : wrongOutputCellsReferences)
            if(!cellContainer.isInputCell(cell) || faultyCells.contains(cell.getCoords()))
                cones.add(cell);      
	}

	@Override
	public ModelBasedResult runAlgorithm(ModelConfig data) {
        try {
            return solve(data.getCells(), data.getProperties());
        } catch (Z3Exception ex) {
            Writer.println(ex.getMessage());
        }
        return null;
	}

	private ModelBasedResult solve(CellContainer cellContainer,
			SpreadsheetProperties properties) throws Z3Exception {
        ConstraintStrategyConfiguration.setFaultyCells(properties.getFaultyCells());
		Map<Coords, String> wrongOutputCells = properties.getIncorrectOutputCellsWithExpectedValue();
		List<Coords> correctOutputCells = properties.getCorrectOutputCells();
        // reference values for correct cells
		Map<Coords, ConstExpression> referenceOutputCellValues = 
                getReferenceOutputCellValues(ConstraintStrategyConfiguration.getModelGranularity(),
                       cellContainer, correctOutputCells, wrongOutputCells);
        
		initializeConesNoInputCells(cellContainer, wrongOutputCells.keySet(), ConstraintStrategyConfiguration.getFaultyCells());
        
//		long start = System.currentTimeMillis();

		initialize(cellContainer, Coords.convertCoordsSet(wrongOutputCells.keySet()));

		ConstraintStrategyResult solution = solveConstraints(cellContainer, referenceOutputCellValues);

        
        if(ConstraintStrategyConfiguration.verifySolution() && (ConstraintStrategyConfiguration.getModelGranularity() == EModelGranularity.Simple ||
                ConstraintStrategyConfiguration.getModelGranularity() == EModelGranularity.Sophisticated))
        {
            referenceOutputCellValues = getReferenceOutputCellValues(
                    EModelGranularity.Value, cellContainer, correctOutputCells, 
                    wrongOutputCells);

            solution = checkSolution(solution, cellContainer, referenceOutputCellValues);
        }

//		long overallDuration = System.currentTimeMillis() - start;
		TimeSpan overallSolvingDuration = solution.getRuntime();
		// report runtimes
//		Writer.println("Overall duration :         " + overallDuration + "ms");
//		Writer.println(String.format("Overall solving duration : %s (%s)",
//				overallSolvingDuration.toString(Precision.MILLISECONDS),
//				overallSolvingDuration));

		// initialize result object
		ModelBasedResult result = new ModelBasedResult(
				properties.getExcelSheetName(), ConstraintStrategyConfiguration.getStrategy(),
				ConstraintStrategyConfiguration.useCones(), cellContainer.getFormulaCoords().size(),
				numConstraints);
		result.setRuntime(overallSolvingDuration.getTimeSpan(Precision.MILLISECONDS));
        result.addSolvingTimes(solution.getRuntimeSolvingTimes());
        result.setRuntimeValidating(solution.getRuntimeValidating(Precision.MILLISECONDS));
		result.addDiagnoses(solution.getHighPriorityDiagnoses(), solution.getLowPriorityDiagnoses());
		result.setMessage(solution.getConstraintBasedInformationAsString());
        result.setCones(cones);
        result.setMaxDiagnosisSize(solution.getMaxDiagnosisSize());
        
        //Remove input cells from result
        Set<Cell> inputVariables = new HashSet<>();
        for(Cell cell : cellContainer.getCells())
            if(cellContainer.isInputCell(cell) && 
                    !ConstraintStrategyConfiguration.getFaultyCells().contains(cell.getCoords()))
                inputVariables.add(cell);
        
        result.removeInputCells(inputVariables);
		return result;
	}
	
	/**
	 * Solves a given test case by applying the particular strategy
	 * implementation
	 * 
	 * @param cellContainer
	 *            Cell container, where the given input cells' values already
	 *            contain the particular values provided by the test case
	 * @param referenceValues
	 *            Observations of wrong output cells and its correct values
	 * @return
	 * @throws MinionTimeOutoutException 
	 */
	public abstract ConstraintStrategyResult solveConstraints(
			CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues);

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
    
    private Map<Coords, ConstExpression> getReferenceOutputCellValues(
            EModelGranularity modelGranularity, CellContainer cellContainer,
            List<Coords> correctOutputCells, Map<Coords, String> wrongCells)
    {
        Map<Coords, ConstExpression> referenceOutputCellValues = new HashMap<>();
		// reference values for correct cells
		switch (modelGranularity) {
		case Value:
			if (!ConstraintStrategyConfiguration.useCones()) {
				for (Coords cellCoords : correctOutputCells) {
                    /*if(cellContainer.getCell(cellCoords).getCellValue().getCellType() == 
                            org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING &&
                            !ConstraintStrategyConfiguration.useStrings())
                        continue;*/
					referenceOutputCellValues.put(cellCoords, ConstExpression
							.create(cellContainer.getCell(cellCoords)
									.getCellValue()));
				}
			}
			referenceOutputCellValues.putAll(ConstExpression
					.convertExpectedOutput(wrongCells));
			break;
		case Simple:
		case Sophisticated:
			if (!ConstraintStrategyConfiguration.useCones()) {
				for (Coords cellCoords : correctOutputCells) {
					referenceOutputCellValues.put(
							cellCoords,
							ConstExpression.create(new CellValue(1)));
				}
			}
			for (Coords cellCoords : wrongCells.keySet()) {
				referenceOutputCellValues.put(
						cellCoords,
						ConstExpression.create(new CellValue(0)));
			}
			break;
		case Dependency:
			if (!ConstraintStrategyConfiguration.useCones()) {
				for (Coords cellCoords : correctOutputCells) {
					referenceOutputCellValues.put(
							cellCoords,
							ConstExpression.create(new CellValue(1)));
				}
			}
			for (Coords cellCoords : wrongCells.keySet()) {
				referenceOutputCellValues.put(
						cellCoords,
						ConstExpression.create(new CellValue(0)));
			}
			break;
		case Comparison:
			if (!ConstraintStrategyConfiguration.useCones()) {
				for (Coords cellCoords : correctOutputCells) {
					referenceOutputCellValues.put(
							cellCoords,
							ConstExpression.create(new CellValue(1)));
				}
			}
			for (Coords cellCoords : wrongCells.keySet()) {
				double computedValue = cellContainer.getCell(cellCoords).getCellValue().getNumberValue();
				double expectedValue = Double.parseDouble(wrongCells.get(cellCoords));
				boolean isExpectedSmaller = expectedValue<computedValue;
				referenceOutputCellValues.put(
						cellCoords,
						ConstExpression.create(new CellValue(isExpectedSmaller?2:0)));
			}
			break;
		default:
			System.err.println("Granularity model "
					+ modelGranularity + " not supported!");
			break;
		}
        
        return referenceOutputCellValues;
    }
}
