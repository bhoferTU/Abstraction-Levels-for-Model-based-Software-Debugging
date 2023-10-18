package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import at.tugraz.ist.debugging.spreadsheets.expressions.Expression;
import java.util.ArrayList;
import java.util.List;

import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.WorkbookInfo;
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;

/**
 * Base class for the representation of built-in functions
 */
public abstract class Function extends Expression {
	public static final String ABS_FUNCTION_NAME = "ABS";
	public static final String AND_FUNCTION_NAME = "AND";
	public static final String AVG_FUNCTION_NAME = "AVERAGE";
	public static final String IF_FUNCTION_NAME = "IF";
	//public static final String IF_FUNCTION_NAME = "WENN";
	public static final String MAX_FUNCTION_NAME = "MAX";
	public static final String MIN_FUNCTION_NAME = "MIN";
	public static final String MOD_FUNCTION_NAME = "MOD";
	public static final String NOT_FUNCTION_NAME = "NOT";
	public static final String OR_FUNCTION_NAME = "OR";
	public static final String SUM_FUNCTION_NAME = "SUM";
    public static final String COUNT_FUNCTION_NAME = "COUNT";
    public static final String PRODUCT_FUNCTION_NAME = "PRODUCT";
    public static final String POWER_FUNCTION_NAME = "POWER";
    public static final String SUMPRODUCT_FUNCTION_NAME = "SUMPRODUCT";
    public static final String PI_FUNCTION_NAME = "PI";
    public static final String SQRT_FUNCTION_NAME = "SQRT";
    public static final String VAR_FUNCTION_NAME = "VAR";
    public static final String RANK_FUNCTION_NAME = "RANK";
    public static final String SMALL_FUNCTION_NAME = "SMALL";
    
    protected String functionName;
    
    public Function(IConstraintExpression[] operands) {
        super(operands);
    }
    
    public Function(String functionName, IConstraintExpression[] operands) {
        super(operands);
        this.functionName = functionName;
    }
    
	/**
	 * Factory method for built-in functions
	 * 
	 * @param name
	 *            Function name
	 * @param operands
	 *            Operands
	 * @return Corresponding function object
	 */
	public static Function Create(String name, IConstraintExpression[] operands, int ws, WorkbookInfo wb) {
		switch (name) {
		case IF_FUNCTION_NAME:
			return IfFunction.Create(operands, ws, wb);
		case SUM_FUNCTION_NAME:
			return new SumFunction(operands);
		case AVG_FUNCTION_NAME:
			return new AvgFunction(operands);
		case AND_FUNCTION_NAME:
			return new LogicalFunction(operands,
					LogicalFunction.LogicalFunctionType.And);
		case OR_FUNCTION_NAME:
			return new LogicalFunction(operands,
					LogicalFunction.LogicalFunctionType.Or);
		case MIN_FUNCTION_NAME:
			return new MinMaxFunction(operands, MinMaxFunction.FunctionType.Min);
		case MAX_FUNCTION_NAME:
			return new MinMaxFunction(operands, MinMaxFunction.FunctionType.Max);
		case ABS_FUNCTION_NAME:
			if (operands.length != 1)
				throw new RuntimeException(
						"Function: abs() only supported for one argument");
			return new AbsFunction(operands[0]);
		case NOT_FUNCTION_NAME:
			if (operands.length != 1)
				throw new RuntimeException(
						"Function: not() only supported for one argument");
			return new NotFunction(operands[0]);
		case MOD_FUNCTION_NAME:
			if (operands.length != 2)
				throw new RuntimeException(
						"Function: mod() only supported for two arguments");
			return new ModFunction(operands[0], operands[1]);
        case COUNT_FUNCTION_NAME:
            return new CountFunction(operands);   
        case PRODUCT_FUNCTION_NAME:
            if(operands.length < 1)
                throw new RuntimeException(
						"Function: product() only supported for 1 argument or more");
            return new ProductFunction(operands);
        case POWER_FUNCTION_NAME:
            if(operands.length != 2)
                throw new RuntimeException(
						"Function: power() only supported for two arguments");
            return new PowerFunction(operands[0], operands[1]);
        case SUMPRODUCT_FUNCTION_NAME:
            if(operands.length < 2)
                throw new RuntimeException(
						"Function: sumproduct() only supported for 2 arguments or more");
            return new SumProductFunction(operands);
        case PI_FUNCTION_NAME:
            if (operands.length != 0)
                throw new RuntimeException(
						"Function: pi() requires no arguments");
            return new PiFunction(operands);
         case SQRT_FUNCTION_NAME:
            if (operands.length != 1)
                throw new RuntimeException(
						"Function: sqrt() only supported for 1 argument");
            return new SqrtFunction(operands[0]);   
        case VAR_FUNCTION_NAME:
            if (operands.length < 1)
                throw new RuntimeException(
						"Function: var() only supported for 1 argument or more");
            return new VarFunction(operands);
        case RANK_FUNCTION_NAME:
            if (operands.length < 2)
                throw new RuntimeException(
						"Function: rank() only supported for 2 arguments or more");
            return new RankFunction(operands);
        case SMALL_FUNCTION_NAME:
            if (operands.length != 2)
                throw new RuntimeException(
						"Function: small() only supported for 2 arguments");
            return new SmallFunction(operands);
		default:
			/*throw new InvalidOperationException(String.format(
					"IConstraintExpression not implemented for function '%s'",
					name));*/ //allow parsing, throw exception once we try to solve constraints on this variable
			return new ShellFunction(operands);
		}
	}

	/**
	 * Returns a list of expressions which does not contain a cell reference but
	 * the expressions of its referred cells
	 * 
	 * @param expressions
	 *            Expressions which may include cell references
	 * @return Expressions without cell references including the referred cells'
	 *         expressions
	 */
	public List<IConstraintExpression> getFlattedExpressions(
			IConstraintExpression[] expressions) {
		List<IConstraintExpression> flatExpr = new ArrayList<IConstraintExpression>();
		for (IConstraintExpression expression : expressions) {
			if (expression instanceof CellReference) {
				flatExpr.addAll(((CellReference) expression)
						.getReferencedExpressions());
			} else {
				flatExpr.add(expression);
			}
		}
		return flatExpr;
	}
    
    public String getFunctionName() { return functionName; }

	/**
	 * Generates the string representation of the function object
	 * 
	 * @param functionName
	 * @param expressions
	 * @return
	 */
	public String toString(String functionName,
			IConstraintExpression[] expressions) {
		String stringVal = functionName + "(%s)";

		String paramString = "";
		if (expressions != null) {
			for (int i = 0; i < expressions.length; i++) {
				paramString += i > 0 ? ", " : "";
				paramString += expressions[i];
			}
		}
		return String.format(stringVal, paramString);
	}
}
