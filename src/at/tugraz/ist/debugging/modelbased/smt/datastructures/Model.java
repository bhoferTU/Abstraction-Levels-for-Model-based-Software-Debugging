package at.tugraz.ist.debugging.modelbased.smt.datastructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a model provided by the SMT solver
 * 
 * This class only supports boolean variables (i.e. abnormals)
 * 
 */
public class Model {
	/**
	 * Variables and their assignments
	 */
	private Map<String, Boolean> values = new HashMap<String, Boolean>();

	/**
	 * Adds a new variable assignment to the model
	 * 
	 * @param name
	 * @param value
	 */
	public void add(String name, boolean value) {
		this.values.put(name, value);
	}

	/**
	 * Returns an array of variables whose assignment is false
	 * 
	 * @return
	 */
	public String[] getFalseVariables() {
		Set<String> vars = getFalseVariablesSet();
		String[] varsArray = new String[vars.size()];
		varsArray = vars.toArray(varsArray);
		return varsArray;
	}

	/**
	 * Returns the set of variables whose assignment is false
	 * 
	 * @return
	 */
	public Set<String> getFalseVariablesSet() {
		Set<String> vars = new HashSet<String>();
		for (Map.Entry<String, Boolean> entry : values.entrySet())
			if (!entry.getValue())
				vars.add(entry.getKey());
		return vars;
	}

	/**
	 * Returns an array of variables whose assignment is true
	 * 
	 * @return
	 */
	public String[] getTrueVariables() {
		Set<String> vars = getTrueVariablesSet();
		String[] varsArray = new String[vars.size()];
		varsArray = vars.toArray(varsArray);
		return varsArray;
	}

	/**
	 * Returns the set of variables whose assignment is true
	 * 
	 * @return
	 */
	public Set<String> getTrueVariablesSet() {
		Set<String> vars = new HashSet<String>();
		for (Map.Entry<String, Boolean> entry : values.entrySet())
			if (entry.getValue())
				vars.add(entry.getKey());
		return vars;
	}

	public int size() {
		return values.size();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Model:");
		for (Map.Entry<String, Boolean> var : values.entrySet()) {
			s.append(" ");
			s.append(String.format("%s=%s", var.getKey(), var.getValue()));
		}
		return s.toString();
	}

}
