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

import io.github.mtrevisan.pizza.bakingpans.BakingPanMaterial;
import io.github.mtrevisan.pizza.utils.Helper;

import java.time.Duration;


/*
https://www.cpp.edu/~lllee/TK3111heat.pdf pag 114 + 136 (Unsteady State Conduction!!!)
https://skill-lync.com/projects/Solving-the-2D-heat-conduction-equation-for-steady-state-and-unsteady-state-using-iterative-methods-75446
https://www.researchgate.net/publication/333582112_THE_DESIGN_OF_A_PIZZA_TOASTER
http://facstaff.cbu.edu/rprice/lectures/unsteady.html

model:
convection + radiation top
air
mozzarella
tomato
crust
(baking paper)
tray
air
convection + radiation bottom

Lumped-heat assumed!
The desired midplane temperature was 73.9 °C as set in the food industry for cooked food(8)
*/
public class Toaster{

	/** Specific gas constant for dry air [J / (kg · K)]. */
	private static final double R_DRY_AIR = 287.05;
	/** Specific gas constant for water vapor [J / (kg · K)]. */
	private static final double R_WATER_VAPOR = 461.495;

	private static final double[] AIR_SPECIFIC_HEAT_COEFFICIENTS = {0.251625, -9.2525e-5, 2.1334e-7, -1.0043e-10};
	private static final double[] WATER_VAPOR_SPECIFIC_HEAT_COEFFICIENTS = {0.452219, -1.29224e-4, 4.17008e-7, -2.00401e-10};
	/** Ratio of molar mass of water to molar mass of air. */
	private static final double WATER_AIR_MOLAR_MASS_RATIO = 18.01528 / 28.97;

	private static final double[] WATER_VAPOR_PRESSURE_COEFFICIENTS = {0.99999683, -9.0826951e-3, 7.8736169e-5, -6.1117958e-7, 4.3884187e-9, -2.9883885e-11, 2.1874425e-13, -1.7892321e-15, 1.1112018e-17, -3.0994571e-20};

	private static final double[] WATER_SPECIFIC_HEAT_COEFFICIENTS = {0.2366, 2.37e-4, -6.723e-6, 8.118e-8, -4.984e-10, 1.422e-12, -1.5612e-15};

	private static final double[] AIR_VISCOSITY_PRESSURE_COEFFICIENTS = {-2.44358e-10, 1.17237e-11, 1.25541e-16};

	private static final double[] AIR_CONDUCTIVITY_COEFFICIENTS = {-3.9333e-4, 1.0184e-4, -4.8574e-8, 1.5207e-11};

	//Stefan-Boltzmann constant [W / (m² · K⁴)]
	private static final double SIGMA = 5.670374419e-8;

	private static final double EMISSIVITY_NICHROME_WIRE = 0.87;
	private static final double EMISSIVITY_PIZZA = 0.5;

	//[°C]
	private static final double DESIRED_BAKED_DOUGH_TEMPERATURE = 73.9;


	public static void main(String[] a){
		new Toaster(
			0.002, 0.002, 0.009, 0.016,
			BakingPanMaterial.ALUMINIUM, 0.001, 0.033,
			OvenType.FORCED_CONVECTION, 760., 0.0254, 760., 0.0254,
			20.85, 1013.25, 0.5, 45.723851, 27.);
//		new Toaster(
//			0.002, 0.001, 0.015, 0.042,
//			BakingPanMaterial.ALUMINIUM, 0.001, 0.068,
//			OvenType.FORCED_CONVECTION, 220., 0.15, 220., 0.15,
//			17., 1013.25, 0.5, 45.723851, 27.);
	}

	Toaster(
		//pizza
			final double layerThicknessMozzarella, final double layerThicknessTomato, final double layerThicknessDough, final double pizzaArea,
			//pan
			final BakingPanMaterial panMaterial, final double panThickness, final double panArea,
			//oven
			final OvenType ovenType, final double bakingTemperatureTop, final double topDistance, final double bakingTemperatureBottom, final double bottomDistance,
			//ambient
			final double ambientTemperature, final double airPressure, final double airRelativeHumidity, final double latitude, final double altitude){
		final double gravity = calculateGravity(latitude, altitude);
		final double rayleighNumberTop = calculateRayleighNumber(bakingTemperatureTop, airPressure, airRelativeHumidity, topDistance, ambientTemperature, gravity);
		final double rayleighNumberBottom = calculateRayleighNumber(bakingTemperatureBottom, airPressure, airRelativeHumidity, bottomDistance, ambientTemperature, gravity);

		//top air thermal conductivity [W / (m · K)]
		final double airThermalConductivityTop = calculateAirThermalConductivity(bakingTemperatureTop);
		//convective thermal coefficient [W / (m² · K)]
		final double nusseltNumberTop = calculateNusseltNumberTop(rayleighNumberTop);
		final double h_top = airThermalConductivityTop * nusseltNumberTop / topDistance;
		//bottom air thermal conductivity [W / (m · K)]
		final double airThermalConductivityBottom = calculateAirThermalConductivity(bakingTemperatureBottom);
		//convective thermal coefficient [W / (m² · K)]
		final double nusseltNumberBottom = calculateNusseltNumberBottom(rayleighNumberBottom);
		final double h_bottom = airThermalConductivityBottom * nusseltNumberBottom / topDistance;
		//mozzarella thermal conductivity [W / (m · K)]
		final double thermalConductivityMozzarella = calculateThermalConductivity(ambientTemperature, 0.2, 0.19, 0.022, 0., 0.09, 0.579);
		//tomato thermal conductivity [W / (m · K)]
		final double thermalConductivityTomato = calculateThermalConductivity(ambientTemperature, 0.013, 0.002, 0.07, 0., 0.00011, 0.91489);
		//dough thermal conductivity [W / (m · K)]
		final double thermalConductivityDough = calculateThermalConductivity(ambientTemperature, 0.013, 0.011, 0.708, 0.019, 0.05, 0.15);

		//[K / W]
		final double thermalResistanceTopAir = topDistance / (h_top * pizzaArea);
		//[K / W]
		final double thermalResistanceMozzarella = layerThicknessMozzarella / (thermalConductivityMozzarella * pizzaArea);
		//[K / W]
		final double thermalResistanceTomato = layerThicknessTomato / (thermalConductivityTomato * pizzaArea);
		//[K / W]
		final double thermalResistanceDoughTop = (layerThicknessDough / 2.) / (thermalConductivityDough * pizzaArea);
		//[K / W]
		final double thermalResistanceBottomAir = bottomDistance / (h_bottom * pizzaArea);
		//[K / W]
		final double thermalResistancePan = panThickness / (panMaterial.thermalConductivity(25.) * panArea);
		//[K / W]
		final double thermalResistanceDoughBottom = (layerThicknessDough / 2.) / (thermalConductivityDough * pizzaArea);
		//[K / W]
		final double thermalResistanceTop = thermalResistanceTopAir + thermalResistanceMozzarella + thermalResistanceTomato
			+ thermalResistanceDoughTop;
		//[K / W]
		final double thermalResistanceBottom = thermalResistanceBottomAir + thermalResistancePan + thermalResistanceDoughBottom;

		//energy transferred by convection to the top surface of the pizza [W]
		final double energyConvectionTop = (bakingTemperatureTop - DESIRED_BAKED_DOUGH_TEMPERATURE) / thermalResistanceTop;
		//energy transferred by convection to the bottom surface of the tray [W]
		final double energyConvectionBottom = (bakingTemperatureBottom - DESIRED_BAKED_DOUGH_TEMPERATURE) / thermalResistanceBottom;


		final double viewFactor12 = 0.87;
		//proportion of the radiation which leaves surface 1 that strikes surface 2
		final double factorTop = calculateRadiationFactor(pizzaArea, EMISSIVITY_PIZZA, viewFactor12);
		//energy transferred by radiation to the top surface of the pizza [W]
		final double energyRadiationTop = factorTop * SIGMA * (Math.pow(bakingTemperatureTop, 4.) - Math.pow(ambientTemperature, 4.));
		final double factorBottom = calculateRadiationFactor(pizzaArea, panMaterial.emissivity, viewFactor12);
		//energy transferred by radiation to the bottom surface of the tray [W]
		final double energyRadiationBottom = factorBottom * SIGMA * (Math.pow(bakingTemperatureBottom, 4.) - Math.pow(ambientTemperature, 4.));

		final double totalEnergyTop = energyConvectionTop + energyRadiationTop;
		final double totalEnergyBottom = energyConvectionBottom + energyRadiationBottom;


		//Biot number represents the ratio of heat transfer resistance in the interior of the system (L / k in Bi = h · L / k) to the
		//resistance between the surroundings and the system surface (1 / h).
		//Therefore, small Bi represents the case were the surface film impedes heat transport and large Bi the case where conduction through
		//and out of the solid is the limiting factor.
		final double biotNumberDough = h_bottom * (layerThicknessDough / 2.) / thermalConductivityDough;
		//NOTE: Biot number should be less than about 0.1 to consider lumped-heat capacity calculations...

		final double xiDough = 0.5553;
		final double cDough = 1.0511;
		final double theta = (DESIRED_BAKED_DOUGH_TEMPERATURE - bakingTemperatureTop) / (ambientTemperature - bakingTemperatureTop);
		final double fourierNumberDough = Math.log(theta / cDough) / -Math.pow(xiDough, 2.);
		//thermal diffusivity = thermalConductivity / (density · specificHeat) [m² / s]
		final double alpha2 = 1.3e-7;
		final Duration tDough = Duration.ofSeconds((long)(fourierNumberDough * Math.pow(layerThicknessDough, 2.) / alpha2));

		System.out.println(tDough);
	}

	private double calculateRadiationFactor(double area, final double emissivity, final double viewFactor){
		return 1. / ((1. - EMISSIVITY_NICHROME_WIRE) / (EMISSIVITY_NICHROME_WIRE * area) + 1. / (area * viewFactor)
			+ (1. - emissivity) / (emissivity * area));
	}

	private double calculateNusseltNumberTop(final double rayleighNumber){
		return (rayleighNumber <= 1.e7?
			//10^4 <= Ra <= 10^7, Pr >= 0.7
			0.52 * Math.pow(rayleighNumber, 0.2):
			//10^7 <= Ra <= 10^11
			0.15 * Math.pow(rayleighNumber, 1. / 3.));
	}

	private double calculateNusseltNumberBottom(final double rayleighNumber){
		return 0.54 * Math.pow(rayleighNumber, 0.25);
	}

	/**
	 * @see <a href="https://en.wikipedia.org/wiki/Gravity_of_Earth">Gravity of Earth</a>
	 *
	 * @param latitude	Latitude [°].
	 * @param altitude	Altitude [m].
	 * @return	The gravitational acceleration [m / s²].
	 */
	private double calculateGravity(final double latitude, final double altitude){
		final double sinLat = Math.sin(Math.toRadians(latitude));
		final double sinLat2 = sinLat * sinLat;
		final double g0 = 9.7803253359 * (1. + 0.001931850400 * sinLat2) / Math.sqrt(1. - 0.006694384442 * sinLat2);
		return g0 - 3.086e-6 * altitude;
	}

	//https://www3.nd.edu/~sst/teaching/AME60634/lectures/AME60634_F13_lecture25.pdf
	private double calculateRayleighNumber(final double temperature, final double pressure, final double relativeHumidity,
			final double distanceFromHeatSource, final double initialTemperature, final double gravity){
		//thermal expansion coefficient [K^-1]
		final double thermalExpansion = calculateAirThermalExpansion(temperature, relativeHumidity);
		final double density = calculateAirDensity(temperature, pressure, relativeHumidity);
		final double kinematicViscosity = calculateKinematicViscosity(temperature, pressure, density);
		final double thermalConductivity = calculateAirThermalConductivity(temperature);
		final double specificHeat = calculateAirSpecificHeat(temperature);
		final double thermalDiffusivity = calculateThermalDiffusivity(thermalConductivity, specificHeat, density);
		//FIXME this is only for natural convection!
		return gravity * thermalExpansion * (temperature - initialTemperature) * Math.pow(distanceFromHeatSource, 3.)
			/ (kinematicViscosity * thermalDiffusivity);
	}

	//https://backend.orbit.dtu.dk/ws/portalfiles/portal/117984374/PL11b.pdf
	//cds.cern.ch/record/732229/files/0404117.pdf?version=2
	private double calculateAirThermalExpansion(final double temperature, final double relativeHumidity){
		//[cal / (g · K)]
		final double specificHeatAir = Helper.evaluatePolynomial(AIR_SPECIFIC_HEAT_COEFFICIENTS, temperature + WaterHelper.ABSOLUTE_ZERO);
		//[cal / (g · K)]
		final double specificHeatWaterVapor = Helper.evaluatePolynomial(WATER_VAPOR_SPECIFIC_HEAT_COEFFICIENTS,
			temperature + WaterHelper.ABSOLUTE_ZERO);
		//[J / (kg · K)]
		return calculateWaterSpecificHeat(temperature)
			* (specificHeatAir + relativeHumidity * (WATER_AIR_MOLAR_MASS_RATIO * specificHeatWaterVapor - specificHeatAir))
			/ (1. - (1. - WATER_AIR_MOLAR_MASS_RATIO) * relativeHumidity);
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Specific heat of water [J / (kg · K)].
	 */
	private double calculateWaterSpecificHeat(final double temperature){
		return 1. / Helper.evaluatePolynomial(WATER_SPECIFIC_HEAT_COEFFICIENTS, temperature);
	}

	private double calculateAirDensity(final double temperature, final double pressure, final double relativeHumidity){
		final double densityDryAir = pressure * 100. / (R_DRY_AIR * (temperature + WaterHelper.ABSOLUTE_ZERO));
		final double vaporPressureWater = 6.1078 / Math.pow(Helper.evaluatePolynomial(WATER_VAPOR_PRESSURE_COEFFICIENTS, temperature), 8.);
		final double densityMoist = relativeHumidity * vaporPressureWater / (R_WATER_VAPOR * (temperature + WaterHelper.ABSOLUTE_ZERO));
		return densityDryAir + densityMoist;
	}

	/**
	 * @see <a href="https://www.govinfo.gov/content/pkg/GOVPUB-C13-1e519f3df711118b18efd158bf34023b/pdf/GOVPUB-C13-1e519f3df711118b18efd158bf34023b.pdf">Interpolation formulas for viscosity of six gases: air, nitrogen, carbon dioxide, helium, argon, and oxygen</a>
	 *
	 * @param temperature	Temperature [°C].
	 * @param density	Air density [kg / m³].
	 * @return	The kinematic viscosity [m² / s].
	 */
	private double calculateKinematicViscosity(final double temperature, final double pressure, final double density){
		//Sutherland equation
		final double temp = temperature + WaterHelper.ABSOLUTE_ZERO;
		final double dynamicViscosity0 = 1.458e-6 * temp * Math.sqrt(temp) / (temp + 110.4);
		final double dynamicViscosityP = Helper.evaluatePolynomial(AIR_VISCOSITY_PRESSURE_COEFFICIENTS, pressure);
		return (dynamicViscosity0 + dynamicViscosityP) / density;
	}

	private double calculateAirThermalConductivity(final double temperature){
		return Helper.evaluatePolynomial(AIR_CONDUCTIVITY_COEFFICIENTS, temperature + WaterHelper.ABSOLUTE_ZERO);
	}

	private double calculateThermalConductivity(final double temperature, final double protein, final double fat, final double carbohydrate,
		final double fiber, final double ash, final double water){
		final double proteinFactor = 0.17881 + (0.0011958 - 2.7178e-6 * temperature) * temperature;
		final double fatFactor = 0.18071 + (-2.7604e-4 - 1.7749e-7 * temperature) * temperature;
		final double carbohydrateFactor = 0.20141 + (0.0013874 - 4.3312e-6 * temperature) * temperature;
		final double fiberFactor = 0.18331 + (0.0012497 - 3.1683e-6 * temperature) * temperature;
		final double ashFactor = 0.32962 + (0.0014011 - 2.9069e-6 * temperature) * temperature;
		final double waterFactor = 0.57109 + (0.0017625 - 6.7036e-6 * temperature) * temperature;
		return proteinFactor * protein
			+ fatFactor * fat
			+ carbohydrateFactor * carbohydrate
			+ fiberFactor * fiber
			+ ashFactor * ash
			+ waterFactor * water;
	}

	/**
	 * @see <a href="https://backend.orbit.dtu.dk/ws/portalfiles/portal/117984374/PL11b.pdf">Calculation methods for the physical properties of air used in the calibration of microphones</a>
	 *
	 * @param temperature	Air temperature [°C].
	 * @return	The air specific heat [J / (kg · K)].
	 */
	private double calculateAirSpecificHeat(final double temperature){
		return 1002.5 + 275.e-6 * Math.pow(temperature + WaterHelper.ABSOLUTE_ZERO - 200., 2.);
	}

	private double calculateThermalDiffusivity(final double thermalConductivity, final double specificHeat, final double density){
		return thermalConductivity / (specificHeat * density);
	}

}
