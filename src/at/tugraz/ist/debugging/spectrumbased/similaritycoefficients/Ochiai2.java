package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Ochiai2 extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() + PNANB(), Math.sqrt(PAB() + PNAB())
				* (PAB() + PANB()) * (PNANB() + PNAB()) * (PNANB() + PANB()));
	}

	@Override
	public String getCoefficientName() {
		return "Ochiai 2";
	}

}
