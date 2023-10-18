package at.tugraz.ist.debugging.spreadsheets.evaluation.ranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import at.tugraz.ist.util.StatUtils;

public class Ranking<K> implements IRanking<K> {

	private Map<K, Double> cellMap;
	protected double max;
	protected double[] quart;

	private List<RankEntry<K>> rankList;
	private Map<Double, RankEntry<K>> rankMap;

	protected Ranking() {

	}

	public Ranking(Map<K, Double> map) {
		init(map);
	}

	/**
	 * compare two Rankings, regardless of their values their RankEntries must
	 * have the same positions with the same cells contained
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IRanking<?>))
			return false;
		
		IRanking<?> otherRanking = (IRanking<?>) o;
		
		int numRanks = otherRanking.getNumberDistinctRanks();
		
		if (getNumberDistinctRanks() != numRanks)
			return false;

		for (int index = 0; index < numRanks; index++) {
			RankEntry<K> ourEntry = getRankEntry(index);
			RankEntry<?> theirEntry = otherRanking.getRankEntry(index);

			if (!ourEntry.equals(theirEntry))
				return false;
			
		}

		return true;
	}

	@Override
	public Double getCellRanking(K cell) {
		return cellMap.get(cell);
	}

	@Override
	public Map<K, RankPosition> getFaultyPosition(Collection<K> faultyCells) {
		Map<K, RankPosition> map = new HashMap<K, RankPosition>();

		for (K cell : faultyCells)
			map.put(cell, getPosition(cell));

		return map;
	}

	@Override
	public int getNumberDistinctRanks() {
		return rankList.size();
	}

	private RankPosition getPosition(K cell) {
		int before = 0;

		for (RankEntry<K> entry : rankList) {
			if (entry.contains(cell))
				return new RankPosition(before, entry.size());
			else
				before += entry.size();
		}
		// cell was not found in rankList
		return new RankPosition(-1, 0);
	}

	@Override
	public List<RankEntry<K>> getRankEntries() {
		return rankList;
	}

	@Override
	public RankEntry<K> getRankEntry(int listIndex) {
		return rankList.get(listIndex);
	}

	@Override
	public double getRelativeRank(double rank) {
		double relative = rank / max;
		if (quart != null) {
			if (quart.length > 2)
				if (rank < quart[quart.length - 3])
					return relative * 0.5;
			if (quart.length > 1)
				if (rank < quart[quart.length - 2])
					return relative * 0.7;
			if (quart.length > 0)
				if (rank < quart[quart.length - 1])
					return relative * 0.8;
		}

		return relative;
	}

	protected void init(Map<K, Double> map) {
		this.cellMap = map;

		rankMap = new TreeMap<Double, RankEntry<K>>(Collections.reverseOrder());

		for (Entry<K, Double> entry : map.entrySet()) {
			if (rankMap.containsKey(entry.getValue())) {
				RankEntry<K> rank = rankMap.get(entry.getValue());
				rank.add(entry.getKey());
			} else
				rankMap.put(entry.getValue(), new RankEntry<K>(
						entry.getValue(), entry.getKey()));
		}

		rankList = new ArrayList<RankEntry<K>>();
		rankList.addAll(rankMap.values());

		if (!rankList.isEmpty())
			max = rankList.get(0).getValue();

		List<Double> values = new ArrayList<Double>(rankMap.keySet());
		quart = StatUtils.getQuartile(values);
	}

	@Override
	public String toString() {
		
		if (rankList.isEmpty())
			return "Empty Ranking";
		
		String string = "";
		for (RankEntry<K> rank : rankList)
			string += rank.toString() + "\n";
		return string;
	}
	
}
