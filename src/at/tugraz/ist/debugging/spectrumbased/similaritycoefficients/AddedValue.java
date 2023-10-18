package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class AddedValue extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return Math.max(PB_given_A() - PB(), PA_given_B() - PA());
	}

	@Override
	public String getCoefficientName() {
		return "Added Value";
	}

}
