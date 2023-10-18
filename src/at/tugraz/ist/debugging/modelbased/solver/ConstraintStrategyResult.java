package at.tugraz.ist.debugging.modelbased.solver;

import java.util.List;
import java.util.Map;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;
import java.util.ArrayList;
import java.util.HashMap;

public class ConstraintStrategyResult {

	private Integer abnormalsAmount;

    private List<List<Cell>> highPriorityDiagnoses = null;
    private List<List<Cell>> lowPriorityDiagnoses = null;
    
	private String errorMsg;

	private TimeSpan runtime;
//    private TimeSpan runtimeSolving;
	private final Map<Integer, TimeSpan> solvingTime;
    private TimeSpan runtimeValidating;

	private Integer variablesAmount;
	
	private Integer maxDiagnosisSize;
    
    public Integer getMaxDiagnosisSize() {
		return maxDiagnosisSize;
	}

	public void setMaxDiagnosisSize(Integer maxDiagnosisSize) {
		this.maxDiagnosisSize = maxDiagnosisSize;
	}

	public ConstraintStrategyResult( 
            List<List<Cell>> highPriorityDiagnoses,
            List<List<Cell>> lowPriorityDiagnoses) {
        runtime = new TimeSpan(Precision.MILLISECONDS);
        solvingTime = new HashMap<Integer, TimeSpan>();
        runtimeValidating = new TimeSpan(Precision.MILLISECONDS);
		this.highPriorityDiagnoses = highPriorityDiagnoses;
        this.lowPriorityDiagnoses = lowPriorityDiagnoses;
	}

	public ConstraintStrategyResult(
			List<List<Cell>> highPriorityDiagnoses,
			ConstraintStrategyGenerationInformation info) {
        runtime = new TimeSpan(Precision.MILLISECONDS);
        solvingTime = new HashMap<Integer, TimeSpan>();
        runtimeValidating = new TimeSpan(Precision.MILLISECONDS);
		this.highPriorityDiagnoses = highPriorityDiagnoses;
        this.lowPriorityDiagnoses = new ArrayList<>();
		this.variablesAmount = info.getCellVariablesCount();
		this.abnormalsAmount = info.getAbnormalsCount();
	}
    
    public ConstraintStrategyResult(
			List<List<Cell>> highPriorityDiagnoses,
            List<List<Cell>> lowPriorityDiagnoses,
			ConstraintStrategyGenerationInformation info) {
        runtime = new TimeSpan(Precision.MILLISECONDS);
        solvingTime = new HashMap<Integer, TimeSpan>();
        runtimeValidating = new TimeSpan(Precision.MILLISECONDS);
		this.highPriorityDiagnoses = highPriorityDiagnoses;
        this.lowPriorityDiagnoses = lowPriorityDiagnoses;
		this.variablesAmount = info.getCellVariablesCount();
		this.abnormalsAmount = info.getAbnormalsCount();
	}

	public String getConstraintBasedInformationAsString() {
		return String.format("variables: %s abnormals: %s", variablesAmount,
				abnormalsAmount);
	}
    
    public List<List<Cell>> getHighPriorityDiagnoses() {
        return highPriorityDiagnoses;
	}
    
    public List<List<Cell>> getLowPriorityDiagnoses() {
        return lowPriorityDiagnoses;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public TimeSpan getRuntime() {
		
        runtime.add(getRuntimeSolving());
        runtime.add(runtimeValidating);
		return runtime;
	}

	public long getRuntime(Precision precision) {
        runtime.add(getRuntimeSolving());
        runtime.add(runtimeValidating);
		return runtime.getTimeSpan(precision);
	}
    
    public TimeSpan getRuntimeSolving() {
    	TimeSpan total = new TimeSpan(0.0, Precision.MICROSECONDS);
    	for(TimeSpan t : solvingTime.values()){
    		total.add(t);
    	}
		return total;
	}
    
    public Map<Integer, TimeSpan> getRuntimeSolvingTimes() {
    	return solvingTime;
	}
    
//    public TimeSpan getRuntimeSolving(Precision precision) {
//    	TimeSpan t = getRuntimeSolving();
//    	t.
//		return ge;
//	}
    
    public long getRuntimeValidating(Precision precision) {
		return runtimeValidating.getTimeSpan(precision);
	}

	public void setAbnormalsAmount(int abnormalsAmount) {
		this.abnormalsAmount = abnormalsAmount;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public void setVariablesAmount(int variablesAmount) {
		this.variablesAmount = variablesAmount;
	}
    
    public void addRuntimeSolving(TimeSpan runtimeSolving, int diagSize) {
		this.solvingTime.put(diagSize, runtimeSolving);
	}
    
    public void setRuntimeSolving(TimeSpan runtimeSolving) {
		this.solvingTime.put(-1, runtimeSolving);
	}
    
    public void addSolvingTimes(Map<Integer, TimeSpan> solvingTimes){
    	for(int i: solvingTimes.keySet()){
    		this.solvingTime.put(i,solvingTimes.get(i));
    	}
    }
    
    public void addSolvingTime(Integer i, TimeSpan solvingTime){
    	this.solvingTime.put(i,solvingTime);
    }
    
    public void setRuntimeValidating(TimeSpan runtimeValidating) {
		this.runtimeValidating = runtimeValidating;
	}
    
    public void addSolutions(List<List<Cell>> solutions){
    	this.highPriorityDiagnoses.addAll(solutions);
    }
}
