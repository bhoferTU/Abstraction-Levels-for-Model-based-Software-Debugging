package at.tugraz.ist.debugging.spreadsheets.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.SpectrumConfig;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.evaluation.AlgorithmResult;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.RankPosition;
import at.tugraz.ist.util.datastructures.Pair;

public class SpectrumBasedResult extends AlgorithmResult {

	private SpectrumConfig data;
	
	public SpectrumConfig getData() {
		return data;
	}


	private IRanking<Coords> ranking;

	public SpectrumBasedResult(SpectrumConfig spectrumData) {
		setInfo(spectrumData);
	}
	
	

	@Override
	public IRanking<Coords> getRanking() {
		return ranking;
	}

	private void setInfo(SpectrumConfig spectrumData) {
		this.data = spectrumData;

//		SpreadsheetProperties config = data.getProperties();

//		int numFormulas = data.getCells().getFormulaCoords().size();
//
//		List<Coords> positives = config.getCorrectOutputCells();
//		List<Coords> negatives = config.getIncorrectOutputCells();
//
//		String fileName = config.getExcelSheetName();
//		if (fileName.contains("sampleSpreadsheets\\TO_USE\\")) {
//			fileName = fileName.substring("sampleSpreadsheets\\TO_USE\\"
//					.length());
//
//		}
//		fileName = fileName.replace("SEEDED", "");
//		fileName = fileName.replace("\\\\", ": ");
//		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
//		
//		fileName = fileName.replace("..\\spreadsheets:", "");

//		String faultTypes = "";
//		for (String faultType : config.getFaultMapping().values()) {
//			faultTypes += faultType + " ";
//		}

//		this.addData("Fault file", fileName);
//		this.addData("Number of faults", new Integer(config.getFaultMapping()
//				.size()).toString());
//		this.addData("Fault type", faultTypes);
//		this.addData("Number of formulas", new Integer(numFormulas).toString());
//		this.addData("Number of correct output cells",
//				new Integer(positives.size()).toString());
//		this.addData("Number of faulty output cells",
//				new Integer(negatives.size()).toString());
	}

	public void setSpectrumBasedAlgo(String algoName, IRanking<Coords> result) {
		if (ranking == null)
			this.ranking = result;

		RankPosition min = null;
		for (RankPosition pos : result.getFaultyPosition(
				data.getProperties().getFaultyCells()).values()) {
			if (min == null)
				min = pos;

			if (min.getNumItemsBefore() > pos.getNumItemsBefore() || min.getNumItemsBefore()==-1)
				min = pos;
		}

//		String add = " (" + ((data.isDynamic())?"dyn":"s") + ")"; 
		
		this.addData(algoName + " Ranking", min.getNumItemsBefore().toString());
		this.addData(algoName + " Same", String.valueOf(min.getNumItemsSame()-1));
	}
	
	public void setRanking(IRanking<Coords> result){
		if (ranking == null)
			this.ranking = result;
	}
	
	public RankPosition getRankPosition(IRanking<Coords> result) {
		RankPosition min = null;
		for (RankPosition pos : result.getFaultyPosition(
				data.getProperties().getFaultyCells()).values()) {
			if (min == null)
				min = pos;

			if (min.getNumItemsBefore() > pos.getNumItemsBefore() || min.getNumItemsBefore()==-1)
				min = pos;
		}
		return min;
	}

	public void setUnionAndIntersection() {
		List<Set<Coords>> negativeCones = data.getNegativeCones();

		String add = " (" + ((data.isDynamic())?"dyn":"s") + ")"; 
		
		Set<Coords> union = data.getUnionSize(negativeCones);
		this.addData("Union size"+add, new Integer(union.size()).toString());

		Pair<Integer, Boolean> intersection = data.getIntersection(
				negativeCones, data.getProperties().getFaultyCells());

		this.addData("Intersection size"+add, intersection.getFirst().toString());
		this.addData("Intersection fault contained"+add, intersection.getSecond()
				.toString());

	}
	
	public void setConesDynamicStatic() {
		
		data.setDynamic(false);
		List<Integer> sizesStatic = setConeSizes();

		data.setDynamic(true);
		List<Integer> sizesDynamic = setConeSizes();
		
		for (int count = 0; count < sizesStatic.size(); count++)
		{
			this.addData("C_"+count+"_s", String.valueOf(sizesStatic.get(count)));
			this.addData("C_"+count+"_d", String.valueOf(sizesDynamic.get(count)));

		}
	
	}
	
	public List<Integer> setConeSizes()
	{
		List<Set<Coords>> cones = data.getNegativeCones();
		
		String add = " (" + ((data.isDynamic())?"d":"s") + ")"; 
		
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		
		int sum = 0;
		
		for (Set<Coords> cone : cones)
		{
			sum += cone.size();
			sizes.add(cone.size());
		}
		
		this.addData("Cone size sum"+add, String.valueOf(sum));
		this.addData("Cone size avg"+add, String.valueOf(sum/cones.size()));
		
		/*for (int count = 0; count < sizes.size(); count++)
		{
			this.addData("C_"+count+add, String.valueOf(sizes.get(count)));
		}*/
	
		return sizes;
	}
	
	
	public boolean isFaultyContained()
	{
		if (ranking == null)
			return false;
		
		Map<Coords, RankPosition> positions = ranking.getFaultyPosition(data.getProperties().getFaultyCells());
	
		for (RankPosition pos : positions.values())
		{
			if (pos.getNumItemsBefore() < 0)
				return false;
		}
		
		return true;
	}

}
