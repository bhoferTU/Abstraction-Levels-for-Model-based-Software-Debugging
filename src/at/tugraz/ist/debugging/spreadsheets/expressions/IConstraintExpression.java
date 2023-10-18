package at.tugraz.ist.debugging.spreadsheets.expressions;

import java.util.Set;

/**
 * Base class for constant and complex expressions which are combined in a tree
 * structure and/or associated with a certain cell instance
 * 
 */
public interface IConstraintExpression extends IConstraint, IExpression {

	/**
	 * Recursively collects all expressions which are used as a condition in an
	 * if-then-else structure.
	 * 
	 * @return
	 */
	public Set<IConstraintExpression> getConditionalExpressions();

}
