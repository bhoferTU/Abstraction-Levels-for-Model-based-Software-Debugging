package at.tugraz.ist.debugging.spreadsheets.expressions.evaluation;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;

/**
 * Represents a cell evaluation exception
 * 
 */
public class EvaluationException extends RuntimeException {

	private static final long serialVersionUID = -6588733812644446575L;

	String cellName = "?";
	String expression = "?";

	public EvaluationException(String message) {
		super(message);
	}

	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getMessage() {
		return cellName + ": (=" + expression + ") " + super.getMessage();
	}
	
	public EvaluationException setCell(Coords cellCoords) {
		//because this is an Exception, we print the human readable string
		this.cellName = cellCoords.getUserString(); 
		return this;
	}

	public EvaluationException setExpression(String expression) {
		this.expression = expression;
		return this;
	}
}
