package at.tugraz.ist.debugging.modelbased.solver;

import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;

/**
 * Base class for generation information classes which are different for the
 * different subpackages
 * 
 */
public abstract class ConstraintStrategyGenerationInformation {

	/**
	 * Cone which should be used for filtering necessary cells; if the cone is
	 * null then all cells are translated into corresponding CSP/SMT
	 */
	protected Set<Cell> cone;
    
    /**
     * determines whether cones are used or not
     */
    protected boolean useCones;

	protected EModelGranularity modelGranularity = EModelGranularity.Value;

	public ConstraintStrategyGenerationInformation(Set<Cell> cone, boolean useCones,
			EModelGranularity modelGranularity) {
		this.cone = cone;
        this.useCones = useCones;
		this.modelGranularity = modelGranularity;
	}

	public abstract int getAbnormalsCount();

	public abstract int getCellVariablesCount();

	public EModelGranularity getModelGranularity() {
		return modelGranularity;
	}

	public void setModelGranularity(EModelGranularity modelGranularity) {
		this.modelGranularity = modelGranularity;
	}
}
