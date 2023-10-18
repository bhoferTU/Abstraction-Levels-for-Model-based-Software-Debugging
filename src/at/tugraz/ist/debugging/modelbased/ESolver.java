package at.tugraz.ist.debugging.modelbased;

/**
 * Supported solvers
 */
public enum ESolver {
    
    /**
	 * Choco constraint solver
	 */
	Choco,
    
    /**
	 * Minion constraint solver
	 */
	Minion,
    
    /**
	 * Z3 SMT solver
	 */
	Z3,
}
