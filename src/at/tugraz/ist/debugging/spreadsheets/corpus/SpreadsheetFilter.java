package at.tugraz.ist.debugging.spreadsheets.corpus;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.util.CorpusLocation;
import at.tugraz.ist.util.fileManipulation.Directory;

public class SpreadsheetFilter {
	
	boolean debug = true;
	
	/**
	 * information we need:
	 * 
	 * - point to spreadsheet corpus
	 * - ideally, folder with the spreadsheets and/or properties
	 * 
	 * a set of filter criterions 
	 * 	- min # of formulas
	 *  - wrong format (poi cannot open)
	 *  - min # of IF clauses
	 *  - min # of REAL
	 *  - max # of REAL numbers
	 */
	
	public SpreadsheetFilter(int minFormulas, int minIF) {
		this.minNumFormulas = minFormulas;
		this.minNumIF = minIF;
	}
	
	
	int minNumFormulas, minNumIF;
	
	public List<String> getFilteredList(String sourceDirectory)
	{
		List<String> originalFiles = getListOfFiles(sourceDirectory);
		
		if (debug == true)
			System.out.println("Recursive List of Files: " + originalFiles.size());

		
		List<String> filteredFiles = new ArrayList<String>();
		
		for (String filename : originalFiles)
		{
			String spreadsheetFilename = getSpreadsheet(filename);

			if (spreadsheetFilename == null)
				continue;
			
			if (checkFile(spreadsheetFilename))
				filteredFiles.add(filename);
		}
		
		return filteredFiles;
	}
	
	private String getSpreadsheet(String filename)
	{
		if (filename.endsWith(".xls")  || filename.endsWith(".xlsx"))
			return filename;
		
		if (filename.endsWith(".properties"))
		{
			try {
				SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties(filename);
				return spreadsheetProperties.getExcelSheetName(); //todo: or path
			} catch (SpreadsheetPropertiesException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private boolean checkFile(String file) {
		
		boolean enoughFormulas = false;
		boolean enoughIF = false;
		
		boolean errorOccurred = false;
		
		FileInputStream fip = null;
		
		try {
			fip = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(fip);
			int formulaCounter = 0;
			int numberOfIfs = 0;
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				Sheet sheet = wb.getSheetAt(i);
	
				Iterator<Row> rowIterator = sheet.rowIterator();
				while (rowIterator.hasNext()) {
	
					Row row = rowIterator.next();
					if (row == null) {
						continue;
					}
	
					Iterator<Cell> cellIterator = row.cellIterator();
	
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						if (cell == null)
							continue;
	
						if (cell.getCellTypeEnum() == CellType.FORMULA) {
							formulaCounter++;
							numberOfIfs += StringUtils.countMatches(cell.getCellFormula(), "IF");
						}
						
					}
	
				}
			}
			
			enoughFormulas = (formulaCounter >= minNumFormulas);
			enoughIF = (numberOfIfs >= minNumIF);

		} catch (OldExcelFormatException e) {
			errorOccurred = true;
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			errorOccurred = true; //see what caused this!
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(file);
			errorOccurred = true;
			if (fip != null) {
				try {
					fip.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		
		if (enoughFormulas && enoughIF && !errorOccurred)
			return true;
		
		return false;
	}

	private List<String> getListOfFiles(String sourceDirectory)
	{
		List<String> files = null;
		
		if (sourceDirectory.endsWith(CorpusLocation.config))
		{
			files = Directory.getFilesRecursively(sourceDirectory, ".properties");
		}
		else //if (sourceDirectory.endsWith(CorpusLocation.spreadsheet))
		{
			files = Directory.getFilesRecursively(sourceDirectory, ".xls");
			files.addAll(Directory.getFilesRecursively(sourceDirectory, ".xlsx"));
		}
		
		return files;
	}
	
	

	
}
