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
import io.github.mtrevisan.pizza.ingredients.Sugar;
import io.github.mtrevisan.pizza.utils.Helper;
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


//effect of ingredients!! https://www.maltosefalcons.com/blogs/brewing-techniques-tips/yeast-propagation-and-maintenance-principles-and-practices
public final class DoughCore{

	private static final Logger LOGGER = LoggerFactory.getLogger(DoughCore.class);


	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_CARBON = 12.0107;
	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_HYDROGEN = 1.00784;
	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_OXYGEN = 15.9994;

	/** Molecular weight of glucose [g/mol]. */
	private static final double MOLECULAR_WEIGHT_GLUCOSE = MOLECULAR_WEIGHT_CARBON * 6. + MOLECULAR_WEIGHT_HYDROGEN * 12.
		+ MOLECULAR_WEIGHT_OXYGEN * 6.;


	/**
	 * Maximum sugar quantity [% w/w].
	 * <p>(should be 3.21 mol/l = 3.21 · MOLECULAR_WEIGHT_GLUCOSE / 10. [% w/w] = 57.82965228 (?))</p>
	 *
	 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6333755/">Stratford, Steels, Novodvorska, Archer, Avery. Extreme Osmotolerance and Halotolerance in Food-Relevant Yeasts and the Role of Glycerol-Dependent Cell Individuality. 2018.</a>
	 */
	private static final double SUGAR_MAX = 3.21 * MOLECULAR_WEIGHT_GLUCOSE / 10.;

	/** [mg/l] */
	private static final double WATER_CHLORINE_DIOXIDE_MAX = 1. / 0.0931;
	/** [mg/l] */
	private static final double WATER_FIXED_RESIDUE_MAX = 1500.;
	private static final double PURE_WATER_PH = 5.4;

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

	private static final double DOUGH_WEIGHT_ACCURACY = 0.01;

	static final int VOLUME_PERCENT_ACCURACY_DIGITS = 2;
	static final int WEIGHT_ACCURACY_DIGITS = 2;
	static final int HEAVY_WEIGHT_ACCURACY_DIGITS = 1;
	static final int TEMPERATURE_ACCURACY_DIGITS = 1;


	private Flour flour;

	/** Total water quantity w.r.t. flour [% w/w]. */
	private double waterQuantity;
	/** Chlorine dioxide in water [mg/l]. */
	private double waterChlorineDioxide;
	/**
	 * pH of water.
	 * <p>
	 * Hard water is more alkaline than soft water, and can decrease the activity of yeast.
	 * Water that is slightly acid (pH a little below 7) is preferred for bread baking.
	 * </p>
	 */
	private double waterPH = PURE_WATER_PH;
	/** Fixed residue in water [mg/l]. */
	private double waterFixedResidue;
	/** Calcium carbonate (CaCO₃) in water [mg/l] = [°F · 10] = [°I · 7] = [°dH · 5.6]. */
	private double waterCalciumCarbonate;

	/** TODO Total water quantity w.r.t. flour in milk [% w/w]. */
	private double milkWater;
	/** TODO Total fat quantity w.r.t. flour in milk [% w/w]. */
	private double milkFat;

	/** TODO Egg content w.r.t. flour [% w/w]. */
	private double egg;
	/** TODO Total water quantity w.r.t. flour in egg [% w/w]. */
	private double eggWater;
	/** TODO Total fat quantity w.r.t. flour in egg [% w/w]. */
	private double eggFat;

	/** Total sugar (glucose) quantity w.r.t. flour [% w/w]. */
	private double sugarQuantity;
	private Sugar sugar;

	/** Total fat quantity w.r.t. flour [% w/w]. */
	private double fatQuantity;
	private Fat fat;

	/** Total salt quantity w.r.t. flour [% w/w]. */
	private double saltQuantity;

	private final YeastModelAbstract yeastModel;
	/** Yeast quantity [% w/w]. */
	private double yeast;
	private YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	private double rawYeast = 1.;

	private Atmosphere atmosphere;


	/** Whether to correct for ingredients' content in fat/salt/water. */
	private boolean correctForIngredients;
	/** Whether to correct for humidity in the flour. */
	private boolean correctForFlourHumidity;


	public static DoughCore create(final YeastModelAbstract yeastModel) throws DoughException{
		return new DoughCore(yeastModel);
	}


	private DoughCore(final YeastModelAbstract yeastModel) throws DoughException{
		if(yeastModel == null)
			throw DoughException.create("A yeast model must be provided");

		this.yeastModel = yeastModel;
	}


	/**
	 * @param flour	Flour data.
	 * @return	The instance.
	 */
	public DoughCore withFlourParameters(final Flour flour) throws DoughException{
		if(flour == null)
			throw DoughException.create("Missing flour");

		this.flour = flour;

		return this;
	}


	/**
	 * @param waterQuantity	Water quantity w.r.t. flour [% w/w].
	 * @param chlorineDioxide	Chlorine dioxide in water [mg/l].
	 * @param pH	pH of water.
	 * @param fixedResidue	Fixed residue in water [mg/l].
	 * @return	This instance.
	 * @throws DoughException	If water is too low, or chlorine dioxide is too low or too high, or fixed residue is too low or too high.
	 */
	public DoughCore addWater(final double waterQuantity, final double chlorineDioxide, final double calciumCarbonate, final double pH,
			final double fixedResidue) throws DoughException{
		if(waterQuantity < 0.)
			throw DoughException.create("Hydration [% w/w] cannot be less than zero");
		if(chlorineDioxide < 0. || chlorineDioxide >= WATER_CHLORINE_DIOXIDE_MAX)
			throw DoughException.create("Chlorine dioxide [mg/l] in water must be between 0 and {} mg/l",
				Helper.round(WATER_CHLORINE_DIOXIDE_MAX, 2));
		if(calciumCarbonate < 0.)
			throw DoughException.create("Calcium carbonate in water must be non-negative");
		if(pH < 0. || pH > 14.)
			throw DoughException.create("pH of water must be between 0 and 14");
		if(fixedResidue < 0. || fixedResidue >= WATER_FIXED_RESIDUE_MAX)
			throw DoughException.create("Fixed residue [mg/l] of water must be between 0 and {} mg/l",
				Helper.round(WATER_FIXED_RESIDUE_MAX, 2));

		if(this.waterQuantity + waterQuantity > 0.){
			waterChlorineDioxide = (this.waterQuantity * waterChlorineDioxide + waterQuantity * chlorineDioxide) / (this.waterQuantity + waterQuantity);
			waterCalciumCarbonate = (this.waterQuantity * waterCalciumCarbonate + waterQuantity * calciumCarbonate) / (this.waterQuantity + waterQuantity);
			waterPH = (this.waterQuantity * waterPH + waterQuantity * pH) / (this.waterQuantity + waterQuantity);
			waterFixedResidue = (this.waterQuantity * waterFixedResidue + waterQuantity * fixedResidue) / (this.waterQuantity + waterQuantity);
		}
		this.waterQuantity += waterQuantity;

		return this;
	}


	/**
	 * @param sugarQuantity	Sugar quantity w.r.t. flour [% w/w].
	 * @param sugar	Sugar data.
	 * @return	This instance.
	 * @throws DoughException	If sugar is too low or too high.
	 */
	public DoughCore addSugar(final double sugarQuantity, final Sugar sugar) throws DoughException{
		if(sugarQuantity < 0. || sugarQuantity >= SUGAR_MAX)
			throw DoughException.create("Sugar [% w/w] must be between 0 and {} % w/w",
				Helper.round(SUGAR_MAX, VOLUME_PERCENT_ACCURACY_DIGITS));
		if(this.sugar != null)
			throw DoughException.create("Sugar was already set");

		this.sugarQuantity += sugar.type.factor * sugarQuantity * sugar.carbohydrate;
		addWater(sugarQuantity * sugar.water, 0., 0., PURE_WATER_PH, 0.);
		this.sugar = sugar;

		return this;
	}


	/**
	 * @param fatQuantity	Fat quantity w.r.t. flour [% w/w].
	 * @param fat	Fat data.
	 * @return	This instance.
	 * @throws DoughException	If fat is too low or too high.
	 */
	public DoughCore addFat(final double fatQuantity, final Fat fat) throws DoughException{
		if(fatQuantity < 0.)
			throw DoughException.create("Fat [% w/w] must be greater than or equals to 0%");
		if(this.fat != null)
			throw DoughException.create("Fat was already set");

		this.fatQuantity += fatQuantity * fat.fat;
		addWater(fatQuantity * fat.water, 0., 0., PURE_WATER_PH, 0.);
		addSalt(fatQuantity * fat.salt);
		this.fat = fat;

		return this;
	}


	/**
	 * @param saltQuantity	Salt quantity w.r.t. flour [% w/w].
	 * @return	This instance.
	 * @throws DoughException	If salt is too low or too high.
	 */
	public DoughCore addSalt(final double saltQuantity) throws DoughException{
		if(saltQuantity < 0.)
			throw DoughException.create("Salt [% w/w] must be positive");

		this.saltQuantity += saltQuantity;

		return this;
	}


	/**
	 * @param yeastType	Yeast type.
	 * @param rawYeast	Raw yeast content [% w/w].
	 * @return	The instance.
	 */
	public DoughCore withYeastParameters(final YeastType yeastType, final double rawYeast) throws DoughException{
		if(yeastType == null)
			throw DoughException.create("Missing yeast type");
		if(rawYeast <= 0. || rawYeast > 1.)
			throw DoughException.create("Raw yeast quantity must be between 0 and 1");

		this.yeastType = yeastType;
		this.rawYeast = rawYeast;

		return this;
	}


	/**
	 * @param atmosphere	Atmosphere data.
	 * @return	This instance.
	 * @throws DoughException	If pressure is negative or above maximum.
	 */
	public DoughCore withAtmosphere(final Atmosphere atmosphere) throws DoughException{
		if(atmosphere.pressure < 0. || atmosphere.pressure >= ATMOSPHERIC_PRESSURE_MAX)
			throw DoughException.create("Atmospheric pressure [hPa] must be between 0 and {} hPa",
				Helper.round(ATMOSPHERIC_PRESSURE_MAX, 1));

		this.atmosphere = atmosphere;

		return this;
	}


	public DoughCore withCorrectForIngredients(){
		correctForIngredients = true;

		return this;
	}

	public DoughCore withCorrectForFlourHumidity(){
		correctForFlourHumidity = true;

		return this;
	}


	/**
	 * Modify specific growth ratio in order to account for sugar, fat, salt, water, and chlorine dioxide.
	 * <p>
	 * Yeast activity is impacted by:
	 * <ul>
	 *    <li>quantity percent of flour</li>
	 *    <li>temperature</li>
	 *    <li>hydration</li>
	 *    <li>salt</li>
	 *    <li>fat (*)</li>
	 *    <li>sugar</li>
	 *    <li>yeast age (*)</li>
	 *    <li>dough ball size (*)</li>
	 *    <li>gluten development (*)</li>
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
		//TODO calculate ingredientsFactor (account for water and sugar at least)
//		final double kSugar = sugarFactor(yeast, temperature);
////		final double kFat = fatFactor();
//		final double kSalt = saltFactor(yeast, temperature);
//		final double kWater = waterFactor(yeast, temperature);
////		final double kWaterFixedResidue = waterFixedResidueFactor();
////		final double kHydration = kWater * kWaterFixedResidue;
		final double kPH = phFactor();
		final double kAtmosphericPressure = atmosphericPressureFactor(atmosphere.pressure);
		return /*kSugar * kFat * kSalt * kHydration **/ kPH * kAtmosphericPressure;
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
	private double phFactor(){
		//usually between 6 and 6.8
		final double flourPH = 6.4;
		final double fatFactor = (fat.type == Fat.FatType.BUTTER? 1.: 0.);
		//6.1-6.4 for butter
		final double fatPH = 6.25;
		final double compositePH = (flourPH + waterPH * waterQuantity + fatFactor * fatPH * fatQuantity) / (1. + waterQuantity + fatFactor * fatQuantity);

		if(compositePH < yeastModel.getPHMin() || compositePH > yeastModel.getPHMax())
			return 0.;

		final double tmp = (compositePH - yeastModel.getPHMin()) * (compositePH - yeastModel.getPHMax());
		return tmp / (tmp - Math.pow(compositePH - yeastModel.getPHOpt(), 2.));
	}

	/**
	 * @see <a href="https://www.tandfonline.com/doi/pdf/10.1271/bbb.69.1365">Arao, Hara, Suzuki, Tamura. Effect of High-Pressure Gas on io.github.mtrevisan.pizza.Yeast Growth. 2014.</a>
	 *
	 * @return	Correction factor.
	 */
	private double atmosphericPressureFactor(final double atmosphericPressure){
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

		final double totalFraction = totalFraction();
		//NOTE: too complex to extract a formula for each ingredient, it's easier to proceed by approximation
		double flourWeight = doughWeight / totalFraction;
		double waterWeight, fatWeight, saltWeight;
		double difference = 0.;
		Recipe recipe;
		do{
			//refine approximation
			flourWeight += difference * 0.6;

			fatWeight = (flourWeight * (this.fatQuantity * (1. - milkFat) - eggFat)
				- (correctForIngredients? flourWeight * flour.fat: 0.)) / fat.fat;
			saltWeight = flourWeight * this.saltQuantity - fatWeight * this.fat.salt
				- (correctForIngredients? flourWeight * flour.salt + fatWeight * fat.salt: 0.);
			final double sugarWeight = flourWeight * this.sugarQuantity;
			waterWeight = (flourWeight * (this.waterQuantity - eggWater)
				- (correctForIngredients? sugarWeight * sugar.water + fatWeight * fat.water: 0.)
				- (correctForFlourHumidity? flourWeight * Flour.estimatedHumidity(atmosphere.relativeHumidity): 0.))
				/ (milkWater > 0.? milkWater: 1.);
			final double yeast = flourWeight * this.yeast;

			recipe = Recipe.create()
				.withFlour(flourWeight)
				.withWater(Math.max(waterWeight, 0.))
				.withSugar(sugarWeight / (sugar.carbohydrate * sugar.type.factor))
				.withFat(Math.max(fatWeight, 0.))
				.withSalt(Math.max(saltWeight, 0.))
				.withYeast(yeast / (rawYeast * yeastType.factor));

			difference = doughWeight - recipe.doughWeight();
		}while(Math.abs(difference) > DOUGH_WEIGHT_ACCURACY);
		if(waterWeight < 0.)
			LOGGER.warn("Water is already present, excess quantity is {} ({}% w/w)", Helper.round(-waterWeight, WEIGHT_ACCURACY_DIGITS),
				Helper.round(-waterWeight * 100. / flourWeight, DoughCore.VOLUME_PERCENT_ACCURACY_DIGITS));
		if(fatWeight < 0.)
			LOGGER.warn("Fat is already present, excess quantity is {} ({}% w/w)", Helper.round(-fatWeight, WEIGHT_ACCURACY_DIGITS),
				Helper.round(-fatWeight * 100. / flourWeight, DoughCore.VOLUME_PERCENT_ACCURACY_DIGITS));
		if(saltWeight < 0.)
			LOGGER.warn("Salt is already present, excess quantity is {} ({}% w/w)", Helper.round(-saltWeight, WEIGHT_ACCURACY_DIGITS),
				Helper.round(-saltWeight * 100. / flourWeight, DoughCore.VOLUME_PERCENT_ACCURACY_DIGITS));

		if(doughTemperature != null && ingredientsTemperature != null){
			final double waterTemperature = recipe.calculateWaterTemperature(flour, fat.type, ingredientsTemperature, doughTemperature);
			if(waterTemperature >= yeastModel.getTemperatureMax())
				LOGGER.warn("Water temperature ({} °C) is greater that maximum temperature sustainable by the yeast ({} °C): be aware of thermal shock!",
					Helper.round(waterTemperature, TEMPERATURE_ACCURACY_DIGITS),
					Helper.round(yeastModel.getTemperatureMax(), TEMPERATURE_ACCURACY_DIGITS));

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
			yeast = solverYeast.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_YEAST_MAX);
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
		final double lambda = 0.5;
		//TODO calculate the factor
		final double aliveYeast = 0.95 * yeast;


		final double[] ingredientsFactors = new double[procedure.leaveningStages.length];
		for(int i = 0; i < procedure.leaveningStages.length; i ++){
			ingredientsFactors[i] = ingredientsFactor(yeast, procedure.leaveningStages[i].temperature);

			if(ingredientsFactors[i] == 0.)
				throw new IllegalArgumentException("stage " + (i + 1));
		}

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

	//http://arccarticles.s3.amazonaws.com/webArticle/articles/jdfhs282010.pdf
	private double doughVolumeExpansionRatio(final double yeast, final double lambda, final double temperature, final Duration duration){
		//maximum relative volume expansion ratio
		final double alpha = maximumRelativeVolumeExpansionRatio(yeast);
		final double ingredientsFactor = ingredientsFactor(yeast, temperature);

		final double volumeExpansionRatio = yeastModel.volumeExpansionRatio(duration, lambda, alpha, temperature, ingredientsFactor);

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


	private double totalFraction(){
		return 1. + waterQuantity + sugarQuantity + fatQuantity + saltQuantity + yeast;
	}

}
