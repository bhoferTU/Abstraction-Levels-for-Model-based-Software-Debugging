package at.tugraz.ist.debugging.modelbased.choco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategy.TweakExtension;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.util.debugging.Writer;
import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

/**
 * Information class which is needed while building the Choco model
 * 
 * This class provides methods to create the Choco variable instances as well as
 * string mapping functionality. Moreover this class contains cell to Choco
 * variable mappings.
 * 
 */
public class ChocoConstraintStrategyGenerationInformation extends
		ConstraintStrategyGenerationInformation {
	private static final int MAX_INT = 5000;
	private static final int MIN_INT = -2000;
	private static final int STRING_MAPPINGS_AMOUNT = 0;

	private final Map<Cell, IntegerVariable> abnormalVariablesMap = new HashMap<>();

	private final List<IntegerVariable> auxiliaryVariables = new ArrayList<>();

	private final Map<Cell, List<IntegerVariable>> cellToAuxiliaryVariablesMapping = new HashMap<>();

	private final CPModel model;

	private final Map<IntegerVariable, Cell> revAbnormalVariablesMap = new HashMap<>();

	/**
	 * Counter for string to int mappings
	 */
	int stringMappingCounter = MAX_INT + 1;

	/**
	 * String to integer mapping
	 */
	private Map<String, Integer> stringToIntMapping = new HashMap<>();

	private List<IntegerVariable> tmpAuxiliaryVariables = null;

	/**
	 * Cell to cell (Choco) variable mapping
	 */
	private Map<Cell, Variable> variables = new HashMap<>();

	public ChocoConstraintStrategyGenerationInformation(CPModel model,
			Set<Cell> cone, boolean useCones, EModelGranularity modelGranularity) {
		super(cone, useCones, modelGranularity);
		this.model = model;
	}

	public IntegerVariable addAuxConstraint(Constraint constraint) {
		IntegerVariable auxVar = getNewAuxiliaryVariable();
		addConstraint(Choco.reifiedConstraint(auxVar, constraint));
		return auxVar;
	}

	public IntegerVariable addAuxIfThenElse(Constraint conditionConstraint,
			IntegerExpressionVariable thenConstraint,
			IntegerExpressionVariable elseConstraint) {
		IntegerVariable auxVar = getNewAuxiliaryVariable();
		addConstraint(Choco.or(Choco.and(conditionConstraint,
				Choco.eq(auxVar, thenConstraint)), Choco.and(
				Choco.not(conditionConstraint),
				Choco.eq(auxVar, elseConstraint))));
		// addConstraint(Choco.ifThenElse(conditionConstraint,
		// Choco.eq(auxVar, thenConstraint),
		// Choco.eq(auxVar, elseConstraint)));
		return auxVar;
	}

	public IntegerVariable addAuxIntDiv(IntegerExpressionVariable expr1,
			IntegerExpressionVariable expr2) {
		IntegerVariable auxVar = getNewAuxiliaryVariable();
		if (!(expr1 instanceof IntegerVariable))
			expr1 = addAuxIntegerConstraint(expr1);
		if (!(expr2 instanceof IntegerVariable))
			expr2 = addAuxIntegerConstraint(expr1);

		// addConstraint(Choco.intDiv(auxVar, (IntegerVariable) expr1,
		// (IntegerVariable) expr2));
		addConstraint(Choco.eq(expr1, Choco.mult(auxVar, expr2)));
		return auxVar;
	}

	public IntegerVariable addAuxIntegerConstraint(
			IntegerExpressionVariable expr) {
		IntegerVariable auxVar = getNewAuxiliaryVariable();
		addConstraint(Choco.eq(auxVar, expr));
		return auxVar;
	}

	public IntegerVariable addBoolAuxConstraint(Constraint constraint) {
		IntegerVariable auxVar = getNewBoolAuxiliaryVariable();
		addConstraint(Choco.reifiedConstraint(auxVar, constraint));
		return auxVar;
	}

	private void addConstraint(Constraint constraint) {
		model.addConstraint(constraint);
	}

	/**
	 * Creates all necessary cell variables for a given cell container and adds
	 * the cell constraints to the CP model
	 * 
	 * @param cellContainer
	 *            Cell container whose cells should be mapped to variables
	 */
	public void createVariablesAndCellConstraints(CellContainer cellContainer,
			boolean useStrings) {
		variables = new HashMap<>();
		for (Cell cell : cellContainer.getCells()) {
			// ignore cells which are not included in the cone
			if (useCones && !cone.contains(cell))
				continue;

			Object value = cell.evaluate();
			String cellPosition = cell.getCoords().getConstraintString();
			String cellVariableName = ConstraintStrategy.VAR_CELL_NAME_PREFIX
					+ cellPosition;

			IntegerVariable var = null;

			if (value instanceof Boolean)
				var = Choco.makeBooleanVar(cellVariableName);
			else if (value instanceof Integer) {
				var = Choco.makeIntVar(cellVariableName, MIN_INT, MAX_INT
						+ STRING_MAPPINGS_AMOUNT, Options.V_BOUND);
			} else if (value instanceof String) {
				if (useStrings) {
					Writer.println(String.format(
							"String value in cell %s is mapped to integer",
							cell.getCoords().getUserString()));
					// int val = mapString((String) value);
					var = Choco.makeIntVar(cellVariableName, MIN_INT, MAX_INT
							+ STRING_MAPPINGS_AMOUNT, Options.V_BOUND);
				} else {
					Writer.println(String.format(
							"String value in cell %s is ignored",
							cell.getCoords().getUserString()));
				}

			}

			else if (value == null) {
				// RESTRICTION: null cells are mapped to integers
				var = Choco.makeIntVar(cellVariableName);
				Writer.println(String
						.format("Warning (Constraint strategy): Cell %s is mapped to an integer variable",
								cell.getCoords().getUserString()));
			}
			// throw new InvalidOperationException(String.format(
			else
				throw new InvalidOperationException(String.format(
						"Cell %s: Result type '%s' is not supported",
						cell.getCoords().getUserString(), value.getClass().getName()));

			if (var != null) {
				variables.put(cell, var);
				model.addVariable(var);
			}
		}

		// add constraints for each cell
		for (Cell cell : getVariables().keySet()) {
			Constraint cellConstraint = cell.getChocoConstraint(this);

			// AB v line constraint
			if (cellContainer.isInputCell(cell)) {
				// there is no abnormal variable for input cells
				model.addConstraint(cellConstraint);
				IntegerVariable var = (IntegerVariable) variables.get(cell);
				Object value = cell.evaluate();
				if (value instanceof String)
					value = mapString((String) value);

				if (value != null) {
					var.setLowB((Integer) value);
					var.setUppB((Integer) value);
				}
				var.addOption(Options.V_NO_DECISION);
			} else {
				// add abnormal variable to model's variables
				IntegerVariable abnormalVariable = Choco
						.makeBooleanVar(ConstraintStrategy.VAR_CELL_NOTABNORMAL_PREFIX
								+ cell.getCoords().getConstraintString());
				model.addVariable(abnormalVariable);
				abnormalVariablesMap.put(cell, abnormalVariable);
				revAbnormalVariablesMap.put(abnormalVariable, cell);
				model.addConstraint(Choco.implies(
						Choco.eq(abnormalVariable, Choco.ZERO), cellConstraint));
				IntegerVariable var = (IntegerVariable) variables.get(cell);
				var.addOption(Options.V_NO_DECISION);
			}
			if (ChocoConstraintStrategy.VARIABLE_ORDER == TweakExtension.UseExternalVariableOrder)
				setAuxiliaryVariableToCellMapping(cell);
		}
	}

	@Override
	public int getAbnormalsCount() {
		return abnormalVariablesMap.size();
	}

	public Map<Cell, IntegerVariable> getAbnormalVariables() {
		return abnormalVariablesMap;
	}

	public List<IntegerVariable> getCellAuxiliaryVariables(Cell cell) {
		if (cellToAuxiliaryVariablesMapping.containsKey(cell))
			return cellToAuxiliaryVariablesMapping.get(cell);
		return new ArrayList<IntegerVariable>();
	}

	@Override
	public int getCellVariablesCount() {
		return variables.size();
	}

	private IntegerVariable getNewAuxiliaryVariable() {
		IntegerVariable var = Choco.makeIntVar(
				"aux" + auxiliaryVariables.size(), MIN_INT, MAX_INT
						+ STRING_MAPPINGS_AMOUNT, Options.V_BOUND);
		auxiliaryVariables.add(var);

		// add auxiliary variable to temporarily unmapped aux vars
		if (ChocoConstraintStrategy.VARIABLE_ORDER == TweakExtension.UseExternalVariableOrder) {
			if (tmpAuxiliaryVariables == null)
				tmpAuxiliaryVariables = new ArrayList<IntegerVariable>();
			tmpAuxiliaryVariables.add(var);
		}
		model.addVariable(var);
		return var;
	}

	private IntegerVariable getNewBoolAuxiliaryVariable() {
		IntegerVariable var = Choco.makeIntVar(
				"aux" + auxiliaryVariables.size(), 0, 1, Options.V_BOUND);
		auxiliaryVariables.add(var);

		// add auxiliary variable to temporarily unmapped aux vars
		if (ChocoConstraintStrategy.VARIABLE_ORDER == TweakExtension.UseExternalVariableOrder) {
			if (tmpAuxiliaryVariables == null)
				tmpAuxiliaryVariables = new ArrayList<IntegerVariable>();
			tmpAuxiliaryVariables.add(var);
		}
		model.addVariable(var);
		return var;
	}

	public int getOverallVariablesCount() {
		return variables.size() + abnormalVariablesMap.size()
				+ auxiliaryVariables.size();
	}

	public Map<IntegerVariable, Cell> getRevAbnormalVariables() {
		return revAbnormalVariablesMap;
	}

	public Map<Cell, Variable> getVariables() {
		return variables;
	}

	/**
	 * Determines an integer value which represents the given string and stores
	 * this mapped pair in the stringToIntMapping map. The integer value is
	 * returned afterwards.
	 * 
	 * @param value
	 * @return
	 */
	public int mapString(String value) {
		if (stringToIntMapping.containsKey(value))
			return stringToIntMapping.get(value);

		// MessageDigest md;
		// try {
		// md = MessageDigest.getInstance("MD5");
		//
		// byte[] digest = md.digest(value.getBytes());
		// int intVal = new BigInteger(Arrays.copyOf(digest, 3)).intValue();
		// stringToIntMapping.put(value, intVal);
		//
		// return intVal;
		//
		// } catch (NoSuchAlgorithmException e) {
		// e.printStackTrace();
		// throw new RuntimeException(
		// "String mapping: Cannot generate MD5 hash instance", e);
		// }

		if (stringToIntMapping.size() > STRING_MAPPINGS_AMOUNT)
			throw new RuntimeException(
					"Maximum number of string mappings exceeded");
		stringToIntMapping.put(value, stringMappingCounter);
		stringMappingCounter++;
		return stringMappingCounter - 1;
	}

	private void setAuxiliaryVariableToCellMapping(Cell cell) {
		if (ChocoConstraintStrategy.VARIABLE_ORDER != TweakExtension.UseExternalVariableOrder)
			throw new InvalidOperationException(
					"Applying auxiliary - cell mapping not supported if external variable order is not activated");
		if (tmpAuxiliaryVariables == null)
			return;

		cellToAuxiliaryVariablesMapping.put(cell, tmpAuxiliaryVariables);
		tmpAuxiliaryVariables = null;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Choco Constraint Strategy Generation Information\n");
		s.append(String.format("Amount of cell variables:     %d\n",
				variables.size()));
		s.append(String.format("Amount of abnormal variables: %d\n",
				abnormalVariablesMap.size()));
		return s.toString();
	}
}
