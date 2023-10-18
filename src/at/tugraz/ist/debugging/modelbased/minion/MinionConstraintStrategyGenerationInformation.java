package at.tugraz.ist.debugging.modelbased.minion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.util.debugging.Writer;

public class MinionConstraintStrategyGenerationInformation extends
		ConstraintStrategyGenerationInformation {

	public enum Domain {
		BOOLEAN, INTEGER, INT3
	}
	
	private int abnormalIndex = -1;

	public static int MAX_INT_DOMAIN = 50000;
	public static int MIN_INT_DOMAIN = -2000; //-2000; 0
	protected static final int STRING_MAPPINGS_AMOUNT = 1000;

	protected static final String TEMP_VAR_PREFIX = "tmp";

	protected static final String VAR_CELL_ABNORMAL_PREFIX = "ab";

	protected static String variablePostfix_ = null;

	public int getAbnormalIndex() {
		return abnormalIndex;
	}

	public void setAbnormalIndex(int abnormalIndex) {
		this.abnormalIndex = abnormalIndex;
	}
	
	public static int getMaxIntDomain() {
		return MAX_INT_DOMAIN;
	}

	public static int getMinIntDomain() {
		return MIN_INT_DOMAIN;
	}

	public static String getVariablePostfix() {
		return variablePostfix_;
	}

	public static void setMaxIntDomain(int maxIntDomain) {
		MAX_INT_DOMAIN = maxIntDomain;
	}

	public static void setMinIntDomain(int minIntDomain) {
		MIN_INT_DOMAIN = minIntDomain;
	}

	public static void setVariablePostfix(String variablePostfix_) {
		MinionConstraintStrategyGenerationInformation.variablePostfix_ = variablePostfix_;
	}

	int abnormalCounter = 0;

	Map<Integer, Cell> abnormalToCellMapping = new HashMap<Integer, Cell>();
	private List<List<Cell>> blockingSolutions;
	Map<Cell, Integer> cellToAbnormalMapping = new HashMap<Cell, Integer>();

	/**
	 * stores the amount of cell variables (needed since the tempVarCounter is
	 * increasing)
	 */
	int cellVariableCounter = 0;

	//List<String> constraints = new ArrayList<String>();
	Set<String> constraints = new HashSet<String>();

	int solutionSize = 1;

	/**
	 * Counter for string to int mappings
	 */
	int stringMappingCounter = MAX_INT_DOMAIN;

	/**
	 * String to integer mapping
	 */
	private Map<String, Integer> stringToIntMapping = new HashMap<String, Integer>();

	int tempVarCounter = 0;

	Set<String> testCase = new HashSet<String>();

	Map<String, Domain> variables = new HashMap<String, Domain>();

	public MinionConstraintStrategyGenerationInformation(Set<Cell> cone, 
            boolean useCones, EModelGranularity modelGranularity) {
		super(cone, useCones, modelGranularity);
	}

	private boolean computeAllDiagnoses = false;

	public MinionConstraintStrategyGenerationInformation(Set<Cell> cone,
														 boolean useCones, EModelGranularity modelGranularity,
														 boolean computeAllDiagnoses) {
		super(cone, useCones, modelGranularity);
		this.computeAllDiagnoses = computeAllDiagnoses;
	}

	public void addTestCase(Set<String> constraint) {
		testCase.addAll(constraint);
	}

	protected String addVariablePostFix(String constraints) {
		if (variablePostfix_ == null) {
			return constraints;
		}
		for (String variable : variables.keySet()) {
			constraints = constraints.replaceAll(variable, variable + "_"
					+ variablePostfix_);
		}
		return constraints;
	}

	public void convertDependencyBased(CellContainer cellContainer,
			Boolean useStrings) {

		for (Cell cell : cellContainer.getCells()) {
			Object value = cell.evaluate();
			if (value instanceof String && !useStrings) {
				continue;
			}
			if (useCones && !cone.contains(cell))
				continue;
			String cellVariableName = cell.getCoords().getMinionString();
			variables.put(cellVariableName, Domain.BOOLEAN);
		}
		cellVariableCounter = variables.size();

		for (Cell cell : cellContainer.getCells()) {
			
			if (useCones && !cone.contains(cell))
				continue;
			String cellName = cell.getCoords().getMinionString();
			if (variables.containsKey(cellName)) {
				MinionExpressionConstraints cellConstraints = cell
						.getMinionConstraints(this);
				constraints.addAll(cellConstraints.getConstraints());
				testCase.addAll(cellConstraints.getConstraintsTC());
			}

		}
	}
	
	public void convertComparisonBased(CellContainer cellContainer,
			Boolean useStrings) {

		for (Cell cell : cellContainer.getCells()) {
			Object value = cell.evaluate();
			if (value instanceof String && !useStrings) {
				continue;
			}
			if (useCones && !cone.contains(cell))
				continue;
			String cellVariableName = cell.getCoords().getMinionString();
			variables.put(cellVariableName, Domain.INT3);
		}
		cellVariableCounter = variables.size();

		for (Cell cell : cellContainer.getCells()) {
			
			if (useCones && !cone.contains(cell))
				continue;
			String cellName = cell.getCoords().getMinionString();
			if (variables.containsKey(cellName)) {
				MinionExpressionConstraints cellConstraints = cell
						.getMinionConstraints(this);
				constraints.addAll(cellConstraints.getConstraints());
				testCase.addAll(cellConstraints.getConstraintsTC());
			}

		}
	}

	public void convertValueBased(CellContainer cellContainer,
			Boolean USE_STRING) {
		for (Cell cell : cellContainer.getCells()) {
			if (useCones && !cone.contains(cell))
				continue;
			
			

			Object value = cell.evaluate();
			String cellVariableName = cell.getCoords().getMinionString();

			if (value instanceof Boolean)
				variables.put(cellVariableName, Domain.BOOLEAN);
			else if (value instanceof Integer) {
				variables.put(cellVariableName, Domain.INTEGER);
			} else if (value instanceof String) {
				if (USE_STRING) {
					Writer.println(String.format(
							"String value in cell %s is mapped to integer",
							cell.getCoords().getUserString()));
					Integer val = mapString((String) value);
					variables.put(cellVariableName, Domain.INTEGER);
					setVariableValue(cellVariableName, val.toString());
				} else {
					Writer.println(String.format(
							"String value in cell %s is ignored",
							cell.getCoords().getUserString()));
				}
			} else if (value == null) {
				// RESTRICTION: null cells are mapped to integers
				variables.put(cellVariableName, Domain.INTEGER);
				setVariableValue(cellVariableName, "0");
				Writer.println(String
						.format("Warning (Constraint strategy): Cell %s is mapped to an integer variable",
								cell.getCoords().getUserString()));
			} else
				throw new InvalidOperationException(String.format(
						"Cell %s: Result type '%s' is not supported",
						cell.getCoords().getUserString(), value.getClass().getName()));
		}

		cellVariableCounter = variables.size();

		for (Cell cell : cellContainer.getCells()) {
			if (useCones && !cone.contains(cell))
				continue;

			String cellName = cell.getCoords().getMinionString();
			if (variables.containsKey(cellName)) {
				MinionExpressionConstraints cellConstraints = cell
						.getMinionConstraints(this);
				constraints.addAll(cellConstraints.getConstraints());
				testCase.addAll(cellConstraints.getConstraintsTC());
			}

		}
	};

	public void deleteVariable(String varName) {
		variables.remove(varName);
	}

	@Override
	public int getAbnormalsCount() {
		return abnormalToCellMapping.size();
	}

	public List<List<Cell>> getBlockingSolutions() {
		return blockingSolutions;
	}

	public Cell getCellForAbnormalIndex(Integer abnormalIndex) {
		return abnormalToCellMapping.get(abnormalIndex);
	}

	@Override
	public int getCellVariablesCount() {
		return cellVariableCounter;
	}

	protected String getConstraintsForBlockingSolutions() {
		StringBuilder sb = new StringBuilder();
		for (List<Cell> solution : blockingSolutions) {
			if (solution.size() == 1) {
				sb.append("element(ab,"
						+ cellToAbnormalMapping.get(solution.get(0)) + ",0)"
						+ System.lineSeparator());
			} else {
				sb.append("watched-or({");
				for (Cell c : solution) {
					sb.append("element(ab," + cellToAbnormalMapping.get(c)
							+ ",0),");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("})" + System.lineSeparator());
			}
		}
		return sb.toString();
	}

	public Integer getNextAbnormalVariableIndex(Cell cell) {
		if (variables.containsKey(VAR_CELL_ABNORMAL_PREFIX + "["
				+ abnormalCounter + "]"))
			variables.remove(VAR_CELL_ABNORMAL_PREFIX + "[" + abnormalCounter
					+ "]");
		variables.put(VAR_CELL_ABNORMAL_PREFIX + "[" + (++abnormalCounter)
				+ "]", Domain.BOOLEAN);
		abnormalToCellMapping.put(abnormalCounter - 1, cell);
		cellToAbnormalMapping.put(cell, abnormalCounter - 1);
		abnormalIndex = abnormalCounter-1;
		return abnormalCounter - 1;
	}

	public String getNextAuxiliaryVariable(Domain domain) {
		String var = TEMP_VAR_PREFIX + (tempVarCounter++);
		variables.put(var, domain);
		return var;
	}

	public Integer getNumberAbnormalVariables() {
		return abnormalToCellMapping.size();
	}

	public int getNumberOfConstraints() {
		return constraints.size();
	}

	public int getSolutionSize() {
		return solutionSize;
	}

	public Domain getVariableDomain(String varName) {
		return variables.get(varName);
	}

	public boolean isAuxhiliaryVariable(String varName) {
		if (varName.startsWith(TEMP_VAR_PREFIX))
			return true;
		return false;
	}

	public boolean isVariableDefined(String varName) {
		return variables.containsKey(varName);
	}

	/**
	 * Determines an integer value which represents the given string and stores
	 * this mapped pair in thestringToIntMapping map. The integer value is
	 * returned afterwards.
	 * 
	 * @param value
	 * @return
	 */
	public int mapString(String value) {
		if (stringToIntMapping.containsKey(value))
			return stringToIntMapping.get(value);

		if (stringToIntMapping.size() > STRING_MAPPINGS_AMOUNT)
			throw new RuntimeException(
					"Maximum number of string mappings exceeded");
		stringToIntMapping.put(value, stringMappingCounter);
		stringMappingCounter--;
		return stringMappingCounter - 1;
	}

	public void setBlockingSolutions(List<List<Cell>> blockingSolutions) {
		this.blockingSolutions = blockingSolutions;
	}

	public void setSolutionSize(int solutionSize) {
		this.solutionSize = solutionSize;
	}

	private void setVariableValue(String variableName, String value) {
		testCase.add("eq(" + variableName + "," + value + ")");
	}
	
	public static
	<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MINION 3" + System.lineSeparator());
		if(this.getModelGranularity()==EModelGranularity.Comparison){
			sb.append("# Modeling the domain = > <");
			sb.append(System.lineSeparator());
			sb.append("# Values: 0 < / 1 = / 2 >");
			sb.append(System.lineSeparator());
			sb.append(System.lineSeparator());
		}
		sb.append("**VARIABLES**" + System.lineSeparator());
		
		List<String> sortedVariables = asSortedList(variables.keySet());
		for (String variable : sortedVariables) {
			if (variables.get(variable).equals(Domain.BOOLEAN)) {
				sb.append("BOOL " + variable + System.lineSeparator());
			} else if (variables.get(variable).equals(Domain.INTEGER)) {
				sb.append("DISCRETE " + variable + " {" + MIN_INT_DOMAIN + ".."
						+ MAX_INT_DOMAIN + "}" + System.lineSeparator());
			} else if (variables.get(variable).equals(Domain.INT3)) {
				sb.append("DISCRETE " + variable + " {" + 0 + ".."
						+ 2 + "}" + System.lineSeparator());
			}
		}
		if(this.getModelGranularity()==EModelGranularity.Comparison){
			sb.append(MinionConstraints.getComparisonModelTables());
		}
		if(this.getModelGranularity()==EModelGranularity.Dependency){
			sb.append(MinionConstraints.getDependencyModelTables());
		}
		sb.append("**SEARCH**" + System.lineSeparator());
		sb.append("VARORDER [" + VAR_CELL_ABNORMAL_PREFIX + "]"
				+ System.lineSeparator());
		sb.append("PRINT [" + VAR_CELL_ABNORMAL_PREFIX + "]");
		
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());

		sb.append("**CONSTRAINTS**" + System.lineSeparator());
		sb.append("# System description" + System.lineSeparator());
		for (String constraint : constraints) {
			sb.append(constraint + System.lineSeparator());
		}

		sb.append(System.lineSeparator());
		sb.append("# TEST CASE / Observations" + System.lineSeparator());
		List<String> sortedTestCase = asSortedList(testCase);
		for (String constraint : sortedTestCase) {
			sb.append(constraint + System.lineSeparator());
		}

		sb.append(System.lineSeparator());
		sb.append("#SIZE OF SOLUTION" + System.lineSeparator());
		sb.append("watchsumgeq(" + VAR_CELL_ABNORMAL_PREFIX + ","
				+ solutionSize + ")" + System.lineSeparator());
		sb.append("watchsumleq(" + VAR_CELL_ABNORMAL_PREFIX + ","
				+ solutionSize + ")" + System.lineSeparator());

		if(!computeAllDiagnoses){
			if (blockingSolutions.size() > 0) {
				sb.append(this.getConstraintsForBlockingSolutions());
			}
		}

		sb.append("**EOF**" + System.lineSeparator());
		return addVariablePostFix(sb.toString());
	}

}
