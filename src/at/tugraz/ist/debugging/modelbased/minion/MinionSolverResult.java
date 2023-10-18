package at.tugraz.ist.debugging.modelbased.minion;

public class MinionSolverResult {
	private String error;
	private String solution;

	public MinionSolverResult() {

	}

	public MinionSolverResult(String solution, String error) {
		this.solution = solution;
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public String getSolution() {
		return solution;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

}
