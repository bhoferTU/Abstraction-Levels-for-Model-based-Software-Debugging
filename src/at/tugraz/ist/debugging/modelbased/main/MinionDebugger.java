package at.tugraz.ist.debugging.modelbased.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.Diagnosis;
import at.tugraz.ist.debugging.modelbased.EDebuggingAlgorithm;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.ESolverAccessOption;
import at.tugraz.ist.debugging.modelbased.ModelBasedResult;
import at.tugraz.ist.debugging.modelbased.Strategy;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.corpus.SpreadsheetInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICellContainer;
import at.tugraz.ist.debugging.spreadsheets.evaluation.Result;
import at.tugraz.ist.util.RuntimeProcessExecuter;
import at.tugraz.ist.util.IO.OutputConfigurator;
import at.tugraz.ist.util.datastructures.Pair;
import at.tugraz.ist.util.debugging.Writer;
//import at.tugraz.ist.util.fileManipulation.Directory;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;

public class MinionDebugger {

	public static String csvFileName = "";
	public static boolean failed = false;
	public static int failedDomainSize = Integer.MAX_VALUE;
	public static Integer runs = 1;
	public static int timeout = 60 * 60 * 2; // in seconds

	public static void debugAllFiles(List<String> files, Integer[] domainSizes,
			Boolean negativeDomainValue) {
		for (String file : files) {
			if (skip(file)) {
				continue;
			}
			MinionDebugger.setFailed(false);
			System.out.println("\n" + file);
			MinionDebugger.differentDomainSizes(file, domainSizes,
					negativeDomainValue);
		}
	}

	public static void differentDomainSizes(String file, Integer[] domainSizes,
			Boolean negativeDomainValue) {
		for (Integer domainSize : domainSizes) {

			System.out.print(domainSize + ", ");
			MinionConstraintStrategyGenerationInformation
					.setMaxIntDomain(domainSize);
			int numDomainSize = domainSize;
			if (negativeDomainValue) {
				MinionConstraintStrategyGenerationInformation
						.setMinIntDomain(-domainSize);
				numDomainSize += domainSize + 1;
			} else {
				MinionConstraintStrategyGenerationInformation
						.setMinIntDomain(0);
				numDomainSize += 1;
			}

			if (MinionDebugger.isFailed()) {
				BufferedWriter writer;
				try {
					writer = new BufferedWriter(new FileWriter(csvFileName,
							true));
					SpreadsheetProperties properties = new SpreadsheetProperties(
							file);
					writer.write(properties.getExcelSheetName()
							+ ";;failed: see logfile;" + timeout
							+ " seconds;;;;;;;;" + domainSize + "\n");
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SpreadsheetPropertiesException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				continue;
			}

			try {
				MinionDebugger minionDebugger = new MinionDebugger();
				minionDebugger.debug(file, csvFileName, numDomainSize);
			} catch (Exception e) {
				System.err.println("Exception in file " + file);
				System.err.println(e.toString());
			}

		}

	}

	public static void domainSizeFromFileName(String file) {
		Integer domainSize = 0;
		if (file.contains("INT")) {
			String size = file.substring(file.indexOf("INT"));
			size = size.substring(3, size.indexOf("_"));
			Integer siz = Integer.parseInt(size);
			domainSize = siz * 4;
			MinionConstraintStrategyGenerationInformation
					.setMaxIntDomain(domainSize);
			MinionConstraintStrategyGenerationInformation.setMinIntDomain(0);
		}

		try {
			MinionDebugger minionDebugger = new MinionDebugger();
			minionDebugger.debug(file, csvFileName, domainSize);
		} catch (Exception e) {
			System.err.println("Exception in file " + file);
			System.err.println(e.toString());
		}
	}

	public static String getCsvFileName() {
		return csvFileName;
	}

	public static Integer getRuns() {
		return runs;
	}

	public static int getTimeout() {
		return timeout;
	}

	public static boolean isFailed() {
		return failed;
	}

	public static void main(String[] args) {

		OutputConfigurator.setOutputAndErrorStreamToFile("Results_Minion_2020.log");
		// MinionDebugger.setRuns(100);

		// List<String> directories = new ArrayList<String>();
		// // directories.add("fibonacci\\configurationfiles"); too large
		// numbers
		// directories.add("SpreadsheetsLinear\\configurationfiles"); // numbers
		// // grow up
		// // to 100
		// directories.add("linear\\configurationfiles"); // solution is either
		// // 0 or 1
		//
		// for (String directory : directories) {
		// List<String> files = Directory.getFiles(directory, ".properties");
		//
		// ArrayList<Integer> domSizes = new ArrayList<Integer>();
		// int arrayLength = 50;
		// for (int i = 1; i <= arrayLength; i++) {
		// domSizes.add(i * 20000);
		// }
		// Integer[] domainSizes = new Integer[arrayLength];
		// domainSizes = domSizes.toArray(domainSizes);
		//
		// Date now = new Date(System.currentTimeMillis());
		// SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd hh_mm");
		// String dir = directory.substring(0, directory.indexOf("\\"));
		//
		// String cvsFileName = "results_minion_" + dir + "_" + ft.format(now) +
		// "_positiveOnly" + ".csv";
		// MinionDebugger.setCsvFileName(cvsFileName);
		// debugAllFiles(files, domainSizes, false);
		//
		// cvsFileName = "results_minion_" + dir + "_" + ft.format(now) +
		// "_negativesAlso" + ".csv";
		// MinionDebugger.setCsvFileName(cvsFileName);
		// debugAllFiles(files, domainSizes, true);
		// }

		MinionDebugger md = new MinionDebugger();
		MinionDebugger.setRuns(10);
		List<String> files = new ArrayList<String>();
		files.add("Benchmarks\\RunningExample_2020_BookChapter.properties");
//				files = Directory.getFiles("test\\configuration_files",
//				".properties");
		for (String file : files) {

//			if (!MinionDebugger.skip(file)
//					|| file.contains("AFW_training_1Faults_Fault4"))
//				continue;

			// if(!file.contains("AFW_arithmetics00_1Faults_Fault2"))
			// continue;

			 md.debug(file, "valueVSdepModelsResults.csv", 10000);

//			md.storeInfo("IntegerCorpusInfo2.csv", file);
		}

	}

	public static void setCsvFileName(String cvsFileName) {
		MinionDebugger.csvFileName = cvsFileName;
	}

	public static void setFailed(boolean failed) {
		MinionDebugger.failed = failed;
	}

	public static void setRuns(Integer runs) {
		MinionDebugger.runs = runs;
	}

	// private static Boolean skip(String fileName) {
	// String number = fileName.substring(fileName.lastIndexOf("_") + 1,
	// fileName.lastIndexOf("."));
	// Integer num = Integer.parseInt(number);
	// // if (num == 70)
	// // return false;
	// return false;
	// }

	public static void setTimeout(int timeout) {
		MinionDebugger.timeout = timeout;
	}

	public static Boolean skip(String file) {

		if (file.contains("AFW_amortization_"))
			return false;

		if (file.contains("AFW_area"))
			return false;

		if (file.contains("AFW_arithmetics00"))
			return false;

		if (file.contains("AFW_arithmetics01"))
			return false;

		if (file.contains("AFW_arithmetics"))
			return true;

		if (file.contains("AFW_austrian_league"))
			return false;

		if (file.contains("AFW_bank_account"))
			return true;

		if (file.contains("AFW_birthdays"))
			return false;

		if (file.contains("AFW_book_recommendation"))
			return true;

		if (file.contains("AFW_computer_shopping"))
			return false;

		if (file.contains("AFW_conditionals"))
			return false;

		if (file.contains("AFW_dice_rolling"))
			return false;

		if (file.contains("AFW_energy"))
			return true;

		if (file.contains("AFW_euclidean_algorithm"))
			return false;

		if (file.contains("AFW_fibonacci"))
			return false;

		if (file.contains("AFW_matrix"))
			return false;

		if (file.contains("AFW_oscars2012"))
			return true;

		if (file.contains("AFW_parabola"))
			return true;

		if (file.contains("AFW_prom_calculator"))
			return false;

		if (file.contains("AFW_ranking"))
			return false;

		if (file.contains("AFW_shares"))
			return false;

		if (file.contains("AFW_shopping_bedroom"))
			return false;

		if (file.contains("AFW_training_1Faults_Fault4"))
			return true;

		if (file.contains("AFW_training"))
			return false;

		return false;

	}

	public void debug(String propertyFileName, String csvFileName,
			Integer domainSize) {
		MinionConstraintStrategyGenerationInformation.setMinIntDomain(-10000);
		MinionConstraintStrategyGenerationInformation.setMaxIntDomain(10000);

		String excelFileName = null;
		BufferedWriter writer = null;
		Result.clear();
		try {
            Strategy strategy = new Strategy(ESolver.Minion, 
                    EDebuggingAlgorithm.ConstraintBased, ESolverAccessOption.API,
                    EModelGranularity.Value);

			long totaltime = 0;

			File file = new File(csvFileName);
			boolean fileExists = false;
			if (file.exists()) {
				fileExists = true;
			}

			writer = new BufferedWriter(new FileWriter(csvFileName, true));
		
		
			Writer.setActive(false);
			SpreadsheetProperties properties = new SpreadsheetProperties(
					propertyFileName);
            ConstraintStrategyConfiguration.setStrategy(strategy);
            ConstraintStrategyConfiguration.setUseCones(false);
            ConstraintStrategyConfiguration.setEarlyTermination(false);
            ConstraintStrategyConfiguration.setRuns(runs);
            ConstraintStrategyConfiguration.setUseStrings(false);
            ConstraintStrategyConfiguration.setVerifySolution(false);

			excelFileName = properties.getExcelSheetName();

			ConstraintStrategyConfiguration.setModelGranularity(EModelGranularity.Value);
			Pair<List<Diagnosis>, Long> valueResult = debug2(
					propertyFileName, domainSize, excelFileName, writer,
					strategy, totaltime, properties);
            
			ConstraintStrategyConfiguration.setModelGranularity(EModelGranularity.Simple);
			Pair<List<Diagnosis>, Long> simpleDepResult = debug2(
					propertyFileName, domainSize, excelFileName, writer,
					strategy, totaltime, properties);
            
			ConstraintStrategyConfiguration.setModelGranularity(EModelGranularity.Sophisticated);
			Pair<List<Diagnosis>, Long> sophisticatedDepResult = debug2(
					propertyFileName, domainSize, excelFileName, writer,
					strategy, totaltime, properties);
			
			ConstraintStrategyConfiguration.setModelGranularity(EModelGranularity.Dependency);
			Pair<List<Diagnosis>, Long> dependencyResult = debug2(
					propertyFileName, domainSize, excelFileName, writer,
					strategy, totaltime, properties);

			ModelComparisionResult result = new ModelComparisionResult(
					propertyFileName.substring(propertyFileName
							.lastIndexOf(File.separator) + 1),
					valueResult.getFirst(), simpleDepResult.getFirst(),
					sophisticatedDepResult.getFirst(), valueResult.getSecond(),
					simpleDepResult.getSecond(),
					sophisticatedDepResult.getSecond());
			if (!fileExists) {
				writer.write(ModelComparisionResult.getColumnHeader() + System.lineSeparator());
				fileExists=true;
			}
			writer.write(result.toString() + System.lineSeparator());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (writer != null) {
					if (excelFileName != null)
						writer.write(excelFileName + "; failed:;"
								+ e.getMessage() + "\n");
					else if (propertyFileName != null)
						writer.write(propertyFileName + "; failed:;"
								+ e.getMessage() + "\n");
					else
						writer.write("?;failed:;" + e.getMessage() + "\n");
					writer.flush();
					writer.close();
				}
			} catch (Exception e2) {
			}
		}
	}

	private Pair<List<Diagnosis>, Long> debug2(String propertyFileName,
			Integer domainSize, String excelFileName, BufferedWriter writer,
			Strategy strategy, long totaltime,
			SpreadsheetProperties properties)
			throws InterruptedException, IOException {

		ModelBasedResult result = null;
		int retries = 3;
		
		TimeSpan totaltime2 = new TimeSpan(Precision.MILLISECONDS);
        
        String directoryName = null;
        File file = new File(propertyFileName);
        if(file.getParentFile() != null)
            directoryName = file.getParentFile().toString();
        else
            directoryName = System.getProperty("user.dir");

		for (int r = 1; r <= runs; ++r) {
			if (MinionDebugger.isFailed()) {
				break;
			}
			Executor executor = new Executor(directoryName, properties);

			try {
				executor.start();
			} catch (RuntimeException e) {
				if (retries-- == 0)
					throw e;
				--r;
				continue;
			}

			if (timeout <= 0)
				executor.join();
			else {
				executor.join(timeout * 1000);
			}
			result = executor.getResult();

			if (executor.isAlive()) {
				System.err.println(propertyFileName + " ... timeout!");
				MinionDebugger.setFailed(true);
				RuntimeProcessExecuter
						.killProcess(ConstraintStrategy.externalProcess);
			} else if (result == null) {
				System.err.println(excelFileName + ";" + strategy.getName()
						+ ";failed: see logfile;" + timeout
						+ " seconds;;;;;;;;" + domainSize + "\n");
				MinionDebugger.setFailed(true);
			} else {

				result.setDomainSize(domainSize);

				totaltime2.add(executor.getResult().getRuntime());
				System.out
						.println("Time: " + executor.getResult().getRuntime());

				Diagnosis diag = new Diagnosis();
				for (Coords c : properties.getFaultMapping().keySet()) {
					diag.addFaultCell(new Cell(c.getColumn(), c.getRow() - 1, c
							.getWorksheet()));
				}
				if (!subDiagnosisFound(result.getAllDiagnoses(), diag)) {
					System.err.println(propertyFileName
							+ ": faulty cell(s) not contained in diagnosis.");
				}
			}
		}
		totaltime2.divide(runs);
		return new Pair<List<Diagnosis>, Long>(result.getAllDiagnoses(), new Long(totaltime2.toString()));
	}

	public void storeInfo(String cvsFile, String propertiesFile) {
		BufferedWriter writer = null;

		File file = new File(cvsFile);
		boolean fileExists = false;
		if (file.exists()) {
			fileExists = true;
		}

		try {
			writer = new BufferedWriter(new FileWriter(cvsFile, true));

			if (!fileExists) {
				writer.write("equivalence;"
						+ SpreadsheetInformation.getHeader());
			}
			Writer.setActive(false);
			SpreadsheetProperties properties = new SpreadsheetProperties(
					propertiesFile);

			ICellContainer container = CellContainer.create(properties
					.getExcelSheetPath());

			SpreadsheetInformation info = new SpreadsheetInformation(
					new SpreadsheetProperties(propertiesFile), container);

			CellContainer cellContainer = CellContainer.create(properties
					.getExcelSheetName());
			int equivalencePossible = 0;
			for (Cell cell : cellContainer.getCells()) {
				if (cell.getAllReferencesRecursive().size() == 0)
					continue;
				if (cell.getExpression().isEquivalencePossible())
					equivalencePossible++;
			}

			writer.write(equivalencePossible + ";" + info.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpreadsheetPropertiesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static public boolean subDiagnosisFound(List<Diagnosis> diagnoses,
			Diagnosis desiredDiagnosis) {
		Set<Coords> desDiagnosis = desiredDiagnosis.getCells();

		for (Diagnosis diagnosis : diagnoses) {
			Set<Coords> diag = diagnosis.getCells();
			boolean contained = true;
			for (Coords cell : diag) {
				if (!desDiagnosis.contains(cell)) {
					contained = false;
					break;
				}
			}
			if (contained)
				return true;
		}
		return false;
	}
}
