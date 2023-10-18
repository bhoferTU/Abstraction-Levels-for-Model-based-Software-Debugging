package at.tugraz.ist.debugging.spreadsheets.evaluation.ranking;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RankingBig<K> extends Ranking<K> {

	public RankingBig(Map<K, BigDecimal> map) {
		super();

		Map<K, Double> converted = new HashMap<K, Double>();

		for (Entry<K, BigDecimal> entry : map.entrySet()) {
			converted.put(entry.getKey(), entry.getValue().doubleValue());
		}

		init(converted);
	}

}
