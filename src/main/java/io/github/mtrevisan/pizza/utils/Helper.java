/**
 * Copyright (c) 2019-2020 Mauro Trevisan
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.pizza.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class Helper{

	private Helper(){}


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
