package at.tugraz.ist.debugging.spreadsheets.parser;

import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import java.util.Stack;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.WorkbookDependentFormula;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.ErrPtg;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.MemAreaPtg;
import org.apache.poi.ss.formula.ptg.MemErrPtg;
import org.apache.poi.ss.formula.ptg.MemFuncPtg;
import org.apache.poi.ss.formula.ptg.MissingArgPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.ParenthesisPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RangePtg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.formula.ptg.ScalarConstantPtg;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.apache.poi.ss.formula.ptg.ValueOperatorPtg;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.AreaReference;

import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.WorkbookInfo;
import at.tugraz.ist.debugging.spreadsheets.exceptions.CoordinatesException;
import at.tugraz.ist.debugging.spreadsheets.expressions.BinaryOperation;
import at.tugraz.ist.debugging.spreadsheets.expressions.CellReference;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.expressions.MissingArgument;
import at.tugraz.ist.debugging.spreadsheets.expressions.UnaryOperation;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.BoolConstant;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.DoubleConstant;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.IntConstant;
import at.tugraz.ist.debugging.spreadsheets.expressions.constants.StringConstant;
import at.tugraz.ist.debugging.spreadsheets.expressions.functions.Function;

/**
 * Parser which is responsible for building an AST given the POI token stream
 * 
 * Instances of this class are used by the cell container where the parse()
 * function is called for each cell, the returned IExpression object is then
 * associated with the particular cell instance.
 * 
 */
public class Parser {
	/**
	 * Cell container which uses the current parser instance in order to build
	 * the AST for each cell
	 */
	CellContainer cellContainer;

	WorkbookInfo ewb;

	/**
	 * Stack of expressions which were already created by the parsing function
	 */
	Stack<IConstraintExpression> parsingStack = new Stack<IConstraintExpression>();

	Stack<String> stack = new Stack<String>();

	private int ws;

	public Parser(CellContainer cellContainer, WorkbookInfo ewb) {
		this.cellContainer = cellContainer;
		this.ewb = ewb;
	}

	/**
	 * Removes numOperands expressions from the parsing stack and returns them
	 * as an array.
	 * 
	 * @param numOperands
	 * @return
	 */
	private IConstraintExpression[] getExpressionsArray(int numOperands) {
		IConstraintExpression[] params = new IConstraintExpression[numOperands];
		for (int opIndex = numOperands - 1; opIndex >= 0; --opIndex) {
			params[opIndex] = parsingStack.pop();
		}
		return params;
	}

	/**
	 * Checks whether a given Ptg class corresponds to a binary operation
	 * 
	 * @param ptgClass
	 * @return
	 */
	private boolean isBinary(Class<?> ptgClass) {
		return BinaryOperation.PtgToBinaryOperator.containsKey(ptgClass);
	}

	/**
	 * Checks whether a given Ptg class corresponds to an unary operation
	 * 
	 * @param ptgClass
	 * @return
	 */
	private boolean isUnary(Class<?> ptgClass) {
		return UnaryOperation.PtgToUnaryOperator.containsKey(ptgClass);
	}

	/**
	 * Parse function which takes some POI tokens (Ptg objects) and builds an
	 * AST. The root object of this AST as well as its sub-nodes are IExpression
	 * instances.
	 * 
	 * @param ptgs
	 *            Tokens which should be parsed (left to right)
	 * @param wsIndex
	 *            Sheet index of the cell
	 * @return AST root node
	 */
	public IConstraintExpression parse(Ptg[] ptgs, int wsIndex) {
		if (ptgs == null || ptgs.length == 0) {
			throw new IllegalArgumentException("ptgs must not be null");
		}

		this.ws = wsIndex;
		stack.clear();
		parsingStack.clear();

		for (int tokenIndex = 0; tokenIndex < ptgs.length; tokenIndex++) {
			Ptg ptg = ptgs[tokenIndex];

			try {

				parseIExpression(ptg);

				parseString(ptg);

				if (!parsingStack.isEmpty() && !stack.isEmpty()) {
					IConstraintExpression expr = parsingStack.peek();

					expr.setStringExpression(stack.peek());
				}
			} catch (CoordinatesException coordEx) {
				throw new ParsingException("Invalid Reference:" + coordEx.getMessage());
			}
		}

		if (stack.isEmpty()) {
			// inspection of the code above reveals that every stack.pop() is
			// followed by a
			// stack.push(). So this is either an internal error or impossible.
			throw new IllegalStateException("Stack underflow");
		}
		@SuppressWarnings("unused")
		String result = stack.pop();
		if (!stack.isEmpty()) {
			// Might be caused by some tokens like AttrPtg and Mem*Ptg, which
			// really shouldn't
			// put anything on the stack
			throw new IllegalStateException("too much stuff left on the stack");
		}

		if (parsingStack.size() > 2)
			throw new InvalidOperationException("Stack size > 1 at the end");

		return parsingStack.peek();
	}

	private void parseString(Ptg ptg) {
		// TODO - what about MemNoMemPtg?
		if (ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
			// marks the start of a list of area expressions which will be
			// naturally combined
			// by their trailing operators (e.g. UnionPtg)
			// TODO - put comment and throw exception in toFormulaString() of
			// these classes
			return;
		}
		if (ptg instanceof ParenthesisPtg) {
			String contents = stack.pop();
			stack.push("(" + contents + ")");
			return;
		}
		if (ptg instanceof AttrPtg) {
			AttrPtg attrPtg = ((AttrPtg) ptg);
			if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isSkip()) {
				return;
			}
			if (attrPtg.isSpace()) {
				// POI currently doesn't render spaces in formulas
				return;
				// but if it ever did, care must be taken:
				// tAttrSpace comes *before* the operand it applies to, which
				// may be consistent
				// with how the formula text appears but is against the RPN
				// ordering assumed here
			}
			if (attrPtg.isSemiVolatile()) {
				// similar to tAttrSpace - RPN is violated
				return;
			}
			if (attrPtg.isSum()) {
				String[] operands = getOperands(stack, attrPtg.getNumberOfOperands());
				stack.push(attrPtg.toFormulaString(operands));
				return;
			}
			throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
		}

		if (ptg instanceof WorkbookDependentFormula) {
			WorkbookDependentFormula optg = (WorkbookDependentFormula) ptg;
			stack.push(optg.toFormulaString(ewb.getRenderingWB()));
			// throw new RuntimeException("WB dependent.");
			return;
		}
		if (!(ptg instanceof OperationPtg)) {
			stack.push(ptg.toFormulaString());
			return;
		}

		OperationPtg o = (OperationPtg) ptg;
		String[] operands = getOperands(stack, o.getNumberOfOperands());
		stack.push(o.toFormulaString(operands));

	}

	private void parseIExpression(Ptg ptg) throws CoordinatesException {
		if (ptg instanceof ScalarConstantPtg) {
			parseScalarConstant((ScalarConstantPtg) ptg);
		} else if (ptg instanceof ValueOperatorPtg) {
			parseValueOperator((ValueOperatorPtg) ptg);
		} else if (ptg instanceof ParenthesisPtg) {
			// ignore parenthesis token
		} else if (ptg instanceof RefPtg) {
			RefPtg refPtg = (RefPtg) ptg;

			parsingStack.push(new CellReference(refPtg.getColumn(), refPtg.getRow(), ws, cellContainer));
		} else if (ptg instanceof AreaPtg) {
			AreaPtg refPtg = (AreaPtg) ptg;
			parsingStack.push(new CellReference(refPtg.getFirstColumn(), refPtg.getFirstRow(), refPtg.getLastColumn(),
					refPtg.getLastRow(), ws, cellContainer));
		} else if (ptg instanceof FuncVarPtg) {
			FuncVarPtg funcVarPtg = (FuncVarPtg) ptg;
			pushFunction(funcVarPtg.getName(), funcVarPtg.getNumberOfOperands());
		} else if (ptg instanceof FuncPtg) {
			FuncPtg funcPtg = (FuncPtg) ptg;
			pushFunction(funcPtg.getName(), funcPtg.getNumberOfOperands());
		} else if (ptg instanceof AttrPtg) {
			AttrPtg attrPtg = (AttrPtg) ptg;
			if (attrPtg.isSum())
				pushFunction(Function.SUM_FUNCTION_NAME, attrPtg.getNumberOfOperands());
			else {
				IConstraintExpression[] params = getExpressionsArray(attrPtg.getNumberOfOperands());
				parsingStack.push(new ParserControlAttribute(ParserControlAttribute.Type.ATTR, params));

				// Ptg[] ptgParams = getPtgArray(attrPtg.getNumberOfOperands(),
				// tokens);
				// wrapper.push(new AttrWrapper(attrPtg, ptgParams));

			}
		} else if (ptg instanceof Ref3DPtg) {
			Ref3DPtg refPtg = (Ref3DPtg) ptg;
			int internalWSIndex = getWSIndex(refPtg.getExternSheetIndex());
			parsingStack.push(new CellReference(refPtg.getColumn(), refPtg.getRow(), internalWSIndex, cellContainer));
		} else if (ptg instanceof Area3DPtg) {
			Area3DPtg refPtg = (Area3DPtg) ptg;

			int internalWSIndex = getWSIndex(refPtg.getExternSheetIndex());

			if (internalWSIndex == -1)
				throw new ParsingException("Invalid Reference: " + refPtg);

			parsingStack.push(new CellReference(refPtg.getFirstColumn(), refPtg.getFirstRow(), refPtg.getLastColumn(),
					refPtg.getLastRow(), internalWSIndex, cellContainer));

		} else if (ptg instanceof NamePtg) {
			NamePtg namePtg = (NamePtg) ptg;
			Name aNamedCell = ewb.getWb().getNameAt(namePtg.getIndex());
			AreaReference aref = new AreaReference(aNamedCell.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);
			parsingStack.push(new CellReference(aref.getFirstCell().getCol(), aref.getFirstCell().getRow(),
					aref.getLastCell().getCol(), aref.getLastCell().getRow(), ws, cellContainer));

		} else if (ptg instanceof MemFuncPtg) {
			MemFuncPtg memFunPtg = (MemFuncPtg)ptg;
//			memFunPtg.
		}else if (ptg instanceof RangePtg){
			RangePtg rangePtg = (RangePtg)ptg;
		}

		else
			throw new ParsingException("Invalid token (" + ptg + ")");
	}

	/**
	 * Given a scalar constant this method pushes a corresponding constant
	 * expression onto the parsing stack which contains the same information as
	 * the Ptg object
	 * 
	 * @param ptg
	 */
	public void parseScalarConstant(ScalarConstantPtg ptg) {
		if ((ptg instanceof IntPtg && ConstraintStrategyConfiguration.getStrategy() != null
				&& ConstraintStrategyConfiguration.getStrategy().getSolver() != ESolver.Z3)
				|| (ptg instanceof IntPtg && ConstraintStrategyConfiguration.getStrategy() == null))
			parsingStack.push(new IntConstant(((IntPtg) ptg).getValue()));
		else if (ptg instanceof IntPtg && ConstraintStrategyConfiguration.getStrategy() != null
				&& ConstraintStrategyConfiguration.getStrategy().getSolver() == ESolver.Z3)
			parsingStack.push(new DoubleConstant(((double) ((IntPtg) ptg).getValue())));
		else if (ptg instanceof BoolPtg)
			parsingStack.push(new BoolConstant(((BoolPtg) ptg).getValue()));
		else if (ptg instanceof MissingArgPtg)
			parsingStack.push(new MissingArgument());
		else if (ptg instanceof NumberPtg)
			parsingStack.push(new DoubleConstant(((NumberPtg) ptg).getValue()));
		else if (ptg instanceof StringPtg)
			parsingStack.push(new StringConstant(((StringPtg) ptg).getValue()));
		else if (ptg instanceof ErrPtg)
			throw new ErrorCellException("#N/A");
		// throw new ParsingException("Invalid Cell Reference!");
		else
			throw new ParsingException("Invalid token in scalar constant parse function (" + ptg + ")");
	}

	/**
	 * Given a Ptg object this method pushes a corresponding unary or binary
	 * operation onto the parsing stack
	 * 
	 * @param ptg
	 */
	public void parseValueOperator(ValueOperatorPtg ptg) {
		try {
			if (isBinary(ptg.getClass())) {
				parsingStack.push(BinaryOperation.Create(parsingStack.pop(), parsingStack.pop(), ptg.getClass()));
			} else if (isUnary(ptg.getClass())) {
				parsingStack.push(new UnaryOperation(parsingStack.pop(), ptg.getClass()));
			}
		} catch (Exception e) {
			throw new ParsingException("error during parsing token of " + ptg);
		}
	}

	/**
	 * Pushes a certain function call onto the parsing stack
	 * 
	 * @param functionName
	 *            Function name
	 * @param numOperands
	 *            Number of operands
	 */
	private void pushFunction(String functionName, int numOperands) {

		IConstraintExpression[] params = getExpressionsArray(numOperands);
		parsingStack.push(Function.Create(functionName, params, ws, ewb));
	}

	private int getWSIndex(int index) {
		EvaluationWorkbook wb = ewb.getEvaluationWB();
		return wb.convertFromExternSheetIndex(index);

	}

	/**
	 * copied from POI
	 * 
	 * @param stack
	 * @param nOperands
	 * @return
	 */
	private static String[] getOperands(Stack<String> stack, int nOperands) {
		String[] operands = new String[nOperands];

		for (int j = nOperands - 1; j >= 0; j--) { // reverse iteration because
													// args were pushed in-order
			if (stack.isEmpty()) {
				String msg = "Too few arguments supplied to operation. Expected (" + nOperands + ") operands but got ("
						+ (nOperands - j - 1) + ")";
				throw new IllegalStateException(msg);
			}
			operands[j] = stack.pop();
		}
		return operands;
	}

}
