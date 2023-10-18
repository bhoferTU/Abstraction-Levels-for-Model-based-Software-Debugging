package at.tugraz.ist.debugging.modelbased.smt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstants.Option;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstants.Satisfiability;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation.Avoidance;
import at.tugraz.ist.debugging.modelbased.smt.datastructures.Model;
import at.tugraz.ist.debugging.modelbased.smt.datastructures.UnsatCore;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyResult;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpanMeasurement;

/**
 * Implementation of the MCSes-U algorithm by Liffiton et al.
 * 
 * This implementation is based on the SMT code generator and parser, i.e. it
 * uses a pipe to communicate the SMT solver.
 * 
 */
public class SMTMCSesUConstraintStrategy extends SMTConstraintStrategy {

	public SMTMCSesUConstraintStrategy() {
		super();
	}

	@Override
	protected void initialize(CellContainer cellContainer,
			Set<String> wrongCells) {
		super.initialize(cellContainer, wrongCells);
		info = new SMTConstraintStrategyGenerationInformation(generator, cones,
				ConstraintStrategyConfiguration.useCones(), 
                ConstraintStrategyConfiguration.getModelGranularity());
	}

	@Override
	public ConstraintStrategyResult solveConstraints(
			CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues) {

		try {
			TimeSpan runtime = new TimeSpan(0, ConstraintStrategyConfiguration.getPrecision());
			TimeSpanMeasurement measurement = new TimeSpanMeasurement(
					ConstraintStrategyConfiguration.getPrecision());
			// // add constraint that at least one abnormal must be set
			// (otherwise
			// // we would not gain anything from a satisfied run)

			// STACK FRAME 1
			// add constraints for input cells
			generator.addOption(Option.ProduceModels, true);
			generator.addOption(Option.ProduceUnsatCores, true);
			// create variables
			createVariables(cellContainer, info);
			Writer.println(info.toString());

			// add constraints for output cells
			for (Cell cell : info.getAbnormalVariables().keySet()) {
				cell.addSMTConstraint(info);
			}

			abnormals = info.getAbnormalVariableNames();
			weightVariable = info.addWeightFunction(generator.not(abnormals));

			generator.addPushFrame();

			for (Cell cell : info.getInputCells()) {
				cell.addSMTConstraint(info);
			}

			// STACK FRAME 2
			// add constraints for observed wrong cells
			generator.addPushFrame();
			for (Entry<Coords, ConstExpression> entry : referenceValues
					.entrySet()) {
				Cell currentCell = cellContainer.getCell(entry.getKey());
				if (currentCell == null)
					throw new InvalidOperationException(
							"ConstraintStrategy: Output cell must not be null");
				if (entry.getValue() == null) {
				} else {
					generator.addAssertion(generator.equal(info.getVariables()
							.get(currentCell), entry.getValue()
							.getSMTConstraint(info)));
				}
			}

			// boolean terminate = false;
			Satisfiability satisfiability;
			int k = 1;
			List<List<Cell>> mcs = new ArrayList<List<Cell>>();

			// retrieve core
			measurement.start();
			generator.addCheckSat(abnormals);
			satisfiability = parser.isSatisfied();
			runtime.add(measurement.stop());

			if (satisfiability != Satisfiability.Unsat) {
				// there is no problem
				Writer.println("There is no problem");
                ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
                result.setRuntimeSolving(runtime);
                return result;
			}
			generator.addGetUnsatCore();
			UnsatCore core = parser.getUnsatCore();

			measurement.start();
			generator.addCheckSat();
			satisfiability = parser.isSatisfied();
			runtime.add(measurement.stop());

			while (satisfiability == Satisfiability.Sat) {

				// STACK FRAME 3 (AllSAT)
				// instrument all constraints whose abnormals are in core
				// and add at most property
				Model model = null;
				do {
					if (model != null)
						info.addAvoid(model, Avoidance.FalseOnly);
					generator.addPushFrame();
					generator.addAssertion(generator.leq(weightVariable,
							generator.getValue(k)));
					for (String abnormal : abnormals) {
						// avoid the relaxation of constraints which are
						// not included in the core
						if (!core.contains(abnormal)) {
							generator.addAssertion(abnormal);
						}
					}

					measurement.start();
					generator.addCheckSat();
					satisfiability = parser.isSatisfied();
					runtime.add(measurement.stop());

					if (satisfiability == Satisfiability.Sat) {
						generator.addGetValue(abnormals);
						model = parser.getModel();
						// Writer.println(model.toString());
						List<Cell> abnormalAssignment = new ArrayList<Cell>();
						for (String abnormalVarName : model.getFalseVariables()) {
							abnormalAssignment.add(info
									.getAbnormalVariable(abnormalVarName));
						}
						mcs.add(abnormalAssignment);
					}
					generator.addPopFrame();
				} while (satisfiability == Satisfiability.Sat);

				// update core
				String[] nonAbnormals = new String[abnormals.length
						- core.size()];
				int i = 0;
				for (String abnormal : abnormals) {
					if (!core.contains(abnormal)) {
						nonAbnormals[i] = abnormal;
						i++;
					}
				}

				generator.addPushFrame();
				generator.addAssertion(generator.leq(weightVariable,
						generator.getValue(k)));

				measurement.start();
				generator.addCheckSat(nonAbnormals);
				satisfiability = parser.isSatisfied();
				runtime.add(measurement.stop());

				if (satisfiability == Satisfiability.Unsat) {
					// we have a new core
					generator.addGetUnsatCore();
					UnsatCore newCore = parser.getUnsatCore();
					int oldCoreSize = core.size();
					core.addCore(newCore);
					Writer.println("Core size: " + core.size());
					if (oldCoreSize == core.size()) {
						if (ConstraintStrategyConfiguration.useEarlyTermination()
								&& !mcs.isEmpty()) {
							Writer.println(abnormalsToString(mcs));
							ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
                            result.setRuntimeSolving(runtime);
                            return result;
						}
						k++;
					}

				} else {
					if (ConstraintStrategyConfiguration.useEarlyTermination() && !mcs.isEmpty()) {
						Writer.println(abnormalsToString(mcs));
                        ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
                        result.setRuntimeSolving(runtime);
                        return result;
					}

					k++;
				}

				generator.addPopFrame();

				measurement.start();
				generator.addCheckSat();
				satisfiability = parser.isSatisfied();
				runtime.add(measurement.stop());

			}

			// REENTER STACK FRAME 1
			// pop assertions of current run
			generator.addPopFrame();

			// REENTER STACK FRAME 0
			// pop assertions for input cells of current test case
			generator.addPopFrame();

			Writer.println(abnormalsToString(mcs));
			ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
            result.setRuntimeSolving(runtime);
            return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
