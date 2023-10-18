package at.tugraz.ist.debugging.modelbased.smt;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator.SortType;
import at.tugraz.ist.debugging.modelbased.smt.datastructures.Model;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyGenerationInformation;

/**
 * SMT information class which is needed during the generation of SMT code and
 * during the algorithm's execution
 * 
 * This class stores information about variables like cell and abnormal
 * variables and provides different functionalities needed by the diagnosis
 * search algorithm, e.g. - string mapping - adding upper bound clause - adding
 * blocking clauses (avoid multiple reportings of the same diagnosis)
 */
public class SMTConstraintStrategyGenerationInformation extends
		ConstraintStrategyGenerationInformation {

	/**
	 * Abnormal variable avoidance mode
	 */
	public enum Avoidance {
		/**
		 * Add all literals to the blocking clause
		 */
		All,
		/**
		 * Add abnormals only
		 */
		FalseOnly,
		/**
		 * Add non-abnormals only
		 */
		TrueOnly
	}

	/**
	 * Abnormal variable names
	 */
	private final Map<Cell, String> abnormalVariablesMap = new HashMap<>();

	/**
	 * Code generator instance
	 */
	private final SMTCodeGenerator codeGenerator;

	/**
	 * Abnormal variable name to cell mapping
	 */
	private final Map<String, Cell> revAbnormalVariablesMap = new HashMap<>();

	/**
	 * String to integer (hash value) mapping
	 */
	private final Map<String, Integer> stringMapping = new HashMap<>();

	/**
	 * Cell variable names
	 */
	private final Map<Cell, String> variables = new HashMap<>();

	public SMTConstraintStrategyGenerationInformation(
			SMTCodeGenerator codeGenerator, Set<Cell> cone, boolean useCones,
			EModelGranularity modelGranularity) {
		super(cone, useCones, modelGranularity);
		this.codeGenerator = codeGenerator;
	}

	/**
	 * Adds an abnormal variable to the cell-abnormal variable string mapping
	 * and appends the variable declaration code to the already existing SMT
	 * code
	 * 
	 * @param cell
	 *            Cell
	 * @param varName
	 *            Name of the abnormal (boolean) variable which should be added
	 *            to the SMT formula
	 * @throws IOException
	 *             Error while adding the variable declaration
	 */
	public void addAbnormalVariable(Cell cell, String varName)
			throws IOException {
		abnormalVariablesMap.put(cell, varName);
		revAbnormalVariablesMap.put(varName, cell);
		codeGenerator.addConstDeclaration(varName, SortType.Bool);
	}

	/**
	 * Adds a blocking clause to the SMT code which ensures that the given model
	 * will not be reported in a following solve run
	 * 
	 * At this the clause structure is determined by the avoidance mode
	 * parameter.
	 * 
	 * @param model
	 *            Model which should be avoided
	 * @param avoidanceMode
	 *            clause structure
	 * @throws IOException
	 *             Error while adding SMT code
	 */
	public void addAvoid(Model model, Avoidance avoidanceMode)
			throws IOException {
		String[] trueVariables = model.getTrueVariables();

		if (avoidanceMode == Avoidance.TrueOnly) {
			codeGenerator.addAssertion(codeGenerator.not(codeGenerator
					.and(trueVariables)));
			return;
		}
		String[] falseVariables = codeGenerator.not(model.getFalseVariables());
		if (avoidanceMode == Avoidance.FalseOnly) {
			codeGenerator.addAssertion(codeGenerator.not(codeGenerator
					.and(falseVariables)));
			return;
		}

		codeGenerator.addAssertion(codeGenerator.not(codeGenerator.and(
				codeGenerator.and(trueVariables),
				codeGenerator.and(falseVariables))));
	}

	/**
	 * Adds a new assertion for the assignment of a particular cell.
	 * 
	 * At this, a distinction between input and non-input cells is made. While
	 * the method adds an assertion cellVariable = cellExpression for input
	 * cells, the assertion for non-input cells will be as follows: non-abnormal
	 * => cellVariable = cellExpression.
	 * 
	 * @param cell
	 *            Cell variable which should be represented
	 * @param smtConstraint
	 *            SMT code representing the cell expression
	 * @throws IOException
	 *             Error while adding SMT code
	 */
	public void addCellAssignment(Cell cell, String smtConstraint)
			throws IOException {
		if (abnormalVariablesMap.containsKey(cell)) {
			// is not an input cell
			codeGenerator.addAssertion(codeGenerator.implies(
					abnormalVariablesMap.get(cell),
					codeGenerator.equal(variables.get(cell), smtConstraint)));
		} else {
			// is an input cell
			codeGenerator.addAssertion(codeGenerator.equal(variables.get(cell),
					smtConstraint));
		}
	}

	/**
	 * Adds a string to integer mapping and returns the integer value
	 * corresponding to the given string
	 * 
	 * The mapping is determined by applying a hash function to the given string
	 * value. It is then stored in the internal string to integer map
	 * 
	 * @param value
	 * @return
	 */
	public Integer addStringMapping(String value) {
		if (stringMapping.containsKey(value))
			return stringMapping.get(value);

		MessageDigest md;
		Integer intVal = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(value.getBytes());
			intVal = new BigInteger(Arrays.copyOf(digest, 4)).intValue();
		} catch (NoSuchAlgorithmException e) {
			intVal = new Random().nextInt();
		}

		stringMapping.put(value, intVal);
		return intVal;
	}

	/**
	 * Adds a variable to the cell-variable string mapping and appends the
	 * declaration code to the already existing SMT code
	 * 
	 * @param cell
	 *            Cell instance which should be added
	 * @param cellVariableName
	 *            Corresponding cell variable name
	 * @param type
	 *            Sort type of the SMT variable
	 * @throws IOException
	 *             Error while adding the variable declaration
	 */
	public void addVariable(Cell cell, String cellVariableName, SortType type)
			throws IOException {
		variables.put(cell, cellVariableName);
		codeGenerator.addDeclaration(cellVariableName, type);
	}

	/**
	 * Adds a weight function given some boolean variables
	 * 
	 * The weight function is able to count the number of false boolean
	 * variables - while a false variable in the set of given boolean variables
	 * has weight 0, a true variable has weight 1. The result of the weight
	 * function is thus the amount of true variables
	 * 
	 * @param booleanVariables
	 *            Variable names of boolean variables which should influence the
	 *            weight function's result
	 * @return Weight function variable
	 * @throws IOException
	 *             Error while adding SMT code
	 */
	public String addWeightFunction(String[] booleanVariables)
			throws IOException {
		String weightParam = "weight_param";
		String weightVariable = "weight";
		codeGenerator.addDeclaration(weightVariable, SortType.Int);
		String body = codeGenerator.ite(weightParam, codeGenerator.getValue(1),
				codeGenerator.getValue(0));
		String parameter = codeGenerator
				.toParameter(weightParam, SortType.Bool);

		codeGenerator
				.addDefinition("weight_fun", SortType.Int, body, parameter);

		String[] calls = new String[booleanVariables.length];
		for (int i = 0; i < booleanVariables.length; i++)
			calls[i] = codeGenerator.call("weight_fun", booleanVariables[i]);

		codeGenerator.addAssertion(codeGenerator.equal(weightVariable,
				codeGenerator.plus(calls)));

		return weightVariable;
	}

	/**
	 * Determines the most general sort given an arbitrary amount of sort types
	 * 
	 * The sort type is determined by the following hierarchy (less to more
	 * general): - bool - integer - real
	 * 
	 * @param types
	 *            Sort types
	 * @return Most general sort type
	 */
	public SortType determineMostGeneralSort(SortType... types) {
		if (types == null || types.length == 0)
			throw new RuntimeException();
		if (types.length == 1)
			return types[0];

		SortType currentMostGeneral = types[0];
		for (int i = 1; i < types.length; i++) {
			if (types[i].getValue() > currentMostGeneral.getValue())
				currentMostGeneral = types[i];
		}
		return currentMostGeneral;
	}

	/**
	 * Determines the SMT sort of a given value
	 * 
	 * @param value
	 *            Value whose SMT sort should be determined
	 * @return Sort type which is able to represent the content of the given
	 *         value object
	 */
	public SortType determineSort(Object value) {
		if (value instanceof Integer)
			return SortType.Int;
		else if (value instanceof Boolean)
			return SortType.Bool;
		else if (value instanceof Double)
			return SortType.Real;
		else if (value instanceof String)
			return SortType.Int;
		else if (value == null)
			return SortType.Int;
		throw new RuntimeException(String.format(
				"Sort type for given object '%s' could not be determined",
				value));

	}

	@Override
	public int getAbnormalsCount() {
		return abnormalVariablesMap.size();
	}

	/**
	 * Returns the cell which corresponds to a particular abnormal variable
	 * 
	 * @param abnormalVarName
	 *            Name of the abnormal variable whose corresponding cell should
	 *            be returned
	 * @return
	 */
	public Cell getAbnormalVariable(String abnormalVarName) {
		return revAbnormalVariablesMap.get(abnormalVarName);
	}

	/**
	 * @return Array of abnormal variable names
	 */
	public String[] getAbnormalVariableNames() {
		String[] vars = new String[abnormalVariablesMap.size()];
		int i = 0;
		for (String var : abnormalVariablesMap.values()) {
			vars[i] = var;
			i++;
		}
		return vars;
	}

	/**
	 * @return Cell to abnormal variables mapping
	 */
	public Map<Cell, String> getAbnormalVariables() {
		return abnormalVariablesMap;
	}

	@Override
	public int getCellVariablesCount() {
		return variables.size();
	}

	/**
	 * @return Code generator instance
	 */
	public SMTCodeGenerator getCodeGenerator() {
		return this.codeGenerator;
	}

	/**
	 * @return Input cells
	 */
	public Set<Cell> getInputCells() {
		Set<Cell> cells = new HashSet<Cell>(variables.keySet());
		cells.removeAll(abnormalVariablesMap.keySet());
		return cells;
	}

	/**
	 * @return Cell to cell variable name mapping
	 */
	public Map<Cell, String> getVariables() {
		return variables;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("SMT Constraint Strategy Generation Information\n");
		s.append(String.format("Amount of cell variables:     %d\n",
				variables.size()));
		s.append(String.format("Amount of abnormal variables: %d\n",
				abnormalVariablesMap.size()));
		return s.toString();
	}

}
