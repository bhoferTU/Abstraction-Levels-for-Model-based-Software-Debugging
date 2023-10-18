package at.tugraz.ist.debugging.spreadsheets.evaluation.ranking;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * each cell has a value attached to it that indicates suspiciousness of
 * containing the fault this value allows us to rank cells, starting with high
 * suspiciousness
 * 
 * additionally, cells may have the same value, creating a tie. therefore, this
 * interface maps to {RankEntry}, allowing one value have an arbitrary number of
 * cells attached to it.
 * 
 * @author egetzner
 * 
 */

public interface IRanking<K> {


	/**
	 * compare two Rankings, regardless of their values their RankEntries must
	 * have the same positions with the same cells contained
	 * 
	 * @param o
	 * @return
	 */
	public boolean equals(Object o);
	
	/**
	 * 
	 * @param cell
	 * @return the value/suspiciousness of the given cell
	 */
	Double getCellRanking(K cell);

	/**
	 * the position of a cell is determined by the number of cells that need to
	 * be examined before this cell (at least higher rank) and the number of
	 * cells with the same suspiciousness.
	 * 
	 * This method returns this Position Pair for each faulty cell
	 * 
	 * @param faultyCells
	 * @return a map of the cells with the number of cells that need to b
	 */
	Map<K, RankPosition> getFaultyPosition(Collection<K> faultyCells);

	/**
	 * 
	 * @return the number of rankEntrys (distinct values)
	 */
	int getNumberDistinctRanks();

	/**
	 * 
	 * @return a list of all rank entries
	 */
	List<RankEntry<K>> getRankEntries();

	/**
	 * 
	 * @param listIndex
	 *            : the position in the distinct-ranks list
	 * @return the RankEntry (list of all coordinates with the same value)
	 */
	RankEntry<K> getRankEntry(int listIndex);

	/**
	 * 
	 * @param rank
	 * @return returns a suspiciousness that takes quartiles and number of ranks
	 *         into account.
	 */
	double getRelativeRank(double rank);
}
