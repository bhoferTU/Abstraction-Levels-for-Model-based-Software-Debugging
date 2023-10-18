package at.tugraz.ist.debugging.spreadsheets.exceptions;

/**
 * exception for integer values that are too large to handle
 * 
 * @author egetzner
 * 
 */
public class IntegerOverflowException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1615076448118677784L;
	private int number, maximum;

	/**
	 * 
	 * @param num
	 *            the number we tried to get
	 * @param max
	 *            the maximum number we can handle
	 */
	public IntegerOverflowException(int num, int max) {
		this.number = num;
		this.maximum = max;
	}

	@Override
	public String getLocalizedMessage() {
		return "IntegerOverflowException: " + number + " too large, max: "
				+ maximum;
	}

}
