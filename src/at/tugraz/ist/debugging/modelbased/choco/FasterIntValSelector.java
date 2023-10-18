package at.tugraz.ist.debugging.modelbased.choco;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.AbstractSearchHeuristic;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class FasterIntValSelector extends AbstractSearchHeuristic implements
		ValSelector<IntDomainVar> {

	protected Set<IntDomainVar> abnormals;
	protected RandomIntValSelector intValSelector = new RandomIntValSelector();

	protected Set<IntDomainVar> otherVars;

	public FasterIntValSelector(Solver solver, IntDomainVar[] abnormals,
			IntDomainVar[] otherVars) {
		super(solver);
		this.abnormals = new HashSet<IntDomainVar>();
		Collections.addAll(this.abnormals, abnormals);
		this.otherVars = new HashSet<IntDomainVar>();
		Collections.addAll(this.otherVars, otherVars);
	}

	@Override
	public int getBestVal(IntDomainVar arg0) {
		if (abnormals.contains(arg0))
			return arg0.getInf();

		return intValSelector.getBestVal(arg0);
	}

}
