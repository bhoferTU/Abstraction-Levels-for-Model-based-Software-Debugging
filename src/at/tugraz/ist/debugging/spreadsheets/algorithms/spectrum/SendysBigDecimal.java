package at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import HittingSet.SubSetContainer;
import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.Ochiai;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.ObservationMatrix;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.RankingBig;

/**
 * Implements Hofer's and Wotawa's Spectrum-Enhanced Dynamic Slicing approach
 * 
 * @author bhofer
 * 
 */
public class SendysBigDecimal extends SHSCBigDecimal {

	public static Map<Coords, BigDecimal> getFaultPropabilites(
			List<Set<Coords>> negativeCones, ObservationMatrix obs) {
		Map<Coords, BigDecimal> initalFaultProbabilities = new HashMap<Coords, BigDecimal>();
		BigDecimal sum = new BigDecimal("0");
		Map<Coords, Double> coefficients = obs
				.getCoefficientValues(new Ochiai());
		for (Double coeff : coefficients.values()) {

			BigDecimal bdcoeff = new BigDecimal(coeff);
			sum = sum.add(bdcoeff);
		}
		for (Coords cell : coefficients.keySet()) {
			Double value = coefficients.get(cell);

			BigDecimal bdvalue = new BigDecimal(value);
			initalFaultProbabilities.put(cell, bdvalue);
		}
		SubSetContainer diagnoses = SHSC.computeHittingSets(SHSC
				.getConesAsSet(negativeCones));
		Map<Coords, BigDecimal> faultProbabilites = computeFaultPropabiltities(
				initalFaultProbabilities, diagnoses);
		return faultProbabilites;
	}

	@Override
	protected IRanking<Coords> runAlgorithm() {

		Map<Coords, BigDecimal> probs = getFaultPropabilites(
				data.getNegativeCones(), data.getObservationMatrix());

		return new RankingBig<Coords>(probs);
	}

}
