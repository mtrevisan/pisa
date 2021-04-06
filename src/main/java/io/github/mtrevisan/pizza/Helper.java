package io.github.mtrevisan.pizza;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class Helper{

	/**
	 * Evaluate polynomial using Horner's method.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Horner%27s_method">Horner's method</a>.
	 *
	 * @param c
	 * @param x
	 * @return
	 */
	public static double evaluatePolynomial(final double[] c, final double x) {
		double y = 0;
		for(int i = c.length - 1; i >= 0; i --)
			y = c[i] + y * x;
		return y;
	}

	public static double round(final double value, final int decimalPlaces){
		if(decimalPlaces < 0)
			throw new IllegalArgumentException();

		return BigDecimal.valueOf(value)
			.setScale(decimalPlaces, RoundingMode.HALF_UP)
			.doubleValue();
	}

}
