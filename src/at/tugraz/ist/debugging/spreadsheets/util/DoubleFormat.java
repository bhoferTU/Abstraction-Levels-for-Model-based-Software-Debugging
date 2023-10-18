package at.tugraz.ist.debugging.spreadsheets.util;

/**
 *  formats double numbers, used in gui
 */
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class DoubleFormat {
	public final static DecimalFormat format = new DecimalFormat("#.###");
	public final static NumberFormat locale = NumberFormat
			.getInstance(Locale.US);;

}
