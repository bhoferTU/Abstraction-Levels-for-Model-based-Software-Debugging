package at.tugraz.ist.debugging.modelbased;

import java.util.ArrayList;
import java.util.List;


public class SolverConfiguration {
    
    private ESolver solver;
    private List<EDebuggingAlgorithm> algorithms;
    private List<ESolverAccessOption> options;
    private List<EModelGranularity> granularities;
    

    public SolverConfiguration(ESolver solver)
    {
        this.solver = solver;
        this.options = new ArrayList<>();
        this.algorithms = new ArrayList<>();
        this.granularities = new ArrayList<>();
    }
    
    public void addAlgorithm(EDebuggingAlgorithm algorithm)
    {
        this.algorithms.add(algorithm);
    }
    
    public void addOption(ESolverAccessOption option)
    {
        this.options.add(option);
    }
    
    public void addGranularity(EModelGranularity granularity)
    {
        this.granularities.add(granularity);
    }
    
    public ESolver getSolver() { return solver; }
    
    public List<EDebuggingAlgorithm> getAlgorithms() { return algorithms; }
    
    public List<ESolverAccessOption> getOptions() { return options; }
    
    public List<EModelGranularity> getGranulatities() { return granularities; }
}
