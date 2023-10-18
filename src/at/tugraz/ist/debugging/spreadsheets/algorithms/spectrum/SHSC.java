package at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum;

import hsDag.MinHittingSets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import HittingSet.SubSet;
import HittingSet.SubSetContainer;
import at.tugraz.ist.debugging.spreadsheets.algorithms.SpectrumBasedAlgorithm;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.ObservationMatrix;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.Ranking;

/**
 * Implements Wotawa's Slicing-Hitting-Set-Computation approach
 * 
 * @author bhofer
 * 
 */
public class SHSC extends SpectrumBasedAlgorithm {

	protected static Map<Coords, Double> computeFaultPropabiltities(
			Map<Coords, Double> initialFaultProbability,
			SubSetContainer diagnoses) {
		Double sum = 0.0;
		for (Double value : initialFaultProbability.values()) {
			sum += value;
		}
		for (Coords cell : initialFaultProbability.keySet()) {
			Double old = initialFaultProbability.get(cell);
			initialFaultProbability.put(cell, old / sum);
		}

		HashMap<SubSet, Double> faultProbDiagnoses = new HashMap<SubSet, Double>();
		for (SubSet subSet : diagnoses) {
			Double prob = new Double(1.0);
			for (Coords cell : initialFaultProbability.keySet()) {
				String line = cell.getCSVString();
				if (subSet.contains(line)) {
					prob *= initialFaultProbability.get(cell);
				} else {
					prob *= (1 - initialFaultProbability.get(cell));
				}
			}
			faultProbDiagnoses.put(subSet, prob);
		}
		HashMap<Coords, Double> newFaultProbability = new HashMap<Coords, Double>();
		Double total = 0.0;

		for (Coords cell : initialFaultProbability.keySet()) {
			String line = cell.getCSVString();
			Double prob = new Double(0.0);
			for (SubSet diag : faultProbDiagnoses.keySet()) {
				if (diag.contains(line)) {
					prob += faultProbDiagnoses.get(diag);
				}
			}
			newFaultProbability.put(cell, prob);
			total += prob;
		}
		for (Coords cell : newFaultProbability.keySet()) {
			Double old = newFaultProbability.get(cell);
			newFaultProbability.put(cell, old / total);
		}
		return newFaultProbability;
	}

	public static SubSetContainer computeHittingSets(Set<Set<String>> conflicts) {
		SubSetContainer conflictContainer = new SubSetContainer();
		LinkedList<Object> components = new LinkedList<Object>();
		for (Set<String> conflict : conflicts) {
			SubSet subset = new SubSet();
			subset.addAll(conflict);
			conflictContainer.add(subset);
			for (String line : conflict) {
				if (!components.contains(line)) {
					components.add(line);
				}
			}
		}
		MinHittingSets hsAlgorithm = new MinHittingSets(true,
				conflictContainer, components);
		hsAlgorithm.compute(3, -1, -1);
		SubSetContainer result = hsAlgorithm.returnDiagnoses();
		return result;
	}

	public static Set<Set<String>> getConesAsSet(List<Set<Coords>> coneList) {
		Set<Set<String>> cones = new HashSet<Set<String>>();
		for (Set<Coords> cone : coneList) {
			Set<String> hashSet = new HashSet<String>();
			for (Coords cell : cone) {
				hashSet.add(cell.getCSVString());
			}
			cones.add(hashSet);
		}
		return cones;
	}

	public static Map<Coords, Double> getFaultPropabilites(
			List<Set<Coords>> negativeCones, ObservationMatrix obs) {
		Map<Coords, Integer> cells = obs.getMapping();
		Map<Coords, Double> initalFaultProbabilities = new HashMap<Coords, Double>();
		Double value = 1.0 / cells.size();
		for (Coords cell : cells.keySet()) {
			initalFaultProbabilities.put(cell, value);
		}
		SubSetContainer diagnoses = computeHittingSets(getConesAsSet(negativeCones));
		Map<Coords, Double> faultProbabilites = computeFaultPropabiltities(
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
		Map<Coords, Double> faultPropabilities = getFaultPropabilites(
				negativeCones, obs);
		return getRanking(faultPropabilities);
	}

	public static Map<Coords, Integer> getRanking(
			Map<Coords, Double> faultPropabilities) {
		Map<Coords, Integer> ranking = new HashMap<Coords, Integer>();
		for (Coords cell : faultPropabilities.keySet()) {
			int rank = 1;
			Double coeff = faultPropabilities.get(cell);
			for (Double coefficient : faultPropabilities.values()) {
				if (coefficient > coeff) {
					rank++;
				}
			}
			ranking.put(cell, rank);
		}
		return ranking;
	}

	@Override
	protected IRanking<Coords> runAlgorithm() {
		return new Ranking<Coords>(getFaultPropabilites(
				data.getNegativeCones(), data.getObservationMatrix()));
	}

}
