package at.tugraz.ist.debugging.spreadsheets.evaluation;

public abstract class TestCaseReductionResult {

	public static final String LINEBREAK = "\n";

	public static final String SEPERATOR = ";";
	String faultFile = null;

	Integer numberOfCorrectOutputCells = -1;
	Integer numberOfIncorrectOutputCells = -1;

	public TestCaseReductionResult(String faultFile,
			Integer numberOfCorrectOutputCells,
			Integer numberOfIncorrectOutputCells) {
		super();
		this.faultFile = faultFile;
		this.numberOfCorrectOutputCells = numberOfCorrectOutputCells;
		this.numberOfIncorrectOutputCells = numberOfIncorrectOutputCells;
	}

	@Override
	public abstract String toString();

}
