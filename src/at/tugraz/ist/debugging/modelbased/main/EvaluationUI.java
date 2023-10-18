package at.tugraz.ist.debugging.modelbased.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import at.tugraz.ist.debugging.modelbased.EDebuggingAlgorithm;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.ESolverAccessOption;
import at.tugraz.ist.debugging.modelbased.ModelBasedResult;
import at.tugraz.ist.debugging.modelbased.Strategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.util.RuntimeProcessExecuter;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.exception.InputParseException;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;

public class EvaluationUI {

    private static final String PROPERTY_FILE_PATH_PARAM = "property-file=";
    private static final String SOLVER_NAME_PARAM = "solver=";
    private static final String ALGORITHM_NAME_PARAM = "algorithm=";
    private static final String SOLVER_ACCESS_OPTION_PARAM = "option=";
    private static final String CONE_FLAG_PARAM = "cone-flag=";
    private static final String TIMEOUT_PARAM = "timeout=";
    private static final String RUNS_PARAM = "runs=";
    private static final String USE_STRINGS_PARAM = "use-strings=";
    private static final String EARLY_TERMINATION_PARAM = "early-termination=";
    private static final String GRANULARITY_PARAM = "granularity=";
    private static final String VERIFY_SOLUTION = "verify-solution=";
    
    private static String propertyFilePath = null;
    private static String directoryPath = null;
    private static ESolver solver = null;
    private static EDebuggingAlgorithm algorithm = null;
    private static ESolverAccessOption option = null;
    private static boolean coneFlag = false;
    private static EModelGranularity granularity = null;
    private static int timeout = 60;
    private static int runs = 0;
    private static boolean earlyTermination = false;
    private static boolean useStrings = false;
    private static boolean verifySolution = true;
    
    
	/**
	 * @param args
	 */
	public static void main(String[] args)
    {
        BufferedWriter writer = null;
        String excelFileName = null;
        TimeSpan totaltime = new TimeSpan(Precision.MILLISECONDS);
        boolean writeHeader = false;

        try
        {
            parseInput(args);
                        
            Strategy strategy = new Strategy(solver, algorithm, option, granularity);

            
			String csvFileName = strategy.getName()
					+ (coneFlag ? "_cone_" : "_nocone_") + runs + "runs.csv";

            if(!new File(csvFileName).exists())
                writeHeader = true;
			writer = new BufferedWriter(new FileWriter(csvFileName, true));
			Writer.setActive(false);
            
			SpreadsheetProperties properties = new SpreadsheetProperties(
					propertyFilePath);
            
            ConstraintStrategyConfiguration.setStrategy(strategy);
            ConstraintStrategyConfiguration.setUseCones(coneFlag);
            ConstraintStrategyConfiguration.setEarlyTermination(earlyTermination);
            ConstraintStrategyConfiguration.setRuns(runs);
            ConstraintStrategyConfiguration.setUseStrings(useStrings);
            ConstraintStrategyConfiguration.setVerifySolution(verifySolution);

			excelFileName = properties.getExcelSheetName();

			int retries = 3;
			for (int r = 1; r <= runs; ++r) {
				Executor executor = new Executor(directoryPath, properties);
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
				else
					executor.join(timeout * 1000);
				
                ModelBasedResult result = executor.getResult();
				if (executor.isAlive()) {
					System.out.println(" ... timeout!");
					writer.write(excelFileName + ";" + strategy.getName()
							+ ";Diagnosis not found within:;" + timeout + " seconds"
							+ System.lineSeparator());
					writer.flush();
					writer.close();
					RuntimeProcessExecuter
							.killProcess(ConstraintStrategy.externalProcess);
					System.exit(0);
				}

				if (r < runs) {
                    if(executor.getResult() != null)
                    {
                        totaltime.add(executor.getResult().getRuntime());
                        continue;
                    }
				}

				if (result == null) {
					writer.write(properties.getExcelSheetName() + ";"
							+ strategy.getName() + ";failed:;"
							+ executor.getErrorMessage()
							+ System.lineSeparator());
					writer.flush();
					writer.close();
					System.exit(0);
				}

				totaltime.add(executor.getResult().getRuntimeSolving());
				totaltime.divide(runs);
				result.setRuntime(totaltime);
				if (EvaluationGUI.checkDiagnosis(result.getAllDiagnoses(), properties.getFaultyCells()) == true) {
					result.setDiagnosisContained(true);
				} else {
					result.setDiagnosisContained(false);
				}
                
                if(writeHeader)
                    writer.write(ModelBasedResult.getColumnHeader() + System.lineSeparator());
                
				writer.write(result.toString() + System.lineSeparator());
				writer.flush();
				writer.close();
			}
		}
        catch (Exception e)
        {
            if(e instanceof InputParseException || e instanceof IllegalArgumentException)
            {
                showUsage();
                System.out.println(((InputParseException)e).getErrorMessage());
                System.exit(-1);
            }
            
			e.printStackTrace();
			try {
				if (writer != null) {
					if (excelFileName != null)
						writer.write(excelFileName + "; failed:;"
								+ e.getMessage() + System.lineSeparator());
					else if (propertyFilePath != null)
						writer.write(propertyFilePath + "; failed:;"
								+ e.getMessage() + System.lineSeparator());
					else
						writer.write("?;failed:;" + e.getMessage()
								+ System.lineSeparator());
					writer.flush();
					writer.close();
				}
			} catch (Exception e2) {
			}
		}
		System.exit(0);
	}


    
    private static void showUsage()
    {
        System.out.println("usage: <property-file=filepath> "
                + "<solver=solver name> "
                + "[<algorithm=algorithm name> "
                + "<option=solver access option name> "
                + "<cone-flag={1,0}> "
                + "<granularity=model granularity>"
                + "<timeout={0=no timeout[sec]}> "
                + "<runs=numberofruns> "
                + "<use-strings={1,0}> "
                + "<early-termination={1,0}>]");
			System.out.println("  solvers: ");
			for (ESolver solver : ESolver.values())
				System.out.println("        " + solver.name());
            System.out.println('\n');
            System.out.println("  algorithms: ");
            for (EDebuggingAlgorithm algorithm : EDebuggingAlgorithm.values())
				System.out.println("        " + algorithm.name());
            System.out.println('\n');
             System.out.println("  solver-access-options: ");
            for (ESolverAccessOption option : ESolverAccessOption.values())
				System.out.println("        " + option.name());
            System.out.println('\n');
            for (EModelGranularity granularity : EModelGranularity.values())
				System.out.println("        " + granularity.name());
            System.out.println('\n');
    }
    
    private static void parseInput(String[] args) throws InputParseException
    {
            if (args.length < 2 || args.length > 11)
                throw new InputParseException("Wrong number of arguments!");

            for(String arg : args)
                parseArgument(arg);
            
            if(runs <= 0)
                runs = 1;
            if(timeout <= 0)
                timeout = 0;
            
            if(granularity == null)
                granularity = EModelGranularity.Value;
            
            if(granularity == EModelGranularity.Value)
                verifySolution = false;
    }
    
    private static void parseArgument(String arg) throws InputParseException
    {
        if(arg.contains(PROPERTY_FILE_PATH_PARAM)){
            propertyFilePath = arg.split(PROPERTY_FILE_PATH_PARAM)[1].trim();
           
            File file = new File(propertyFilePath);
            if(file.exists())
            {
                if(file.getParentFile() != null)
                    directoryPath = file.getParentFile().toString();
                else
                    directoryPath = System.getProperty("user.dir");
                return;
            }
            else
                throw new InputParseException("Properties file: " + propertyFilePath + " does not exist!");
        }

        if(arg.contains(SOLVER_NAME_PARAM))
        {
            solver = ESolver.valueOf(arg.split(SOLVER_NAME_PARAM)[1].trim());
            return;
        }
        
        if(arg.contains(ALGORITHM_NAME_PARAM))
        {
            algorithm = EDebuggingAlgorithm.valueOf(
                    arg.split(ALGORITHM_NAME_PARAM)[1].trim());
            return;
        }
        
        if(arg.contains(SOLVER_ACCESS_OPTION_PARAM))
        {
            option = ESolverAccessOption.valueOf(
                    arg.split(SOLVER_ACCESS_OPTION_PARAM)[1].trim());
            return;
        }
        
        if(arg.contains(CONE_FLAG_PARAM))
        {
            if(Integer.parseInt(arg.split(CONE_FLAG_PARAM)[1].trim()) == 1)
                coneFlag = true;
            else
                coneFlag = false;
            return;
        }
        
        if(arg.contains(GRANULARITY_PARAM))
        {
            granularity = EModelGranularity.valueOf(arg.split(GRANULARITY_PARAM)[1].trim());
            return;
        }
        
        if(arg.contains(TIMEOUT_PARAM))
        {
            timeout = Integer.parseInt(arg.split(TIMEOUT_PARAM)[1].trim());
            return;
        }
        
        if(arg.contains(RUNS_PARAM))
        {
            runs = Integer.parseInt(arg.split(RUNS_PARAM)[1].trim());
            return;
        }
        
        if(arg.contains(USE_STRINGS_PARAM))
        {
            if(Integer.parseInt(arg.split(USE_STRINGS_PARAM)[1].trim()) == 1)
                useStrings = true;
            else 
                useStrings = false;
            return;
        }
        
        if(arg.contains(EARLY_TERMINATION_PARAM))
        {
            if(Integer.parseInt(arg.split(EARLY_TERMINATION_PARAM)[1].trim()) == 1)
                earlyTermination = true;
            else
                earlyTermination = false;
            return;
        }
        
        if(arg.contains(VERIFY_SOLUTION))
        {
            if(Integer.parseInt(arg.split(VERIFY_SOLUTION)[1].trim()) == 1)
                verifySolution = true;
            else
                verifySolution = false;
            return;
        }

        throw new InputParseException("Invalid argument: " + arg + ".");
    }
}
