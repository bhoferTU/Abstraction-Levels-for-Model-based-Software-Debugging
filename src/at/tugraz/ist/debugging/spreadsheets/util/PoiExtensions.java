package at.tugraz.ist.debugging.spreadsheets.util;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.util.CellReference;

import at.tugraz.ist.util.MathUtils;

/**
 * this class provides some useful functions
 */
public class PoiExtensions {

	/**
	 * compares the results of cell evaluation to an obejct - can be used to
	 * test evaluation by comparing it with evaluation result of poi
	 * 
	 * @param cellValue
	 *            which should be compared with result
	 * @param result
	 *            object which is the reference result of poi
	 */
	public static void compareExpressionResultToCellValue(CellValue cellValue,
			Object result) {

		if (cellValue == null && result == null) {
			throw new InvalidOperationException(
					"Error while comparing computed expression result with reference value from sheet: Cell value and result are not available");
		}

		if (cellValue == null) {
			throw new InvalidOperationException(
					"Error while comparing computed expression result with reference value from sheet: Cell value is not available");

		}

		if (result == null) {
			throw new InvalidOperationException(
					"Error while comparing computed expression result with reference value from sheet: Result is not available");

		}

		switch (cellValue.getCellTypeEnum()) {
		case BOOLEAN:
			if (!(result instanceof Boolean)
					|| cellValue.getBooleanValue() != (Boolean) result)
				throw new InvalidOperationException(
						String.format(
								"Error while comparing computed expression result with reference value from sheet. Result: %s\texpected: %s",
								result, cellValue.getStringValue()));
			break;
		case NUMERIC:
			if (((result instanceof Double) && cellValue.getNumberValue() != (Double) result)
					|| ((result instanceof Integer) && cellValue
							.getNumberValue() != (Integer) result)
					|| !(result instanceof Double)
					&& !(result instanceof Integer))
				throw new InvalidOperationException(
						String.format(
								"Error while comparing computed expression result with reference value from sheet. Result: %s\texpected: %s",
								result, cellValue.getNumberValue()));
			break;
		case STRING:
			if (!cellValue.getStringValue().equals(result))
				throw new InvalidOperationException(
						String.format(
								"Error while comparing computed expression result with reference value from sheet. Result: '%s'\texpected: '%s'",
								result, cellValue.getStringValue()));
			break;
		case BLANK:
			throw new InvalidOperationException(
					"Error while comparing computed expression result with reference value from sheet: Blank cell type is not supported");
		case ERROR:
			throw new InvalidOperationException(
					"Error while comparing computed expression result with reference value from sheet: Error cell type is not supported");
	    default:
	    	throw new InvalidOperationException(
					"Error while comparing computed expression result with reference value from sheet: Error cell type is not supported");
		}
	}

	/**
	 * prints an array of ptgs
	 * 
	 * @param ptgs
	 *            array which should be printed
	 */
	public static void dumpPtgArray(Ptg[] ptgs) {
		System.out.println("PTG Array size: " + ptgs.length);
		for (Ptg ptg : ptgs) {
			System.out.println(ptg.getClass().getSimpleName() + "\t\t"
					+ ptg.toString());
		}

	}
	
	public static int getColumnIndex(String colName)
	{
		return CellReference.convertColStringToIndex(colName);
	}

	/**
	 * calculates the column name given by its position
	 * 
	 * @param column
	 *            of cell
	 * @return name
	 */
	public static String getColumnString(int column) {
		
		return CellReference.convertNumToColString(column);
		/*
		char[] baseValues = Integer.toString(column, 26).toCharArray();
		for (int i = 0; i < baseValues.length; i++) {
			if (baseValues.length > 1 && i < 1) {
				if (baseValues[i] >= 0x30 && baseValues[i] <= 0x39)
					baseValues[i] += 0x30;// + i<baseValues.length?1:0;
				else
					baseValues[i] += 0x9;// + i<baseValues.length?1:0;
			} else {
				if (baseValues[i] >= 0x30 && baseValues[i] <= 0x39)
					baseValues[i] += 0x31;// + i<baseValues.length?1:0;
				else
					baseValues[i] += 0xa;// + i<baseValues.length?1:0;
			}

		}
		return String.valueOf(baseValues).toUpperCase();*/
	}

	/**
	 * returns true if given number can be converted to an integer using given
	 * epsilon as accuracy
	 * 
	 * @param doubleValue
	 *            which should be converted
	 * @return true if conversation is possible, false otherwise
	 */
	public static boolean seemsToBeInt(Double doubleValue) {
        return (Math.abs(doubleValue.intValue() - doubleValue) < MathUtils.EPSILON);
	}
    
    /**
	 * returns true if given number can be converted to an integer
	 * 
	 * @param doubleValue which should be converted
	 * @return true if conversation is possible, false otherwise
	 */
	public static boolean isInt(Double doubleValue) {
        return (doubleValue % 1) == 0;
	}
}
