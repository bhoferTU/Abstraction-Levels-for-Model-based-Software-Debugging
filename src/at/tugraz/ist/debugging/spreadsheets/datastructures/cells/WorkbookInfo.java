package at.tugraz.ist.debugging.spreadsheets.datastructures.cells;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

public class WorkbookInfo {

	private int activeWS;

	/**
	 * Last column (column with max. index)
	 */
	private int maxColumns;

	/**
	 * Last row (row with max. index)
	 */
	private int maxRows;

	/**
	 * each spreadsheet as it is named in excel
	 */
	private List<String> wsnames;

	private Map<Integer,Cell> evalCellMap;

	private EvaluationWorkbook ewb;

	private FormulaEvaluator eval;

	private FormulaRenderingWorkbook rwb;
	
	private Workbook wb;

	public Workbook getWb() {
		return wb;
	}

	public void setWb(Workbook wb) {
		this.wb = wb;
	}

	public WorkbookInfo(int maxColumns2, int maxRows2) {
		this.setMaxColumns(maxColumns2);
		this.setMaxRows(maxRows2);
		
		evalCellMap = new HashMap<Integer,Cell>();
		
	}

	public int getActiveWS() {
		return activeWS;
	}

	public int getMaxColumns() {
		return maxColumns;
	}

	public int getMaxRows() {
		return maxRows;
	}

	public List<String> getWSNames() {
		return wsnames;
	}

	public void setActiveWS(int index) {
		this.activeWS = index;
	}

	private void setMaxColumns(int maxColumns) {
		this.maxColumns = maxColumns;
	}

	private void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

	public void setWSNames(List<String> wsnames) {
		this.wsnames = wsnames;
	}

	public void setEvalCell(int wsIndex, Cell evalCell) {
		evalCellMap.put(wsIndex,evalCell);
	}
	
	public Cell getEvalCell(int wsIndex) {
		return evalCellMap.get(wsIndex);
	}


	public EvaluationWorkbook getEvaluationWB() {
		return ewb;
	}
	
	public void setEvaluationWB(EvaluationWorkbook ewb)
	{
		this.ewb = ewb;
	}

	public void setFormulaEvaluator(FormulaEvaluator evaluator) {
		this.eval = evaluator;
	}

	public FormulaEvaluator getFormulaEvaluator() {
		return eval;
	}

	public FormulaRenderingWorkbook getRenderingWB() {
		return this.rwb;
	}
	
	public void setWB(FormulaRenderingWorkbook frwb)
	{
		this.rwb = frwb;
	}
}
