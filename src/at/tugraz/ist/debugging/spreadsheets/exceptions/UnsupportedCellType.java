package at.tugraz.ist.debugging.spreadsheets.exceptions;

/**
 * 
 * exception for an unsupported cell type
 * 
 * @author egetzner
 * 
 */
public class UnsupportedCellType extends Exception {

	/**
   * 
   */
	private static final long serialVersionUID = 503641770551599856L;

	private String type, wanted;

	/**
	 * 
	 * @param type
	 * @param wanted
	 */
	public UnsupportedCellType(String type, String wanted) {
		this.type = type;
		this.wanted = wanted;
	}

	@Override
	public String getLocalizedMessage() {

		return "Unsupported Cell Type: " + type + " cannot be converted to "
				+ wanted;
	}
}
