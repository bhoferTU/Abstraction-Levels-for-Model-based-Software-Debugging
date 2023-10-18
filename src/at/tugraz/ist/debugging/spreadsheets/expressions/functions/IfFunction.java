package at.tugraz.ist.debugging.spreadsheets.expressions.functions;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.choco.ChocoConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraints;
import at.tugraz.ist.debugging.modelbased.minion.MinionExpressionConstraints;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstraintStrategyGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.WorkbookInfo;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.parser.ParserControlAttribute;
import at.tugraz.ist.debugging.spreadsheets.util.POIEvaluator;
import choco.Choco;
import choco.IPretty;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

/**
 * Implementation of the Excel IF function
 * 
 */
class IfFunction extends ShellFunction {
	
    IConstraintExpression condition;
	IConstraintExpression elseExpression;

	IConstraintExpression thenExpression;
	
	Boolean isIfTrue = null;
	

	/**
	 * Constructor for if functions with optional else expressions
	 * 
	 * @param operands
	 */
	protected IfFunction(IConstraintExpression[] operands) {
		super(Function.IF_FUNCTION_NAME, operands);
		condition = operands[0];
		thenExpression = operands[1];
		if (operands.length == 3)
			elseExpression = operands[2];
	}
    
    /**
	 * Factory method
	 * 
	 * @param controlAttributes
	 * @param wb 
	 * @param ws 
	 * @return
	 */
	public static Function Create(IConstraintExpression[] controlAttributes, int ws, WorkbookInfo wb) {
		if (controlAttributes.length < 2)
			throw new InvalidOperationException(
					"Too less operands for IF function");
		IConstraintExpression[] operands = new IConstraintExpression[controlAttributes.length];
		for (int i = 0; i < controlAttributes.length; i++) {
			if (!(controlAttributes[i] instanceof ParserControlAttribute))
				throw new InvalidOperationException(
						"Invalid argument for IF function");
			IConstraintExpression[] controlExpressions = ((ParserControlAttribute) controlAttributes[i])
					.getExpressions();
			if (controlExpressions == null || controlExpressions.length != 1)
				throw new InvalidOperationException(
						"Invalid expression in argument for IF function");
			operands[i] = controlExpressions[0];
		}

		IfFunction func = new IfFunction(operands);
		func.setContext(ws, wb);
		return func;
	}

	private int ws;
	private WorkbookInfo wb;
	
	private void setContext(int ws, WorkbookInfo wb)
	{
		this.ws = ws;
		this.wb = wb;
	}

	private boolean addIf()
	{
		if (isIfTrue == null)
		{
			Boolean conditionResult = POIEvaluator.evaluateCondition(condition.getFormula(), ws, wb);
			
			//Object conditionResOld = condition.evaluate();

			if (conditionResult == null)
				throw new RuntimeException("Evaluation Error: POIEvaluator returned null for " + condition.getFormula());
			/*
			if (((boolean)conditionResOld) != conditionResult){
				
				System.out.println(conditionResOld + " " + conditionResult);
				
				throw new RuntimeException("Evaluation Error: " 
						+ condition.getFormula() +" old eval:"+ conditionResOld);
			}*/
			
			isIfTrue = conditionResult;
		}

		return isIfTrue;
	}
	
	@Override
	public Object evaluate() {
		Object conditionResult = condition.evaluate();
		if (conditionResult instanceof Integer) {
			conditionResult = (int) conditionResult != 0;
		} else if (conditionResult instanceof Double) {
			conditionResult = (double) conditionResult != 0.0;
		}

		if ((boolean) conditionResult)
			return thenExpression.evaluate();
		else if (elseExpression != null)
			return elseExpression.evaluate();
		else
			return false;

	}

	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		Set<Cell> cells = new HashSet<Cell>();
		cells.addAll(condition.getReferencedCells(dynamic, faultyConst));

		boolean addIf = true;
		if (dynamic) {			
			addIf = addIf();
		}

		if (addIf)
			cells.addAll(thenExpression.getReferencedCells(dynamic, faultyConst));

		if ((dynamic && !addIf) || !dynamic) {
			if (elseExpression != null)
				cells.addAll(elseExpression.getReferencedCells(dynamic, faultyConst));

		}
		return cells;
	}

	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		Set<Coords> cells = new HashSet<Coords>();
		cells.addAll(condition.getReferences(dynamic));

		boolean addIf = true;
		if (dynamic) {
			addIf = addIf();
		}

		if (addIf)
			cells.addAll(thenExpression.getReferences(dynamic));

		if ((dynamic && !addIf) || !dynamic) {
			if (elseExpression != null)
				cells.addAll(elseExpression.getReferences(dynamic));
		}
		return cells;
	}

	@Override
	public IPretty getChocoConstraint(
			ChocoConstraintStrategyGenerationInformation info) {
		IPretty cond = condition.getChocoConstraint(info);
		if (!(cond instanceof IntegerExpressionVariable))
			throw new InvalidOperationException(
					"Condition must be integer or bool");

		Constraint conditionConstraint = Choco.neq(
				(IntegerExpressionVariable) cond, Choco.ZERO);

		IPretty thenConstraint = thenExpression.getChocoConstraint(info);
		if (elseExpression == null)
			throw new InvalidOperationException(
					"Empty else branch currently not supported");

		IPretty elseConstraint = elseExpression.getChocoConstraint(info);

		if (!(thenConstraint instanceof IntegerExpressionVariable)
				|| !(elseConstraint instanceof IntegerExpressionVariable))
			throw new InvalidOperationException(
					"Branches need to be integer expressions");

		return info.addAuxIfThenElse(conditionConstraint,
				(IntegerExpressionVariable) thenConstraint,
				(IntegerExpressionVariable) elseConstraint);

		/*
		 * pervious implementation of the Choco if-then-else structure using
		 * Choco's ifthenelse method
		 */
		// return Choco.ifThenElse(conditionConstraint,
		// (IntegerExpressionVariable) thenConstraint,
		// (IntegerExpressionVariable) elseConstraint);
	}

	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		Set<IConstraintExpression> conditionalExpressions = new HashSet<IConstraintExpression>();
		conditionalExpressions.add(condition);
		return conditionalExpressions;
	}

	@Override
	/**
	 * 2022 Function modified in order to consider Comparison Models
	 * @author inica
	 * **/
	public MinionExpressionConstraints getMinionValueConstraints(
			MinionConstraintStrategyGenerationInformation info) {
		
		MinionExpressionConstraints condConstraints = condition
				.getMinionValueConstraints(info);
		MinionExpressionConstraints thenConstraints = thenExpression
				.getMinionValueConstraints(info);
		MinionExpressionConstraints elseConstraints = elseExpression
				.getMinionValueConstraints(info);

		Domain resultType = thenConstraints.getResultType();
		String resultVariable = info.getNextAuxiliaryVariable(resultType);
		MinionExpressionConstraints newConstraint = null;
		
		
		
		if (info.getModelGranularity() == EModelGranularity.Comparison) {
			
			newConstraint=getMinionComparisionConstraints(info,condConstraints.getVarname(),thenConstraints.getVarname(),elseConstraints.getVarname(), resultVariable);
			
		}
		else
			if (info.getModelGranularity() == EModelGranularity.Dependency) {
				
				newConstraint=getMinionDependencyConstraints(info,condConstraints.getVarname(),thenConstraints.getVarname(),elseConstraints.getVarname(), resultVariable);
				
			}
			else
		{
		String notCond = info.getNextAuxiliaryVariable(Domain.BOOLEAN);
		newConstraint = MinionConstraints
				.getIFConstraints(condConstraints.getVarname(), notCond,
						thenConstraints.getVarname(),
						elseConstraints.getVarname(), resultVariable,
						resultType);
		}
		
		newConstraint.addConstraints(condConstraints);
		//Iulia: Problem in VBM: divion by 0 in then OR else branch
		
		newConstraint.addConstraints(thenConstraints);
		newConstraint.addConstraints(elseConstraints);
		
		return newConstraint;
	}
	/**
	 * * @author inica
	 * **/
	
	private MinionExpressionConstraints getMinionComparisionConstraints(MinionConstraintStrategyGenerationInformation info, String condName, String thenName, String elseName, String result ) {
		
		MinionExpressionConstraints newConstraint = MinionConstraints
				.getIFTableConstraints(info.getAbnormalIndex(), condName, thenName, elseName, result);
						                 
		return newConstraint; 
		
	}
private MinionExpressionConstraints getMinionDependencyConstraints(MinionConstraintStrategyGenerationInformation info, String condName, String thenName, String elseName, String result ) {
		
		MinionExpressionConstraints newConstraint = MinionConstraints
				.getIFDTableConstraints(info.getAbnormalIndex(), condName, thenName, elseName, result);
						                 
		return newConstraint; 
		
	}

	@Override
	public String getSMTConstraint(
			SMTConstraintStrategyGenerationInformation info) {
		// RESTRICTION: if without else is not supported
		if (elseExpression == null)
			throw new InvalidOperationException(
					"If without else is not supported");

		String smtCond = condition.getSMTConstraint(info);
		String smtThen = thenExpression.getSMTConstraint(info);
		String smtElse = elseExpression.getSMTConstraint(info);

		return info.getCodeGenerator().ite(smtCond, smtThen, smtElse);
	}

	@Override
	public Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info)
			throws Z3Exception {

		Expr smtCond = condition.getZ3Constraint(info);
		Expr smtThen = thenExpression.getZ3Constraint(info);
		Expr smtElse = elseExpression.getZ3Constraint(info);

		if (!(smtCond instanceof BoolExpr))
			throw new RuntimeException(
					"Cannot apply non-boolean expression as condition for ite");

		return info.getContext().MkITE((BoolExpr) smtCond, smtThen, smtElse);
	}

	@Override
	public Boolean isEquivalencePossible() {
		return false;
	}

	@Override
	public String toString() {
		String elseExpressionString = "";
		if (elseExpression != null)
			elseExpressionString = "; " + elseExpression.toString();

		return "IF(" + condition + "; " + thenExpression.toString()
				+ elseExpressionString + ")";
	}
}