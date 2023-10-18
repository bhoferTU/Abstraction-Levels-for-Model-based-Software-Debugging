package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Klosgen extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return Math.sqrt(PAB())
				* Math.max(PB_given_A() - PB(), PA_given_B() - PA());
	}

	@Override
	public String getCoefficientName() {
		return "Klosgen";
	}

}
