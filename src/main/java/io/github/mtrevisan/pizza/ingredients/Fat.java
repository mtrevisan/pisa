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
package io.github.mtrevisan.pizza.ingredients;

import io.github.mtrevisan.pizza.DoughException;
import io.github.mtrevisan.pizza.utils.Helper;

import java.util.Objects;


public final class Fat{

	/**
	 * @see <a href="https://www.tandfonline.com/doi/full/10.1080/10942910701586273">Fasina, Colley. Viscosity and specific heat of vegetable oils as a function of temperature: 35°C to 180 °C. 2008.</a>
	 */
	public enum FatType{
		ALMOND_OIL(2.28581, 1.4566e-03, 1.3889e-05, -3.0282e-08),
		CANOLA_OIL(2.15364, 8.88e-04, 2.2062e-05, -6.658e-08),
		CORN_OIL(1.6653, -1.119e-03, 4.446e-05, -1.4734e-07),
		GRAPESEED_OIL(1.56561, -8.551e-04, 3.0181e-05, -7.568e-08),
		HAZELNUT_OIL(1.710717, -4.1854e-04, 2.4914e-05, -6.8208e-08),
		OLIVE_OIL(2.36357, -4.379756e-02, 0.001162965, -1.5038497e-05, 1.0330492e-07, -3.619801e-10, 5.08154e-13),
		PEANUT_OIL(2.13996, -8.0979e-03, 2.21472e-04, -2.26777e-06, 1.156938e-08, -2.35476e-11),
		SAFFLOWER_OIL(2.0508, -2.936e-04, 3.50983e-05, -1.13766e-07),
		SESAME_OIL(2.08728, 1.0255e-04, 2.5556e-05, -6.8968e-08),
		SOYBEAN_OIL(1.60554, 1.5652e-03, 1.430277e-05, -4.58771e-08),
		SUNFLOWER_OIL(2.217634, -1.629e-04, 3.0853e-05, -8.797e-08),
		WALNUT_OIL(2.001465, -5.26e-05, 3.336e-05, -1.18765e-07),

		BUTTER(0., 2.72);


		//coefficients for the linear interpolation of specific heat
		private final double[] coeffs;


		FatType(final double... coeffs){
			this.coeffs = coeffs;
		}

		public double specificHeat(final double temperature){
			return Helper.evaluatePolynomial(coeffs, temperature);
		}

	}


	public final FatType type;
	/** Raw fat content [% w/w]. */
	public final double fat;
	/** Salt content [% w/w]. */
	public final double salt;
	/** Water content [% w/w]. */
	public final double water;

	/** Fat density [g / ml]. */
	final double density;


	/**
	 * @param type	Fat type.
	 * @param fat	Fat content [% w/w].
	 * @param water	Water content [% w/w].
	 * @return	The instance.
	 * @throws DoughException	If there are errors in the parameters' values.
	 */
	public static Fat create(final FatType type, final double fat, final double salt, final double water, final double density)
			throws DoughException{
		Objects.requireNonNull(type, "Type must be non null");
		if(fat < 0.)
			throw DoughException.create("Fat content must be non-negative");
		if(salt < 0.)
			throw DoughException.create("Salt content must be non-negative");
		if(water < 0.)
			throw DoughException.create("Water content must be non-negative");
		if(density <= 0.)
			throw DoughException.create("Density must be positive");

		return new Fat(type, fat, salt, water, density);
	}

	private Fat(final FatType type, final double fat, final double salt, final double water, final double density){
		this.type = type;
		this.fat = fat;
		this.salt = salt;
		this.water = water;
		this.density = density;
	}

}
