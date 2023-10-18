package at.tugraz.ist.debugging.modelbased.main;

import java.io.File;

import at.tugraz.ist.debugging.modelbased.DebuggingStrategyFactory;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.IModelBasedStrategy;
import at.tugraz.ist.debugging.modelbased.ModelBasedResult;
import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyResult;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.algorithm.ModelConfig;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.util.time.TimeSpan.Precision;

public class Executor extends Thread {
	String error;
	String directoryName;
	SpreadsheetProperties properties;
	ModelBasedResult result;

	public Executor(String directoryName, SpreadsheetProperties properties) {
		this.properties = properties;
		this.result = null;
		this.error = null;
		this.directoryName = directoryName;
	}

	public String getErrorMessage() {
		return error;
	}

	public ModelBasedResult getResult() {
		if (result == null && ConstraintStrategyConfiguration.getStrategy().getSolver() == ESolver.Minion) {
			System.out.println("timeout?");
			result = new ModelBasedResult(properties.getExcelSheetName(), ConstraintStrategyConfiguration.getStrategy(),
					ConstraintStrategyConfiguration.useCones(), -1, -1);// 
			ConstraintStrategyResult solution = MinionConstraintStrategy.result;
//			result.setRuntime(overallSolvingDuration.getTimeSpan(Precision.MILLISECONDS));
	        result.addSolvingTimes(solution.getRuntimeSolvingTimes());
	        result.setRuntimeValidating(solution.getRuntimeValidating(Precision.MILLISECONDS));
			result.addDiagnoses(solution.getHighPriorityDiagnoses(), solution.getLowPriorityDiagnoses());
			result.setMessage(solution.getConstraintBasedInformationAsString());
//	        result.setCones(cones);
	        result.setMaxDiagnosisSize(solution.getMaxDiagnosisSize());
		}
		return result;
	}

	@Override
	public void run() {
		try {
			
			String spreadsheetPath = directoryName;
			if (directoryName.lastIndexOf(File.separator)!=-1)
				spreadsheetPath = directoryName.substring(0, directoryName.lastIndexOf(File.separator));
		    spreadsheetPath = spreadsheetPath + File.separator + properties.getExcelSheetName();
			if(properties.getExcelSheetName().contains("..\\"))
				spreadsheetPath = properties.getExcelSheetPath();
			CellContainer cellContainer = CellContainer.create(spreadsheetPath);

			ModelConfig configData = new ModelConfig(properties, cellContainer);
			IModelBasedStrategy strategyInstance = DebuggingStrategyFactory
					.get(ConstraintStrategyConfiguration.getStrategy());

			result = strategyInstance.runAlgorithm(configData);
		} catch (Exception e) {
			System.out.println(" ... failed: " + e.getMessage());
			error = e.getMessage();
		}
	}
	
	
}
