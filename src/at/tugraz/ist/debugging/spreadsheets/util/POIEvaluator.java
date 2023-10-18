package at.tugraz.ist.debugging.spreadsheets.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;

import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.WorkbookInfo;


public class POIEvaluator {
	
	static public Boolean evaluateCondition(String formula, int wsIndex, WorkbookInfo wb)
	{
		Cell formulaCell = wb.getEvalCell(wsIndex);
		
		//System.out.println("formulaCell: " + formulaCell);
		
		formulaCell.setCellFormula(formula);
		
		//System.out.println("formulaCell: " + formulaCell);

		wb.getFormulaEvaluator().clearAllCachedResultValues();
		
		CellValue cellValue = wb.getFormulaEvaluator().evaluate(formulaCell);

		boolean condition = false;
		
		switch (cellValue.getCellTypeEnum()) {
		case BOOLEAN:
			// System.out.println("boolan type");
			condition = cellValue.getBooleanValue();
			break;
		case NUMERIC:
			condition = (cellValue.getNumberValue() != 0);
			break;
		case STRING:
			condition = cellValue.getBooleanValue(); // TODO check
			break;
		case BLANK:
			break;
		case ERROR:
			break;
		// CELL_TYPE_FORMULA will never happen
		case FORMULA:
			break;
	    default:
	    	System.err.println("Not implemented: "+cellValue.getCellTypeEnum());
		}
		

		return condition;
	}
	
	public static void removeCell(Cell cellToDelete) {
		cellToDelete.getRow().removeCell(cellToDelete);
	}

}
