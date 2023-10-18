package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class RelativeRisk extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PB_given_A(), PB_given_NA());
	}

	@Override
	public String getCoefficientName() {
		return "Relative Risk";
	}

}
