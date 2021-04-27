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
	 * (should be 3.21 mol/l = 3.21 * MOLECULAR_WEIGHT_GLUCOSE / 10. [% w/w] = 57.82965228 (?)) [% w/w]
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
	static final double FAT_MAX = 1.;

	/**
	 * (should be 2.04 mol/l = 2.04 * MOLECULAR_WEIGHT_SODIUM_CHLORIDE / 10. [% w/w] = 11.922324876 (?)) [% w/w]
	 *
	 * @see #saltFactor()
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
	 * [mg/l] = 1 ppm
	 *
	 * @see #waterChlorineDioxideFactor()
	 */
	public static final double WATER_CHLORINE_DIOXIDE_MAX = 1. / 0.0931;
	/**
	 * TODO
	 * [mg/l]
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
	 * @see #calculateYeast(Procedure, double)
	 */
	private static final double SOLVER_YEAST_MAX = 1.;
	private static final int SOLVER_EVALUATIONS_MAX = 100;

	private static final double DOUGH_WEIGHT_PRECISION = 0.001;

	//densities: http://www.fao.org/3/a-ap815e.pdf
	//plot graphs: http://www.shodor.org/interactivate/activities/SimplePlot/
	//regression: https://planetcalc.com/5992/
	//regression: https://planetcalc.com/8735/
	//regression: http://www.colby.edu/chemistry/PChem/scripts/lsfitpl.html


	//accuracy is ±0.001%
	private final BaseUnivariateSolver<UnivariateFunction> solverYeast = new BracketingNthOrderBrentSolver(0.000_01, 5);

	private final YeastModelAbstract yeastModel;

	/** Total sugar (glucose) quantity w.r.t. flour [% w/w]. */
	private double sugar;
	SugarType sugarType;
	/** Raw sugar content [% w/w]. */
	private double sugarContent = 1.;
	/** Water content in sugar [% w/w]. */
	private double sugarWaterContent;

	/** Total fat quantity w.r.t. flour [% w/w]. */
	private double fat;
	/** Raw fat content [% w/w]. */
	private double fatContent = 1.;
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
	/**
	 * Calcium carbonate (CaCO3) in water [mg/l] = [°F * 10] = [°I * 7] = [°dH * 5.6].
	 *
	 * TODO Generally, water of medium hardness, with about 100 to 150 ppm of minerals, is best suited to bread baking. The minerals in water provide food for the yeast, and therefore can benefit fermentation. However, if the water is excessively hard, there will be a tightening effect on the gluten, as well as a decrease in the fermentation rate (the minerals make water absorption more difficult for the proteins in the flour). On the other hand, if water is excessively soft, the lack of minerals will result in a dough that is sticky and slack. Generally speaking, most water is not extreme in either direction, and if water is potable, it is suitable for bread baking.
	 */
	private double waterCalciumCarbonate;

	/** Yeast quantity [% w/w]. */
	double yeast;
	private YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	private double rawYeast = 1.;


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
		addPureWater(sugar * waterContent);
		this.sugarType = sugarType;
		this.sugarContent = sugarContent;
		this.sugarWaterContent = waterContent;

		return this;
	}

	/**
	 * @param fat	Fat quantity w.r.t. flour [% w/w].
	 * @param fatContent	Sucrose content [% w/w].
	 * @param waterContent	Water content [% w/w].
	 * @param saltContent	Salt content [% w/w].
	 * @return	This instance.
	 * @throws DoughException	If fat is too low or too high.
	 */
	public Dough addFat(final double fat, final double fatContent, final double waterContent, final double saltContent)
			throws DoughException{
		this.fat += fat * fatContent;
		addPureWater(fat * waterContent);
		addSalt(fat * saltContent);
		this.fatWaterContent = waterContent;
		this.fatSaltContent = saltContent;

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
	 * @return	This instance.
	 * @throws DoughException	If water is too low.
	 */
	public Dough addPureWater(final double water) throws DoughException{
		return addWater(water, 0., 0., PURE_WATER_PH, 0.);
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

		//convert [% w/w] to [mol/l]
		final double glucose = fractionOverTotal(sugar * 10.) / MOLECULAR_WEIGHT_GLUCOSE;
		//convert [% w/w] to [mol/l]
		final double sodiumChloride = fractionOverTotal(salt * 10.) / MOLECULAR_WEIGHT_SODIUM_CHLORIDE;
		if(glucose <= 0.3 && sodiumChloride > Math.exp((1. - Math.log(Math.pow(1. + Math.exp(1.0497 * glucose), 1.3221)))
				* (glucose / (0.0066 + 0.7096 * glucose))) || glucose > 0.3 && sodiumChloride > 1.9930 * (3. - glucose) / 2.7)
			throw DoughException.create("Salt and sugar are too much, yeast will die");
	}


	/**
	 * @param ingredients	The recipe ingredients.
	 * @param procedure	The recipe procedure.
	 * @param doughWeight	Desired dough weight [g].
	 * @return	The recipe.
	 */
	public Recipe createRecipe(final Ingredients ingredients, final Procedure procedure, final double doughWeight)
			throws DoughException, YeastException{
		if(ingredients == null)
			throw new IllegalArgumentException("Ingredients must be valued");
		if(procedure == null)
			throw new IllegalArgumentException("Procedure must be valued");
		validate();
		ingredients.validate(yeastModel);
		procedure.validate(yeastModel);

		//calculate yeast:
		calculateYeast(procedure, ingredients.atmosphericPressure);

		//calculate ingredients:
		final Recipe recipe = calculateIngredients(ingredients, doughWeight);

		//calculate times:
		LocalTime last = procedure.timeToBake.minus(procedure.seasoning);
		recipe.withSeasoningInstant(last);
		final LocalTime[][] stageStartEndInstants = new LocalTime[procedure.stagesWork.length][2];
		for(int i = procedure.stagesWork.length - 1; i >= 0; i --){
			last = last.minus(procedure.stagesWork[i]);
			stageStartEndInstants[i][1] = last;
			last = last.minus(procedure.leaveningStages[i].duration);
			stageStartEndInstants[i][0] = last;
		}
		recipe.withStageStartEndInstants(stageStartEndInstants)
			.withDoughMakingInstant(last.minus(procedure.doughMaking));
		return recipe;
	}

	/**
	 * Find the initial yeast able to obtain a given volume expansion ratio after a series of consecutive stages at a given duration at
	 * temperature.
	 *
	 * @param procedure	Data for procedure.
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 */
	void calculateYeast(final Procedure procedure, final double atmosphericPressure) throws YeastException{
		try{
			final UnivariateFunction f = yeast -> {
				final double alpha = maximumRelativeVolumeExpansionRatio(yeast);
				double lambda = estimatedLag(yeast);
				LeaveningStage currentStage = procedure.leaveningStages[0];
				double volumeExpansionRatio = 0.;
				Duration duration = Duration.ZERO;
				if(procedure.targetVolumeExpansionRatioAtLeaveningStage > 0){
					int stretchAndFoldIndex = 0;
					Duration stretchAndFoldDuration = Duration.ZERO;
					for(int i = 1; i < procedure.leaveningStages.length; i ++){
						final LeaveningStage previousStage = procedure.leaveningStages[i - 1];
						duration = duration.plus(previousStage.duration);
						currentStage = procedure.leaveningStages[i];

						//avoid modifying `lambda` if the temperature is the same
						double currentVolume = 0.;
						if(previousStage.temperature != currentStage.temperature){
							final double ingredientsFactor = ingredientsFactor(previousStage.temperature, atmosphericPressure);
							final double previousVolume = yeastModel.volumeExpansionRatio(duration.toMinutes() / 60., lambda, alpha,
								previousStage.temperature, ingredientsFactor);
							lambda = Math.max(lambda - previousStage.duration.toMinutes() / 60., 0.);
							currentVolume = yeastModel.volumeExpansionRatio(duration.toMinutes() / 60., lambda, alpha, currentStage.temperature,
								ingredientsFactor);

							volumeExpansionRatio += previousVolume - currentVolume;
						}
						//account for stage volume decrease
						volumeExpansionRatio -= currentVolume * previousStage.volumeDecrease;

						//apply stretch&fold volume reduction:
						double stretchAndFoldVolumeDecrease = 0.;
						while(procedure.stretchAndFoldStages != null && stretchAndFoldIndex < procedure.stretchAndFoldStages.length){
							final StretchAndFoldStage stretchAndFoldStage = procedure.stretchAndFoldStages[stretchAndFoldIndex];
							if(stretchAndFoldDuration.plus(stretchAndFoldStage.lapse).compareTo(duration) > 0)
								break;

							stretchAndFoldIndex ++;
							stretchAndFoldDuration = stretchAndFoldDuration.plus(stretchAndFoldStage.lapse);

							final double ingredientsFactor = ingredientsFactor(currentStage.temperature, atmosphericPressure);
							final double volumeAtStretchAndFold = yeastModel.volumeExpansionRatio(duration.minus(previousStage.duration)
								.plus(stretchAndFoldDuration).toMinutes() / 60., lambda, alpha, currentStage.temperature, ingredientsFactor);
							stretchAndFoldVolumeDecrease += (volumeAtStretchAndFold - stretchAndFoldVolumeDecrease)
								* stretchAndFoldStage.volumeDecrease;
						}
						volumeExpansionRatio -= stretchAndFoldVolumeDecrease;

						//early exit if target volume expansion ratio references an inner stage
						if(i == procedure.targetVolumeExpansionRatioAtLeaveningStage)
							break;
					}
				}

				final double ingredientsFactor = ingredientsFactor(currentStage.temperature, atmosphericPressure);
				if(ingredientsFactor == 0.)
					return Double.POSITIVE_INFINITY;

				//NOTE: last `stage.volumeDecrease` is NOT taken into consideration!
				volumeExpansionRatio += yeastModel.volumeExpansionRatio(duration.plus(currentStage.duration).toMinutes() / 60., lambda,
					alpha, currentStage.temperature, ingredientsFactor);
				return volumeExpansionRatio * (1. - currentStage.volumeDecrease) - procedure.targetDoughVolumeExpansionRatio;
			};
			yeast = solverYeast.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_YEAST_MAX);
		}
		catch(final NoBracketingException e){
			throw YeastException.create("No yeast quantity will ever be able to produce the given expansion ratio");
		}
		catch(final TooManyEvaluationsException e){
			throw YeastException.create("Cannot calculate yeast quantity, try increasing maximum number of evaluations in the solver");
		}
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
	 * @param yeast	Quantity of yeast [% w/w].
	 * @return	The estimated lag [hrs].
	 */
	public double estimatedLag(final double yeast){
		//transform [% w/w] to [g/l]
		final double s = fractionOverTotal(salt * 10.);
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
	 * @param temperature	Temperature [°C].
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 * @return	Factor to be applied to maximum specific growth rate.
	 */
	private double ingredientsFactor(final double temperature, final double atmosphericPressure){
		final double kSugar = sugarFactor(temperature);
		final double kFat = fatFactor();
		final double kSalt = saltFactor();
		final double kWater = waterFactor();
		final double kWaterChlorineDioxide = waterChlorineDioxideFactor();
		final double kWaterPH = waterPHFactor(temperature);
		final double kWaterFixedResidue = waterFixedResidueFactor();
		final double kHydration = kWater * kWaterChlorineDioxide * kWaterPH * kWaterFixedResidue;
		final double kAtmosphericPressure = atmosphericPressureFactor(atmosphericPressure);
		return kSugar * kFat * kSalt * kHydration * kAtmosphericPressure;
	}

	/**
	 * @see <a href="https://uwaterloo.ca/chem13-news-magazine/april-2015/activities/fermentation-sugars-using-yeast-discovery-experiment">The fermentation of sugars using yeast: A discovery experiment</a>
	 * @see <a href="https://www.bib.irb.hr/389483/download/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlic, Querol, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzevii and their interspecific hybrid. 2009.</a>
	 * @see <a href="http://www.biologydiscussion.com/industrial-microbiology-2/yeast-used-in-bakery-foods/yeast-used-in-bakery-foods-performance-determination-forms-effect-industrial-microbiology/86555">Yeast used in bakery foods: Performance, determination, forms & effect. Industrial Microbiology</a>
	 * @see <a href="https://bib.irb.hr/datoteka/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlića, Querolb, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzeviiand their interspecific hybrid. 2009.</a>
	 *
	 * @param temperature	Temperature [°C].
	 * @return	Correction factor.
	 */
	double sugarFactor(final double temperature){
		/**
		 * base is pH 5.4±0.1, 20 mg/l glucose
		 * @see io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast#getMaximumSpecificGrowthRate()
		 */
		final double basePH = 5.4;
		final double baseSugar = 20. / 1000.;
		final double baseMu = (0.3945 + (-0.00407 + 0.0000096 * baseSugar) * baseSugar
			+ (-0.00375 + 0.000025 * baseSugar) * temperature
			+ (0.003 - 0.00002 * baseSugar) * basePH
			) / 3.;

		return (0.3945 + (-0.00407 + 0.0000096 * sugar) * sugar
			+ (-0.00375 + 0.000025 * sugar) * temperature
			+ (0.003 - 0.00002 * sugar) * waterPH
		) / (3. * baseMu);
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
	 * @see <a href="https://www.microbiologyresearch.org/docserver/fulltext/micro/64/1/mic-64-1-91.pdf">Watson. Effects of Sodium Chloride on Steady-state Growth and Metabolism of Saccharomyces cerevisiae. 1970. Journal of General Microbiology. Vol 64.</a>
	 * @see <a href="https://aem.asm.org/content/aem/43/4/757.full.pdf">Wei, Tanner, Malaney. Effect of Sodium Chloride on baker's yeast growing in gelatin. 1981. Applied and Environmental Microbiology. Vol. 43, No. 4.</a>
	 * @see <a href="https://meridian.allenpress.com/jfp/article/70/2/456/170132/Use-of-Logistic-Regression-with-Dummy-Variables">López, Quintana, Fernández. Use of logistic regression with dummy variables for modeling the growth–no growth limits of Saccharomyces cerevisiae IGAL01 as a function of Sodium chloride, acid type, and Potassium Sorbate concentration according to growth media. 2006. Journal of Food Protection. Vol 70, No. 2.</a>
	 * @see <a href="https://undergradsciencejournals.okstate.edu/index.php/jibi/article/view/2512">Lenaburg, Kimmons, Kafer, Holbrook, Franks. Yeast Growth: The effect of tap water and distilled water on yeast fermentation with salt additives. 2016.</a>
	 * @see <a href="https://meridian.allenpress.com/jfp/article/71/7/1412/172677/Individual-Effects-of-Sodium-Potassium-Calcium-and">Bautista-Gallego, Arroyo-López, Durán-Quintana, Garrido-Fernández. Individual Effects of Sodium, Potassium, Calcium, and Magnesium Chloride Salts on Lactobacillus pentosus and Saccharomyces cerevisiae Growth. 2008.</a>
	 * @see <a href="https://onlinelibrary.wiley.com/doi/abs/10.1002/jsfa.4575">Beck, Jekle, Becker. Impact of sodium chloride on wheat flour dough for yeast-leavened products. II. Baking quality parameters and their relationship. 2010.</a>
	 *
	 * @return	Correction factor.
	 */
	double saltFactor(){
		final double x = 11.7362 * salt;
		final double a = (Double.isInfinite(Math.exp(x))? 1. - 0.0256 * x: 1. - Math.log(Math.pow(1. + Math.exp(x), 0.0256)));
		final double b = salt / (87.5679 - 0.2725 * salt);
		return Math.exp(a * b);
	}

	/**
	 * https://buonapizza.forumfree.it/?t=75686746
	 * @see <a href="https://www.nature.com/articles/s41598-018-36786-2.pdf">Minervini, Dinardo, de Angelis, Gobbetti. Tap water is one of the drivers that establish and assembly the lactic acid bacterium biota during sourdough preparation. 2018.</a>
	 * @see <a href="http://fens.usv.ro/index.php/FENS/article/download/328/326">Codina, Mironeasa, Voica. Influence of wheat flour dough hydration levels on gas production during dough fermentation and bread quality. 2011. Journal of Faculty of Food Engineering. Vol. X, Issue 4.</a>
	 *
	 * @return	Correction factor.
	 */
	double waterFactor(){
		return (HYDRATION_MIN <= water && water < HYDRATION_MAX? Helper.evaluatePolynomial(WATER_COEFFICIENTS, water): 0.);
	}

	/**
	 * https://academic.oup.com/mutage/article/19/2/157/1076450
	 * Buschini, Carboni, Furlini, Poli, Rossi. sodium hypochlorite-, chlorine dioxide- and peracetic acid-induced genotoxicity detected by Saccharomyces cerevisiae tests [2004]
	 *
	 * @return	Correction factor.
	 */
	double waterChlorineDioxideFactor(){
		return Math.max(1. - waterChlorineDioxide * fractionOverTotal(water) / WATER_CHLORINE_DIOXIDE_MAX, 0.);
	}

	/**
	 * @see <a href="https://academic.oup.com/femsyr/article/15/2/fou005/534737">Peña, Sánchez, Álvarez, Calahorra, Ramírez. Effects of high medium pH on growth, metabolism and transport in Saccharomyces cerevisiae. 2015.</a>
	 * @see <a href="https://oatao.univ-toulouse.fr/1556/1/Serra_1556.pdf">Serra, Strehaiano, Taillandier. Influence of temperature and pH on Saccharomyces bayanus var. uvarum growth; impact of a wine yeast interspecifichy bridization on these parameters. 2005.</a>
	 * @see <a href="https://bib.irb.hr/datoteka/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlića, Querolb, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzeviiand their interspecific hybrid. 2009.</a>
	 *
	 * @param temperature	Temperature [°C].
	 * @return	Correction factor.
	 */
	private double waterPHFactor(final double temperature){
		/**
		 * base is pH 5.4±0.1, 20 mg/l glucose
		 * @see io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast#getMaximumSpecificGrowthRate()
		 */
		final double basePH = 5.4;
		final double baseSugar = 20. / 1000.;
		final double baseMu = (0.22
			+ (0.42625 + (-0.301 + 0.052 * basePH) * basePH)
			+ (-0.026125 + 0.0095 * basePH) * temperature
			+ (0.00011 - 0.00004 * basePH) * baseSugar
		) / 9.;

		return Math.max((0.22
			+ (0.42625 + (-0.301 + 0.052 * waterPH) * waterPH)
				+ (-0.026125 + 0.0095 * waterPH) * temperature
				+ (0.00011 - 0.00004 * waterPH) * sugar
		) / (9. * baseMu), 0.);
	}

	/**
	 * TODO Se la durezza dell’acqua è troppo elevata la fermentazione subisce rallentamenti a causa della formazione di una struttura glutinica troppo rigida. In caso contrario, dove la durezza dell’acqua risulta essere troppo scarsa, l’impasto si presenta assai appiccicoso e poco manipolabile. In questo frangente sarà utile abbassare l’idratazione.
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
	double atmosphericPressureFactor(final double atmosphericPressure){
		return (atmosphericPressure < ATMOSPHERIC_PRESSURE_MAX?
			1. - PRESSURE_FACTOR_K * Math.pow(atmosphericPressure / Math.pow(10_000., 2.), PRESSURE_FACTOR_M): 0.);
	}

	private double fractionOverTotal(final double value){
		return value / (1. + water);
	}

	private Recipe calculateIngredients(final Ingredients ingredients, final double doughWeight){
		final double totalFraction = 1. + water + sugar + yeast + salt + fat;
		double totalFlour = doughWeight / totalFraction;
		double yeast, flour, water, sugar, fat, salt,
			difference;
		final double waterCorrection = calculateWaterCorrection(ingredients);
		do{
			yeast = totalFlour * this.yeast / (yeastType.factor * rawYeast);
			flour = totalFlour - yeast * (1. - rawYeast);
			water = Math.max(totalFlour * this.water - waterCorrection, 0.);
			sugar = totalFlour * this.sugar / (sugarType.factor * sugarContent);
			final double fatCorrection = calculateFatCorrection(ingredients, flour);
			fat = Math.max(totalFlour * this.fat - fatCorrection, 0.) / fatContent;
			final double saltCorrection = calculateSaltCorrection(ingredients, flour);
			salt = Math.max(totalFlour * this.salt - saltCorrection, 0.);

			//refine approximation:
			final double calculatedDough = flour + water + yeast + sugar + salt + fat;
			difference = doughWeight - calculatedDough;
			totalFlour += difference * 0.6;
		}while(Math.abs(difference) > DOUGH_WEIGHT_PRECISION);

		//calculate water temperature:
		final Double waterTemperature = (ingredients.doughTemperature != null && ingredients.ingredientsTemperature != null?
			(doughWeight * ingredients.doughTemperature - (doughWeight - water) * ingredients.ingredientsTemperature) / water:
			null);
		if(waterTemperature != null && waterTemperature >= yeastModel.getTemperatureMax())
			LOGGER.warn("Water temperature ({} °C) is greater that maximum temperature sustainable by the yeast ({} °C), be aware of thermal shock!",
				Helper.round(waterTemperature, 1), Helper.round(yeastModel.getTemperatureMax(), 1));

		return Recipe.create()
			.withFlour(flour)
			.withWater(water, waterTemperature)
			.withYeast(yeast)
			.withSugar(sugar)
			.withFat(fat)
			.withSalt(salt);
	}

	private double calculateWaterCorrection(final Ingredients ingredients){
		double waterCorrection = 0.;
		if(ingredients.correctForIngredients)
			waterCorrection += sugar * sugarWaterContent + fat * fatWaterContent;
		if(ingredients.correctForFlourHumidity)
			//NOTE: 70.62% is to obtain a humidity of 13.5%
			waterCorrection += Flour.estimatedHumidity(ingredients.airRelativeHumidity) - Flour.estimatedHumidity(0.7062);
		return waterCorrection;
	}

	private double calculateFatCorrection(final Ingredients ingredients, final double flour){
		return (ingredients.correctForIngredients? flour * ingredients.flour.fatContent: 0.);
	}

	private double calculateSaltCorrection(final Ingredients ingredients, final double flour){
		return (ingredients.correctForIngredients? flour * ingredients.flour.saltContent + fat * fatSaltContent: 0.);
	}


	/**
	 * @see <a href="https://www.academia.edu/2421508/Characterisation_of_bread_doughs_with_different_densities_salt_contents_and_water_levels_using_microwave_power_transmission_measurements">Campbell. Characterisation of bread doughs with different densities, salt contents and water levels using microwave power transmission measurements. 2005.</a>
	 * @see <a href="https://core.ac.uk/download/pdf/197306213.pdf">Kubota, Matsumoto, Kurisu, Sizuki, Hosaka. The equations regarding temperature and concentration of the density and viscosity of sugar, salt and skim milk solutions. 1980.</a>
	 * @see <a href="https://shodhganga.inflibnet.ac.in/bitstream/10603/149607/15/10_chapter%204.pdf">Density studies of sugar solutions</a>
	 * @see <a href="https://www.researchgate.net/publication/280063894_Mathematical_modelling_of_density_and_viscosity_of_NaCl_aqueous_solutions">Simion, Grigoras, Rosu, Gavrila. Mathematical modelling of density and viscosity of NaCl aqueous solutions. 2014.</a>
	 * @see <a href="https://www.researchgate.net/publication/233266779_Temperature_and_Concentration_Dependence_of_Density_of_Model_Liquid_Foods">Darros-Barbosa, Balaban, Teixeira.Temperature and concentration dependence of density of model liquid foods. 2003.</a>
	 *
	 * @param flour	Flour weight [g].
	 * @param dough	Final dough weight [g].
	 * @param fatDensity	Density of the fat [kg/l].
	 * @param temperature	Temperature of the dough [°C].
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 */
	double density(final double flour, final double dough, final double fatDensity, final double temperature,
			final double atmosphericPressure){
		//TODO
		//density of flour + salt + sugar + water
		double doughDensity = 1.41
			- 0.00006762 * atmosphericPressure
			+ 0.00640 * salt
//			+ 0.00746 * salt - 0.000411 * (doughTemperature + ABSOLUTE_ZERO)
//			+ 0.000426 * sugar - 0.000349 * (doughTemperature + ABSOLUTE_ZERO)
			- 0.00260 * water;

		final double pureWaterDensity = 999.84259 + (6.793952e-2 + (-9.09529e-3 + (1.001685e-4 + (-1.120083e-6 + 6.536332e-9 * temperature)
			* temperature) * temperature) * temperature) * temperature;

		//account for fat
		final double fraction = fat * flour / dough;
		return 1. / ((1. - fraction) / doughDensity + fraction / fatDensity);
	}

}
