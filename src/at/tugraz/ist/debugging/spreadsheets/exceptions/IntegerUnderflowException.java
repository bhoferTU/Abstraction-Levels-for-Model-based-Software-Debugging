package at.tugraz.ist.debugging.spreadsheets.exceptions;

/**
 * 
 * exception for integer values that are too small to handle
 * 
 * @author egetzner
 * 
 */
public class IntegerUnderflowException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2417943702461898259L;
	private int number, minimum;

	/**
	 * 
	 * @param num
	 *            actual number
	 * @param max
	 *            expected maximum
	 */
	public IntegerUnderflowException(int num, int max) {
		this.number = num;
		this.minimum = max;
	}

	@Override
	public String getLocalizedMessage() {
		return "IntegerUnderflowException: " + number + " too small, min: "
				+ minimum;
	}

}
