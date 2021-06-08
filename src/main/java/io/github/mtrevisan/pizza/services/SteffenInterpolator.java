package io.github.mtrevisan.pizza.services;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.MathArrays;


/**
 * The aim is to construct a piecewise cubic interpolation function that passes through {@code N} given data points in a monotonic way
 * with the slope of the curve changing continuously across the junction points.
 *
 * https://math.stackexchange.com/questions/45218/implementation-of-monotone-cubic-interpolation
 * https://www.weatherclasses.com/uploads/1/3/1/3/131359169/steffen_interpolation_paper.pdf
 */
public class SteffenInterpolator{

	public PolynomialSplineFunction interpolate(final double[] x, final double[] y) throws DimensionMismatchException,
			NumberIsTooSmallException, NonMonotonicSequenceException{
		if(x.length != y.length)
			throw new DimensionMismatchException(x.length, y.length);
		if(x.length < 3)
			throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS, x.length, 3, true);
		MathArrays.checkOrder(x);

		//number of intervals; the number of data points is n + 1
		final int n = x.length - 1;

		//differences between knot points
		final double[] h = new double[n];
		//slope between knot points
		final double[] s = new double[n];
		for(int i = 0; i < n; i ++){
			final double deltaX = x[i + 1] - x[i];
			h[i] = deltaX;
			s[i] = (y[i + 1] - y[i]) / deltaX;
		}

		final double[] y_dot = new double[n + 1];
		for(int i = 1; i < n; i ++){
			final double p = 0.5 * (h[i] * s[i - 1] + h[i - 1] * s[i]) / (h[i - 1] + h[i]);
			y_dot[i] = (Math.signum(s[i - 1]) + Math.signum(s[i])) * Math.min(p, Math.min(Math.abs(s[i - 1]), Math.abs(s[i])));
		}
		y_dot[0] = s[0];
		y_dot[n] = s[n - 1];

		//cubic spline coefficients
		final PolynomialFunction[] polynomials = new PolynomialFunction[n];
		final double[] coefficients = new double[4];
		for(int i = 0; i < n; i ++){
			coefficients[0] = y[i];
			coefficients[1] = y_dot[i];
			coefficients[2] = (3. * s[i] - 2. * y_dot[i] - y_dot[i + 1]) / h[i];
			coefficients[3] = (y_dot[i] + y_dot[i + 1] - 2. * s[i]) / (h[i] * h[i]);
			polynomials[i] = new PolynomialFunction(coefficients);
		}

		return new PolynomialSplineFunction(x, polynomials);
	}

}
