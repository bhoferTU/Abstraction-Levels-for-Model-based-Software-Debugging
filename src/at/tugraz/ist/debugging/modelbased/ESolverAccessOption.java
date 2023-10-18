package at.tugraz.ist.debugging.modelbased;

/**
 * Defines the options whether the solver should be accessed via its API or 
 * through an SMT-LIB file
 */
public enum ESolverAccessOption {
    /**
	 * access via API
	 */
	API,
    
    /**
	 * access via SMT-LIB file
	 */
	SMT,
    
}
