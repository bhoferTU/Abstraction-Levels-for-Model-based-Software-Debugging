package at.tugraz.ist.debugging.modelbased.choco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyResult;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.IntConstant;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.StringConstant;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpanMeasurement;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solution;
import choco.kernel.solver.search.ISolutionPool;

public class ChocoConstraintStrategy extends ConstraintStrategy {
	public enum TweakExtension {
		AbnormalsFirst, None, RandomAbnormals, RandomAbnormalsGoal, UseExternalVariableOrder
	};

	public static final TweakExtension VARIABLE_ORDER = TweakExtension.AbnormalsFirst;

	private IntegerVariable[] getVariableSearchOrder(
			CellContainer cellContainer,
			ChocoConstraintStrategyGenerationInformation info) {
		List<Cell> sortedCells = new ArrayList<Cell>(
				info.getOverallVariablesCount());

		Map<Cell, Variable> variables = new HashMap<Cell, Variable>(
				info.getVariables());

		Map<Cell, Set<Cell>> cellToCellReferences = new HashMap<Cell, Set<Cell>>();

		for (Cell cell : cellContainer.getCells()) {
			// check if cell is ignored
			if (!variables.containsKey(cell))
				continue;

			if (cellContainer.isInputCell(cell)) {
				// add input cells immediately
				sortedCells.add(cell);
				variables.remove(cell);
			} else {
				// save cell references
				cellToCellReferences.put(cell, cell.getAllReferencesRecursive());
			}
		}

		// solve regarding to cell references
		while (!variables.isEmpty()) {
			// cells which are added during an iteration
			Set<Cell> addedCells = new HashSet<Cell>();
			Set<Cell> availableCells = new HashSet<Cell>(sortedCells);
			for (Cell cell : variables.keySet()) {
				Set<Cell> tmpCells = new HashSet<Cell>(
						cellToCellReferences.get(cell));
				tmpCells.removeAll(availableCells);
				if (tmpCells.isEmpty()) {
					addedCells.add(cell);
					sortedCells.add(cell);
				}
			}

			// remove handled cells
			for (Cell cell : addedCells)
				variables.remove(cell);
		}

		// create variables array given the sorted cells list
		variables = info.getVariables();
		List<IntegerVariable> sortedVariables = new ArrayList<>(
				info.getOverallVariablesCount());

		for (Cell cell : sortedCells) {
			if (info.getAbnormalVariables().get(cell) != null)
				sortedVariables.add(info.getAbnormalVariables().get(cell));

			// add auxiliary variables
			sortedVariables.addAll(info.getCellAuxiliaryVariables(cell));

			// add cell variables
			Variable var = variables.get(cell);
			if (!(var instanceof IntegerVariable))
				throw new UnsupportedOperationException(
						"Cannot handle unsupported cell variable type");
			sortedVariables.add((IntegerVariable) var);
		}

		// build array
		IntegerVariable[] sortedVariablesArray = new IntegerVariable[sortedVariables
				.size()];
		sortedVariablesArray = sortedVariables.toArray(sortedVariablesArray);
		return sortedVariablesArray;

	}

	// private void dumpResult(CellContainer cellContainer, CPSolver solver,
	// Map<Cell, Variable> variables,
	// Map<Cell, IntegerVariable> abnormalVariables) {
	// // input cells
	// for (Cell cell : cellContainer.getCells()) {
	// if (variables.containsKey(cell)) {
	// Writer.println(String.format(
	// "%1s: %2$-15s %3$-15s",
	// cell.getPositionAsString(),
	// solver.getVar(variables.get(cell)),
	// abnormalVariables.containsKey(cell) ? String.format(
	// "Abnormal: %s",
	// solver.getVar(abnormalVariables.get(cell)))
	// : ""));
	// }
	// }
	// }

	@Override
	protected void initialize(CellContainer cellContainer,
			Set<String> wrongCells) {

	}

	@Override
	public ConstraintStrategyResult solveConstraints(
			CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues) {

		if (!ConstraintStrategyConfiguration.useEarlyTermination())
			throw new UnsupportedOperationException(
					"Full diagnosis finding is currently not implemented");

		CPModel model = new CPModel();
		List<List<Cell>> abnormalSolutions = new ArrayList<List<Cell>>();
		// create variables for cells and abnormals
		ChocoConstraintStrategyGenerationInformation info = new ChocoConstraintStrategyGenerationInformation(
				model, cones, ConstraintStrategyConfiguration.useCones(), 
                ConstraintStrategyConfiguration.getModelGranularity());
		info.createVariablesAndCellConstraints(cellContainer,
				ConstraintStrategyConfiguration.useStrings());
		Writer.println(info.toString());

		Map<Cell, IntegerVariable> abnormalVariablesMap = info
				.getAbnormalVariables();
		Map<IntegerVariable, Cell> revAbnormalVariablesMap = info
				.getRevAbnormalVariables();

		Set<IntegerVariable> abnormalSet = new HashSet<IntegerVariable>(
				abnormalVariablesMap.values());

		// create array with all variable which are not abnormals
		Set<IntegerVariable> otherIntVars = new HashSet<IntegerVariable>();
		for (int i = 0; i < model.getNbIntVars(); ++i) {
			if (!abnormalSet.contains(model.getIntVar(i)))
				otherIntVars.add(model.getIntVar(i));
		}

		IntegerVariable[] otherIntVarsArray = new IntegerVariable[otherIntVars
				.size()];
		otherIntVarsArray = otherIntVars.toArray(otherIntVarsArray);

		List<IntegerVariable> knownVariablesList = new ArrayList<IntegerVariable>();

		// Writer.println(model.pretty());

		IntegerVariable upperBound = Choco.makeIntVar("upperBound", 0, info
				.getVariables().size());
		IntegerVariable upperBoundActive = Choco
				.makeBooleanVar("upperBoundActive");

		IntegerVariable[] abnormalVariables = new IntegerVariable[abnormalVariablesMap
				.size()];
		abnormalVariables = abnormalVariablesMap.values().toArray(
				abnormalVariables);

		// upper bound logic
		model.addVariable(upperBoundActive);
		model.addConstraint(Choco.implies(
				Choco.eq(upperBoundActive, Choco.ONE),
				Choco.eq(Choco.sum(abnormalVariables), upperBound)));

		// add constraints which cell is wrong
		for (Entry<Coords, ConstExpression> entry : referenceValues.entrySet()) {
			Cell currentCell = cellContainer.getCell(entry.getKey());
			if (currentCell == null)
				throw new InvalidOperationException(
						"ConstraintStrategy: Output cell must not be null");
			if (entry.getValue() == null) {
			} else {
				if (entry.getValue() instanceof IntConstant) {
					int value = ((IntConstant) entry.getValue()).evaluate();
					Variable var = info.getVariables().get(currentCell);
					if (!(var instanceof IntegerVariable))
						throw new InvalidOperationException(
								String.format(
										"Cell %d: Integer variable must be represented as an integer variable",
										currentCell.getCoords().getUserString()));

					model.addConstraint(Choco.eq((IntegerVariable) var, value));
					knownVariablesList.add((IntegerVariable) var);
				} else if (entry.getValue() instanceof StringConstant) {
					if (ConstraintStrategyConfiguration.useStrings()) {
						int value = info.mapString(((StringConstant) entry
								.getValue()).evaluate());
						Variable var = info.getVariables().get(currentCell);
						if (!(var instanceof IntegerVariable))
							throw new InvalidOperationException(
									String.format(
											"Cell %d: String representative variable must be integer variable",
											currentCell.getCoords().getUserString()));
						model.addConstraint(Choco.eq((IntegerVariable) var,
								value));
						knownVariablesList.add((IntegerVariable) var);
					}
				} else {
					throw new InvalidOperationException(String.format(
							"Cell %d: Constant type '%s' is not recognized",
							currentCell.getCoords().getUserString(), entry.getValue()
									.getTypeAsString()));

				}
			}
		}

		IntegerVariable[] knownVariables = new IntegerVariable[knownVariablesList
				.size()];
		knownVariables = knownVariablesList.toArray(knownVariables);

		CPSolver solver = new CPSolver();

		boolean hasSolution = false;

		Constraint upperBoundConstraint = null;
		Constraint upperBoundValueConstraint = null;

		upperBoundConstraint = Choco.eq(upperBoundActive, 1);
		upperBoundValueConstraint = Choco.eq(upperBound, 0);

		TimeSpan runtime = new TimeSpan(0, ConstraintStrategyConfiguration.getPrecision());
		TimeSpanMeasurement measurement = new TimeSpanMeasurement(
				ConstraintStrategyConfiguration.getPrecision());
		int k = 1;
		do {
			solver.clear();
			solver.setModel(null);

			// solve without upper bound constraints
			if (upperBoundConstraint != null
					&& model.contains(upperBoundConstraint)) {
				model.removeConstraint(upperBoundConstraint);
				model.removeConstraint(upperBoundValueConstraint);
			}
			upperBoundConstraint = Choco.eq(upperBoundActive, 1);
			upperBoundValueConstraint = Choco.eq(upperBound, k);
			model.addConstraint(upperBoundConstraint);
			model.addConstraint(upperBoundValueConstraint);
			solver.read(model);
			// configure solution pool
			solver.getConfiguration().putInt(
					Configuration.SOLUTION_POOL_CAPACITY, 100);

			switch (VARIABLE_ORDER) {
			case None:
				break;
			case RandomAbnormals:
				solver.setVarIntSelector(new StaticVarOrder(solver, solver
						.getVar(abnormalVariables)));
				break;
			case RandomAbnormalsGoal:
				solver.clearGoals();
				solver.addGoal(BranchingFactory.minDomMinVal(solver,
						solver.getVar(abnormalVariables)));
				break;
			case UseExternalVariableOrder:
				solver.setVarIntSelector(new StaticVarOrder(solver, solver
						.getVar(getVariableSearchOrder(cellContainer, info))));
				break;
			case AbnormalsFirst:
				solver.setVarIntSelector(new FasterIntVarSelector(solver,
						abnormalVariables, otherIntVarsArray));
				solver.setValIntIterator(new IncreasingDomain());
				solver.setValIntSelector(new MinVal());
			default:
				break;

			}

			// System.out.println(model.pretty());

			measurement.start();
			hasSolution = solver.solveAll();
			// Writer.println("Solving Runtime: " + (System.currentTimeMillis()
			// - time)
			// + "ms");
			runtime.add(measurement.stop());
			if (hasSolution) {
				ISolutionPool solutionPool = solver.getSearchStrategy()
						.getSolutionPool();
				List<Solution> solverSolutions = solutionPool.asList();
				for (Solution solution : solverSolutions) {
					List<Cell> abnormalSolution = new ArrayList<Cell>();
					List<IntegerVariable> blockingClauseList = new ArrayList<IntegerVariable>();
					for (IntegerVariable abnormalVar : abnormalVariables) {
						if (solution.getIntValue(solver.getIntVarIndex(solver
								.getVar(abnormalVar))) != 0) {
							abnormalSolution.add(revAbnormalVariablesMap
									.get(abnormalVar));
							blockingClauseList.add(abnormalVar);
						}
					}
					// add blocking clause
					// IntegerVariable[] blockingClauseArray = new
					// IntegerVariable[abnormalSolution
					// .size()];
					// blockingClauseArray = blockingClauseList
					// .toArray(blockingClauseArray);
					// model.addConstraint(Choco.not(Choco
					// .and(blockingClauseArray)));
					abnormalSolutions.add(abnormalSolution);
				}
			}
			k++;
		} while (!hasSolution && k <= abnormalVariables.length);
		Writer.println(abnormalsToString(abnormalSolutions));
        ConstraintStrategyResult result = new ConstraintStrategyResult(abnormalSolutions, info); 
        result.setRuntimeSolving(runtime);
		return result;
	}
}
