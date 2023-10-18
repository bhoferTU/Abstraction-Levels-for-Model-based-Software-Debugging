package at.tugraz.ist.debugging.spreadsheets.configuration.algorithm;

import at.tugraz.ist.debugging.spreadsheets.evaluation.AlgorithmResult;

public interface IDebugAlgorithm<I extends IAlgorithmInput, O extends AlgorithmResult> {

	public O runAlgorithm(I input);
	public String getName();
	
}
