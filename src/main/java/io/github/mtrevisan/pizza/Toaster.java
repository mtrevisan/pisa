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


/*
https://www.cpp.edu/~lllee/TK3111heat.pdf pag 114 + 136 (Unsteady State Conduction!!!)
https://skill-lync.com/projects/Solving-the-2D-heat-conduction-equation-for-steady-state-and-unsteady-state-using-iterative-methods-75446
https://www.researchgate.net/publication/333582112_THE_DESIGN_OF_A_PIZZA_TOASTER

convection top
air
cheese
tomato
crust
tray
air
convection bottom

Lumped-heat assumed!
The desired midplane temperature was 73.9 °C as set in the food industry for cooked food(8)

heat transfer - convection:
qtop = DT / Rtop = 80.734 W
qbottom = DT / Rbottom = 200.916 W
qtotal = qtop + qbottom = 281.65 W
Ti = 21.1 °C
DT = Tinf - Tmid = 760 - 74 = 686.111 K

heat transfer - radiation:
eps1 = 0.87, eps2 = 0.5, F12 = 0.8
qtop = sigma * (T1^4 - T2^4) / ((1 - eps1) / (eps1 * A) + 1 / (A * F12) + (1 - eps2) / (eps2 * A)) = 433.71 W
qbottom = same (eps2 = 0.8) = 630.92 W
qtotal = qtop + qbottom = 1064.6 W
*/
public class Toaster{

	Toaster(
				//pizza
				final double cheeseLayerThickness, final double tomatoLayerThickness, final double doughLayerThickness, final double pizzaArea,
				//pan
				final BakingPanMaterial panMaterial, final double panThickness, final double panArea,
				//oven
				final OvenType ovenType, final double bakingTemperatureTop, final double topDistance, final double bakingTemperatureBottom, final double bottomDistance,
				//ambient
				final double ambientTemperature, final double ambientHumidityRatio){
		//[K^-1]
		final double thermalExpansionCoeff = 1.55e-3;
		//[m / s^2]
		final double gravity = 9.807;
		//kinematic viscosity [m^2 / s]
		final double nu = 7.64e-5;
		//diffusivity [m^2 / s]
		final double alpha = 1.09e-4;
		final double Ra_top = calculateRayleighNumber(bakingTemperatureTop, topDistance, ambientTemperature, thermalExpansionCoeff,
			gravity, nu, alpha);
		final double Ra_bottom = calculateRayleighNumber(bakingTemperatureBottom, bottomDistance, ambientTemperature, thermalExpansionCoeff,
			gravity, nu, alpha);

		//[W / (m * K)]
		final double airThermalConductivity = 5.49e-2;
		//Nusselt number (10^4 <= Ra <= 10^7, Pr >= 0.7, or 10^7 <= Ra <= 10^11)
		final double nusseltTop = (Ra_top <= 1.e7? 0.52 * Math.pow(Ra_top, 0.2): 0.15 * Math.pow(Ra_top, 1. / 3.));
		//Nusselt number (10^4 <= Ra <= 10^9, Pr >= 0.7)
		final double nusseltBottom = 0.54 * Math.pow(Ra_bottom, 0.25);
		//convective thermal coefficient [W / (m^2 * K)]
		final double h_top = airThermalConductivity * nusseltTop / topDistance;
		//convective thermal coefficient [W / (m^2 * K)]
		final double h_bottom = airThermalConductivity * nusseltBottom / topDistance;
		//cheese thermal conductivity [W / (m * K)]
		final double thermalConductivityCheese = 0.384;
		//tomato thermal conductivity [W / (m * K)]
		final double thermalConductivityTomato = 0.546;
		//dough thermal conductivity [W / (m * K)]
		final double thermalConductivityDough = 0.262;
		//[K / W]
		final double R_top_air = 1. / (h_top * pizzaArea);
		//[K / W]
		final double R_cheese = cheeseLayerThickness / (thermalConductivityCheese * pizzaArea);
		//[K / W]
		final double R_tomato = tomatoLayerThickness / (thermalConductivityTomato * pizzaArea);
		//[K / W]
		final double R_dough_top = (doughLayerThickness / 2.) / (thermalConductivityDough * pizzaArea);
		//[K / W]
		final double R_bottom_air = 1. / (h_bottom * pizzaArea);
		//[K / W]
		final double R_pan = panThickness / (panMaterial.thermalConductivity * panArea);
		//[K / W]
		final double R_dough_bottom = (doughLayerThickness / 2.) / (thermalConductivityDough * pizzaArea);
		//[K / W]
		final double R_top = R_top_air + R_cheese + R_tomato + R_dough_top;
		//[K / W]
		final double R_bottom = R_bottom_air + R_pan + R_dough_bottom;

		//FIXME what if bakingTemperatureTop != bakingTemperatureBottom?
		//energy required to bring the dough to 73.9 °C [W]
		final double qx = (bakingTemperatureTop - 73.9) / (R_top + R_bottom);

		//TODO
	}

	private double calculateRayleighNumber(final double bakingTemperatureTop, final double topDistance, final double ambientTemperature,
			final double thermalExpansionCoeff, final double gravity, final double nu, final double alpha){
		return thermalExpansionCoeff * gravity * (bakingTemperatureTop - ambientTemperature) * Math.pow(topDistance, 3.) / (nu * alpha);
	}

}
