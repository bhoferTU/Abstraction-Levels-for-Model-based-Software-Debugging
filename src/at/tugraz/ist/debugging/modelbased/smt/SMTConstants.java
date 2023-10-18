package at.tugraz.ist.debugging.modelbased.smt;

/**
 * Helper class for SMT generator and parser
 * 
 */
public class SMTConstants {
	/**
	 * Supported SMT configuration options
	 */
	public enum Option {
		ProduceModels, ProduceUnsatCores
	}

	/**
	 * Satisfiability
	 */
	public enum Satisfiability {
		Sat, Unknown, Unsat
	}

	/**
	 * SMT string corresponding to boolean value false
	 */
	public static final String SMT_FALSE = "false";

	/**
	 * SMT string corresponding to satisfiability SAT
	 */
	private static final String SMT_SAT = "sat";

	/**
	 * SMT string corresponding to boolean value true
	 */
	public static final String SMT_TRUE = "true";

	/**
	 * SMT string corresponding to satisfiability UNKNOWN
	 */
	private static final String SMT_UNKNOWN = "unknown";

	/**
	 * SMT string corresponding to satisfiability UNSAT
	 */
	private static final String SMT_UNSAT = "unsat";

	/**
	 * Converts an SMT string to a boolean value
	 * 
	 * @param value
	 *            SMT string value
	 * @return Corresponding boolean value
	 */
	public static boolean stringToBool(String value) {
		if (value.equals(SMT_TRUE))
			return true;
		else if (value.equals(SMT_FALSE))
			return false;

		throw new RuntimeException(String.format(
				"SMT: Cannot convert string '%s' to boolean value", value));
	}

	/**
	 * Converts an SMT string to a satisfiability value
	 * 
	 * @param untrimmedValue
	 *            SMT string value
	 * @return Satisfiability
	 */
	public static Satisfiability stringToSatisfiablity(String untrimmedValue) {
		String value = untrimmedValue.trim();
		if (value.equals(SMT_SAT))
			return Satisfiability.Sat;
		else if (value.equals(SMT_UNSAT))
			return Satisfiability.Unsat;
		else if (value.equals(SMT_UNKNOWN))
			return Satisfiability.Unknown;

		throw new RuntimeException(String.format(
				"SMT: Cannot convert string '%s' to boolean value", value));
	}
}
