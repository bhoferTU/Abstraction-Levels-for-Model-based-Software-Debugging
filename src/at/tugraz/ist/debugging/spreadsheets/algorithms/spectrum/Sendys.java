package at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import HittingSet.SubSetContainer;
import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.Ochiai;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.ObservationMatrix;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.Ranking;

/**
 * Implements Hofer's and Wotawa's Spectrum-Enhanced Dynamic Slicing approach
 * 
 * @author bhofer
 * 
 */
public class Sendys extends SHSC {

	public static Map<Coords, Double> getFaultPropabilites(
			List<Set<Coords>> negativeCones, ObservationMatrix obs) {
		
		Map<Coords, Double> initalFaultProbabilities = new HashMap<Coords, Double>();
		Double sum = 0.0;
		Map<Coords, Double> coefficients = obs
				.getCoefficientValues(new Ochiai());
		for (Double coeff : coefficients.values()) {
			sum += coeff;
		}
		for (Coords cell : coefficients.keySet()) {
			Double value = coefficients.get(cell);
			initalFaultProbabilities.put(cell, value);
		}
		SubSetContainer diagnoses = computeHittingSets(getConesAsSet(negativeCones));
		Map<Coords, Double> faultProbabilites = computeFaultPropabiltities(
				initalFaultProbabilities, diagnoses);
		return faultProbabilites;
	}
	
	@Override
	protected IRanking<Coords> runAlgorithm() {
		return new Ranking<Coords>(getFaultPropabilites(
				data.getNegativeCones(), data.getObservationMatrix()));
	}

}
