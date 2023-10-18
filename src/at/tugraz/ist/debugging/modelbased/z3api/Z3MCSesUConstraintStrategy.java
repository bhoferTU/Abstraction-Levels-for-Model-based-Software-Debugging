package at.tugraz.ist.debugging.modelbased.z3api;

import java.util.ArrayList;
import java.util.Arrays;
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

/**
 * Implementation of the MCSes-U algorithm which uses the Z3 API for Java in
 * order to solve the SMT problems. The MCSes-U algorithm uses the unsat core
 * information for the exclusion of notabnormals from the MCS and thus results 
 * in a performance gain.
 */
public class Z3MCSesUConstraintStrategy extends Z3ConstraintStrategy {

	public Z3MCSesUConstraintStrategy() throws Z3Exception {
		super();
	}

	@Override
	public ConstraintStrategyResult runSolvingAlgorithm() {
   		try {
			Status satisfiability;
			int upperBound = 1;
			mcs = new ArrayList<>();
            
            //System.out.println(solver.toString());
			measurement.start();
			satisfiability = solver.Check(notabnormals);
			runtime.add(measurement.stop());

			// retrieve core
			if (satisfiability != Status.UNSATISFIABLE) {
				// there is no problem
				Writer.println("There is no problem");
				ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
                result.setRuntimeSolving(runtime);
                return result;
			}

			Expr[] core = solver.UnsatCore();
			List<Expr> coreList = new ArrayList<>();
			coreList.addAll(Arrays.asList(core));

            //System.out.println(solver.toString());
			measurement.start();
			satisfiability = solver.Check();
			runtime.add(measurement.stop());

			while (satisfiability == Status.SATISFIABLE) {

				// STACK FRAME 3 (AllSAT)
				// instrument all constraints whose notabnormals are in core
				// and add at most property
				Model model = null;
				do {
					if (model != null)
						info.addAvoid(model, Avoidance.FalseOnly);
					solver.Push();
					solver.Assert(ctx.MkLe(weightVariable, ctx.MkInt(upperBound)));
					for (BoolExpr notabnormal : info.getNotabnormalVariables()) {
						// avoid the relaxation of constraints which are
						// not included in the core
						if (!coreList.contains(notabnormal)) {
							solver.Assert(notabnormal);
						}
					}

					measurement.start();
					satisfiability = solver.Check();
					runtime.add(measurement.stop());
                    
					if (satisfiability == Status.SATISFIABLE) {
						List<Cell> notabnormalAssignment = new ArrayList<>();
						model = solver.Model();
						for (BoolExpr notabnormalVar : info.getFalseVariables(model))
                        {
							notabnormalAssignment.add(
                                info.getNotabnormalVariable(notabnormalVar));
						}
						mcs.add(notabnormalAssignment);
					}
					solver.Pop();
				} while (satisfiability == Status.SATISFIABLE);

				// update core
				BoolExpr[] NonConflictingNotabnormals = new BoolExpr[info
						.getNotabnormalVariables().length - coreList.size()];
				int i = 0;
				for (BoolExpr notabnormal : notabnormals) {
					if (!coreList.contains(notabnormal)) {
						NonConflictingNotabnormals[i] = notabnormal;
						i++;
					}
				}

				solver.Push();
				solver.Assert(ctx.MkLe(weightVariable, ctx.MkInt(upperBound)));

				measurement.start();
				satisfiability = solver.Check(NonConflictingNotabnormals);
				runtime.add(measurement.stop());

				if (satisfiability == Status.UNSATISFIABLE) {
					// we have a new core
					Expr[] newCore = solver.UnsatCore();
					int oldCoreSize = coreList.size();
					coreList.addAll(Arrays.asList(newCore));
					Writer.println("Core size: " + coreList.size());
					if (oldCoreSize == coreList.size()) {
						if (ConstraintStrategyConfiguration.useEarlyTermination()
								&& !mcs.isEmpty()) {
							ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
                            result.setRuntimeSolving(runtime);
                            return result;
						}
						upperBound++;
					}
				} else {
					if (ConstraintStrategyConfiguration.useEarlyTermination() && !mcs.isEmpty()) {
						Writer.println(abnormalsToString(mcs));
						ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
                        result.setRuntimeSolving(runtime);
                        return result;
					}
					upperBound++;
				}

				solver.Pop();

				measurement.start();
				satisfiability = solver.Check();
				runtime.add(measurement.stop());
			}

			// REENTER STACK FRAME 1
			// pop assertions of current run
			solver.Pop();

			// REENTER STACK FRAME 0
			// pop assertions for input cells of current test case
			solver.Pop();

			Writer.println(abnormalsToString(mcs));
			ConstraintStrategyResult result = new ConstraintStrategyResult(mcs, info); 
            result.setRuntimeSolving(runtime);
            return result;
		} catch (Z3Exception e) {
			throw new RuntimeException(e);
		}
	}
}
