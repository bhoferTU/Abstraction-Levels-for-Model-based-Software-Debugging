package at.tugraz.ist.debugging.modelbased.smt.datastructures;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the UNSAT core provided by the SMT solver
 * 
 */
public class UnsatCore {
	Set<String> variables = new HashSet<String>();

	/**
	 * Adds a tracking variable (i.e. abnormal variable) to the set of variables
	 * contained in the core
	 * 
	 * @param name
	 */
	public void add(String name) {
		variables.add(name);
	}

	/**
	 * Adds all variables from a given UNSAT core to the current core instance
	 * 
	 * @param core
	 */
	public void addCore(UnsatCore core) {
		variables.addAll(core.variables);

	}

	/**
	 * Checks whether the given variable is contained in the UNSAT core
	 * 
	 * @param abnormal
	 * @return
	 */
	public boolean contains(String abnormal) {
		return variables.contains(abnormal);
	}

	/**
	 * Returns a set of UNSAT core variables
	 * 
	 * @return
	 */
	public Set<String> getVariables() {
		return variables;
	}

	/**
	 * Returns an array of UNSAT core variables
	 * 
	 * @return
	 */
	public String[] getVariablesArray() {
		String[] varsArray = new String[variables.size()];
		varsArray = variables.toArray(varsArray);
		return varsArray;
	}

	public int size() {
		return variables.size();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Unsat core:");
		for (String var : variables) {
			s.append(" ");
			s.append(var);
		}
		return s.toString();
	}
}
