/**
 * Copyright (c) 2022 Mauro Trevisan
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

import io.github.mtrevisan.pizza.ingredients.Atmosphere;
import io.github.mtrevisan.pizza.ingredients.Fat;
import io.github.mtrevisan.pizza.ingredients.Flour;
import io.github.mtrevisan.pizza.ingredients.Carbohydrate;
import io.github.mtrevisan.pizza.ingredients.Water;
import io.github.mtrevisan.pizza.ingredients.Yeast;
import io.github.mtrevisan.pizza.utils.Helper;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaePedonYeast;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;


//effect of ingredients!! https://www.maltosefalcons.com/blogs/brewing-techniques-tips/yeast-propagation-and-maintenance-principles-and-practices
public final class Dough2{

	private static final Logger LOGGER = LoggerFactory.getLogger(Dough2.class);


	private static final double[] CHLORINE_DIOXIDE_COEFFICIENTS = new double[]{1., -0.17, 0.00762};

	/**
	 * @see #ATMOSPHERIC_PRESSURE_MAX
	 */
	private static final double PRESSURE_FACTOR_K = 1.46;
	/**
	 * @see #ATMOSPHERIC_PRESSURE_MAX
	 */
	private static final double PRESSURE_FACTOR_M = 2.031;
	/**
	 * Minimum inhibitory pressure [hPa].
	 *
	 * @see #PRESSURE_FACTOR_K
	 * @see #PRESSURE_FACTOR_M
	 */
	private static final double ATMOSPHERIC_PRESSURE_MAX = Math.pow(10_000., 2.) * Math.pow(1. / PRESSURE_FACTOR_K, (1. / PRESSURE_FACTOR_M));

	/** [% w/w] */
	private static final double SOLVER_YEAST_MAX = 0.2;
	private static final int SOLVER_EVALUATIONS_MAX = 100;

	//accuracy is ±0.001%
	private final BaseUnivariateSolver<UnivariateFunction> solverYeast = new BracketingNthOrderBrentSolver(0.000_01,
		5);


	private DoughCore core;


	public static Dough2 create(final DoughCore core) throws DoughException, YeastException{
		return new Dough2(core);
	}


	private Dough2(final DoughCore core) throws DoughException{
		if(core == null)
			throw DoughException.create("Core data must be provided");

		this.core = core;
	}


	public static void main(String[] args) throws DoughException, YeastException{
		DoughCore core = DoughCore.create(Yeast.create(new SaccharomycesCerevisiaePedonYeast(), Yeast.YeastType.INSTANT_DRY, 1., 0.95))
			.withFlour(Flour.create(230., 0., 0.0008, 1.3, 0., 0., 0.001))
			.addWater(0.65, Water.create(Water.PURE_WATER_CONTENT, 0.02, 0., 237., 7.9))
			.addCarbohydrate(0.004, Carbohydrate.create(Carbohydrate.CarbohydrateType.SUCROSE, 0.998, 0.0005))
			.addFat(0.021, Fat.create(Fat.FatType.OLIVE_OIL, 0.913, 0.002, 0., 0.9175))
			.addSalt(0.016)
			.withAtmosphere(Atmosphere.create(1015.6, 0.55));
		LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		LeaveningStage stage2 = LeaveningStage.create(20., Duration.ofHours(1l));
		Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(20, 15));
		Dough2 dough = Dough2.create(core);
		Recipe recipe = dough.createRecipe(procedure, 767.55, 18., 27.);

//		System.out.println("yeast = " + Helper.round(recipe.getYeast(), 5) + "%");
		//flour: 453.0 g, water: 294.5 g at 31.0 °C, sugar: 1.81 g, fat: 10.42 g, salt: 7.25 g, yeast: 0.6 g
		System.out.println(recipe);
	}


	/**
	 * Modify specific growth ratio in order to account for carbohydrate, fat, salt, water, and chlorine dioxide.
	 * <p>
	 * Yeast activity is impacted by:
	 * <ul>
	 *    <li>quantity percent of flour</li>
	 *    <li>temperature</li>
	 *    <li>hydration</li>
	 *    <li>salt</li>
	 *    <li>fat</li>
	 *    <li>carbohydrate</li>
	 *    <li>yeast age</li>
	 *    <li>dough ball size</li>
	 *    <li>gluten development</li>
	 *    <li>altitude (atmospheric pressure)</li>
	 *    <li>water chemistry (level of chlorination especially)</li>
	 *    <li>container material and thickness (conductivity if ambient and dough temperatures vary, along with heat dissipation from fermentation) (*)</li>
	 *    <li>flour chemistry (enzyme activity, damaged starch, etc.) (*)</li>
	 * </ul>
	 * </p>
	 *
	 * Yeast aging: https://onlinelibrary.wiley.com/doi/pdf/10.1002/bit.27210
	 * https://www.researchgate.net/publication/318756298_Bread_Dough_and_Baker's_Yeast_An_Uplifting_Synergy
	 *
	 * @param yeast	Yeast [% w/w].
	 * @param temperature	Temperature [°C].
	 * @return	Factor to be applied to maximum specific growth rate.
	 */
	double ingredientsFactor(final double yeast, final double temperature){
		//TODO calculate ingredientsFactor (account for water and carbohydrate at least)
//		final double kCarbohydrate = carbohydrateFactor(yeast, temperature);
////		final double kFat = fatFactor();
		final double kSalt = saltFactor(yeast);
		final double kWater = waterChlorineDioxideFactor(yeast);
////		final double kWaterFixedResidue = waterFixedResidueFactor();
		final double kHydration = kWater/* * kWaterFixedResidue*/;
		final double kPH = doughPHFactor();
		final double kAtmospherePressure = atmospherePressureFactor(core.atmosphere.pressure);
		return /*kCarbohydrate * kFat **/ kSalt * kHydration * kPH * kAtmospherePressure;
	}

	/**
	 * normally between 1.8-2.2%
	 * @see <a href="https://aip.scitation.org/doi/pdf/10.1063/5.0037822">Linda, Amalina, Umar. Measurement of oxygen consumption of Saccharomyces cerevisiae using BiochipC under influenced of sodium chloride and glucose. 2021.</a>
	 *
	 * @param yeast	Yeast [% w/w].
	 * @return	Correction factor.
	 */
	private double saltFactor(final double yeast){
		double factor = 1.;
		if(core.saltQuantity > 0.){
			///the following formula is for 1.e7 CFU/ml yeast
			final double densityFactor = yeast * (Yeast.YeastType.FY_CELL_COUNT / 1.e7) / core.totalFraction();
			//transform [% w/w] to [g / l]
			final double salt = core.saltQuantity * densityFactor * 10.;
			factor = 1. - 0.00126 * salt;
		}
		return Math.max(factor, 0.);
	}

	/**
	 * @see <a href="https://annalsmicrobiology.biomedcentral.com/track/pdf/10.1007/s13213-012-0494-8.pdf">Zhu, Chen, Yu. Fungicidal mechanism of chlorine dioxide on Saccharomyces cerevisiae. 2013.</a>
	 * @see <a href="https://academic.oup.com/mutage/article/19/2/157/1076450">Buschini, Carboni, Furlini, Poli, Rossi. Sodium hypochlorite-, chlorine dioxide- and peracetic acid-induced genotoxicity detected by Saccharomyces cerevisiae tests. 2004.</a>
	 *
	 * @param yeast	yeast [% w/w].
	 * @return	Correction factor.
	 */
	private double waterChlorineDioxideFactor(final double yeast){
		///the following formula is for 1e8 CFU/ml yeast
		final double densityFactor = yeast * (Yeast.YeastType.FY_CELL_COUNT / 1.e8) / core.totalFraction();
		final double equivalentChlorineDioxide = core.waterQuantity * core.water.chlorineDioxide * densityFactor;
		return Math.max(Helper.evaluatePolynomial(CHLORINE_DIOXIDE_COEFFICIENTS, equivalentChlorineDioxide), 0.);
	}

	/**
	 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1388350/">Rosso, Lobry, Bajard, Flandrois. Convenient model to describe the combined effects of temperature and pH on microbial growth. 1995.</a>
	 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC91662/">Mambré, Kubaczka, Chéné. Combined effects of pH and sugar on growth rate of Zygosaccharomyces rouxii, a bakery product spoilage yeast. 1999.</a>
	 * @see <a href="https://www.researchgate.net/profile/Sandra-Antonini/publication/335275152_Interaction_of_4-ethylphenol_pH_sucrose_and_ethanol_on_the_growth_and_fermentation_capacity_of_the_industrial_strain_of_Saccharomyces_cerevisiae_PE-2/links/5d5edff0299bf1b97cff2252/Interaction-of-4-ethylphenol-pH-sucrose-and-ethanol-on-the-growth-and-fermentation-capacity-of-the-industrial-strain-of-Saccharomyces-cerevisiae-PE-2.pdf>Covre, Silva, Bastos, Antonini. Interaction of 4-ethylphenol, pH, sucrose and ethanol on the growth and fermentation capacity of the industrial strain of Saccharomyces cerevisiae PE-2. 2019.</a>
	 * @see <a href="https://oatao.univ-toulouse.fr/1556/1/Serra_1556.pdf">Serra, Strehaiano, Taillandier. Influence of temperature and pH on Saccharomyces bayanus var. uvarum growth; impact of a wine yeast interspecifichy bridization on these parameters. 2005.</a>
	 * @see <a href="https://www.scielo.br/pdf/bjm/v39n2/a24.pdf">Yalcin, Ozbas. Effects of pH and temperature on growth and glycerol production kinetics of two indigenous wine strains of Saccharomyces cerevisiae from Turkey. 2008.</a>
	 * @see <a href="http://ache.org.rs/CICEQ/2010/No2/12_3141_2009.pdf">Shafaghat, Najafpour, Rezaei, Sharifzadeh. Optimal growth of Saccharomyces cerevisiae (PTCC 24860) on pretreated molasses for ethanol production: Application of response surface methodology. 2010.</a>
	 * @see <a href="https://academic.oup.com/femsyr/article/15/2/fou005/534737">Peña, Sánchez, Álvarez, Calahorra, Ramírez. Effects of high medium pH on growth, metabolism and transport in Saccharomyces cerevisiae. 2015.</a>
	 * @see <a href="https://bib.irb.hr/datoteka/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlića, Querolb, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzeviiand their interspecific hybrid. 2009.</a>
	 *
	 * Maximum specific growth rate rises until before 2.1 pH, decreases until 2.7 pH, then rises until 6 pH, then decreases again,
	 * reaching 0 at 9 pH.
	 *
	 * @return	Correction factor.
	 */
	private double doughPHFactor(){
		//usually between 6 and 6.8
		final double flourPH = 6.4;
		final double fatFactor = (core.fat.type == Fat.FatType.BUTTER? 1.: 0.);
		//6.1-6.4 for butter
		final double fatPH = 6.25;
		final double compositePH = (flourPH + core.water.pH * core.waterQuantity + fatFactor * fatPH * core.fatQuantity)
			/ (1. + core.waterQuantity + fatFactor * core.fatQuantity);

		if(compositePH < core.yeast.model.getPHMin() || compositePH > core.yeast.model.getPHMax())
			return 0.;

		final double tmp = (compositePH - core.yeast.model.getPHMin()) * (compositePH - core.yeast.model.getPHMax());
		return Math.max(tmp / (tmp - Math.pow(compositePH - core.yeast.model.getPHOpt(), 2.)), 0.);
	}

	/**
	 * @see <a href="https://www.tandfonline.com/doi/pdf/10.1271/bbb.69.1365">Arao, Hara, Suzuki, Tamura. Effect of High-Pressure Gas on io.github.mtrevisan.pizza.Yeast Growth. 2014.</a>
	 *
	 * @return	Correction factor.
	 */
	private double atmospherePressureFactor(final double atmosphericPressure){
		return (atmosphericPressure < ATMOSPHERIC_PRESSURE_MAX?
			1. - PRESSURE_FACTOR_K * Math.pow(atmosphericPressure / Math.pow(10_000., 2.), PRESSURE_FACTOR_M): 0.);
	}


	/**
	 * @param procedure	The recipe procedure.
	 * @param doughWeight	Desired dough weight [g].
	 * @param ingredientsTemperature	Temperature of ingredients [°C].
	 * @param doughTemperature	Desired dough temperature [°C].
	 * @return	The recipe.
	 */
	public Recipe createRecipe(final Procedure procedure, final double doughWeight, final Double ingredientsTemperature,
			final Double doughTemperature) throws YeastException{
		if(procedure == null)
			throw new IllegalArgumentException("Procedure must be valued");

		calculateYeast(procedure);

		final double totalFraction = core.totalFraction();
		//NOTE: too complex to extract a formula for each ingredient, it's easier to proceed by approximation
		double flourWeight = doughWeight / totalFraction;
		double waterWeight, fatWeight, saltWeight;
		double difference = 0.;
		Recipe recipe;
		do{
			//refine approximation
			flourWeight += difference * 0.6;

			fatWeight = (flourWeight * core.fatQuantity
				- (core.correctForIngredients? flourWeight * core.flour.fat: 0.)) / core.fat.fat;
			saltWeight = flourWeight * core.saltQuantity - fatWeight * core.fat.salt
				- (core.correctForIngredients? flourWeight * core.flour.salt + fatWeight * core.fat.salt: 0.);
			final double carbohydrateWeight = flourWeight * core.carbohydrateQuantity;
			waterWeight = flourWeight * core.waterQuantity
				- (core.correctForIngredients? carbohydrateWeight * core.carbohydrate.water + fatWeight * core.fat.water: 0.)
				- (core.flour.correctForHumidity? flourWeight * Flour.estimatedHumidity(core.atmosphere.relativeHumidity): 0.);
			final double yeastWeight = flourWeight * core.yeastQuantity;

			recipe = Recipe.create()
				.withFlour(flourWeight)
				.withWater(Math.max(waterWeight, 0.) / core.water.water)
				.withCarbohydrate(carbohydrateWeight / (core.carbohydrate.carbohydrate * core.carbohydrate.type.factor))
				.withFat(Math.max(fatWeight, 0.) / core.fat.fat)
				.withSalt(Math.max(saltWeight, 0.))
				.withYeast(yeastWeight / (core.yeast.yeast * core.yeast.type.factor));

			difference = doughWeight - recipe.doughWeight();
		}while(Math.abs(difference) > DoughCore.DOUGH_WEIGHT_ACCURACY);
		if(waterWeight < 0.)
			LOGGER.warn("Water is already present, excess quantity is {} ({}% w/w)", Helper.round(-waterWeight, DoughCore.WEIGHT_ACCURACY_DIGITS),
				Helper.round(-waterWeight * 100. / flourWeight, DoughCore.VOLUME_PERCENT_ACCURACY_DIGITS));
		if(fatWeight < 0.)
			LOGGER.warn("Fat is already present, excess quantity is {} ({}% w/w)", Helper.round(-fatWeight, DoughCore.WEIGHT_ACCURACY_DIGITS),
				Helper.round(-fatWeight * 100. / flourWeight, DoughCore.VOLUME_PERCENT_ACCURACY_DIGITS));
		if(saltWeight < 0.)
			LOGGER.warn("Salt is already present, excess quantity is {} ({}% w/w)", Helper.round(-saltWeight, DoughCore.WEIGHT_ACCURACY_DIGITS),
				Helper.round(-saltWeight * 100. / flourWeight, DoughCore.VOLUME_PERCENT_ACCURACY_DIGITS));

		if(doughTemperature != null && ingredientsTemperature != null){
			final double waterTemperature = recipe.calculateWaterTemperature(core.flour, core.fat.type, ingredientsTemperature, doughTemperature);
			if(waterTemperature >= core.yeast.model.getTemperatureMax())
				LOGGER.warn("Water temperature ({} °C) is greater that maximum temperature sustainable by the yeast ({} °C): be aware of thermal shock!",
					Helper.round(waterTemperature, DoughCore.TEMPERATURE_ACCURACY_DIGITS),
					Helper.round(core.yeast.model.getTemperatureMax(), DoughCore.TEMPERATURE_ACCURACY_DIGITS));

			recipe.withWaterTemperature(waterTemperature);
		}

		return recipe;
	}

	/**
	 * Find the initial yeast able to obtain a given volume expansion ratio after a series of consecutive stages at a given duration at
	 * temperature.
	 *
	 * @param procedure	Data for procedure.
	 */
	private void calculateYeast(final Procedure procedure) throws YeastException{
		try{
			final UnivariateFunction f = yeast -> volumeExpansionRatioDifference(yeast, procedure);
			core.yeastQuantity = solverYeast.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_YEAST_MAX);
		}
		catch(final NoBracketingException nbe){
			throw YeastException.create("No amount of yeast will ever be able to produce the given expansion ratio", nbe);
		}
		catch(final IllegalArgumentException iae){
			throw YeastException.create("No amount of yeast will ever be able to produce the given expansion ratio due to the adverse environment in "
				+ iae.getMessage());
		}
		catch(final TooManyEvaluationsException tmee){
			throw YeastException.create("Cannot calculate yeast quantity, try increasing maximum number of evaluations in the solver",
				tmee);
		}
	}

	//https://www.mdpi.com/2076-2607/9/1/47/htm
	//https://www.researchgate.net/publication/318756298_Bread_Dough_and_Baker's_Yeast_An_Uplifting_Synergy
	private double volumeExpansionRatioDifference(final double yeast, final Procedure procedure) throws MathIllegalArgumentException{
		//lag phase duration [hrs]
		//TODO calculate lambda
		final double lambda = lagPhaseDuration(yeast);
		//TODO calculate the factor
		final double aliveYeast = core.yeast.aliveYeast * yeast;

		//consider multiple leavening stages
		LeaveningStage stage = procedure.leaveningStages[0];
		Duration ongoingDuration = stage.duration;
		double doughVolumeExpansionRatio = doughVolumeExpansionRatio(aliveYeast, lambda, stage.temperature, ongoingDuration);
		for(int i = 1; i <= procedure.targetVolumeExpansionRatioAtLeaveningStage; i ++){
			stage = procedure.leaveningStages[i];

			final double previousExpansionRatio = doughVolumeExpansionRatio(aliveYeast, lambda, stage.temperature, ongoingDuration);
			ongoingDuration = ongoingDuration.plus(stage.duration);
			final double currentExpansionRatio = doughVolumeExpansionRatio(aliveYeast, lambda, stage.temperature, ongoingDuration);

			doughVolumeExpansionRatio += currentExpansionRatio - previousExpansionRatio;
		}

		return doughVolumeExpansionRatio - procedure.targetDoughVolumeExpansionRatio;
	}


	/**
	 * @see <a href="https://meridian.allenpress.com/jfp/article/71/7/1412/172677/Individual-Effects-of-Sodium-Potassium-Calcium-and">Bautista-Gallego, Arroyo-López, Durán-Quintana, Garrido-Fernández. Individual Effects of Sodium, Potassium, Calcium, and Magnesium Chloride Salts on Lactobacillus pentosus and Saccharomyces cerevisiae Growth. 2008.</a>
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 *
	 * @param yeast	Quantity of yeast [% w/w].
	 * @return	The estimated lag [hrs].
	 */
	private double lagPhaseDuration(final double yeast){
		///the following formula is for 2.51e7 CFU/ml yeast
		final double densityFactor = yeast * (Yeast.YeastType.FY_CELL_COUNT / 2.51e7) / core.totalFraction();
		//transform [% w/w] to [g / l]
		final double salt = core.saltQuantity * densityFactor * 10.;
		final double saltLag = Math.log(1. + Math.exp(0.494 * (salt - 84.)));

		//FIXME this formula is for 36±1 °C
		final double lag = (yeast > 0.? 0.0068 * Math.pow(yeast, -0.937): Double.POSITIVE_INFINITY);

		return lag + saltLag;
	}

	//http://arccarticles.s3.amazonaws.com/webArticle/articles/jdfhs282010.pdf
	private double doughVolumeExpansionRatio(final double yeast, final double lambda, final double temperature, final Duration duration){
		//maximum relative volume expansion ratio
		final double alpha = maximumRelativeVolumeExpansionRatio(yeast);
		final double ingredientsFactor = ingredientsFactor(yeast, temperature);

		final double volumeExpansionRatio = core.yeast.model.volumeExpansionRatio(duration, lambda, alpha, temperature, ingredientsFactor);

		//correct for yeast quantity:
		//FIXME calculate k
		//k is so that 4% yeast in flour with 1.5% sugar and 60% water at 27-30 °C for 1 hrs gives a volume expansion ratio of 220%
		//that is, k = (220% - 1) / (yeastModel.volumeExpansionRatio(1., lambda, alpha, (27. + 30.) / 2., 1.) * 0.04)
		//		final double k = 13.7;
		//170 is about 317%
		final double k = 170.;
		return 1. + k * volumeExpansionRatio * yeast;
	}

	/**
	 * Maximum relative volume expansion ratio.
	 *
	 * @param yeast	Quantity of yeast [% w/w].
	 * @return	The maximum relative volume expansion ratio (∆V / V).
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 */
	private double maximumRelativeVolumeExpansionRatio(final double yeast){
		//FIXME this formula is for 36±1 °C
		//vertex must be at 1.1%
		return (yeast < 0.011? 24_546. * (0.022 - yeast) * yeast: 2.97);
	}

}
