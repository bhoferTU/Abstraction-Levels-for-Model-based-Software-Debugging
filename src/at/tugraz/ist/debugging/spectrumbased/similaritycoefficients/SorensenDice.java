package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class SorensenDice extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(2.0 * PAB(), 2.0 * PAB() + PNAB() + PANB());
	}

	@Override
	public String getCoefficientName() {
		return "Sorensen-Dice";
	}

}
