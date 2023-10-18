package at.tugraz.ist.debugging.modelbased.z3api;

import java.util.ArrayList;
import java.util.List;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyResult;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation.Avoidance;
import at.tugraz.ist.util.debugging.Writer;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;
import com.microsoft.z3.enumerations.Z3_lbool;

/**
 * Implementation of the MCSes algorithm which uses the Z3 API for Java in order
 * to solve the SMT problems
 */
public class Z3MCSesConstraintStrategy extends Z3ConstraintStrategy {

	public Z3MCSesConstraintStrategy() throws Z3Exception {
		super();
	}

	@Override
	public ConstraintStrategyResult runSolvingAlgorithm() {  
        try
        {
			Status satisfiability;
			boolean terminate = false;
            
			int upperBound = 0;
            mcs = new ArrayList<>();
			do {
				// find solutions for given test case
				measurement.start();
				satisfiability = solver.Check();
				runtime.add(measurement.stop());

				if (satisfiability == Status.UNKNOWN) {
					Writer.println("Warning: Solver returned unknown!");
				}

				if (satisfiability == Status.SATISFIABLE) {
					upperBound++;
					while (satisfiability == Status.SATISFIABLE) {
						solver.Push();
						solver.Assert(ctx.MkLt(weightVariable, ctx.MkInt(upperBound)));

						measurement.start();
						satisfiability = solver.Check();
						runtime.add(measurement.stop());

						if (satisfiability == Status.SATISFIABLE) {
							Model model = solver.Model();
							solver.Pop();

							// add constraint in order to avoid that we get the
							// solution again
							info.addAvoid(model, Avoidance.FalseOnly);
							List<Cell> notabnormalAssignment = new ArrayList<>();
							for (BoolExpr notabnormalVar : notabnormals)
                            {
								Expr res = model.Evaluate(notabnormalVar, false);
								if (res.IsBool() && res.BoolValue() == Z3_lbool.Z3_L_FALSE)
									notabnormalAssignment.add(info
											.getNotabnormalVariable(notabnormalVar));
							}
							// if we found a solution with no notabnormals set
							if (notabnormalAssignment.isEmpty()){
								ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
                                result.setRuntimeSolving(runtime);
                                return result;
                            }
							mcs.add(notabnormalAssignment);

						} else
							solver.Pop();
					}
				} else {
					if (satisfiability == Status.UNKNOWN)
						Writer.println("Warning: Solver returned unknown! Terminate loop.");
					terminate = true;
				}
			} while (!terminate
					&& (!ConstraintStrategyConfiguration.useEarlyTermination() || mcs
							.isEmpty()));
			Writer.println(abnormalsToString(mcs));
			ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
            result.setRuntimeSolving(runtime);
            return result;
		} catch (Z3Exception e) {
			throw new RuntimeException(e);
		}
	}
}
