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


public class Oven{

	private static final Logger LOGGER = LoggerFactory.getLogger(Oven.class);


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
	private final FirstOrderIntegrator integrator = new GraggBulirschStoerIntegrator(0.1, 1., 1.e-5, 1.e-5);
	private final BaseUnivariateSolver<UnivariateFunction> solverBakingTime = new BracketingNthOrderBrentSolver(1., 5);


	OvenType ovenType;
	/** Whether this oven has top heating */
	boolean hasTopHeating;
	/** Whether this oven has bottom heating */
	boolean hasBottomHeating;

	Double bakingTemperatureTop;
	Double bakingTemperatureBottom;


	public static Oven create(){
		return new Oven();
	}

	private Oven(){}

	public Oven withOvenType(final OvenType ovenType) throws DoughException{
		if(ovenType == null)
			throw DoughException.create("Missing oven type");

		this.ovenType = ovenType;

		return this;
	}

	public Oven withHasTopHeating(){
		this.hasTopHeating = true;

		return this;
	}

	public Oven withHasBottomHeating(){
		this.hasBottomHeating = true;

		return this;
	}

	public Oven withBakingTemperatureTop(final double bakingTemperatureTop) throws DoughException{
		if(bakingTemperatureTop <= 0.)
			throw DoughException.create("Baking top temperature too low");

		this.bakingTemperatureTop = bakingTemperatureTop;

		return this;
	}

	public Oven withBakingTemperatureBottom(final double bakingTemperatureBottom) throws DoughException{
		if(bakingTemperatureBottom <= 0.)
			throw DoughException.create("Baking bottom temperature too low");

		this.bakingTemperatureBottom = bakingTemperatureBottom;

		return this;
	}

	public void validate() throws DoughException{
		if(ovenType == null)
			throw DoughException.create("Oven type must be given");
	}



	/**
	 * @param recipe	Recipe.
	 * @param targetPizzaHeight	Desired pizza height [cm].
	 * @param bakingInstruments	Baking instruments.
	 * @return	The recipe with baking temperature and duration.
	 * @throws DoughException
	 * @throws YeastException
	 */
	public Recipe bakeRecipe(final Dough dough, final Recipe recipe, final double targetPizzaHeight,
			final BakingInstruments bakingInstruments) throws DoughException{
		validate();

		final double totalBakingPansArea = bakingInstruments.getBakingPansTotalArea();

		//TODO

		//calculate baking temperature:
		//FIXME
		final double fatDensity = 0.9175;
		final double doughVolume = recipe.doughWeight() / dough.density(recipe.getFlour(), recipe.doughWeight(), fatDensity,
			dough.ingredientsTemperature, dough.atmosphericPressure);
		//[cm]
		final double initialDoughHeight = doughVolume / totalBakingPansArea;
		//FIXME the factor accounts for water content and gases produced by levain
		final double bakingRatio = 0.405 * targetPizzaHeight / initialDoughHeight;
		//apply inverse Charles-Gay Lussac
		final double bakingTemperature = bakingRatio * (dough.ingredientsTemperature + Water.ABSOLUTE_ZERO) - Water.ABSOLUTE_ZERO;
		//TODO calculate baking temperature (must be bakingTemperature > waterBoilingTemp and bakingTemperature > maillardReactionTemperature)
		//https://www.campdenbri.co.uk/blogs/bread-dough-rise-causes.php
		final double brineBoilingTemperature = Water.boilingTemperature(recipe.getSalt() / recipe.getWater(),
			recipe.getSugar() / recipe.getWater(), dough.sugarType, dough.atmosphericPressure);
		if(bakingTemperature < brineBoilingTemperature)
			LOGGER.warn("Cannot bake at such a temperature able to generate a pizza with the desired height");
		else{
			//https://bakerpedia.com/processes/maillard-reaction/
			if(bakingTemperature < MAILLARD_REACTION_TEMPERATURE)
				LOGGER.warn("Cannot bake at such a temperature able to generate the Maillard reaction");

			recipe.withBakingTemperature(bakingTemperature);
		}
		if(hasTopHeating)
			withBakingTemperatureTop(recipe.getBakingTemperature());
		if(hasBottomHeating)
			withBakingTemperatureBottom(recipe.getBakingTemperature());
		//FIXME
		//[cm]
		final double cheeseLayerThickness = 0.2;
		//FIXME
		//[cm]
		final double tomatoLayerThickness = 0.2;
		final Duration bakingDuration = calculateBakingDuration(dough, bakingInstruments, initialDoughHeight, cheeseLayerThickness,
			tomatoLayerThickness, brineBoilingTemperature);
		recipe.withBakingDuration(bakingDuration);
		return recipe;
	}

	//TODO account for baking temperature
	// https://www.campdenbri.co.uk/blogs/bread-dough-rise-causes.php
	//initialTemperature is somewhat between params.temperature(UBound(params.temperature)) and params.ambientTemperature
	//volumeExpansion= calculateCharlesGayLussacVolumeExpansion(initialTemperature, params.bakingTemperature)
	private double calculateCharlesGayLussacVolumeExpansion(final double initialTemperature, final double finalTemperature){
		return (finalTemperature + Water.ABSOLUTE_ZERO) / (initialTemperature + Water.ABSOLUTE_ZERO);
	}

	/**
	 * @param dough	Dough data.
	 * @param bakingInstruments	Baking instruments.
	 * @param doughLayerThickness	Initial dough height [cm].
	 * @param cheeseLayerThickness	Cheese layer thickness [cm].
	 * @param tomatoLayerThickness	Tomato layer thickness [cm].
	 * @param brineBoilingTemperature	Brine (contained into the dough) boiling temperature [°C].
	 * @return
	 */
	private Duration calculateBakingDuration(final Dough dough, final BakingInstruments bakingInstruments, double doughLayerThickness,
			double cheeseLayerThickness, double tomatoLayerThickness, final double brineBoilingTemperature){
		cheeseLayerThickness /= 100.;
		tomatoLayerThickness /= 100.;
		doughLayerThickness /= 100.;
		final ThermalDescriptionODE ode = new ThermalDescriptionODE(cheeseLayerThickness, tomatoLayerThickness, doughLayerThickness,
			OvenType.FORCED_AIR, bakingTemperatureTop, bakingTemperatureBottom, dough.ingredientsTemperature,
			dough.airRelativeHumidity);

		final double bbt = (brineBoilingTemperature - dough.ingredientsTemperature) / (bakingTemperatureTop - dough.ingredientsTemperature);
		final UnivariateFunction f = time -> {
			final double[] y = ode.getInitialState();
			if(time > 0.)
				integrator.integrate(ode, 0., y, time, y);

			//https://blog.thermoworks.com/bread/homemade-bread-temperature-is-key/
			//assure each layer has at least reached the water boiling temperature
			double min = y[0];
			for(int i = 2; i < y.length; i += 2)
				min = Math.min(y[i], min);
			return min - bbt;
		};
		final double time = solverBakingTime.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_BAKING_TIME_MAX);
		return Duration.ofSeconds((long)time);
	}

}
