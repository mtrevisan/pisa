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


import io.github.mtrevisan.pizza.utils.Helper;


public class Water{

	//[°C]
	public static final double ABSOLUTE_ZERO = 273.15;

	private static final double[] BOILING_TEMPERATURE_COEFFICIENTS = new double[]{19.46, 0.36395, -1.27769e-3, 3.21349e-6, -5.12207e-9, 4.92425e-12, -2.59915e-15, 5.7739e-19};
	private static final double[] BOILING_TEMPERATURE_A_COEFFICIENTS = new double[]{17.95, 0.2823, -0.0004584};
	private static final double[] BOILING_TEMPERATURE_B_COEFFICIENTS = new double[]{6.56, 0.05267, 0.0001536};

	private static final double[] PURE_WATER_DENSITY_A_COEFFICIENTS = new double[]{0.824493, -0.0040899, 0.000076438, -0.00000082467, 0.0000000053875};
	private static final double[] PURE_WATER_DENSITY_B_COEFFICIENTS = new double[]{-0.00572466, 0.00010227, -0.0000016546};
	private static final double[] PURE_WATER_DENSITY_RHO0_COEFFICIENTS = new double[]{999.842594, 0.06793952, -0.00909529, 0.0001001685, -0.000001120083, 0.000000006536336};

	private static final double[] SECANT_BULK_MODULUS_AW_COEFFICIENTS = new double[]{3.239908, 0.00143713, 0.000116092, -0.000000577905};
	private static final double[] SECANT_BULK_MODULUS_A_COEFFICIENTS = new double[]{0.0022838, -0.000010981, -0.0000016078};
	private static final double[] SECANT_BULK_MODULUS_BW_COEFFICIENTS = new double[]{0.0000850935, -0.00000612293, 0.000000052787};
	private static final double[] SECANT_BULK_MODULUS_B_COEFFICIENTS = new double[]{-0.00000099348, 0.000000020816, 0.00000000091697};
	private static final double[] SECANT_BULK_MODULUS_KW_COEFFICIENTS = new double[]{19652.21, 148.4206, -2.327105, 0.01360477, -0.00005155288};
	private static final double[] SECANT_BULK_MODULUS_K0_1_COEFFICIENTS = new double[]{54.6746, -0.603459, 0.0109987, -0.00006167};
	private static final double[] SECANT_BULK_MODULUS_K0_2_COEFFICIENTS = new double[]{0.07944, 0.016483, -0.00053009};

	private static final double[] SPECIFIC_HEAT_A0_COEFFICIENTS = new double[]{4.193, -2.273e-4, 2.369e-6, 1.670e-10};
	private static final double[] SPECIFIC_HEAT_AP_COEFFICIENTS = new double[]{-3.978e-5, 3.229e-7, -1.073e-11};
	private static final double[] SPECIFIC_HEAT_AP2_COEFFICIENTS = new double[]{1.913e-9, -4.176e-11, 2.306e-13};
	private static final double[] SPECIFIC_HEAT_B0_COEFFICIENTS = new double[]{5.020e-3, -9.961e-6, 6.815e-8};
	private static final double[] SPECIFIC_HEAT_BT_COEFFICIENTS = new double[]{-2.605e-5, 4.585e-8, 7.642e-10};
	private static final double[] SPECIFIC_HEAT_BT2_COEFFICIENTS = new double[]{-3.649e-8, 2.496e-10};
	private static final double[] SPECIFIC_HEAT_BT3_COEFFICIENTS = new double[]{1.186e-6, 4.346e-9};


	/**
	 * @see <a href="https://journals.ametsoc.org/view/journals/bams/98/7/bams-d-16-0174.1.xml#e7">Methods for computing the boiling temperature of water at varying pressures</a>
	 *
	 * @param salinity	Salt quantity [kg/kg].
	 * @param pressure	Pressure [hPa].
	 * @return	The boiling temperature of salted water [°C].
	 */
	public double boilingTemperature(final double salinity, final double pressure){
		final double temperature = Helper.evaluatePolynomial(BOILING_TEMPERATURE_COEFFICIENTS, pressure);

		//boiling point elevation [K]
		final double a = Helper.evaluatePolynomial(BOILING_TEMPERATURE_A_COEFFICIENTS, temperature);
		final double b = Helper.evaluatePolynomial(BOILING_TEMPERATURE_B_COEFFICIENTS, temperature);
		final double saltBPE = (b + a * salinity) * salinity;

		return temperature + saltBPE;
	}


	/**
	 * Validity: -2 < temperature < 40 °C; 0 < salinity < 42 g/kg.
	 * Accuracy: ±0.01%.
	 *
	 * @see <a href="https://metgen.pagesperso-orange.fr/metrologieen19.htm">METROLOGY - ARTICLE N°18: Calculation of the density of water</a>
	 * @see <a href="http://www.csgnetwork.com/water_density_calculator.html">Ocean water density calculator</a>
	 * @see <a href="https://www.translatorscafe.com/unit-converter/en-US/calculator/salt-water-density/">Salt water density calculator</a>
	 *
	 * @param salinity	Salt quantity [%].
	 * @param temperature	Temperature [°C].
	 * @param pressure	Pressure [hPa].
	 */
	public double density(final double salinity, final double temperature, final double pressure){
		final double rho0 = pureWaterDensity(salinity, temperature);

		//account for pressure
		final double bulkModulus = secantBulkModulus(salinity, temperature, pressure);
		return rho0 / (1000. - pressure / bulkModulus);
	}

	/**
	 * @see <a href="https://www.niot.res.in/COAT/coat_pdf/CHAP%20III%20-%20Equation%20of%20State.pdf">Chapter 3 - Equation of state</a>
	 *
	 * @param salinity	Salt quantity [%].
	 * @param temperature	Temperature [°C].
	 * @return	The density [g/l].
	 */
	private double pureWaterDensity(final double salinity, final double temperature){
		final double a = Helper.evaluatePolynomial(PURE_WATER_DENSITY_A_COEFFICIENTS, temperature);
		final double b = Helper.evaluatePolynomial(PURE_WATER_DENSITY_B_COEFFICIENTS, temperature);
		final double rho0 = Helper.evaluatePolynomial(PURE_WATER_DENSITY_RHO0_COEFFICIENTS, temperature);
		return rho0 + (a + b * Math.sqrt(salinity) + 0.00048314 * salinity) * salinity;
	}

	/**
	 * @see <a href="https://www.niot.res.in/COAT/coat_pdf/CHAP%20III%20-%20Equation%20of%20State.pdf">Chapter 3 - Equation of state</a>
	 *
	 * @param salinity	Salt quantity [%].
	 * @param temperature	Temperature [°C].
	 * @param pressure	Pressure [hPa].
	 */
	private double secantBulkModulus(final double salinity, final double temperature, final double pressure){
		final double aw = Helper.evaluatePolynomial(SECANT_BULK_MODULUS_AW_COEFFICIENTS, temperature);
		final double a = aw + (Helper.evaluatePolynomial(SECANT_BULK_MODULUS_A_COEFFICIENTS, temperature)
			+ 0.000191075 * Math.sqrt(salinity)) * salinity;
		final double bw = Helper.evaluatePolynomial(SECANT_BULK_MODULUS_BW_COEFFICIENTS, temperature);
		final double b = bw + Helper.evaluatePolynomial(SECANT_BULK_MODULUS_B_COEFFICIENTS, temperature) * salinity;
		final double kw = Helper.evaluatePolynomial(SECANT_BULK_MODULUS_KW_COEFFICIENTS, temperature);
		final double k0 = kw + (Helper.evaluatePolynomial(SECANT_BULK_MODULUS_K0_1_COEFFICIENTS, temperature)
			+ Helper.evaluatePolynomial(SECANT_BULK_MODULUS_K0_2_COEFFICIENTS, temperature) * Math.sqrt(salinity)) * salinity;
		return k0 + (a + b * pressure) * pressure;
	}

	/**
	 * @see <a href="https://shodhganga.inflibnet.ac.in/bitstream/10603/149607/15/10_chapter%204.pdf">Density studies of sugar solutions</a>
	 * @see <a href="https://core.ac.uk/download/pdf/197306213.pdf">Kubota, Matsumoto, Kurisu, Sizuki, Hosaka. The equations regarding temperature and concentration of the density and viscosity of sugar, salt and skim milk solutions. 1980.</a>
	 * @see <a href="file:///C:/Users/mauro/Downloads/Year2015Vol21Issue1Pages41-52copy.pdf">Simion, Grigoras, Rosu, Gavrila. Mathematical modelling of density and viscosity of NaCl aqueous solutions. 2014.</a>
	 *
	 * @param hydration	Hydration [%].
	 * @param salinity	Salt quantity [%].
	 * @param sugar	Sugar quantity [%].
	 * @param temperature	Temperature [°C].
	 * @return	The density [g/l].
	 */
	public double brineDensity(final double pureWaterDensity, final double hydration, final double salinity, final double sugar, final double temperature){
		//molarity of sucrose: 342.29648 g/mol
		//molarity of salt: 58.44277 g/mol
		//convert salt and sugar to [g/l]
		return pureWaterDensity
			+ ((0.020391744 * salinity + 0.003443681 * sugar)
			+ (-0.000044231 * salinity + 0.0000004195 * sugar) * (temperature + ABSOLUTE_ZERO)
			) * 1000 / hydration;
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
	 * @return	The specific heat [J / (kg * K)].
	 */
	public double specificHeat(final double salinity, final double temperature, final double pressure){
		final double a0 = Helper.evaluatePolynomial(SPECIFIC_HEAT_A0_COEFFICIENTS, temperature);
		final double ap = Helper.evaluatePolynomial(SPECIFIC_HEAT_AP_COEFFICIENTS, temperature);
		final double ap2 = Helper.evaluatePolynomial(SPECIFIC_HEAT_AP2_COEFFICIENTS, temperature);
		final double b0 = Helper.evaluatePolynomial(SPECIFIC_HEAT_B0_COEFFICIENTS, salinity);
		final double bt = Helper.evaluatePolynomial(SPECIFIC_HEAT_BT_COEFFICIENTS, temperature);
		final double bt2 = Helper.evaluatePolynomial(SPECIFIC_HEAT_BT2_COEFFICIENTS, temperature);
		final double bt3 = Helper.evaluatePolynomial(SPECIFIC_HEAT_BT3_COEFFICIENTS, temperature);
		return 1000. * (a0 + (ap + ap2 * pressure) * pressure)
			- (temperature + ABSOLUTE_ZERO) * (b0 + bt * temperature * salinity + bt2 * temperature * salinity * salinity + bt3 * salinity * pressure);
	}

}
