package at.tugraz.ist.debugging.spreadsheets.configuration;

/**
 * This exception is thrown in the constructor
 * {@link SpreadsheetProperties#SpreadsheetProperties(String)} when the
 * projectFile could not be found or when the file does not contain the excel
 * file name or the faulty cells
 * 
 * @author bhofer
 * 
 */
public class SpreadsheetPropertiesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SpreadsheetPropertiesException(String msg) {
		super(msg);
	}

}
