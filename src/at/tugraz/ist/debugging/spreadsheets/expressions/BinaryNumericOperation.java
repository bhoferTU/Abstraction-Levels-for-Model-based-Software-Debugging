package at.tugraz.ist.debugging.spreadsheets.expressions;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator.SortType;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.ExpressionReturnType;
import at.tugraz.ist.util.MathUtils;
import choco.Choco;
import choco.IPretty;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Class for binary arithmetic operations
 * 
 */
class BinaryNumericOperation extends BinaryOperation {
	private static List<Class<?>> SupportedTypes = new ArrayList<Class<?>>() {
		private static final long serialVersionUID = -141214533758314659L;

		{
			add(Double.class);
			add(Integer.class);
			add(Boolean.class);
		}
	};

	public BinaryNumericOperation(IConstraintExpression operand1, IConstraintExpression operand2,
			BinaryOperator operator) {
		super(operand1, operand2, operator);
	}

	@Override
	public Object evaluate() {
		Object res1 = null;
		Object res2 = null;
		Integer intRes1 = null;
		Integer intRes2 = null;
		Double dRes1 = null;
		Double dRes2 = null;
		ExpressionReturnType returnType = ExpressionReturnType.Integer;

		try {
			res1 = this.operand1.evaluate();
			res2 = this.operand2.evaluate();

			if (res1 != null && !SupportedTypes.contains(res1.getClass())
					|| res2 != null && !SupportedTypes.contains(res2.getClass()))
				throw new EvaluationException("Cannot evaluate binary operation because of invalid operand type");

			if (res1 instanceof Double || res2 instanceof Double)
				returnType = ExpressionReturnType.Double;

			if (res1 instanceof Integer || res1 instanceof Boolean) {
				intRes1 = (res1 instanceof Boolean) ? ((Boolean) res1 ? 1 : 0) : (Integer) res1;
			} else if (res1 instanceof Double)
				dRes1 = (Double) res1;
			if (res2 instanceof Integer || res2 instanceof Boolean) {
				intRes2 = (res2 instanceof Boolean) ? ((Boolean) res2 ? 1 : 0) : (Integer) res2;
			} else if (res2 instanceof Double)
				dRes2 = (Double) res2;

			if (returnType == ExpressionReturnType.Integer && (intRes1 == null || intRes2 == null))
				return intRes1 != null ? intRes1 : intRes2 != null ? intRes2 : 0;

			if (returnType == ExpressionReturnType.Double) {
				dRes1 = dRes1 != null ? dRes1 : (intRes1 != null ? intRes1 : 0);
				dRes2 = dRes2 != null ? dRes2 : (intRes2 != null ? intRes2 : 0);
			}

			if (returnType == ExpressionReturnType.Double && (dRes1 == null || dRes2 == null))
				throw new EvaluationException("Invalid operation");

			switch (this.operator) {
			case Add:
				if (returnType == ExpressionReturnType.Integer)
					return intRes1 + intRes2;
				return dRes1 + dRes2;
			case Concat:
				throw new InvalidOperationException("Concat is invalid operation for integer operands");
			case Divide:
				return  MathUtils.Divide(res1, res2);
			case Multiply:
				if (returnType == ExpressionReturnType.Integer)
					return intRes1 * intRes2;
				return dRes1 * dRes2;
			case Percent:
				// RESTRICTION: not supported
				throw new RuntimeException("operation percent not implemented yet");
			case Power:
				if (returnType == ExpressionReturnType.Integer)
					return (int) Math.pow(intRes1, intRes2);
				return Math.pow(dRes1, dRes2);
			case Subtract:
				if (returnType == ExpressionReturnType.Integer)
					return intRes1 - intRes2;
				return dRes1 - dRes2;
			default:
				break;

			}
		} catch (Exception e) {
			String msg = "Error during evaluation (" + e.getMessage() + ")";
			if (res1 != null)
				msg += "\n res1 = " + res1 + " (" + res1.getClass() + ")";
			if (res2 != null)
				msg += "\n res2 = " + res2 + " (" + res2.getClass() + ")";
			if (intRes1 != null)
				msg += "\n intRes1 = " + intRes1 + " (" + intRes1.getClass() + ")";
			if (intRes2 != null)
				msg += "\n intRes2 = " + intRes2 + " (" + intRes2.getClass() + ")";
			if (dRes1 != null)
				msg += "\n dRes1 = " + dRes1 + " (" + dRes1.getClass() + ")";
			if (dRes2 != null)
				msg += "\n dRes2 = " + dRes2 + " (" + dRes2.getClass() + ")";
			if (returnType != null)
				msg += "\n returnType = " + returnType;
			throw new EvaluationException(msg, e);
		}
		throw new InvalidOperationException("Could not calculate binary operation");
	}

	@Override
	public IPretty getChocoConstraint(ChocoConstraintStrategyGenerationInformation info) {

		IPretty opConstraint1 = operand1.getChocoConstraint(info);
		IPretty opConstraint2 = operand2.getChocoConstraint(info);

		if (!(opConstraint1 instanceof IntegerExpressionVariable
				|| !(opConstraint2 instanceof IntegerExpressionVariable)))
			throw new InvalidOperationException(
					"Cannot create constraint for an arithmetic binary operation where at least one operand is no integer");

		IntegerExpressionVariable intConstraint1 = (IntegerExpressionVariable) opConstraint1;
		IntegerExpressionVariable intConstraint2 = (IntegerExpressionVariable) opConstraint2;

		IntegerExpressionVariable res = null;

		switch (this.operator) {
		case Add:
			res = Choco.plus(intConstraint1, intConstraint2);
			break;
		case Concat:
			throw new InvalidOperationException("Concat is invalid operation for integer operands");
		case Divide:
			return info.addAuxIntDiv(intConstraint1, intConstraint2);
		// res = Choco.div(intConstraint1, intConstraint2);
		case Multiply:
			res = Choco.mult(intConstraint1, intConstraint2);
			break;
		case Percent:
			// RESTRICTION: not supported
			throw new RuntimeException("operation percent not implemented yet");
		case Power:
			res = Choco.power(intConstraint1, intConstraint2);
			break;
		case Subtract:
			res = Choco.minus(intConstraint1, intConstraint2);
			break;
		default:
			throw new InvalidOperationException("Unknown binary operation");
		}

		return info.addAuxIntegerConstraint(res);
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(MinionConstraintStrategyGenerationInformation info) {
		MinionExpressionConstraints constraints1 = operand1.getMinionValueConstraints(info);
		MinionExpressionConstraints constraints2 = operand2.getMinionValueConstraints(info);

		String resultVarName = info.getNextAuxiliaryVariable(constraints1.getResultType());
		MinionExpressionConstraints newConstraint = null;

		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			switch(this.operator){
			case Add:
			case Multiply:
				newConstraint = MinionConstraints.getPLUSMULTTableConstraints(info.getAbnormalIndex(),constraints1.getVarname(), constraints2.getVarname(),
						resultVarName);
				break;
			case Divide:
			case Subtract:
				newConstraint = MinionConstraints.getMINUSDIVTableConstraints(info.getAbnormalIndex(),constraints1.getVarname(), constraints2.getVarname(),
						resultVarName);
				break;
			default:
				throw new InvalidOperationException("Unknown binary operation");
			}
		}else
			if (info.getModelGranularity() == EModelGranularity.Dependency) {
				switch(this.operator){
				case Add:
				case Subtract:
					newConstraint = MinionConstraints.getPlusMinusTableConstraints(info.getAbnormalIndex(),constraints1.getVarname(), constraints2.getVarname(),
							resultVarName);
					break;
				case Divide:
					newConstraint = MinionConstraints.getDivTableConstraints(info.getAbnormalIndex(),constraints1.getVarname(), constraints2.getVarname(),
							resultVarName);
					break;
				case Multiply:
					newConstraint = MinionConstraints.getMultRelationalOpMinMaxTableConstraints(info.getAbnormalIndex(),constraints1.getVarname(), constraints2.getVarname(),
							resultVarName);
					break;
				default:
					throw new InvalidOperationException("Unknown binary operation");
				}
			}
		else 
		{
			switch (this.operator) {
			case Add:
				newConstraint = MinionConstraints.getADDConstraint(constraints1.getVarname(), constraints2.getVarname(),
						resultVarName);
				break;
			case Concat:
				throw new InvalidOperationException("Concat is invalid operation for integer operands");
			case Divide:
				newConstraint = MinionConstraints.getDIVConstraint(constraints1.getVarname(), constraints2.getVarname(),
						resultVarName);
				break;
			case Multiply:
				newConstraint = MinionConstraints.getMULTConstraint(constraints1.getVarname(),
						constraints2.getVarname(), resultVarName);
				break;
			case Percent:
				// RESTRICTION: not supported
				throw new RuntimeException("operation percent not implemented yet");
			case Power:
				newConstraint = MinionConstraints.getPOWConstraint(constraints1.getVarname(), constraints2.getVarname(),
						resultVarName);
				break;
			case Subtract:
				newConstraint = MinionConstraints.getMINUSConstraint(constraints1.getVarname(),
						constraints2.getVarname(), resultVarName);
				break;
			default:
				throw new InvalidOperationException("Unknown binary operation");
			}
		}

		newConstraint.addConstraints(constraints1);
		newConstraint.addConstraints(constraints2);
		return newConstraint;

	}

	@Override
	public String getSMTConstraint(SMTConstraintStrategyGenerationInformation info) {

		String opConstraint1 = operand1.getSMTConstraint(info);
		String opConstraint2 = operand2.getSMTConstraint(info);

		SortType sortType = info.determineSort(evaluate());
		opConstraint1 = info.getCodeGenerator().toSortType(opConstraint1, info.determineSort(operand1.evaluate()),
				sortType);
		opConstraint2 = info.getCodeGenerator().toSortType(opConstraint2, info.determineSort(operand2.evaluate()),
				sortType);

		switch (this.operator) {
		case Add:
			return info.getCodeGenerator().plus(opConstraint1, opConstraint2);
		case Concat:
			throw new InvalidOperationException("Concat is invalid operation for integer operands");
		case Divide:
			return info.getCodeGenerator().div(opConstraint1, opConstraint2);
		case Multiply:
			return info.getCodeGenerator().mult(opConstraint1, opConstraint2);
		case Percent:
			// RESTRICTION: not supported
			throw new RuntimeException("operation percent not implemented yet");
		case Power:
			return info.getCodeGenerator().power(opConstraint1, opConstraint2);
		case Subtract:
			return info.getCodeGenerator().minus(opConstraint1, opConstraint2);
		default:
			throw new InvalidOperationException("Unknown binary operation");
		}
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info) throws Z3Exception {

		Expr opConstraint1 = operand1.getZ3Constraint(info);
		Expr opConstraint2 = operand2.getZ3Constraint(info);

		SortType sortType = info.determineSort(evaluate());
		opConstraint1 = info.toSortType(opConstraint1, sortType);
		opConstraint2 = info.toSortType(opConstraint2, sortType);

		if (!(opConstraint1 instanceof ArithExpr) || !(opConstraint2 instanceof ArithExpr))
			throw new RuntimeException("At least one item is not an arithmetic expression");

		ArithExpr arithExpr1 = (ArithExpr) opConstraint1;
		ArithExpr arithExpr2 = (ArithExpr) opConstraint2;
		switch (this.operator) {
		case Add:
			return info.getContext().MkAdd(new ArithExpr[] { arithExpr1, arithExpr2 });
		case Concat:
			throw new InvalidOperationException("Concat is invalid operation for integer operands");
		case Divide:
			/*
			 * info.addAssert(info.getContext().MkNot(
			 * info.getContext().MkEq(arithExpr2, info.getContext().MkInt(0))));
			 */
			return info.getContext().MkDiv(arithExpr1, arithExpr2);
		case Multiply:
			return info.getContext().MkMul(new ArithExpr[] { arithExpr1, arithExpr2 });
		case Percent:
			// RESTRICTION: not supported
			throw new RuntimeException("operation percent not implemented yet");
		case Power:
			return info.getContext().MkPower(arithExpr1, arithExpr2);
		case Subtract:
			return info.getContext().MkSub(new ArithExpr[] { arithExpr1, arithExpr2 });
		default:
			throw new InvalidOperationException("Unknown binary operation");
		}
	}

	@Override
	public Boolean isEquivalencePossible() {
		if (!operand1.isEquivalencePossible() && !operand2.isEquivalencePossible())
			return false;

		switch (this.operator) {
		case Add:
		case Divide:
		case Subtract:
			return true;

		case Multiply:
		case Power:
			Object result1 = operand1.evaluate();
			Object result2 = operand2.evaluate();
			if (result1 instanceof Integer && (Integer) result1 == 0
					|| result1 instanceof Double && (Double) result1 == 0.0)
				return false;
			if (result2 instanceof Integer && (Integer) result2 == 0
					|| result2 instanceof Double && (Double) result2 == 0.0)
				return false;
			if (this.operator == BinaryOperator.Power && result1 instanceof Integer && (Integer) result1 == 1
					|| result1 instanceof Double && (Double) result1 == 1.0)
				return false;
			return true;

		case Percent:
		case Concat:
			return false;
		default:
			return false;
		}
	}
}