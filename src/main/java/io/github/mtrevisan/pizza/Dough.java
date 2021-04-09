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


public class Dough{

	//standard atmosphere [hPa]
	public static final double ONE_ATMOSPHERE = 1013.25;

	/**
	 * @see #sugarFactor()
	 */
	private static final double[] SUGAR_COEFFICIENTS = new double[]{1., 4.9, -50.};
	/**
	 * @see #sugarFactor()
	 * @see #SUGAR_COEFFICIENTS
	 */
	public static final double SUGAR_MAX = Math.exp(-0.3154 / 0.403);

	/**
	 * @see #saltFactor()
	 * @see #SALT_MAX
	 */
	private static final double[] SALT_COEFFICIENTS = new double[]{1., -0.05, -45., -1187.5};
	/**
	 * @see #saltFactor()
	 * @see #SALT_COEFFICIENTS
	 */
	public static final double SALT_MAX = 0.08321;

	/**
	 * @see #waterFactor()
	 * @see #HYDRATION_MIN
	 * @see #HYDRATION_MAX
	 */
	private static final double[] WATER_COEFFICIENTS = new double[]{-1.292, 7.65, -6.25};
	/**
	 * [%]
	 *
	 * @see #WATER_COEFFICIENTS
	 * @see #waterFactor()
	 */
	public static final double HYDRATION_MIN = (7.65 - Math.sqrt(Math.pow(7.65, 2.) - 4. * 6.25 * 1.292)) / (2. * 6.25);
	/**
	 * [%]
	 *
	 * @see #WATER_COEFFICIENTS
	 * @see #waterFactor()
	 */
	public static final double HYDRATION_MAX = (7.65 + Math.sqrt(Math.pow(7.65, 2.) - 4. * 6.25 * 1.292)) / (2. * 6.25);

	/**
	 * @see #chlorineDioxideFactor()
	 */
	public static final double CHLORINE_DIOXIDE_MAX = 0.0931;

	/**
	 * @see #atmosphericPressureFactor()
	 * @see #ATMOSPHERIC_PRESSURE_MAX
	 */
	private static final double PRESSURE_FACTOR_K = 1.46;
	/**
	 * @see #atmosphericPressureFactor()
	 * @see #ATMOSPHERIC_PRESSURE_MAX
	 */
	private static final double PRESSURE_FACTOR_M = 2.031;
	/**
	 * Minimum inhibitory pressure [hPa].
	 *
	 * @see #atmosphericPressureFactor()
	 * @see #PRESSURE_FACTOR_K
	 * @see #PRESSURE_FACTOR_M
	 */
	public static final double ATMOSPHERIC_PRESSURE_MAX = Math.pow(10_000., 2.) * Math.pow(1. / PRESSURE_FACTOR_K, (1. / PRESSURE_FACTOR_M));

	/**
	 * [%]
	 *
	 * @see #backtrackStages(LeaveningStage...)
	 * @see #calculateEquivalentDuration(double, LeaveningStage, LeaveningStage, double)
	 */
	private static final double MAX_YEAST = 1.;
	/**
	 * @see #backtrackStages(LeaveningStage...)
	 */
	private static final double MAX_TARGET_VOLUME_EXPANSION_RATIO = 2.;
	/**
	 * [hrs]
	 *
	 * @see #calculateEquivalentDuration(double, LeaveningStage, LeaveningStage, double)
	 */
	private static final double MAX_DURATION = 100.;

	//densities: http://www.fao.org/3/a-ap815e.pdf
	//plot graphs: http://www.shodor.org/interactivate/activities/SimplePlot/

	//Volume factor after each kneading, corresponding to a new stage (V_i = V_i-1 * (1 - STRETCH_AND_FOLD_VOLUME_REDUCTION)) [%]
	private static final double STRETCH_AND_FOLD_VOLUME_REDUCTION = 1. - 0.4187;


	//accuracy is ±0.001%
	private final BracketingNthOrderBrentSolver solverYeast = new BracketingNthOrderBrentSolver(0.000_01, 5);
	//accuracy is ±1.2 min
	private final BracketingNthOrderBrentSolver solverDuration = new BracketingNthOrderBrentSolver(0.02, 5);


	private final YeastModelAbstract yeastModel;
	//[%]
	private double sugar;
	//[%]
	private double fat;
	//[%]
	private double salt;
	//[%]
	private double hydration;
	//[mg/l]
	private double chlorineDioxide;
	//[hPa]
	private double atmosphericPressure = ONE_ATMOSPHERE;


	//https://planetcalc.com/5992/
	//TODO time[hrs] from FY[%] @ 25 °C: time[hrs] = 0.0665 * Math.pow(FY[%], -0.7327)
	//FY[%] = Math.pow(time[hrs] / 0.0665, 1. / -0.7327)
	//https://www.pizzamaking.com/forum/index.php?topic=22649.20
	//https://www.pizzamaking.com/forum/index.php?topic=26831.0


	public static Dough create(final YeastModelAbstract yeastModel){
		return new Dough(yeastModel);
	}

	private Dough(final YeastModelAbstract yeastModel){
		this.yeastModel = yeastModel;
	}

	public void addSugar(final double sugar, final double sugarContent, final double waterContent){
		this.sugar += sugar * sugarContent;
		hydration += sugar * waterContent;
	}

	public void addFat(final double fat, final double fatContent, final double waterContent, final double saltContent){
		this.fat += fat * fatContent;
		hydration += fat * waterContent;
		salt += fat * saltContent;
	}

	public void addSalt(final double salt){
		this.salt += salt;
	}

	public void addHydration(final double hydration){
		this.hydration += hydration;
	}

	public void withChlorineDioxide(final double chlorineDioxide){
		this.chlorineDioxide = chlorineDioxide;
	}

	public void withAtmosphericPressure(final double atmosphericPressure){
		this.atmosphericPressure = atmosphericPressure;
	}

	public void validate() throws DoughException{
		if(yeastModel == null)
			throw DoughException.create("A yeast model must be provided");
		if(sugar < 0. || sugar > SUGAR_MAX)
			throw DoughException.create("Sugar [%] must be between 0 and "
				+ Helper.round(SUGAR_MAX * 100., 1) + "%");
		if(fat < 0.)
			throw DoughException.create("Fat [%] cannot be less than zero");
		if(salt < 0. || salt >= SALT_MAX)
			throw DoughException.create("Salt [%] must be between 0 and "
				+ Helper.round(SALT_MAX * 100., 3) + "%");
		if(hydration < HYDRATION_MIN || hydration > HYDRATION_MAX)
			throw DoughException.create("Hydration [%] cannot be between "
				+ Helper.round(HYDRATION_MIN * 100., 1)
				+ "% and " + Helper.round(HYDRATION_MAX * 100., 1) + "%");
		if(chlorineDioxide < 0. || chlorineDioxide >= CHLORINE_DIOXIDE_MAX)
			throw DoughException.create("Chlorine dioxide [mg/l] must be between 0 and "
				+ Helper.round(CHLORINE_DIOXIDE_MAX, 2) + " mg/l");
		if(atmosphericPressure <= 0. || atmosphericPressure >= ATMOSPHERIC_PRESSURE_MAX)
			throw DoughException.create("Atmospheric pressure [hPa] must be between 0 and "
				+ Helper.round(ATMOSPHERIC_PRESSURE_MAX, 1) + " hPa");
	}

	/**
	 * Find the initial yeast able to obtain a given volume expansion ratio after a series of consecutive stages at a given duration at
	 * temperature.
	 *
	 * @param stages	Data for stages.
	 * @return	Yeast to use at first stage [%].
	 */
	public double backtrackStages(final LeaveningStage... stages){
		final LeaveningStage lastStage = stages[stages.length - 1];
		//find the maximum volume expansion ratio
		//FIXME 0.9? MAX_TARGET_VOLUME_EXPANSION_RATIO?
		final double targetVolumeExpansionRatio = Math.min(0.9 * volumeExpansionRatio(MAX_YEAST, lastStage.temperature, lastStage.duration),
			MAX_TARGET_VOLUME_EXPANSION_RATIO);

		double previousEquivalentDuration = 0.;
		for(int i = stages.length - 1; i > 0; i --){
			final double duration23 = calculateEquivalentDuration(targetVolumeExpansionRatio, stages[i - 1], stages[i],
				previousEquivalentDuration);
			previousEquivalentDuration += duration23;
		}

		//find the yeast at stage 1 able to generate a volume of `targetVolumeExpansionRatio` in time `duration12 + stage1.duration`
		//at temperature `stage1.temperature`
		final LeaveningStage firstStage = stages[0];
		final double totalEquivalentDuration = firstStage.duration + previousEquivalentDuration;
		return calculateEquivalentYeast(targetVolumeExpansionRatio, firstStage, totalEquivalentDuration);
	}

	/**
	 * Calculate the volume expansion ratio.
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Romano, Toraldo, Cavella, Masi. Description of leavening of bread dough with mathematical modelling. 2007.</a>
	 * @see <a href="http://www.doiserbia.nb.rs/img/doi/1451-9372/2010/1451-93721000029S.pdf">Shafaghat, Najafpour, Rezaei, Sharifzadeh. Optimal growth of Saccharomyces cerevisiaea on pretreated molasses for the ethanol production. 2010.</a>
	 *
	 * @param yeast	Quantity of yeast [%].
	 * @param temperature	Temperature [°C].
	 * @param leaveningDuration	Leavening duration [hrs].
	 * @return	The volume expansion ratio.
	 */
	double volumeExpansionRatio(final double yeast, final double temperature, final double leaveningDuration){
		final double alpha = maximumRelativeVolumeExpansionRatio(yeast);
		final double lambda = estimatedLag(yeast);
		final double ingredientsFactor = ingredientsFactor();

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
	double maximumRelativeVolumeExpansionRatio(final double yeast){
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
	 * Return the equivalent total duration able to generate a given volume expansion ratio at stage 1 temperature.
	 *
	 * @param targetVolumeExpansionRatio	Target maximum volume expansion ratio.
	 * @param stage1	Data for stage 1.
	 * @param stage2	Data for stage 2.
	 * @param previousEquivalentDuration	Previous equivalent duration [hrs].
	 * @return	Duration at first stage [hrs].
	 */
	private double calculateEquivalentDuration(final double targetVolumeExpansionRatio, final LeaveningStage stage1,
			final LeaveningStage stage2, final double previousEquivalentDuration){
		final double yeast2 = calculateEquivalentYeast(targetVolumeExpansionRatio, stage2,
			stage2.duration + previousEquivalentDuration);

		final UnivariateFunction f12 = duration -> (volumeExpansionRatio(yeast2, stage1.temperature, duration) - targetVolumeExpansionRatio);
		return solverDuration.solve(100, f12, 0., MAX_DURATION);
	}

	/**
	 * Return the equivalent yeast able to generate a given volume expansion ratio at stage 1 temperature in a given duration.
	 *
	 * @param targetVolumeExpansionRatio	Target maximum volume expansion ratio.
	 * @param stage	Data for stage.
	 * @param duration	Duration [hrs].
	 * @return	Yeast quantity [%].
	 */
	private double calculateEquivalentYeast(final double targetVolumeExpansionRatio, final LeaveningStage stage, final double duration){
		final UnivariateFunction f = yeast -> (volumeExpansionRatio(yeast, stage.temperature, duration) - targetVolumeExpansionRatio);
		return solverYeast.solve(100, f, 0., MAX_YEAST);
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
	 * @return	Factor to be applied to maximum specific growth rate.
	 */
	private double ingredientsFactor(){
		final double kSugar = sugarFactor();
		final double kFat = fatFactor();
		final double kSalt = saltFactor();
		final double kWater = waterFactor();
		final double kChlorineDioxide = chlorineDioxideFactor();
		final double kAtmosphericPressure = atmosphericPressureFactor();
		return kSugar * kFat * kSalt * kWater * kChlorineDioxide * kAtmosphericPressure;
	}

	/**
	 * @see <a href="https://uwaterloo.ca/chem13-news-magazine/april-2015/activities/fermentation-sugars-using-yeast-discovery-experiment">The fermentation of sugars using yeast: A discovery experiment</a>
	 * @see <a href="https://www.bib.irb.hr/389483/download/389483.Arroyo-Lopez_et_al.pdf">Arroyo-López, Orlic, Querol, Barrio. Effects of temperature, pH and sugar concentration on the growth parameters of Saccharomyces cerevisiae, S. kudriavzevii and their interspecific hybrid. 2009.</a>
	 * @see <a href="http://www.biologydiscussion.com/industrial-microbiology-2/yeast-used-in-bakery-foods/yeast-used-in-bakery-foods-performance-determination-forms-effect-industrial-microbiology/86555">Yeast used in bakery foods: Performance, determination, forms & effect. Industrial Microbiology</a>
	 *
	 * @return	Correction factor.
	 */
	double sugarFactor(){
		if(sugar < 0.03)
			return Math.min(Helper.evaluatePolynomial(SUGAR_COEFFICIENTS, sugar), 1.);
		if(sugar < SUGAR_MAX)
			return -0.3154 - 0.403 * Math.log(sugar);
		return 0.;
	}

	/**
	 * TODO high fat content inhibits leavening
	 * 0.1-0.2% is desirable
	 *
	 * @return	Correction factor.
	 */
	double fatFactor(){
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
	 * @return	Correction factor.
	 */
	double saltFactor(){
		return Math.max(Helper.evaluatePolynomial(SALT_COEFFICIENTS, salt), 0.);
	}

	/**
	 * https://buonapizza.forumfree.it/?t=75686746
	 * @see <a href="https://www.nature.com/articles/s41598-018-36786-2.pdf">Minervini, Dinardo, de Angelis, Gobbetti. Tap water is one of the drivers that establish and assembly the lactic acid bacterium biota during sourdough preparation. 2018.</a>
	 * @see <a href="http://fens.usv.ro/index.php/FENS/article/download/328/326">Codina, Mironeasa, Voica. Influence of wheat flour dough hydration levels on gas production during dough fermentation and bread quality. 2011. Journal of Faculty of Food Engineering. Vol. X, Issue 4.</a>
	 *
	 * @return	Correction factor.
	 */
	double waterFactor(){
		return (HYDRATION_MIN <= hydration && hydration < HYDRATION_MAX? Helper.evaluatePolynomial(WATER_COEFFICIENTS, hydration): 0.);
	}

	/**
	 * https://academic.oup.com/mutage/article/19/2/157/1076450
	 * Buschini, Carboni, Furlini, Poli, Rossi. sodium hypochlorite-, chlorine dioxide- and peracetic acid-induced genotoxicity detected by Saccharomyces cerevisiae tests [2004]
	 *
	 * @return	Correction factor.
	 */
	double chlorineDioxideFactor(){
		return Math.max(1. - chlorineDioxide / CHLORINE_DIOXIDE_MAX, 0.);
	}

	/**
	 * @see <a href="https://www.tandfonline.com/doi/pdf/10.1271/bbb.69.1365">Arao, Hara, Suzuki, Tamura. Effect of High-Pressure Gas on io.github.mtrevisan.pizza.Yeast Growth. 2014.</a>
	 *
	 * @return	Correction factor.
	 */
	double atmosphericPressureFactor(){
		return (atmosphericPressure < ATMOSPHERIC_PRESSURE_MAX?
			1. - PRESSURE_FACTOR_K * Math.pow(atmosphericPressure / Math.pow(10_000., 2.), PRESSURE_FACTOR_M): 0.);
	}


	/**
	 * @see <a href="https://www.academia.edu/2421508/Characterisation_of_bread_doughs_with_different_densities_salt_contents_and_water_levels_using_microwave_power_transmission_measurements">Campbell. Characterisation of bread doughs with different densities, salt contents and water levels using microwave power transmission measurements. 2005.</a>
	 * @see <a href="https://shodhganga.inflibnet.ac.in/bitstream/10603/149607/15/10_chapter%204.pdf">Density studies of sugar solutions</a>	 *
	 *
	 * @param dough	Final dough weight [g].
	 */
	public double calculateDoughVolume(final double dough, final double fatDensity){
		//density of flour + water + salt
		double doughDensity = 1.41 - 0.00006762 * atmosphericPressure + 0.00640 * salt - 0.00260 * hydration;

		//account for fat
		final double fraction = fat / dough;
		doughDensity = 1. / ((1. - fraction) / doughDensity + fraction / fatDensity);

		//TODO account for sugar

		return dough / doughDensity;
	}

}
