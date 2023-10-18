package at.tugraz.ist.debugging.spreadsheets.algorithms;

import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.ISpectrumAlgorithm;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.SpectrumConfig;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;

public abstract class SpectrumBasedAlgorithm implements ISpectrumAlgorithm {

	protected SpectrumConfig data;
	protected Boolean dynamic = null;
	
	abstract protected IRanking<Coords> runAlgorithm();

	@Override
	public SpectrumBasedResult runAlgorithm(SpectrumConfig input) {
		this.data = input;
		
		if (dynamic != null)
			data.setDynamic(dynamic);
		
		if (data.hasChanged())
		{
			data.init();
		}
		
		SpectrumBasedResult result = new SpectrumBasedResult(data);
		IRanking<Coords> rank = runAlgorithm();
		result.setRanking(rank);
//		result.setSpectrumBasedAlgo(getName(), rank);
		return result;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
}
