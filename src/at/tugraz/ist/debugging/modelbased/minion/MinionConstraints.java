package at.tugraz.ist.debugging.modelbased.minion;

import java.util.HashSet;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;

/**
 * Collection of Minion constraints
 * 
 * @author bhofer
 * 
 */
/**
 * 
 * @author inica for TABLES minMaxFunction, ifFunction, relationalOPFunction
 *
 */
public class MinionConstraints {

	/**
	 * Creates a constraint which ensures that result=|reference|
	 * 
	 * @param reference
	 *            Identifier of an Integer variable
	 * @param result
	 *            Identifier of another Integer Variable
	 * @return Constraint which ensures that result=|reference|
	 */
	
	
	public static MinionExpressionConstraints getABSOLUTConstraint(String reference, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("abs(" + result + "," + reference + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraint which ensures that op1 + op1 is equal to result
	 * 
	 * @param op1
	 *            Identifier for the variable used as first operand
	 * @param op2
	 *            Identifier for the variable used as second operand
	 * @param result
	 *            Identifier for the variable which should be equal to op1 + op2
	 * @return Constraint which ensures that op1 + op1 is equal to result
	 */
	public static MinionExpressionConstraints getADDConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("sumleq([" + op1 + "," + op2 + "]," + result + ")");
		constraints.add("sumgeq([" + op1 + "," + op2 + "]," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraint which ensures that result is true if all other
	 * variables in ops are true.
	 * 
	 * @param ops
	 *            Set of Identifiers for Boolean variables
	 * @param result
	 *            Identifier for a Boolean variable
	 * @return Constraint which ensured that ops[0] AND ops[1] AND ... ops[n] is
	 *         equal to the value of result
	 */
	public static MinionExpressionConstraints getANDConstraint(Set<String> ops, String result) {
		Set<String> constraints = new HashSet<String>();
		String constraint = "min([";
		for (String op : ops) {
			constraint += op + ",";

		}
		constraint = constraint.substring(0, constraint.length() - 1);
		constraint += "]," + result + ")";
		constraints.add(constraint);
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates constraints which ensure that the average value of the
	 * referencedCells is stored in result
	 * 
	 * @param referencedCells
	 *            Set of identifier
	 * @param intermediate
	 *            Identifier representing a auxiliary Integer variable
	 * @param result
	 *            Identifier representing an Integer variable
	 * @return Constraint which ensures that results contains the average value
	 *         of the referencedCells
	 */
	public static MinionExpressionConstraints getAVGConstraints(Set<String> referencedCells, String intermediate,
			String result) {
		MinionExpressionConstraints sumConstraints = getSUMConstraint(referencedCells, intermediate);
		Integer divident = referencedCells.size();
		MinionExpressionConstraints divConstraints = getDIVConstraint(intermediate, divident.toString(), result);
		divConstraints.addConstraints(sumConstraints);
		return divConstraints;
	}

	/**
	 * Creates a constraints which ensures that result is equal to the value
	 * 
	 * @param value
	 *            Value (Boolean/Integer)
	 * @param result
	 *            Identifier for a variable (Boolean/Integer)
	 * @param resultType
	 *            Domain of the variable (Boolean/Integer)
	 * @return Constraint which ensures that result is equal to the value
	 */
	public static MinionExpressionConstraints getConstantDefinition(String value, String result, Domain resultType) {
		Set<String> constraintSet = new HashSet<String>();
		Set<String> constraintSetTC = new HashSet<String>();
		constraintSetTC.add("eq(" + result + "," + value + ")");
		return new MinionExpressionConstraints(constraintSet, constraintSetTC, result, resultType, true);
	}
	
	


	/**
	 * Creates a constraint which ensures that op1 / op2 is equal to the value
	 * of result
	 * 
	 * @param op1
	 *            Identifier for an Integer variable
	 * @param op2
	 *            Identifier for an Integer variable
	 * @param result
	 *            Identifier for an Integer variable
	 * @return Constraint which ensures that op1 divided by op2 is equal to
	 *         result
	 */
	public static MinionExpressionConstraints getDIVConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		// code modification 06.2022 - quick fix for the division in IF branch
			constraints.add("eq("+ op2 + ",0), "+"div(" + op1 + "," + op2 + "," + result + ")");
			//constraints.add("div(" + op1 + "," + op2 + "," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}
/**
 * TABLES for the Comparison Model 
 */
 
	public static MinionExpressionConstraints getPLUSTableConstraints(int abnormalIndex, String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], plusFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.INT3);
	}

	public static MinionExpressionConstraints getMULTTableConstraints(int abnormalIndex, String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], multFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.INT3);
	}
	public static MinionExpressionConstraints getMINUSTableConstraints(int abnormalIndex, String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], minusFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.INT3);
	}
	
	public static MinionExpressionConstraints getDIVTableConstraints(int abnormalIndex, String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], divFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.INT3);
	}
	public static MinionExpressionConstraints getEQUALTableConstraints(int abnormalIndex, String op, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op + "," + result + "], equalityFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.INT3);
	}
	/**
	 * Table Constraints for REL_OP, MIN_MAX, IF
	 * @author inica
	 * 
	 */
	
	
	public static MinionExpressionConstraints getRelOperatorsTableConstraints(int abnormalIndex, String op1, String op2, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], relationalOPFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}
	
	public static MinionExpressionConstraints getMinMaxTableConstraints(int abnormalIndex, String op1, String op2, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], minMaxFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.INT3);
	}
	
	public static MinionExpressionConstraints getIFTableConstraints(int abnormalIndex, String cond, String op1, String op2, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"],"+ cond + "," + op1 + "," + op2 + "," + result + "], ifFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.INT3);
	}

	/**
	 * Creates a constraint which ensures that the values of op1 is equal to
	 * result.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param result
	 *            Identifier for another variable
	 * @param domain
	 *            Domain (int/bool) for the variables
	 * @return Constraint which ensures that values of op1 and result are equal.
	 */
	public static MinionExpressionConstraints getEQUALConstraint(String op1, String result, Domain domain) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("eq(" + op1 + "," + result + ")");
		return new MinionExpressionConstraints(constraints, result, domain);
	}

	/**
	 * Creates a constraint which ensures that the value of op1 is equal to the
	 * value of op2 if result is true. If result is false, the value of op1 and
	 * op2 can be different, but must not be different.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param op2
	 *            Identifier for another variable
	 * @param result
	 *            Identifier for a Boolean variable. If result is true op1 is
	 *            equal to op2.
	 * @return Constraint which ensures that the value of the variables op1 and
	 *         op2 are equal when result is true.
	 */
	public static MinionExpressionConstraints getEqualImplyConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reifyimply(eq(" + op1 + "," + op2 + ")," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that result is set to true if and only
	 * if the value of op1 is equal to the value of op2. If result is false, the
	 * value of op1 must differ from the value of op2. This constraint is used
	 * to model op1==op2 that is often used in conditionals.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param op2
	 *            Identifier for another variable
	 * @param result
	 *            Identifier for a Boolean variable that is true if op1 is equal
	 *            to op2
	 * @return Constraint which ensures that the value of the variable result is
	 *         true if and only if op1 is equal to op2.
	 */
	public static MinionExpressionConstraints getEqualReifyConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reify(eq(" + op1 + "," + op2 + ")," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a {@link MinionExpressionConstraints} expression that does not
	 * contain any constraints, but the value as variable name. This method is
	 * needed when using constants within formulas.
	 * 
	 * @param value
	 *            Value of the constant
	 * @param resultType
	 *            Domain (Boolean/Integer) of the value
	 * @return an empty constraint
	 */
	public static MinionExpressionConstraints getFakeConstantDefintion(String value, Domain resultType) {
		return new MinionExpressionConstraints(new HashSet<String>(), new HashSet<String>(), value, resultType, true);
	}

	/**
	 * Creates a constraint which ensures that result is set to true if and only
	 * if op1>op2. If op1<=op2, results is set to false This constraint is used
	 * to model op1<op2 that is often used in conditionals.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param op2
	 *            Identifier for another variable
	 * @param result
	 *            Identifier for a Boolean variable that is true if op1>op2
	 * @return Constraint which ensures that the value of the variable result is
	 *         true if and only if op1>op2.
	 */
	public static MinionExpressionConstraints getGREATERConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reify(ineq(" + op2 + "," + op1 + ",-1)," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that result is set to true if and only
	 * if op1>=op2. If op1<op2, results is set to false This constraint is used
	 * to model op1<op2 that is often used in conditionals.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param op2
	 *            Identifier for another variable
	 * @param result
	 *            Identifier for a Boolean variable that is true if op1>=op2
	 * @return Constraint which ensures that the value of the variable result is
	 *         true if and only if op1>=op2.
	 */
	public static MinionExpressionConstraints getGREATEREQUALConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reify(ineq(" + op2 + "," + op1 + ",0)," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which models an IF-THEN-ELSE construct.
	 * 
	 * @param cond
	 *            Identifier representing the result of the condition (Boolean)
	 * @param notCond
	 *            Identifier representing the negated result of the condition
	 *            (Boolean)
	 * @param then
	 *            Identifier representing the result of the THEN branch
	 *            (Boolean/Integer)
	 * @param elsePart
	 *            Identifier representing the result of the ELSE branch
	 *            (Boolean/Integer)
	 * @param result
	 *            Identifier representing the result variable (Boolean/Integer)
	 * @param resultType
	 *            Domain (Boolean/Integer) of the result variable
	 * @return Constraint which ensures that result is equal to then when cond
	 *         is true, otherwise result is equal to elsePart
	 */
	public static MinionExpressionConstraints getIFConstraints(String cond, String notCond, String then,
			String elsePart, String result, Domain resultType) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reifyimply(eq(" + result + "," + then + ")," + cond + ")");
		constraints.add("reifyimply(eq(" + result + "," + elsePart + ")," + notCond + ")");
		constraints.add("diseq(" + cond + "," + notCond + ")");
		return new MinionExpressionConstraints(constraints, result, resultType);
	}

	/**
	 * Creates a constraint which ensures that result is set to true if and only
	 * if op1<op2. If op1>=op2, results is set to false This constraint is used
	 * to model op1<op2 that is often used in conditionals.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param op2
	 *            Identifier for another variable
	 * @param result
	 *            Identifier for a Boolean variable that is true if op1<op2
	 * @return Constraint which ensures that the value of the variable result is
	 *         true if and only if op1<op2.
	 */
	public static MinionExpressionConstraints getLESSConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reify(ineq(" + op1 + "," + op2 + ",-1)," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that result is set to true if and only
	 * if op1<=op2. If op1>op2, results is set to false This constraint is used
	 * to model op1<op2 that is often used in conditionals.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param op2
	 *            Identifier for another variable
	 * @param result
	 *            Identifier for a Boolean variable that is true if op1<=op2
	 * @return Constraint which ensures that the value of the variable result is
	 *         true if and only if op1<=op2.
	 */
	public static MinionExpressionConstraints getLESSEQUALConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reify(ineq(" + op1 + "," + op2 + ",0)," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that result is equal to the largest
	 * value of the referencedCells
	 * 
	 * @param referencedCells
	 *            Set of identifier for Integer variables
	 * @param result
	 *            Identifier for an Integer variable
	 * @return Constraint which ensures that result is equal to the largest
	 *         value of the referencedCells
	 */
	public static MinionExpressionConstraints getMAXConstraint(Set<String> referencedCells, String result) {
		Set<String> constraints = new HashSet<String>();
		String elementVector = "";

		for (String cell : referencedCells) {
			elementVector += cell + ",";
		}

		elementVector = elementVector.substring(0, elementVector.length() - 1);
		constraints.add("max([" + elementVector + "]," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraints that ensures that result is equal to the minimum
	 * value of the referencedCells
	 * 
	 * @param referencedCells
	 *            Set of identifier for Integer variables
	 * @param result
	 *            Identifier for an Integer variable
	 * @return Constraint that ensures that result is equal to the minimum value
	 *         of the referencedCells
	 */
	public static MinionExpressionConstraints getMINConstraint(Set<String> referencedCells, String result) {
		Set<String> constraints = new HashSet<String>();
		String elementVector = "";

		for (String cell : referencedCells) {
			elementVector += cell + ",";
		}

		elementVector = elementVector.substring(0, elementVector.length() - 1);
		constraints.add("min([" + elementVector + "]," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraint which ensures that minuend - subtrahend is equal to
	 * result
	 * 
	 * @param minuend
	 *            Identifier for the variable used as minuend
	 * @param subtrahend
	 *            Identifier for the variable used as subtrahend
	 * @param result
	 *            Identifier for the variable which should be equal to minuend -
	 *            subtrahend
	 * @return Constraint which ensures that minuend - subtrahend is equal to
	 *         result
	 */
	public static MinionExpressionConstraints getMINUSConstraint(String minuend, String subtrahend, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("weightedsumgeq([1,-1],[" + minuend + "," + subtrahend + "]," + result + ")");
		constraints.add("weightedsumleq([1,-1],[" + minuend + "," + subtrahend + "]," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraint which ensures dividend % devisior is equal to result
	 * 
	 * @param dividend
	 *            Identifier of an Integer variable representing the dividend
	 * @param devisor
	 *            Identifier of an Integer variable representing the devisor
	 * @param result
	 *            Identifier of an Integer variable representing the remainder
	 *            of the division
	 * @return Constraint which ensures dividend % devisior is equal to result
	 */
	public static MinionExpressionConstraints getMODConstraint(String dividend, String devisor, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("modulo(" + dividend + "," + devisor + "," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that op1 * op2 is equal to the value
	 * of result
	 * 
	 * @param op1
	 *            Identifier for an Integer variable
	 * @param op2
	 *            Identifier for an Integer variable
	 * @param result
	 *            Identifier for an Integer variable
	 * @return Constraint which ensures that op1 * op2 is equl to the value of
	 *         result
	 */
	public static MinionExpressionConstraints getMULTConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("product(" + op1 + "," + op2 + "," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraint which ensures referencedCell!=result
	 * 
	 * @param referencedCell
	 *            Identifier of the Boolean variable that should be negated
	 * @param result
	 *            Identifier of the Boolean variable that represents the negated
	 *            referencedCell
	 * @return Constraint which ensures referencedCell!=result
	 */
	public static MinionExpressionConstraints getNOTConstraint(String referencedCell, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("diseq(" + referencedCell + "," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that result is set to true if and only
	 * if the value of op1 is disequal to the value of op2. If result is false,
	 * the value of op1 must equal to the value of op2. This constraint is used
	 * to model op1!=op2 that is often used in conditionals.
	 * 
	 * @param op1
	 *            Identifier for a variable
	 * @param op2
	 *            Identifier for another variable
	 * @param result
	 *            Identifier for a Boolean variable that is true if op1 differs
	 *            from op2
	 * @return Constraint which ensures that the value of the variable result is
	 *         true if and only if the value op1 differs from that of op2
	 */
	public static MinionExpressionConstraints getNotEqualRefeiyConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("reify(diseq(" + op1 + "," + op2 + ")," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that result is true if at least one
	 * variable in ops is true.
	 * 
	 * @param ops
	 *            Set of Identifiers for Boolean variables
	 * @param result
	 *            Identifier for a Boolean variable
	 * @return Constraint which ensures that ops[0] OR ops[1] OR ... ops[n] is
	 *         equal to the value of result
	 */
	public static MinionExpressionConstraints getORConstraint(Set<String> ops, String result) {
		Set<String> constraints = new HashSet<String>();
		String constraint = "max([";
		for (String op : ops) {
			constraint += op + ",";

		}
		constraint = constraint.substring(0, constraint.length() - 1);
		constraint += "]," + result + ")";
		constraints.add(constraint);
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}

	/**
	 * Creates a constraint which ensures that op1^op2 is equal to result
	 * 
	 * @param op1
	 *            Identifier for an Integer variable
	 * @param op2
	 *            Identifier for an Integer variable
	 * @param result
	 *            Identifier for an Integer variable
	 * @return Constraint which ensures that op1^op2 is equal to result
	 */
	public static MinionExpressionConstraints getPOWConstraint(String op1, String op2, String result) {
		Set<String> constraints = new HashSet<String>();
		constraints.add("pow(" + op1 + "," + op2 + "," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraint which ensures that the sum of the referencedCells is
	 * equal to result
	 * 
	 * @param referencedCells
	 *            Set of Identifiers for variables
	 * @param result
	 *            Identifier for an Integer variable
	 * @return Constraint which ensures that the sum of referencedCells is equal
	 *         to result
	 */
	public static MinionExpressionConstraints getSUMConstraint(Set<String> referencedCells, String result) {
		Set<String> constraints = new HashSet<String>();
		String weightVector = "";
		String elementVector = "";

		for (String cell : referencedCells) {
			weightVector += "1,";
			elementVector += cell + ",";
		}
		weightVector = weightVector.substring(0, weightVector.length() - 1);
		elementVector = elementVector.substring(0, elementVector.length() - 1);

		constraints.add("weightedsumgeq([" + weightVector + "],[" + elementVector + "]," + result + ")");
		constraints.add("weightedsumleq([" + weightVector + "],[" + elementVector + "]," + result + ")");
		return new MinionExpressionConstraints(constraints, result, Domain.INTEGER);
	}

	/**
	 * Creates a constraint which ensures AB(abnormalIndex) OR constraint.
	 * 
	 * @param constraints
	 * @param abnormalIndex
	 * @param cellName
	 * @return Constraint which ensures AB(abnormalIndex) OR constraint
	 */
	public static MinionExpressionConstraints surroundConstraintsWithAbnormalClause(
			MinionExpressionConstraints constraints, Integer abnormalIndex, String cellName) {
		Set<String> constraintSet = new HashSet<String>();
		for (String constraint : constraints.getConstraints()) {
			String newConstraint = "watched-or({element(ab," + abnormalIndex + ",1), " + constraint + "})";
			constraintSet.add(newConstraint);
		}
		return new MinionExpressionConstraints(constraintSet, constraints.getConstraintsTC(), cellName,
				constraints.getResultType());
	}
	

	
/**
 * new tables for QDM - assumption: real values
 * @return
 */
	public static String getComparisonModelTables() {
		return ("\n**TUPLELIST**\n"
	             +"plusFunction  40 4\n"
				 + "0 1 1 1\n"
				 + "0 2 1 2\n"
				 + "0 1 2 2\n"
				 + "0 2 2 2\n"
				 + "0 0 1 0\n"
				 + "0 1 0 0\n"
				 + "0 0 0 0\n"
				 + "0 0 2 0\n"
				 + "0 0 2 1\n"
				 + "0 0 2 2\n"
				 + "0 2 0 0\n"
				 + "0 2 0 1\n"
				 + "0 2 0 2\n"
				 +"1 0 0 0\n1 0 0 1\n1 0 0 2\n1 1 0 0\n1 1 0 1\n"
					+ "1 1 0 2\n1 2 0 0\n1 2 0 1\n1 2 0 2\n1 0 1 0\n1 0 1 1\n1 0 1 2\n1 0 2 0\n1 0 2 1\n1 0 2 2\n1 1 1 0\n1 1 1 1\n1 1 1 2\n1 2 1 0\n1 2 1 1\n1 2 1 2\n1 2 2 0\n1 2 2 1\n1 2 2 2\n1 1 2 0\n1 1 2 1\n1 1 2 2\n\n"
				 +"multFunction 52 4\n"
				 + "0 0 0 0\n"
				 + "0 0 0 1\n"
				 + "0 0 0 2\n"
				 + "0 0 1 0\n"
				 + "0 0 1 1\n"
				 + "0 0 1 2\n"
				 + "0 0 2 0\n"
				 + "0 0 2 1\n"
				 + "0 0 2 2\n"
				 + "0 1 0 0\n"
				 + "0 1 0 1\n"
				 + "0 1 0 2\n"
				 + "0 1 1 1\n"
				 + "0 1 2 2\n"
				 + "0 1 2 0\n"
				 + "0 1 2 1\n"
				 + "0 2 0 0\n"
				 + "0 2 0 1\n"
				 + "0 2 0 2\n"
				 + "0 2 1 2\n"
				 + "0 2 1 0\n"
				 + "0 2 1 1\n"
				 + "0 2 2 2\n"
				 + "0 2 2 0\n"
				 + "0 2 2 1\n"
	            +"1 0 0 0\n1 0 0 1\n1 0 0 2\n1 1 0 0\n1 1 0 1\n"
				+ "1 1 0 2\n1 2 0 0\n1 2 0 1\n1 2 0 2\n1 0 1 0\n1 0 1 1\n1 0 1 2\n1 0 2 0\n1 0 2 1\n1 0 2 2\n1 1 1 0\n1 1 1 1\n1 1 1 2\n1 2 1 0\n1 2 1 1\n1 2 1 2\n1 2 2 0\n1 2 2 1\n1 2 2 2\n1 1 2 0\n1 1 2 1\n1 1 2 2\n\n"
				+ "minusFunction 40 4\n"
				 + "0 0 0 0\n"
				 + "0 0 0 1\n"
				 + "0 0 0 2\n"
				 + "0 0 1 0\n"
				 + "0 0 2 0\n"
				 + "0 1 0 2\n"
				 + "0 1 1 1\n"
				 + "0 1 2 0\n"
				 + "0 2 0 2\n"
				 + "0 2 1 2\n"
				 + "0 2 2 2\n"
				 + "0 2 2 0\n"
				 + "0 2 2 1\n"
	            +"1 0 0 0\n1 0 0 1\n1 0 0 2\n1 1 0 0\n1 1 0 1\n"
				+ "1 1 0 2\n1 2 0 0\n1 2 0 1\n1 2 0 2\n1 0 1 0\n1 0 1 1\n1 0 1 2\n1 0 2 0\n1 0 2 1\n1 0 2 2\n1 1 1 0\n1 1 1 1\n1 1 1 2\n1 2 1 0\n1 2 1 1\n1 2 1 2\n1 2 2 0\n1 2 2 1\n1 2 2 2\n1 1 2 0\n1 1 2 1\n1 1 2 2\n\n"
				+ "divFunction 50 4\n"
				 + "0 0 0 0\n"
				 + "0 0 0 1\n"
				 + "0 0 0 2\n"
				 + "0 0 1 0\n"
				 + "0 0 1 2\n"
				 + "0 0 2 0\n"
				 + "0 0 2 1\n"
				 + "0 0 2 2\n"
				 + "0 1 0 0\n"
				 + "0 1 0 1\n"
				 + "0 1 0 2\n"
				 + "0 1 1 1\n"
				 + "0 1 2 2\n"
				 + "0 1 2 0\n"
				 + "0 1 2 1\n"
				 + "0 2 0 0\n"
				 + "0 2 0 1\n"
				 + "0 2 0 2\n"
				 + "0 2 1 2\n"
				 + "0 2 1 0\n"
				 + "0 2 2 2\n"
				 + "0 2 2 0\n"
				 + "0 2 2 1\n"
	            +"1 0 0 0\n1 0 0 1\n1 0 0 2\n1 1 0 0\n1 1 0 1\n"
				+ "1 1 0 2\n1 2 0 0\n1 2 0 1\n1 2 0 2\n1 0 1 0\n1 0 1 1\n1 0 1 2\n1 0 2 0\n1 0 2 1\n1 0 2 2\n1 1 1 0\n1 1 1 1\n1 1 1 2\n1 2 1 0\n1 2 1 1\n1 2 1 2\n1 2 2 0\n1 2 2 1\n1 2 2 2\n1 1 2 0\n1 1 2 1\n1 1 2 2\n\n"
				+ "equalityFunction 12 3 \n0 0 0\n0 1 1\n0 2 2\n1 0 0\n1 0 1\n1 0 2\n1 1 0\n1 1 1\n1 1 2\n1 2 0\n1 2 1\n1 2 2\n\n"
				+ "relationalOPFunction  35 4 \n"
				+ "0 1 1 1 \n"
				+ "0 0 0 0 \n"
				+ "0 0 0 1 \n"
				+ "0 0 1 0 \n"
				+ "0 0 1 1 \n"
				+ "0 1 0 0 \n"
				+ "0 1 0 1 \n"
				+ "0 0 2 0 \n"
				+ "0 0 2 1 \n"
				+ "0 2 0 0 \n"
				+ "0 2 0 1 \n"
				+ "0 1 2 0 \n"
				+ "0 1 2 1 \n"
				+ "0 2 1 0 \n"
				+ "0 2 1 1 \n"
				+ "0 2 2 0 \n"
				+ "0 2 2 1 \n"
				+ "1 1 1 0 \n"
				+ "1 1 1 1 \n"
				+ "1 0 0 0 \n"
				+ "1 0 0 1 \n"
				+ "1 0 1 0 \n"
				+ "1 0 1 1 \n"
				+ "1 1 0 0 \n"
				+ "1 1 0 1 \n"
				+ "1 0 2 0 \n"
				+ "1 0 2 1 \n"
				+ "1 2 0 0 \n"
				+ "1 2 0 1 \n"
				+ "1 1 2 0 \n"
				+ "1 1 2 1 \n"
				+ "1 2 1 0 \n"
				+ "1 2 1 1 \n"
				+ "1 2 2 0 \n"
				+ "1 2 2 1 \n\n"
				+"ifFunction  92 5 \n"
				+ "0 1 1 1 1 \n"
				+ "0 1 2 1 2 \n"
				+ "0 1 2 1 1 \n"
				+ "0 1 0 1 0 \n"
				+ "0 1 0 1 1 \n"
				+ "0 1 1 2 2 \n"
				+ "0 1 1 2 1 \n"
				+ "0 1 1 0 0 \n"
				+ "0 1 1 0 1 \n"
				+ "0 1 2 2 2 \n"
				+ "0 1 0 0 0 \n"
				+ "0 0 0 0 0 \n"
				+ "0 0 0 0 1 \n"
				+ "0 0 0 0 2 \n"
				+ "0 0 0 1 0 \n"
				+ "0 0 0 1 1 \n"
				+ "0 0 0 1 2 \n"
				+ "0 0 0 2 0 \n"
				+ "0 0 0 2 1 \n"
				+ "0 0 0 2 2 \n"
				+ "0 0 1 0 0 \n"
				+ "0 0 1 0 1 \n"
				+ "0 0 1 0 2 \n"
				+ "0 0 1 1 0 \n"
				+ "0 0 1 1 1 \n"
				+ "0 0 1 1 2 \n"
				+ "0 0 1 2 0 \n"
				+ "0 0 1 2 1 \n"
				+ "0 0 1 2 2 \n"
				+ "0 0 2 0 0 \n"
				+ "0 0 2 0 1 \n"
				+ "0 0 2 0 2 \n"
				+ "0 0 2 1 0 \n"
				+ "0 0 2 1 1 \n"
				+ "0 0 2 1 2 \n"
				+ "0 0 2 2 0 \n"
				+ "0 0 2 2 1 \n"
				+ "0 0 2 2 2 \n"
				+ "1 0 0 0 0 \n"
				+ "1 0 0 0 1 \n"
				+ "1 0 0 0 2 \n"
				+ "1 0 0 1 0 \n"
				+ "1 0 0 1 1 \n"
				+ "1 0 0 1 2 \n"
				+ "1 0 0 2 0 \n"
				+ "1 0 0 2 1 \n"
				+ "1 0 0 2 2 \n"
				+ "1 0 1 0 0 \n"
				+ "1 0 1 0 1 \n"
				+ "1 0 1 0 2 \n"
				+ "1 0 1 1 0 \n"
				+ "1 0 1 1 1 \n"
				+ "1 0 1 1 2 \n"
				+ "1 0 1 2 0 \n"
				+ "1 0 1 2 1 \n"
				+ "1 0 1 2 2 \n"
				+ "1 0 2 0 0 \n"
				+ "1 0 2 0 1 \n"
				+ "1 0 2 0 2 \n"
				+ "1 0 2 1 0 \n"
				+ "1 0 2 1 1 \n"
				+ "1 0 2 1 2 \n"
				+ "1 0 2 2 0 \n"
				+ "1 0 2 2 1 \n"
				+ "1 0 2 2 2 \n"
				+ "1 1 0 0 0 \n"
				+ "1 1 0 0 1 \n"
				+ "1 1 0 0 2 \n"
				+ "1 1 0 1 0 \n"
				+ "1 1 0 1 1 \n"
				+ "1 1 0 1 2 \n"
				+ "1 1 0 2 0 \n"
				+ "1 1 0 2 1 \n"
				+ "1 1 0 2 2 \n"
				+ "1 1 1 0 0 \n"
				+ "1 1 1 0 1 \n"
				+ "1 1 1 0 2 \n"
				+ "1 1 1 1 0 \n"
				+ "1 1 1 1 1 \n"
				+ "1 1 1 1 2 \n"
				+ "1 1 1 2 0 \n"
				+ "1 1 1 2 1 \n"
				+ "1 1 1 2 2 \n"
				+ "1 1 2 0 0 \n"
				+ "1 1 2 0 1 \n"
				+ "1 1 2 0 2 \n"
				+ "1 1 2 1 0 \n"
				+ "1 1 2 1 1 \n"
				+ "1 1 2 1 2 \n"
				+ "1 1 2 2 0 \n"
				+ "1 1 2 2 1 \n"
				+ "1 1 2 2 2 \n\n"
				+"minMaxFunction 44 4\n"
				+ "0 1 1 1\n"
				+ "0 2 1 2\n"
				+ "0 2 1 1\n"
				+ "0 1 2 2\n"
				+ "0 1 2 1\n"
				+ "0 2 2 2\n"
				+ "0 0 1 1\n"
				+ "0 0 1 0\n"
				+ "0 1 0 1\n"
				+ "0 1 0 0\n"
				+ "0 0 0 0\n"
				+ "0 0 2 2\n"
				+ "0 0 2 0\n"
				+ "0 0 2 1\n"
				+ "0 2 0 2\n"
				+ "0 2 0 0\n"
				+ "0 2 0 1\n"
				+ "1 0 0 0\n"
				+ "1 0 0 1\n"
				+ "1 0 0 2\n"
				+ "1 1 0 0\n"
				+ "1 1 0 1\n"
				+ "1 1 0 2\n"
				+ "1 2 0 0\n"
				+ "1 2 0 1\n"
				+ "1 2 0 2\n"
				+ "1 0 1 0\n"
				+ "1 0 1 1\n"
				+ "1 0 1 2\n"
				+ "1 0 2 0\n"
				+ "1 0 2 1\n"
				+ "1 0 2 2\n"
				+ "1 1 1 0\n"
				+ "1 1 1 1\n"
				+ "1 1 1 2\n"
				+ "1 2 1 0\n"
				+ "1 2 1 1\n"
				+ "1 2 1 2\n"
				+ "1 2 2 0\n"
				+ "1 2 2 1\n"
				+ "1 2 2 2\n"
				+ "1 1 2 0\n"
				+ "1 1 2 1\n"
				+ "1 1 2 2\n\n");
	}
	
	/**
	 * NEW 2022 - we consider coincidental correctness in case of DBM 
	 * @author inica for TABLES multRelationalOPFunction, plusMinusFunction, divFunction, ifFunction
	 *
	 */
	
	
	
	public static String getDependencyModelTables() {
		return ("\n**TUPLELIST**\n\n"
				+ "multRelOpMinMaxFunction  15 4 \n"
				+ "0 1 1 1 \n"
				+ "0 0 0 0 \n"
				+ "0 0 0 1 \n"
				+ "0 0 1 0 \n"
				+ "0 0 1 1 \n"
				+ "0 1 0 0 \n"
				+ "0 1 0 1 \n"
				+ "1 1 1 0 \n"
				+ "1 1 1 1 \n"
				+ "1 0 0 0 \n"
				+ "1 0 0 1 \n"
				+ "1 0 1 0 \n"
				+ "1 0 1 1 \n"
				+ "1 1 0 0 \n"
				+ "1 1 0 1 \n\n"
				+ "plusMinusFunction  13 4 \n"
				+ "0 1 1 1 \n"
				+ "0 0 0 0 \n"
				+ "0 0 0 1 \n"
				+ "0 0 1 0 \n"
				+ "0 1 0 0 \n"
				+ "1 1 1 0 \n"
				+ "1 1 1 1 \n"
				+ "1 0 0 0 \n"
				+ "1 0 0 1 \n"
				+ "1 0 1 0 \n"
				+ "1 0 1 1 \n"
				+ "1 1 0 0 \n"
				+ "1 1 0 1 \n\n"
				+ "divFunction  14 4 \n"
				+ "0 1 1 1 \n"
				+ "0 0 0 0 \n"
				+ "0 0 0 1 \n"
				+ "0 0 1 0 \n"
				+ "0 1 0 0 \n"
				+ "0 1 0 1 \n"
				+ "1 1 1 0 \n"
				+ "1 1 1 1 \n"
				+ "1 0 0 0 \n"
				+ "1 0 0 1 \n"
				+ "1 0 1 0 \n"
				+ "1 0 1 1 \n"
				+ "1 1 0 0 \n"
				+ "1 1 0 1 \n\n"
				+"ifFunctionD  30 5 \n"
				+ "0 1 1 1 1 \n"
				+ "0 1 0 1 0 \n"
				+ "0 1 0 1 1 \n"
				+ "0 1 1 0 0 \n"
				+ "0 1 1 0 1 \n"
				+ "0 1 0 0 0 \n"  
				+ "0 0 0 0 0 \n"
				+ "0 0 0 0 1 \n"
				+ "0 0 0 1 0 \n"
				+ "0 0 0 1 1 \n"
				+ "0 0 1 0 0 \n"
				+ "0 0 1 0 1 \n"
				+ "0 0 1 1 0 \n"
				+ "0 0 1 1 1 \n"
				+ "1 0 0 0 0 \n"
				+ "1 0 0 0 1 \n"
				+ "1 0 0 1 0 \n"
				+ "1 0 0 1 1 \n"
				+ "1 0 1 0 0 \n"
				+ "1 0 1 0 1 \n"
				+ "1 0 1 1 0 \n"
				+ "1 0 1 1 1 \n"
				+ "1 1 0 0 0 \n"
				+ "1 1 0 0 1 \n"
				+ "1 1 0 1 0 \n"
				+ "1 1 0 1 1 \n"
				+ "1 1 1 0 0 \n"
				+ "1 1 1 0 1 \n"
				+ "1 1 1 1 0 \n"
				+ "1 1 1 1 1 \n\n"
				+ "equalityFunctionD 6 3 \n"
				+ "0 0 0\n"
				+ "0 1 1\n"
				+ "1 0 0\n"
				+ "1 0 1\n"
				+ "1 1 0\n"
				+ "1 1 1\n\n");
	}
	/**
	 * DBM Table Constraints for multRelationalOPFunction, plusMinusFunction, divFunction, ifFunction
	 * @author inica
	 * 
	 */
	
	
	public static MinionExpressionConstraints getMultRelationalOpMinMaxTableConstraints(int abnormalIndex, String op1, String op2, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], multRelOpMinMaxFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}
	
	public static MinionExpressionConstraints getPlusMinusTableConstraints(int abnormalIndex, String op1, String op2, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], plusMinusFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}
	
	public static MinionExpressionConstraints getDivTableConstraints(int abnormalIndex, String op1, String op2, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op1 + "," + op2 + "," + result + "], divFunction)");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}
	
	public static MinionExpressionConstraints getIFDTableConstraints(int abnormalIndex, String cond, String op1, String op2, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"],"+ cond + "," + op1 + "," + op2 + "," + result + "], ifFunctionD)");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}
	public static MinionExpressionConstraints getEQUALDTableConstraints(int abnormalIndex, String op, String result){
		Set<String> constraints = new HashSet<String>();
		constraints.add("table([ab["+abnormalIndex+"]," + op + "," + result + "], equalityFunctionD)");
		return new MinionExpressionConstraints(constraints, result, Domain.BOOLEAN);
	}
	

}
