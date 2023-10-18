package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.Diagnosis;
import at.tugraz.ist.debugging.modelbased.EDebuggingAlgorithm;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.ESolverAccessOption;
import at.tugraz.ist.debugging.modelbased.ModelBasedResult;
import at.tugraz.ist.debugging.modelbased.Strategy;
import at.tugraz.ist.debugging.modelbased.main.Executor;
import at.tugraz.ist.debugging.modelbased.main.MinionDebugger;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.Ochiai;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.ObservationMatrix;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.util.RuntimeProcessExecuter;
import at.tugraz.ist.util.IO.OutputConfigurator;
import at.tugraz.ist.util.datastructures.Pair;
import at.tugraz.ist.util.fileManipulation.Directory;

public class SimulateWrongUserInputEvaluation {

	static int repeditions = 100;
	static Random randomGenerator = new Random(System.currentTimeMillis());
	public static BufferedWriter writer = null;
	public static boolean fileExists = false;
	final static String PATH = "Benchmarks" + File.separator + "INTEGER" + File.separator + "configuration_files";
	public static Integer runs = 1;
	public static int timeout = 60 * 60 * 2; // in seconds

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OutputConfigurator.setOutputAndErrorStreamToFile("Results.log");
		String csvFile = "SimulateWrongUserInputEvaluation.csv";
		File f = new File(csvFile);

		if (f.exists()) {
			fileExists = true;
		}

		try {
			writer = new BufferedWriter(new FileWriter(csvFile, true));

		} catch (IOException e) {
			e.printStackTrace();
		}

		SimulateWrongUserInputEvaluation swuie = new SimulateWrongUserInputEvaluation();
		SimulateWrongUserInputEvaluation.setupConstraintBasedApproach();

		List<String> files = new ArrayList<String>();
		files = Directory.getFilesRecursively(PATH, ".properties");
//		 files.add("Benchmarks\\INTEGER\\configuration_files\\fromAFW\\AFW_training_1Faults_Fault4.properties");
		for (String file : files) {
			try {
				if(!file.contains("1Faults"))
					continue;
//				if(file.contains("AFW_a")||file.contains("AFW_b")||file.contains("AFW_c"))
//					continue;
				if(!(file.contains("AFW_birthdays")||file.contains("AFW_book")))
					continue;
//				if((file.contains("AFW_ranking_1Faults_Fault1")||file.contains("AFW_shopping_bedroom2_1Faults_Fault1")||file.contains("AFW_training_1Faults_Fault2")))
//					continue;
				SpreadsheetProperties properties = new SpreadsheetProperties(file);
				System.out.println(file);
//				swuie.debugModelBased(properties, new Result());
				swuie.simulate(properties);
			} catch (SpreadsheetPropertiesException e) {
				e.printStackTrace();
			}
		}

	}

	private static void setupConstraintBasedApproach() {
		Strategy strategy = new Strategy(ESolver.Minion, EDebuggingAlgorithm.ConstraintBased, ESolverAccessOption.API,
				EModelGranularity.Sophisticated);
		ConstraintStrategyConfiguration.setStrategy(strategy);
		ConstraintStrategyConfiguration.setUseCones(false);
		ConstraintStrategyConfiguration.setEarlyTermination(false);
		ConstraintStrategyConfiguration.setRuns(1);
		ConstraintStrategyConfiguration.setUseStrings(false);
		ConstraintStrategyConfiguration.setVerifySolution(false);
	}
	
NumberFormat formatter = NumberFormat.getNumberInstance(Locale.GERMAN);
	public void simulate(SpreadsheetProperties properties) {
		ObservationMatrix obs = setUpObservationMatrix(properties);
		int positiveTestingDecision = properties.getCorrectOutputCells().size();
		int negativeTestingDecision = properties.getIncorrectOutputCells().size();
		

		Map<Coords, Integer> coefficientRanking = obs.getCoefficientRanking(new Ochiai());
		Pair<Integer, Integer> baseResult = SflTestCaseReductionEvaluator.getRankingResult(coefficientRanking,
				properties.getFaultyCells());
		Double avgBaseResult = (double) baseResult.getFirst() + (double) baseResult.getSecond() / 2 - 0.5;
		Result result = new Result();
		result.addData("File", getShortName(properties.getExcelSheetName()));
		result.addData("Changed Positive Testing Decisions", "0");
		result.addData("Changed Negative Testing Decisions", "0");
		result.addData("Ranking best case", baseResult.getFirst().toString());
		result.addData("Size of critical tie", baseResult.getSecond().toString());
		result.addData("Average ranking", formatter.format(avgBaseResult));
		result.addData("Normalized improvement/worsening", "0");
		
		debugModelBased(properties, result, true);
		writeToFile(result);

		for (Integer i = 0; i <= positiveTestingDecision; i++) {
			for (Integer j = 0; j < negativeTestingDecision; j++) {
				if (i == 0 && j == 0)
					continue;
				for (int k = 0; k < repeditions; k++) {
//					boolean[] errorVector = obs.getErrorVector().clone();
					try {
//						changeTestingDecision(errorVector, i, j);
						SpreadsheetProperties newProperties = changeTestingDecision(properties, i, j);
//						ObservationMatrix obs2 = new ObservationMatrix(obs.getObservationMatrix(), obs.getCells(), errorVector);
						ObservationMatrix obs2 = setUpObservationMatrix(newProperties);
						coefficientRanking = obs2.getCoefficientRanking(new Ochiai());
						Pair<Integer, Integer> singleResult = SflTestCaseReductionEvaluator.getRankingResult(
								coefficientRanking, properties.getFaultyCells());
						result = new Result();
						result.addData("File", getShortName(properties.getExcelSheetName()));
						result.addData("Changed Positive Testing Decisions", i.toString());
						result.addData("Changed Negative Testing Decisions", j.toString());
						result.addData("Ranking best case", singleResult.getFirst().toString());
						result.addData("Size of critical tie", singleResult.getSecond().toString());
						Double avgResult = (double) singleResult.getFirst() + (double) singleResult.getSecond() / 2
								- 0.5;
						result.addData("Average ranking", formatter.format(avgResult));
						Double worsening = avgResult / avgBaseResult;
						result.addData("Normalized improvement/worsening", formatter.format(worsening - 1));
						debugModelBased(newProperties, result, false);
						writeToFile(result);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}

	}
	
	private int numDiagnoses=0;

	private void debugModelBased(SpreadsheetProperties properties, Result result2, boolean newDataSet) {
		int retries = 3;
		ModelBasedResult result = null;

		for (int r = 1; r <= runs; ++r) {
			if (MinionDebugger.isFailed()) {
				break;
			}
			Executor executor = new Executor(PATH, properties);

			try {
				executor.start();
			} catch (RuntimeException e) {
				if (retries-- == 0)
					throw e;
				--r;
				continue;
			}
			try {
				if (timeout <= 0)

					executor.join();

				else {
					executor.join(timeout * 1000);
				}
				result = executor.getResult();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (executor.isAlive()) {
				System.err.println(properties.getExcelSheetName() + " ... timeout!");
				MinionDebugger.setFailed(true);
				RuntimeProcessExecuter.killProcess(ConstraintStrategy.externalProcess);
			} else if (result == null) {
				System.err.println(properties.getExcelSheetName() + "failed!");
				MinionDebugger.setFailed(true);
			} else {

				// totaltime += executor.getResult().getRuntime();
				// System.out.println("Time: " +
				// executor.getResult().getRuntime());

				Diagnosis diag = new Diagnosis();
				for (Coords c : properties.getFaultMapping().keySet()) {
					diag.addFaultCell(new Cell(c.getColumn(), c.getRow() - 1, c.getWorksheet()));
				}
				result2.addData("DepBased: Required diagnoses", diag.toString());
				result2.addData("DepBased: Computed diagnoses", result.getDiagnosisAsString());
				result2.addData("DepBased: Size of computed dignoses set", getNumberOfCoords(result.getAllDiagnoses()).toString());
				if (!MinionDebugger.subDiagnosisFound(result.getAllDiagnoses(), diag)) {
					result2.addData("DepBased: Diagnosis contained", "NO");
				} else {
					result2.addData("DepBased: Diagnosis contained", "YES");
				}
				int numberDia = getNumberOfCoords(result.getAllDiagnoses());
				if(newDataSet)
					numDiagnoses=numberDia;
				Double worsening = (double)numberDia / (double)numDiagnoses ;
				result2.addData("Ratio", formatter.format(worsening-1));

			}
		}
	}
	
	private Integer getNumberOfCoords(List<Diagnosis> diagnoses){
		Set<Coords> set = new HashSet<Coords>();
		for(Diagnosis diag: diagnoses){
			set.addAll(diag.getCells());
		}
		return set.size();
	}

	private String getShortName(String fileName) {
		int index = fileName.lastIndexOf('/');
		if (fileName.lastIndexOf('\\') > index)
			index = fileName.lastIndexOf('\\');
		return fileName.substring(index + 1, fileName.lastIndexOf('.'));
	}

	private void writeToFile(Result result) {
		try {
			if (!fileExists) {
				writer.write(Result.getColumnHeader());
				fileExists = true;
			}
			writer.write(result.toString());
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SpreadsheetProperties changeTestingDecision(SpreadsheetProperties properties, int fromTrueToFalse,
			int fromFalseToTrue) {
		SpreadsheetProperties newProperties = properties.copy();
		Set<Coords> newCoords = new HashSet<Coords>();
		for (int i = 0; i < fromTrueToFalse; i++) {
			int random = randomGenerator.nextInt(newProperties.getCorrectOutputCells().size());
			Coords coord = newProperties.getCorrectOutputCells().remove(random);
			newCoords.add(coord);
		}
		for (int i = 0; i < fromFalseToTrue; i++) {
			int random = randomGenerator.nextInt(newProperties.getIncorrectOutputCellsWithExpectedValue().size());
			Coords coord = newProperties.getIncorrectOutputCells().get(random);
			newProperties.getIncorrectOutputCellsWithExpectedValue().remove(coord);
			newProperties.getCorrectOutputCells().add(coord);
		}
		for (Coords coord : newCoords) {
			newProperties.getIncorrectOutputCellsWithExpectedValue().put(coord, "UNKNOWN");
		}
		return newProperties;
	}

//	private void changeTestingDecision(boolean[] testingDecisions, int fromTrueToFalse, int fromFalseToTrue)
//			throws Exception {
//		List<Integer> positiveTestingDecisions = new ArrayList<Integer>();
//		List<Integer> negativeTestingDecisions = new ArrayList<Integer>();
//		for (int i = 0; i < testingDecisions.length; i++) {
//			if (testingDecisions[i] == false)
//				positiveTestingDecisions.add(i);
//			else
//				negativeTestingDecisions.add(i);
//		}
//
//		if (positiveTestingDecisions.size() < fromTrueToFalse || negativeTestingDecisions.size() < fromFalseToTrue)
//			throw new Exception("There are more testing decision changes required than possible!");
//
//		for (int i = 0; i < fromTrueToFalse; i++) {
//			int random = randomGenerator.nextInt(positiveTestingDecisions.size());
//			int changeIndex = positiveTestingDecisions.get(random);
//			testingDecisions[changeIndex] = !testingDecisions[changeIndex];
//		}
//
//		for (int i = 0; i < fromFalseToTrue; i++) {
//			int random = randomGenerator.nextInt(negativeTestingDecisions.size());
//			int changeIndex = negativeTestingDecisions.get(random);
//			testingDecisions[changeIndex] = !testingDecisions[changeIndex];
//		}
//	}

	private ObservationMatrix setUpObservationMatrix(SpreadsheetProperties properties) {
		CellContainer cells = CellContainer.create(PATH + properties.getExcelSheetName());

		List<Set<Coords>> positiveCones = new ArrayList<Set<Coords>>();
		for (Coords cell : properties.getCorrectOutputCells()) {
			Set<Coords> cone = cells.getICell(cell).getCone(false, true);
			cone.add(cell);
			positiveCones.add(cone);
		}

		List<Set<Coords>> negativeCones = new ArrayList<Set<Coords>>();
		for (Coords cell : properties.getIncorrectOutputCells()) {
			Set<Coords> cone = cells.getICell(cell).getCone(false, true);
			cone.add(cell);
			negativeCones.add(cone);
		}
		Set<Coords> range = cells.getFormulaCoords();
		// printSet(range);
		range.addAll(cells.getOutputCoords());
		range.addAll(cells.getInputCoords());

		ObservationMatrix obs = new ObservationMatrix(positiveCones, negativeCones, range);
		return obs;
	}

}
