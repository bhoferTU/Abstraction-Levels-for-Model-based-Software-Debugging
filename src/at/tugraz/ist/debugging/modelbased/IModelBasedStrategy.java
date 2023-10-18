package at.tugraz.ist.debugging.modelbased;

import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.IDebugAlgorithm;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.ModelConfig;

/**
 * Interface for all debugging strategies
 * 
 */
public interface IModelBasedStrategy extends
		IDebugAlgorithm<ModelConfig, ModelBasedResult> {

}
