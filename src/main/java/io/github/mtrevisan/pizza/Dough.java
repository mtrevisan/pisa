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
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;


public final class Dough{

	private static final Logger LOGGER = LoggerFactory.getLogger(Dough.class);


	/** Standard ambient temperature [°C]. */
	private static final double STANDARD_AMBIENT_TEMPERATURE = 25.;
	/** Standard ambient pressure [hPa]. */
	static final double STANDARD_AMBIENT_PRESSURE = 1013.25;

	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_CARBON = 12.0107;
	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_HYDROGEN = 1.00784;
	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_OXYGEN = 15.9994;
	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_SODIUM = 22.989769;
	/** [g/mol] */
	private static final double MOLECULAR_WEIGHT_CHLORINE = 35.453;

	/** Molecular weight of glucose [g/mol]. */
	private static final double MOLECULAR_WEIGHT_GLUCOSE = MOLECULAR_WEIGHT_CARBON * 6. + MOLECULAR_WEIGHT_HYDROGEN * 12.
		+ MOLECULAR_WEIGHT_OXYGEN * 6.;
	/** [g/mol] */
	static final double MOLECULAR_WEIGHT_SODIUM_CHLORIDE = MOLECULAR_WEIGHT_SODIUM + MOLECULAR_WEIGHT_CHLORINE;


	/**
	 * (should be 3.21 mol/l = 3.21 · MOLECULAR_WEIGHT_GLUCOSE / 10. [% w/w] = 57.82965228 (?)) [% w/w]
	 *
	 * @see #sugarFactor(double)
	 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6333755/">Stratford, Steels, Novodvorska, Archer, Avery. Extreme Osmotolerance and Halotolerance in Food-Relevant Yeasts and the Role of Glycerol-Dependent Cell Individuality. 2018.</a>
	 */
	static final double SUGAR_MAX = 3.21 * MOLECULAR_WEIGHT_GLUCOSE / 10.;

	/**
	 * TODO
	 * [% w/w]
	 *
	 * @see #fatFactor()
	 */
	private static final double FAT_MAX = 1.;

	/**
	 * (should be 2.04 mol/l = 2.04 · MOLECULAR_WEIGHT_SODIUM_CHLORIDE / 10. [% w/w] = 11.922324876 (?)) [% w/w]
	 *
	 * @see #saltFactor(double, double)
	 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6333755/">Stratford, Steels, Novodvorska, Archer, Avery. Extreme Osmotolerance and Halotolerance in Food-Relevant Yeasts and the Role of Glycerol-Dependent Cell Individuality. 2018.</a>
	 */
	static final double SALT_MAX = 2.04 * MOLECULAR_WEIGHT_SODIUM_CHLORIDE / 10.;

	/**
	 * @see #waterFactor()
	 * @see #HYDRATION_MIN
	 * @see #HYDRATION_MAX
	 */
	private static final double[] WATER_COEFFICIENTS = {-1.292, 7.65, -6.25};
	/**
	 * [% w/w]
	 *
	 * @see #WATER_COEFFICIENTS
	 * @see #waterFactor()
	 */
	static final double HYDRATION_MIN = (7.65 - Math.sqrt(Math.pow(7.65, 2.) - 4. * 6.25 * 1.292)) / (2. * 6.25);
	/**
	 * [% w/w]
	 *
	 * @see #WATER_COEFFICIENTS
	 * @see #waterFactor()
	 */
	static final double HYDRATION_MAX = (7.65 + Math.sqrt(Math.pow(7.65, 2.) - 4. * 6.25 * 1.292)) / (2. * 6.25);

	/**
	 * [mg/l]
	 *
	 * @see #waterChlorineDioxideFactor(double, double)
	 */
	public static final double WATER_CHLORINE_DIOXIDE_MAX = 1. / 0.0931;
	/**
	 * TODO
	 * [mg / l]
	 *
	 * @see #waterFixedResidueFactor()
	 */
	public static final double WATER_FIXED_RESIDUE_MAX = 1500.;
	public static final double PURE_WATER_PH = 5.4;

	/**
	 * @see #atmosphericPressureFactor(double)
	 * @see #ATMOSPHERIC_PRESSURE_MAX
	 */
	private static final double PRESSURE_FACTOR_K = 1.46;
	/**
	 * @see #atmosphericPressureFactor(double)
	 * @see #ATMOSPHERIC_PRESSURE_MAX
	 */
	private static final double PRESSURE_FACTOR_M = 2.031;
	/**
	 * Minimum inhibitory pressure [hPa].
	 *
	 * @see #atmosphericPressureFactor(double)
	 * @see #PRESSURE_FACTOR_K
	 * @see #PRESSURE_FACTOR_M
	 */
	public static final double ATMOSPHERIC_PRESSURE_MAX = Math.pow(10_000., 2.) * Math.pow(1. / PRESSURE_FACTOR_K, (1. / PRESSURE_FACTOR_M));

	/**
	 * [% w/w]
	 *
	 * @see #calculateYeast(Procedure)
	 */
	private static final double SOLVER_YEAST_MAX = 0.2;
	private static final int SOLVER_EVALUATIONS_MAX = 100;

	private static final double DOUGH_WEIGHT_PRECISION = 0.001;

	//densities: http://www.fao.org/3/a-ap815e.pdf
	//plot graphs: http://www.shodor.org/interactivate/activities/SimplePlot/
	//regression: https://planetcalc.com/5992/
	//regression: https://planetcalc.com/8735/
	//regression: http://www.colby.edu/chemistry/PChem/scripts/lsfitpl.html


	//accuracy is ±0.001%
	private final BaseUnivariateSolver<UnivariateFunction> solverYeast = new BracketingNthOrderBrentSolver(0.000_01,
		5);

	private final YeastModelAbstract yeastModel;

	/** Total sugar (glucose) quantity w.r.t. flour [% w/w]. */
	private double sugar;
	private SugarType sugarType;
	/** Raw sugar content [% w/w]. */
	private double rawSugar = 1.;
	/** Water content in sugar [% w/w]. */
	private double sugarWaterContent;

	/** Total fat quantity w.r.t. flour [% w/w]. */
	private double fat;
	/** Raw fat content [% w/w]. */
	private double rawFat = 1.;
	/** Fat density [g / ml]. */
	double fatDensity;
	/** Water content in fat [% w/w]. */
	private double fatWaterContent;
	/** Salt content in fat [% w/w]. */
	private double fatSaltContent;

	/** Total salt quantity w.r.t. flour [% w/w]. */
	private double salt;

	/** Total water quantity w.r.t. flour [% w/w]. */
	private double water;
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

	/** Total water quantity w.r.t. flour in milk [% w/w]. */
	private double milkWater;
	/** Total fat quantity w.r.t. flour in milk [% w/w]. */
	private double milkFat;

	/** Yeast quantity [% w/w]. */
	double yeast;
	private YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	private double rawYeast = 1.;

	private Flour flour;

	/** Egg content w.r.t. flour [% w/w]. */
	private double egg;
	/** Total shell quantity w.r.t. egg [% w/w]. */
	private double eggShell;
	/** Total water quantity w.r.t. flour in egg [% w/w]. */
	private double eggWater;
	/** Total fat quantity w.r.t. flour in egg [% w/w]. */
	private double eggFat;

	/** Temperature of ingredients [°C]. */
	Double ingredientsTemperature;
	/** Desired dough temperature [°C]. */
	private Double doughTemperature;

	/** Whether to correct for ingredients' content in fat/salt/water. */
	private boolean correctForIngredients;
	/** Whether to correct for humidity in the flour. */
	private boolean correctForFlourHumidity;
	/** Relative humidity of the air [% w/w]. */
	Double airRelativeHumidity;
	/** Atmospheric pressure [hPa]. */
	double atmosphericPressure = STANDARD_AMBIENT_PRESSURE;


	public static Dough create(final YeastModelAbstract yeastModel) throws DoughException{
		return new Dough(yeastModel);
	}

	private Dough(final YeastModelAbstract yeastModel) throws DoughException{
		if(yeastModel == null)
			throw DoughException.create("A yeast model must be provided");

		this.yeastModel = yeastModel;
	}


	/**
	 * @param sugar	Sugar quantity w.r.t. flour [% w/w].
	 * @param sugarType	Sugar type.
	 * @param sugarContent	Sucrose content [% w/w].
	 * @param waterContent	Water content [% w/w].
	 * @return	This instance.
	 * @throws DoughException	If sugar is too low or too high.
	 */
	public Dough addSugar(final double sugar, final SugarType sugarType, final double sugarContent, final double waterContent)
			throws DoughException{
		if(sugar < 0.)
			throw DoughException.create("Sugar [% w/w] must be positive");

		this.sugar += sugarType.factor * sugar * sugarContent;
		addWater(sugar * waterContent, 0., 0., PURE_WATER_PH, 0.);
		this.sugarType = sugarType;
		rawSugar = sugarContent;
		sugarWaterContent = waterContent;

		return this;
	}

	/**
	 * @param fat	Fat quantity w.r.t. flour [% w/w].
	 * @param density	Fat density [g / ml].
	 * @param fatContent	Sucrose content [% w/w].
	 * @param waterContent	Water content [% w/w].
	 * @param saltContent	Salt content [% w/w].
	 * @return	This instance.
	 * @throws DoughException	If fat is too low or too high.
	 */
	public Dough addFat(final double fat, final double fatContent, final double density, final double waterContent,
			final double saltContent) throws DoughException{
		final double factor = 1. / (this.fat + fat);
		rawFat = (this.fat * rawFat + fat * fatContent) * factor;
		fatDensity = (this.fat * fatDensity + fat * density) * factor;
		fatWaterContent = (this.fat * fatWaterContent + fat * waterContent) * factor;
		fatSaltContent = (this.fat * fatSaltContent + fat * saltContent) * factor;

		this.fat += fat * fatContent;
		addWater(fat * waterContent, 0., 0., PURE_WATER_PH, 0.);
		addSalt(fat * saltContent);

		if(fat < 0. || this.fat > FAT_MAX)
			throw DoughException.create("Fat [% w/w] must be between 0 and {}%", Helper.round(FAT_MAX * 100., 1));

		return this;
	}

	/**
	 * @param salt	Salt quantity w.r.t. flour [% w/w].
	 * @return	This instance.
	 * @throws DoughException	If salt is too low or too high.
	 */
	public Dough addSalt(final double salt) throws DoughException{
		if(salt < 0.)
			throw DoughException.create("Salt [% w/w] must be positive");

		this.salt += salt;

		return this;
	}

	/**
	 * @param water	Water quantity w.r.t. flour [% w/w].
	 * @param chlorineDioxide	Chlorine dioxide in water [mg/l].
	 * @param pH	pH of water.
	 * @param fixedResidue	Fixed residue in water [mg/l].
	 * @return	This instance.
	 * @throws DoughException	If water is too low, or chlorine dioxide is too low or too high, or fixed residue is too low or too high.
	 */
	public Dough addWater(final double water, final double chlorineDioxide, final double calciumCarbonate, final double pH,
			final double fixedResidue) throws DoughException{
		if(water < 0.)
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

		if(this.water + water > 0.){
			waterChlorineDioxide = (this.water * waterChlorineDioxide + water * chlorineDioxide) / (this.water + water);
			waterCalciumCarbonate = (this.water * waterCalciumCarbonate + water * calciumCarbonate) / (this.water + water);
			waterPH = (this.water * waterPH + water * pH) / (this.water + water);
			waterFixedResidue = (this.water * waterFixedResidue + water * fixedResidue) / (this.water + water);
		}
		this.water += water;

		return this;
	}

	/**
	 * @param milk	Milk quantity w.r.t. flour [% w/w].
	 * @param pH	pH of milk.
	 * @param waterContent	Water in milk [% w/w].
	 * @param fatContent	Fat in milk [% w/w].
	 * @return	This instance.
	 * @throws DoughException	If water is too low, or chlorine dioxide is too low or too high, or fixed residue is too low or too high.
	 */
	public Dough addMilk(final double milk, final double pH, final double waterContent, final double fatContent) throws DoughException{
		if(milk < 0.)
			throw DoughException.create("Hydration [% w/w] cannot be less than zero");

		final double water = milk * waterContent;
		if(this.water + water > 0.){
			waterChlorineDioxide = this.water * waterChlorineDioxide / (this.water + water);
			waterCalciumCarbonate = this.water * waterCalciumCarbonate / (this.water + water);
			waterPH = (this.water * waterPH + water * pH) / (this.water + water);
			waterFixedResidue = (this.water * waterFixedResidue + water * 0.09) / (this.water + water);
		}
		this.water += water;
		this.fat += milk * fatContent;

		milkWater = waterContent;
		milkFat = fatContent;

		return this;
	}

	/**
	 * @param egg	Egg weight [% w/w].
	 * @param pH	pH of egg.
	 * @param shellContent	Shell in egg [% w/w].
	 * @param waterContent	Water in egg [% w/w].
	 * @param fatContent	Fat in egg [% w/w].
	 * @return	This instance.
	 */
	public Dough addEgg(final double egg, final double pH, final double shellContent, final double waterContent, final double fatContent){
		final double water = egg * (1. - shellContent) * waterContent;
		if(this.water + water > 0.){
			waterChlorineDioxide = this.water * waterChlorineDioxide / (this.water + water);
			waterCalciumCarbonate = this.water * waterCalciumCarbonate / (this.water + water);
			waterPH = (this.water * waterPH + water * pH) / (this.water + water);
		}
		this.water += water;
		this.fat += egg * (1. - shellContent) * fatContent;

		this.egg += egg;
		eggShell = shellContent;
		eggWater = waterContent;
		eggFat = fatContent;

		return this;
	}

	/**
	 * @param yeastType	Yeast type.
	 * @param rawYeast	Raw yeast content [% w/w].
	 * @return	The instance.
	 */
	public Dough withYeast(final YeastType yeastType, final double rawYeast) throws DoughException{
		if(yeastType == null)
			throw DoughException.create("Missing yeast type");
		if(rawYeast <= 0. || rawYeast > 1.)
			throw DoughException.create("Raw yeast quantity must be between 0 and 1");

		this.yeastType = yeastType;
		this.rawYeast = rawYeast;

		return this;
	}

	/**
	 * @param flour	Flour data.
	 * @return	The instance.
	 */
	public Dough withFlour(final Flour flour) throws DoughException{
		if(flour == null)
			throw DoughException.create("Missing flour");

		this.flour = flour;

		return this;
	}

	/**
	 * @param ingredientsTemperature	Temperature of ingredients [°C].
	 * @return	The instance.
	 */
	public Dough withIngredientsTemperature(final double ingredientsTemperature) throws DoughException{
		if(ingredientsTemperature <= yeastModel.getTemperatureMin() || yeastModel.getTemperatureMax() <= ingredientsTemperature)
			throw DoughException.create("Ingredients temperature [°C] must be between {} and {} °C",
				Helper.round(yeastModel.getTemperatureMin(), 1), Helper.round(yeastModel.getTemperatureMax(), 1));

		this.ingredientsTemperature = ingredientsTemperature;

		return this;
	}

	/**
	 * @param doughTemperature	Desired dough temperature [°C].
	 * @return	The instance.
	 */
	public Dough withDoughTemperature(final double doughTemperature) throws DoughException{
		if(doughTemperature <= yeastModel.getTemperatureMin() || yeastModel.getTemperatureMax() <= doughTemperature)
			throw DoughException.create("Dough temperature [°C] must be between {} and {} °C",
				Helper.round(yeastModel.getTemperatureMin(), 1), Helper.round(yeastModel.getTemperatureMax(), 1));

		this.doughTemperature = doughTemperature;

		return this;
	}

	public Dough withCorrectForIngredients(){
		correctForIngredients = true;

		return this;
	}

	public Dough withCorrectForFlourHumidity(){
		correctForFlourHumidity = true;

		return this;
	}

	/**
	 * @param airRelativeHumidity	Relative humidity of the air [% w/w].
	 * @return	The instance.
	 */
	public Dough withAirRelativeHumidity(final double airRelativeHumidity){
		this.airRelativeHumidity = airRelativeHumidity;

		return this;
	}

	/**
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 * @return	This instance.
	 * @throws DoughException	If pressure is negative or above maximum.
	 */
	public Dough withAtmosphericPressure(final double atmosphericPressure) throws DoughException{
		if(atmosphericPressure < 0. || atmosphericPressure >= ATMOSPHERIC_PRESSURE_MAX)
			throw DoughException.create("Atmospheric pressure [hPa] must be between 0 and {} hPa",
				Helper.round(ATMOSPHERIC_PRESSURE_MAX, 1));

		this.atmosphericPressure = atmosphericPressure;

		return this;
	}

	/**
	 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6333755/">Stratford, Steels, Novodvorska, Archer, Avery. Extreme Osmotolerance and Halotolerance in Food-Relevant Yeasts and the Role of Glycerol-Dependent Cell Individuality. 2018.</a>
	 *
	 * @throws DoughException	If validation fails.
	 */
	private void validate() throws DoughException{
		if(water < HYDRATION_MIN || water > HYDRATION_MAX)
			throw DoughException.create("Hydration [% w/w] must be between {} and {}%",
				Helper.round(HYDRATION_MIN * 100., 1), Helper.round(HYDRATION_MAX * 100., 1));
		if(fractionOverTotal(sugar) > SUGAR_MAX)
			throw DoughException.create("Sugar [% w/w] must be less than {}%", Helper.round(SUGAR_MAX * 100., 1));
		if(fractionOverTotal(salt) > SALT_MAX)
			throw DoughException.create("Salt [% w/w] must be less than {}%", Helper.round(SALT_MAX * 100., 1));

		//convert [% w/w] to [mol / l]
		final double glucose = fractionOverTotal(sugar * 10.) / MOLECULAR_WEIGHT_GLUCOSE;
		//convert [% w/w] to [mol / l]
		final double sodiumChloride = fractionOverTotal(salt * 10.) / MOLECULAR_WEIGHT_SODIUM_CHLORIDE;
		if(glucose <= 0.3 && sodiumChloride > Math.exp((1. - Math.log(Math.pow(1. + Math.exp(1.0497 * glucose), 1.3221)))
				* (glucose / (0.0066 + 0.7096 * glucose))) || glucose > 0.3 && sodiumChloride > 1.9930 * (3. - glucose) / 2.7)
			throw DoughException.create("Salt and sugar are too much, yeast will die");
	}


	/**
	 * @param procedure	The recipe procedure.
	 * @param doughWeight	Desired dough weight [g].
	 * @return	The recipe.
	 */
	public Recipe createRecipe(final Procedure procedure, final double doughWeight) throws DoughException, YeastException{
		if(procedure == null)
			throw new IllegalArgumentException("Procedure must be valued");
		validate();
		procedure.validate(yeastModel);

		//calculate yeast:
		calculateYeast(procedure);

		//calculate ingredients:
		final Recipe recipe = calculateIngredients(doughWeight);

		//calculate times:
		LocalTime last = procedure.timeToBake.minus(procedure.seasoning);
		recipe.withSeasoningInstant(last);
		final LocalTime[][] stageStartEndInstants = new LocalTime[procedure.leaveningStages.length][2];
		for(int i = stageStartEndInstants.length - 1; i >= 0; i --){
			last = last.minus(procedure.leaveningStages[i].afterStageWork);
			stageStartEndInstants[i][1] = last;
			last = last.minus(procedure.leaveningStages[i].duration);
			stageStartEndInstants[i][0] = last;
		}
		final LocalTime doughMakingInstant = last.minus(procedure.doughMaking);
		last = stageStartEndInstants[0][0];
		final LocalTime[] stretchAndFoldStartInstants = new LocalTime[procedure.stretchAndFoldStages.length];
		for(int i = 0; i < stretchAndFoldStartInstants.length; i ++){
			last = last.plus(procedure.stretchAndFoldStages[i].lapse);
			stretchAndFoldStartInstants[i] = last;
		}
		recipe.withStageStartEndInstants(stageStartEndInstants)
			.withStretchAndFoldStartInstants(stretchAndFoldStartInstants)
			.withDoughMakingInstant(doughMakingInstant);
		return recipe;
	}

	/**
	 * Find the initial yeast able to obtain a given volume expansion ratio after a series of consecutive stages at a given duration at
	 * temperature.
	 *
	 * @param procedure	Data for procedure.
	 */
	@SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
	void calculateYeast(final Procedure procedure) throws YeastException{
		//reset variable
		yeast = 0.;

		try{
			final UnivariateFunction f = yeast -> calculateVolumeExpansionRatioDifference(yeast, procedure);
			yeast = solverYeast.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_YEAST_MAX);
		}
		catch(final NoBracketingException e){
			throw YeastException.create("No yeast quantity will ever be able to produce the given expansion ratio");
		}
		catch(final TooManyEvaluationsException e){
			throw YeastException.create("Cannot calculate yeast quantity, try increasing maximum number of evaluations in the solver");
		}
	}

	double calculateVolumeExpansionRatioDifference(final double yeast, final Procedure procedure){
		final double temperature = (doughTemperature != null? doughTemperature:
			(ingredientsTemperature != null? ingredientsTemperature: STANDARD_AMBIENT_TEMPERATURE));
		final double kWaterChlorineDioxide = waterChlorineDioxideFactor(yeast, temperature);
		final double aliveYeast = kWaterChlorineDioxide * yeast;
		final double alpha = maximumRelativeVolumeExpansionRatio(aliveYeast);
		final double lambda = estimatedLag(aliveYeast, temperature);
		final double[] ingredientsFactors = new double[procedure.leaveningStages.length];
		for(int i = 0; i < procedure.leaveningStages.length; i ++){
			ingredientsFactors[i] = ingredientsFactor(aliveYeast, procedure.leaveningStages[i].temperature, atmosphericPressure);
			if(ingredientsFactors[i] == 0.)
				return Double.POSITIVE_INFINITY;
		}


		LeaveningStage currentStage;
		LeaveningStage previousStage = LeaveningStage.ZERO;

		double volumeExpansionRatio = 0.;
		Duration duration = previousStage.duration;

		for(int i = 0; i < procedure.leaveningStages.length && i <= procedure.targetVolumeExpansionRatioAtLeaveningStage; i ++){
			currentStage = procedure.leaveningStages[i];

			//avoid modifying `lambda` if the temperature is the same
			if(i > 0 && previousStage.temperature != currentStage.temperature){
				final double previousVolume = yeastModel.volumeExpansionRatio(getHours(duration), lambda, alpha,
					previousStage.temperature, ingredientsFactors[i]);
				final double currentVolume = yeastModel.volumeExpansionRatio(getHours(duration), lambda, alpha,
					currentStage.temperature, ingredientsFactors[i]);

				//account for stage volume decrease
				volumeExpansionRatio += previousVolume - currentVolume;
			}
			else if(i == 0){
				final double currentVolume = yeastModel.volumeExpansionRatio(getHours(duration.plus(currentStage.duration)),
					lambda, alpha, currentStage.temperature, ingredientsFactors[i]);

				volumeExpansionRatio += currentVolume;
			}


			duration = duration.plus(currentStage.duration);
			previousStage = currentStage;
		}

		return volumeExpansionRatio - procedure.targetDoughVolumeExpansionRatio;
	}

	private double getHours(final Duration duration){
		return duration.toMinutes() / 60.;
	}

	/**
	 * Maximum relative volume expansion ratio.
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 *
	 * @param yeast	Quantity of yeast [% w/w].
	 * @return	The estimated lag [hrs].
	 */
	private double maximumRelativeVolumeExpansionRatio(final double yeast){
		//FIXME this formula is for 36±1 °C
		//vertex must be at 1.1%
		return (yeast < 0.011? 24_546. * (0.022 - yeast) * yeast: 2.97);
	}

	/**
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 * @see <a href="https://meridian.allenpress.com/jfp/article/71/7/1412/172677/Individual-Effects-of-Sodium-Potassium-Calcium-and">Bautista-Gallego, Arroyo-López, Durán-Quintana, Garrido-Fernández. Individual Effects of Sodium, Potassium, Calcium, and Magnesium Chloride Salts on Lactobacillus pentosus and Saccharomyces cerevisiae Growth. 2008.</a>
	 *
	 * @param yeast   Quantity of yeast [% w/w].
	 * @param temperature   Temperature [°C].
	 * @return	The estimated lag [hrs].
	 */
	private double estimatedLag(final double yeast, final double temperature){
		///the following formula is for 2.51e7 CFU/ml yeast
		final double yeastRatio = getYeastRatio(yeast, temperature, 2.51e7);
		//transform [% w/w] to [g / l]
		final double s = fractionOverTotal(salt) * 10. / yeastRatio;
		final double saltLag = Math.log(1. + Math.exp(0.494 * (s - 84.)));

		//FIXME this formula is for 36±1 °C
		final double lag = (yeast > 0.? 0.0068 * Math.pow(yeast, -0.937): Double.POSITIVE_INFINITY);

		return lag + saltLag;
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
	 *
	 * @param yeast	yeast [% w/w]
	 * @param temperature	Temperature [°C].
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 * @return	Factor to be applied to maximum specific growth rate.
	 */
	private double ingredientsFactor(final double yeast, final double temperature, final double atmosphericPressure){
//		final double kTemperature = temperatureFactor(temperature);
//		final double kSugar = sugarFactor(temperature);
//		final double kFat = fatFactor();
		final double kSalt = saltFactor(yeast, temperature);
//		final double kWater = waterFactor();
//		final double kWaterPH = waterPHFactor(yeast, temperature);
//		final double kWaterFixedResidue = waterFixedResidueFactor();
//		final double kHydration = kWater * kWaterPH * kWaterFixedResidue;
		final double kAtmosphericPressure = atmosphericPressureFactor(atmosphericPressure);
		return /*kTemperature * kSugar * kFat * */kSalt * /*kHydration * */kAtmosphericPressure;
	}

	/**
	 * https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwja7bqJ6-nwAhUk_7sIHbO_DcE4HhAWMAF6BAgCEAQ&url=https%3A%2F%2Fdigikogu.taltech.ee%2Ftestimine%2Fet%2FDownload%2F0a9ce955-b3f0-4513-94c2-148de01a6e46%2FEffectofchangingenvironmentalconditionsonthe.pdf&usg=AOvVaw0TgJTvRIHCd4Od6YRm8uNW
	 *
	 * @param temperature	Temperature [°C].
	 * @return	Correction factor.
	 */
	private double temperatureFactor(final double temperature){
		return Math.exp(17.177 - 5449.8 / (temperature + Water.ABSOLUTE_ZERO));
	}

	/**
	 * @see <a href="https://uwaterloo.ca/chem13-news-magazine/april-2015/activities/fermentation-sugars-using-yeast-discovery-experiment">The fermentation of sugars using yeast: A discovery experiment</a>
	 * @see <a href="http://www.biologydiscussion.com/industrial-microbiology-2/yeast-used-in-bakery-foods/yeast-used-in-bakery-foods-performance-determination-forms-effect-industrial-microbiology/86555">Yeast used in bakery foods: Performance, determination, forms & effect. Industrial Microbiology</a>
	 * @see <a href="https://bib.irb.hr/datoteka/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlića, Querolb, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzeviiand their interspecific hybrid. 2009.</a>
	 *
	 * https://ttu-ir.tdl.org/bitstream/handle/2346/8716/31295003966958.pdf
	 * https://iranjournals.nlai.ir/bitstream/handle/123456789/88633/5855EFA78193B19FE41ABB3F40CED664.pdf
	 * https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwip0IKgz-zwAhVo_7sIHWWMA7QQFjAHegQICxAE&url=https%3A%2F%2Fwww.mdpi.com%2F2311-5637%2F5%2F1%2F10%2Fpdf&usg=AOvVaw1l5HVZ9GZR2v0oIdwKZ_fo
	 *
	 * https://www.craftybaking.com/how-baking-works/yeast
	 *
	 * @param temperature	Temperature [°C].
	 * @return	Correction factor.
	 */
	private double sugarFactor(final double temperature){
		//[g/l]
		final double s = 1000. * getSugarRatio(sugar + (flour != null? flour.sugar * SugarType.GLUCOSE.factor: 0.), temperature);
		//TODO
		//Monod equation
		//T[°C]	Ki[g/l]
		//30	14.1
		//34	14.5
		//37	19.0
//		//Monod saturation constant [g/l]
//		final double Ks = (0.2454 + 0.0067 * temperature) * temperature;

//		//Andrews model:
//		//Monod saturation constant [g/l]
//		final double Ks = 7.919;
//		//inhibition constant [g/l]
//		final double Ksi = 249.365;
//		return s / ((Ks + s) * (1 + s / Ksi));

		return 1.;

//		/**
//		 * base is pH 5.4±0.1, 20 mg/l glucose
//		 * @see io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast#getMaximumSpecificGrowthRate()
//		 */
//		final double basePH = 5.4;
//		final double baseSugar = 20. / 1000.;
//		final double baseMu = (0.3945 + (-0.00407 + 0.0000096 * baseSugar) * baseSugar
//			+ (-0.00375 + 0.000025 * baseSugar) * temperature
//			+ (0.003 - 0.00002 * baseSugar) * basePH
//			) / 3.;
//
//		final double s = fractionOverTotal(sugar);
//		return (0.3945 + (-0.00407 + 0.0000096 * s) * s
//			+ (-0.00375 + 0.000025 * s) * temperature
//			+ (0.003 - 0.00002 * s) * waterPH
//		) / (3. * baseMu);
	}

	/**
	 * TODO high fat content inhibits leavening
	 * 0.1-0.2% is desirable
	 *
	 * @return	Correction factor.
	 */
	private double fatFactor(){
		//0 <= fat <= FAT_MAX
		//1+fat/300?
		return 1.;
	}

	/**
	 * @see <a href="https://meridian.allenpress.com/jfp/article/71/7/1412/172677/Individual-Effects-of-Sodium-Potassium-Calcium-and">Bautista-Gallego, Arroyo-López, Durán-Quintana, Garrido-Fernández. Individual Effects of Sodium, Potassium, Calcium, and Magnesium Chloride Salts on Lactobacillus pentosus and Saccharomyces cerevisiae Growth. 2008.</a>
	 * @see <a href="https://www.microbiologyresearch.org/docserver/fulltext/micro/64/1/mic-64-1-91.pdf">Watson. Effects of Sodium Chloride on Steady-state Growth and Metabolism of Saccharomyces cerevisiae. 1970. Journal of General Microbiology. Vol 64.</a>
	 * @see <a href="https://aem.asm.org/content/aem/43/4/757.full.pdf">Wei, Tanner, Malaney. Effect of Sodium Chloride on baker's yeast growing in gelatin. 1981. Applied and Environmental Microbiology. Vol. 43, No. 4.</a>
	 * @see <a href="https://meridian.allenpress.com/jfp/article/70/2/456/170132/Use-of-Logistic-Regression-with-Dummy-Variables">López, Quintana, Fernández. Use of logistic regression with dummy variables for modeling the growth–no growth limits of Saccharomyces cerevisiae IGAL01 as a function of Sodium chloride, acid type, and Potassium Sorbate concentration according to growth media. 2006. Journal of Food Protection. Vol 70, No. 2.</a>
	 * @see <a href="https://undergradsciencejournals.okstate.edu/index.php/jibi/article/view/2512">Lenaburg, Kimmons, Kafer, Holbrook, Franks. Yeast Growth: The effect of tap water and distilled water on yeast fermentation with salt additives. 2016.</a>
	 * @see <a href="https://www.academia.edu/28193854/Impact_of_sodium_chloride_on_wheat_flour_dough_for_yeast_leavened_products_II_Baking_quality_parameters_and_their_relationship">Beck, Jekle, Becker. Impact of sodium chloride on wheat flour dough for yeast-leavened products. II. Baking quality parameters and their relationship. 2010.</a>
	 *
	 * @param yeast   yeast [% w/w]
	 * @return	Correction factor.
	 */
	private double saltFactor(final double yeast, final double temperature){
		double factor = 1.;
		if(salt > 0.){
			///the following formula is for 2.51e7 CFU/ml yeast
			final double yeastRatio = getYeastRatio(yeast, temperature, 2.51e7);
			final double s = fractionOverTotal(salt) / yeastRatio;
			final double x = 11.7362 * s;
			final double a = (Double.isInfinite(Math.exp(x))? 1. - 0.0256 * x: 1. - Math.log(Math.pow(1. + Math.exp(x), 0.0256)));
			final double b = s / (87.5679 - 0.2725 * s);
			factor = Math.exp(a * b);
		}
		return factor;
	}

	/**
	 * @see <a href="https://www.nature.com/articles/s41598-018-36786-2.pdf">Minervini, Dinardo, de Angelis, Gobbetti. Tap water is one of the drivers that establish and assembly the lactic acid bacterium biota during sourdough preparation. 2018.</a>
	 * @see <a href="http://fens.usv.ro/index.php/FENS/article/download/328/326">Codina, Mironeasa, Voica. Influence of wheat flour dough hydration levels on gas production during dough fermentation and bread quality. 2011. Journal of Faculty of Food Engineering. Vol. X, Issue 4.</a>
	 *
	 * @return	Correction factor.
	 */
	private double waterFactor(){
		//TODO
		return 1.;

//		return (-2.0987 + 7.5743 * water) * water;

//		return (HYDRATION_MIN <= water && water < HYDRATION_MAX? Helper.evaluatePolynomial(WATER_COEFFICIENTS, water): 0.);
	}

	/**
	 * https://academic.oup.com/mutage/article/19/2/157/1076450
	 * Buschini, Carboni, Furlini, Poli, Rossi. Sodium hypochlorite-, chlorine dioxide- and peracetic acid-induced genotoxicity detected by Saccharomyces cerevisiae tests. 2004.
	 *
	 * @param yeast   yeast [% w/w]
	 * @return	Correction factor.
	 */
	private double waterChlorineDioxideFactor(final double yeast, final double temperature){
		///the following formula is for 1e8 CFU/ml yeast
		final double yeastRatio = getYeastRatio(yeast, temperature, 1.e8);

		final double w = (yeastRatio > 0.? fractionOverTotal(water) / yeastRatio: 0.);
		return Math.max(1. - waterChlorineDioxide * w / WATER_CHLORINE_DIOXIDE_MAX, 0.);
	}

	private double getYeastRatio(final double yeast, final double temperature, final double baseDensity){
		final double doughDensity = Recipe.create()
			.withFlour(1.)
			.withWater(water)
			.withYeast(yeast)
			.withSugar(sugar)
			.withFat(fat)
			.withSalt(salt)
			.density(fatDensity, temperature, atmosphericPressure);
		return fractionOverTotal(yeast * rawYeast) * doughDensity * (YeastType.FY_CELL_COUNT / baseDensity);
	}

	private double getSugarRatio(final double sugar, final double temperature){
		double sugarRatio = 0.;
		if(sugar > 0.){
			final double doughDensity = Recipe.create()
				.withFlour(1.)
				.withWater(water)
				.withYeast(yeast)
				.withSugar(sugar)
				.withFat(fat)
				.withSalt(salt)
				.density(fatDensity, temperature, atmosphericPressure);
			sugarRatio = fractionOverTotal(sugar) * (sugarType != null? sugarType.factor: SugarType.GLUCOSE.factor) * doughDensity;
		}
		return sugarRatio;
	}

	/**
	 * @see <a href="https://bib.irb.hr/datoteka/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlića, Querolb, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzeviiand their interspecific hybrid. 2009.</a>
	 * @see <a href="https://academic.oup.com/femsyr/article/15/2/fou005/534737">Peña, Sánchez, Álvarez, Calahorra, Ramírez. Effects of high medium pH on growth, metabolism and transport in Saccharomyces cerevisiae. 2015.</a>
	 *
	 * @see <a href="https://oatao.univ-toulouse.fr/1556/1/Serra_1556.pdf">Serra, Strehaiano, Taillandier. Influence of temperature and pH on Saccharomyces bayanus var. uvarum growth; impact of a wine yeast interspecifichy bridization on these parameters. 2005.</a>
	 *
	 * pH
	 * pH is important in dough-making because it affects chemical and biological reactions. Most notably, it affects the rate of amylase
	 * enzyme performance (conversion of starch to sugar) and, as a result, the rate of fermentation. The optimum pH for starch conversion
	 * and fermentation and, hence, for pizza dough, is about 5, or slightly acidic. This pH level is best achieved by using water with
	 * pH 6.5 to 8, with pH 7 being the optimum.
	 *
	 * http://ache.org.rs/CICEQ/2010/No2/12_3141_2009.pdf
	 * https://www.scielo.br/pdf/bjm/v39n2/a24.pdf
	 * https://core.ac.uk/download/pdf/12040042.pdf
	 * https://www.researchgate.net/profile/Sandra-Antonini/publication/335275152_Interaction_of_4-ethylphenol_pH_sucrose_and_ethanol_on_the_growth_and_fermentation_capacity_of_the_industrial_strain_of_Saccharomyces_cerevisiae_PE-2/links/5d5edff0299bf1b97cff2252/Interaction-of-4-ethylphenol-pH-sucrose-and-ethanol-on-the-growth-and-fermentation-capacity-of-the-industrial-strain-of-Saccharomyces-cerevisiae-PE-2.pdf
	 *
	 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC91662/
	 *
	 * Maximum specific growth rate starts at 0 at 0 pH, then rises until before 2.1 pH, decreases until 2.7 pH, then rises until 6 pH, then decreases again, reaching 0 at 9 pH.
	 *
	 * @param yeast	yeast [% w/w]
	 * @return	Correction factor.
	 */
	private double waterPHFactor(final double yeast, final double temperature){
		//TODO
		final double totalFraction = totalFraction();
		final double compositePH = (
			waterPH * water
			//flour
			+ 6.2 * 1.
			+ 5.5 * sugar
			+ 3.3 * yeast) / totalFraction;
		final double cph = (compositePH - 2.75) / (4.25 - 2.75);
		final double t = (temperature - 18.) / (30. - 18.);
		final double as = (0.025 + 0.013 * cph) * cph
			+ 0.019 * t * cph;
//		return Math.max(compositePH <= 5.6? compositePH / 5.6: 1. - (compositePH - 5.6) / 3.4, 0.);

		/**
		 * base is pH 5.4±0.1, 20 mg/l glucose
		 * @see io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast#getMaximumSpecificGrowthRate()
		 */
//		final double baseMu = getPHMu(temperature, 5.4, 20. / 1000.);

//		return Math.max(getPHMu(temperature, compositePH, sugar / totalFraction) / baseMu, 0.);

		return 1.;
	}

	private double getPHMu(final double temperature, final double ph, final double sugar){
		final double t = (temperature - 18.) / (30. - 18.);
		final double s = (sugar - 150.) / (250. - 150.);
		final double p = (ph - 2.75) / (4.25 - 2.75);
		return 0.22
			+ (0.108 - 0.014 * t) * t
			+ (0.025 + 0.013 * p) * p
			+ (-0.040 + 0.032 * s) * s
			+ 0.019 * t * p
			+ 0.010 * t * s
			- 0.001 * p * s;
	}

	/**
	 * TODO Se la durezza dell’acqua è troppo elevata la fermentazione subisce rallentamenti a causa della formazione di una struttura glutinica troppo rigida. In caso contrario, dove la durezza dell’acqua risulta essere troppo scarsa, l’impasto si presenta assai appiccicoso e poco manipolabile. In questo frangente sarà utile abbassare l’idratazione.
	 *
	 * Hardness
	 * Various minerals can be found in water. Two of them—calcium and magnesium—play a major role in water hardness and also in
	 * dough-making. The type and amount of these minerals varies with the locale.
	 * Medium-hard water—that is, water with 50 to 100 ppm (parts per million) of carbonates—is the best for baking. It contains the right
	 * amount of mineral salts—mostly of calcium and magnesium—which strengthen gluten and also, to some extent, serve as yeast nutrients.
	 * Soft water (less than 50 ppm carbonates) has a shortage of those salts, which tends to result in a soft, sticky dough because there’s
	 * less gluten-tightening effect from minerals. To counteract stickiness, reduce the water portion by about 2 percent. It can also help
	 * to increase the salt portion up to 2.5 percent of flour weight. On the baked pizza, the soft water tends to produce a crust texture
	 * and color that’s less than optimum.
	 * Hard water (over 100 ppm carbonates) has too much of the salts. This toughens gluten excessively, which retards the fermentation or
	 * rise of dough. To counteract that, increase the yeast level and, if it’s used, adjust the amount of yeast food. Also, adding malt or
	 * malted flour might help.
	 * Water from a city source usually has a proper degree of hardness for good dough development. However, a pizzeria in a small town or
	 * one that draws ground water might have excessively hard water.
	 *
	 * TODO Generally, water of medium hardness, with about 100 to 150 ppm of minerals, is best suited to bread baking. The minerals in water provide food for the yeast, and therefore can benefit fermentation. However, if the water is excessively hard, there will be a tightening effect on the gluten, as well as a decrease in the fermentation rate (the minerals make water absorption more difficult for the proteins in the flour). On the other hand, if water is excessively soft, the lack of minerals will result in a dough that is sticky and slack. Generally speaking, most water is not extreme in either direction, and if water is potable, it is suitable for bread baking.
	 * https://pdfs.semanticscholar.org/793b/586b66ccefcc0bee1da2d1b480425850bc45.pdf
	 *
	 * @return	Correction factor.
	 */
	private double waterFixedResidueFactor(){
		//0 <= fixedResidue <= WATER_FIXED_RESIDUE_MAX
		return 1.;
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

	private double fractionOverTotal(final double value){
		return value / totalFraction();
	}

	private double totalFraction(){
		return 1. + water + sugar + yeast + salt + fat;
	}

	private Recipe calculateIngredients(final double doughWeight){
		double yeast, flour, water, sugar, fat, salt,
			difference;
		double totalFlour = fractionOverTotal(doughWeight);
		final double waterCorrection = calculateWaterCorrection();
		final double yeastFactor = this.yeast / (yeastType.factor * rawYeast);
		final double sugarFactor = this.sugar / (sugarType.factor * rawSugar);
		do{
			yeast = totalFlour * yeastFactor;
			flour = totalFlour - yeast * (1. - rawYeast);
			sugar = totalFlour * sugarFactor;
			final double fatCorrection = calculateFatCorrection(flour);
			final double rawEgg = egg * (1. - eggShell);
			fat = Math.max(totalFlour * this.fat * (1. - milkFat) - rawEgg * eggFat - fatCorrection, 0.) / rawFat;
			water = Math.max((totalFlour * this.water - rawEgg * eggWater - sugar * sugarWaterContent - fat * fatWaterContent - waterCorrection)
				/ (milkWater > 0.? milkWater: 1.), 0.);
			final double saltCorrection = calculateSaltCorrection(flour);
			salt = Math.max(totalFlour * this.salt - fat * fatSaltContent - saltCorrection, 0.);

			//refine approximation:
			final double calculatedDough = flour + water + yeast + sugar + salt + fat;
			difference = doughWeight - calculatedDough;
			totalFlour += difference * 0.6;
		}while(Math.abs(difference) > DOUGH_WEIGHT_PRECISION);

		final Recipe recipe = Recipe.create()
			.withFlour(flour)
			.withWater(water)
			.withYeast(yeast)
			.withSugar(sugar)
			.withFat(fat)
			.withSalt(salt);

		if(doughTemperature != null && ingredientsTemperature != null){
			//calculate water temperature:
			final double waterTemperature = (doughWeight * doughTemperature - (doughWeight - water) * ingredientsTemperature) / water;
			if(waterTemperature >= yeastModel.getTemperatureMax())
				LOGGER.warn("Water temperature ({} °C) is greater that maximum temperature sustainable by the yeast ({} °C), be aware of thermal shock!",
					Helper.round(waterTemperature, 1), Helper.round(yeastModel.getTemperatureMax(), 1));

			recipe.withWaterTemperature(waterTemperature);
		}

		return recipe;
	}

	private double calculateWaterCorrection(){
		double correction = 0.;
		if(correctForIngredients)
			correction += sugar * sugarWaterContent + fat * fatWaterContent;
		if(correctForFlourHumidity)
			//FIXME: 70.62% is to obtain a humidity of 13.5%
			correction += Flour.estimatedHumidity(airRelativeHumidity) - Flour.estimatedHumidity(0.7062);
		return correction;
	}

	private double calculateFatCorrection(final double flourWeight){
		return (correctForIngredients? flourWeight * flour.fat: 0.);
	}

	private double calculateSaltCorrection(final double flourWeight){
		return (correctForIngredients? flourWeight * flour.salt + fat * fatSaltContent: 0.);
	}


	/**
	 * FIXME without any reference!!
	 *
	 * Physical data expressed on the strength of the gluten mesh.
	 * <p>In theory it is the maximum time that a dough can "remain leavened".
	 * <br />
	 * This figure does not take into account other variables of the dough, such as hydration or the quality of the flour.</p>
	 *
	 * @return	Resilience.
	 */
	public Duration getMaxLeaveningDuration(){
		return Duration.ofMinutes((long)((flour.strength - 157.8) * 60. / 11.11));
	}

}
