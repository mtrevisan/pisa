/**
 * Copyright (c) 2021 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
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


public class Integrator{

	/**
	 * @see <a href="https://en.wikipedia.org/wiki/Romberg%27s_method">Romberg's method</a>
	 *
	 * @param f	The function to be integrated.
	 * @param a	The inferior limit of integration.
	 * @param b	The superior limit of integration.
	 * @param eps	The absolute error.
	 * @param max	The maximum number of iterations.
	 * @return	The integral of the given function.
	 */
	public static double integrate(final UnivariateFunction f, final double a, final double b, double eps, int max){
		eps = Math.max(eps, 1e-15);
		max ++;

		//first index will not be used
		final double[] s = new double[max + 1];
		//used to hold the value R(n-1,m-1), from the previous row so that 2 arrays are not needed
		double r = 0.;
		double lastValue = Double.POSITIVE_INFINITY;
		for(int k = 1; k < max; k ++){
			for(int i = 1; i <= k; i ++){
				if(i == 1){
					r = s[i];
					s[i] = integrateTrapezoidal(f, a, b, (int)Math.pow(2., k - 1.));
				}
				else{
					s[k] = (Math.pow(4., i - 1.) * s[i - 1] - r) / (Math.pow(4., i - 1.) - 1.);
					r = s[i];
					s[i] = s[k];
				}
			}

			if(Math.abs(lastValue - s[k]) < eps)
				return s[k];

			lastValue = s[k];
		}

		return s[max - 1];
	}

	private static double integrateTrapezoidal(final UnivariateFunction f, final double a, final double b, final int n){
		double sum = (f.value(a) + f.value(b)) * 0.5;
		for(int k = 1; k < n; k ++)
			sum += f.value(a + k * (b - a) / n);
		return sum * (b - a) / n;
	}

}
