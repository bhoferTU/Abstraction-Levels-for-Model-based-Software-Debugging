package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.Ochiai;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.ObservationMatrix;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICell;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICellContainer;
import at.tugraz.ist.util.IO.OutputConfigurator;
import at.tugraz.ist.util.datastructures.Pair;

public class SflTestCaseReductionEvaluator {

	public static String csvFileName = "";

	private static Random rand = new Random(System.currentTimeMillis());

	public static Integer runs = 100;

	public static BufferedWriter writer = null;

	public static Boolean SMART_SELECT = false;

	public static enum SELECTION_TECHNIQUE {
		COINCIDENTAL, CONESIZE, INFORMATIONGAIN, DUPLICATEFAULTY
	};

	public static SELECTION_TECHNIQUE selectedTechnique = SELECTION_TECHNIQUE.DUPLICATEFAULTY;

	public static void debug(String file, Boolean detailedResults) {
		SpreadsheetProperties properties;
		try {
			properties = new SpreadsheetProperties(file);

			ICellContainer cells = CellContainer.create(properties.getExcelSheetName());

			List<Coords> outputCoords = properties.getCorrectOutputCells();
			// outputCoords.addAll(properties.getIncorrectOutputCells());
			computeCoincidentialCorrectness(outputCoords, cells);

			int numberOfCorrectOutputCells = properties.getCorrectOutputCells().size();
			int numberOfIncorrrectOutputCells = properties.getIncorrectOutputCells().size();
			for (int correct = 0; correct <= numberOfCorrectOutputCells; correct++) {
				// for (int incorrect = 1; incorrect <=
				// numberOfIncorrrectOutputCells; incorrect++) {
				for (int incorrect = 1; incorrect <= numberOfIncorrrectOutputCells; incorrect++) {
					List<Integer> ranking = new ArrayList<Integer>();
					List<Integer> same = new ArrayList<Integer>();
					List<Double> average = new ArrayList<Double>();
					// int ranking = 0;
					// int same = 0;
					for (int run = 0; run < runs; run++) {
						Pair<Integer, Integer> singleResult = SflTestCaseReductionEvaluator.getResult(cells,
								properties, correct, incorrect);
						ranking.add(singleResult.getFirst());
						same.add(singleResult.getSecond());
						Double avg = ((double) singleResult.getFirst()) + ((double) singleResult.getSecond()) / 2;
						average.add(avg);
						// ranking += singleResult.getFirst();
						// same += singleResult.getSecond();

						System.out.println("corr: " + correct + ", incorr: " + incorrect + ", ranking: "
								+ singleResult.getFirst() + ", same: " + singleResult.getSecond() + ", avg: " + avg);
					}

					TestCaseReductionResult result;
					if (detailedResults) {
						result = new TestCaseReductionDetailedResult(file, correct, incorrect, average);
					} else {
						result = new TestCaseReductionSummaryResult(file, correct, incorrect, ranking, same);
					}

					try {
						writer.write(result.toString());
						writer.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (SpreadsheetPropertiesException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getCsvFileName() {
		return csvFileName;
	}

	/**
	 * 
	 * @param ranking
	 * @param faultyCells
	 * @return Pair.first() = BestCaseRanking, Pair.second = critical tie size
	 */
	public static Pair<Integer, Integer> getRankingResult(Map<Coords, Integer> ranking, List<Coords> faultyCells) {
		int minRanking = ranking.size();
		int criticalTieSize = 0;
		for (Coords cell : faultyCells) {
			if (ranking.get(cell) < minRanking) {
				minRanking = ranking.get(cell);
			}
		}
		for (Integer rank : ranking.values()) {
			if (rank == minRanking)
				criticalTieSize++;
		}
		return new Pair<Integer, Integer>(minRanking + 1, criticalTieSize);
	}

	public static Pair<Integer, Integer> getResult(ICellContainer cells, SpreadsheetProperties config,
			int numberOfCorrectOutputCells, int numberOfInorrectOutputCells) {

		List<Coords> positives = null;
		if (SMART_SELECT && selectedTechnique!=SELECTION_TECHNIQUE.DUPLICATEFAULTY)
			positives = SflTestCaseReductionEvaluator.smartSelect(config.getCorrectOutputCells(),
					numberOfCorrectOutputCells);
		else
			positives = SflTestCaseReductionEvaluator.randomSelect(config.getCorrectOutputCells(),
					numberOfCorrectOutputCells);
		System.out.print("Corr: ");
		for (Coords cell : positives)
			System.out.print(cell.getCSVString() + ";");

		System.out.print(" ");

		List<Coords> negatives = SflTestCaseReductionEvaluator.randomSelect(config.getIncorrectOutputCells(),
				numberOfInorrectOutputCells);
		
		if(SMART_SELECT && selectedTechnique==SELECTION_TECHNIQUE.DUPLICATEFAULTY){
			negatives = duplicateCones(negatives, numberOfCorrectOutputCells);
		}
		Map<Coords, Integer> ranking = SflTestCaseReductionEvaluator.sfl(cells, positives, negatives);
		return getRankingResult(ranking, config.getFaultyCells());
	}
	
	public static List<Coords> duplicateCones(List<Coords> cones, int number){
		List<Coords> newCone = new ArrayList<Coords>();
		for(Coords coords:cones){
			newCone.add(coords);
		}
		while(newCone.size()<number){
			Integer index = rand.nextInt(cones.size());
			newCone.add(cones.get(index));
		}
		return newCone;
	}

	public static Integer getRuns() {
		return runs;
	}

	public static List<String> getSpreadsheetsWithManyOutputCells() {
		List<String> files = new ArrayList<String>();

		// files.add("Benchmarks\\ISCAS_Bool\\Configuration_files\\c432_BOOL_tc1_1_1Fault.properties");//
		// a very short test example;

//		files.add("Benchmarks\\ISCAS_Bool\\Configuration_files\\c5315_BOOL_tc1_71_1Fault.properties");//
		// ToDo: add again

		 files.add("Benchmarks\\ISCAS_Bool\\Configuration_files\\c7552_BOOL_tc1_96_1Fault.properties");
		// //ToDo: add again

		// files.add("Benchmarks\\Configuration_files\\financial\\my_financial_model_1FAULTS_FAULTVERSION2.properties");
		 files.add("Benchmarks\\Configuration_files\\financial\\my_financial_model_1FAULTS_FAULTVERSION5.properties");
		// // ToDo: add again
		// files.add("Benchmarks\\Configuration_files\\homework\\G140S04_1FAULTS_FAULTVERSION2.properties");
		// files.add("Benchmarks\\Configuration_files\\homework\\G140S04_1FAULTS_FAULTVERSION3.properties");
		// files.add("Benchmarks\\Configuration_files\\homework\\G140S04_1FAULTS_FAULTVERSION5.properties");

		// files.add("Benchmarks\\Configuration_files\\homework\\WorldPopPlay_1FAULTS_FAULTVERSION1.properties");
		// // ToDo:
		// add
		// again

		// files.add("Benchmarks\\Configuration_files\\homework\\WorldPopPlay_1FAULTS_FAULTVERSION2.properties");
		// files.add("Benchmarks\\Configuration_files\\homework\\WorldPopPlay_1FAULTS_FAULTVERSION3.properties");
		// files.add("Benchmarks\\Configuration_files\\homework\\WorldPopPlay_1FAULTS_FAULTVERSION4.properties");
		// files.add("Benchmarks\\Configuration_files\\homework\\WorldPopPlay_1FAULTS_FAULTVERSION5.properties");

		return files;
	}

	public static void main(String[] args) {

		// OutputConfigurator.setOutputAndErrorStreamToFile("ObservationMatrices.csv");
		//
		List<String> files = SflTestCaseReductionEvaluator.getSpreadsheetsWithManyOutputCells();
		//
		// for(String file:files){
		// System.out.println(file);
		// printObsvervationMatrix(file);
		// System.out.println("\n\n\n");
		// }

		SMART_SELECT = true;

		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd hh_mm");
		SflTestCaseReductionEvaluator.setCsvFileName("results_tcReduction_SMART_" + ft.format(now) + ".csv");
		SflTestCaseReductionEvaluator.setRuns(100);

		File file = new File(csvFileName);
		OutputConfigurator.setOutputAndErrorStreamToFile(csvFileName.replace(".csv", ".log"));

		boolean fileExists = false;
		if (file.exists()) {
			fileExists = true;
		}

		boolean detailedResults = true;

		try {
			writer = new BufferedWriter(new FileWriter(csvFileName, true));
			if (!fileExists) {
				if (detailedResults) {
					writer.write(TestCaseReductionDetailedResult.getHeader());
				} else {
					writer.write(TestCaseReductionSummaryResult.getHeader());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String file2 : files) {
			System.out.println(file2);
			coincidentialCorrectness = null;
			SflTestCaseReductionEvaluator.debug(file2, detailedResults);
		}

	}

	public static void printObsvervationMatrix(String propertiesFile) {
		try {
			SpreadsheetProperties properties = new SpreadsheetProperties(propertiesFile);

			ICellContainer cells = CellContainer.create(properties.getExcelSheetName());

			List<Set<Coords>> positiveCones = new ArrayList<Set<Coords>>();
			for (Coords cell : properties.getCorrectOutputCells()) {
				Set<Coords> cone = cells.getICell(cell).getCone(false, true);
				cone.add(cell);
				positiveCones.add(cone);
			}

			List<Set<Coords>> negativeCones = new ArrayList<Set<Coords>>();
			for (Coords cell : properties.getIncorrectOutputCells()) {
				Set<Coords> cone = cells.getICell(cell).getCone(false, true);
				cone.add(cell);
				negativeCones.add(cone);
			}

			ObservationMatrix obs = new ObservationMatrix(positiveCones, negativeCones, cells.getFormulaCoords());
			System.out.println(obs.toCSV());

		} catch (Exception e) {
			System.err.println(e.toString());
		}

	}

	/**
	 * For performance reasons, we split the selection process (either select or
	 * delete items)
	 * 
	 * @param original
	 * @param numberOfItems
	 * @return
	 */
	public static List<Coords> randomSelect(List<Coords> original, int numberOfItems) {
		List<Coords> selectedOnes = new ArrayList<Coords>();

		if (numberOfItems * 2 < original.size()) {
			while (selectedOnes.size() < numberOfItems) {
				Integer index = rand.nextInt(original.size());
				Coords coordinates = original.get(index);
				if (!selectedOnes.contains(coordinates))
					selectedOnes.add(coordinates);
				// original.remove(coordinates);
			}
		} else {
			for (Coords cell : original) {
				selectedOnes.add(cell);
			}
			while (selectedOnes.size() > numberOfItems) {
				Integer index = rand.nextInt(original.size());
				Coords coordinates = original.get(index);
				if (selectedOnes.contains(coordinates)) {
					selectedOnes.remove(coordinates);
				}

			}
		}
		return selectedOnes;
	}

	public static List<Coords> smartSelect(List<Coords> original, int numberOfItems) {
		List<Coords> selectedOnes = new ArrayList<Coords>();
		while (selectedOnes.size() < numberOfItems) {

			if (selectedTechnique == SELECTION_TECHNIQUE.COINCIDENTAL) {
				for (List<Coords> coordsList : sortedCoincidentialCorrectness) {
					if (coordsList.size() <= (numberOfItems - selectedOnes.size())) {
						selectedOnes.addAll(coordsList);
					} else {
						List<Coords> coordsList2 = new ArrayList<Coords>();
						for (Coords coord : coordsList)
							coordsList2.add(coord);
						while (selectedOnes.size() < numberOfItems) {
							Integer index = rand.nextInt(coordsList2.size());
							Coords coord = coordsList2.remove(index.intValue());
							selectedOnes.add(coord);
						}
					}
				}
			} else if (selectedTechnique == SELECTION_TECHNIQUE.CONESIZE) {
				for (List<Coords> coordsList : sortedConeSize) {
					if (coordsList.size() <= (numberOfItems - selectedOnes.size())) {
						selectedOnes.addAll(coordsList);
					} else {
						List<Coords> coordsList2 = new ArrayList<Coords>();
						for (Coords coord : coordsList)
							coordsList2.add(coord);
						while (selectedOnes.size() < numberOfItems) {
							Integer index = rand.nextInt(coordsList2.size());
							Coords coord = coordsList2.remove(index.intValue());
							selectedOnes.add(coord);
						}
					}
				}
			} else if (selectedTechnique == SELECTION_TECHNIQUE.INFORMATIONGAIN) {
				for(Coords coords: informationGain){
					selectedOnes.add(coords);
					if(selectedOnes.size()==numberOfItems)
						break;
				}
			}
		}

		return selectedOnes;
	}

	static Map<Coords, Integer> coincidentialCorrectness = null;
	static List<List<Coords>> sortedCoincidentialCorrectness = null;
	// static List<List<List<Coords>>> verySortedCoincidentialCorrectness =
	// null;
	static List<List<Coords>> sortedConeSize = null;
	static List<Coords> informationGain = null;

	private static void computeCoincidentialCorrectness(List<Coords> original, ICellContainer cells) {
		coincidentialCorrectness = new HashMap<Coords, Integer>();
		sortedCoincidentialCorrectness = new ArrayList<List<Coords>>();
		// verySortedCoincidentialCorrectness = new
		// ArrayList<List<List<Coords>>>();
		sortedConeSize = new ArrayList<List<Coords>>();

		for (Coords coord : original) {
			ICell cell = cells.getICell(coord);
			if (cell.isFormulaCell()) {
				Cell c = (Cell) cell;
				coincidentialCorrectness.put(coord, c.getRecursiveNumberOfPossibleCoincidentialCorrectness(cells));
			}
		}

		Collection<Integer> unsorted = coincidentialCorrectness.values();
		Set<Integer> unsortedSet = new HashSet<>(unsorted);
		List<Integer> sorted = asSortedList(unsortedSet);

		for (Integer value : sorted) {
			List<Coords> coordsList = new ArrayList<Coords>();
			Map<Coords, Integer> coneSize = new HashMap<Coords, Integer>();
			for (Coords coords : coincidentialCorrectness.keySet()) {
				if (coincidentialCorrectness.get(coords) == value) {
					coordsList.add(coords);
					Cell c = (Cell) cells.getICell(coords);
					coneSize.put(coords, c.getCone(false, false).size());
				}
			}
			sortedCoincidentialCorrectness.add(coordsList);

			// Set<Integer> unsortedConeSizes = new
			// HashSet<Integer>(coneSize.values());
			// List<Integer> sortedConeSizes = asSortedList(unsortedConeSizes);
			// List<List<Coords>> conLists = new ArrayList<List<Coords>>();
			// for(Integer coneSiz: sortedConeSizes){
			// List<Coords> conList = new ArrayList<Coords>();
			// for(){
			//
			// }
			// conLists.add(conList);
			// }
			// verySortedCoincidentialCorrectness.add(conLists);
		}

		Map<Coords, Integer> coneSizeMap = new HashMap<Coords, Integer>();
		for (Coords coord : original) {
			Cell cell = (Cell) cells.getICell(coord);
			coneSizeMap.put(coord, cell.getCone(false, false).size());
		}
		Set<Integer> unsortedConeSizes = new HashSet<Integer>(coneSizeMap.values());
		List<Integer> sortedConeSizes = asSortedList(unsortedConeSizes);
	
		for (Integer coneSize : sortedConeSizes) {
			List<Coords> coords = new ArrayList<Coords>();
			for (Coords coord : original) {
				if (coneSizeMap.get(coord).intValue() == coneSize.intValue()){
					coords.add(coord);
					
				}
			}
			sortedConeSize.add(0, coords);
		}
		
		List<Coords> rest = new ArrayList<Coords>();
		for(Coords coord: original)
			rest.add(coord);
		
		informationGain = new ArrayList<Coords>();
		
		Set<Coords> collectedInformation = new HashSet<Coords>();
		
		while(rest.size()>0){
			Coords coord = getCoordsWithLargestInformationGain(collectedInformation, rest, cells);
			rest.remove(coord);
			Cell cell = (Cell) cells.getICell(coord);
			collectedInformation.addAll(cell.getCone(false, false));
			informationGain.add(coord);
		}

		System.out.println("************* Coindidental Correctness *****************");
		for (Coords coord : coincidentialCorrectness.keySet()) {
			System.out.println(coord.toString() + " " + coincidentialCorrectness.get(coord));
		}
		System.out.println("************* Coindidental Correctness Group *****************");
		for (List<Coords> cordList : sortedCoincidentialCorrectness) {
			for (Coords coord : cordList)
				System.out.print(coord.toString() + ", ");
			System.out.println();
		}
		System.out.println("************* Cone Size Group *****************");
		for (List<Coords> cordList : sortedConeSize) {
			for (Coords coord : cordList)
				System.out.print(coord.toString() + ", ");
			System.out.println();
		}
		System.out.println("************* Information Gaine *****************");
		for (Coords coord : informationGain){
			System.out.print(coord.toString() + ", ");
		}
	}
	
	public static Coords getCoordsWithLargestInformationGain(Set<Coords> collectedInformation, List<Coords> newInfo, ICellContainer cells){
		if(newInfo.size()==0)
			return null;
		int infoGain = -1;
		Coords largestInfoCoord = null;
		for(Coords coords:newInfo){
			Cell cell = (Cell) cells.getICell(coords);
			Set<Coords> cone = cell.getCone(false, false);
			int currentGain = getInformationGain(collectedInformation, cone);
			if(currentGain > infoGain){
				infoGain = currentGain;
				largestInfoCoord = coords;
			}
		}
		System.out.println("Adding coord "+largestInfoCoord.toString()+", infoGain: "+infoGain);
		
		return largestInfoCoord;
	}
	
	public static int getInformationGain(Set<Coords> collectedInformation, Set<Coords> newInformation){
		int gain = 0;
		for(Coords coords: newInformation){
			if(!collectedInformation.contains(coords))
				gain++;
		}
		return gain;
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	public static void setCsvFileName(String cvsFileName) {
		SflTestCaseReductionEvaluator.csvFileName = cvsFileName;
	}

	public static void setRuns(Integer runs) {
		SflTestCaseReductionEvaluator.runs = runs;
	}

	public static Map<Coords, Integer> sfl(ICellContainer cells, List<Coords> correctOutputCells,
			List<Coords> incorrectOutputCells) {

		List<Set<Coords>> positiveCones = new ArrayList<Set<Coords>>();
		for (Coords cell : correctOutputCells) {
			Set<Coords> cone = cells.getICell(cell).getCone(false, true);
			cone.add(cell);
			positiveCones.add(cone);
		}

		List<Set<Coords>> negativeCones = new ArrayList<Set<Coords>>();
		for (Coords cell : incorrectOutputCells) {
			Set<Coords> cone = cells.getICell(cell).getCone(false, true);
			cone.add(cell);
			negativeCones.add(cone);
		}

		ObservationMatrix obs = new ObservationMatrix(positiveCones, negativeCones, cells.getFormulaCoords());

		// System.out.println(obs.toString());

		Map<Coords, Integer> coefficientRanking = obs.getCoefficientRanking(new Ochiai());

		return coefficientRanking;
	}

}
