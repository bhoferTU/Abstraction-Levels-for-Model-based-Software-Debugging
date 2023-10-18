package at.tugraz.ist.debugging.spreadsheets.evaluation.ranking;

import at.tugraz.ist.util.datastructures.Pair;

public class RankPosition extends Pair<Integer, Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param before
	 *            : the number of cells before this one, 0 if none, -1 if not in
	 *            ranking
	 * @param same
	 *            : the number of cells with the same rank as this one,
	 *            including this cell, 0 if not in ranking
	 */
	public RankPosition(int before, int same) {
		super(before, same);
	}

	public RankPosition()
	{
		super(0,-1);
	}
	
	public Integer getNumItemsBefore() {
		return getFirst();
	}

	public Integer getNumItemsSame() {
		return getSecond();
	}
}
