package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class OddsRatio extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() * PNANB(), PANB() * PNAB());
	}

	@Override
	public String getCoefficientName() {
		return "Odds Ratio";
	}

}
