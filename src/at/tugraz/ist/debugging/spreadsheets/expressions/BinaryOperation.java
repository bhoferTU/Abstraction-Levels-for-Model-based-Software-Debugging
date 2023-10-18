package at.tugraz.ist.debugging.spreadsheets.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.formula.ptg.AddPtg;
import org.apache.poi.ss.formula.ptg.ConcatPtg;
import org.apache.poi.ss.formula.ptg.DividePtg;
import org.apache.poi.ss.formula.ptg.EqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterEqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterThanPtg;
import org.apache.poi.ss.formula.ptg.LessEqualPtg;
import org.apache.poi.ss.formula.ptg.LessThanPtg;
import org.apache.poi.ss.formula.ptg.MultiplyPtg;
import org.apache.poi.ss.formula.ptg.NotEqualPtg;
import org.apache.poi.ss.formula.ptg.PowerPtg;
import org.apache.poi.ss.formula.ptg.SubtractPtg;
import org.apache.poi.ss.formula.ptg.ValueOperatorPtg;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;

/**
 * Abstract class for binary operations
 * 
 */
public abstract class BinaryOperation extends Expression {
	/**
	 * Supported binary operators
	 */
	public enum BinaryOperator {
		Add, Concat, Divide, Equal, GreaterEqual, GreaterThan, LessEqual, LessThan, Multiply, NotEqual, Percent, Power, Subtract
	}
    
	/**
	 * Relational binary operators
	 */
	public static List<BinaryOperator> BoolBinaryOperators = new ArrayList<BinaryOperator>() {
		private static final long serialVersionUID = -788812040330224628L;
		{
			add(BinaryOperator.Equal);
			add(BinaryOperator.GreaterEqual);
			add(BinaryOperator.GreaterThan);
			add(BinaryOperator.LessEqual);
			add(BinaryOperator.LessThan);
			add(BinaryOperator.NotEqual);
		}
	};

	/**
	 * Ptg class to binary operator mapping
	 */
	public static Map<Class<?>, BinaryOperator> PtgToBinaryOperator = new HashMap<Class<?>, BinaryOperator>() {
		private static final long serialVersionUID = -5828575049228146743L;
		{
			put(AddPtg.class, BinaryOperator.Add);
			put(ConcatPtg.class, BinaryOperator.Concat);
			put(DividePtg.class, BinaryOperator.Divide);
			put(EqualPtg.class, BinaryOperator.Equal);
			put(GreaterEqualPtg.class, BinaryOperator.GreaterEqual);
			put(GreaterThanPtg.class, BinaryOperator.GreaterThan);
			put(LessEqualPtg.class, BinaryOperator.LessEqual);
			put(LessThanPtg.class, BinaryOperator.LessThan);
			put(MultiplyPtg.class, BinaryOperator.Multiply);
			put(NotEqualPtg.class, BinaryOperator.NotEqual);
//			put(PercentPtg.class, BinaryOperator.Percent); // Hobi: this is not a Binary oberator - it requires only one operand! e.g. 22%
			put(PowerPtg.class, BinaryOperator.Power);
			put(SubtractPtg.class, BinaryOperator.Subtract);
		}
	};

    /**
	 * First operand
	 */
	protected IConstraintExpression operand1;

	/**
	 * Second operand
	 */
	protected IConstraintExpression operand2;

	/**
	 * Operator
	 */
	protected BinaryOperator operator;

    
	/**
	 * 
	 * @param operand1
	 * @param operand2
	 * @param operator
	 */
	protected BinaryOperation(IConstraintExpression operand1,
			IConstraintExpression operand2, BinaryOperator operator) {
		super(operand1, operand2);
        this.operand1 = operand1;
        this.operand2 = operand2;
		this.operator = operator;
	}
    
	/**
	 * Factory method for binary operations
	 * 
	 * @param operand1
	 * @param operand2
	 * @param operator
	 * @return
	 */
	public static BinaryOperation Create(IConstraintExpression operand1,
			IConstraintExpression operand2, BinaryOperator operator) {
		if (BoolBinaryOperators.contains(operator)) {
			return new BinaryBoolOperation(operand1, operand2, operator);
		} else {
			return new BinaryNumericOperation(operand1, operand2, operator);
		}
	}

	/**
	 * Factory method for binary operations
	 * 
	 * @param operand2
	 * @param operand1
	 * @param operatorClass
	 *            Represents the type of operation
	 * @return
	 */
	public static BinaryOperation Create(IConstraintExpression operand2,
			IConstraintExpression operand1,
			Class<? extends ValueOperatorPtg> operatorClass) {
		BinaryOperator operator = PtgToBinaryOperator.get(operatorClass);
		return Create(operand1, operand2, operator);

	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		Set<Cell> cells = new HashSet<Cell>();
		cells.addAll(operand1.getReferencedCells(dynamic, faultyConst));
		cells.addAll(operand2.getReferencedCells(dynamic, faultyConst));
		return cells;
	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		Set<IConstraintExpression> conditionalExpressions = new HashSet<IConstraintExpression>();
		conditionalExpressions.addAll(operand1.getConditionalExpressions());
		conditionalExpressions.addAll(operand2.getConditionalExpressions());
		return conditionalExpressions;
	}

	@Override
	public int getNumberOperations() {
		return 1 + operand1.getNumberOperations()
				+ operand2.getNumberOperations();
	}
    
    public IConstraintExpression[] getOperands()
    {
        IConstraintExpression[] operands = new IConstraintExpression[2];
        operands[0] = operand1;
        operands[1] = operand2;
        return operands;
    }
    
    public BinaryOperator getOperator() { return operator; }

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		Set<Coords> cells = new HashSet<Coords>();
		cells.addAll(operand1.getReferences(false));
		cells.addAll(operand2.getReferences(false));
		return cells;
	}
	
	String formula;
	
	public void setStringExpression(String expression)
	{
		this.formula = expression;
	}
	
	public String getFormula()
	{
		return this.formula;
	}

	@Override
	public String toString() {
		return "(" + operand1 + " " + operator + " " + operand2 + ")";
	}

}
