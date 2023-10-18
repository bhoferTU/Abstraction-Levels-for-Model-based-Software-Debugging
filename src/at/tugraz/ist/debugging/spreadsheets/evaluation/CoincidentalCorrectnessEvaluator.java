package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.Ochiai;
import at.tugraz.ist.debugging.spreadsheets.algorithms.SpectrumBasedResult;
import at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum.SFL;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.ISpectrumAlgorithm;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.SpectrumConfig;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICell;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.RankPosition;
import at.tugraz.ist.util.IO.OutputConfigurator;
import at.tugraz.ist.util.fileManipulation.Directory;

/**
 * This class serves to compute the evaluation data for the QRS 2016 paper.
 * 
 * @author bhofer
 *
 */
public class CoincidentalCorrectnessEvaluator {
	
	public enum Corpus {ISCAS, EUSES};
	
    public static Corpus corpusType;
	

	public static int RUNS = 100;

	public static void main(String[] args) {
//		 List<String> corpus = Directory.getFiles("Benchmarks\\ISCAS85\\configuration_files", "properties");
		corpusType = Corpus.ISCAS;
		 List<String> corpus = Directory.getFilesRecursively("Benchmarks\\EUSES_Spreadsheets\\configuration_files", "properties");
		 corpus.addAll(Directory.getFilesRecursively("Benchmarks\\EUSES_DoubleFaults\\configuration_files", "properties"));
		 corpus.addAll(Directory.getFilesRecursively("Benchmarks\\EUSES_TripleFaults\\configuration_files", "properties"));
		 corpusType = Corpus.EUSES;
//		List<String> corpus = new ArrayList<String>();
//		corpus.add("Benchmarks\\ISCAS85\\configuration_files\\c432_BOOL_tc1_1_1Fault.properties");
		CoincidentalCorrectnessEvaluator.computeStatusQuo(corpus);

	}

	private static void computePotentialCCoutputRanking(String file, CellContainer cells, SpectrumBasedResult result) throws SpreadsheetPropertiesException {
		int potentialCCoutput = 0;
		// ToDo
		
			SpreadsheetProperties properties = new SpreadsheetProperties(file);
			List<Coords> correctOutputCells = properties.getCorrectOutputCells();
			List<Coords> nonPotentialCoincidentalCorrectOuputCells = new ArrayList<Coords>();
			for (Coords coord : correctOutputCells) {
//				System.out.println(coord.toString());
				
				ICell cell = cells.getICell(coord);
				Set<Coords> cone = cell.getCone(false, false);
				boolean masked = false;
				for (Coords coords : cone) {
//					System.out.println("  "+coord.toString());
					if (!cells.getCell(coords).getExpression().isEquivalencePossible()) {
						masked = true;
						break;
					}
				}
				if (!masked){
					nonPotentialCoincidentalCorrectOuputCells.add(coord);
				}else{
					potentialCCoutput++;
				}
			}
			properties.setCorrectOutputCells(nonPotentialCoincidentalCorrectOuputCells);

			SpectrumConfig spectrumData = new SpectrumConfig(properties, cells);
			ISpectrumAlgorithm algorithm = new SFL(new Ochiai());
			IRanking<Coords> ranking = algorithm.runAlgorithm(spectrumData).getRanking();
			result.setSpectrumBasedAlgo("SFL w/o CCp", ranking);

			result.addData("PotentialCCOutput", potentialCCoutput);
	}

	/**
	 * Initial Ranking
	 */
	private static void computeInitialRanking(String file, CellContainer cells, SpectrumBasedResult result) {
		try {
			SpreadsheetProperties properties = new SpreadsheetProperties(file);
			SpectrumConfig spectrumData = new SpectrumConfig(properties, cells);

			ISpectrumAlgorithm algorithm = new SFL(new Ochiai());
			IRanking<Coords> ranking = algorithm.runAlgorithm(spectrumData).getRanking();
			result.setSpectrumBasedAlgo("SFL initial", ranking);

		} catch (SpreadsheetPropertiesException e) {
			System.err.println(file + ": " + e.toString());
		}
	}

	/**
	 * Ranking ignoring all coincidental correct output values
	 */
	private static void computeRankingWithoutCoincidentalCorrect(String file, CellContainer cells,
			SpectrumBasedResult result) {
		try {
			SpreadsheetProperties properties = new SpreadsheetProperties(file);
			List<Coords> correctOutputCells = properties.getCorrectOutputCells();
			List<Coords> nonCoincidentalCorrectOuputCells = new ArrayList<Coords>();
			for (Coords coord : correctOutputCells) {
				ICell cell = cells.getICell(coord);
				Set<Coords> cone = cell.getCone(false, false);
				boolean contained = false;
				for (Coords faultyCell : properties.getFaultyCells()) {
					if (cone.contains(faultyCell)) {
						contained = true;
						break;
					}
				}
				if (!contained)
					nonCoincidentalCorrectOuputCells.add(coord);
			}
			properties.setCorrectOutputCells(nonCoincidentalCorrectOuputCells);

			SpectrumConfig spectrumData = new SpectrumConfig(properties, cells);
			ISpectrumAlgorithm algorithm = new SFL(new Ochiai());
			IRanking<Coords> ranking = algorithm.runAlgorithm(spectrumData).getRanking();
			result.setSpectrumBasedAlgo("SFL w/o CC", ranking);

		} catch (SpreadsheetPropertiesException e) {
			System.err.println(file + ": " + e.toString());
		}
	}

	/**
	 * Ranking when randomly removing correct output values
	 */
	public static void computeRankingRandom(String file, CellContainer cells, SpectrumBasedResult result,
			int coincidentalCorrect) {

		int numberOfRuns = RUNS;
		if (coincidentalCorrect == 0)
			numberOfRuns = 1;
		Random rand = new Random(System.currentTimeMillis());

		int rank = 0;
		int same = 0;

		for (int i = 0; i < numberOfRuns; i++) {
			try {
				SpreadsheetProperties properties = new SpreadsheetProperties(file);
				List<Coords> correctOutputCells = properties.getCorrectOutputCells();
				if (correctOutputCells.size() == coincidentalCorrect)
					numberOfRuns = 1;

				int changed = 0;
				while (changed < coincidentalCorrect) {
					int randInt = rand.nextInt(correctOutputCells.size());
					correctOutputCells.remove(randInt);
					changed++;
				}
				properties.setCorrectOutputCells(correctOutputCells);

				SpectrumConfig spectrumData = new SpectrumConfig(properties, cells);
				ISpectrumAlgorithm algorithm = new SFL(new Ochiai());
				IRanking<Coords> ranking = algorithm.runAlgorithm(spectrumData).getRanking();
				RankPosition min = result.getRankPosition(ranking);
				rank += min.getNumItemsBefore();
				same += min.getNumItemsSame() - 1;

			} catch (SpreadsheetPropertiesException e) {
				System.err.println(file + ": " + e.toString());
			}
		}
		Double avg_rank = (double) rank / (double) numberOfRuns;
		Double avg_same = (double) same / (double) numberOfRuns;
		result.addData("SFL Random Ranking", avg_rank.toString().replace('.', ','));
		result.addData("SFL Random Same", avg_same.toString().replace('.', ','));
	}

	/**
	 * The complete status quo includes: - File name - Circuit - Domain - TC -
	 * Number of faults - Number of formulas - Number of correct output values -
	 * Number of coincidental correct output values --> How many benchmarks
	 * suffer from coincidental correctness? - Number of erroneous output values
	 * - SFL Initial Ranking - SFL Initial Same - SFL w/o CC Ranking - SFL w/o
	 * CC Same - SFL Random Ranking (average) - SFL Random Same (average)
	 * 
	 * @param corpus
	 */
	public static void computeStatusQuo(List<String> corpus) {
		OutputConfigurator.setOutputAndErrorStreamToFile("CoincidentalCorrectness_StatusQuo.log");
		Date date = new Date(System.currentTimeMillis());
		System.out.println("Start on " + date.toString());

		PermanantResultStorage storage = new PermanantResultStorage("CoincidentalCorrectness_StatusQuo.csv");

		for (String file : corpus) {
//			if(!file.contains("UT_Modeling_Tools_1FAULTS_FAULTVERSION3"))
//				continue;
			try {
				SpreadsheetProperties properties = new SpreadsheetProperties(file);
				CellContainer cells = CellContainer.create(properties.getExcelSheetPath());
				SpectrumConfig spectrumData = new SpectrumConfig(properties, cells);
				SpectrumBasedResult result = new SpectrumBasedResult(spectrumData);
				if(corpusType==Corpus.ISCAS)
					addIscasFileInfo(result, file);
				else if(corpusType==Corpus.EUSES)
					addEusesFileInfo(result, file);
				else
					System.err.println("Corpus type not suppored!");

				int coincidentalCorrect = 0;
				for (Set<Coords> cone : spectrumData.getPositiveCones()) {
					for (Coords faultyCell : properties.getFaultyCells()) {
						if (cone.contains(faultyCell)) {
							coincidentalCorrect++;
							break;
						}
					}
				}
				result.addData("Formulas", cells.getFormulaCoords().size());
				result.addData("Correct total", properties.getCorrectOutputCells().size());
				result.addData("Coincidental correct", coincidentalCorrect);
				result.addData("Erroneous total", properties.getIncorrectOutputCells().size());

				computeInitialRanking(file, cells, result);
				computeRankingWithoutCoincidentalCorrect(file, cells, result);
				computeRankingRandom(file, cells, result, coincidentalCorrect);
				computePotentialCCoutputRanking(file, cells, result);
				computeRankingRandom(file, cells, result, coincidentalCorrect);

				storage.storeResult(result);

			} catch (Exception e) {
				System.err.println(file + ": " + e.toString());
				e.printStackTrace();
			}
		}
		date = new Date(System.currentTimeMillis());
		System.out.println("Finish on " + date.toString());
	}

	private static void addIscasFileInfo(Result result, String file) {
		// c1355_BOOL_tc1_31_1Fault
		file = file.substring(file.lastIndexOf("\\") + 1).replace(".properties", "");
		result.addData("File", file);
		String circuit = file.substring(0, file.indexOf("_"));
		result.addData("Circuit", circuit);
		file = file.substring(file.indexOf("_") + 1);
		String domain = file.substring(0, file.indexOf("_"));
		result.addData("Domain", domain);
		file = file.substring(file.indexOf("_") + 1);
		String tc = file.substring(0, file.indexOf("_"));
		result.addData("TC", tc);
		file = file.substring(file.indexOf("_") + 1);
		String fault = file.substring(file.lastIndexOf("_") + 1, file.lastIndexOf("F"));
		result.addData("Faults", fault);
	}
	
	private static void addEusesFileInfo(Result result, String file) {
		// c1355_BOOL_tc1_31_1Fault
		file = file.substring(file.lastIndexOf("configuration_files\\") + "configuration_files".length()+1).replace(".properties", "");
		result.addData("File", file);
//		String circuit = file.substring(0, file.indexOf("."));
//		result.addData("Circuit", circuit);
//		file = file.substring(file.indexOf("_") + 1);
//		String domain = file.substring(0, file.indexOf("_"));
//		result.addData("Domain", "INT");
//		file = file.substring(file.indexOf("_") + 1);
//		String tc = file.substring(0, file.indexOf("_"));
//		result.addData("TC", "-");
		String fault = file.substring(0,file.indexOf("FAULTS"));
		fault = fault.substring(fault.lastIndexOf('_')+1);
//		String fault = file.substring(file.lastIndexOf("_") + 1, file.lastIndexOf("F"));
		result.addData("Faults", fault);
	}
}
