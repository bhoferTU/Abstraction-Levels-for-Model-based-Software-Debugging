package at.tugraz.ist.debugging.spreadsheets.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.SimilarityCoefficient;

public class ObservationMatrix {

	Map<Coords, Integer> cells;
	Map<Coords, Integer[]> compressedInfo = null;
	boolean[] error;
	Map<Integer, Coords> integerToCellMap = null;
	boolean[][] obs;
	
	
	public Map<Coords, Integer> getCells() {
		return cells;
	}

	public boolean[] getErrorVector() {
		return error;
	}

	public boolean[][] getObservationMatrix() {
		return obs;
	}


	/**
	 * @param obs
	 * @param cells
	 * @param error
	 * @throws ObservationMatrixCreationException
	 */
	public ObservationMatrix(boolean[][] obs, Map<Coords, Integer> cells,
			boolean[] error) throws ObservationMatrixCreationException {
		if (obs.length != cells.size())
			throw new ObservationMatrixCreationException(
					"The length of the observation matrix does not fit to the cells map!");

		for (int i = 0; i < obs.length; i++) {
			if (obs[i].length != error.length)
				throw new ObservationMatrixCreationException(
						"The length of the observation matrix entries do not fit to the error vector!");
		}

		this.obs = obs;
		this.cells = cells;
		computeIntegerToCellMap();
		this.error = error;
		computeCompressedInfo();
	}

	/**
	 * @param positiveCones
	 *            List of cones from positive output cells
	 * @param negativeCones
	 *            List of cones from negative output cells
	 * @param range
	 *            Set of cells that should be considered in this observation
	 *            matrix
	 * @param similarityCoefficients
	 */
	public ObservationMatrix(List<Set<Coords>> positiveCones,
			List<Set<Coords>> negativeCones, Set<Coords> range) {
		int index = 0;
		cells = new HashMap<Coords, Integer>();
		for (Coords cell : range) {
			cells.put(cell, index++);
		}

		error = new boolean[positiveCones.size() + negativeCones.size()];
		for (int i = positiveCones.size(); i < error.length; i++) {
			error[i] = true;
		}

		obs = new boolean[cells.size()][];
		for (int i = 0; i < cells.size(); i++) {
			obs[i] = new boolean[error.length];
		}

		int counter = 0;
		for (Set<Coords> cone : positiveCones) {
			for (Coords cell : cone) {
				if (cells.containsKey(cell)) {
					obs[cells.get(cell)][counter] = true;
				}

			}
			counter++;
		}
		for (Set<Coords> cone : negativeCones) {
			for (Coords cell : cone) {
				if (cells.containsKey(cell)) {
					obs[cells.get(cell)][counter] = true;
				}
			}
			counter++;
		}

		computeCompressedInfo();
		computeIntegerToCellMap();
	}

	protected void computeCompressedInfo() { // [a11, a10, a01, a00]
		compressedInfo = new HashMap<Coords, Integer[]>();

		for (Coords cell : cells.keySet()) {
			int failedInvolved = 0;
			int passedInvolved = 0;
			int failedNotInvolved = 0;
			int passedNotInvolved = 0;

			for (int i = 0; i < error.length; i++) {
				if (obs[cells.get(cell)][i] == true && error[i] == true)
					failedInvolved++;
				else if (obs[cells.get(cell)][i] == true && error[i] == false)
					passedInvolved++;
				else if (obs[cells.get(cell)][i] == false && error[i] == true)
					failedNotInvolved++;
				else
					passedNotInvolved++;
			}
			compressedInfo.put(cell, new Integer[] { failedInvolved,
					passedInvolved, failedNotInvolved, passedNotInvolved });

		}

	}

	private void computeIntegerToCellMap() {
		if (integerToCellMap == null) {
			integerToCellMap = new HashMap<Integer, Coords>();
			for (Coords cell : cells.keySet()) {
				integerToCellMap.put(cells.get(cell), cell);
			}
		}
	}

	private String fill(String str, int fillSize) {
		StringBuilder strB = new StringBuilder();
		while ((fillSize - str.length()) / 2 > strB.length())
			strB.append(" ");
		strB.append(str);
		while (strB.length() < fillSize)
			strB.append(" ");
		return strB.toString();
	}

	/**
	 * 
	 * @param sc
	 *            The coefficient that should be used
	 * @return The ranking number for each cell (starting with 1, cells with the
	 *         same number have both the lower ranking number)
	 */
	public Map<Coords, Integer> getCoefficientRanking(SimilarityCoefficient sc) {
		Map<Coords, Double> coefficientValues = getCoefficientValues(sc);
		Collection<Double> unsorted = coefficientValues.values();
		List<Double> sorted = asSortedList(unsorted);

		Map<Coords, Integer> ranking = new HashMap<Coords, Integer>();
		
		for(Coords cell : cells.keySet()){
			ranking.put(cell, sorted.size()-sorted.lastIndexOf(coefficientValues.get(cell)));
		}
		
		return ranking;
	}
	
	public static
	<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
	/**
	 * 
	 * @param sc
	 *            The coefficient that should be used
	 * @return The computed coefficient for each cell
	 */
	public Map<Coords, Double> getCoefficientValues(SimilarityCoefficient sc) {
		Map<Coords, Double> coefficients = new HashMap<Coords, Double>();
		for (Coords cell : cells.keySet()) {
			int a11 = compressedInfo.get(cell)[0];
			int a10 = compressedInfo.get(cell)[1];
			int a01 = compressedInfo.get(cell)[2];
			int a00 = compressedInfo.get(cell)[3];

			coefficients.put(cell,
					sc.getSimilarityCoefficient(a11, a10, a01, a00));
		}
		return coefficients;
	}

	public Map<Coords, Integer> getMapping() {
		return cells;
	}

	private int getSizeOfLongestCell() {
		int length = 0;
		for (Coords cell : cells.keySet()) {
			if (cell.getShortString().length() > length)
				length = cell.getShortString().length();
		}
		return length+1;
	}

	@Override
	public String toString() {
		StringBuilder strB = new StringBuilder();

		int length = getSizeOfLongestCell();

//		strB.append("OBSERVATION MATRIX");
//		strB.append(System.lineSeparator());
		for (Integer i = 0; i < cells.size(); i++) {
			String cell = integerToCellMap.get(i).getShortString();
			strB.append(fill(cell, length));
		}
		strB.append("| ERROR");
		strB.append(System.lineSeparator());
		for (int i = 0; i < (cells.size()) * length; i++) {
			strB.append("-");
		}
		strB.append("-------");
		strB.append(System.lineSeparator());

		for (Integer j = 0; j < error.length; j++) {
			for (int i = 0; i < cells.size(); i++) {
				strB.append(fill(obs[i][j] ? "\u2022" : " ", length));
			}
			strB.append("|  ");
			strB.append(error[j] ? "\u2022" : " ");
			strB.append(System.lineSeparator());
		}
		strB.append(System.lineSeparator());

		return strB.toString();
	}
	
	
	public String toCSV() {
		StringBuilder strB = new StringBuilder();

//		strB.append("OBSERVATION MATRIX");
//		strB.append(System.lineSeparator());
		for (Integer i = 0; i < cells.size(); i++) {
			String cell = integerToCellMap.get(i).getShortString();
			strB.append(cell);
			strB.append(";");
		}
		strB.append("ERROR");
		strB.append(System.lineSeparator());
		
		for (Integer j = 0; j < error.length; j++) {
			for (int i = 0; i < cells.size(); i++) {
				strB.append(obs[i][j] ? "1" : "0");
				strB.append(";");
			}
			strB.append(error[j] ? "1" : "0");
			strB.append(System.lineSeparator());
		}
		return strB.toString();
	}
	
	@Override
	public boolean equals(Object obj){
		if(! (obj instanceof ObservationMatrix))
			return false;
		ObservationMatrix obs2 = (ObservationMatrix) obj;
		if(obs2.cells.size() != cells.size())
			return false;
		if(obs2.error.length != error.length)
			return false;
		if(obs2.obs.length != obs.length)
			return false;
		for(int i=0; i<obs.length;i++){
			if(obs2.obs[i].length != obs[i].length){
				return false;
			}
		}
		
		for(Coords coord :cells.keySet()){
			if(!obs2.cells.containsKey(coord))
				return false;
		}
		
		for(Coords coord: compressedInfo.keySet()){
			Integer[] compressed1 = compressedInfo.get(coord);
			Integer[] compressed2 = obs2.compressedInfo.get(coord);
			for(int i=0;i<compressed1.length;i++){
				if(compressed1[i]!=compressed2[i])
					return false;
			}
		}
		
		return true;
	}

}
