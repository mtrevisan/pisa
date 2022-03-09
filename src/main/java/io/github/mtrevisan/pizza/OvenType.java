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


//https://www.researchgate.net/publication/7373482_Wind-chill-equivalent_temperatures_Regarding_the_impact_due_to_the_variability_of_the_environmental_convective_heat_transfer_coefficient
public enum OvenType{
	NATURAL_CONVECTION(){
		@Override
		double heatTransferCoefficient(final double temperature){
			return 8066.6 + (-76.01 + 0.19536 * temperature) * temperature;
		}
	},
	FORCED_CONVECTION(){
		@Override
		double heatTransferCoefficient(final double temperature){
			//convective air speed: 1 m/s
			return 1697.7 + (-9.66 + 0.02544 * temperature) * temperature;
		}
	};


	/** Specific gas constant for dry air [J / (kg · K)]. */
	private static final double R_DRY_AIR = 287.05;
	/** Specific gas constant for water vapor [J / (kg · K)]. */
	private static final double R_WATER_VAPOR = 461.495;

	private static final double[] AIR_VISCOSITY_COEFFICIENTS = {170.258, 0.605434, -1.33200e-3};
	private static final double[] AIR_VISCOSITY_PRESSURE_COEFFICIENTS = {-2.44358e-3, 1.17237, 0.125541};
	private static final double[] AIR_CONDUCTIVITY_COEFFICIENTS = {-3.9333e-4, 1.0184e-4, -4.8574e-8, 1.5207e-11};
	private static final double[] AIR_PRANDTL_COEFFICIENTS = {1.393e9, 322000., -1200., 1.1};


	static double calculateAirDensity(final double temperature, final double pressure, final double relativeHumidity){
		final double densityDryAir = pressure * 100. / (R_DRY_AIR * (temperature + WaterHelper.ABSOLUTE_ZERO));
		//Arden Buck equation
		//https://en.wikipedia.org/wiki/Arden_Buck_equation
		final double vaporPressureWater = 611.21 * Math.exp((18.678 - temperature / 234.5) * (temperature / (temperature + 257.14)));
		final double densityMoist = relativeHumidity * vaporPressureWater / (R_WATER_VAPOR * (temperature + WaterHelper.ABSOLUTE_ZERO));
		return densityDryAir + densityMoist;
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Air thermal conductivity [W / (m · K)].
	 */
	static double calculateAirThermalConductivity(final double temperature){
		return Helper.evaluatePolynomial(AIR_CONDUCTIVITY_COEFFICIENTS, temperature + WaterHelper.ABSOLUTE_ZERO);
	}

	/**
	 * @see <a href="https://backend.orbit.dtu.dk/ws/portalfiles/portal/117984374/PL11b.pdf">Calculation methods for the physical properties of air used in the calibration of microphones</a>
	 *
	 * @param temperature	Air temperature [°C].
	 * @return	The air specific heat [J / (kg · K)].
	 */
	static double calculateAirSpecificHeat(final double temperature){
		return 1002.5 + 275.e-6 * Math.pow(temperature + WaterHelper.ABSOLUTE_ZERO - 200., 2.);
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Heat transfer coefficient [W / (m² · K)].
	 */
	abstract double heatTransferCoefficient(final double temperature);

	/**
	 * Empirical equation that can be used for air speed from 2 to 20 m/s.
	 *
	 * @param airTemperature   temperature [°C].
	 * @param airPressure   air pressure [hPa].
	 * @param airRelativeHumidity   air relative humidity [%].
	 * @param airSpeed   air speed [m / s].
	 * @param pizzaDiameter   pizza diameter [mm].
	 * @return	Convective heat transfer coefficient [W / (m² · K)].
	 */
	static double heatTransferCoefficient(final double airTemperature, final double airPressure, final double airRelativeHumidity,
			final double airSpeed, final double pizzaDiameter){
		final double airDensity = calculateAirDensity(airTemperature, airPressure, airRelativeHumidity);

		//calculate air dynamic viscosity [N · s / m²2]
		final double airViscosity0 = Helper.evaluatePolynomial(AIR_VISCOSITY_COEFFICIENTS, airTemperature);
		//convert [hPa] to [MPa]
		final double airViscosityP = Helper.evaluatePolynomial(AIR_VISCOSITY_PRESSURE_COEFFICIENTS, airPressure / 10_000.);
		final double airViscosity = 1.e-7 * (airViscosity0 + airViscosityP);

		//calculate air thermal conductivity [W / (m · K)]
		final double airConductivity = Helper.evaluatePolynomial(AIR_CONDUCTIVITY_COEFFICIENTS, airTemperature + WaterHelper.ABSOLUTE_ZERO);

		//calculate air Prandtl number at 1000 hPa
		//specificHeat * airViscosity / airConductivity;
		final double prandtlNumber = 1.e9 / Helper.evaluatePolynomial(AIR_PRANDTL_COEFFICIENTS, airTemperature);

//		//https://backend.orbit.dtu.dk/ws/portalfiles/portal/117984374/PL11b.pdf
//		//[cal / (g · K)]
//		final double specificHeatAir = Helper.evaluatePolynomial(AIR_SPECIFIC_HEAT_COEFFICIENTS, airTemperature + ABSOLUTE_ZERO);
//		//[cal / (g · K)]
//		final double specificHeatWater = Helper.evaluatePolynomial(WATER_VAPOR_SPECIFIC_HEAT_COEFFICIENTS,
//			airTemperature + ABSOLUTE_ZERO);
//		//[J / (kg · K)]
//		final double specificHeat = WATER_SPECIFIC_HEAT
//			* (specificHeatAir + airRelativeHumidity * (WATER_AIR_MOLAR_MASS_RATIO * specificHeatWater - specificHeatAir))
//			/ (1. - (1. - WATER_AIR_MOLAR_MASS_RATIO) * airRelativeHumidity);
//		final double prandtlNumber2 = 1000. * specificHeat * airViscosity / airConductivity;

		final double reynoldsNumber = airDensity * airSpeed * pizzaDiameter / airViscosity;

		return (airConductivity / pizzaDiameter) * 0.228 * Math.pow(reynoldsNumber, 0.731) * Math.pow(prandtlNumber, 0.333);
	}

}
