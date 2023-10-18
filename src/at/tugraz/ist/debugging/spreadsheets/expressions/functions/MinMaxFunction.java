package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
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
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * Implementation of the Excel MIN/MAX function
 * 
 */
class MinMaxFunction extends ShellFunction {
	/**
	 * Supported functions
	 * 
	 */
	public enum FunctionType {
		Max, Min
	}

	private FunctionType functionType;

	public MinMaxFunction(IConstraintExpression[] expressions,
			FunctionType functionType) {
		super(expressions);
		this.functionType = functionType;
        if(this.functionType == FunctionType.Max)
            functionName = Function.MAX_FUNCTION_NAME;
        else
            functionName = Function.MIN_FUNCTION_NAME;
	}

	/**
	 * Compares two given expressions and takes the min/max expression according
	 * to the current instance's function type
	 * 
	 * @param val1
	 * @param val2
	 * @return
	 */
	private IConstraintExpression compareAndTakeAccordingToFunction(
			IConstraintExpression val1, IConstraintExpression val2) {

		if (val1 == null && val2 == null)
			throw new InvalidOperationException(
					"MIN/MAX evaluation: At least expression 2 must not be null");

		IConstraintExpression largerExpr = null;

		Object res2 = val2.evaluate();
		boolean isRes2Valid = (res2 instanceof Integer)
				|| (res2 instanceof Double);
		// check if there is no current min/max expression
		if (val1 == null) {
			return isRes2Valid ? val2 : null;
		}
		Object res1 = val1.evaluate();
		boolean isRes1Valid = (res1 instanceof Integer)
				|| (res1 instanceof Double);

		if (!isRes1Valid && !isRes2Valid)
			return null;
		else if (isRes1Valid && !isRes2Valid)
			return val1;
		else if (!isRes1Valid && isRes2Valid)
			return val2;

		if (res1 instanceof Integer && res2 instanceof Integer) {
			largerExpr = (Integer) res1 > (Integer) res2 ? val1 : val2;
		} else {
			Double dRes1 = (res1 instanceof Integer) ? ((Integer) res1)
					.doubleValue() : (Double) res1;
			Double dRes2 = (res2 instanceof Integer) ? ((Integer) res2)
					.doubleValue() : (Double) res2;
			largerExpr = dRes1 > dRes2 ? val1 : val2;
		}
		return functionType == FunctionType.Max ? largerExpr
				: (val1 == largerExpr) ? val2 : val1;

	}

	@Override
	public Object evaluate() {
		List<IConstraintExpression> exprs = getFlattedExpressions(operands);

		if (exprs.size() == 0)
			return 0;

		IConstraintExpression resultExpression = null;
		for (int i = 0; i < exprs.size(); i++) {
			IConstraintExpression val = exprs.get(i);
			resultExpression = compareAndTakeAccordingToFunction(
					resultExpression, val);
		}

		if (resultExpression == null)
			return 0;

		return resultExpression.evaluate();
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException("Not supported");
	}

	@Override
	/**
	 * Function for generating the MINION constraints representing the MIN and MAX functions in the VALUE based MODEL
	 * **/
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		//  inica:  String auxVar = info.getNextAuxiliaryVariable(Domain.INTEGER);
		//String auxVar = info.getNextAuxiliaryVariable(info.getModelGranularity()==EModelGranularity.Comparison?Domain.INT3:Domain.INTEGER);
		String auxVar;
		MinionExpressionConstraints constraints;
		EModelGranularity granularity = info.getModelGranularity();
		switch (granularity){
		case Dependency: 
			auxVar = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
			constraints = new MinionExpressionConstraints(new HashSet<String>(), auxVar, Domain.BOOLEAN);
			break;
		case Value:
			auxVar= info.getNextAuxiliaryVariable(Domain.INTEGER);
			constraints = new MinionExpressionConstraints(new HashSet<String>(), auxVar, Domain.INTEGER);
			break;
		case Comparison:
			auxVar= info.getNextAuxiliaryVariable(Domain.INT3);
			constraints = new MinionExpressionConstraints(new HashSet<String>(), auxVar, Domain.INT3);
			break;
		default:
			System.err.println("ModelGranularity " + granularity
					+ " not supported!");
			return null;
		}
		//MinionExpressionConstraints constraints = new MinionExpressionConstraints(
		//		new HashSet<String>(), auxVar, info.getModelGranularity()==EModelGranularity.Comparison?Domain.INT3:Domain.INTEGER);
		Set<String> referencedCells = new HashSet<String>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				for (Cell referredCell : ((CellReference) expression)
						.getDirectlyReferencedCells()) {
					referencedCells.add(referredCell.getCoords().getMinionString());
				}
			} else {

				MinionExpressionConstraints constraints1 = expression
						.getMinionValueConstraints(info);
				constraints.addConstraints(constraints1);
				referencedCells.add(constraints1.getVarname());
			}

		}
		MinionExpressionConstraints constraints2;
		if(info.getModelGranularity()==EModelGranularity.Comparison) {
			constraints2=getMinionComparisionConstraints(info,referencedCells);
			constraints2.addConstraints(constraints);
			
		}
		else
		if(info.getModelGranularity()==EModelGranularity.Dependency) {
			constraints2=getMinionDependencyConstraints(info,referencedCells);
			constraints2.addConstraints(constraints);
			
		}
		else{
		        constraints2 = functionType == FunctionType.Min ? MinionConstraints
				.getMINConstraint(referencedCells, auxVar) : MinionConstraints
				.getMAXConstraint(referencedCells, auxVar);
		        constraints2.addConstraints(constraints);
		
		}
		return constraints2;
	}
	/**
	 * MINION Function for the Comparison based MODEL
	 * @author inica
	 * **/
	
	private MinionExpressionConstraints getMinionComparisionConstraints(MinionConstraintStrategyGenerationInformation info, Set<String> cells) {
		Set<String> constraints = new HashSet<String>();
		String[] cells2 = new String[cells.size()];
		cells2 = cells.toArray(cells2);
		for(int i=0;i<cells2.length;i++){
			String cell = cells2[i];
			try{
				Integer.parseInt(cell);
				cells2[i]="1"; // the cell's value is equivalent(0<,1=,2>) 
			}catch(NumberFormatException e){
				// it is a cell reference, not a number
				System.out.println("cell reference in MIN /MAX");
			}
		}
		
		String resultVariable = "";
		String oldResultVariable = cells2[0];
		for(int i=1;i<cells.size();i++){
			resultVariable = info.getNextAuxiliaryVariable(Domain.INT3);
			constraints.addAll(MinionConstraints.getMinMaxTableConstraints(info.getAbnormalIndex(), oldResultVariable, cells2[i], resultVariable).getConstraints());
			oldResultVariable = resultVariable;
		}
					
		return new MinionExpressionConstraints(constraints,	 new HashSet<String>(), oldResultVariable, Domain.INT3); 
		
	}
	
	/**
	 * MINION Function for the Comparison based MODEL
	 * @author inica
	 * **/
	
	private MinionExpressionConstraints getMinionDependencyConstraints(MinionConstraintStrategyGenerationInformation info, Set<String> cells) {
		Set<String> constraints = new HashSet<String>();
		String[] cells2 = new String[cells.size()];
		cells2 = cells.toArray(cells2);
		for(int i=0;i<cells2.length;i++){
			String cell = cells2[i];
			try{
				Integer.parseInt(cell);
				cells2[i]="1"; // the cell's value is correct
			}catch(NumberFormatException e){
				// it is a cell reference, not a number
				System.out.println("cell reference in MIN /MAX");
			}
		}
		
		String resultVariable = "";
		String oldResultVariable = cells2[0];
		for(int i=1;i<cells.size();i++){
			resultVariable = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
			constraints.addAll(MinionConstraints.getMultRelationalOpMinMaxTableConstraints(info.getAbnormalIndex(), oldResultVariable, cells2[i], resultVariable).getConstraints());
			oldResultVariable = resultVariable;
		}
					
		return new MinionExpressionConstraints(constraints,	 new HashSet<String>(), oldResultVariable, Domain.BOOLEAN); 
		
	}
	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		throw new InvalidOperationException("Not supported");
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {
		List<ArithExpr> smtExpressionsList = new ArrayList<ArithExpr>();
		for (IConstraintExpression expression : operands) {
			if (expression instanceof CellReference) {
				for (Cell referredCell : ((CellReference) expression)
						.getDirectlyReferencedCells()) {
					Expr var = info.getVariables().get(referredCell);
					if (var instanceof ArithExpr)
						smtExpressionsList.add((ArithExpr) var);
				}
			} else {
				Expr expr = expression.getZ3Constraint(info);
				if (expr instanceof ArithExpr)
					smtExpressionsList.add((ArithExpr) expr);
			}
		}
		ArithExpr[] smtExpressions = new ArithExpr[smtExpressionsList.size()];
		smtExpressions = smtExpressionsList.toArray(smtExpressions);

		if (smtExpressions.length == 0)
			return info.getContext().MkInt(0);

        
        if(functionType == FunctionType.Min)
        {
            ArithExpr expr = smtExpressions[0];
            for (int i = 0; i < smtExpressions.length - 1; i++) {
                expr = min2(info.getContext(), expr, smtExpressions[i + 1]);
            }
            return expr;
        }
        else
        {
            ArithExpr expr = smtExpressions[0];
            for (int i = 0; i < smtExpressions.length - 1; i++) {
                expr = max2(info.getContext(), expr, smtExpressions[i + 1]);
            }
            return expr;
        }
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

	@Override
	public String toString() {
		return super.toString(
				functionType == FunctionType.Min ? MIN_FUNCTION_NAME
						: MAX_FUNCTION_NAME, operands);
	}
    
    /**
	 * Generates a function which calculates the maximum of two given arithmetic
	 * expressions
	 * 
	 * @param expr1
	 * @param expr2
	 * @return if-then-else structure which determines the maximum value
	 * @throws Z3Exception
	 */
	public ArithExpr max2(Context ctx, ArithExpr expr1, ArithExpr expr2) 
            throws Z3Exception {
		return (ArithExpr) ctx.MkITE(ctx.MkGt(expr1, expr2), expr1, expr2);
	}
    
    /**
	 * Generates a function which calculates the minimum of two given arithmetic
	 * expressions
	 * 
	 * @param expr1
	 * @param expr2
	 * @return if-then-else structure which determines the maximum value
	 * @throws Z3Exception
	 */
	public ArithExpr min2(Context ctx, ArithExpr expr1, ArithExpr expr2) 
            throws Z3Exception {
		return (ArithExpr) ctx.MkITE(ctx.MkLt(expr1, expr2), expr1, expr2);
	}
}