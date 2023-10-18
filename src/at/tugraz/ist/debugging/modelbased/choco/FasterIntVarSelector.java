package at.tugraz.ist.debugging.modelbased.choco;

import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class FasterIntVarSelector extends AbstractIntVarSelector {

	// static boolean firstTime = true;

	protected IntegerVariable[] abnormals;
	protected AbstractIntVarSelector internalVarSelector;

	protected IntegerVariable[] otherVars;

	public FasterIntVarSelector(Solver solver,
			IntegerVariable[] abnormalVariables,
			IntegerVariable[] otherIntVarsArray) {
		super(solver);
		this.abnormals = abnormalVariables;
		this.otherVars = otherIntVarsArray;
		this.internalVarSelector = new MinDomain(solver,
				solver.getVar(otherIntVarsArray));
	}

	private boolean isFeasible() {
		CPSolver feasibleSolver = new CPSolver();
		feasibleSolver.clear();
		feasibleSolver.setModel(null);
		feasibleSolver.read(solver.getModel());
		for (int i = 0; i < abnormals.length; i++)
			feasibleSolver.addConstraint(Choco.eq(abnormals[i],
					solver.getVar(abnormals[i]).getVal()));

		boolean solution = feasibleSolver.solve();
		// if (solution && firstTime) {
		// System.out.println("found a solution with: \t"
		// + feasibleSolver.solutionToString());
		// firstTime = false;
		// }

		return solution;
	}

	@Override
	public IntDomainVar selectVar() {
		for (int i = 0; i < abnormals.length; i++)
			if (!solver.getVar(abnormals[i]).isInstantiated())
				return solver.getVar(abnormals[i]);

		// we are only interested in a abnormal assignment
		if (isFeasible())
			return null;
		// System.out.println("no abnormals need other vars");

		return internalVarSelector.selectVar();
	}
}
