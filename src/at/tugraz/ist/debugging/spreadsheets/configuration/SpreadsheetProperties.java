package at.tugraz.ist.debugging.spreadsheets.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.exceptions.CoordinatesException;
import at.tugraz.ist.util.fileManipulation.FileTools;

/**
 * This class contains all information about the spreadsheet that is debugged.
 * 
 * @author bhofer, egetzner
 * 
 */
public class SpreadsheetProperties {

	public static SpreadsheetProperties generate(String projectFile)
			throws SpreadsheetPropertiesException {
		return new SpreadsheetProperties(projectFile);
	}

	private Properties configFile;

	private String excelSheetName = "";
	private String propertyFileName;

	protected List<Coords> correctOutputCells = new ArrayList<Coords>();
	protected Map<Coords, String> faultMapping = new HashMap<Coords, String>();
	protected Map<Coords, String> incorrectOutCellsWithExpectedValue = new HashMap<Coords, String>();

	/**
	 * list of faulty cells (wrong formulas)
	 */
	private List<Coords> faultyCells = null;


	public SpreadsheetProperties() {

	}
	

	
	public SpreadsheetProperties copy(){
		SpreadsheetProperties newProperties = new SpreadsheetProperties();
		newProperties.configFile = this.configFile;
		newProperties.excelSheetName = this.excelSheetName;
		newProperties.propertyFileName = this.propertyFileName;
		for(Coords coords: this.correctOutputCells){
			newProperties.correctOutputCells.add(coords);
		}
		for(Coords coords: this.faultMapping.keySet()){
			newProperties.faultMapping.put(coords, this.faultMapping.get(coords));
		}
		for(Coords coords: this.incorrectOutCellsWithExpectedValue.keySet()){
			newProperties.incorrectOutCellsWithExpectedValue.put(coords, this.incorrectOutCellsWithExpectedValue.get(coords));
		}
		
		return newProperties;
	}

	/**
	 * Reads the spreadsheet information from a simple txt-file and saves the
	 * data within the created object. The txt-file has the following format:
	 * 
	 * <pre>
	 *  # Lines starting with '#' are comments
	 *  # notation: worksheet!column!row, e.g. 0!A!3
	 *  #           worksheet starts with 0
	 *  #           column starts with A (see Excel notation)
	 *  #           row starts with 1
	 *  
	 *  # path to the excel file
	 *  EXCEL_SHEET=test.xls
	 *  
	 *  #  for correct output cells, i = ascending number 
	 *  CORRECT_OUTPUT_i=0!A!100
	 *  
	 *  # for incorrect output cells, i = ascending number 
	 *  INCORRECT_OUTPUT_i=1!A!1 
	 *  
	 *  # expected value for the corresponding output cell
	 *  INCORRECT_OUTCELL_EXPECTED_VALUE_i
	 *   
	 *  # for cells containing wrong formulas, i = ascending number
	 *  FAULTY_CELLS_i=1!C!4
	 *  
	 *  # fault type for the corresponding faulty formula cell
	 *  FAULT_TYPE_i=FRC
	 * </pre>
	 * 
	 * @param projectFile
	 *            A text file with the spreadsheet information
	 * @throws SpreadsheetPropertiesException
	 *             Is thrown when the projectFile could not be found or when the
	 *             file does not contain the excel file name or the faulty cells
	 */
	public SpreadsheetProperties(String projectFile)
			throws SpreadsheetPropertiesException {
		File file;
		InputStream iS = null;
		try {
			file = new File(projectFile);

			iS = new FileInputStream(file);

			configFile = new Properties();
			configFile.load(iS);

			propertyFileName = projectFile;
			excelSheetName = getProperty("EXCEL_SHEET");
			correctOutputCells = getListOfCoordinates("CORRECT_OUTPUT", false);

			setIncorrectMapping();
			setFaultMapping();

		} catch (SpreadsheetPropertiesException e) {
			throw e;
		} catch (Exception e) {
			throw new SpreadsheetPropertiesException(
					"Error in initializing ProjectInformation when loading file "
							+ projectFile + ": " + e.getMessage());
		} finally {
			try {
				if (iS != null)
					iS.close();
			} catch (IOException e) {
				// Silent catch
			}
		}

	}

	/**
	 * @return List of the output cells which contain the expected (correct
	 *         value)
	 */
	public List<Coords> getCorrectOutputCells() {
		return correctOutputCells;
	}

	/**
	 * @return Path to the Excelsheet that is debugged
	 */
	public String getExcelSheetName() {
		return excelSheetName;
	}

	/**
	 * @return Absolute Path to the Excelsheet that is debugged
	 */
	public String getExcelSheetPath() {
		String excelRelativePath = FileTools
				.convertPathToSystemPath(excelSheetName);

		return FileTools.findAbsoluteFilename(propertyFileName, excelRelativePath);
	}

	/**
	 * @return Map of cells that contains faulty formulas as keys and the
	 *         corresponding fault types as values.
	 */
	public Map<Coords, String> getFaultMapping() {
		return faultMapping;
	}

	/**
	 * Gets the faulty cells
	 * 
	 * @return faulty Cells
	 */
	public List<Coords> getFaultyCells() {
		if (faultyCells == null) {
			faultyCells = new ArrayList<Coords>(faultMapping.keySet());

			// faultyCells = new Vector<String>();
			// for (Coords coordinate : faultMapping.keySet()) {
			// faultyCells.add(convertCoordinatesToCellposition(coordinate));
			// }
		}
		return faultyCells;
	}

	/**
	 * @return List of the output cells which do NOT contain the expected
	 */
	public List<Coords> getIncorrectOutputCells() {
		return new ArrayList<Coords>(
				incorrectOutCellsWithExpectedValue.keySet());
	}

	/**
	 * @return Map of the incorrect output cell and the expected value. The map
	 *         contains null as value for those output cells where the expected
	 *         value is not known.
	 */
	public Map<Coords, String> getIncorrectOutputCellsWithExpectedValue() {
		return incorrectOutCellsWithExpectedValue;
	}

	@SuppressWarnings("unchecked")
	private List<Coords> getListOfCoordinates(String propertyName,
			Boolean exception) throws SpreadsheetPropertiesException,
			CoordinatesException {
		List<Coords> properties = new ArrayList<Coords>();

		Enumeration<String> enumerator = (Enumeration<String>) configFile
				.propertyNames();
		while (enumerator.hasMoreElements()) {
			String currentProp = enumerator.nextElement();
			if (currentProp.startsWith(propertyName)) {
				String coordinatesString = configFile.getProperty(currentProp);
				Coords coordinates = new Coords(coordinatesString);
				properties.add(coordinates);
			}
		}
		if (properties.size() == 0 && exception == true)
			throw new SpreadsheetPropertiesException("Property " + propertyName
					+ " not found!");
		return properties;
	}

	@SuppressWarnings({ "unchecked" })
	protected List<String> getListOfProperties(String propertyName,
			Boolean exception) throws SpreadsheetPropertiesException {
		List<String> properties = new ArrayList<String>();

		Enumeration<String> enumerator = (Enumeration<String>) configFile
				.propertyNames();
		while (enumerator.hasMoreElements()) {
			String currentProp = enumerator.nextElement();
			if (currentProp.startsWith(propertyName))
				properties.add(configFile.getProperty(currentProp));
		}
		if (properties.size() == 0 && exception == true)
			throw new SpreadsheetPropertiesException("Property " + propertyName
					+ " not found!");

		return properties;
	}

	public String getProperty(String propertyName)
			throws SpreadsheetPropertiesException {
		String property = configFile.getProperty(propertyName);
		if (property == null)
			throw new SpreadsheetPropertiesException("Property " + propertyName
					+ " not found!");

		return FileTools.convertPathToSystemPath(property);
	}

	/**
	 * @return File name of the properties file represented by this instance
	 */
	public String getPropertyFileName() {
		return propertyFileName;
	}

	/**
	 * Creates a new file and saves the information of object in it in the
	 * preferred format (see {@link #SpreadsheetProperties(String)})
	 * 
	 * @param fileName
	 *            Path to the file that should be created
	 */
	public void saveToFile(String fileName) {
		File file = new File(fileName);
		BufferedWriter fW = null;

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			fW = new BufferedWriter(new FileWriter(fileName, false));

			fW.write("# simple configuration file" + System.lineSeparator());
			fW.write("# notation: worksheet!column!row, e.g. 0!A!3"
					+ System.lineSeparator());
			fW.write("#           worksheet starts with 0"
					+ System.lineSeparator());
			fW.write("#           column starts with A (see Excel notation)"
					+ System.lineSeparator());
			fW.write("#           row starts with 1" + System.lineSeparator()
					+ System.lineSeparator());

			fW.write("EXCEL_SHEET="
					+ excelSheetName.replace("\\", "\\\\").replace("/", "\\\\"));

			fW.write(System.lineSeparator());
			fW.write(System.lineSeparator());

			int correct = 0;
			for (Coords cell : correctOutputCells) {
				StringBuilder strB = new StringBuilder();
				strB.append("CORRECT_OUTPUT_");
				strB.append(++correct);
				strB.append("=");
				strB.append(cell.getCSVString());
				strB.append(System.lineSeparator());
				fW.write(strB.toString());
			}
			fW.write(System.lineSeparator());

			int incorrect = 0;
			for (Coords cell : incorrectOutCellsWithExpectedValue.keySet()) {
				StringBuilder strB = new StringBuilder();
				strB.append("INCORRECT_OUTPUT_");
				strB.append(++incorrect);
				strB.append("=");
				strB.append(cell.getCSVString());
				strB.append(System.lineSeparator());
				if (incorrectOutCellsWithExpectedValue.get(cell) != null) {
					strB.append("INCORRECT_OUTCELL_EXPECTED_VALUE_");
					strB.append(incorrect);
					strB.append("=");
					strB.append(incorrectOutCellsWithExpectedValue.get(cell));
					strB.append(System.lineSeparator());
				}
				fW.write(strB.toString());
			}
			fW.write(System.lineSeparator());

			int faulty = 0;
			for (Coords cell : faultMapping.keySet()) {
				StringBuilder strB = new StringBuilder();
				strB.append("FAULTY_CELLS_");
				strB.append(++faulty);
				strB.append("=");
				strB.append(cell.getCSVString());
				strB.append(System.lineSeparator());

				strB.append("FAULT_TYPE_");
				strB.append(faulty);
				strB.append("=");
				strB.append(faultMapping.get(cell));
				strB.append(System.lineSeparator());

				fW.write(strB.toString());
			}

			fW.flush();
			fW.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @param correctOutputCells
	 *            List of the output cells which contain the expected (correct
	 *            value)
	 */
	public void setCorrectOutputCells(List<Coords> correctOutputCells) {
		this.correctOutputCells = correctOutputCells;
	}

	/**
	 * @param excelSheetName
	 *            Path to the Excelsheet that is debugged
	 */
	public void setExcelSheetName(String excelSheetName) {
		this.excelSheetName = excelSheetName;
	}

	@SuppressWarnings("unchecked")
	private void setFaultMapping() throws CoordinatesException,
			SpreadsheetPropertiesException {
		faultMapping = new HashMap<Coords, String>();

		Enumeration<String> enumerator = (Enumeration<String>) configFile
				.propertyNames();
		while (enumerator.hasMoreElements()) {
			String currentProp = enumerator.nextElement();
			if (currentProp.startsWith("FAULTY_CELLS")) {
				String ending = currentProp.substring("FAULTY_CELLS".length());
				String faultyCell = configFile.getProperty(currentProp);
				String faultType = configFile
						.getProperty("FAULT_TYPE" + ending);
				if (faultType == null)
					throw new SpreadsheetPropertiesException(
							"There exists no fault type (FAULT_TYPE" + ending
									+ ") for " + currentProp);
				Coords coordinates = new Coords(faultyCell);
				faultMapping.put(coordinates, faultType);
			}
		}
	}

	/**
	 * @param faultMapping
	 *            Map of cells that contains faulty formulas as keys and the
	 *            corresponding fault types as values.
	 */
	public void setFaultMapping(Map<Coords, String> faultMapping) {

		this.faultMapping = faultMapping;
	}

	@SuppressWarnings("unchecked")
	private void setIncorrectMapping() throws CoordinatesException,
			SpreadsheetPropertiesException {
		incorrectOutCellsWithExpectedValue = new HashMap<Coords, String>();

		Enumeration<String> enumerator = (Enumeration<String>) configFile
				.propertyNames();

		while (enumerator.hasMoreElements()) {
			String currentProp = enumerator.nextElement();

			if (currentProp.startsWith("INCORRECT_OUTPUT")) {

				String ending = currentProp.substring("INCORRECT_OUTPUT"
						.length());

				String incorrectCell = configFile.getProperty(currentProp);

				String expectedValue = configFile
						.getProperty("INCORRECT_OUTCELL_EXPECTED_VALUE"
								+ ending);

				if (expectedValue != null)
					expectedValue = expectedValue.trim();

				Coords coordinates = new Coords(incorrectCell);

				incorrectOutCellsWithExpectedValue.put(coordinates,
						expectedValue);

			}
		}
	}

	/**
	 * @param incorrectCellsWithExpectedValue
	 *            Map of the incorrect output cell and the expected value. The
	 *            map should contain null as value for those output cells where
	 *            the expected value is not known.
	 */
	public void setIncorrectOutputCellsWithExpectedValue(
			Map<Coords, String> incorrectCellsWithExpectedValue) {
		this.incorrectOutCellsWithExpectedValue = incorrectCellsWithExpectedValue;
	}

}
