package at.tugraz.ist.debugging.spreadsheets.exceptions;

/**
 * 
 * exception for a filetype that is not supported
 * 
 * @author egetzner
 * 
 */
public class InvalidFiletypeException extends Exception {

	/**
   * 
   */
	private static final long serialVersionUID = 3416590873205960074L;
	private String filename;

	/**
	 * for use with POIReader, invalid file
	 * 
	 * @param filename
	 */
	public InvalidFiletypeException(String filename) {
		this.filename = filename;
	}

	@Override
	public String getLocalizedMessage() {
		return "Invalid filetype for file: " + filename;
	}

}
