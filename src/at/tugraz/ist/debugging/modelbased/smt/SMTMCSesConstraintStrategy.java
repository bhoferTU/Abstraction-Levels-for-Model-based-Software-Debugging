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
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyResult;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpanMeasurement;

/**
 * Contains the implementation of the MCSes algorithm by Liffiton and Sakallah
 * 
 * The implementation of this MCSes enumeration is based on the SMT code
 * generator and parser
 * 
 */
public class SMTMCSesConstraintStrategy extends SMTConstraintStrategy {

	public SMTMCSesConstraintStrategy() {
		super();
	}

	@Override
	protected void initialize(CellContainer cellContainer,
			Set<String> wrongCells) {

		super.initialize(cellContainer, wrongCells);
		try {
			abnormals = null;
			weightVariable = null;

			info = new SMTConstraintStrategyGenerationInformation(generator,
					cones, ConstraintStrategyConfiguration.useCones(), 
                    ConstraintStrategyConfiguration.getModelGranularity());
			generator.addOption(Option.ProduceModels, true);
            //generator.addSetLogic();
            
			// create variables
			createVariables(cellContainer, info);

			// add constraints for output cells
			for (Cell cell : info.getAbnormalVariables().keySet()) {
				cell.addSMTConstraint(info);
			}

			abnormals = info.getAbnormalVariableNames();
			weightVariable = info.addWeightFunction(generator.not(abnormals));

			Writer.println(info.toString());

		} catch (IOException e) {
			throw new RuntimeException("SMT-MCS: error while initialization", e);
		}
	}

	@Override
	public ConstraintStrategyResult solveConstraints(
			CellContainer cellContainer,
			Map<Coords, ConstExpression> referenceValues) {

		try {
			TimeSpan runtime = new TimeSpan(0, ConstraintStrategyConfiguration.getPrecision());
			TimeSpanMeasurement measurement = new TimeSpanMeasurement(
					ConstraintStrategyConfiguration.getPrecision());
			// STACK FRAME 1
			// add constraints for input cells
			generator.addPushFrame();
			for (Cell cell : info.getInputCells()) {
				cell.addSMTConstraint(info);
			}

			// add constraints for observed wrong cells
			for (Entry<Coords, ConstExpression> entry : referenceValues
					.entrySet()) {
				Cell currentCell = cellContainer.getCell(entry.getKey());
				// Writer.println("Current cell"
				// + currentCell.getPositionAsString());
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
			List<List<Cell>> abnormalAssignments = new ArrayList<List<Cell>>();
			boolean terminate = false;

			int k = 0;
			do {
				// find solutions for given test case
				measurement.start();
				generator.addCheckSat();
				satisfiability = parser.isSatisfied();
				runtime.add(measurement.stop());

				if (satisfiability == Satisfiability.Unknown) {
					// try simplification
					Writer.println("Warning: Solver returned unknown! Using simplifying checksat next...");
					generator.addCheckSatSimplify();
					satisfiability = parser.isSatisfied();
				}

				if (satisfiability == Satisfiability.Sat) {
					k++;
					while (satisfiability == Satisfiability.Sat) {
						generator.addPushFrame();
						generator.addAssertion(generator.lt(weightVariable,
								generator.getValue(k)));

						measurement.start();
						generator.addCheckSat();
						satisfiability = parser.isSatisfied();
						runtime.add(measurement.stop());

						if (satisfiability == Satisfiability.Sat) {
							generator.addGetValue(abnormals);
							Model model = parser.getModel();

							generator.addPopFrame();

							// add constraint in order to avoid that we get the
							// solution
							// again
							if (model.getFalseVariablesSet().size() == 0) {
                                ConstraintStrategyResult result = new ConstraintStrategyResult(abnormalAssignments, info); 
                                result.setRuntimeSolving(runtime);
                                return result;
                            }
							info.addAvoid(model, Avoidance.FalseOnly);
							List<Cell> abnormalAssignment = new ArrayList<Cell>();
							for (String abnormalVarName : model
									.getFalseVariables()) {
								abnormalAssignment.add(info
										.getAbnormalVariable(abnormalVarName));
							}
							if (abnormalAssignment.size() == 0) {
								generator.addPopFrame();
								generator.addPopFrame();
								ConstraintStrategyResult result = new ConstraintStrategyResult(abnormalAssignments, info); 
                                result.setRuntimeSolving(runtime);
                                return result;
							}
							abnormalAssignments.add(abnormalAssignment);

						} else
							generator.addPopFrame();
					}
				} else {
					if (satisfiability == Satisfiability.Unknown)
						Writer.println("Warning: Solver returned unknown! Terminate loop.");
					terminate = true;
				}
			} while (!terminate
					&& (!ConstraintStrategyConfiguration.useEarlyTermination() || abnormalAssignments
							.isEmpty()));

			generator.addPopFrame();
			Writer.println(abnormalsToString(abnormalAssignments));
            ConstraintStrategyResult result = new ConstraintStrategyResult(abnormalAssignments, info); 
            result.setRuntimeSolving(runtime);
            return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
