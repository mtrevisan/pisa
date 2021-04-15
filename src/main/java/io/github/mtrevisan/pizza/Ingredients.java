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
package io.github.mtrevisan.pizza;


public class Ingredients{

	/** Total dough weight [g]. */
	double dough;
	/** [g] */
	double doughPrecision;
	/** Temperature of ingredients [°C]. */
	double ingredientsTemperature;
	/** Desired dough temperature [°C]. */
	double doughTemperature;

	/** Chlorine dioxide in water [mg/l]. */
	double waterChlorineDioxide;
	/** Fixed residue in water [mg/l]. */
	double waterFixedResidue;

	Flour flour;

	YeastType yeastType;
	/** Raw yeast content [%]. */
	double rawYeast = 1.;

	SugarType sugarType;
	/** Raw sugar content [%]. */
	double sugarContent = 1.;
	/** Water content in sugar [%]. */
	double sugarWaterContent;

	/** Raw fat content [%]. */
	double fatContent = 1.;
	/** Water content in fat [%]. */
	double fatWaterContent;
	/** Salt content in fat [%]. */
	double fatSaltContent;


	public static Ingredients create(final double dough, final double doughPrecision){
		return new Ingredients(dough, doughPrecision);
	}

	private Ingredients(final double dough, final double doughPrecision){
		this.dough = dough;
		this.doughPrecision = doughPrecision;
	}

}
