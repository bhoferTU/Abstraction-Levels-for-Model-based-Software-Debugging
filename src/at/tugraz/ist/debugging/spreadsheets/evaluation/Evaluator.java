package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import at.tugraz.ist.debugging.spectrumbased.similaritycoefficients.Ochiai;
import at.tugraz.ist.debugging.spreadsheets.algorithms.SpectrumBasedResult;
import at.tugraz.ist.debugging.spreadsheets.algorithms.spectrum.SFL;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.ISpectrumAlgorithm;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.SpectrumConfig;
import at.tugraz.ist.debugging.spreadsheets.corpus.SpreadsheetFilter;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.util.CorpusLocation;
import at.tugraz.ist.util.IO.OutputConfigurator;
import at.tugraz.ist.util.fileManipulation.FileTools;

import com.google.common.io.Files;

public class Evaluator {
	
	public static PermanantResultStorage resultStorage;

	public static void main(String[] args) {
		/**
		 * Input: which files should be debugged. Point to corpus, also create "FileFilter"
		 * 
		 * so that algorithm really only takes a list of strings, which can come from anywhere. 
		 */
		
		/**
		 * for each file, execute algorithms (maybe several)
		 * --> write to Results to container
		 */
		
		/**
		 * write Results of all files to one file
		 */
		
		runEvaluation("integer",true);
		runEvaluation("euses",true);

		System.out.println("done.");
		
/*		int minFormulas = 6;
		int minIF = 1;
		SpreadsheetFilter filter = new SpreadsheetFilter(minFormulas, minIF);
		String PATH = CorpusLocation.PATH_EUSES;
*/
		//evaluate(PATH, filter, algorithms);
	}
	
	
	public static void runEvaluation(String corpusType, boolean cones)
	{
		List<ISpectrumAlgorithm> algorithms = new ArrayList<ISpectrumAlgorithm>();
		algorithms.add(new SFL(new Ochiai()));
		
		//create output files
		OutputConfigurator.setOutputAndErrorStreamToFile("Results.log");
		
		resultStorage = new PermanantResultStorage("results_" + corpusType 
				+ "_"+((cones)?"cones":"sfl")
				+ ".csv");
		
		evaluate("if_"+corpusType+".txt",algorithms, cones);
	}

	public static void evaluate(String filePath, List<ISpectrumAlgorithm> algos, boolean justCones)
	{
		File list = new File(filePath);
		
		List<SpectrumBasedResult> resultList = new ArrayList<SpectrumBasedResult>();
		
		try {
			BufferedReader reader = Files.newReader(list, Charset.defaultCharset());
			
			String line = reader.readLine();
			
			while (line != null)
			{
				if (line.isEmpty())
					break;

		//		System.out.println(line);
				
				String path = FileTools.convertPathToSystemPath("..\\..\\"+line+".properties");
				
				SpectrumBasedResult result = null;
				
				if (justCones)
					result = runConesStaticDynamic(path);
				else
					result = runAlgoStaticAndDynamic(path, algos);
				
				if (result != null)
					resultList.add(result);
					//resultStorage.storeResult(result);

				line = reader.readLine();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (SpectrumBasedResult res : resultList)
			resultStorage.storeResult(res);
	}
	
	private static SpectrumBasedResult runConesStaticDynamic(String fname)
	{
		SpreadsheetProperties config = null;

		try {
			config = new SpreadsheetProperties(fname);
			CellContainer cells = CellContainer.create(config.getExcelSheetPath()); 
			
			SpectrumConfig spectrumData = new SpectrumConfig(config, cells);
			SpectrumBasedResult res = new SpectrumBasedResult(spectrumData);
			res.setConesDynamicStatic();
			
			return res;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static void evaluate(String corpusPath, SpreadsheetFilter filter, List<ISpectrumAlgorithm> algorithms)
	{
		System.out.println("Get Filtered File List...");

		List<String> fileList = filter.getFilteredList(corpusPath+CorpusLocation.config);
		
		System.out.println("Filtered Files size: " + fileList.size());

		for (String fname : fileList)
		{
			SpectrumBasedResult result = runAlgoStaticAndDynamic(fname,algorithms);
			
			if (result != null)
				resultStorage.storeResult(result);
			else
				System.err.println(fname + " could not be debugged.");

		}
	}
	
	public static SpectrumBasedResult runAlgoStaticAndDynamic(String fname,
			List<ISpectrumAlgorithm> algorithms) {

		SpreadsheetProperties config = null;

		try {
			config = new SpreadsheetProperties(fname);
			CellContainer cells = CellContainer.create(config.getExcelSheetPath()); 
			
			SpectrumConfig spectrumData = new SpectrumConfig(config, cells);
			SpectrumBasedResult res = new SpectrumBasedResult(spectrumData);

			//List<Set<Coords>> negativeCones = spectrumData.getNegativeCones();
			
			//run with static!!
			runAlgorithms(res, spectrumData, algorithms);

			spectrumData.setDynamic(true);

			runAlgorithms(res, spectrumData, algorithms);
			
			return res;

		} catch (SpreadsheetPropertiesException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private static void runAlgorithms(SpectrumBasedResult res, SpectrumConfig spectrumData, List<ISpectrumAlgorithm> algorithms)
	{
		res.setUnionAndIntersection();

		for (ISpectrumAlgorithm algo : algorithms) {
			IRanking<Coords> ranking = algo.runAlgorithm(
					spectrumData).getRanking();
			res.setSpectrumBasedAlgo(algo.getName(), ranking);
		}
	}

}
