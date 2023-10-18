package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tugraz.ist.util.IO.OutputConfigurator;

public class SflTcRecductionAnalysis {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String logFileName = "results_tcReduction_c5315.log";
//		String logFileName = "results_tcReduction_c7553.log";
//		String logFileName = "results_tcReduction_my_financial_model.log";
//		String logFileName = "results_tcReduction_WorldPopPlay.log";
		Map<Integer, Map<String, Integer>> resultsWorse = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, Map<String, Integer>> resultsSame = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, Map<String, Integer>> resultsBetter = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, Double> avgRanking = new HashMap<Integer, Double>();
		List<String> lines = new ArrayList<String>();
		
		OutputConfigurator.setOutputAndErrorStreamToFile(logFileName.replace("results", "SUMMARY").replace(".log", ".csv"));
		
		Set<String> cells = new HashSet<>();
		
		Integer maxNumberCorrectCells=0;
		

		try {
			BufferedReader br = new BufferedReader(new FileReader(logFileName));
			
			String line = "";
			Double avgResult = 0.0;
			Integer count = 0;
			Integer numberCorrectCells=0;
			
			while((line = br.readLine()) != null) {
				if (!line.contains("avg: "))
					continue;
				lines.add(line);
				String subLine = line.substring(line.indexOf(" corr: ") + " corr: ".length());
				subLine = subLine.substring(0, subLine.indexOf(","));
				Integer currentNumberCorrectCells = Integer.parseInt(subLine);
				if(currentNumberCorrectCells!=numberCorrectCells){
					avgResult = avgResult/count;
					avgRanking.put(numberCorrectCells, avgResult);
					
					avgResult = 0.0;
					count = 0;
					numberCorrectCells=currentNumberCorrectCells;
				}
				
				Double avg = Double.valueOf(line.substring(line.indexOf("avg: ") + "avg: ".length()));
				avgResult += avg;
				count++;				
			}
			avgResult = avgResult/count;
			avgRanking.put(numberCorrectCells, avgResult);
			
			
			for(String line2: lines) {
				line = line2;
				

								
				String subLine = line.substring(line.indexOf(" corr: ") + " corr: ".length());
				subLine = subLine.substring(0, subLine.indexOf(","));
				numberCorrectCells = Integer.parseInt(subLine);
				if(numberCorrectCells==0)
					continue;
				if(numberCorrectCells>maxNumberCorrectCells)
					maxNumberCorrectCells = numberCorrectCells;
				
				subLine = line.substring("Corr: ".length(), line.lastIndexOf(";"));
				String[] outputCells = subLine.split(";");
				
				for(String outputCell : outputCells){
					cells.add(outputCell);
				}
				Double avg = Double.valueOf(line.substring(line.indexOf("avg: ") + "avg: ".length()));
				
				if(avg > avgRanking.get(numberCorrectCells)){
					addResult(resultsWorse, numberCorrectCells, outputCells);
				}else if(avg < avgRanking.get(numberCorrectCells)){
					addResult(resultsBetter, numberCorrectCells, outputCells);
				}else{
					addResult(resultsSame, numberCorrectCells, outputCells);
				}
				
			}
			br.close();
			
			System.out.println("******* WORSE  *******");
			printResult(resultsWorse, cells, maxNumberCorrectCells);
			System.out.println("*******  SAME  *******");
			printResult(resultsSame, cells, maxNumberCorrectCells);
			System.out.println("******* BETTER *******");
			printResult(resultsBetter, cells, maxNumberCorrectCells);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Corr: 0!F!3280;corr: 1, incorr: 5, ranking: 2, same: 211, avg: 107.5
		// Corr: 0!F!3241;0!F!170!F!28780!F!31180!F!3171corr: 5, incorr: 5,
		// ranking: 9, same: 184, avg: 101.0
	}
	
	public static void addResult(Map<Integer, Map<String, Integer>> results, Integer numberCorrectCells, String[] outputCells){
		Map<String, Integer> result = null;
		if (results.containsKey(numberCorrectCells)) {
			result = results.get(numberCorrectCells);
		} else {
			result = new HashMap<String, Integer>();
		}
		for (String outputCell : outputCells) {
			if (result.containsKey(outputCell)) {
				result.put(outputCell, result.get(outputCell) + 1);
			}else{
				result.put(outputCell, 1);
			}
		}
		results.put(numberCorrectCells, result);
	}
	
	
	public static void printResult(Map<Integer, Map<String, Integer>> results){
		for(Integer key:results.keySet()){
			System.out.println("Results for "+key+" correct cells:");
			for(String cell: results.get(key).keySet()){
				System.out.print(cell+": "+results.get(key).get(cell)+"; ");
			}
			System.out.print("\n");
		}
	}
	
	
	public static void printResult(Map<Integer, Map<String, Integer>> results, Set<String> cells, Integer maxNumberCorrectCells){
		System.out.print("Results for x correct cells;");
		for(int i=1; i<=maxNumberCorrectCells; i++){
			System.out.print(i);
            System.out.print(";");
		}
		System.out.print("\n");
		
		for(String cell : cells){
			System.out.print(cell);
			System.out.print(";");
			for(int i=1; i<=maxNumberCorrectCells; i++){
				System.out.print(results.containsKey(i)?results.get(i).containsKey(cell)?results.get(i).get(cell):0:0);
				System.out.print(";");
			}
			System.out.print("\n");
		}
		
	}
}
