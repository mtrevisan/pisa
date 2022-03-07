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


/**
 * @see <a href="https://www.tandfonline.com/doi/full/10.1080/10942910701586273">Fasina, Colley. Viscosity and specific heat of vegetable oils as a function of temperature: 35°C to 180 °C. 2008.</a>
 */
public enum FatType{
	ALMOND_OIL(3314., 2.143),
	CANOLA_OIL(3003., 2.086),
	CORN_OIL(3162., 1.963),
	GRAPESEED_OIL(2920., 2.037),
	HAZELNUT_OIL(2492., 1.807),
	OLIVE_OIL(1715., 2.025),
	PEANUT_OIL(3677., 2.449),
	SAFFLOWER_OIL(2832., 2.181),
	SESAME_OIL(3043., 2.446),
	SOYBEAN_OIL(2792., 1.956),
	SUNFLOWER_OIL(3477., 2.566),
	WALNUT_OIL(2835., 2.165),

	BUTTER(0., 2.72);


	//coefficients for the linear interpolation of specific heat
	private final double m;
	private final double b;


	FatType(final double m, final double b){
		this.m = m;
		this.b = b;
	}

	public double specificHeat(final double temperature){
		return m * temperature + b;
	}

}
