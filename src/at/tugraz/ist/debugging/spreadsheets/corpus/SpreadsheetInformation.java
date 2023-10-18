package at.tugraz.ist.debugging.spreadsheets.corpus;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICell;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICellContainer;
import at.tugraz.ist.util.Messages;
import at.tugraz.ist.util.fileManipulation.FileTools;

public class SpreadsheetInformation {

	final static String SEPARATOR = Messages.getString("Seperator0");

	public static String getHeader() {
		StringBuilder strB = new StringBuilder();
		strB.append("Name of the properties file");
		strB.append(SEPARATOR);
		strB.append("Name of the spreadsheet file");
		strB.append(SEPARATOR);

		strB.append("Number of formula cells");
		strB.append(SEPARATOR);
		strB.append("Average number of referenced cells per formula cell");
		strB.append(SEPARATOR);
		strB.append("Average number of operators per formula cell");
		strB.append(SEPARATOR);
		strB.append("Max. number of referenced cells per formula cell");
		strB.append(SEPARATOR);
		strB.append("Max. number of operators per formula cell");
		strB.append(SEPARATOR);
		
		strB.append("Number of If's");
		strB.append(SEPARATOR);

		strB.append("Number of input cells");
		strB.append(SEPARATOR);
		strB.append("Number of errorneous output cells");
		strB.append(SEPARATOR);
		strB.append("Number of correct output cells");
		strB.append(SEPARATOR);
		strB.append("Number of coincidental correct output cells");
		strB.append(SEPARATOR);

		strB.append("Number of faults");
		strB.append(SEPARATOR);
		strB.append(System.lineSeparator());
		return strB.toString();
	}

	private double averageNumberOfOperatorsPerFormula = -1;

	private double averageNumberOfReferencedCellsPerFormula = -1;
	private ICellContainer cells;
	private int maxNumberOfOperatorsPerFormula = -1;
	private int maxNumberOfReferencedCellsPerFormula = -1;
	private int numberOfCoincidentalCorrectOutputCells = -1;

	private int numberOfCorrectOutputCells = -1;
	private int numberOfErroneousOutputCells = -1;
	private int numberOfFaults;
	private int numberOfFormulaCells = -1;
	
	private int numberOfIfs = -1;

	private int numberOfInputCells = -1;

	private String propertiesFileName;

	private String spreadsheetFileName;

	public SpreadsheetInformation(SpreadsheetProperties properties,
			ICellContainer container) {
		propertiesFileName = FileTools.convertSystemPath(properties.getPropertyFileName());
		spreadsheetFileName = FileTools.convertSystemPath(properties.getExcelSheetName());

		numberOfErroneousOutputCells = properties.getIncorrectOutputCells()
				.size();
		numberOfCorrectOutputCells = properties.getCorrectOutputCells().size();
		numberOfFaults = properties.getFaultMapping().size();

		this.cells = container;

		try {

			numberOfFormulaCells = container.getFormulaCoords().size();
			computeFormulaSpecificInformation();

			numberOfInputCells = container.getInputCoords().size();
			numberOfCoincidentalCorrectOutputCells = getNumberOfCoincidentalCorrectOutputCells(
					properties.getCorrectOutputCells(), properties
							.getFaultMapping().keySet());

		} catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}

	}

	/**
	 * Requires that CellMap.populate() is called for the current spreadsheet.
	 * 
	 * @param cells
	 * 
	 */
	private void computeFormulaSpecificInformation() {
		int sumOfReferencedCells = 0;
		int sumOfOperators = 0;
		numberOfIfs = 0;

		for (Coords cell : cells.getFormulaCoords()) {
			ICell fcell = cells.getICell(cell);
			int numReferencedCells = fcell.getReferences(false).size();
			sumOfReferencedCells += numReferencedCells;
			if (numReferencedCells > maxNumberOfReferencedCellsPerFormula) {
				maxNumberOfReferencedCellsPerFormula = numReferencedCells;
			}

			int numOperators = 0;
			try {
				numOperators = fcell.getExpression().getNumberOperations();
				fcell.getFormulaString();
				int ifs =  StringUtils.countMatches(fcell.getFormulaString(), "IF");// CharMatcher.is("IF").countIn(fcell.getFormulaString());
				numberOfIfs +=ifs;
//				if(ifs!=0){
//					System.out.println(cell.toString()+" "+fcell.getFormulaString());
//				}

				/*
				 * for(Ptg parseThing: fcell.getParseTokens()){ if (parseThing
				 * instanceof OperationPtg){ numOperators++; } }
				 */
				sumOfOperators += numOperators;
				if (numOperators > maxNumberOfOperatorsPerFormula) {
					maxNumberOfOperatorsPerFormula = numOperators;
				}
			} catch (Exception e) {
				System.err
						.println("Exception when parsing the formula for operators");
				e.printStackTrace();
			}
		}
		averageNumberOfReferencedCellsPerFormula = (double) sumOfReferencedCells
				/ (double) cells.getFormulaCoords().size();
		averageNumberOfOperatorsPerFormula = (double) sumOfOperators
				/ (double) cells.getFormulaCoords().size();
	}

	/**
	 * Requires that CellMap.populate() is called for the current spreadsheet.
	 * 
	 * @param correctOutputCells
	 * @param faultyCells
	 * @return
	 */
	private int getNumberOfCoincidentalCorrectOutputCells(
			List<Coords> correctOutputCells, Set<Coords> faultyCells) {
		if (numberOfCoincidentalCorrectOutputCells == -1) {
			numberOfCoincidentalCorrectOutputCells = 0;
			for (Coords cell : correctOutputCells) {
				Set<Coords> cone = cells.getICell(cell).getCone(false, true);
//				System.out.println(cell.toString());
				for (Coords faultyCell : faultyCells) {
					if (cone.contains(faultyCell)) {
						numberOfCoincidentalCorrectOutputCells++;
						// System.out.println("coincidental correctness for cell "+cell.toString());
						break;
					}
				}
			}
		}
		return numberOfCoincidentalCorrectOutputCells;
	}

	@Override
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append(propertiesFileName.substring(propertiesFileName.indexOf("Benchmarks"),propertiesFileName.lastIndexOf('.')));
		strB.append(SEPARATOR);
		strB.append(spreadsheetFileName);
		strB.append(SEPARATOR);

		strB.append(numberOfFormulaCells);
		strB.append(SEPARATOR);
		strB.append(averageNumberOfReferencedCellsPerFormula);
		strB.append(SEPARATOR);
		strB.append(averageNumberOfOperatorsPerFormula);
		strB.append(SEPARATOR);
		strB.append(maxNumberOfReferencedCellsPerFormula);
		strB.append(SEPARATOR);
		strB.append(maxNumberOfOperatorsPerFormula);
		strB.append(SEPARATOR);
		
		strB.append(numberOfIfs);
		strB.append(SEPARATOR);

		strB.append(numberOfInputCells);
		strB.append(SEPARATOR);
		strB.append(numberOfErroneousOutputCells);
		strB.append(SEPARATOR);
		strB.append(numberOfCorrectOutputCells);
		strB.append(SEPARATOR);
		strB.append(numberOfCoincidentalCorrectOutputCells);
		strB.append(SEPARATOR);

		strB.append(numberOfFaults);
		strB.append(SEPARATOR);
		strB.append(System.lineSeparator());
		return strB.toString(); //.replace('.', ','); //TODO: FIXME to locale
	}

}
