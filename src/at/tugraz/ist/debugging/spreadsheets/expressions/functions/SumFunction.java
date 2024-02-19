package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTCodeGenerator.SortType;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.IntConstant;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.EvaluationException;
import at.tugraz.ist.debugging.spreadsheets.expressions.evaluation.ExpressionReturnType;
import choco.Choco;
import choco.IPretty;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.real.RealExpressionVariable;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Z3Exception;

/**
 * Implementation of the Excel SUM function
 * 
 */
class SumFunction extends ShellFunction {

	public SumFunction(IConstraintExpression[] summands) {
		super(Function.SUM_FUNCTION_NAME, summands);
	}

	@Override
	public Object evaluate() {
		BigDecimal doubleSum = new BigDecimal(0);
		int intSum = 0;
		ExpressionReturnType returnType = ExpressionReturnType.Integer;

		List<Object> results = new LinkedList<Object>();
		List<IConstraintExpression> flatExpr = new LinkedList<IConstraintExpression>();
		try {
			for (IConstraintExpression expression : operands) {
				results.clear();
				// evaluate all results
				if (expression instanceof CellReference) {
					// evaluate referenced cells
					flatExpr.addAll(((CellReference) expression)
							.getReferencedExpressions());

				} else {
					// evaluate formula
					flatExpr.add(expression);
				}
			}

			for (IConstraintExpression expr : flatExpr) {
				Object result = expr.evaluate();
				if (returnType == ExpressionReturnType.Integer) {
					if (result instanceof Integer)
						intSum += (Integer) result;
					else if (result instanceof Double) {
						doubleSum = new BigDecimal(intSum).add((new BigDecimal(
								(Double) result)));
						returnType = ExpressionReturnType.Double;
					}
				} else if (returnType == ExpressionReturnType.Double) {
					if (result instanceof Integer)
						doubleSum = doubleSum.add(new BigDecimal(
								(Integer) result));
					else if (result instanceof Double)
						doubleSum = doubleSum.add(new BigDecimal(
								(Double) result));
				}
			}
		} catch (Exception e) {
			throw new EvaluationException("Error during calculating sum", e);
		}

		if (returnType == ExpressionReturnType.Integer)
			return intSum;
		return Double.valueOf(doubleSum.toPlainString());
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		List<IntegerExpressionVariable> integerExpressions = new ArrayList<IntegerExpressionVariable>();
		for (IConstraintExpression summand : operands) {
			if (summand instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) summand)
						.getDirectlyReferencedCells()) {
					Variable var = info.getVariables().get(cell);
					if (var instanceof IntegerExpressionVariable)
						integerExpressions.add((IntegerExpressionVariable) var);
					else if (var instanceof RealExpressionVariable)
						throw new InvalidOperationException(
								"Sum of real values is currently not supported");
					else if (var == null) {
						throw new InvalidOperationException(String.format(
								"Sum: The variable for cell %s does not exist",
								cell.getCoords().getUserString()));

					}
				}
			} else {
				IPretty var = summand.getChocoConstraint(info);
				if (var instanceof RealExpressionVariable)
					throw new InvalidOperationException(
							"Sum of real values is currently not supported");
				if (var instanceof IntegerExpressionVariable)
					integerExpressions.add((IntegerExpressionVariable) var);
			}
		}
		IntegerExpressionVariable[] summandVariables = new IntegerExpressionVariable[integerExpressions
				.size()];
		summandVariables = integerExpressions.toArray(summandVariables);
		return info.addAuxIntegerConstraint(Choco.sum(summandVariables));
	}

	@Override
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		
		Set<String> cells = new HashSet<String>();
		

		for (IConstraintExpression summand : operands) {
			if (summand instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) summand)
						.getDirectlyReferencedCells()) {
					cells.add(cell.getCoords().getMinionString());
				}
			}else if(summand instanceof IntConstant){
				cells.add(summand.toString());
			}else {
				// System.err.println("Not Implemented exception");
				/**
				 * 23.02.2022: Discussion with Birgit:
				 * it should be: "Expression" instead of "exception"
				 * Birgit: never found an expression/formula inside the EXCEL SUM function, but just single cell references 
				 *         and range references
				 * **/
				System.err.println("Not Implemented Expression!");
			}
		}
		if(info.getModelGranularity()==EModelGranularity.Comparison)
			return getMinionComparisionConstraints(info,cells);
		// ** 2022 new DBM  EModelGranularity.Dependency
		if(info.getModelGranularity()==EModelGranularity.Dependency)
			return getMinionDependencyConstraints(info,cells);
		else{
			String resultVariable = info.getNextAuxiliaryVariable(info.getModelGranularity()==EModelGranularity.Comparison?Domain.INT3:Domain.INTEGER);
			return MinionConstraints
				.getSUMConstraint(cells, resultVariable);
		}
		
//		return null;
	}
	
	// ** 2022 new DBM
	private MinionExpressionConstraints getMinionDependencyConstraints(MinionConstraintStrategyGenerationInformation info, Set<String> cells) {
		Set<String> constraints = new HashSet<String>();
		String[] cells2 = new String[cells.size()];
		cells2 = cells.toArray(cells2);
		for(int i=0;i<cells2.length;i++){
			String cell = cells2[i];
			try{
				Integer.parseInt(cell);
				cells2[i]="1"; // the cells value is correct (0 incorrect, 1 correct) 
			}catch(NumberFormatException e){
				// it's okay, it is a cell reference, not a number
			}
		}
		
		String resultVariable = "";
		String oldResultVariable = cells2[0];
		for(int i=1;i<cells.size();i++){
			resultVariable = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
			constraints.addAll(MinionConstraints.getPlusMinusTableConstraints(info.getAbnormalIndex(), oldResultVariable, cells2[i], resultVariable).getConstraints());
			oldResultVariable = resultVariable;
		}
					
		return new MinionExpressionConstraints(constraints,	 new HashSet<String>(), oldResultVariable, Domain.BOOLEAN); 
		
	}
	
	private MinionExpressionConstraints getMinionComparisionConstraints(MinionConstraintStrategyGenerationInformation info, Set<String> cells) {
		Set<String> constraints = new HashSet<String>();
		String[] cells2 = new String[cells.size()];
		cells2 = cells.toArray(cells2);
		for(int i=0;i<cells2.length;i++){
			String cell = cells2[i];
			try{
				Integer.parseInt(cell);
				cells2[i]="1"; // the cells value is correct (0<,1=,2>) 
			}catch(NumberFormatException e){
				// it's okay, it is a cell reference, not a number
			}
		}
		
		String resultVariable = "";
		String oldResultVariable = cells2[0];
		for(int i=1;i<cells.size();i++){
			resultVariable = info.getNextAuxiliaryVariable(Domain.INT3);
			constraints.addAll(MinionConstraints.getPLUSTableConstraints(info.getAbnormalIndex(), oldResultVariable, cells2[i], resultVariable).getConstraints());
			oldResultVariable = resultVariable;
		}
					
		return new MinionExpressionConstraints(constraints,	 new HashSet<String>(), oldResultVariable, Domain.INT3); 
		
	}
	
	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		List<String> expressions = new ArrayList<String>();
		List<SortType> sortTypes = new ArrayList<SortType>();
		for (IConstraintExpression summand : operands) {
			if (summand instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) summand)
						.getDirectlyReferencedCells()) {
					SortType sortType = info.determineSort(cell.evaluate());
					if (sortType == SortType.Int || sortType == SortType.Real) {
						expressions.add(info.getVariables().get(cell));
						sortTypes.add(sortType);
					}
				}
			} else {
				SortType sortType = info.determineSort(summand.evaluate());
				if (sortType == SortType.Int || sortType == SortType.Real) {
					expressions.add(summand.getSMTConstraint(info));
					sortTypes.add(sortType);
				}
			}
		}

		// convert all participants to the sort type corresponding to the
		// resulting value
		SortType targetType = info.determineSort(evaluate());
		String[] summandsArray = new String[expressions.size()];
		summandsArray = expressions.toArray(summandsArray);
		for (int i = 0; i < summandsArray.length; i++) {
			info.getCodeGenerator().toSortType(summandsArray[i],
					sortTypes.get(i), targetType);
		}

		return info.getCodeGenerator().sum(summandsArray);
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {        
		List<ArithExpr> expressions = new ArrayList<>();
		for (IConstraintExpression summand : operands) {
			if (summand instanceof CellReference) {
				// evaluate referenced cells
				for (Cell cell : ((CellReference) summand).getDirectlyReferencedCells()) {
					Expr expression = info.getVariables().get(cell);
					if (expression instanceof IntExpr
							|| expression instanceof RealExpr) {
						expressions.add((ArithExpr) expression);
					}
				}
			} else {
				Expr expression = summand.getZ3Constraint(info);
				if (expression instanceof IntExpr
						|| expression instanceof RealExpr) {
					expressions.add((ArithExpr) expression);
				}
			}
		}

		// convert all participants to the sort type corresponding to the
		// resulting value
		SortType targetType = info.determineSort(evaluate());
		ArithExpr[] summandsArray = new ArithExpr[expressions.size()];
		summandsArray = expressions.toArray(summandsArray);
		for (int i = 0; i < summandsArray.length; i++) {
			info.toSortType(summandsArray[i], targetType);
		}
		return info.getContext().MkAdd(summandsArray);
	}

	@Override
	public Boolean isEquivalencePossible() {
		for (IConstraintExpression summand : operands) {
			if (!summand.isEquivalencePossible()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return super.toString(SUM_FUNCTION_NAME, operands);
	}
}