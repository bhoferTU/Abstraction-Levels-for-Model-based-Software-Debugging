package at.tugraz.ist.debugging.spreadsheets.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICell;
import at.tugraz.ist.debugging.spreadsheets.exceptions.CoordinatesException;
import at.tugraz.ist.util.fileManipulation.FileTools;

/**
 * This class serves the purpose of validating spreadsheet-property files.
 * 
 * @author pkoch
 * 
 */
public class SpreadsheetPropertiesValidator {

	/**
	 * List of Messages describing the errors which occurred during the last
	 * invocation of the checkPropertyFile method.
	 */
	private static List<String> errorMessages;

	/**
	 * String containing the path to the referenced excel sheet, if obtainable
	 * Filled by the method checkPropertyValues.
	 */
	private static String excelSheetName;

	/**
	 * Set which contains all category-property names which will be expected.
	 */
	private static Set<String> expectedCategoryProperties;

	/**
	 * Set which contains all unique property names which will be expected.
	 */
	private static Set<String> expectedUniqueProperties;

	/**
	 * If set, enables validation of the excel sheet.
	 */
	public static final int FLAG_CHECK_EXCEL_SHEET = 2;

	/**
	 * If set, enables validation of the contained property names. Disabling
	 * this flag also disables the checks for property values and the
	 * excel-sheet.
	 */
	public static final int FLAG_CHECK_PROPERTY_NAMES = 0;

	/**
	 * If set, enables validation of the contained property values. Disabling
	 * this flag also disables the check of the excel-sheet.
	 */
	public static final int FLAG_CHECK_PROPERTY_VALUES = 1;

	/**
	 * If set, report non-incremental property identifiers . Only takes effect
	 * if flagUniquePropertyNames and flagIntegerIdentifiers are set as well.
	 */
	public static final int FLAG_INCREMENTING_IDENTIFIERS = 7;

	/**
	 * if set, report non-integer property identifiers. (e.g. CORRECT_OUTPUT_A)
	 */
	public static final int FLAG_INTEGER_IDENTIFIERS = 6;

	/**
	 * If set, report invalid lines in property file.
	 */
	public static final int FLAG_REPORT_INVALID_LINES = 3;

	/**
	 * If set, report for syntactically valid but unknown properties.
	 */
	public static final int FLAG_REPORT_UNKNOWN_PROPERTIES = 4;

	/**
	 * If set, report duplicate property names
	 */
	public static final int FLAG_UNIQUE_PROPERTY_NAMES = 5;

	/**
	 * Boolean flags to customize validation process.
	 */
	private static boolean[] flags;

	/**
	 * Map containing all found coordinate-strings which were assigned to one of
	 * the property categories. Filled by the method checkPropertyValues.
	 */
	private static Map<String, Set<String>> knownPropertyCoordinates;

	/**
	 * List of property names which will be filled by the readPropertyFile
	 * method.
	 */
	private static List<String> properties;

	/**
	 * Initialize basic flag configuration. Initially, all flags are enable.
	 */
	static {
		flags = new boolean[8];
		resetFlags();
	}

	/**
	 * Initialize basic expected unique property names. Initially only
	 * EXCEL_SHEET will be expected.
	 */
	static {
		expectedUniqueProperties = new HashSet<String>();
		addExpectedUniqueProperty("EXCEL_SHEET");
	}

	/**
	 * Initialize basic expected category-property names. Initially contains
	 * CORRECT_OUTPUT, INCORRECT_OUTPUT, INCORRECT_OUTCELL_EXPECTED_VALUE,
	 * FAULTY_CELLS and FAULTY_TYPE.
	 */
	static {
		expectedCategoryProperties = new HashSet<String>();
		addExpectedCategoryProperty("CORRECT_OUTPUT");
		addExpectedCategoryProperty("INCORRECT_OUTPUT");
		addExpectedCategoryProperty("INCORRECT_OUTCELL_EXPECTED_VALUE");
		addExpectedCategoryProperty("FAULTY_CELLS");
		addExpectedCategoryProperty("FAULT_TYPE");
	}

	/**
	 * Initialize empty list for error messages.
	 */
	static {
		errorMessages = new ArrayList<String>();
	}

	/**
	 * Adds a message to the list of occurring errors.
	 * 
	 * @param message
	 *            Message which will be added.
	 */
	private static void addErrorMessage(String message) {
		errorMessages.add(message);
	}

	/**
	 * Adds a category-property name which will be expected during validation.
	 * 
	 * @param name
	 *            The name of the property which will be added.
	 */
	public static void addExpectedCategoryProperty(String name) {
		expectedCategoryProperties.add(name);
	}

	/**
	 * Adds an unique property name which will be expected during validation.
	 * 
	 * @param name
	 *            The name of the property which will be added.
	 */
	public static void addExpectedUniqueProperty(String name) {
		expectedUniqueProperties.add(name);
	}

	/**
	 * Validates the excel sheet in reference to the expected cell formats.
	 * Reported errors: missing EXCEL_SHEET property; unresolved path to
	 * spreadsheet; inaccessible spreadsheet file; unexpected non-formula
	 * cell-types; invalid coordinate strings. Used flags: none
	 * 
	 * @param filename
	 *            Name of the .properties file
	 * @return boolean success of validation
	 */
	private static boolean checkExcelSheet(String filename) {
		// check if name was successfully obtained
		if (excelSheetName == null) {
			// abort
			addErrorMessage("No EXCEL_SHEET property could be obtained.");
			return false;
		}

		// otherwise
		// success indicator
		boolean success = true;

		String absolutePath = FileTools.findAbsoluteFilename(
				filename, excelSheetName);

		if (absolutePath == null) {
			addErrorMessage("The referenced excel sheet " + excelSheetName
					+ " can't be found on the filesystem.");
			return false;
		}

		// check if referenced excel sheet can be accessed
		File excelSheet = new File(absolutePath);
		if (!excelSheet.canRead()) {
			addErrorMessage("The referenced excel sheet " + excelSheetName
					+ " can't be accessed.");
			return false;
		}

		// check if referenced cells contain formulas
		CellContainer cells = CellContainer.create(absolutePath);

		try {

			if (cells == null)
				throw new Exception();

		} catch (Exception e) {
			addErrorMessage("The referenced excel sheet " + excelSheetName
					+ " can't be accessed.");
			return false;
		}

		System.out.println("check correct cells");
		// check correct cells
		for (String coord : knownPropertyCoordinates.get("CORRECT_OUTPUT_")) {
			ICell cell;
			try {
				cell = cells.getICell(new Coords(coord));
				if (cell == null || !(cell.isFormulaCell())) {
					addErrorMessage("The correct output cell " + coord
							+ " does not contain a formula.");
					success = false;
				}
			} catch (CoordinatesException e) {
				addErrorMessage("The CORRECT_OUTPUT properties contain a non-valid coordinate value "
						+ coord);
				success = false;
			}
		}

		// check faulty cells
		for (String coord : knownPropertyCoordinates.get("FAULTY_CELLS_")) {
			try {
				ICell cell = cells.getICell(new Coords(coord));
				if (cell == null || !(cell.isFormulaCell())) {
					addErrorMessage("The faulty cell " + coord.toString()
							+ " does not contain a formula.");
					success = false;
				}
			} catch (CoordinatesException e) {
				addErrorMessage("The FAULTY_CELLS properties contain a non-valid coordinate value "
						+ coord);
				success = false;
			}
		}

		// check incorrect cells
		for (String coord : knownPropertyCoordinates.get("INCORRECT_OUTPUT_")) {
			try {
				ICell cell = cells.getICell(new Coords(coord));
				if (cell == null || !(cell.isFormulaCell())) {
					addErrorMessage("The incorrect cell " + coord.toString()
							+ " does not contain a formula.");
					success = false;
				}
			} catch (CoordinatesException e) {
				addErrorMessage("The INCORRECT_OUTPUT properties contain a non-valid coordinate value "
						+ coord);
				success = false;
			}
		}

		return success;
	}

	/**
	 * Validates a .properties file, defined by its file path.
	 * <p>
	 * This method is intended to be used prior to the construction of a
	 * SpreadsheetProperties object from a specific .properties file. The checks
	 * are subsequent and follow the basic order:
	 * <ol>
	 * <li>parse .properties file</li>
	 * <li>check property names</li>
	 * <li>check property values</li>
	 * <li>check excel sheet</li>
	 * </ol>
	 * To which detail the file will be validated may be controlled by the flags
	 * FLAG_CHECK_PROPERTY_NAMES, FLAG_CHECK_PROPERTY_VALUES and
	 * FLAG_CHECK_EXCEL_SHEET. Setting one of this flags to false will also
	 * prohibit consecutive checks. Additionally, further flags can be used to
	 * skip specific validations.
	 * </p>
	 * <p>
	 * The list of initially reported errors contains
	 * <ul>
	 * <li>malformed lines in .properties file</li>
	 * <li>inaccessible .properties file</li>
	 * <li>multiple use of the same property name</li>
	 * <li>invalid use of unique properties (e.g. EXCEL_SHEET)</li>
	 * <li>non-integer property enumeration</li>
	 * <li>non-incremental property enumeration</li>
	 * <li>unexpected property names</li>
	 * <li>duplicate coordinates within a category</li>
	 * <li>coordinate overlap of correct and incorrect cells</li>
	 * <li>missing EXCEL_SHEET property</li>
	 * <li>unresolved path to spreadsheet</li>
	 * <li>inaccessible spreadsheet file</li>
	 * <li>unexpected non-formula cell-types</li>
	 * <li>invalid coordinate strings</li>
	 * </ul>
	 * </p>
	 * NOTE: the provided excel sheet file needs to be accessible of a
	 * parent-folder of the provided .properties file!
	 * 
	 * @param filename
	 *            Path to .properties file which will be validated.
	 * @return Boolean which indicates the success of the validation. If false
	 *         is returned, the occurred errors can be polled by the
	 *         getErrorMessages method.
	 */
	public static boolean checkPropertiesFile(String filename) {

		clearErrorMessages();
		boolean success = true;

		// start checking..
		// open file and load property keys into list
		success = success && readPropertyFile(filename);

		// parse found properties
		if (!flags[FLAG_CHECK_PROPERTY_NAMES])
			return success;
		success = checkPropertyNames() && success;

		// check validity of property values and obtain name of excel-sheet
		excelSheetName = null;
		if (!flags[FLAG_CHECK_PROPERTY_VALUES])
			return success;
		success = checkPropertyValues(filename) && success;

		// check accessibility and contents of excel sheet
		if (!flags[FLAG_CHECK_EXCEL_SHEET])
			return success;
		success = checkExcelSheet(filename) && success;

		// return resulting verdict
		return success;
	}

	/**
	 * Validates the property names provided by the preceding processing steps.
	 * Reported errors: multiple property names; invalid use of unique
	 * properties; non-integer property enumeration; non-incremental property
	 * enumeration; unexpected property names. Used flags:
	 * FLAG_UNIQUE_PROPERTY_NAMES, FLAG_INTEGER_IDENTIFIERS,
	 * FLAG_INCREMENTING_IDENTIFIERS, FLAG_REPORT_UNKNOWN_PROPERTIES
	 * 
	 * @return boolean success of validation
	 */
	private static boolean checkPropertyNames() {
		// datastructure to check for unique properties
		Set<String> knownProperties = new HashSet<String>();

		// datastructure to check for incrementing identifiers
		Map<String, Integer> nextPropertyIndices = new HashMap<String, Integer>();
		for (String key : expectedCategoryProperties)
			nextPropertyIndices.put(key, 1);

		boolean success = true;

		// parse entries
		for (String property : properties) {

			// check name uniqueness
			if (knownProperties.contains(property)
					&& flags[FLAG_UNIQUE_PROPERTY_NAMES]) {
				addErrorMessage("Propertyfile contains multiple occurances of the following property name: "
						+ property);
				success = false;
			}
			knownProperties.add(property);

			// indicates whether the current property is part of a known
			// category
			boolean propertyHandled = false;

			// check unique properties
			for (String key : expectedUniqueProperties) {
				if (property.startsWith(key)) {
					propertyHandled = true;
					// check for exact match
					if (!key.equals(property)) {
						addErrorMessage("Invalid use of sub category "
								+ property + " of unique property " + key);
						success = false;
					}
				}
			}

			// check catetory properties
			for (String key : expectedCategoryProperties) {
				if (property.startsWith(key)) {
					propertyHandled = true;

					// integer-enumeration based checks
					if (flags[FLAG_INTEGER_IDENTIFIERS]) {
						try {
							int index = Integer.parseInt(property.substring(key
									.length() + 1));

							// check for incremental identifiers
							if (flags[FLAG_INCREMENTING_IDENTIFIERS]) {
								if (nextPropertyIndices.get(key) != index) {
									addErrorMessage("Property name is non-incremental: "
											+ property);
									success = false;
								} else
									nextPropertyIndices.put(key, index + 1);
							}

						} catch (NumberFormatException e) {
							// only integer identifiers are allowed
							addErrorMessage("Property name is non-integer: "
									+ property);
							success = false;
						}
					}
				}
			}

			// no matching property has been foundproperty is of unknown
			// category
			if (!propertyHandled && flags[FLAG_REPORT_UNKNOWN_PROPERTIES]) {
				addErrorMessage("PropertyFile contains unknown property: "
						+ property);
				success = false;
			}
		}

		return success;
	}

	/**
	 * Validates the property values contained in the .properties file. Also
	 * provides the sets of coordinate strings per category and the filename of
	 * the excel sheet for further processing. Reported errors: duplicate
	 * coordinates within a category; overlap of correct and incorrect cells.
	 * Used flags: none
	 * 
	 * @param filename
	 *            Name of the .properties file
	 * @return boolean success of validation
	 */
	@SuppressWarnings("unchecked")
	private static boolean checkPropertyValues(String filename) {
		boolean success = true;

		// each propertyCategory which contains coordinates may only contain
		// each coordinate once
		// set of properties to check coordinate entries for
		Set<String> coordinateProperties = new HashSet<String>();
		coordinateProperties.add("CORRECT_OUTPUT_");
		coordinateProperties.add("INCORRECT_OUTPUT_");
		coordinateProperties.add("FAULTY_CELLS_");

		// map of set of coordinate strings each property category already
		// contains
		knownPropertyCoordinates = new HashMap<String, Set<String>>();
		for (String property : coordinateProperties)
			knownPropertyCoordinates.put(property, new HashSet<String>());

		// create config file to get values from
		Properties configFile = new Properties();
		try {
			configFile.load(new FileInputStream(filename));
		} catch (Exception e) {
			addErrorMessage("ConfigFile can't be accessed during process.");
			return false;
		}

		// now check each property in the config file and record the coordinates
		Enumeration<String> enumerator = (Enumeration<String>) configFile
				.propertyNames();
		while (enumerator.hasMoreElements()) {
			String currentProp = enumerator.nextElement();
			for (String coordinateProperty : coordinateProperties) {
				if (currentProp.startsWith(coordinateProperty)) {
					String coordinatesString = configFile
							.getProperty(currentProp);
					if (knownPropertyCoordinates.get(coordinateProperty)
							.contains(coordinatesString)) {
						addErrorMessage("Duplicate coordinate "
								+ coordinatesString + " in property "
								+ coordinateProperty);
						success = false;
					} else
						knownPropertyCoordinates.get(coordinateProperty).add(
								coordinatesString);
				}
			}
		}

		// no incorrect cell may be contained in the list of correct cells
		for (String coord : knownPropertyCoordinates.get("INCORRECT_OUTPUT_"))
			if (knownPropertyCoordinates.get("CORRECT_OUTPUT_").contains(coord)) {
				addErrorMessage("Coordinate " + coord
						+ " is marked as correct and incorrect simultaneously.");
				success = false;
			}

		// extract EXCEL_SHEET property for further use
		excelSheetName = FileTools.convertPathToSystemPath(configFile
				.getProperty("EXCEL_SHEET"));

		return success;
	}

	/**
	 * Clears the list of error messages.
	 */
	private static void clearErrorMessages() {
		errorMessages.clear();
	}

	/**
	 * Returns a list, containing messages that describe the errors which
	 * occurred during the last invocation of the checkPropertyFile method.
	 */
	public static List<String> getErrorMessages() {
		return errorMessages;
	}

	/**
	 * Reads from the provided .properties file and adds fitting property-names
	 * to the properties data-structure for further processing. Reported errors:
	 * malformed lines in .properties file; inaccessible file. Used flags:
	 * FLAG_REPORT_INVALID_LINES
	 * 
	 * @param filename
	 *            Name of the .properties file
	 * @return boolean success of validation
	 */
	private static boolean readPropertyFile(String filename) {
		// open file and load property keys into list
		File propertyFile = new File(filename);
		properties = new ArrayList<String>();
		boolean success = true;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(propertyFile)));
			String line;
			// for each line
			while ((line = br.readLine()) != null) {
				// remove leading white spaces
				line = line.trim();

				// skip empty lines and comment lines, and lines not containing
				// a property
				if (line.length() == 0 || line.charAt(0) == '#')
					continue;

				if (line.indexOf('=') <= 0) {// invalid line
					if (flags[FLAG_REPORT_INVALID_LINES]) {
						addErrorMessage("Invalid entry in property file: "
								+ line);
						success = false;
					}
					continue;
				}
				properties.add(line.substring(0, line.indexOf('=')));

			}

			br.close();
		} catch (IOException e) {
			addErrorMessage("PropertyFile cannot be accessed: "
					+ e.getMessage());
			return false;
		}
		return success;
	}

	/**
	 * Removes n category-property from the set of expected unique properties.
	 * 
	 * @param name
	 *            The name of the property which will be removed.
	 */
	public static void removeExpectedCategoryProperty(String name) {
		expectedCategoryProperties.remove(name);
	}

	/**
	 * Removes an unique property from the set of expected unique properties.
	 * 
	 * @param name
	 *            The name of the property which will be removed.
	 */
	public static void removeExpectedUniqueProperty(String name) {
		expectedUniqueProperties.remove(name);
	}

	/**
	 * Resets all flags to their default values.
	 */
	public static void resetFlags() {
		flags[FLAG_CHECK_PROPERTY_NAMES] = true; // 0
		flags[FLAG_CHECK_PROPERTY_VALUES] = true; // 1
		flags[FLAG_CHECK_EXCEL_SHEET] = true; // 2
		flags[FLAG_REPORT_INVALID_LINES] = true; // 3
		flags[FLAG_REPORT_UNKNOWN_PROPERTIES] = true; // 4
		flags[FLAG_UNIQUE_PROPERTY_NAMES] = true; // 5
		flags[FLAG_INTEGER_IDENTIFIERS] = true; // 6
		flags[FLAG_INCREMENTING_IDENTIFIERS] = true; // 7
	}

	/**
	 * Enables or disables a specified validation flag.
	 * 
	 * @param flag
	 *            Flag which will be set.
	 * @param value
	 *            Value the flag will be set to.
	 */
	public static void setFlag(int flag, boolean value) {
		flags[flag] = value;
	}
}
