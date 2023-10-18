package at.tugraz.ist.debugging.spreadsheets.expressions;

import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator.SortType;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.ExpressionReturnType;
import choco.Choco;
import choco.IPretty;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * Class for binary relational operations
 * 
 * 
 */
class BinaryBoolOperation extends BinaryOperation {
	/**
	 * Supported types of operands
	 */
	private static List<Class<?>> SupportedTypes = new ArrayList<Class<?>>() {
		private static final long serialVersionUID = 2622947423370096707L;
		{
			add(Double.class);
			add(Integer.class);
			add(Boolean.class);
			add(String.class);
		}
	};

	public BinaryBoolOperation(IConstraintExpression operand1,
			IConstraintExpression operand2, BinaryOperator operator) {
		super(operand1, operand2, operator);
	}

	@Override
	public Boolean evaluate() {
		Object res1 = this.operand1.evaluate();
		Object res2 = this.operand2.evaluate();

		if (res1 == null)
			res1 = 0;
		if (res2 == null)
			res2 = 0;

		if (!SupportedTypes.contains(res1.getClass())
				|| !SupportedTypes.contains(res2.getClass()))
			throw new InvalidOperationException(
					"Cannot determine result of relational expression because of invalid operand type");
		
        if(res1 instanceof String)
            res1 = res1.toString().toLowerCase();
        if(res2 instanceof String)
            res2 = res2.toString().toLowerCase();

        
		switch (this.operator) {
		case Equal:
			return res1.equals(res2);
		case NotEqual:
			return !res1.equals(res2);
		default:
			break;
		}

		Integer intRes1 = null;
		Integer intRes2 = null;
		Double dRes1 = null;
		Double dRes2 = null;

		if (res1 instanceof Integer)
			intRes1 = (Integer) res1;
		else if (res1 instanceof Double)
			dRes1 = (Double) res1;

		if (res2 instanceof Integer)
			intRes2 = (Integer) res2;
		else if (res2 instanceof Double)
			dRes2 = (Double) res2;

		if (res1 instanceof Boolean && res2 instanceof Boolean) {
			intRes1 = (Boolean) res1 ? 1 : 0;
			intRes2 = (Boolean) res2 ? 1 : 0;
		} else if (res1 instanceof Boolean) {
			boolean value = (Boolean) res1;
			return (value && operator == BinaryOperator.GreaterEqual || operator == BinaryOperator.GreaterThan);
		} else if (res2 instanceof Boolean) {
			boolean value = (Boolean) res2;
			return (!value && operator == BinaryOperator.LessEqual || operator == BinaryOperator.LessThan);
		}
		ExpressionReturnType operandTypes = (intRes1 == null || intRes2 == null) ? ExpressionReturnType.Double
				: ExpressionReturnType.Integer;

		if (operandTypes == ExpressionReturnType.Double) {
			dRes1 = dRes1 != null ? dRes1 : intRes1;
			dRes2 = dRes2 != null ? dRes2 : intRes2;
		}

		switch (this.operator) {
		case GreaterEqual:
			if (operandTypes == ExpressionReturnType.Integer)
				return intRes1 >= intRes2;
			return dRes1 >= dRes2;
		case GreaterThan:
			if (operandTypes == ExpressionReturnType.Integer)
				return intRes1 > intRes2;
			return dRes1 > dRes2;
		case LessEqual:
			if (operandTypes == ExpressionReturnType.Integer)
				return intRes1 <= intRes2;
			return dRes1 <= dRes2;
		case LessThan:
			if (operandTypes == ExpressionReturnType.Integer)
				return intRes1 < intRes2;
			return dRes1 < dRes2;
		default:
			throw new InvalidOperationException(
					"Invalid compare operation requested");
		}

	}

	@Override
	public IntegerVariable getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		IPretty opConstraint1 = operand1.getChocoConstraint(info);
		IPretty opConstraint2 = operand2.getChocoConstraint(info);

		if (!(opConstraint1 instanceof IntegerExpressionVariable)
				|| !(opConstraint2 instanceof IntegerExpressionVariable))
			throw new InvalidOperationException(
					"Cannot get binary operation constraint for non-integer operands");

		IntegerExpressionVariable intConstraint1 = (IntegerExpressionVariable) opConstraint1;
		IntegerExpressionVariable intConstraint2 = (IntegerExpressionVariable) opConstraint2;
		switch (this.operator) {
		case GreaterEqual:
			return info.addBoolAuxConstraint(Choco.geq(intConstraint1,
					intConstraint2));
		case GreaterThan:
			return info.addBoolAuxConstraint(Choco.gt(intConstraint1,
					intConstraint2));
		case LessEqual:
			return info.addBoolAuxConstraint(Choco.leq(intConstraint1,
					intConstraint2));
		case LessThan:
			return info.addBoolAuxConstraint(Choco.lt(intConstraint1,
					intConstraint2));
		case Equal:
			return info.addBoolAuxConstraint(Choco.eq(intConstraint1,
					intConstraint2));
		case NotEqual:
			return info.addBoolAuxConstraint(Choco.neq(intConstraint1,
					intConstraint2));
			// case Percent:
			// break;
		default:
			throw new EvaluationException("Unknown operation");
		}
	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		return new HashSet<IConstraintExpression>();
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		MinionExpressionConstraints constraints1 = operand1
				.getMinionValueConstraints(info);
		MinionExpressionConstraints constraints2 = operand2
				.getMinionValueConstraints(info);

		String resultVarName = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
		MinionExpressionConstraints newConstraint = null;
		
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			/**
			 * @author inica 2022:
			 **/
			switch (this.operator) {
			case Equal: case GreaterEqual: case GreaterThan: case LessEqual: case LessThan: case NotEqual:
				newConstraint = MinionConstraints.getRelOperatorsTableConstraints(info.getAbnormalIndex(),
						        constraints1.getVarname(), constraints2.getVarname(),
						        resultVarName);
				break;
			default:
				throw new InvalidOperationException("Unknown operation");
			}
			
		}
		else
			if (info.getModelGranularity() == EModelGranularity.Dependency) {
				/**
				 * @author inica 2022:
				 **/
				switch (this.operator) {
				case Equal: case GreaterEqual: case GreaterThan: case LessEqual: case LessThan: case NotEqual:
					newConstraint = MinionConstraints.getMultRelationalOpMinMaxTableConstraints(info.getAbnormalIndex(),
							        constraints1.getVarname(), constraints2.getVarname(),
							        resultVarName);
					break;
				default:
					throw new InvalidOperationException("Unknown operation");
				}
				
			}
			else
		switch (this.operator) {
		case Equal:
			newConstraint = MinionConstraints.getEqualReifyConstraint(
					constraints1.getVarname(), constraints2.getVarname(),
					resultVarName);
			break;
		case GreaterEqual:
			newConstraint = MinionConstraints.getGREATEREQUALConstraint(
					constraints1.getVarname(), constraints2.getVarname(),
					resultVarName);
			break;
		case GreaterThan:
			newConstraint = MinionConstraints.getGREATERConstraint(
					constraints1.getVarname(), constraints2.getVarname(),
					resultVarName);
			break;
		case LessEqual:
			newConstraint = MinionConstraints.getLESSEQUALConstraint(
					constraints1.getVarname(), constraints2.getVarname(),
					resultVarName);
			break;
		case LessThan:
			newConstraint = MinionConstraints.getLESSConstraint(
					constraints1.getVarname(), constraints2.getVarname(),
					resultVarName);
			break;
		case NotEqual:
			newConstraint = MinionConstraints.getNotEqualRefeiyConstraint(
					constraints1.getVarname(), constraints2.getVarname(),
					resultVarName);
			break;
		default:
			throw new InvalidOperationException("Unknown operation");
		}

		newConstraint.addConstraints(constraints1);
		newConstraint.addConstraints(constraints2);
		return newConstraint;
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		String opConstraint1 = operand1.getSMTConstraint(info);
		String opConstraint2 = operand2.getSMTConstraint(info);

		// result will be bool, thus the types of operand1 and operand 2 must
		// fit
		SortType type1 = info.determineSort(operand1.evaluate());
		SortType type2 = info.determineSort(operand2.evaluate());
		SortType sortType = info.determineMostGeneralSort(type1, type2);

		opConstraint1 = info.getCodeGenerator().toSortType(opConstraint1,
				type1, sortType);
		opConstraint2 = info.getCodeGenerator().toSortType(opConstraint2,
				type2, sortType);

		switch (this.operator) {
		case Equal:
			return info.getCodeGenerator().equal(opConstraint1, opConstraint2);

		case GreaterEqual:
			return info.getCodeGenerator().geq(opConstraint1, opConstraint2);
		case GreaterThan:
			return info.getCodeGenerator().gt(opConstraint1, opConstraint2);
		case LessEqual:
			return info.getCodeGenerator().leq(opConstraint1, opConstraint2);
		case LessThan:
			return info.getCodeGenerator().lt(opConstraint1, opConstraint2);
		case NotEqual:
			return info.getCodeGenerator().notEqual(opConstraint1,
					opConstraint2);
			// case Percent:
			// break;
		default:
			throw new InvalidOperationException("Unknown operation");
		}
	}

	@Override
	public BoolExpr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info) 
            throws Z3Exception {
		Expr expr1 = operand1.getZ3Constraint(info);
		Expr expr2 = operand2.getZ3Constraint(info);

		// result will be bool, thus the types of operand1 and operand 2 must
		// fit
		SortType sortType = info.determineMostGeneralSort(expr1, expr2);

		expr1 = info.toSortType(expr1, sortType);
		expr2 = info.toSortType(expr2, sortType);

		boolean isArith = (expr1 instanceof ArithExpr && expr2 instanceof ArithExpr);

		switch (this.operator) {
		case Equal:
			return info.getContext().MkEq(expr1, expr2);
		case GreaterEqual:
			if (!isArith)
				throw new RuntimeException(
						"Cannot apply gt to non-arithmetic expressions");
			return info.getContext().MkGe((ArithExpr) expr1, (ArithExpr) expr2);
		case GreaterThan:
			if (!isArith)
				throw new RuntimeException(
						"Cannot apply gt to non-arithmetic expressions");
			return info.getContext().MkGt((ArithExpr) expr1, (ArithExpr) expr2);
		case LessEqual:
			if (!isArith)
				throw new RuntimeException(
						"Cannot apply gt to non-arithmetic expressions");
			return info.getContext().MkLe((ArithExpr) expr1, (ArithExpr) expr2);
		case LessThan:
			if (!isArith)
				throw new RuntimeException(
						"Cannot apply gt to non-arithmetic expressions");
			return info.getContext().MkLt((ArithExpr) expr1, (ArithExpr) expr2);
		case NotEqual:
			return info.getContext()
					.MkNot(info.getContext().MkEq(expr1, expr2));
			// case Percent:
			// break;
		default:
			throw new InvalidOperationException("Unknown operation");
		}
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

}