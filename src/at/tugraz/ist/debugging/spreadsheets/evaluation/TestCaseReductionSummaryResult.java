package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TestCaseReductionSummaryResult extends TestCaseReductionResult {

	public static String getHeader() {
		StringBuilder strBuild = new StringBuilder();

		strBuild.append("Spreadsheet" + SEPERATOR);
		strBuild.append("Number of correct outptut cells" + SEPERATOR);
		strBuild.append("Number of incorrect outptut cells" + SEPERATOR);
		strBuild.append("Average best case" + SEPERATOR);
		strBuild.append("Average average case" + SEPERATOR);
		strBuild.append("Average worst case" + SEPERATOR);
		strBuild.append(LINEBREAK);

		return strBuild.toString();
	}

	Double SflRanking = -1.0;

	Double SflSame = -1.0;

	public TestCaseReductionSummaryResult(String faultFile,
			Integer numberOfCorrectOutputCells,
			Integer numberOfIncorrrectOutputCells, Double sflRanking,
			Double sflSame) {
		super(faultFile, numberOfCorrectOutputCells,
				numberOfIncorrrectOutputCells);

		SflRanking = sflRanking;
		SflSame = sflSame;
	}

	public TestCaseReductionSummaryResult(String faultFile,
			Integer numberOfCorrectOutputCells,
			Integer numberOfIncorrrectOutputCells, List<Integer> sflRanking,
			List<Integer> sflSame) {

		super(faultFile, numberOfCorrectOutputCells, numberOfCorrectOutputCells);

		int rank = 0;
		for (Integer i : sflRanking) {
			rank += i;
		}
		int same = 0;
		for (Integer i : sflSame) {
			same += i;
		}

		SflRanking = (double) rank / (double) sflRanking.size();
		SflSame = (double) same / (double) sflSame.size();
	}

	@Override
	public String toString() {
		StringBuilder strBuild = new StringBuilder();

		strBuild.append(faultFile + SEPERATOR);
		strBuild.append(numberOfCorrectOutputCells + SEPERATOR);
		strBuild.append(numberOfIncorrectOutputCells + SEPERATOR);
		strBuild.append(NumberFormat.getNumberInstance(Locale.GERMAN).format(SflRanking) + SEPERATOR);
		strBuild.append(NumberFormat.getNumberInstance(Locale.GERMAN).format(SflRanking + SflSame / 2) + SEPERATOR);
		strBuild.append(NumberFormat.getNumberInstance(Locale.GERMAN).format(SflRanking + SflSame) + SEPERATOR);

		strBuild.append(LINEBREAK);

		return strBuild.toString();
	}

}
