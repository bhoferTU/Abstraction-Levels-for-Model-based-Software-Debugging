package at.tugraz.ist.debugging.modelbased;

/**
 * Supported debugging algorithms
 */
public enum EDebuggingAlgorithm {
    
    /**
	 * MCSes spreadsheet debugging algorithm for SMT solvers
	 */
	MCSes,
    
    /**
	 * MCSes-U spreadsheet debugging algorithm for SMT solvers
	 */
	MCSesU,
    
    /**
	 * Constraint based debugging algorithm for the constraint solvers (Minion, Choco)
	 */
    ConstraintBased;
}
