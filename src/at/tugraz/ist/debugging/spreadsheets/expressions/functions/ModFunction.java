package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import at.tugraz.ist.debugging.modelbased.Cell;

import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import choco.IPretty;

import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Implementation of the Excel MOD function
 * 
 */
class ModFunction extends ShellFunction {
	IConstraintExpression devisor;
	IConstraintExpression divident;

	public ModFunction(IConstraintExpression divident,
			IConstraintExpression devisor) {
		super(Function.MOD_FUNCTION_NAME, new IConstraintExpression[] { divident, devisor });
		this.divident = divident;
		this.devisor = devisor;
	}

	@Override
	public Object evaluate() {

		Object resultDivident = divident.evaluate();
		Object resultDevisor = devisor.evaluate();

		if (resultDivident instanceof Integer && resultDevisor instanceof Integer)
			return (Integer) resultDivident % (Integer) resultDevisor;
        else if(resultDivident instanceof Double && resultDevisor instanceof Double)
        {
            Double doubleDivident = (double)resultDivident;
            Double doubleDevisor = (double)resultDevisor;
            if( Math.floor(doubleDivident) == doubleDivident && 
                Math.floor(doubleDevisor) == doubleDevisor)
                return doubleDivident.intValue() % doubleDevisor.intValue();
        }
        
		throw new RuntimeException(String.format(
					"MOD: cannot determine mod() of type '%s' and '%s'",
					resultDivident.getClass().getName(), resultDevisor
							.getClass().getName()));
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: mod -> get...Constraint not implemented");
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		String auxVar1 = info.getNextAuxiliaryVariable(Domain.INTEGER);

		MinionExpressionConstraints constraints = divident
				.getMinionValueConstraints(info);
		MinionExpressionConstraints constraints0 = devisor
				.getMinionValueConstraints(info);
		MinionExpressionConstraints constraints1 = MinionConstraints
				.getMODConstraint(constraints.getVarname(),
						constraints0.getVarname(), auxVar1);
		constraints1.addConstraints(constraints);
		constraints1.addConstraints(constraints0);
		return constraints1;

	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		//TODO: Implement me
        throw new NotImplementedException("Function: mod -> get...Constraint not implemented");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		List<IntExpr> expressions = new ArrayList<>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) expression).getDirectlyReferencedCells()) {
					Expr reference = info.getVariables().get(cell);
					if (reference instanceof IntExpr)
						expressions.add((IntExpr) reference);
                    else if(reference instanceof RealExpr)
                        expressions.add(info.getContext().MkReal2Int((RealExpr)reference));
                    else 
                        throw new RuntimeException("Z3: Function: mod() only supported for two integer arguments");
				}
			} else {
				Expr reference = expression.getZ3Constraint(info);
				if (reference instanceof IntExpr)
					expressions.add((IntExpr) reference);
                else if(reference instanceof RealExpr)
                        expressions.add(info.getContext().MkReal2Int((RealExpr)reference));
                else 
                    throw new RuntimeException("Z3: Function: mod() only supported for two integer arguments");
			}
		}

        if(expressions.size() != 2)
            throw new RuntimeException("Function: mod() only supported for two arguments");
		return info.getContext().MkMod(expressions.get(0), expressions.get(1));
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

	@Override
	public String toString() {
		IConstraintExpression[] expression = { divident, devisor };
		return super.toString(MOD_FUNCTION_NAME, expression);
	}
}