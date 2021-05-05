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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


public final class Oven{

	private static final Logger LOGGER = LoggerFactory.getLogger(Oven.class);

	//[°C]
	private static final double DESIRED_BAKED_DOUGH_TEMPERATURE = 73.9;


	/**
	 * [s]
	 *
	 * @see #calculateBakingDuration(Dough, BakingInstruments, double, double, double, double)
	 */
	private static final double SOLVER_BAKING_TIME_MAX = 1800.;
	private static final int SOLVER_EVALUATIONS_MAX = 100;

	//https://www.oreilly.com/library/view/cooking-for-geeks/9781449389543/ch04.html
	private static final double MAILLARD_REACTION_TEMPERATURE = 155.;


	//accuracy is ±1 s
	private final FirstOrderIntegrator integrator = new GraggBulirschStoerIntegrator(0.003, 1., 1.e-5, 1.e-5);
	private final BaseUnivariateSolver<UnivariateFunction> solverBakingTime = new BracketingNthOrderBrentSolver(0.1, 5);


	OvenType ovenType;

	/** [m] */
	double distanceHeaterTop;
	/** [°C] */
	double bakingTemperatureTop;
	/** [m] */
	double distanceHeaterBottom;
	/** [°C] */
	double bakingTemperatureBottom;


	public static Oven create(final OvenType ovenType) throws OvenException{
		return new Oven(ovenType);
	}

	private Oven(final OvenType ovenType) throws OvenException{
		if(ovenType == null)
			throw OvenException.create("Missing oven type");

		this.ovenType = ovenType;
	}

	public final Oven withDistanceHeaterTop(final double distanceHeater) throws OvenException{
		if(distanceHeater <= 0.)
			throw OvenException.create("Top heater distance from the tray cannot be non-positive");

		distanceHeaterTop = distanceHeater;

		return this;
	}

	public final Oven withDistanceHeaterBottom(final double distanceHeater) throws OvenException{
		if(distanceHeater <= 0.)
			throw OvenException.create("Bottom heater distance from the tray cannot be non-positive");

		distanceHeaterBottom = distanceHeater;

		return this;
	}

	public final void validate() throws OvenException{
		if(ovenType == null)
			throw OvenException.create("Oven type must be given");
		if(distanceHeaterTop == 0. && distanceHeaterBottom == 0.)
			throw OvenException.create("Oven must have at least an heater component");
	}



	/**
	 * @param recipe	Recipe.
	 * @param targetPizzaHeight	Desired pizza height [cm].
	 * @param bakingInstruments	Baking instruments.
	 * @return	The baking instructions.
	 * @throws OvenException	If validation fails.
	 */
	public final BakingInstructions bakeRecipe(final Dough dough, final Recipe recipe, final double targetPizzaHeight,
			final BakingInstruments bakingInstruments) throws OvenException{
		validate();
		bakingInstruments.validate();

		//FIXME
		final double densityFat = 0.9175;
		//FIXME
		final double densityTomato = 1.06;
		//FIXME
		final double densityMozzarella = 1.029;

		final double densityDough = recipe.density(densityFat, dough.ingredientsTemperature, dough.atmosphericPressure);
		final double totalBakingPansArea = bakingInstruments.getBakingPansTotalArea();
		//[cm]
		final double initialLayerThicknessDough = recipe.doughWeight() / (densityDough * totalBakingPansArea);

		final double bakingRatio = targetPizzaHeight / initialLayerThicknessDough;
		final double bakingTemperature = calculateBakingTemperature(dough, bakingRatio);
		if(bakingTemperature < DESIRED_BAKED_DOUGH_TEMPERATURE)
			throw OvenException.create("Cannot bake at such a temperature able to generate a pizza with the desired height");
		//https://bakerpedia.com/processes/maillard-reaction/
		if(bakingTemperature < MAILLARD_REACTION_TEMPERATURE)
			LOGGER.warn("Cannot bake at such a temperature able to generate the Maillard reaction");

		if(distanceHeaterTop > 0.)
			bakingTemperatureTop = bakingTemperature;
		if(distanceHeaterBottom > 0.)
			bakingTemperatureBottom = bakingTemperature;

		//[g]
		final double seasoningOregano = totalBakingPansArea / 1400.;
		//[g]
		final double seasoningMozzarella = totalBakingPansArea / 2.45;
		//[g]
		final double seasoningTomato = totalBakingPansArea / 4.15;

		//[cm]
		final double layerThicknessMozzarella = seasoningMozzarella / (densityMozzarella * totalBakingPansArea);
		//[cm]
		final double layerThicknessTomato = seasoningTomato / (densityTomato * totalBakingPansArea);
		final Duration bakingDuration = calculateBakingDuration(dough, bakingInstruments, initialLayerThicknessDough,
			layerThicknessMozzarella, layerThicknessTomato, DESIRED_BAKED_DOUGH_TEMPERATURE);

		return BakingInstructions.create()
			.withBakingTemperature(bakingTemperature)
			.withBakingDuration(bakingDuration);
	}

	private double calculateBakingTemperature(final Dough dough, final double bakingRatio){
		//apply inverse Charles-Gay Lussac
		//FIXME the factor accounts for water content and gases produced by levain
		//https://www.campdenbri.co.uk/blogs/bread-dough-rise-causes.php
		return 0.4048 * bakingRatio * (dough.ingredientsTemperature + Water.ABSOLUTE_ZERO) - Water.ABSOLUTE_ZERO;
	}

	/**
	 * @param dough	Dough data.
	 * @param bakingInstruments	Baking instruments.
	 * @param layerThicknessDough	Initial dough height [cm].
	 * @param layerThicknessMozzarella	Cheese layer thickness [cm].
	 * @param layerThicknessTomato	Tomato layer thickness [cm].
	 * @param desiredBakedDoughTemperature	Brine (contained into the dough) boiling temperature [°C].
	 * @return	Baking duration.
	 */
	private Duration calculateBakingDuration(final Dough dough, final BakingInstruments bakingInstruments, double layerThicknessDough,
			double layerThicknessMozzarella, double layerThicknessTomato, final double desiredBakedDoughTemperature){
		layerThicknessMozzarella /= 100.;
		layerThicknessTomato /= 100.;
		layerThicknessDough /= 100.;
		final ThermalDescriptionODE ode = new ThermalDescriptionODE(layerThicknessMozzarella, layerThicknessTomato, layerThicknessDough,
			OvenType.FORCED_CONVECTION, bakingTemperatureTop, distanceHeaterTop, bakingTemperatureBottom, distanceHeaterBottom,
			dough.ingredientsTemperature, dough.atmosphericPressure, dough.airRelativeHumidity, bakingInstruments.bakingPans[0]);

java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
for(int t = 20; t <= 2000; t += 40){
	final double[] y = ode.getInitialState();
	integrator.integrate(ode, 0., y, t, y);
	System.out.println(
		//pan
		df.format(y[34])
		//pan-dough
		+ "\t" + df.format(y[32])
		//dough
		+ "\t" + df.format(y[30]) + "\t" + df.format(y[28]) + "\t" + df.format(y[26]) + "\t" + df.format(y[24]) + "\t" + df.format(y[22]) + "\t" + df.format(y[20])
		//dough-tomato
		+ "\t" + df.format(y[18])
		//tomato
		+ "\t" + df.format(y[16]) + "\t" + df.format(y[14]) + "\t" + df.format(y[12])
		//tomato-mozzarella
		+ "\t" + df.format(y[10])
		//mozzarella
		+ "\t" + df.format(y[8]) + "\t" + df.format(y[6]) + "\t" + df.format(y[4]) + "\t" + df.format(y[2])
		//top
		+ "\t" + df.format(y[0])
		);
}

		final double dbdt = calculateFourierTemperature(desiredBakedDoughTemperature, dough.ingredientsTemperature, bakingTemperatureTop);
		final UnivariateFunction f = time -> {
			final double[] y = ode.getInitialState();
			if(time > 0.)
				integrator.integrate(ode, 0., y, time, y);

			//https://blog.thermoworks.com/bread/homemade-bread-temperature-is-key/
			double min = y[0];
			for(int i = 2; i < y.length; i += 2)
				min = Math.min(y[i], min);
			return min - dbdt;
		};
		final double time = solverBakingTime.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_BAKING_TIME_MAX);
		return Duration.ofSeconds((long)time);
	}

	private double calculateFourierTemperature(final double temperature, final double initialTemperature, final double finalTemperature){
		return (temperature - initialTemperature) / (finalTemperature - initialTemperature);
	}

}
