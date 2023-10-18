package at.tugraz.ist.debugging.spreadsheets.util;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigMath {
	public static final BigDecimal LN2 = new BigDecimal(Math.log(2));
	public static final MathContext MC = MathContext.DECIMAL128;

	public static final BigDecimal ONE = new BigDecimal(1);

	public static final BigDecimal ZERO = new BigDecimal(0);
}
