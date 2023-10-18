package at.tugraz.ist.debugging.modelbased.solver;

import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.Strategy;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.util.time.TimeSpan.Precision;
import java.util.List;

public final class ConstraintStrategyConfiguration {

    /**
	 * Determines whether the program is supposed to terminate early, after the 
     * first set of diagnoses is found, or not.
	 */
    private static boolean earlyTermination;
    
    /**
     * holds the spreadsheets faulty cells
     */
    private static List<Coords> faultyCells;
    
    /**
	 * Determines the number of runs.
	 */
	private static int runs;

    /**
     * Represents the current model granularity
     */
    private static EModelGranularity modelGranularity;
    
    /**
	 * Represents the current constraint strategy.
	 */
	private static Strategy strategy;
    
    /**
	 * Determines whether cones are used or not.
	 */
	private static boolean useCones;
    
	/**
	 * Determines whether strings are used (integer representation of strings)
	 * or should be ignored.
	 */
	private static boolean useStrings;
    
    /**
	 * Determines whether dependency-based solutions should be verified with a 
     * value-based model.
	 */
	private static boolean verifySolution;

    private ConstraintStrategyConfiguration(){}
    
    /**
     * Determines when ConDiag should terminate
     * If set to 1, then only single fault diagnoses will be computed
     * If set to -1, then Diagnoses up to the number of abnormal variables will be computed
     */
    private static int maxDiagnosesSize = -1;
    
    public static int getMaxDiagnosesSize() {
		return maxDiagnosesSize;
	}

	public static void setMaxDiagnosesSize(int maxDiagnosesSize) {
		ConstraintStrategyConfiguration.maxDiagnosesSize = maxDiagnosesSize;
	}

	public static List<Coords> getFaultyCells() {
        return faultyCells;
    }
    
	public static EModelGranularity getModelGranularity() {
		return modelGranularity;
	}

	public static Precision getPrecision() {
		return Precision.NANOSECONDS;
	}

	public static int getRuns() {
		return runs;
	}
    
    public static Strategy getStrategy()
    {
        return strategy;
    }
    
    public static boolean useCones() {
		return useCones;
	}

	public static boolean useEarlyTermination() {
		return earlyTermination;
	}

	public static boolean useStrings() {
		return useStrings;
	}
    
    public static boolean verifySolution()
    {
        return verifySolution;
    }

    public static void setEarlyTermination(boolean earlyTermination) {
		ConstraintStrategyConfiguration.earlyTermination = earlyTermination;
	}
    
    public static void setFaultyCells(List<Coords> faultyCells) {
        ConstraintStrategyConfiguration.faultyCells = faultyCells;
    }

	public static void setModelGranularity(EModelGranularity modelGranularity) {
		ConstraintStrategyConfiguration.modelGranularity = modelGranularity;
	}

	public static void setRuns(int runs) {
		ConstraintStrategyConfiguration.runs = runs;
	}

    public static void setStrategy(Strategy strategy){
        ConstraintStrategyConfiguration.strategy = strategy;
        ConstraintStrategyConfiguration.modelGranularity = strategy.getGranularity();
    }
    
	public static void setUseCones(boolean useCones) {
		ConstraintStrategyConfiguration.useCones = useCones;
	}

	public static void setUseStrings(boolean useStrings) {
		ConstraintStrategyConfiguration.useStrings = useStrings;
	}
    
    public static void setVerifySolution(boolean verifySolution)
    {
        ConstraintStrategyConfiguration.verifySolution = verifySolution;
    }
}
