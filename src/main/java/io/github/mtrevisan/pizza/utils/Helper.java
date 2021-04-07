package io.github.mtrevisan.pizza.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class Helper{

	/**
	 * Evaluate polynomial using Horner's method.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Horner%27s_method">Horner's method</a>.
	 *
	 * @param c	The array of constants (index 0 is the parameter for the 0-th power).
	 * @param x	The value at which to calculate the polynomial.
	 * @return	The polynomial value.
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

	public static boolean changedSign(final double a, final double b, final double ref){
		return ((ref - a) * (ref - b) < 0.);
	}

}
