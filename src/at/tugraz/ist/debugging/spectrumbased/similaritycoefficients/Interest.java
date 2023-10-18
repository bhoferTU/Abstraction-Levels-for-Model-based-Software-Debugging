package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Interest extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB(), PA() * PB());
	}

	@Override
	public String getCoefficientName() {
		return "Interest";
	}

}
