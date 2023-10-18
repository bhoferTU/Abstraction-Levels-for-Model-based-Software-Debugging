package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class SebagSchoenauer extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB(), PANB());
	}

	@Override
	public String getCoefficientName() {
		return "Sebag-Schoenauer";
	}

}
