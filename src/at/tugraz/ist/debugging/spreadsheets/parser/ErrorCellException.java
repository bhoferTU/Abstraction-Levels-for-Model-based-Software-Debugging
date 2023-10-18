package at.tugraz.ist.debugging.spreadsheets.parser;

/**
 * Corresponds to an erroneous cell exception
 * 
 */
public class ErrorCellException extends RuntimeException {

	private static final long serialVersionUID = 1955798432545479731L;
	String cellName = "?";

	public ErrorCellException(String message) {
		super(message);
	}

	public String getCellName() {
		return cellName;
	}

	@Override
	public String getMessage() {
		return cellName + ": " + super.getMessage();
	}

	public ErrorCellException setCell(String cellName) {
		this.cellName = cellName;
		return this;
	}
}
