package at.tugraz.ist.debugging.modelbased.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Properties;
import at.tugraz.ist.debugging.modelbased.Diagnosis;
import at.tugraz.ist.debugging.modelbased.EDebuggingAlgorithm;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.ESolverAccessOption;
import at.tugraz.ist.debugging.modelbased.ModelBasedResult;
import at.tugraz.ist.debugging.modelbased.Strategy;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.util.RuntimeProcessExecuter;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.fileManipulation.Directory;
import at.tugraz.ist.util.fileManipulation.FileTools;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;

public class MinionFileCreator {

	public static int RUNS = 1;
	public static int MAX_DIAGNOSIS_SIZE = 3;
	public static int TIMEOUT = 1200;

	public static boolean ALL_DIAGNOSES = false;
	public static boolean STORE_FINAL_MINION_FILES = false;
	public static boolean STORE_INTERMEDIATE_MINION_FILES = false;

	public static String PATH_TO_PROPERTIES_FILES = "";
	public static String LOG_FILE = "";
	public static String RESULT_FILE = "";
	public static String MINION_FILES_DIR = "";

	public static String CURRENT_FILE = "";
	public static String CURRENT_MODEL = "";

	public static BufferedWriter writer = null;
	public static boolean writeHeader = false;

	public static void main(String[] args) {
		// Save the original System.out
        java.io.PrintStream originalOut = System.out;
		// Check if the correct number of arguments is provided
        if (args.length != 1) {
            System.out.println("Usage: java MinionFileCreator <config_file>");
            return;
        }
     // Get the file name from the command line argument
        String fileName = args[0];
     // Specify the folder path where the file is located
        String folderPath = "experiments/";
     // Construct the full path of the file
        String fullPath = folderPath + fileName;
		try {
			System.out.println("..set configuration from file: "+ fullPath);
			 // Create a File object representing the file
	        File file = new File(fullPath);
			 // Check if the file exists
	        if (file.exists()) {
	            // Read and print the content of the file
	            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    System.out.println(line);
	                }
	                
	                System.out.println("Running..");
	            } catch (IOException e) {
	                System.out.println("Error reading the file: " + e.getMessage());
	            }
	        } else {
	            System.out.println("File not found: " + file.getAbsolutePath());
	        }
	        
			setConfigurationFromFile(fullPath);
			File logFile = new File(LOG_FILE);
			if (!logFile.exists())
				logFile.createNewFile();
			PrintStream log = new PrintStream(logFile);
						System.setOut(log);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Writer.setActive(false);
		
		List<String> files = new ArrayList<String>();

		File file = new File(PATH_TO_PROPERTIES_FILES);
		if(file.isDirectory()){
			files.addAll(Directory.getFiles(PATH_TO_PROPERTIES_FILES, ".properties"));
		}else{
			files.add(PATH_TO_PROPERTIES_FILES);
		}
		runExperiments1(files, RESULT_FILE);
		System.setOut(originalOut);
		System.out.println("Experiment completed.");
	}

public static void setConfigurationFromFile(String configurationFileName) throws Exception{
	File file;
	InputStream iS = null;
	try {
		file = new File(configurationFileName);
		iS = new FileInputStream(file);
		Properties configFile = new Properties();
		configFile.load(iS);
		RUNS = Integer.valueOf(configFile.getProperty("NUM_RUNS"));
		MAX_DIAGNOSIS_SIZE = Integer.valueOf(configFile.getProperty("MAX_DIAGNOSIS_SIZE"));
		TIMEOUT = Integer.valueOf(configFile.getProperty("TIMEOUT_MINUTES"))*60;
		ALL_DIAGNOSES = configFile.getProperty("ALL_DIAGNOSES").equalsIgnoreCase("TRUE")?true:false;
		STORE_FINAL_MINION_FILES = configFile.getProperty("STORE_FINAL_MINION_FILES").equalsIgnoreCase("TRUE")?true:false;
		STORE_INTERMEDIATE_MINION_FILES = configFile.getProperty("STORE_INTERMEDIATE_MINION_FILES").equalsIgnoreCase("TRUE")?true:false;
		PATH_TO_PROPERTIES_FILES = configFile.getProperty("PATH_TO_PROPERTIES_FILES");
		LOG_FILE = configFile.getProperty("LOG_FILE");
		RESULT_FILE = configFile.getProperty("RESULT_FILE");
		MINION_FILES_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + configFile.getProperty("MINION_FILES_DIR");

	} catch (Exception e) {
		throw new Exception(
				"Error in initializing test run when loading file "
						+ configurationFileName + ": " + e.getMessage());
	} finally {
		try {
			if (iS != null)
				iS.close();
		} catch (IOException e) {
			// Silent catch
		}
	}
}


public static void runExperiments1(List<String> files, String csvFile) {

	String directoryName = System.getProperty("user.dir");

	if (STORE_INTERMEDIATE_MINION_FILES || STORE_FINAL_MINION_FILES){
		File minionDir = new File(MINION_FILES_DIR);
		if(!minionDir.exists()){
			minionDir.mkdirs();
		}
	}

	try {
		if (!new File(csvFile).exists())
			writeHeader = true;
		writer = new BufferedWriter(new FileWriter(csvFile, true));

	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	for (String file : files) {
		String pFile = null;
		if (file.contains(directoryName)) {
			pFile = file;
		} else {
			pFile = directoryName + System.getProperty("file.separator") + file;
		}

		String minionOut = pFile.replace(".properties", ".minion");
		minionOut = minionOut.substring(pFile.lastIndexOf(System.getProperty("file.separator")) + 1);
		minionOut = MINION_FILES_DIR + System.getProperty("file.separator") + minionOut;
		createMinionFile(pFile, minionOut);

	}
	try {
	    writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
	

	public static void createMinionFile(String propertiesFile, String outFile) {
		CURRENT_FILE = outFile;
		try {
			SpreadsheetProperties properties = new SpreadsheetProperties(propertiesFile);

			System.out.println(propertiesFile.substring(39));
			CURRENT_MODEL = "_value";
			System.out.println("Value-based model");
			ModelBasedResult valueResult = null;
			valueResult = computeResult(properties, EModelGranularity.Value);
			if(STORE_FINAL_MINION_FILES){
				rename(outFile, CURRENT_MODEL);
			}


			System.out.println("Comparison-based model");
			CURRENT_MODEL = "_comparison";
			ModelBasedResult comparisionResult = computeResult(properties, EModelGranularity.Comparison);
			if(STORE_FINAL_MINION_FILES){
				rename(outFile, CURRENT_MODEL);
			}

			System.out.println("Dependency-based model");
			CURRENT_MODEL = "_dependency";
			ModelBasedResult sophDepResult = computeResult(properties, EModelGranularity.Dependency);
			if(STORE_FINAL_MINION_FILES){
				rename(outFile, CURRENT_MODEL);
			}
			
			List<Diagnosis> trueFault = new ArrayList<Diagnosis>();
			List<Coords> faults = properties.getFaultyCells();
			Diagnosis diag = new Diagnosis();
			diag.addAll(faults);
			trueFault.add(diag);
			

			ComparisionModelResult cmr = new ComparisionModelResult(
					propertiesFile.substring(39).replace(".properties", ""),
					valueResult.getAllDiagnoses(), comparisionResult.getAllDiagnoses(), sophDepResult.getAllDiagnoses(), trueFault,
					valueResult.getRuntimeSolvingDiagGranularity(), comparisionResult.getRuntimeSolvingDiagGranularity(),
					sophDepResult.getRuntimeSolvingDiagGranularity(), properties, ConstraintStrategyConfiguration.useEarlyTermination(),ConstraintStrategyConfiguration.getMaxDiagnosesSize() );

			if (writeHeader) {
				writer.write(ComparisionModelResult.getColumnHeader());
				writeHeader = false;
			}

			writer.write(cmr.toString());
			writer.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static void rename(String file, String modelname){
		try{
			FileTools.renameFile(
					System.getProperty("user.dir") + System.getProperty("file.separator")
							+ MinionConstraintStrategy.getMinionFileName(),
					file.replace(".minion", modelname+".minion"), true);
			}catch(IOException ex){
				System.out.println("########## 2nd try renaming file ############");
				System.out.println(ex.getMessage());
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					FileTools.renameFile(
							System.getProperty("user.dir") + System.getProperty("file.separator")
									+ MinionConstraintStrategy.getMinionFileName(),
							file.replace(".minion", modelname+".minion"), true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}

	public static ModelBasedResult computeResult(SpreadsheetProperties properties, EModelGranularity modelGranularity) {
		List<Map<Integer, TimeSpan>> allTimes = new ArrayList<Map<Integer, TimeSpan>>();
		ModelBasedResult result = null;
		Strategy strategy = new Strategy(ESolver.Minion, EDebuggingAlgorithm.ConstraintBased, ESolverAccessOption.API,
				modelGranularity);
		try {
			ConstraintStrategyConfiguration.setStrategy(strategy);
			ConstraintStrategyConfiguration.setUseCones(false);
			ConstraintStrategyConfiguration.setEarlyTermination(false);
			ConstraintStrategyConfiguration.setRuns(RUNS);
			ConstraintStrategyConfiguration.setUseStrings(false);
			ConstraintStrategyConfiguration.setVerifySolution(false);
			ConstraintStrategyConfiguration.setMaxDiagnosesSize(MAX_DIAGNOSIS_SIZE);

			String directory = System.getProperty("user.dir") + System.getProperty("file.separator")+ "Benchmarks\\";
			
			if(properties.getExcelSheetName().contains("AFW"))
				directory +=  "INTEGER\\";

			if(properties.getPropertyFileName().contains("SumCircPropertyFiles"))
				directory += "ArtifSpreadsheets\\SumCircExcelFiles\\";

			int retries = 3;
			for (int run = 1; run <= ConstraintStrategyConfiguration.getRuns(); ++run) {
				Executor executor = new Executor(directory, properties);
				try {
					executor.start();
				} catch (RuntimeException e) {
					if (retries-- == 0)
						throw e;
					--run;
					continue;
				}
				if (TIMEOUT <= 0)
					executor.join();
				else
					executor.join(TIMEOUT * 1000);

				result = executor.getResult();
				boolean timeout = false;
				if (executor.isAlive()) {
					System.out.println(properties.getExcelSheetName() + " ... timeout!");
					RuntimeProcessExecuter.killProcess(ConstraintStrategy.externalProcess);
					timeout = true;
				}

				if (result == null) {
					System.out.println(properties.getExcelSheetName() + " " + strategy.getName() + " failed: "
							+ executor.getErrorMessage() + System.lineSeparator());
					result = new ModelBasedResult(properties.getExcelSheetName(), strategy,
							ConstraintStrategyConfiguration.useStrings(), -1, -1);
					result.setRuntime(-1);
					break;
				} 
				if (executor.getResult() != null) {
					allTimes.add(executor.getResult().getRuntimeSolvingDiagGranularity());
				}

				if (run < ConstraintStrategyConfiguration.getRuns()) {
					continue;
				}
				if(timeout){
					result.setRuntime(-1);
				}
				if (EvaluationGUI.checkDiagnosis(result.getAllDiagnoses(), properties.getFaultyCells()) == true) {
					result.setDiagnosisContained(true);
				} else {
					result.setDiagnosisContained(false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Map<Integer, TimeSpan> avgTime = new HashMap<Integer, TimeSpan>();
		for(Map<Integer, TimeSpan> time : allTimes){
			for(int i : time.keySet()){
				TimeSpan t = new TimeSpan(Precision.MICROSECONDS);
				if(avgTime.containsKey(i)){
					t = avgTime.get(i);
				}
				t.add(time.get(i));
				avgTime.put(i, t);
			}
			
		}
		for(int i: avgTime.keySet()){
			TimeSpan t = avgTime.get(i);
			t.divide(ConstraintStrategyConfiguration.getRuns());
			avgTime.put(i, t);
		}
		
		result.addSolvingTimes(avgTime);
		return result;

	}

}
