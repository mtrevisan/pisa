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
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;


public class Yeast{

	public static final double SUGAR_MAX = Math.exp(-0.3154 / 0.403);

	//[%]
	public static final double HYDRATION_MIN = (7.65 - Math.sqrt(Math.pow(7.65, 2.) - 4. * 6.25 * 1.292)) / (2. * 6.25);
	//[%]
	public static final double HYDRATION_MAX = (7.65 + Math.sqrt(Math.pow(7.65, 2.) - 4. * 6.25 * 1.292)) / (2. * 6.25);

	public static final double CHLORINE_DIOXIDE_MAX = 0.0931;

	private static final double PRESSURE_FACTOR_K = 1.46;
	private static final double PRESSURE_FACTOR_M = 2.031;
	//[hPa]
	public static final double MINIMUM_INHIBITORY_PRESSURE = Math.pow(10_000., 2.) * Math.pow(1. / PRESSURE_FACTOR_K, (1. / PRESSURE_FACTOR_M));

	private static final double[] SUGAR_COEFFICIENTS = new double[]{1., 4.9, -50.};

	private static final double[] SALT_COEFFICIENTS = new double[]{-0.05, -45., -1187.5};

	private static final double[] WATER_COEFFICIENTS = new double[]{-1.292, 7.65, -6.25};

	//densities: http://www.fao.org/3/a-ap815e.pdf

	//Volume factor after each kneading, corresponding to a new stage (V_i = V_i-1 * (1 - VOLUME_REDUCTION)) [%]
	private static final double VOLUME_REDUCTION = 1. - 0.4187;


	private final BracketingNthOrderBrentSolver solverYeast = new BracketingNthOrderBrentSolver(0.000_1, 5);
	private final BracketingNthOrderBrentSolver solverDuration = new BracketingNthOrderBrentSolver(0.06, 5);

	private YeastModelAbstract yeastModel;


	public Yeast(final YeastModelAbstract yeastModel){
		this.yeastModel = yeastModel;
	}

	//https://planetcalc.com/5992/
	//TODO time[hrs] from FY[%] @ 25 °C: time[hrs] = 0.0665 * Math.pow(FY[%], -0.7327)
	//FY[%] = Math.pow(time[hrs] / 0.0665, 1. / -0.7327)
	//https://www.pizzamaking.com/forum/index.php?topic=22649.20
	//https://www.pizzamaking.com/forum/index.php?topic=26831.0

	/**
	 * Find the yeast able to obtain a given volume expansion ratio after two consecutive stages at a given duration at temperature.
	 *
	 * @param sugar	Sugar content [%].
	 * @param fat	Fat content [%].
	 * @param salinity	Salt content [%].
	 * @param hydration	Hydration [%].
	 * @param chlorineDioxide	Chlorine dioxide quantity [mg/l].
	 * @param pressure	Ambient pressure [hPa].
	 * @param stage1	Data for stage 1.
	 * @param stage2	Data for stage 2.
	 * @return	Yeast to use at first stage [%].
	 */
	public double backtrackStage(final double sugar, final double fat, final double salinity, final double hydration,
			final double chlorineDioxide, final double pressure, final LeaveningStage stage1, final LeaveningStage stage2){
		//[%]
		final double maxYeast = 1.;
		final double maxTargetValue = 2.;
		//[hrs]
		final double maxDuration = 100.;

		//find the maximum volume expansion ratio
		final double targetValue = Math.min(0.9 * volumeExpansionRatio(maxYeast, stage2.temperature, sugar, fat, salinity, hydration,
			chlorineDioxide, pressure, stage2.duration), maxTargetValue);

		//find the yeast at stage 2 able to generate a volume of `targetValue` in time `stage2.duration` at temperature `stage2.temperature`
		final UnivariateFunction f2 = yeast -> (volumeExpansionRatio(yeast, stage2.temperature, sugar, fat, salinity, hydration,
			chlorineDioxide, pressure, stage2.duration) - targetValue);
		final double yeast2 = solverYeast.solve(100, f2, 0., maxYeast);

		//find the duration at `stage1.temperature` able to generate a volume of `targetValue` with yeast quantity `yeast2` at
		//temperature `stage1.temperature`
		final UnivariateFunction f12 = duration -> (volumeExpansionRatio(yeast2, stage1.temperature, sugar, fat, salinity, hydration,
			chlorineDioxide, pressure, duration) - targetValue);
		final double duration12 = solverDuration.solve(100, f12, 0., maxDuration);

		//find the yeast at stage 1 able to generate a volume of `targetValue` in time `duration12 + stage1.duration` at temperature
		//`stage1.temperature`
		final UnivariateFunction f1 = yeast -> (volumeExpansionRatio(yeast, stage1.temperature, sugar, fat, salinity, hydration,
			chlorineDioxide, pressure, duration12 + stage1.duration) - targetValue);
		final double yeast1 = solverYeast.solve(100, f1, 0., maxYeast);

		return yeast1;
	}

	/**
	 * Calculate the volume expansion ratio.
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Romano, Toraldo, Cavella, Masi. Description of leavening of bread dough with mathematical modelling. 2007.</a>
	 *
	 * @param yeast	Quantity of yeast [%].
	 * @param temperature	Temperature [°C].
	 * @param sugar	Sugar content [%].
	 * @param fat	Fat content [%].
	 * @param salinity	Salt content [%].
	 * @param hydration	Hydration [%].
	 * @param chlorineDioxide	Chlorine dioxide quantity [mg/l].
	 * @param pressure	Ambient pressure [hPa].
	 * @param leaveningDuration	Leavening duration [hrs].
	 * @return	The volume expansion ratio.
	 */
	double volumeExpansionRatio(final double yeast, final double temperature, final double sugar, final double fat, final double salinity,
			final double hydration, final double chlorineDioxide, final double pressure, final double leaveningDuration){
		final double alpha = maximumRelativeVolumeExpansionRatio(yeast);
		final double lambda = estimatedLag(yeast);
		final double ingredientsFactor = ingredientsFactor(sugar, fat, salinity, hydration, chlorineDioxide, pressure);

		return yeastModel.volumeExpansionRatio(leaveningDuration, lambda, alpha, temperature, ingredientsFactor);
	}

	/**
	 * Maximum relative volume expansion ratio.
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 *
	 * @param yeast	Quantity of yeast [%].
	 * @return	The estimated lag [hrs].
	 */
	public double maximumRelativeVolumeExpansionRatio(final double yeast){
		//FIXME this formula is for 36±1 °C
		//vertex must be at 1.1%
		return (yeast < 0.011? 24_546. * (0.022 - yeast) * yeast: 2.97);
	}

	/**
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 *
	 * @param yeast	Quantity of yeast [%].
	 * @return	The estimated lag [hrs].
	 */
	public double estimatedLag(final double yeast){
		//FIXME this formula is for 36±1 °C
		return 0.0068 * Math.pow(yeast, -0.937);
	}

	/**
	 * Tinf.
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 *
	 * @param yeast	Quantity of yeast [%].
	 * @return	The estimated exhaustion time [hrs].
	 */
	public double estimatedExhaustion(final double yeast){
		//FIXME this formula is for 36±1 °C
		return 0.0596 * Math.pow(yeast, -0.756);
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	The time to reach the plateau of maximum carbon dioxide production [hrs].
	 */
	//FIXME do something
	public double carbonDioxidePlateau(final double temperature){
		final double tMin = yeastModel.getTemperatureMin();
		final double ln = Math.log((temperature - tMin) / (yeastModel.getTemperatureMax() - tMin));
//		final double lag = -(15.5 + (4.6 + 50.63 * ln) * ln) * ln;
		return -(91.34 + (29 + 20.64 * ln) * ln) * ln / 60.;
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
	 *    <li>altitude (air pressure)</li>
	 *    <li>water chemistry (level of chlorination especially)</li>
	 *    <li>container material and thickness (conductivity if ambient and dough temperatures vary, along with heat dissipation from fermentation) (*)</li>
	 *    <li>flour chemistry (enzyme activity, damaged starch, etc.) (*)</li>
	 * </ul>
	 * </p>
	 *
	 * @param sugar	Sugar content [%].
	 * @param fat	Fat content [%].
	 * @param salinity	Salt content [%].
	 * @param hydration	Hydration [%].
	 * @param chlorineDioxide	Chlorine dioxide quantity [mg/l].
	 * @param pressure	Ambient pressure [hPa].
	 */
	private double ingredientsFactor(final double sugar, final double fat, final double salinity, final double hydration,
			final double chlorineDioxide, final double pressure){
		final double kSugar = sugarFactor(sugar);
		final double kFat = fatFactor(fat);
		final double kSalt = saltFactor(salinity);
		final double kWater = waterFactor(hydration);
		final double kChlorineDioxide = chlorineDioxideFactor(chlorineDioxide);
		final double kAirPressure = airPressureFactor(pressure);
		return kSugar * kFat * kSalt * kWater * kChlorineDioxide * kAirPressure;
	}

	/**
	 * @see <a href="https://uwaterloo.ca/chem13-news-magazine/april-2015/activities/fermentation-sugars-using-yeast-discovery-experiment">The fermentation of sugars using yeast: A discovery experiment</a>
	 * @see <a href="https://www.bib.irb.hr/389483/download/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlic, Querol, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzevii and their interspecific hybrid. 2009.</a>
	 * @see <a href="http://www.biologydiscussion.com/industrial-microbiology-2/yeast-used-in-bakery-foods/yeast-used-in-bakery-foods-performance-determination-forms-effect-industrial-microbiology/86555">Yeast used in bakery foods: Performance, determination, forms & effect. Industrial Microbiology</a>
	 *
	 * @param sugar	Sugar quantity [%].
	 * @return	Correction factor.
	 */
	double sugarFactor(final double sugar){
		if(sugar < 0.03)
			return Math.min(Helper.evaluatePolynomial(SUGAR_COEFFICIENTS, sugar), 1.);
		if(sugar < SUGAR_MAX)
			return -0.3154 - 0.403 * Math.log(sugar);
		return 0.;
	}

	/**
	 * TODO high fat content inhibits leavening
	 *
	 * @param fat	Fat quantity [%].
	 * @return	Correction factor.
	 */
	double fatFactor(final double fat){
		//0 <= fat <= ??%
		final double maxFat = 0.;
		//1+fat/300...?
		return 1.;
	}

	/**
	 * @see <a href="https://www.microbiologyresearch.org/docserver/fulltext/micro/64/1/mic-64-1-91.pdf">Watson. Effects of Sodium Chloride on Steady-state Growth and Metabolism of Saccharomyces cerevisiae. 1970. Journal of General Microbiology. Vol 64.</a>
	 * @see <a href="https://aem.asm.org/content/aem/43/4/757.full.pdf">Wei, Tanner, Malaney. Effect of Sodium Chloride on baker's yeast growing in gelatin. 1981. Applied and Environmental Microbiology. Vol. 43, No. 4.</a>
	 * @see <a href="https://watermark.silverchair.com/0362-028x-70_2_456.pdf">López, Quintana, Fernández. Use of logistic regression with dummy variables for modeling the growth–no growth limits of Saccharomyces cerevisiae IGAL01 as a function of Sodium chloride, acid type, and Potassium Sorbate concentration according to growth media. 2006. Journal of Food Protection. Vol 70, No. 2.</a>
	 *
	 * @param salinity	Salt quantity [%].
	 * @return	Correction factor.
	 */
	double saltFactor(final double salinity){
		return Math.max(1. + Helper.evaluatePolynomial(SALT_COEFFICIENTS, salinity), 0.);
	}

	/**
	 * https://buonapizza.forumfree.it/?t=75686746
	 * Minervini, Dinardo, de Angelis, Gobbetti. Tap water is one of the drivers that establish and assembly the lactic acid bacterium biota during sourdough preparation. 2018. (https://www.nature.com/articles/s41598-018-36786-2.pdf)
	 * Codina, Mironeasa, Voica. Influence of wheat flour dough hydration levels on gas production during dough fermentation and bread quality. 2011. Journal of Faculty of Food Engineering. Vol. X, Issue 4. (http://fens.usv.ro/index.php/FENS/article/download/328/326)
	 *
	 * @param hydration	Hydration quantity [%].
	 * @return	Correction factor.
	 */
	double waterFactor(final double hydration){
		return (HYDRATION_MIN <= hydration && hydration < HYDRATION_MAX? Helper.evaluatePolynomial(WATER_COEFFICIENTS, hydration): 0.);
	}

	/**
	 * https://academic.oup.com/mutage/article/19/2/157/1076450
	 * Buschini, Carboni, Furlini, Poli, Rossi. sodium hypochlorite-, chlorine dioxide- and peracetic acid-induced genotoxicity detected by Saccharomyces cerevisiae tests [2004]
	 *
	 * @param chlorineDioxide	Chlorine dioxide quantity [mg/l].
	 * @return	Correction factor.
	 */
	double chlorineDioxideFactor(final double chlorineDioxide){
		return Math.max(1. - chlorineDioxide / CHLORINE_DIOXIDE_MAX, 0.);
	}

	/**
	 * Arao, Hara, Suzuki, Tamura. Effect of High-Pressure Gas on io.github.mtrevisan.pizza.Yeast Growth. 2014. (https://www.tandfonline.com/doi/pdf/10.1271/bbb.69.1365)
	 *
	 * @param pressure	Ambient pressure [hPa].
	 * @return	Correction factor.
	 */
	double airPressureFactor(final double pressure){
		return (pressure < MINIMUM_INHIBITORY_PRESSURE? 1. - PRESSURE_FACTOR_K * Math.pow(pressure / Math.pow(10_000., 2.),
			PRESSURE_FACTOR_M): 0.);
	}

}
