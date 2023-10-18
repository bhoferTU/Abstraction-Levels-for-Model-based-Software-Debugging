package at.tugraz.ist.debugging.modelbased;

import org.apache.poi.ss.formula.eval.NotImplementedException;

/**
 * holds strategy information
 */
public class Strategy {
    
    private ESolver solver;
    private EDebuggingAlgorithm algorithm;
    private ESolverAccessOption option;
    private EModelGranularity granularity;

    public Strategy(ESolver solver, EDebuggingAlgorithm algorithm, 
            ESolverAccessOption option, EModelGranularity granularity)
    {
        this.solver = solver;
        this.algorithm = algorithm;
        this.option = option;
        this.granularity = granularity;
        
        switch(this.solver) { //TODO: change in case new strategies are available.
            case Choco:
                if(algorithm != EDebuggingAlgorithm.ConstraintBased)
                    this.algorithm = EDebuggingAlgorithm.ConstraintBased;
                if(option != ESolverAccessOption.API)
                    this.option = ESolverAccessOption.API;
                if(granularity != EModelGranularity.Value){
                	System.err.println("Changed model granularity to value-based!");
                    this.granularity = EModelGranularity.Value; // TODO: implement other methods!
                }
                break;
            case Minion:
                if(algorithm != EDebuggingAlgorithm.ConstraintBased)
                    this.algorithm = EDebuggingAlgorithm.ConstraintBased;
                if(option != ESolverAccessOption.API)
                    this.option = ESolverAccessOption.API;
                break;
            case Z3:
                if(algorithm != EDebuggingAlgorithm.MCSes &&
                        algorithm != EDebuggingAlgorithm.MCSesU)
                    this.algorithm = EDebuggingAlgorithm.MCSes; //MCSes is set as default value
                if(option != ESolverAccessOption.API && option != ESolverAccessOption.SMT)
                    this.option = ESolverAccessOption.API; //API is set as default value
                break;
            default:
                throw new NotImplementedException("Strategy " + getName() + " is not available");
        }
    }
    
    public ESolver getSolver() { return solver; }
    public EDebuggingAlgorithm getAlgorithm() { return algorithm; }
    public ESolverAccessOption getOption() { return option; }
    public EModelGranularity getGranularity() { return granularity; }
    
    public String getName()
    {
        return solver.name() + algorithm.name() + option.name() + granularity.name();
    }
}
