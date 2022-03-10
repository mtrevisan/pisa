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

import io.github.mtrevisan.pizza.ingredients.Carbohydrate;
import io.github.mtrevisan.pizza.utils.Helper;


public final class WaterHelper{

	//[°C]
	public static final double ABSOLUTE_ZERO = 273.15;

	private static final double[] BOILING_TEMPERATURE_COEFFICIENTS = {19.46, 0.36395, -1.27769e-3, 3.21349e-6, -5.12207e-9, 4.92425e-12, -2.59915e-15, 5.7739e-19};

	private static final double[] SPECIFIC_HEAT_A0_COEFFICIENTS = {4.193, -2.273e-4, 2.369e-6, 1.670e-10};
	private static final double[] SPECIFIC_HEAT_AP_COEFFICIENTS = {-3.978e-5, 3.229e-7, -1.073e-11};
	private static final double[] SPECIFIC_HEAT_AP2_COEFFICIENTS = {1.913e-9, -4.176e-11, 2.306e-13};
	private static final double[] SPECIFIC_HEAT_B0_COEFFICIENTS = {5.020e-3, -9.961e-6, 6.815e-8};
	private static final double[] SPECIFIC_HEAT_BT_COEFFICIENTS = {-2.605e-5, 4.585e-8, 7.642e-10};
	private static final double[] SPECIFIC_HEAT_BT2_COEFFICIENTS = {-3.649e-8, 2.496e-10};
	private static final double[] SPECIFIC_HEAT_BT3_COEFFICIENTS = {1.186e-6, 4.346e-9};


	private WaterHelper(){}

	/**
	 * @see <a href="https://journals.ametsoc.org/view/journals/bams/98/7/bams-d-16-0174.1.xml">Methods for computing the boiling temperature of water at varying pressures</a>
	 *
	 * @param salt	Salt [% w/w].
	 * @param sugar	Sugar [% w/w].
	 * @param pressure	Pressure [hPa].
	 * @return	The boiling temperature of salted and sweetened water [°C].
	 */
	public static double boilingTemperature(final double salt, final double sugar, final Carbohydrate.CarbohydrateType carbohydrateType, final double pressure){
		final double temperature = Helper.evaluatePolynomial(BOILING_TEMPERATURE_COEFFICIENTS, pressure);

		//boiling point elevation ΔTb = kb ⋅ m ⋅ i [K], for water, kb = 0.515 K/(mol/kg), and i is the number of particles formed when
		//that compound dissolves (for covalent compounds, this number is always 1)
		final double saltBPE = 0.515 * (salt * 1000. / Dough.MOLECULAR_WEIGHT_SODIUM_CHLORIDE) * 2.;
		final double sugarBPE = 0.515 * (sugar * 1000. / carbohydrateType.molecularWeight) * 1.;

		return temperature + saltBPE + sugarBPE;
	}


	/**
	 * @see "Simion, Grigoras, Rosu, Gavrila. Mathematical modelling of density and viscosity of NaCl aqueous solutions. 2014."
	 * @see <a href="https://www.translatorscafe.com/unit-converter/en-US/calculator/salt-water-density/">Salt Water Density Calculator</a>
	 * @see <a href="https://www.engineeringtoolbox.com/density-aqueous-solution-organic-sugar-alcohol-concentration-d_1954.html">Density of aqueous solutions of organic substances as sugars and alcohols</a>
	 *
	 * @param water	Hydration [% w/w].
	 * @param salt	Salt quantity [% w/w].
	 * @param sugar	Sugar quantity [% w/w].
	 * @param temperature	Temperature [°C].
	 * @return	The density [kg/l].
	 */
	public static double brineDensity(final double pureWaterDensity, final double water, final double salt, final double sugar,
			final double temperature){
		//molar mass of glucose: 180.156 g/mol
		//molar mass of sucrose/maltose: 342.29648 g/mol
		//molar mass of salt: 58.44277 g/mol
		//convert salt and sugar to [g/l]
		return pureWaterDensity
			+ ((0.020391744 * salt + 0.003443681 * sugar)
			+ (-0.000044231 * salt + 0.0000004195 * sugar) * (temperature + ABSOLUTE_ZERO)
			) * 1000. / water;
	}


	/**
	 * Validity: 0 < temperature < 374 °C; 0 < salinity < 40 g/kg; 0.1 < pressure < 100 MPa.
	 * Accuracy: ±4.62%.
	 *
	 * @see <a href="http://web.mit.edu/lienhard/www/Thermophysical_properties_of_seawater-DWT-16-354-2010.pdf">Thermophysical properties of seawater: a review of existing correlations and data</a>
	 *
	 * @param salinity	Salinity [g/kg].
	 * @param temperature	Temperature [°C].
	 * @param pressure	Pressure [hPa].
	 * @return	The specific heat [J / (kg · K)].
	 */
	public static double specificHeat(final double salinity, final double temperature, final double pressure){
		final double a0 = Helper.evaluatePolynomial(SPECIFIC_HEAT_A0_COEFFICIENTS, temperature);
		final double ap = Helper.evaluatePolynomial(SPECIFIC_HEAT_AP_COEFFICIENTS, temperature);
		final double ap2 = Helper.evaluatePolynomial(SPECIFIC_HEAT_AP2_COEFFICIENTS, temperature);
		final double b0 = Helper.evaluatePolynomial(SPECIFIC_HEAT_B0_COEFFICIENTS, salinity);
		final double bt = Helper.evaluatePolynomial(SPECIFIC_HEAT_BT_COEFFICIENTS, temperature);
		final double bt2 = Helper.evaluatePolynomial(SPECIFIC_HEAT_BT2_COEFFICIENTS, temperature);
		final double bt3 = Helper.evaluatePolynomial(SPECIFIC_HEAT_BT3_COEFFICIENTS, temperature);
		return 1000. * (a0 + (ap + ap2 * pressure) * pressure)
			- (temperature + ABSOLUTE_ZERO) * (b0 + bt * temperature * salinity + bt2 * temperature * salinity * salinity
			+ bt3 * salinity * pressure);
	}

}
