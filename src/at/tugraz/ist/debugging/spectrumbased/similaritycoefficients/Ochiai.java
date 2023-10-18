package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

/**
 * Computes the similarity coefficient for the given hit spectra by using the
 * Ochiai coefficient.
 * 
 * <pre>
 * coefficient = a11/sqrt((a11+a10)*(a11+a01))
 * a11 = FailedInvolved
 * a10 = PassedInvolved 
 * a01 = FailedNotInvolved
 * </pre>
 * 
 * @author bhofer
 * 
 */
public class Ochiai extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB(), Math.sqrt(PA() * PB()));
	}

	@Override
	public String getCoefficientName() {
		return "Ochiai";
	}

}
