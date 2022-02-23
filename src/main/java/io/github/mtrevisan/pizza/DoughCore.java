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
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;


//effect of ingredients!! https://www.maltosefalcons.com/blogs/brewing-techniques-tips/yeast-propagation-and-maintenance-principles-and-practices
public final class DoughCore{

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

	/**
	 * TODO
	 * [% w/w]
	 */
	private static final double FAT_MAX = 1.;

	/** [mg/l] */
	private static final double WATER_CHLORINE_DIOXIDE_MAX = 1. / 0.0931;
	/** [mg/l] */
	private static final double WATER_FIXED_RESIDUE_MAX = 1500.;
	private static final double PURE_WATER_PH = 5.4;

	/** Standard ambient pressure [hPa]. */
	private static final double STANDARD_AMBIENT_PRESSURE = 1013.25;
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
	/** Raw fat content [% w/w]. */
	private double rawFat = 1.;
	/** Fat density [g / ml]. */
	private double fatDensity;
	/** Water content in fat [% w/w]. */
	private double fatWaterContent;
	/** Salt content in fat [% w/w]. */
	private double fatSaltContent;

	/** Total salt quantity w.r.t. flour [% w/w]. */
	private double salt;

	final YeastModelAbstract yeastModel;
	/** Yeast quantity [% w/w]. */
	double yeast;
	YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	double rawYeast = 1.;


	/** Atmospheric pressure [hPa]. */
	private double atmosphericPressure = STANDARD_AMBIENT_PRESSURE;


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
	 * @param water	Water quantity w.r.t. flour [% w/w].
	 * @param chlorineDioxide	Chlorine dioxide in water [mg/l].
	 * @param pH	pH of water.
	 * @param fixedResidue	Fixed residue in water [mg/l].
	 * @return	This instance.
	 * @throws DoughException	If water is too low, or chlorine dioxide is too low or too high, or fixed residue is too low or too high.
	 */
	public DoughCore addWater(final double water, final double chlorineDioxide, final double calciumCarbonate, final double pH,
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
	public DoughCore addSugar(final double sugar, final SugarType sugarType, final double sugarContent, final double waterContent)
			throws DoughException{
		if(sugar < 0. || sugar >= SUGAR_MAX)
			throw DoughException.create("Sugar [% w/w] must be between 0 and {} % w/w", Helper.round(SUGAR_MAX, 2));

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
	public DoughCore addFat(final double fat, final double fatContent, final double density, final double waterContent,
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
	public DoughCore addSalt(final double salt) throws DoughException{
		if(salt < 0.)
			throw DoughException.create("Salt [% w/w] must be positive");

		this.salt += salt;

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
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 * @return	This instance.
	 * @throws DoughException	If pressure is negative or above maximum.
	 */
	public DoughCore withAtmosphericPressure(final double atmosphericPressure) throws DoughException{
		if(atmosphericPressure < 0. || atmosphericPressure >= ATMOSPHERIC_PRESSURE_MAX)
			throw DoughException.create("Atmospheric pressure [hPa] must be between 0 and {} hPa",
				Helper.round(ATMOSPHERIC_PRESSURE_MAX, 1));

		this.atmosphericPressure = atmosphericPressure;

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
		final double kAtmosphericPressure = atmosphericPressureFactor(atmosphericPressure);
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
		//6.1-6.4 for butter
		final double fatPH = 6.25;
		final double compositePH = (flourPH + waterPH * water + fatPH * fat) / (1. + water + fat);

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

}
