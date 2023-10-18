package at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import HittingSet.SubSet;
import HittingSet.SubSetContainer;
import at.tugraz.ist.debugging.spreadsheets.algorithms.SpectrumBasedAlgorithm;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.ObservationMatrix;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.RankingBig;
import at.tugraz.ist.debugging.spreadsheets.util.BigMath;

/**
 * Implements Wotawa's Slicing-Hitting-Set-Computation approach
 * 
 * @author bhofer
 * 
 */

public class SHSCBigDecimal extends SpectrumBasedAlgorithm {

	protected static Map<Coords, BigDecimal> computeFaultPropabiltities(
			Map<Coords, BigDecimal> initialFaultProbability,
			SubSetContainer diagnoses) {
		BigDecimal sum = new BigDecimal(0);
		for (BigDecimal value : initialFaultProbability.values()) {
			sum = sum.add(value);
		}
		for (Coords cell : initialFaultProbability.keySet()) {
			BigDecimal old = initialFaultProbability.get(cell);
			initialFaultProbability.put(cell, old.divide(sum, BigMath.MC));
		}

		HashMap<SubSet, BigDecimal> faultProbDiagnoses = new HashMap<SubSet, BigDecimal>();
		for (SubSet subSet : diagnoses) {
			BigDecimal prob = new BigDecimal(1.0);
			for (Coords cell : initialFaultProbability.keySet()) {
				String line = cell.getCSVString();
				if (subSet.contains(line)) {
					prob = prob.multiply(initialFaultProbability.get(cell));
				} else {
					prob = prob.multiply(BigMath.ONE
							.subtract(initialFaultProbability.get(cell)));
				}
			}
			faultProbDiagnoses.put(subSet, prob);
		}
		HashMap<Coords, BigDecimal> newFaultProbability = new HashMap<Coords, BigDecimal>();
		BigDecimal total = new BigDecimal(0);

		for (Coords cell : initialFaultProbability.keySet()) {
			String line = cell.getCSVString();
			BigDecimal prob = new BigDecimal(0.0);
			for (SubSet diag : faultProbDiagnoses.keySet()) {
				if (diag.contains(line)) {
					prob = prob.add(faultProbDiagnoses.get(diag));
				}
			}
			newFaultProbability.put(cell, prob);
			total = total.add(prob);
		}
		for (Coords cell : newFaultProbability.keySet()) {
			BigDecimal old = newFaultProbability.get(cell);
			newFaultProbability.put(cell, old.divide(total, BigMath.MC));
		}
		return newFaultProbability;
	}

	public static Map<Coords, BigDecimal> getFaultPropabilites(
			List<Set<Coords>> negativeCones, ObservationMatrix obs) {
		Map<Coords, Integer> cells = obs.getMapping();
		Map<Coords, BigDecimal> initalFaultProbabilities = new HashMap<Coords, BigDecimal>();
		BigDecimal value = BigMath.ONE.divide(new BigDecimal(cells.size()),
				BigMath.MC);
		for (Coords cell : cells.keySet()) {
			initalFaultProbabilities.put(cell, value);
		}
		SubSetContainer diagnoses = SHSC.computeHittingSets(SHSC
				.getConesAsSet(negativeCones));
		Map<Coords, BigDecimal> faultProbabilites = computeFaultPropabiltities(
				initalFaultProbabilities, diagnoses);
		return faultProbabilites;
	}

	/**
	 * 
	 * @param negativeCones
	 * @param obs
	 * @return The ranking number for each cell (starting with 1, cells with the
	 *         same number have both the lower ranking number)
	 */
	public static Map<Coords, Integer> getRanking(
			List<Set<Coords>> negativeCones, ObservationMatrix obs) {
		Map<Coords, BigDecimal> faultPropabilities = getFaultPropabilites(
				negativeCones, obs);
		return getRanking(faultPropabilities);
	}

	public static Map<Coords, Integer> getRanking(
			Map<Coords, BigDecimal> faultPropabilities) {
		Map<Coords, Integer> ranking = new HashMap<Coords, Integer>();
		for (Coords cell : faultPropabilities.keySet()) {
			int rank = 1;
			BigDecimal coeff = faultPropabilities.get(cell);
			for (BigDecimal coefficient : faultPropabilities.values()) {
				if (coefficient.compareTo(coeff) == 1) {
					rank++;
				}
			}
			ranking.put(cell, rank);
		}
		return ranking;
	}

	@Override
	protected IRanking<Coords> runAlgorithm() {

		Map<Coords, BigDecimal> probs = getFaultPropabilites(
				data.getNegativeCones(), data.getObservationMatrix());

		return new RankingBig<Coords>(probs);
	}
}
