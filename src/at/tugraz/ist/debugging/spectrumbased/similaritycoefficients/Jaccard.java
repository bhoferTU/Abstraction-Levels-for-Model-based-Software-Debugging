package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

/**
 * Computes the similarity coefficient for the given hit spectra by using the
 * Jaccard coefficient.
 * 
 * <pre>
 * coefficient = a11/(a11+a10+a01)
 * a11 = FailedInvolved
 * a10 = PassedInvolved 
 * a01 = FailedNotInvolved
 * </pre>
 * 
 * @author bhofer
 * 
 */
public class Jaccard extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB(), PA() + PB() - PAB());
	}

	@Override
	public String getCoefficientName() {
		return "Jaccard";
	}

}
