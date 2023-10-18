package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TestCaseReductionDetailedResult extends TestCaseReductionResult {

	public static String getHeader() {
		StringBuilder strBuild = new StringBuilder();

		strBuild.append("Spreadsheet" + SEPERATOR);
		strBuild.append("Number of correct outptut cells" + SEPERATOR);
		strBuild.append("Number of incorrect outptut cells" + SEPERATOR);
		strBuild.append("Average Rankings" + SEPERATOR);
		strBuild.append(LINEBREAK);

		return strBuild.toString();
	}

	List<Double> averageRanking;

	public TestCaseReductionDetailedResult(String faultFile,
			Integer numberOfCorrectOutputCells,
			Integer numberOfIncorrectOutputCells, List<Double> averageRanking) {
		super(faultFile, numberOfCorrectOutputCells,
				numberOfIncorrectOutputCells);
		this.averageRanking = averageRanking;
	}

	@Override
	public String toString() {
		StringBuilder strBuild = new StringBuilder();

		strBuild.append(faultFile + SEPERATOR);
		strBuild.append(numberOfCorrectOutputCells + SEPERATOR);
		strBuild.append(numberOfIncorrectOutputCells + SEPERATOR);
		for (Double ranking : averageRanking) {
			strBuild.append(NumberFormat.getNumberInstance(Locale.GERMAN).format(ranking) + SEPERATOR);
		}
		strBuild.append(LINEBREAK);
		return strBuild.toString();
	}

}
