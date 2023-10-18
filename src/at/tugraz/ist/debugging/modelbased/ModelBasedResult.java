package at.tugraz.ist.debugging.modelbased;

import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.evaluation.AlgorithmResult;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.Ranking;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;

public class ModelBasedResult extends AlgorithmResult {

	private class ResultComparator implements Comparator<Coords> {
		Map<Coords, Double> results;

		public ResultComparator(Map<Coords, Double> results) {
			this.results = results;
		}

		@Override
		public int compare(Coords lhs, Coords rhs) {
			return results.get(lhs) < results.get(rhs) ? -1 : 1;
		}
	}

	private final Map<Integer, List<Diagnosis>> highPriorityDiagnoses;
	private final Map<Integer, TimeSpan> solvingTime;
	// private final LinkedList<Diagnosis> highPriorityDiagnoses;
	private final LinkedList<Diagnosis> lowPriorityDiagnoses;
	private Boolean diagnosisContained = false;
	private Integer domainSize = 0;
	private String fileName;
	private final Integer formulaCellCount;
	private String message;
	private final Integer numberConstraints;
	private final Strategy strategy;
	private TimeSpan time = new TimeSpan(0.0, Precision.MICROSECONDS);
	// private TimeSpan runtimeSolving = new TimeSpan(0.0,
	// Precision.MICROSECONDS);
	private long runtimeValidating;
	private final boolean usingCone;
	protected Set<Cell> cones;

	public ModelBasedResult(String fileName, Strategy strategy, boolean usingCone, int formulaCellCount,
			int numberConstraints) {
		highPriorityDiagnoses = new HashMap<Integer, List<Diagnosis>>();
		lowPriorityDiagnoses = new LinkedList<>();
		solvingTime = new HashMap<Integer, TimeSpan>();
		this.usingCone = usingCone;
		this.strategy = strategy;
		this.fileName = fileName;

		String counter = fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf("."));
		if (counter.length() == 1)
			this.fileName = this.fileName.replace("_" + counter, "_00" + counter);
		else if (counter.length() == 2)
			this.fileName = this.fileName.replace("_" + counter, "_0" + counter);

		this.formulaCellCount = formulaCellCount;
		this.message = null;
		this.numberConstraints = numberConstraints;

		setUp();
	}

	private Integer maxDiagnosisSize;

	public Integer getMaxDiagnosisSize() {
		return maxDiagnosisSize;
	}

	public void setMaxDiagnosisSize(Integer maxDiagnosisSize) {
		this.maxDiagnosisSize = maxDiagnosisSize;
	}

	public void addDiagnoses(List<List<Cell>> highPriorityDiagnoses, List<List<Cell>> lowPriorityDiagnoses) {
		for (List<Cell> diagnosisList : highPriorityDiagnoses) {
			Diagnosis diagnosis = new Diagnosis();
			for (Cell errorCell : diagnosisList) {
				diagnosis.addFaultCell(errorCell);
			}
			addHighPriorityDiagnosis(diagnosis);
		}

		for (List<Cell> diagnosisList : lowPriorityDiagnoses) {
			Diagnosis diagnosis = new Diagnosis();
			for (Cell errorCell : diagnosisList) {
				diagnosis.addFaultCell(errorCell);
			}
			addLowPriorityDiagnosis(diagnosis);
		}

		// List<Diagnosis> diagnoses = new
		// ArrayList<>(this.highPriorityDiagnoses);
		// diagnoses.addAll(this.lowPriorityDiagnoses);
		// Collections.sort(diagnoses);
		// Set<Coords> cells = new HashSet<>();
		// for (Diagnosis diagnosis : diagnoses)
		// cells.addAll(diagnosis);

		// this.addData("Total number of cells in diagnoses",
		// Integer.toString(cells.size()));
		// this.addData("Number of diagnoses",
		// Integer.toString(diagnoses.size()));
	}

	public void addHighPriorityDiagnosis(Diagnosis diag) {
		List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
		if (highPriorityDiagnoses.containsKey(diag.size()))
			diagnoses = highPriorityDiagnoses.get(diag.size());
		diagnoses.add(diag);

		highPriorityDiagnoses.put(diag.size(), diagnoses);

		Collections.sort(diagnoses);
		Set<Coords> cells = new HashSet<>();
		for (Diagnosis diagnosis : diagnoses)
			cells.addAll(diagnosis);

		// StringBuilder str = new StringBuilder();
		// str.append("{");
		// for (Diagnosis diagnosis : diagnoses)
		// str.append(diagnosis);
		// str.append("}");
		//
		// this.addData("Total number of cells in high priority diagnoses of
		// size "+diagnoses.get(0).size(),
		// Integer.toString(cells.size()));
		// this.addData("Number of high priority diagnoses of size
		// "+diagnoses.get(0).size()",
		// Integer.toString(highPriorityDiagnoses.size()));
		// this.addData("High priority diagnoses", str.toString());
	}

	public void addLowPriorityDiagnosis(Diagnosis diag) {
		lowPriorityDiagnoses.add(diag);

		Collections.sort(lowPriorityDiagnoses);
		Set<Coords> cells = new HashSet<>();
		for (Diagnosis diagnosis : lowPriorityDiagnoses)
			cells.addAll(diagnosis);

		StringBuilder str = new StringBuilder();
		str.append("{");
		for (Diagnosis diagnosis : lowPriorityDiagnoses)
			str.append(diagnosis);
		str.append("}");

		this.addData("Total number of cells in low priority diagnoses", Integer.toString(cells.size()));
		this.addData("Number of low priority diagnoses", Integer.toString(lowPriorityDiagnoses.size()));
		this.addData("Low priority diagnoses", str.toString());
	}

	public boolean containsDiagnosis(Diagnosis diag) {
		for (List<Diagnosis> diagnoses : highPriorityDiagnoses.values()) {
			for (Diagnosis diagnosis : diagnoses)
				if (diagnosis.equals(diag))
					return true;
		}

		for (Diagnosis diagnosis : lowPriorityDiagnoses)
			if (diagnosis.equals(diag))
				return true;

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ModelBasedResult))
			return false;

		ModelBasedResult res = (ModelBasedResult) obj;

		List<Diagnosis> highPriorityDiagnosesCopy = new ArrayList<>();
		for (List<Diagnosis> diag : res.highPriorityDiagnoses.values())
			highPriorityDiagnosesCopy.addAll(diag);

		List<Diagnosis> highPriorityDiagnosesThis = new ArrayList<>();
		for (List<Diagnosis> diag : highPriorityDiagnoses.values())
			highPriorityDiagnosesThis.addAll(diag);

		for (Diagnosis diagnosis : highPriorityDiagnosesThis) {
			if (!highPriorityDiagnosesCopy.contains(diagnosis))
				return false;
			highPriorityDiagnosesCopy.remove(diagnosis);
		}
		if (highPriorityDiagnosesCopy.size() > 0)
			return false;

		List<Diagnosis> lowPriorityDiagnosesCopy = new ArrayList<>(res.lowPriorityDiagnoses);
		for (Diagnosis diagnosis : lowPriorityDiagnoses) {
			if (!lowPriorityDiagnosesCopy.contains(diagnosis))
				return false;
			lowPriorityDiagnosesCopy.remove(diagnosis);
		}
		if (lowPriorityDiagnosesCopy.size() > 0)
			return false;

		// compare additional information
		if (!message.equals(res.message))
			return false;

		return true;
	}

	public List<Diagnosis> getAllDiagnoses() {
		List<Diagnosis> diagnoses = new ArrayList<>();
		for (List<Diagnosis> diag : highPriorityDiagnoses.values())
			diagnoses.addAll(diag);
		diagnoses.addAll(lowPriorityDiagnoses);
		return diagnoses;
	}

	public List<Diagnosis> getHighPriorityDiagnoses() {
		List<Diagnosis> diagnoses = new ArrayList<>();
		for (List<Diagnosis> diag : highPriorityDiagnoses.values())
			diagnoses.addAll(diag);
		return diagnoses;
	}

	public List<Diagnosis> getLowPriorityDiagnoses() {
		return lowPriorityDiagnoses;
	}

	public String getDiagnosisAsString() {
		String s = "HPD: ";
		List<Diagnosis> diagnoses = new ArrayList<>();
		for (List<Diagnosis> diag : highPriorityDiagnoses.values())
			diagnoses.addAll(diag);
		for (Diagnosis diagnosis : diagnoses)
			s += diagnosis;

		if (lowPriorityDiagnoses != null && lowPriorityDiagnoses.size() > 0) {
			s += "LPD: ";
			for (Diagnosis diagnosis : lowPriorityDiagnoses)
				s += diagnosis;
		}
		return s;
	}

	@Override
	public IRanking<Coords> getRanking() {
		return new Ranking<>(getResults()); // TODO: change to coordinates
	}

	public Map<Coords, Double> getResults() {
		Map<Coords, Double> cellMap = new HashMap<>();
		Map<Diagnosis, Double> diagMap = new HashMap<>();
		// LinkedList<Diagnosis> diagnoses = new LinkedList<>()
		List<Diagnosis> diagnoses = new ArrayList<>();
		for (List<Diagnosis> diag : highPriorityDiagnoses.values())
			diagnoses.addAll(diag);
		;
		diagnoses.addAll(lowPriorityDiagnoses);

		for (Diagnosis diagnosis : diagnoses)
			for (Coords cell : diagnosis) {
				if (cell == null)
					continue;
				cellMap.put(cell, 0.0);
			}
		double factor = 1.0 / cellMap.size();
		for (Diagnosis diagnosis : diagnoses) {
			double size = diagnosis.size();
			double prop = Math.pow(factor, size) * Math.pow(1.0 - factor, cellMap.size() - size);
			diagMap.put(diagnosis, prop);
		}
		double max = 0.0;
		for (Entry<Diagnosis, Double> entry : diagMap.entrySet()) {
			for (Coords cell : entry.getKey()) {
				if (cell == null)
					continue;
				double prop = entry.getValue() + cellMap.get(cell);
				cellMap.put(cell, prop);
				if (prop > max)
					max = prop;
			}
		}
		for (Entry<Coords, Double> entry : cellMap.entrySet())
			cellMap.put(entry.getKey(), entry.getValue() / max);
		Map<Coords, Double> sortedCellMap = new TreeMap<>(new ResultComparator(cellMap));
		sortedCellMap.putAll(cellMap);
		return sortedCellMap;
	}

	public TimeSpan getRuntime() {
		return time;
	}

	public TimeSpan getRuntimeSolving() {
		TimeSpan total = new TimeSpan(0.0, Precision.MICROSECONDS);
		for (TimeSpan t : solvingTime.values()) {
			total.add(t);
		}

		return total;
	}

	public Map<Integer, TimeSpan> getRuntimeSolvingDiagGranularity() {

		return solvingTime;
	}

	public long getRuntimeValidating() {
		return runtimeValidating;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public Boolean isDiagnosisContained() {
		return diagnosisContained;
	}

	public boolean removeInputCells(Set<Cell> inputCells) {
		for (int i : highPriorityDiagnoses.keySet()) {
			List<Diagnosis> diag = highPriorityDiagnoses.get(i);
			Iterator<Diagnosis> iterator = diag.iterator();
			while (iterator.hasNext()) {
				Diagnosis diagnosis = iterator.next();
				if (diagnosis.removeInputCells(inputCells, cones) == false)
					iterator.remove();
			}
			highPriorityDiagnoses.put(i, diag);

		}

		Iterator<Diagnosis> iterator = lowPriorityDiagnoses.iterator();
		while (iterator.hasNext()) {
			Diagnosis diagnosis = iterator.next();
			if (diagnosis.removeInputCells(inputCells, cones) == false)
				iterator.remove();
		}
		return highPriorityDiagnoses.size() != 0;
	}

	public void setCones(Set<Cell> cones) {
		this.cones = cones;
	}

	public void setDiagnosisContained(Boolean diagnosisContained) {
		this.diagnosisContained = diagnosisContained;
		this.addData("Diagnosis contained", diagnosisContained ? "yes" : "no");
	}

	public void setDomainSize(Integer domainSize) {
		this.domainSize = domainSize;
		this.addData("DomainSize", domainSize.toString());
	}

	public void setMessage(String message) {
		this.message = message;

		if (message != null) {
			String variables = message.substring("variables: ".length(), message.indexOf("abn"));
			String abnormal = message.substring(message.indexOf("ls: ") + 3);
			this.addData("Message - variables", variables);
			this.addData("Message - abnormal", abnormal);
		}
	}

	/**
	 * 
	 * @param time
	 *            in milliseconds (ms!)
	 */
	public void setRuntime(long time) {
		this.time = new TimeSpan(time, Precision.MILLISECONDS);
		this.addData("Runtime (in ms)", Long.toString(time));
	}

	public void setRuntime(TimeSpan time) {
		this.time = time;
		this.addData("Runtime (in microsec)", time.toString(Precision.MICROSECONDS));
	}

	public void setRuntimeSolving(TimeSpan runtimeSolving, int diagSize) {
		solvingTime.put(diagSize, runtimeSolving);
//		this.runtimeSolving = runtimeSolving;
		this.addData("Runtime Solving (in microseconds)", runtimeSolving.toString(Precision.MICROSECONDS));
	}
	
	public void addSolvingTimes(Map<Integer, TimeSpan> solvingTimes){
    	for(int i: solvingTimes.keySet()){
    		solvingTime.put(i,solvingTimes.get(i));
    	}
    }

	public void setRuntimeValidating(long runtimeValidating) {
		this.runtimeValidating = runtimeValidating;
		this.addData("Runtime Validating (in ms)", Long.toString(runtimeValidating));
	}

	private void setUp() {
		this.addData("File Name", fileName);
		this.addData("Strategy", strategy.getName());
		this.addData("Using cone", usingCone ? "yes" : "no");
		this.addData("Runtime (in ms)", time.toString(Precision.MILLISECONDS));
		
		this.addData("Runtime Solving (in mircoseconds)", getRuntimeSolving().toString(Precision.MICROSECONDS));
		for(int i: solvingTime.keySet()){
			this.addData("Runtime Solving (in mircoseconds) Diagnosis size="+i, solvingTime.get(i).toString(Precision.MICROSECONDS));
		}
		
		
		this.addData("Runtime Validating (in ms)", Long.toString(runtimeValidating));
		this.addData("DomainSize", domainSize.toString());
		this.addData("Formula cell count", formulaCellCount.toString());
		this.addData("Diagnosis contained", diagnosisContained ? "yes" : "no");

		this.addData("Total number of cells in diagnoses", Integer.toString(0));
		this.addData("Number of diagnoses", Integer.toString(0));

		this.addData("High priority diagnoses", "");
		this.addData("Low priority diagnoses", "");

		this.addData("Total number of cells in high priority diagnoses", Integer.toString(0));
		this.addData("Number of high priority diagnoses", Integer.toString(0));

		this.addData("Total number of cells in low priority diagnoses", Integer.toString(0));
		this.addData("Number of low priority diagnoses", Integer.toString(0));

		this.addData("Number of constraints", numberConstraints.toString());
		this.addData("Faulty Cells", ConstraintStrategyConfiguration.getFaultyCells().toString());
		String variables = "-";
		String abnormal = "-";

		if (message != null) {
			variables = message.substring("variables: ".length(), message.indexOf("abn"));
			abnormal = message.substring(message.indexOf("ls: ") + 3);
		}

		this.addData("Message - variables", variables);
		this.addData("Message - abnormal", abnormal);
	}
}
