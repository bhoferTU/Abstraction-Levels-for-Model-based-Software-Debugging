package at.tugraz.ist.debugging.modelbased;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines all possible combinations of solvers and debugging algorithms
 */
public class SolverConfigurator {
    
    public static List<SolverConfiguration> strategies;
    
    private SolverConfigurator()
    {
    }
    
    public static void initializeStrategies()
    {
        strategies = new ArrayList<>();
        
        addChocoStrategies();
        addMinionStrategies();
        addZ3Strategies();
    }
    
    /**
     * defines all possible strategies for the Choco constraint solver
     */
    private static void addChocoStrategies()
    {
        SolverConfiguration strategy = new SolverConfiguration(ESolver.Choco);
        strategy.addAlgorithm(EDebuggingAlgorithm.ConstraintBased);
        strategy.addOption(ESolverAccessOption.API);
        strategy.addGranularity(EModelGranularity.Value);
        strategies.add(strategy);
    }
    
    /**
     * defines all possible strategies for the Minion constraint solver
     */
    private static void addMinionStrategies()
    {
        SolverConfiguration strategy = new SolverConfiguration(ESolver.Minion);
        strategy.addAlgorithm(EDebuggingAlgorithm.ConstraintBased);
        strategy.addOption(ESolverAccessOption.API);
        strategy.addGranularity(EModelGranularity.Value);
        //strategy.addGranularity(EModelGranularity.Simple);
        //strategy.addGranularity(EModelGranularity.Sophisticated);
        strategy.addGranularity(EModelGranularity.Dependency);
        strategy.addGranularity(EModelGranularity.Comparison);
        strategies.add(strategy);
    }

    /**
     * defines all possible strategies for the Z3 SMT solver
     */
    private static void addZ3Strategies()
    {
        SolverConfiguration strategy = new SolverConfiguration(ESolver.Z3);
        strategy.addAlgorithm(EDebuggingAlgorithm.MCSes);
        strategy.addAlgorithm(EDebuggingAlgorithm.MCSesU);
        strategy.addOption(ESolverAccessOption.API);
        strategy.addOption(ESolverAccessOption.SMT);
        strategy.addGranularity(EModelGranularity.Value);
        strategy.addGranularity(EModelGranularity.Simple);
        strategy.addGranularity(EModelGranularity.Sophisticated);
        strategies.add(strategy);
    }
}
