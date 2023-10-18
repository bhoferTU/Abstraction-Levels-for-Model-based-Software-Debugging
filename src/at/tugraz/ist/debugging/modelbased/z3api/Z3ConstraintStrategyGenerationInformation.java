package at.tugraz.ist.debugging.modelbased.z3api;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.ConstExpression;
import at.tugraz.ist.util.debugging.Writer;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Z3Exception;
import com.microsoft.z3.enumerations.Z3_lbool;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public abstract class Z3ConstraintStrategyGenerationInformation extends 
        ConstraintStrategyGenerationInformation{

    /**
	 * Z3 context
	 */
	protected Context ctx;
    
    /**
	 * Z3 solver
	 */
	protected Solver solver;
    
    /**
	 * Cell to cell notabnormal variable mapping
	 */
	protected Map<Cell, BoolExpr> notabnormalVariablesMap;
    
    /**
	 * input variables set
	 */
	protected Set<Cell> inputVariables;
    
    /**
	 * Set of cells which are neither input, output or reference cells
	 */
    protected Set<Cell> normalVariables;
    
    /**
	 * notabnormal variable to cell mapping
	 */
	protected Map<BoolExpr, Cell> revNotabnormalVariablesMap;
    
    /**
	 * String to integer mapping
	 */
	private final Map<String, Integer> stringMapping;
    
    /**
	 * Cell to cell variable mapping
	 */
	protected Map<Cell, Expr> variables;
    
    
    public Z3ConstraintStrategyGenerationInformation(Context ctx,
			Solver solver, Set<Cell> cone, boolean useCones, EModelGranularity modelGranularity) {
		super(cone, useCones, modelGranularity);
		this.ctx = ctx;
		this.solver = solver;
        
        this.notabnormalVariablesMap = new HashMap<>();
        this.revNotabnormalVariablesMap = new HashMap<>();
        this.stringMapping = new HashMap<>();
        this.variables = new HashMap<>();
	}

    /**
	 * Adds an notabnormal variable to the cell to notabnormal variable 
     * and vice versa mapping
	 * 
	 * @param cell
	 * @param var
	 */
	public void addNotabnormalVariable(Cell cell, BoolExpr var) {
		notabnormalVariablesMap.put(cell, var);
		revNotabnormalVariablesMap.put(var, cell);
	}
    
    /**
	 * Adds an assertion to the current solver instance
	 * 
	 * @param expr
	 *            Expression which is asserted to be true
	 * @throws Z3Exception
	 */
	public void addAssert(BoolExpr expr) throws Z3Exception {
		solver.Assert(expr);
	}
    
    /**
	 * Adds a blocking clause to the current solver instance
	 * 
	 * At this the avoidance level determines the structure of the clause: -
	 * All: The model is avoided by building a conjunction of all variable and
	 * their corresponding assignments - TrueOnly: Only true variable
	 * assignments are considered in the blocking clause - FalseOnly: Only false
	 * variable assignments are considered in the blocking clause
	 * 
	 * @param model
	 *            Model which should be blocked
	 * @param avoidanceMode
	 *            Structure of the blocking clause
	 * @throws Z3Exception
	 */
	public void addAvoid(Model model, Z3ValueBasedModelGenerationInformation.Avoidance avoidanceMode)
			throws Z3Exception {
		Set<BoolExpr> trueNotabnormals = new HashSet<BoolExpr>();
		Set<BoolExpr> falseNotabnormals = new HashSet<BoolExpr>();
		for (BoolExpr notabnormal : notabnormalVariablesMap.values()) {
			Expr expr = model.Eval(notabnormal, false);
			if (!expr.IsBool())
				throw new RuntimeException();
			if (expr.BoolValue() == Z3_lbool.Z3_L_TRUE)
				trueNotabnormals.add(notabnormal);
			else if (expr.BoolValue() == Z3_lbool.Z3_L_FALSE)
				falseNotabnormals.add(notabnormal);
		}

		BoolExpr[] trueNotabnormalsArray = new BoolExpr[trueNotabnormals.size()];
		trueNotabnormalsArray = trueNotabnormals.toArray(trueNotabnormalsArray);
		if (avoidanceMode == Z3ValueBasedModelGenerationInformation.Avoidance.TrueOnly) {
			solver.Assert(ctx.MkNot(ctx.MkAnd(trueNotabnormalsArray)));
			return;
		}

		BoolExpr[] falseNotabnormalsArray = new BoolExpr[falseNotabnormals.size()];
		falseNotabnormalsArray = falseNotabnormals.toArray(falseNotabnormalsArray);
		for (int i = 0; i < falseNotabnormalsArray.length; i++) {
			falseNotabnormalsArray[i] = ctx.MkNot(falseNotabnormalsArray[i]);
		}

		if (avoidanceMode == Z3ValueBasedModelGenerationInformation.Avoidance.FalseOnly) {
			solver.Assert(ctx.MkNot(ctx.MkAnd(falseNotabnormalsArray)));
			return;
		}

		solver.Assert(ctx.MkNot(ctx.MkAnd(new BoolExpr[] {
				ctx.MkAnd(trueNotabnormalsArray), ctx.MkAnd(falseNotabnormalsArray) })));
	}
    
    /**
	 * Adds a cell assignment assertion to the solver
	 * 
	 * @param cell
	 * @param expr
	 * @throws Z3Exception
	 */
	public abstract void addCellAssignment(Cell cell, Expr expr) throws Z3Exception;
    
    /**
	 * Returns a cell assignment assertion
	 *
	 * @param cell
	 * @param expr
	 * @throws Z3Exception
	 */
	public BoolExpr getCellAssignment(Cell cell, Expr expr) throws Z3Exception {
			return ctx.MkEq(variables.get(cell), expr);
	}
    
    public abstract void addConstraintsForOutputCells(Cell currentCell,
            Map.Entry<Coords, ConstExpression> entry) throws Z3Exception;
    
    /**
	 * Determines a string to integer mapping for a given string and returns the
	 * corrsponding integer
	 * 
	 * The mapping is determined by calculating the MD5 hash sum of the given
	 * string
	 * 
	 * @param value
	 *            String value which should be mapped to an integer
	 * @return Corresponding integer value
	 */
	public Integer addStringMapping(String value) {
		if (stringMapping.containsKey(value))
			return stringMapping.get(value);

		MessageDigest md;
		Integer intVal = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(value.getBytes());
			intVal = new BigInteger(Arrays.copyOf(digest, 4)).intValue();
		} catch (NoSuchAlgorithmException e) {
			intVal = new Random().nextInt();
		}

		stringMapping.put(value, intVal);
		return intVal;
	}
    
    /**
	 * Adds a variable to the cell to cell variable mapping
	 * 
	 * @param cell
	 * @param var
	 */
	public void addVariable(Cell cell, Expr var) {
		variables.put(cell, var);
	}
    
    /**
	 * Adds a new weight variable which is asserted to be equal to the sum of
	 * all weights for the given boolean variables
	 * 
	 * @param booleanVariables
	 *            Array of variables which should influence the weight sum
	 * @return Int constant which is equal to the weight sum
	 * @throws Z3Exception
	 */
	public IntExpr addWeightFunction(BoolExpr[] booleanVariables)
			throws Z3Exception {

		IntExpr weightVariable = ctx.MkIntConst("weight");

		IntExpr[] expressions = new IntExpr[booleanVariables.length];
		for (int i = 0; i < booleanVariables.length; i++) {
			expressions[i] = getWeight(booleanVariables[i]);
		}

		solver.Assert(ctx.MkEq(weightVariable, ctx.MkAdd(expressions)));
		return weightVariable;
	}
    
    /**
	 * Creates cell and notabnormal variables for the given cell container
	 * 
	 * @param cellContainer
	 * @param useStrings
	 * @throws Z3Exception
	 */
	public void createVariables(CellContainer cellContainer, boolean useStrings)
			throws Z3Exception {
		createVariables(cellContainer, useStrings, "");
	}
    
    /**
	 * Creates cell and notabnormal variables for the given cell container where
	 * each created variable has a certain prefix
	 * 
	 * @param cellContainer
	 * @param useStrings
	 * @param varPrefix
	 *            Common prefix of the newly created variables
	 * 
	 * @throws Z3Exception
	 */
	public abstract void createVariables(CellContainer cellContainer,
			boolean useStrings, String varPrefix) throws Z3Exception;
    
    /**
	 * Creates cell and notabnormal variables for cells that contain no value 
     * (therefore not included in cell container), but are referenced by other 
     * cells.
	 * 
	 * @param cellContainer
	 * @param useStrings
	 * @param varPrefix
	 *            Common prefix of the newly created variables
	 * 
	 * @throws Z3Exception
	 */
    protected void createNullVariable(Cell cell, boolean useStrings, String varPrefix) 
            throws Z3Exception {

        if (useCones && !cone.contains(cell))
            return;

        String cellPosition = cell.getCoords().getConstraintString();
        String cellVariableName = varPrefix
                + ConstraintStrategy.VAR_CELL_NAME_PREFIX + cellPosition;

        switch(modelGranularity)
        {
            case Simple:
            case Sophisticated:
                 // RESTRICTION: null cells are mapped to Boolean
                addVariable(cell, ctx.MkBoolConst(cellVariableName));
                Writer.println(String
                        .format("Warning (Constraint strategy): Cell %s is mapped to a Boolean variable",
                                cell.getCoords().getUserString()));
                break;
            case Value:
                // RESTRICTION: null cells are mapped to integers
                addVariable(cell, ctx.MkRealConst(cellVariableName));
                Writer.println(String
                        .format("Warning (Constraint strategy): Cell %s is mapped to an integer variable",
                                cell.getCoords().getUserString()));
                break;
            default:
                throw new AssertionError(modelGranularity.name());
        }
	}
    
    /**
	 * Determines the most general sort of an arbitrary amount of given
	 * expressions
	 * 
	 * The method determines the sort by the following hierarchy (less to more
	 * general): - Bool - Int - Real
	 * 
	 * @param expressions
	 * @return
	 */
	public SMTCodeGenerator.SortType determineMostGeneralSort(Expr... expressions) {
		if (expressions == null || expressions.length == 0)
			throw new RuntimeException();
		if (expressions.length == 1)
			return determineSort(expressions[0]);

		SMTCodeGenerator.SortType currentMostGeneral = determineSort(expressions[0]);
		for (int i = 1; i < expressions.length; i++) {
			SMTCodeGenerator.SortType currentSortType = determineSort(expressions[i]);
			if (currentSortType.getValue() > currentMostGeneral.getValue())
				currentMostGeneral = currentSortType;
		}
		return currentMostGeneral;
	}
    
    /**
	 * Determines the corresponding Z3 sort of a given object
	 * 
	 * @param value
	 * @return
	 */
	public SMTCodeGenerator.SortType determineSort(Object value) {
		if (value instanceof Integer || value instanceof IntExpr)
			return SMTCodeGenerator.SortType.Int;
		else if (value instanceof Boolean || value instanceof BoolExpr)
			return SMTCodeGenerator.SortType.Bool;
		else if (value instanceof Double || value instanceof RealExpr)
			return SMTCodeGenerator.SortType.Real;
		else if (value instanceof String)
			return SMTCodeGenerator.SortType.Int;

		throw new RuntimeException(String.format(
				"Sort type for given object '%s' could not be determined",
				value));
	}
    
    @Override
	public int getAbnormalsCount() {
		return notabnormalVariablesMap.size();
	}
    
    /**
	 * Returns the cell which corresponds to the given notabnormal variable
	 * 
	 * @param notabnormalVar
	 * @return
	 */
	public Cell getNotabnormalVariable(BoolExpr notabnormalVar) {
		return revNotabnormalVariablesMap.get(notabnormalVar);
	}
    
    /**
	 * @return Array of notabnormal variables
	 */
	public BoolExpr[] getNotabnormalVariables() {
		BoolExpr[] vars = new BoolExpr[notabnormalVariablesMap.size()];
		vars = notabnormalVariablesMap.values().toArray(vars);
		return vars;
	}
    
    /**
	 * @return Cell to notabnormal variables mapping
	 */
	public Map<Cell, BoolExpr> getNotabnormalVariablesMap() {
		return notabnormalVariablesMap;
	}

    @Override
	public int getCellVariablesCount() {
		return variables.size();
	}
    
    public Context getContext() {
		return this.ctx;
	}
    
    /**
	 * initializes the set of normal variables with the cellContainer
	 */
    public void initializeNormalVariables(CellContainer cellContainer)
    {
        normalVariables = new HashSet<>();
        for(Cell cell : variables.keySet())
            if(!cellContainer.isInputCell(cell) && !notabnormalVariablesMap.containsKey(cell))
                normalVariables.add(cell);
    }
    
    /**
	 * @return Set of cells which are neither input, output or reference cells.
	 */
    public Set<Cell> getNormalCells()
    {
        return normalVariables;
    }
    
    /**
	 * Returns an array of variables which are assigned to false
	 * 
	 * @param model
	 *            Model which should be filtered
	 * @return
	 * @throws Z3Exception
	 */
	public BoolExpr[] getFalseVariables(Model model) throws Z3Exception {
		List<BoolExpr> falseNotabnormals = new ArrayList<BoolExpr>();
		for (BoolExpr notabnormal : notabnormalVariablesMap.values()) {
			Expr expr = model.Eval(notabnormal, false);
			if (!expr.IsBool())
				throw new RuntimeException();
			else if (expr.BoolValue() == Z3_lbool.Z3_L_FALSE)
				falseNotabnormals.add(notabnormal);
		}
		BoolExpr[] falseNotabnormalsArray = new BoolExpr[falseNotabnormals.size()];
		return falseNotabnormals.toArray(falseNotabnormalsArray);
	}
    
    /**
	 * initializes the set of input variables with the cellContainer
	 */
    public void initializeInputVariables(CellContainer cellContainer)
    {
        inputVariables = new HashSet<>();
        for(Cell cell : variables.keySet())
            if(cellContainer.isInputCell(cell) && 
                    !ConstraintStrategyConfiguration.getFaultyCells().contains(cell.getCoords()))
                inputVariables.add(cell);
    }
    
    /**
	 * @return Set of input cells
	 */
	public Set<Cell> getInputCells() {
		return inputVariables;
	}

	/**
	 * @return Array of inverted notabnormal variables
	 * @throws Z3Exception
	 */
	public BoolExpr[] getInvertedNotabnormals() throws Z3Exception {
		BoolExpr[] notabnormals = getNotabnormalVariables();
		for (int i = 0; i < notabnormals.length; i++)
			notabnormals[i] = ctx.MkNot(notabnormals[i]);
		return notabnormals;
	}
    
    /**
	 * @return Cell to cell variable mapping
	 */
	public Map<Cell, Expr> getVariables() {
		return variables;
	}
    
    /**
	 * Returns an if-then-else structure which determines the weight of a given
	 * boolean expression. If the expression is true weight will return 1, 0
	 * otherwise.
	 * 
	 * @param b
	 *            Expression for which the weight function should be applied
	 * @return Weight function with sort Int
	 * @throws Z3Exception
	 */
	public IntExpr getWeight(BoolExpr b) throws Z3Exception {
		return (IntExpr) ctx.MkITE(b, ctx.MkInt(1), ctx.MkInt(0));
	}
    
    /**
	 * "Casts" a given expression to a certain sort type
	 * 
	 * @param expr
	 *            Expression which should be "casted"
	 * @param sortType
	 *            Target sort type
	 * @return "Casted" expression
	 * @throws Z3Exception
	 */
	public Expr toSortType(Expr expr, SMTCodeGenerator.SortType sortType) throws Z3Exception {
		if (sortType == SMTCodeGenerator.SortType.Bool && expr instanceof BoolExpr
				|| sortType == SMTCodeGenerator.SortType.Int && expr instanceof IntExpr
				|| sortType == SMTCodeGenerator.SortType.Real && expr instanceof RealExpr)
			return expr;

		if (sortType == SMTCodeGenerator.SortType.Bool) {
			// convert back to bool from int or real
			// Expr numberOne = null;
			Expr numberZero = null;

			if (expr instanceof IntExpr) {
				numberZero = ctx.MkInt(0);
				// numberOne = ctx.MkInt(1);
			} else if (expr instanceof RealExpr) {
				numberZero = ctx.MkReal(0);
				// numberOne = ctx.MkReal(1);
			} else
				throw new RuntimeException(
						String.format(
								"Cannot convert expression of type '%s' to sort type '%s'",
								expr.getClass().getName(), sortType.toString()));
			return ctx.MkNot(ctx.MkEq(expr, numberZero));
		}

		if (expr instanceof BoolExpr) {
			Expr numberOne = sortType == SMTCodeGenerator.SortType.Int ? ctx.MkInt(1) : ctx
					.MkReal(1);
			Expr numberZero = sortType == SMTCodeGenerator.SortType.Int ? ctx.MkInt(0) : ctx
					.MkReal(0);
			BoolExpr boolExpr = (BoolExpr) expr;
			return ctx.MkITE(boolExpr, numberOne, numberZero);
		} else if (expr instanceof IntExpr) {
			return ctx.MkInt2Real((IntExpr) expr);
		} else if (expr instanceof RealExpr) {
			if (sortType == SMTCodeGenerator.SortType.Int)
				return ctx.MkReal2Int((RealExpr) expr);
		}

		throw new RuntimeException(String.format(
				"Cannot convert expression of type '%s' to sort type '%s'",
				expr.getClass().getName(), sortType.toString()));
	}
    
    @Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Z3 Constraint Strategy Generation Information\n");
		s.append(String.format("Amount of cell variables:     %d\n",
				variables.size()));
		s.append(String.format("Amount of notabnormal variables: %d\n",
				notabnormalVariablesMap.size()));
		return s.toString();
	}
}
