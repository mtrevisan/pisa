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

import io.github.mtrevisan.pizza.utils.Helper;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaePedonYeast;
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import java.time.Duration;
import java.time.LocalTime;


//effect of ingredients!! https://www.maltosefalcons.com/blogs/brewing-techniques-tips/yeast-propagation-and-maintenance-principles-and-practices
public final class Dough2{

	/** Standard ambient pressure [hPa]. */
	private static final double STANDARD_AMBIENT_PRESSURE = 1013.25;

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


	private Flour flour;

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

	/** Total sugar (glucose) quantity w.r.t. flour [% w/w]. */
	private double sugar;
	private SugarType sugarType;
	/** Raw sugar content [% w/w]. */
	private double rawSugar = 1.;
	/** Water content in sugar [% w/w]. */
	private double sugarWaterContent;

	/** Total fat quantity w.r.t. flour [% w/w]. */
	private double fat;
	/** Fat density [g / ml]. */
	private double fatDensity;

	/** Total salt quantity w.r.t. flour [% w/w]. */
	private double salt;

	private final YeastModelAbstract yeastModel;
	/** Yeast quantity [% w/w]. */
	private double yeast;
	private YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	private double rawYeast = 1.;


	/** Atmospheric pressure [hPa]. */
	private double atmosphericPressure = STANDARD_AMBIENT_PRESSURE;


	public static Dough2 create(final YeastModelAbstract yeastModel) throws DoughException{
		return new Dough2(yeastModel);
	}


	private Dough2(final YeastModelAbstract yeastModel) throws DoughException{
		if(yeastModel == null)
			throw DoughException.create("A yeast model must be provided");

		this.yeastModel = yeastModel;
	}


	/**
	 * @param flour	Flour data.
	 * @return	The instance.
	 */
	public Dough2 withFlourParameters(final Flour flour) throws DoughException{
		if(flour == null)
			throw DoughException.create("Missing flour");

		this.flour = flour;

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
	public Dough2 addWater(final double water, final double chlorineDioxide, final double calciumCarbonate, final double pH,
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
	 * @param sugar	Sugar quantity w.r.t. flour [% w/w].
	 * @param sugarType	Sugar type.
	 * @param sugarContent	Sucrose content [% w/w].
	 * @param waterContent	Water content [% w/w].
	 * @return	This instance.
	 * @throws DoughException	If sugar is too low or too high.
	 */
	public Dough2 addSugar(final double sugar, final SugarType sugarType, final double sugarContent, final double waterContent)
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
	 * @param yeastType	Yeast type.
	 * @param rawYeast	Raw yeast content [% w/w].
	 * @return	The instance.
	 */
	public Dough2 withYeastParameters(final YeastType yeastType, final double rawYeast) throws DoughException{
		if(yeastType == null)
			throw DoughException.create("Missing yeast type");
		if(rawYeast <= 0. || rawYeast > 1.)
			throw DoughException.create("Raw yeast quantity must be between 0 and 1");

		this.yeastType = yeastType;
		this.rawYeast = rawYeast;

		return this;
	}


	/**
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 * @return	This instance.
	 * @throws DoughException	If pressure is negative or above maximum.
	 */
	public Dough2 withAtmosphericPressure(final double atmosphericPressure) throws DoughException{
		if(atmosphericPressure < 0. || atmosphericPressure >= ATMOSPHERIC_PRESSURE_MAX)
			throw DoughException.create("Atmospheric pressure [hPa] must be between 0 and {} hPa",
				Helper.round(ATMOSPHERIC_PRESSURE_MAX, 1));

		this.atmosphericPressure = atmosphericPressure;

		return this;
	}


	/**
	 * @param procedure	The recipe procedure.
	 * @return	The recipe.
	 */
	public void createRecipe(final Procedure procedure) throws YeastException{
		calculateYeast(procedure);

		//true yeast quantity
		yeast /= rawYeast * yeastType.factor;
	}

	public static void main(String[] args) throws DoughException, YeastException{
		Dough2 dough = Dough2.create(new SaccharomycesCerevisiaePedonYeast())
			.withFlourParameters(Flour.create(230., 0.001, 0.0008, 1.3))
			.addWater(0.6, 0.02, 0., 7.9, 237.)
			.addSugar(0.015, SugarType.SUCROSE, 0.998, 0.0005)
//			.addFat(0.014, 0.913, 0.9175, 0., 0.002)
//			.addSalt(0.015)
			.withYeastParameters(YeastType.FRESH, 1.);
		LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		LeaveningStage stage2 = LeaveningStage.create(20., Duration.ofHours(1l));
		Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(20, 15));
		dough.calculateYeast(procedure);

		System.out.println("yeast = " + dough.yeast + "%");
	}

	/**
	 * Find the initial yeast able to obtain a given volume expansion ratio after a series of consecutive stages at a given duration at
	 * temperature.
	 *
	 * @param procedure	Data for procedure.
	 */
	void calculateYeast(final Procedure procedure) throws YeastException{
		//reset variable
		yeast = 0.;

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
		//that is, k = 1.2 / (yeastModel.volumeExpansionRatio(1., lambda, alpha, (27. + 30.) / 2., 1.) * 0.04)
		final double k = 13.7;
		return 1. + k * volumeExpansionRatio * yeast;
	}

	/**
	 * Maximum relative volume expansion ratio.
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 *
	 * @param yeast	Quantity of yeast [% w/w].
	 * @return	The maximum relative volume expansion ratio (∆V / V).
	 */
	private double maximumRelativeVolumeExpansionRatio(final double yeast){
		//FIXME this formula is for 36±1 °C
		//vertex must be at 1.1%
		return (yeast < 0.011? 24_546. * (0.022 - yeast) * yeast: 2.97);
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
	private double ingredientsFactor(final double yeast, final double temperature){
		//TODO calculate ingredientsFactor (account for water and sugar at least)
//		final double kSugar = sugarFactor(yeast, temperature);
////		final double kFat = fatFactor();
//		final double kSalt = saltFactor(yeast, temperature);
//		final double kWater = waterFactor(yeast, temperature);
////		final double kWaterFixedResidue = waterFixedResidueFactor();
////		final double kHydration = kWater * kWaterFixedResidue;
//		final double kPH = phFactor();
//		final double kAtmosphericPressure = atmosphericPressureFactor(atmosphericPressure);
//		return /*kSugar * kFat * */kSalt * /*kHydration * */kPH * kAtmosphericPressure;
		return 1.;
	}

}
