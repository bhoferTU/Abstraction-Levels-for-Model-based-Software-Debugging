package at.tugraz.ist.debugging.modelbased;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.smt.SMTMCSesConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.smt.SMTMCSesUConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.z3api.Z3MCSesConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.z3api.Z3MCSesUConstraintStrategy;

import com.microsoft.z3.Z3Exception;

/**
 * Encapsulates a factory method
 */
public class DebuggingStrategyFactory {
	/**
	 * Returns a debugging strategy given its Enum representation
	 * 
	 * @param strategy
	 *            Enum value corresponding to the desired debugging strategy
	 * @return instance of IDebuggingStrategy which is defined by strategy
	 */
	static public IModelBasedStrategy get(Strategy strategy) {
		switch (strategy.getSolver())
        {
            case Choco:
                return new ChocoConstraintStrategy();
            case Minion:
                return new MinionConstraintStrategy();
            case Z3:
                switch(strategy.getAlgorithm())
                {
                    case MCSes: {
                        if(strategy.getOption() == ESolverAccessOption.API)
                        {
                            try {
                                return new Z3MCSesConstraintStrategy();
                            } catch (Z3Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if(strategy.getOption() == ESolverAccessOption.SMT)
                            return new SMTMCSesConstraintStrategy();
                        break;
                        }
                    case MCSesU: {
                        if(strategy.getOption() == ESolverAccessOption.API)
                        {
                            try {
                                return new Z3MCSesUConstraintStrategy();
                            } catch (Z3Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if(strategy.getOption() == ESolverAccessOption.SMT)
                            return new SMTMCSesUConstraintStrategy();
                        break;
                        }
                    default:
                        throw new NotImplementedException("Strategy " + 
                                strategy.getName() + " is not available");
                }
                break;
                    default:
                        throw new NotImplementedException("Strategy " + 
                                strategy.getName() + " is not available");
		}
        throw new NotImplementedException("Strategy " + strategy.getName()
					+ " is not available");
	}
    
    /*static public IModelBasedStrategy get(EStrategy strategy) {
		switch (strategy) {

		case ChocoBasisConstraintStrategy:
			return new ChocoConstraintStrategy();
		case SMTMCSesBasisConstraintStrategy:
			return new SMTMCSesConstraintStrategy();
		case SMTMCSesUBasisConstraintStrategy:
			return new SMTMCSesUConstraintStrategy();
		case Z3MCSesBasisConstraintStrategy:
			try {
				return new Z3MCSesConstraintStrategy();
			} catch (Z3Exception e) {
				throw new RuntimeException(e);
			}
		case Z3MCSesUBasisConstraintStrategy:
			try {
				return new Z3MCSesUConstraintStrategy();
			} catch (Z3Exception e) {
				throw new RuntimeException(e);
			}
		case MinionConstraintStrategy:
			return new MinionConstraintStrategy();

		default:
			throw new NotImplementedException("Strategy " + strategy.name()
					+ " is not available");
		}
	}*/
}
