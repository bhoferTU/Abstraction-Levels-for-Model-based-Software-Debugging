package at.tugraz.ist.debugging.modelbased.minion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.minion.MinionCaller.MinionCallerCommunicationMode;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyResult;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.debugging.modelbased.main.MinionFileCreator;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.fileManipulation.FileTools;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;

public class MinionConstraintStrategy extends ConstraintStrategy {
	private static final boolean CLEANUP_TMP_FILES = false;
	private static final MinionCallerCommunicationMode COMMUNICATION_MODE = MinionCallerCommunicationMode.PipeBased;
	final static String MINION_FILE = "tmp.minion";
	
	public static ConstraintStrategyResult result = null;

	public static String getMinionFileName() {
		return MINION_FILE;
	}

	public static TimeSpan getComputationTime(String minionSolution) throws MinionTimeOutoutException {
		String[] lines = minionSolution.split("\n");
		for (String line : lines) {
			if (line.startsWith("Total Wall Time: ")) {
				String substring = line.substring("Total Wall Time: ".length());// in
																				// seconds
				System.out.println(line);
				Double duration = Double.parseDouble(substring);
//				System.out.println("Computed: "+duration.toString());
				TimeSpan timeSpan = new TimeSpan(duration, Precision.SECONDS);
//				System.out.println("TimeSpan: "+timeSpan.toString());
				return timeSpan;
			}
		}
		for(int i=0;i<21;i++){
			System.out.println(i+": "+lines[i]);
		}
		System.out.println("...\n...\n...");
		for(int i=lines.length-10;i<lines.length;i++){
			System.out.println(i+": "+lines[i]);
		}
		
		System.out.println(minionSolution);
		throw new MinionTimeOutoutException(
				"Could not find total wall time in MINION output");
	}

	public static List<List<Cell>> getSolutions(String minionSolution,
			MinionConstraintStrategyGenerationInformation info) {
		List<List<Cell>> solutions = new ArrayList<List<Cell>>();
		String[] lines = minionSolution.split("\n");
		for (String line : lines) {
			if (line.startsWith("Sol: ")) {
				String[] parts = line.split(" ");
				List<Cell> solution = new ArrayList<Cell>();
				for (int i = 1; i < parts.length; i++) {
					if (parts[i].equalsIgnoreCase("1"))
						solution.add(info.getCellForAbnormalIndex(i - 1));
				}
				solutions.add(solution);
			}
		}
		return solutions;
	}

	public static boolean isSubsetContained(List<Cell> element,
			List<List<Cell>> set) {
		for (List<Cell> element2 : set) {
			boolean contained = true;
			for (Cell cell : element2) {
				if (!element.contains(cell))
					contained = false;
			}
			if (contained)
				return true;
		}
		return false;
	}

	@Override
	protected void initialize(CellContainer cellContainer,
			Set<String> wrongCells) {

	}

	@Override
	public ConstraintStrategyResult solveConstraints(
			CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues){
		
		

		MinionConstraintStrategyGenerationInformation minionInfo = 
                new MinionConstraintStrategyGenerationInformation(
				cones, ConstraintStrategyConfiguration.useCones(), 
                        ConstraintStrategyConfiguration.getModelGranularity(), MinionFileCreator.ALL_DIAGNOSES);

		switch (ConstraintStrategyConfiguration.getModelGranularity()) {
		case Value:
			minionInfo.convertValueBased(cellContainer,
					ConstraintStrategyConfiguration.useStrings());
			break;
		case Simple:
		case Sophisticated:
			minionInfo.convertDependencyBased(cellContainer,
					ConstraintStrategyConfiguration.useStrings());
			break;
		case Dependency:
			minionInfo.convertDependencyBased(cellContainer, ConstraintStrategyConfiguration.useStrings());
			break;
		case Comparison:
			minionInfo.convertComparisonBased(cellContainer, ConstraintStrategyConfiguration.useStrings());
			break;
		default:
			System.err.println("Model granularity "
					+ ConstraintStrategyConfiguration.getModelGranularity() + " not supported!");
			break;
		}

		numConstraints = minionInfo.getNumberOfConstraints();
		for (Coords cell : referenceValues.keySet()) {
			String cellName = cell.getMinionString(); 
			String value = referenceValues.get(cell).getValueAsString();
			if (value.equalsIgnoreCase("false")) {
				value = "0";
			} else if (value.equalsIgnoreCase("true")) {
				value = "1";
			}
			MinionExpressionConstraints tc = null;
			switch (ConstraintStrategyConfiguration.getModelGranularity()) {
			case Value:
				tc = MinionConstraints.getConstantDefinition(value, cellName,
						minionInfo.getVariableDomain(cellName));
				break;
			case Simple:
			case Sophisticated:

				tc = MinionConstraints.getConstantDefinition(value, cellName,
						Domain.BOOLEAN);
				break;
			case Dependency:

				tc = MinionConstraints.getConstantDefinition(value, cellName, Domain.BOOLEAN);
				break;
			case Comparison:
				tc = MinionConstraints.getConstantDefinition(value, cellName, Domain.INT3);
				break;
			default:
				System.err.println("Model granularity "
						+ ConstraintStrategyConfiguration.getModelGranularity()
						+ " not supported!");
				break;
			}

			minionInfo.addTestCase(tc.getConstraintsTC());
		}

		List<List<Cell>> solutions = new ArrayList<List<Cell>>();
		
		result = new ConstraintStrategyResult(solutions, minionInfo); 

		int diagSize =0;
		for (diagSize = 1; diagSize <= minionInfo.getNumberAbnormalVariables(); diagSize++) {
			if (ConstraintStrategyConfiguration.useEarlyTermination() && solutions.size() > 0)
				break;
			
			if(ConstraintStrategyConfiguration.getMaxDiagnosesSize()!=-1 && diagSize>ConstraintStrategyConfiguration.getMaxDiagnosesSize())
				break;

//			if (i > 1)
//				break;
			
			System.out.println("compute diagnoses of size "+diagSize);

			MinionCaller minionCaller = MinionCaller.create(COMMUNICATION_MODE);
			minionInfo.setSolutionSize(diagSize);
			minionInfo.setBlockingSolutions(solutions);
			FileTools.writeToFile(MINION_FILE, minionInfo.toString());

			if(MinionFileCreator.STORE_INTERMEDIATE_MINION_FILES){
				String blockNonminimalDiag = MinionFileCreator.ALL_DIAGNOSES?"_AllDiagnoses":"_BlockNonminmalDiagnoses";
				String fileName = MinionFileCreator.CURRENT_FILE.replace(".minion", MinionFileCreator.CURRENT_MODEL+"__"+diagSize+"_DIAGSIZE"+blockNonminimalDiag+".minion");
				FileTools.writeToFile(fileName, minionInfo.toString());
			}

			MinionSolverResult result2 = minionCaller.getSolution(MINION_FILE);
			// TODO: check if result is null and check how to handle thread
			// termination
//			Writer.println(result.getSolution());
			Writer.print(result2.getError());

			if (!result2.getError().equalsIgnoreCase("")) {
				throw new RuntimeException(result2.getError());
			}

			List<List<Cell>> currentSolutions = getSolutions(
					result2.getSolution(), minionInfo);
			
			result.addSolutions(currentSolutions);
			
//			for (List<Cell> solution : currentSolutions) {
				
				//if (!isSubsetContained(solution, solutions))
				//	solutions.add(solution);
//			}
			try {
				result.addSolvingTime(diagSize, getComputationTime(result2.getSolution()));
				
			} catch (MinionTimeOutoutException e) {
				System.err.println(e.getMessage());
				//runtime.put(i, null);
				break;
			}

			if (CLEANUP_TMP_FILES) {
				minionCaller.cleanup(MINION_FILE);
				minionCaller.cleanup();
			}
		}
        

        result.setMaxDiagnosisSize(diagSize-1);
		return result;
	}

}
