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

import java.time.Duration;


/*
https://www.cpp.edu/~lllee/TK3111heat.pdf pag 114 + 136 (Unsteady State Conduction!!!)
https://skill-lync.com/projects/Solving-the-2D-heat-conduction-equation-for-steady-state-and-unsteady-state-using-iterative-methods-75446
https://www.researchgate.net/publication/333582112_THE_DESIGN_OF_A_PIZZA_TOASTER
http://facstaff.cbu.edu/rprice/lectures/unsteady.html

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
*/
public class Toaster{

	//Stefan-Boltzmann constant [W / (m^2 * K^4)]
	private static final double SIGMA = 5.670374419e-8;


	public static void main(String[] a){
		new Toaster(
			0.002, 0.002, 0.009, 0.016,
			BakingPanMaterial.ALUMINIUM, 0.001, 0.033,
			OvenType.FORCED_CONVECTION, 760., 0.0254, 760., 0.0254,
			21.1, 0.5);
	}

	Toaster(
				//pizza
				final double cheeseLayerThickness, final double tomatoLayerThickness, final double doughLayerThickness, final double pizzaArea,
				//pan
				final BakingPanMaterial panMaterial, final double panThickness, final double panArea,
				//oven
				final OvenType ovenType, final double bakingTemperatureTop, final double topDistance, final double bakingTemperatureBottom, final double bottomDistance,
				//ambient
				final double ambientTemperature, final double ambientHumidityRatio){
		//thermal expansion coefficient [K^-1]
		final double thermalExpansion = 1.55e-3;
		//[m / s^2]
		final double gravity = 9.807;
		//kinematic viscosity [m^2 / s]
		final double nu = 7.64e-5;
		//diffusivity [m^2 / s]
		final double alphaAir = 1.09e-4;
		final double rayleighNumberTop = calculateRayleighNumber(bakingTemperatureTop, topDistance, ambientTemperature,
			thermalExpansion, gravity, nu, alphaAir);
		final double rayleighNumberBottom = calculateRayleighNumber(bakingTemperatureBottom, bottomDistance, ambientTemperature,
			thermalExpansion, gravity, nu, alphaAir);

		//[W / (m * K)]
		final double airThermalConductivity = 5.49e-2;
		final double nusseltNumberTop = (rayleighNumberTop <= 1.e7?
			//10^4 <= Ra <= 10^7, Pr >= 0.7
			0.52 * Math.pow(rayleighNumberTop, 0.2):
			//10^7 <= Ra <= 10^11
			0.15 * Math.pow(rayleighNumberTop, 1. / 3.));
		//10^4 <= Ra <= 10^9, Pr >= 0.7
		final double nusseltNumberBottom = 0.54 * Math.pow(rayleighNumberBottom, 0.25);
		//convective thermal coefficient [W / (m^2 * K)]
		final double h_top = airThermalConductivity * nusseltNumberTop / topDistance;
		//convective thermal coefficient [W / (m^2 * K)]
		final double h_bottom = airThermalConductivity * nusseltNumberBottom / topDistance;
		//cheese thermal conductivity [W / (m * K)]
		final double thermalConductivityCheese = 0.384;
		//tomato thermal conductivity [W / (m * K)]
		final double thermalConductivityTomato = 0.546;
		//dough thermal conductivity [W / (m * K)]
		final double thermalConductivityDough = 0.262;
		//[K / W]
		final double thermalResistanceTopAir = topDistance / (h_top * pizzaArea);
		//[K / W]
		final double thermalResistanceCheese = cheeseLayerThickness / (thermalConductivityCheese * pizzaArea);
		//[K / W]
		final double thermalResistanceTomato = tomatoLayerThickness / (thermalConductivityTomato * pizzaArea);
		//[K / W]
		final double thermalResistanceDoughTop = (doughLayerThickness / 2.) / (thermalConductivityDough * pizzaArea);
		//[K / W]
		final double thermalResistanceBottomAir = bottomDistance / (h_bottom * pizzaArea);
		//[K / W]
		final double thermalResistancePan = panThickness / (panMaterial.thermalConductivity * panArea);
		//[K / W]
		final double thermalResistanceDoughBottom = (doughLayerThickness / 2.) / (thermalConductivityDough * pizzaArea);
		//[K / W]
		final double thermalResistanceTop = thermalResistanceTopAir + thermalResistanceCheese + thermalResistanceTomato
			+ thermalResistanceDoughTop;
		//[K / W]
		final double thermalResistanceBottom = thermalResistanceBottomAir + thermalResistancePan + thermalResistanceDoughBottom;

		//[°C]
		final double desiredInnerTemperature = 73.9;
		//energy required to bring the dough to 73.9 °C by convection [W]
		final double energyTop = (bakingTemperatureTop - desiredInnerTemperature) / thermalResistanceTop;
		final double energyBottom = (bakingTemperatureBottom - desiredInnerTemperature) / thermalResistanceBottom;


		//proportion of the radiation which leaves surface 1 that strikes surface 2
		final double viewFactor12 = 0.87;
		final double emissivityNichromeWire = 0.87;
		final double emissivityPizza = 0.5;
		double factor = 1. / ((1. - emissivityNichromeWire) / (emissivityNichromeWire * pizzaArea) + 1. / (pizzaArea * viewFactor12)
			+ (1. - emissivityPizza) / (emissivityPizza * pizzaArea));
		//energy transferred by radiation to the top surface [W]
		final double energy12Top = factor * SIGMA * (Math.pow(bakingTemperatureTop, 4.) - Math.pow(ambientTemperature, 4.));
		final double emissivityAluminumAlloy = 0.8;
		factor = 1. / ((1. - emissivityNichromeWire) / (emissivityNichromeWire * pizzaArea) + 1. / (pizzaArea * viewFactor12)
			+ (1. - emissivityAluminumAlloy) / (emissivityAluminumAlloy * pizzaArea));
		//energy transferred by radiation to the bottom surface [W]
		final double energy12Bottom = factor * SIGMA * (Math.pow(bakingTemperatureTop, 4.) - Math.pow(ambientTemperature, 4.));

		//Biot number represents the ratio of heat transfer resistance in the interior of the system (L / k in Bi = h * L / k) to the
		//resistance between the surroundings and the system surface (1 / h).
		//Therefore, small Bi represents the case were the surface film impedes heat transport and large Bi the case where conduction through
		//and out of the solid is the limiting factor.
		final double biotNumberDough = h_bottom * (doughLayerThickness / 2.) / thermalConductivityDough;
		//NOTE: Biot number should be less than about 0.1 to consider lumped-heat capacity calculations...

		final double xiDough = 0.5553;
		final double cDough = 1.0511;
		final double theta = (desiredInnerTemperature - bakingTemperatureTop) / (ambientTemperature - bakingTemperatureTop);
		final double fourierNumberDough = Math.log(theta / cDough) / -Math.pow(xiDough, 2.);
		//thermal diffusivity = thermalConductivity / (density * specificHeat) [m^2 / s]
		final double alpha2 = 1.3e-7;
		final Duration tDough = Duration.ofSeconds((long)(fourierNumberDough * Math.pow(doughLayerThickness, 2.) / alpha2));

		System.out.println(tDough);
	}

	private double calculateRayleighNumber(final double bakingTemperatureTop, final double topDistance, final double ambientTemperature,
			final double thermalExpansionCoeff, final double gravity, final double nu, final double alpha){
		return thermalExpansionCoeff * gravity * (bakingTemperatureTop - ambientTemperature) * Math.pow(topDistance, 3.) / (nu * alpha);
	}

}
