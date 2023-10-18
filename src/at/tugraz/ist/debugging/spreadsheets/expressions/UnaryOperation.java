package at.tugraz.ist.debugging.spreadsheets.expressions;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import choco.Choco;
import choco.IPretty;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.formula.ptg.UnaryMinusPtg;
import org.apache.poi.ss.formula.ptg.UnaryPlusPtg;
import org.apache.poi.ss.formula.ptg.ValueOperatorPtg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents an arithmetic unary operation (+/-)
 * 
 */
public class UnaryOperation implements IConstraintExpression {
	/**
	 * Supported unary operations
	 * 
	 */
	public enum UnaryOperator {
		UnaryMinus, UnaryPlus
	}

	/**
	 * Ptg to supported unary operations mapping
	 */
	public static Map<Class<?>, UnaryOperator> PtgToUnaryOperator = new HashMap<Class<?>, UnaryOperator>() {
		private static final long serialVersionUID = -5828575049228146744L;

		{
			put(UnaryMinusPtg.class, UnaryOperator.UnaryMinus);
			put(UnaryPlusPtg.class, UnaryOperator.UnaryPlus);
		}
	};

	private IConstraintExpression operand;
	private UnaryOperator operator;

	public UnaryOperation(IConstraintExpression operand,
			Class<? extends ValueOperatorPtg> operatorClass) {
		this(operand, PtgToUnaryOperator.get(operatorClass));
	}

	public UnaryOperation(IConstraintExpression operand, UnaryOperator operator) {
		this.operand = operand;
		this.operator = operator;
	}

	@Override
	public Object evaluate() {
		Object res = operand.evaluate();
//		System.out.println(operand.toString());
//		if(operand.toString().contains("Sheet6!C5"))
//			System.out.println("was here");
		if (res instanceof Integer) {
			Integer intRes = (Integer) res;
			return this.operator == UnaryOperator.UnaryMinus ? -intRes : intRes;
		} else if (res instanceof Double) {
			Double dRes = (Double) res;
			return this.operator == UnaryOperator.UnaryMinus ? -dRes : dRes;
		} else if (res instanceof Boolean) {
			Boolean boolRes = (Boolean) res;
			Integer intRes = boolRes ? 1 : 0;
			if (this.operator == UnaryOperator.UnaryMinus)
				return -intRes;
			return boolRes;
		}
		throw new EvaluationException(
				"Cannot evaluate unary operation because of invalid operand");
	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		return operand.getReferencedCells(dynamic, faultyConst);
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		IPretty res = operand.getChocoConstraint(info);

		if (this.operator == UnaryOperator.UnaryPlus)
			return res;

		if (!(res instanceof IntegerExpressionVariable))
			throw new InvalidOperationException(
					"Unary operation for a non-integer operand is not supported");

		return info.addAuxIntegerConstraint(Choco
				.neg((IntegerExpressionVariable) res));

	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		return operand.getConditionalExpressions();
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		MinionExpressionConstraints constraints = operand
				.getMinionValueConstraints(info);

		if (this.operator == UnaryOperator.UnaryPlus)
			return constraints;

		if (!(constraints.getResultType() == Domain.INTEGER))
			throw new InvalidOperationException(
					"Unary operation for a non-integer operand is not supported");

		String auxVar = info.getNextAuxiliaryVariable(Domain.INTEGER);
		MinionExpressionConstraints constraints2 = MinionConstraints
				.getMULTConstraint(constraints.getVarname(), "-1", auxVar);
		constraints2.addConstraints(constraints);
		return constraints2;
	}

	@Override
	public int getNumberOperations() {
		return 1 + operand.getNumberOperations();
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		return operand.getReferences(false);
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		String res = operand.getSMTConstraint(info);
		return this.operator == UnaryOperator.UnaryPlus ? res : info
				.getCodeGenerator().minus(res);
	}

	@Override
	public Expr getZ3Constraint(Z3ConstraintStrategyGenerationInformation info)
			throws Z3Exception {
		Expr res = operand.getZ3Constraint(info);

		if (this.operator == UnaryOperator.UnaryPlus)
			return res;

		if (res instanceof ArithExpr)
			return info.getContext().MkSub(new ArithExpr[] { (ArithExpr) res });
		else if (res instanceof BoolExpr)
			return info.getContext().MkNot((BoolExpr) res);
		throw new RuntimeException(
				"Cannot apply unary minus operator to non-arith / non-bool operand");
	}

	@Override
	public Boolean isEquivalencePossible() {
		return operand.isEquivalencePossible();
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
		return operator.toString() + operand;
	}

}
