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
package io.github.mtrevisan.pizza.bakingpans;

import io.github.mtrevisan.pizza.utils.Helper;


public enum BakingPanMaterial{
	CAST_IRON(560.548, 0.64, 7200.),

	ALUMINIUM(896.9, 0.8, 2700.),

	//https://inis.iaea.org/search/search.aspx?orig_q=RN:33040336
	STAINLESS_STEEL_304(490. - 530., 0.32 - 0.38, 7850. - 8060.),
	//https://inis.iaea.org/search/search.aspx?orig_q=RN:33040336
	STAINLESS_STEEL_316(490. - 530., 0.44 - 0.51, 7870.),

	//https://www.electronics-cooling.com/1999/09/the-thermal-conductivity-of-ceramics/
	CERAMIC(850., -1., 2000. - 6000.),
	CLAY(0.33, -1., 1680.),
	CORDIERITE_STONE(800. - 850., 0.95, 2000. - 2300.);


	/** [J / (kg · K)] */
	public final double specificHeat;
	//https://www.cpp.edu/~lllee/TK3111heat.pdf pag 19
	//https://en.wikipedia.org/wiki/List_of_thermal_conductivities
	public final double emissivity;
	/** [kg / m³] */
	public final double density;


	BakingPanMaterial(final double specificHeat, final double emissivity, final double density){
		this.specificHeat = specificHeat;
		this.emissivity = emissivity;
		this.density = density;
	}

	private static final double[] THERMAL_CONDUCTIVITY_COEFFICIENTS = {238., 0.0175, -0.000113, 2.8e-8};

	/**
	 *
	 * @param temperature	Temperature [°C].
	 * @return The thermal conductivity [W / (m · K)].
	 */
	public double thermalConductivity(final double temperature){
		return switch(this){
			case CAST_IRON -> 52.;
			case ALUMINIUM -> Helper.evaluatePolynomial(THERMAL_CONDUCTIVITY_COEFFICIENTS, temperature);
			//14 - 17
			case STAINLESS_STEEL_304 -> 14.4;
			//13 - 17
			case STAINLESS_STEEL_316 -> (13. + 17.) / 2.;
			//80 - 200
			case CERAMIC -> (80. + 200.) / 2.;
			//0.15 - 1.8
			case CLAY -> (0.15 + 1.8) / 2.;
			case CORDIERITE_STONE -> 3.;
		};
	};

}
