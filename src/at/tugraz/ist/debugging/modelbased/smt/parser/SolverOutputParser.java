package at.tugraz.ist.debugging.modelbased.smt.parser;

import java.io.IOException;
import java.io.InputStream;

import at.tugraz.ist.debugging.modelbased.smt.SMTConstants;
import at.tugraz.ist.debugging.modelbased.smt.datastructures.Model;
import at.tugraz.ist.debugging.modelbased.smt.datastructures.UnsatCore;

/**
 * Base class for the different output parsers of the SMT solvers.
 * This parser is able to parse the satisfiability string as well as the model 
 * (in case of SAT) and the unsat core (in case of UNSAT).
 */
public abstract class SolverOutputParser {
    
    /**
	 * Input stream which provides the tokens (characters)
	 */
	protected InputStream is;

	protected SolverOutputParser(InputStream is) {
		this.is = is;
	}
    
    /**
	 * Parses the model output string provided by the SMT solver
	 * 
	 * @return
	 * @throws IOException
	 */
    public abstract Model getModel() throws IOException;
    
    /**
	 * Parses the unsat core output string provided by the SMT solver
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract UnsatCore getUnsatCore() throws IOException;
    
    /**
	 * Parses the satisfiability output string provided by the SMT solver
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract SMTConstants.Satisfiability isSatisfied() throws IOException;
}
