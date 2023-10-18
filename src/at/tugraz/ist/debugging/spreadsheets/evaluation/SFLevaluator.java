package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.SimilarityCoefficient;
import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.SimilarityCoefficient.CoefficientComparator;
import at.tugraz.ist.debugging.spreadsheets.algorithms.SpectrumBasedResult;
import at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum.SFL;
import at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum.SHSC;
import at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum.Sendys;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.SpectrumConfig;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.util.IO.OutputConfigurator;
import at.tugraz.ist.util.fileManipulation.Directory;

public class SFLevaluator {

	private static final String COEFFICIENTS_PACKAGE = "at.tugraz.ist.debugging.spectrumbased.similaritycoefficients";

	private static List<String> files = new ArrayList<String>();
	public static String PATH = "Benchmarks" + File.separator;

	public static PermanantResultStorage resultStorage;

	public static void main(String[] args) {

		OutputConfigurator.setOutputAndErrorStreamToFile("Results.log");

		SFLevaluator debug = new SFLevaluator();

		files = Directory.getFilesRecursively(PATH + "Configuration_files"
				+ File.separator, ".properties");
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd hh_mm");
		resultStorage = new PermanantResultStorage("results_" + ft.format(now)
				+ ".csv");

		List<SimilarityCoefficient> coefficients = new ArrayList<SimilarityCoefficient>();
		Reflections reflections = new Reflections(COEFFICIENTS_PACKAGE);
		for (Class<? extends SimilarityCoefficient> sc : reflections
				.getSubTypesOf(SimilarityCoefficient.class)) {
			try {
				coefficients.add(sc.newInstance());
			} catch (Exception e) {
			}
		}

		Collections.sort(coefficients, new CoefficientComparator());

		for (String file : files) {

			System.out.println(file);

			try {
				debug.debug(file, coefficients);
			} catch (Exception e) {
				System.err.println("Exception in file " + file);
				System.err.println(e.toString());
			}
		}
	}

	boolean debug = false;

	public void debug(String configFile,
			List<SimilarityCoefficient> coefficients) {

		SpreadsheetProperties config = null;

		try {
			config = new SpreadsheetProperties(configFile);
			CellContainer cells = CellContainer.create(PATH
					+ config.getExcelSheetName()); // TODO: change to getPath

			SpectrumConfig spectrumData = new SpectrumConfig(config, cells);
			spectrumData.setDebug(debug);

			SpectrumBasedResult res = new SpectrumBasedResult(spectrumData);

			List<Set<Coords>> negativeCones = spectrumData.getNegativeCones();
			if (debug) {
				System.out.println("Negative cones: ");
				spectrumData.printCones(negativeCones);
			}

			res.setUnionAndIntersection();

			for (SimilarityCoefficient sc : coefficients) {
				IRanking<Coords> ranking = new SFL(sc).runAlgorithm(
						spectrumData).getRanking();
				res.setSpectrumBasedAlgo(sc.getCoefficientName(), ranking);
			}

			res.setSpectrumBasedAlgo("SHSC",
					new SHSC().runAlgorithm(spectrumData).getRanking());
			res.setSpectrumBasedAlgo("Sendys",
					new Sendys().runAlgorithm(spectrumData).getRanking());

			resultStorage.storeResult(res);

		} catch (SpreadsheetPropertiesException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
