package at.tugraz.ist.debugging.modelbased.smt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import at.tugraz.ist.debugging.modelbased.smt.SMTConstants.Option;

/**
 * SMT code generator
 * 
 */
public class SMTCodeGenerator {
    
	public enum Operator {
		And, // smt
		Assert, CheckSat, CheckSatUsing, Div, // relational
		Equal, Geq, GetUnsatCore, GetValue, Gt, Implies, Ite, Leq, Lt, Max, Min,

		// arithmetic
		Minus, Mult, Not, NotEqual,

		// logical
		Or, Plus, Pop, Push, ToInt, ToReal

	}

	/**
	 * 
	 * Supported sort types The order of the sort types determines the
	 * generality, i.e. the higher the associated integer value, the more
	 * general
	 */
	public enum SortType {
		// / the higher the value the more general
		Bool(0), Int(1), Real(2);
		private final int value;

		private SortType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

    private static final String SMT_SET_LOGIC = "(set-logic %s)";
	private static final String SMT_CHECKSAT_USING_PARAMS = "then simplify solve-eqs smt";
	private static final String SMT_CONST_DECLARATION_TEMPLATE = "(declare-const %s %s)";
	private static final String SMT_DECLARATION_TEMPLATE = "(declare-fun %s () %s)";
	private static final String SMT_DEFINITION_TEMPLATE = "(define-fun %s (%s) %s %s)";
	private static final String SMT_FUNCTION_0_TEMPLATE = "(%s)";
	private static final String SMT_FUNCTION_TEMPLATE = "(%s %s)";

	private static final Map<Operator, String> SMT_OPERATORS = new HashMap<Operator, String>();

	private static final String SMT_OPTION_TEMPLATE = "(set-option :%s %s)";

	public static final Map<Option, String> SMT_OPTIONS = new HashMap<Option, String>();

	private static final String SMT_PARAM_TEMPLATE = "(%s %s)";

	static {
		SMT_OPERATORS.put(Operator.Assert, "assert");
		SMT_OPERATORS.put(Operator.Push, "push");
		SMT_OPERATORS.put(Operator.Pop, "pop");
		SMT_OPERATORS.put(Operator.CheckSat, "check-sat");
		SMT_OPERATORS.put(Operator.CheckSatUsing, "check-sat-using");
		SMT_OPERATORS.put(Operator.GetValue, "get-value");
		SMT_OPERATORS.put(Operator.GetUnsatCore, "get-unsat-core");
		SMT_OPERATORS.put(Operator.ToReal, "to_real");
		SMT_OPERATORS.put(Operator.ToInt, "to_int");

		SMT_OPERATORS.put(Operator.Equal, "=");
		SMT_OPERATORS.put(Operator.NotEqual, "=");
		SMT_OPERATORS.put(Operator.Geq, ">=");
		SMT_OPERATORS.put(Operator.Gt, ">");
		SMT_OPERATORS.put(Operator.Leq, "<=");
		SMT_OPERATORS.put(Operator.Lt, "<");

		SMT_OPERATORS.put(Operator.Ite, "ite");
		SMT_OPERATORS.put(Operator.Implies, "=>");
		SMT_OPERATORS.put(Operator.Or, "or");
		SMT_OPERATORS.put(Operator.And, "and");
		SMT_OPERATORS.put(Operator.Not, "not");

		SMT_OPERATORS.put(Operator.Minus, "-");
		SMT_OPERATORS.put(Operator.Plus, "+");
		SMT_OPERATORS.put(Operator.Mult, "*");
		SMT_OPERATORS.put(Operator.Div, "div");
		SMT_OPERATORS.put(Operator.Min, "min");
		SMT_OPERATORS.put(Operator.Max, "max");

	}

	static {
		SMT_OPTIONS.put(Option.ProduceModels, "produce-models");
		SMT_OPTIONS.put(Option.ProduceUnsatCores, "produce-unsat-cores");
	}

	/**
	 * Generated SMT code is written to this output stream
	 */
	private OutputStream os;

	/**
	 * 
	 * @param os
	 *            Target stream for generated SMT code
	 */
	public SMTCodeGenerator(OutputStream os) {
		this.os = os;
	}

	/**
	 * Generates the SMT code for an abs() operation
	 * 
	 * @param expression
	 *            Parameter (must be Int or Real sort)
	 * @return
	 */
	public String abs(String expression) {
		return ite(lt(expression, getValue(0)), minus(expression), expression);

	}

	/**
	 * Adds an assertion
	 * 
	 * @param expression
	 *            Expression which is asserted to be true
	 * @throws IOException
	 */
	public void addAssertion(String expression) throws IOException {
		write(String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Assert), expression));
	}
    
    /**
	 * Adds the set-logic command
	 */
	public void addSetLogic() throws IOException {
		write(String.format(SMT_SET_LOGIC, "QF_NRA"));
	}

	/**
	 * Adds the check-sat command If no assumptions are given the check-sat
	 * command will behave as usual. In case of UNSAT a subset of the provided
	 * assumptions (tracking variables) will be reported in the unsat core
	 * 
	 * @param assumptions
	 *            Assumptions Tracking variables, which are assumed to be true
	 * @throws IOException
	 */
	public void addCheckSat(String... assumptions) throws IOException {
		if (assumptions == null || assumptions.length == 0) {
			write(String.format(SMT_FUNCTION_0_TEMPLATE,
					SMT_OPERATORS.get(Operator.CheckSat)));
		} else {
			write(String.format(SMT_FUNCTION_TEMPLATE,
					SMT_OPERATORS.get(Operator.CheckSat),
					concatExpressions(assumptions)));
		}
		os.flush();
	}

	/**
	 * Add a check-sat which leads to a prior formula simplification
	 * 
	 * @throws IOException
	 */
	public void addCheckSatSimplify() throws IOException {
		write(String.format(SMT_FUNCTION_TEMPLATE, SMT_OPERATORS
				.get(Operator.CheckSatUsing), String.format(
				SMT_FUNCTION_0_TEMPLATE, SMT_CHECKSAT_USING_PARAMS)));
		os.flush();
	}

	/**
	 * Generates and adds a constant declaration
	 * 
	 * @param varName
	 *            Variable name
	 * @param type
	 *            Sort
	 * @throws IOException
	 */
	public void addConstDeclaration(String varName, SortType type)
			throws IOException {
		write(String.format(SMT_CONST_DECLARATION_TEMPLATE, varName, type));
	}

	/**
	 * Adds a variable declaration
	 * 
	 * @param varName
	 *            Variable name
	 * @param type
	 *            Sort
	 * @throws IOException
	 */
	public void addDeclaration(String varName, SortType type)
			throws IOException {
		write(String.format(SMT_DECLARATION_TEMPLATE, varName, type));
	}

	/**
	 * Adds a variable (or function) definition
	 * 
	 * @param name
	 *            Name
	 * @param type
	 *            Sort
	 * @param body
	 *            Function body or value
	 * @param params
	 *            Parameters
	 * @throws IOException
	 */
	public void addDefinition(String name, SortType type, String body,
			String... params) throws IOException {
		write(String.format(SMT_DEFINITION_TEMPLATE, name,
				concatExpressions(params), type, body));
	}

	/**
	 * Adds the command for retrieving the UNSAT core
	 * 
	 * This method should only be called after (check-sat) which leaded to UNSAT
	 * 
	 * @throws IOException
	 */
	public void addGetUnsatCore() throws IOException {
		write(String.format(SMT_FUNCTION_0_TEMPLATE,
				SMT_OPERATORS.get(Operator.GetUnsatCore)));
		os.flush();
	}

	/**
	 * Adds the command for retrieving values (subset of the model) after a
	 * successful try to satisfy the formula
	 * 
	 * This method should only be called after (check-sat) which leaded to SAT
	 * 
	 * @param variables
	 *            Variables whose values should be reported
	 * @throws IOException
	 */
	public void addGetValue(String... variables) throws IOException {
		write(String.format(SMT_FUNCTION_TEMPLATE, SMT_OPERATORS
				.get(Operator.GetValue), String.format(SMT_FUNCTION_0_TEMPLATE,
				concatExpressions(variables))));
		os.flush();
	}

	/**
	 * Adds a configuration option
	 * 
	 * @param option
	 *            Option name
	 * @param enabled
	 *            Option enabled state
	 * @throws IOException
	 *             Error while writing SMT code
	 */
	public void addOption(Option option, boolean enabled) throws IOException {
		write(String.format(SMT_OPTION_TEMPLATE, SMT_OPTIONS.get(option),
				enabled ? SMTConstants.SMT_TRUE : SMTConstants.SMT_FALSE));
	}

	/**
	 * Adds the command for removing the top frame from the stack
	 * 
	 * @throws IOException
	 */
	public void addPopFrame() throws IOException {
		write(String.format(SMT_FUNCTION_0_TEMPLATE,
				SMT_OPERATORS.get(Operator.Pop)));
	}

	/**
	 * Adds the command for pushing a new frame onto the stack
	 * 
	 * @throws IOException
	 */
	public void addPushFrame() throws IOException {
		write(String.format(SMT_FUNCTION_0_TEMPLATE,
				SMT_OPERATORS.get(Operator.Push)));
	}

	/**
	 * Generates the SMT code for a logical AND
	 * 
	 * @param expressions
	 * @return
	 */
	public String and(String... expressions) {
		if (expressions == null || expressions.length == 0)
			return "";

		return String
				.format(SMT_FUNCTION_TEMPLATE, SMT_OPERATORS.get(Operator.And),
						concatExpressions(expressions));
	}

	// generation functions

	/**
	 * Generates the SMT code for a function call
	 * 
	 * @param functionName
	 *            Name of the function which should be called
	 * @param expressions
	 *            Parameters
	 * @return
	 */
	public String call(String functionName, String... expressions) {
		if (expressions == null || expressions.length == 0)
			return "";
		return String.format(SMT_FUNCTION_TEMPLATE, functionName,
				concatExpressions(expressions));
	}

	/**
	 * Joins a list of SMT expressions to a string where the particular elements
	 * are splitted by " "
	 * 
	 * @param expressions
	 * @return
	 */
	private String concatExpressions(String... expressions) {
		StringBuilder expressionString = new StringBuilder();
		for (String expr : expressions) {
			if (expressionString.length() > 0)
				expressionString.append(" ");
			expressionString.append(expr);
		}
		return expressionString.toString();
	}

	/**
	 * Generates the SMT code for a division
	 * 
	 * @param expression1
	 * @param expression2
	 * @return
	 */
	public String div(String expression1, String expression2) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Div),
				concatExpressions(expression1, expression2));
	}

	/**
	 * Generates the SMT code for an equality comparison
	 * 
	 * @param expr1
	 *            Expression which should be compared
	 * @param expr2
	 *            Expression which should be compared
	 * @return
	 */
	public String equal(String expr1, String expr2) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Equal),
				concatExpressions(expr1, expr2));
	}

	/**
	 * Generates the SMT code for a >= comparison
	 * 
	 * @param expr1
	 * @param expr2
	 * @return
	 */
	public String geq(String expr1, String expr2) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Geq),
				concatExpressions(expr1, expr2));
	}

	/**
	 * Gets the SMT representation of a given value
	 * 
	 * @param obj
	 *            Boolean, Integer or Double value
	 * @return
	 */
	public String getValue(Object obj) {
		if (obj instanceof Boolean) {
			return (boolean) obj ? SMTConstants.SMT_TRUE
					: SMTConstants.SMT_FALSE;
		} else if (obj instanceof Integer) {
			int value = (int) obj;
			String valueString = Integer.toString(Math.abs(value));
			return value < 0 ? minus(valueString) : valueString;
		} else if (obj instanceof Double) {
			double value = (double) obj;
			String valueString = Double.toString(Math.abs(value));
			return value < 0 ? minus(valueString) : valueString;
		}
		throw new RuntimeException(String.format(
				"Object type %s is not supported", obj == null ? "null-object"
						: obj.getClass().getName()));

	}

	/**
	 * Generates the SMT code for a > comparison
	 * 
	 * @param expr1
	 * @param expr2
	 * @return
	 */
	public String gt(String expr1, String expr2) {
		return String
				.format(SMT_FUNCTION_TEMPLATE, SMT_OPERATORS.get(Operator.Gt),
						concatExpressions(expr1, expr2));
	}

	/**
	 * Generates the SMT code for an implication
	 * 
	 * @param expression1
	 * @param expression2
	 * @return
	 */
	public String implies(String expression1, String expression2) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Implies),
				concatExpressions(expression1, expression2));
	}

	/**
	 * Generates the SMT code for an if-then-else expression
	 * 
	 * @param smtCond
	 *            condition (must be a boolean expression)
	 * @param smtThen
	 *            then expression
	 * @param smtElse
	 *            else expression
	 * @return
	 */
	public String ite(String smtCond, String smtThen, String smtElse) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Ite),
				concatExpressions(smtCond, smtThen, smtElse));
	}

	/**
	 * Generates the SMT code for a <= comparison
	 * 
	 * @param expr1
	 * @param expr2
	 * @return
	 */
	public String leq(String expr1, String expr2) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Leq),
				concatExpressions(expr1, expr2));
	}

	/**
	 * Generates the SMT code for a < comparison
	 * 
	 * @param expr1
	 * @param expr2
	 * @return
	 */
	public String lt(String expr1, String expr2) {
		return String
				.format(SMT_FUNCTION_TEMPLATE, SMT_OPERATORS.get(Operator.Lt),
						concatExpressions(expr1, expr2));
	}

	/**
	 * Generates the SMT code for a max() operation with an arbitrary number of
	 * parameters
	 * 
	 * @param expressions
	 * @return
	 */
	public String max(String... expressions) {
		if (expressions.length == 0)
			return "";
		String expr = expressions[0];

		for (int i = 0; i < expressions.length - 1; i++) {
			expr = max2(expr, expressions[i + 1]);
		}
		return expr;
	}

	/**
	 * Generates the SMT code for a max() operation with two parameters
	 * 
	 * @param expr1
	 * @param expr2
	 * @return
	 */
	private String max2(String expr1, String expr2) {
		return ite(gt(expr1, expr2), expr1, expr2);
	}

	/**
	 * Generates the SMT code for a min() operation with an arbitrary number of
	 * parameters
	 * 
	 * @param expressions
	 * @return
	 */
	public String min(String... expressions) {
		if (expressions.length == 0)
			return "";
		String expr = expressions[0];

		for (int i = 0; i < expressions.length - 1; i++) {
			expr = min2(expr, expressions[i + 1]);
		}
		return expr;
	}

	/**
	 * Generates the SMT code for a min() operation with two parameters
	 * 
	 * @param expr1
	 * @param expr2
	 * @return
	 */
	private String min2(String expr1, String expr2) {
		return ite(lt(expr1, expr2), expr1, expr2);
	}

	/**
	 * Generates the SMT code for a subtraction
	 * 
	 * @param expressions
	 * @return
	 */
	public String minus(String... expressions) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Minus),
				concatExpressions(expressions));
	}

	/**
	 * Generates the SMT code for a multiplication
	 * 
	 * @param expressions
	 * @return
	 */
	public String mult(String... expressions) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Mult),
				concatExpressions(expressions));
	}

	/**
	 * Generates the SMT code for a negation
	 * 
	 * @param expression
	 * @return
	 */
	public String not(String expression) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Not), expression);
	}

	/**
	 * Returns an array of the generated negations of the given SMT expressions
	 * 
	 * @param expressions
	 * @return
	 */
	public String[] not(String[] expressions) {
		String[] negatedExpressions = new String[expressions.length];
		for (int i = 0; i < expressions.length; i++)
			negatedExpressions[i] = String.format(SMT_FUNCTION_TEMPLATE,
					SMT_OPERATORS.get(Operator.Not), expressions[i]);
		return negatedExpressions;
	}

	/**
	 * Generates the SMT code for a neq comparison
	 * 
	 * @param expr1
	 * @param expr2
	 * @return
	 */
	public String notEqual(String expr1, String expr2) {
		return not(equal(expr1, expr2));
	}

	/**
	 * Generates the SMT code for a logical OR
	 * 
	 * @param expressions
	 * @return
	 */
	public String or(String... expressions) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Or), concatExpressions(expressions));
	}

	/**
	 * Generates the SMT code for an addition
	 * 
	 * @param expressions
	 * @return
	 */
	public String plus(String... expressions) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Plus),
				concatExpressions(expressions));
	}

	/**
	 * Generate the SMT code for the power operation
	 * 
	 * @param opConstraint1
	 * @param opConstraint2
	 * @return
	 */
	public String power(String opConstraint1, String opConstraint2) {
		// RESTRICTION: not supported yet
		throw new NotImplementedException("power function is not implemented");
	}

	/**
	 * Generates the SMT code for an addition of an arbitrary number of
	 * expressions
	 * 
	 * @param expressions
	 * @return
	 */
	public String sum(String... expressions) {
		return String.format(SMT_FUNCTION_TEMPLATE,
				SMT_OPERATORS.get(Operator.Plus),
				concatExpressions(expressions));
	}

	/**
	 * Generates the code for a parameter definition
	 * 
	 * @param name
	 *            Parameter name
	 * @param type
	 *            Sort
	 * @return
	 * @throws IOException
	 */
	public String toParameter(String name, SortType type) throws IOException {
		return String.format(SMT_PARAM_TEMPLATE, name, type);
	}

	/**
	 * Converts a given expression to a particular target type
	 * 
	 * @param expression
	 *            Expression which should be converted
	 * @param sourceType
	 *            Source sort
	 * @param targetType
	 *            Target sort
	 * @return
	 */
	public String toSortType(String expression, SortType sourceType,
			SortType targetType) {
		if (sourceType == targetType)
			return expression;

		if (sourceType == SortType.Real && targetType == SortType.Int)
			return String.format(SMT_FUNCTION_TEMPLATE,
					SMT_OPERATORS.get(Operator.ToInt), expression);
		else if (sourceType == SortType.Int && targetType == SortType.Real)
			return String.format(SMT_FUNCTION_TEMPLATE,
					SMT_OPERATORS.get(Operator.ToReal), expression);
		else if (sourceType == SortType.Bool
				&& (targetType == SortType.Int || targetType == SortType.Real)) {
			return ite(expression, targetType == SortType.Int ? getValue(1)
					: getValue(1.0), targetType == SortType.Int ? getValue(0)
					: getValue(0.0));
		} else if ((sourceType == SortType.Int || sourceType == SortType.Real)
				&& targetType == SortType.Bool) {
			return not(equal(expression,
					getValue(sourceType == SortType.Int ? 0 : 0.0)));
		}
		throw new RuntimeException(
				String.format(
						"SMT code generator: Cast error, cannot cast expression '%s' from %s to %s",
						expression, sourceType, targetType));
	}

	/**
	 * Writes a line of code to the output stream
	 * 
	 * @param line
	 * @throws IOException
	 */
	private void write(String line) throws IOException {
		String content = line + "\n";
		os.write(content.getBytes());
        System.out.write(content.getBytes());
		// Writer.print(content);
	}
}
