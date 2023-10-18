package at.tugraz.ist.debugging.spreadsheets.expressions.constants;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.z3api.Z3DependencyBasedModelGenerationInformation;
import at.tugraz.ist.debugging.modelbased.z3api.Z3ValueBasedModelGenerationInformation;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.expressions.Expression;
import at.tugraz.ist.debugging.spreadsheets.expressions.IConstraintExpression;
import at.tugraz.ist.debugging.spreadsheets.parser.ErrorCellException;
import at.tugraz.ist.debugging.spreadsheets.util.PoiExtensions;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Abstract base class for constant expressions
 * 
 */
public abstract class ConstExpression extends Expression {
	public static Map<Coords, ConstExpression> convertExpectedOutput(
			Map<Coords, String> expected) {
		Map<Coords, ConstExpression> correctOutputs = new HashMap<Coords, ConstExpression>();
		for (Entry<Coords, String> entry : expected.entrySet()) {
			String value = entry.getValue();
			ConstExpression expr = create(value);
			/*
			 * TODO: check if boolean is causing any harm 
			 * 
			 * try { expr = new
			 * IntConstant(Integer.parseInt(value.trim())); } catch
			 * (NumberFormatException e1) { try { expr = new
			 * DoubleConstant(Double.parseDouble(value)); } catch
			 * (NumberFormatException e2) { expr = new StringConstant(value); }
			 * }
			 */
			correctOutputs.put(entry.getKey(),
					expr);
		}
		return correctOutputs;
	}

	/**
	 * Factory method for constant expressions which determines the instance
	 * given the POI representation of a cell value
	 * 
	 * @param cellValue
	 * @return Corresponding ConstExpression instance
	 */
	public static ConstExpression create(CellValue cellValue) {
		switch (cellValue.getCellTypeEnum()) {
		case BOOLEAN:
			return new BoolConstant(cellValue.getBooleanValue());
		case NUMERIC:
			if ((ConstraintStrategyConfiguration.getStrategy() != null &&
                    ConstraintStrategyConfiguration.getStrategy().getSolver() == ESolver.Z3) ||
                    !PoiExtensions.isInt(cellValue.getNumberValue()))
                return new DoubleConstant(cellValue.getNumberValue());
			return new IntConstant((int) (cellValue.getNumberValue()));
		case STRING:
			return new StringConstant(cellValue.getStringValue());
		case BLANK:
			throw new ErrorCellException("blank cell");
		case ERROR:
			throw new ErrorCellException("error cell");
		default:
			throw new ErrorCellException("Invalid cell value type");
		}
	}

	/**
	 * Factory method for constant expressions
	 * 
	 * This method creates a constant expression according to a certain decision
	 * heuristics: - FALSE and TRUE will result in a boolean constant - numbers
	 * which include "." will result in a double constant - numbers without "."
	 * will result in an integer constant - other strings will result in an
	 * integer constant
	 * 
	 * @param value
	 * @return
	 */
	public static ConstExpression create(String value) {

		if (value.toUpperCase().equals("TRUE") || value.toUpperCase().equals("FALSE")) {
			return new BoolConstant(value.toUpperCase().equals("TRUE"));
		}

		try {
			BigDecimal bd = new BigDecimal(value);
			if (!value.contains(".")) {
				try {
					return new IntConstant(bd.intValueExact());
				} catch (NumberFormatException e) {
					throw new RuntimeException(
							String.format(
									"Error while converting string '%s' to a constant expression",
									value));
				} catch (ArithmeticException e) {
					throw new RuntimeException(
							String.format(
									"Error while converting string '%s' to a constant expression",
									value));
				}

			}
			return new DoubleConstant(bd);
		} catch (NumberFormatException e) {
		}

		return new StringConstant(value);
	}

	@Override
	public int getNumberOperations() {
		return 0;
	}

	/**
	 * @return String representation of the ConstExpression type
	 */
	public abstract String getTypeAsString();

	/**
	 * @return String representation of the ConstantExpression's value
	 */
	public abstract String getValueAsString();
	
	@Override
	public Set<Cell> getReferencedCells(boolean dynamic, boolean faultyConst) {
		return new HashSet<Cell>();
	}
	
	@Override
	public Set<IConstraintExpression> getConditionalExpressions() {
		return new HashSet<IConstraintExpression>();
	}
	
	@Override
	public Set<Coords> getReferences(boolean dynamic) {
		return new HashSet<Coords>();
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
	public Boolean isEquivalencePossible() {
		return true;
	}
    
    @Override
    public abstract Expr getZ3ValueConstraint(Z3ValueBasedModelGenerationInformation info) 
            throws Z3Exception;
    
    @Override
    protected Expr getZ3DependencyConstraint(Z3DependencyBasedModelGenerationInformation info)
			throws Z3Exception
    {
        Context ctx = info.getContext();
        return ctx.MkBool(true);
    }

}
