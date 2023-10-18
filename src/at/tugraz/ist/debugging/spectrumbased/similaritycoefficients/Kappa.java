package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Kappa extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() + PNANB() - PA() * PB() - (1.0 - PA()) * (1.0 - PB()),
				1.0 - PA() * PB() - (1.0 - PA()) * (1.0 - PB()));
	}

	@Override
	public String getCoefficientName() {
		return "Kappa";
	}

}
