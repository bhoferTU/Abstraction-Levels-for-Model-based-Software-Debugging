package at.tugraz.ist.debugging.modelbased.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Diagnosis;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.evaluation.Result;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;

// Iulia 2022: Work on CORRECT and SPURIOUS DIAGNOSES. Note that in the end the new columns were not included in the csv Files.  
public class ComparisionModelResult extends Result {


	private Map<Integer, Boolean> getTimeoutMap(Map<Integer, TimeSpan> times, boolean earlyTermination,
			int maxDiagSize) {
		Map<Integer, Boolean> timeoutMap = new HashMap<Integer, Boolean>();
		for (int i = 1; i <= maxDiagSize; i++) {
			if (times.containsKey(i) || (earlyTermination && i != 1))
				timeoutMap.put(i, false);
			else
				timeoutMap.put(i, true);
		}
		return timeoutMap;
	}

	public ComparisionModelResult(String propertiesFile, List<Diagnosis> valueBasedDiagnoses,
			List<Diagnosis> comparisonBasedDiagnoses, List<Diagnosis> sophisticatedDependencyDiagnoses, List<Diagnosis> trueFault,
			Map<Integer, TimeSpan> valueBasedTime, Map<Integer, TimeSpan> comparisonBasedTime,
			Map<Integer, TimeSpan> sophisticatedDependencyBasedTime, SpreadsheetProperties properties,
			boolean earlyTermination, int maxDiagSize) {
		super();
		super.clear();
		addData("Properties file", propertiesFile);

		Map<Integer, Boolean> timeoutMapVBM = getTimeoutMap(valueBasedTime, earlyTermination, maxDiagSize);
		Map<Integer, Boolean> timeoutMapDBM = getTimeoutMap(sophisticatedDependencyBasedTime, earlyTermination,
				maxDiagSize);
		Map<Integer, Boolean> timeoutMapCBM = getTimeoutMap(comparisonBasedTime, earlyTermination, maxDiagSize);

		addDiagnosisData("VBM", valueBasedDiagnoses, timeoutMapVBM, maxDiagSize);
		addDiagnosisData("DBM", sophisticatedDependencyDiagnoses, timeoutMapDBM, maxDiagSize);
		addDiagnosisData("CBM", comparisonBasedDiagnoses, timeoutMapCBM, maxDiagSize);

		addData("diag VBM", valueBasedDiagnoses.size());
		addData("diag DBM", sophisticatedDependencyDiagnoses.size());
		addData("diag CBM", comparisonBasedDiagnoses.size());
		
		
        
		addData("All VBM containd in DBM", areAllDiagnosesContained(sophisticatedDependencyDiagnoses,
				valueBasedDiagnoses, timeoutMapDBM, timeoutMapVBM) ? "true" : "false");		
		addData("All VBM containd in CBM",
				areAllDiagnosesContained(comparisonBasedDiagnoses, valueBasedDiagnoses, timeoutMapCBM, timeoutMapVBM)
						? "true" : "false");
		
		addData("True fault in VBM", areAllDiagnosesContained(valueBasedDiagnoses, trueFault, null, null) ? "true" : "false");
		addData("True fault in DBM", areAllDiagnosesContained(sophisticatedDependencyDiagnoses, trueFault, null, null) ? "true" : "false");
		addData("True fault in CBM", areAllDiagnosesContained(comparisonBasedDiagnoses, trueFault, null, null) ? "true" : "false");
		
	
		
		for (int i = 1; i <= maxDiagSize; i++) {
			if (valueBasedTime.containsKey(i)) {
				addData("VBM time [ms] (diagnosis size=" + i + ")",
						valueBasedTime.get(i).toString(Precision.MILLISECONDS).replace("ms", ""));
			} else if (!earlyTermination || i == 1) {
				addData("VBM time [ms] (diagnosis size=" + i + ")", "timeout");
			} else {
				addData("VBM time [ms] (diagnosis size=" + i + ")", "-");
			}
		}


		for (int i = 1; i <= maxDiagSize; i++) {
			if (sophisticatedDependencyBasedTime.containsKey(i)) {
				addData("DBM time [ms] (diagnosis size=" + i + ")",
						sophisticatedDependencyBasedTime.get(i).toString(Precision.MILLISECONDS).replace("ms", ""));
			} else if (!earlyTermination || i == 1) {
				addData("DBM time [ms] (diagnosis size=" + i + ")", "timeout");
			} else {
				addData("DBM time [ms] (diagnosis size=" + i + ")", "-");
			}
		}

		
		for (int i = 1; i <= maxDiagSize; i++) {
			if (comparisonBasedTime.containsKey(i)) {
				addData("CBM time [ms] (diagnosis size=" + i + ")",
						comparisonBasedTime.get(i).toString(Precision.MILLISECONDS).replace("ms", ""));
			} else if (!earlyTermination || i == 1) {
				addData("CBM time [ms] (diagnosis size=" + i + ")", "timeout");
			} else {
				addData("CBM time [ms] (diagnosis size=" + i + ")", "-");
			}
		}
		
		addData("Incorrect Output cells", properties.getIncorrectOutputCells().size());
		addData("Correct Output cells", properties.getCorrectOutputCells().size());
		addData("Faulty cells", properties.getFaultyCells().size());
		
		
		/**
		 * IULIA: 11.2022:
		 * NEW Columns in the RESULTS File: !! Make extend=TRUE !! 
		 * (1) "VBM No. Correct Diagnoses " (see Def. 19 Journal Paper 2022 Patrick Rodler) | "VBM No. Spurious Diagnoses" | "VBM Precision" | "VBM False Discovery Rate"
		 *
		 * " How many "correct" diagnoses (in terms of Def. 19) were among the solutions output by the three model types? We could then compute the 
		 * "precision" of the different models, and also the "false discovery rate" (fraction of "spurious" diagnoses, as per Def. 19)."
		 * 
		 */
		boolean extend=false;
		if(extend)
		 for (Diagnosis temp : trueFault){
			//(1) VBM 
			 int correct=0;
			 int spurious=0;
		     for (Diagnosis diag : valueBasedDiagnoses) {
	             if(isSubDiagnosis(temp, diag)) {
	            	correct+=1;
	            	}
	             
	             else 
	            	spurious+=1;
	         }
		     addData("VBM No. Correct Diagnoses", correct);
		     addData("VBM No. Spurious Diagnoses", spurious);
		     if(valueBasedDiagnoses.size()>0) {
		       addData("VBM Precision", (Float.valueOf(((float)correct/(float)valueBasedDiagnoses.size()))).toString());
		       addData("VBM False Discovery Rate", (Float.valueOf(((float)spurious/(float)valueBasedDiagnoses.size()))).toString());
		     }
		     else {
		    	 addData("VBM Precision", "-");
			     addData("VBM False Discovery Rate", "-");
		     }
		    	 
		     // (1) QDM 
		     correct=0;
			 spurious=0;
		     for (Diagnosis diag : comparisonBasedDiagnoses) {
	             if(isSubDiagnosis(temp, diag))
	            	correct+=1;
	             else 
	            	spurious+=1;
	         }
		     
		     addData("QDM No. Correct Diagnoses", correct);
		     addData("QDM No. Spurious Diagnoses", spurious);
		     if(comparisonBasedDiagnoses.size()>0) {
		        addData("QDM Precision", (Float.valueOf(((float)correct/(float)comparisonBasedDiagnoses.size()))).toString());
		        addData("QDM False Discovery Rate", (Float.valueOf(((float)spurious/(float)comparisonBasedDiagnoses.size()))).toString());
		     }
		     else {
		    	 addData("QDM Precision", "-");
			     addData("QDM False Discovery Rate", "-");
		     }
		    	 
		     
		     //(1) FDM 
		     correct=0;
			 spurious=0;
		     for (Diagnosis diag : sophisticatedDependencyDiagnoses) {
	             if(isSubDiagnosis(temp, diag))
	            	correct+=1;
	             else 
	            	spurious+=1;
	         }
		     
		     addData("FDM No. Correct Diagnoses", correct);
		     addData("FDM No. Spurious Diagnoses", spurious);
		     if(sophisticatedDependencyDiagnoses.size()>0) {
		       addData("FDM Precision", (Float.valueOf(((float)correct/(float)sophisticatedDependencyDiagnoses.size()))).toString());
		       addData("FDM False Discovery Rate", (Float.valueOf(((float)spurious/(float)sophisticatedDependencyDiagnoses.size()))).toString());
		     }
		     else
		     {
		    	 addData("FDM Precision", "-");
			     addData("FDM False Discovery Rate", "-");
		     }
		     /**
		      * 
		      *
		      * (2) Which fraction of SETnok (cf. Def. 19) does the best diagnosis DIAGbest in the solution set cover?
		      */
		    
		     //(2) VBM 
		     int maxFaultyCells=getMaxFaultyCellsInDiags(valueBasedDiagnoses, temp);
		     Float no=Float.valueOf((float)maxFaultyCells/(float)temp.size()*100);
		     addData("VBM % of the actually faulty cells in bestDIAG",no.toString() );
		     
		     // (2) QDM 
		     maxFaultyCells=getMaxFaultyCellsInDiags(comparisonBasedDiagnoses, temp);
		     no=Float.valueOf((float)maxFaultyCells/(float)temp.size()*100);
		     addData("QDM % of the actually faulty cells in bestDIAG",no.toString());
		     
		     // (2) FDM 
		     maxFaultyCells=getMaxFaultyCellsInDiags(sophisticatedDependencyDiagnoses, temp);
		     no=Float.valueOf((float)maxFaultyCells/(float)temp.size()*100);
		     addData("FDM % of the actually faulty cells in bestDIAG",no.toString());
		     
		     /**
		      * (3) How many diagnoses must be (chronologically, in the output set) investigated until:
		      *    (i) the first correct diagnosis (as per Def. 19) is found
		      *    (ii) all faults (i.e., SETnok) are covered by
		      *          (a) all investigated diagnoses, i.e., until SETnok setminus the union of the investigated diagnoses is equal to the empty set
		      *          (b) all investigated correct diagnoses, i.e., until SETnok setminus the union of the investigated correct diagnoses is 
		      *          equal to the empty set
		      *    (iii) one diagnosis equal to SETnok is found (i.e., *one* diagnosis covers all actually faulty cells)      
		      */
		     
		     //(3) (i) VBM
		     int noDiags=getNoDiagsUntilCorrect(valueBasedDiagnoses, temp);
		    
		     addData("VBM First CORRECT diagnosis after .. diagnoses", noDiags);		
		     //(3) (i) QDM
		     noDiags=getNoDiagsUntilCorrect(comparisonBasedDiagnoses, temp);
		     addData("QDM First CORRECT diagnosis after .. diagnoses", noDiags);		 
		     //(3) (i) FDM
		     noDiags=getNoDiagsUntilCorrect(sophisticatedDependencyDiagnoses, temp);
		     
		     addData("FDM First CORRECT diagnosis after .. diagnoses", noDiags);		  
		     
		     
		     //(3) (ii) (a) VBM
		     noDiags=getNoDiagsUntilAllFaultsCovered(valueBasedDiagnoses, temp);
		     addData("VBM All faults are covered after .. diagnoses", noDiags);
		     addData("VBM All faults are covered after .. diagnoses", noDiags);		
		     //(3) (ii) (a) QDM
		     noDiags=getNoDiagsUntilAllFaultsCovered(comparisonBasedDiagnoses, temp);
		     addData("QDM All faults are covered after .. diagnoses", noDiags);		
		     //(3) (ii) (a) FDM
		     noDiags=getNoDiagsUntilAllFaultsCovered(sophisticatedDependencyDiagnoses, temp);
		     addData("FDM All faults are covered after .. diagnoses", noDiags);		    
		     
		     
		     //(3) (ii) (b) VBM
		     noDiags=getNoCorrectDiagsUntilAllFaultsCovered(valueBasedDiagnoses, temp);
		     addData("VBM All faults are covered after ..correct diagnoses", noDiags);		
		     
		     //(3) (ii) (b) QDM
		     noDiags=getNoCorrectDiagsUntilAllFaultsCovered(comparisonBasedDiagnoses, temp);
		     addData("QDM All faults are covered after ..correct diagnoses", noDiags);		   
		     //(3) (ii) (b) FDM
		     noDiags=getNoCorrectDiagsUntilAllFaultsCovered(sophisticatedDependencyDiagnoses, temp);
		     addData("FDM All faults are covered after ..correct diagnoses", noDiags);		   
		     
		     
		     //(3) (iii) VBM
		     noDiags=getNoDiagsUntilComplete(valueBasedDiagnoses, temp);
		     if(noDiags<valueBasedDiagnoses.size())
		        addData("VBM One COMPLETE diagnosis after .. diagnoses", noDiags);
		     else
		    	 addData("VBM One COMPLETE diagnosis after .. diagnoses", "no complete diagnosis");
		   //(3) (iii) QDM
		     noDiags=getNoDiagsUntilComplete(comparisonBasedDiagnoses, temp);
		     if(noDiags<comparisonBasedDiagnoses.size())
		        addData("QDM One COMPLETE diagnosis after .. diagnoses", noDiags);
		     else
		    	 addData("QDM One COMPLETE diagnosis after .. diagnoses", "no complete diagnosis");
		   //(3) (iii) FDM
		     noDiags=getNoDiagsUntilComplete(sophisticatedDependencyDiagnoses, temp);
		     if(noDiags<sophisticatedDependencyDiagnoses.size())
		        addData("FDM One COMPLETE diagnosis after .. diagnoses", noDiags);
		     else
		    	 addData("FDM One COMPLETE diagnosis after .. diagnoses", "no complete diagnosis");
		     
		 }
	
	}
		 

	private int getNoDiagsUntilComplete(List<Diagnosis> diags, Diagnosis trueFaults) {
		// TODO Auto-generated method stub
		int inc=0;
		for (Diagnosis diag : diags) {
            if(diag.equals(trueFaults)) {
           	 return inc;
           	 }
            else 
           	 inc++;
        }

		return inc;
	}

	private int getNoCorrectDiagsUntilAllFaultsCovered(List<Diagnosis> diags, Diagnosis trueFaults) {
		// TODO Auto-generated method stub
		int inc=0;
		
		Diagnosis temp=(Diagnosis) trueFaults.clone();
		Set<Coords> faultyCells= temp.getCells();
		for (Diagnosis diag : diags) {
            if(isSubDiagnosis(trueFaults, diag)) {
               inc++;
               
               for(Coords cell:diag) {
            	   
   	    		   faultyCells.remove(cell);
   	    		   
   	    		if (faultyCells.isEmpty()) {
   	    			return inc;
   	    		}
               }
            }
        }
		
		return inc;
	}

	private int getNoDiagsUntilAllFaultsCovered(List<Diagnosis> diags, Diagnosis trueFaults) {
		// TODO Auto-generated method stub
		int inc=0;
		Diagnosis temp=(Diagnosis) trueFaults.clone();
		Set<Coords> faultyCells= temp.getCells();
		Set<Coords> toBeRemoved=new HashSet<Coords>();;
	    for (Diagnosis diag : diags) {
	    	Set<Coords> diagCells= diag.getCells();
	    	inc++; 
	    	for(Coords fault:faultyCells) {
				 for (Coords cell:diagCells) {
					if(fault.equals(cell)) {
						toBeRemoved.add(fault);
					}
				    	if(toBeRemoved.equals(temp.getCells()))
						      return inc;
				 }
			}
	    	for(Coords cell:toBeRemoved) {
	    		faultyCells.remove(cell);
	    		if (faultyCells.isEmpty())
	    			return inc;
	    	}
	    	
	     }
		return inc;
	}

	private int getNoDiagsUntilCorrect(List<Diagnosis> diags, Diagnosis trueFaults) {
		// TODO Auto-generated method stub
		int inc=0;
	     for (Diagnosis diag : diags) {
            if(isSubDiagnosis(trueFaults, diag)) {
           	return inc;
           	}
            else 
           	inc++;
        }

		return inc;
	}

	private int getMaxFaultyCellsInDiags(List<Diagnosis> diagnoses, Diagnosis trueFault) {
		// TODO Auto-generated method stub
		int max=0;
		Set<Coords> faultyCells= trueFault.getCells();
		for(Diagnosis diag : diagnoses) {
			Set<Coords> diagCells= diag.getCells();
			int i=0;
			for(Coords fault:faultyCells) {
				for (Coords cell:diagCells)
					if(fault.equals(cell)) 
					 i++;
				if(i>max)
					max=i;
			}
						
		}
		return max;
	}

	private void addDiagnosisData(String modelName, List<Diagnosis> diagnoses, Map<Integer, Boolean> timeoutMap,
			int maxDiagSize) {
		Map<Integer, List<Diagnosis>> diagnosesBySize = getDiagnosesBySize(diagnoses);
		for (int i = 1; i <= maxDiagSize; i++) {
			if (diagnosesBySize.containsKey(i)) {
				addData(modelName + " diagnosis size=" + i, diagnosisToString(diagnosesBySize.get(i)));
				addData(modelName + " diagnosis amount (size=" + i + ")", diagnosesBySize.get(i).size());
			} else if (timeoutMap.get(i) == true) {
				addData(modelName + " diagnosis size=" + i, "timeout");
				addData(modelName + " diagnosis amount (size=" + i + ")", "timeout");
			} else {
				addData(modelName + " diagnosis size=" + i, "-");
				addData(modelName + " diagnosis amount (size=" + i + ")", 0);
			}
		}
	}

	private Map<Integer, List<Diagnosis>> getDiagnosesBySize(List<Diagnosis> diagnoses) {
		Map<Integer, List<Diagnosis>> diagnosesBySize = new HashMap<Integer, List<Diagnosis>>();
		for (Diagnosis diag : diagnoses) {
			int size = diag.size();
			List<Diagnosis> sameSizeDiag = new ArrayList<Diagnosis>();
			if (diagnosesBySize.containsKey(size))
				sameSizeDiag = diagnosesBySize.get(size);
			sameSizeDiag.add(diag);
			diagnosesBySize.put(size, sameSizeDiag);
		}
		return diagnosesBySize;
	}

	private boolean areAllDiagnosesContained(List<Diagnosis> superset, List<Diagnosis> subset,
			Map<Integer, Boolean> timeoutSuperset, Map<Integer, Boolean> timeoutSubset) {

		Boolean timeoutHappened = false;

		if (timeoutSuperset != null) {
			for (Boolean timeout : timeoutSuperset.values()) {
				if (timeout)
					timeoutHappened = true;
				break;
			}
		}

		if (timeoutSubset != null && timeoutHappened == false) {
			for (Boolean timeout : timeoutSubset.values()) {
				if (timeout)
					timeoutHappened = true;
				break;
			}
		}

		for (Diagnosis diagnosis : subset) {
			if (superset.contains(diagnosis))
				continue;

			boolean contained = false;
			for (Diagnosis diag : superset) {
				if (isSubDiagnosis(diagnosis, diag)) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				if (!timeoutHappened) {
					System.out.println(super.getResultEntry("Properties file") + ": Diagnosis " + diagnosis.toString()
							+ " not contained!");
				}
				return false;
			}
		}
		return true;
	}

	private String diagnosisToString(List<Diagnosis> diagnoses) {
		StringBuilder strB = new StringBuilder();
		for (Diagnosis diagnosis : diagnoses) {
			strB.append("(");
			for (Coords cell : diagnosis.getCells()) {
				strB.append(cell.getConstraintString());
				strB.append(",");
			}
			strB.append("),");
		}
		String str = strB.toString().replace(",)", (")"));
		if (str.length() > 0)
			return str.substring(0, str.length() - 1);
		return "";
	}

	private Integer getNumberOfCellsInDiagnoses(List<Diagnosis> diagnoses) {
		Set<Coords> cells = new HashSet<Coords>();
		for (Diagnosis diagnosis : diagnoses) {
			cells.addAll(diagnosis.getCells());
		}
		return cells.size();
	}

	private boolean isSubDiagnosis(Diagnosis superDiagnosis, Diagnosis subDiagnosis) {
		for (Coords subCell : subDiagnosis.getCells()) {
			if (!superDiagnosis.getCells().contains(subCell))
				return false;
			
		}
		return true;
	}

}
