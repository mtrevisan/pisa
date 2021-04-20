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


public class Ingredients{

	/** Temperature of ingredients [°C]. */
	Double ingredientsTemperature;
	/** Desired dough temperature [°C]. */
	Double doughTemperature;
	/** Desired pizza height [cm]. */
	Double targetPizzaHeight;

	/** Whether to correct for ingredients' content in fat/salt/water. */
	boolean correctForIngredients;
	/** Whether to correct for humidity in the flour. */
	boolean correctForHumidity;
	/** Relative humidity of the air [% w/w]. */
	Double airRelativeHumidity;

	/** Chlorine dioxide in water [mg/l]. */
	Double waterChlorineDioxide;
	/**
	 * Calcium carbonate (CaCO3) in water [mg/l] = [°F * 10] = [°I * 7] = [°dH * 5.6].
	 *
	 * TODO Generally, water of medium hardness, with about 100 to 150 ppm of minerals, is best suited to bread baking. The minerals in water provide food for the yeast, and therefore can benefit fermentation. However, if the water is excessively hard, there will be a tightening effect on the gluten, as well as a decrease in the fermentation rate (the minerals make water absorption more difficult for the proteins in the flour). On the other hand, if water is excessively soft, the lack of minerals will result in a dough that is sticky and slack. Generally speaking, most water is not extreme in either direction, and if water is potable, it is suitable for bread baking.
	 */
	Double waterCalciumCarbonate;
	/**
	 * Fixed residue in water [mg/l].
	 * TODO
	 */
	Double waterFixedResidue;
	/**
	 * pH in water.
	 * <p>
	 * Hard water is more alkaline than soft water, and can decrease the activity of yeast.
	 * Water that is slightly acid (pH a little below 7) is preferred for bread baking.
	 * </p>
	 */
	double waterPH = Dough.PURE_WATER_PH;

	Flour flour;

	YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	double rawYeast = 1.;

	SugarType sugarType;
	/** Raw sugar content [% w/w]. */
	double sugarContent = 1.;
	/** Water content in sugar [% w/w]. */
	Double sugarWaterContent;

	/** Raw fat content [% w/w]. */
	double fatContent = 1.;
	/** Water content in fat [% w/w]. */
	Double fatWaterContent;
	/** Salt content in fat [% w/w]. */
	Double fatSaltContent;


	/**
	 * @param ingredientsTemperature	Temperature of ingredients [°C].
	 * @return	The instance.
	 */
	public Ingredients withIngredientsTemperature(final double ingredientsTemperature){
		this.ingredientsTemperature = ingredientsTemperature;

		return this;
	}

	/**
	 * @param doughTemperature	Desired dough temperature [°C].
	 * @return	The instance.
	 */
	public Ingredients withDoughTemperature(final double doughTemperature){
		this.doughTemperature = doughTemperature;

		return this;
	}

	/**
	 * @param targetPizzaHeight	Desired pizza height [cm].
	 * @return	The instance.
	 */
	public Ingredients withTargetPizzaHeight(final double targetPizzaHeight){
		this.targetPizzaHeight = targetPizzaHeight;

		return this;
	}

	public Ingredients withCorrectForIngredients(){
		this.correctForIngredients = true;

		return this;
	}

	public Ingredients withCorrectForHumidity(){
		this.correctForHumidity = true;

		return this;
	}

	/**
	 * @param airRelativeHumidity	Relative humidity of the air [% w/w].
	 * @return	The instance.
	 */
	public Ingredients withAirRelativeHumidity(final double airRelativeHumidity){
		this.airRelativeHumidity = airRelativeHumidity;

		return this;
	}

	/**
	 * @param chlorineDioxide	Chlorine dioxide in water [mg/l].
	 * @return	The instance.
	 */
	public Ingredients withWater(final double chlorineDioxide) throws DoughException{
		return withWater(chlorineDioxide, 0., 0., Dough.PURE_WATER_PH);
	}

	/**
	 * @param chlorineDioxide	Chlorine dioxide in water [mg/l].
	 * @param calciumCarbonate	Calcium carbonate (CaCO3) in water [mg/l]
	 * @param fixedResidue Fixed residue in water [mg/l].
	 * @param ph	pH in water.
	 * @return	The instance.
	 */
	public Ingredients withWater(final double chlorineDioxide, final double calciumCarbonate, final double fixedResidue, final double ph)
			throws DoughException{
		if(chlorineDioxide < 0.)
			throw DoughException.create("Chlorine dioxide in water must be non-negative");
		if(fixedResidue < 0.)
			throw DoughException.create("Fixed residue of water must be non-negative");
		if(calciumCarbonate < 0.)
			throw DoughException.create("Calcium carbonate in water must be non-negative");
		if(ph < 0. || ph > 14.)
			throw DoughException.create("pH in water must be between 0 and 14");

		this.waterChlorineDioxide = chlorineDioxide;
		this.waterCalciumCarbonate = calciumCarbonate;
		this.waterFixedResidue = fixedResidue;
		this.waterPH = ph;

		return this;
	}

	/**
	 * @param flour	Flour data.
	 * @return	The instance.
	 */
	public Ingredients withFlour(final Flour flour) throws DoughException{
		if(flour == null)
			throw DoughException.create("Missing flour");

		this.flour = flour;

		return this;
	}

	/**
	 * @param yeastType	Yeast type.
	 * @return	The instance.
	 */
	public Ingredients withYeast(final YeastType yeastType) throws DoughException{
		return withYeast(yeastType, 1.);
	}

	/**
	 * @param yeastType	Yeast type.
	 * @param rawYeast	Raw yeast content [% w/w].
	 * @return	The instance.
	 */
	public Ingredients withYeast(final YeastType yeastType, final double rawYeast) throws DoughException{
		if(yeastType == null)
			throw DoughException.create("Missing yeast type");
		if(rawYeast <= 0. || rawYeast > 1.)
			throw DoughException.create("Raw yeast quantity must be between 0 and 1");

		this.yeastType = yeastType;
		this.rawYeast = rawYeast;

		return this;
	}

	/**
	 * @param sugarType	Sugar type.
	 * @return	The instance.
	 */
	public Ingredients withSugar(final SugarType sugarType) throws DoughException{
		return withSugar(sugarType,  1., 0.);
	}

	/**
	 * @param sugarType	Sugar type.
	 * @param sugarContent	Raw sugar content [% w/w].
	 * @param waterContent	Water content in sugar [% w/w].
	 * @return	The instance.
	 */
	public Ingredients withSugar(final SugarType sugarType, final double sugarContent, final double waterContent) throws DoughException{
		if(sugarType == null)
			throw DoughException.create("Missing sugar type");
		if(sugarContent <= 0. || sugarContent > 1.)
			throw DoughException.create("Raw sugar content must be between 0 and 1");
		if(waterContent < 0. || waterContent > 1.)
			throw DoughException.create("Sugar water content must be between 0 and 1");

		this.sugarType = sugarType;
		this.sugarContent = sugarContent;
		this.sugarWaterContent = waterContent;

		return this;
	}

	/**
	 * @param fatContent	Raw fat content [% w/w].
	 * @return	The instance.
	 */
	public Ingredients withFat(final double fatContent) throws DoughException{
		return withFat(fatContent,  0., 0.);
	}

	/**
	 * @param fatContent	Raw fat content [% w/w].
	 * @param waterContent	Water content in fat [% w/w].
	 * @param saltContent	Salt content in fat [% w/w].
	 * @return	The instance.
	 */
	public Ingredients withFat(final double fatContent, final double waterContent, final double saltContent) throws DoughException{
		if(fatContent <= 0. || fatContent > 1.)
			throw DoughException.create("Raw fat content must be between 0 and 1");
		if(waterContent < 0. || waterContent > 1.)
			throw DoughException.create("Fat water content must be between 0 and 1");
		if(saltContent < 0. || saltContent > 1.)
			throw DoughException.create("Fat salt content must be between 0 and 1");

		this.fatContent = fatContent;
		this.fatWaterContent = waterContent;
		this.fatSaltContent = saltContent;

		return this;
	}

	void validate(final YeastModelAbstract yeastModel) throws DoughException{
		if(flour == null)
			throw DoughException.create("Missing flour");
		if(ingredientsTemperature != null && (ingredientsTemperature <= yeastModel.getTemperatureMin()
				|| ingredientsTemperature >= yeastModel.getTemperatureMax()))
			throw DoughException.create("Ingredients temperature [°C] must be between "
				+ Helper.round(yeastModel.getTemperatureMin(), 1) + " °C and "
				+ Helper.round(yeastModel.getTemperatureMax(), 1) + " °C");
		if(doughTemperature != null && (doughTemperature <= yeastModel.getTemperatureMin()
				|| doughTemperature >= yeastModel.getTemperatureMax()))
			throw DoughException.create("Dough temperature [°C] must be between "
				+ Helper.round(yeastModel.getTemperatureMin(), 1) + " °C and "
				+ Helper.round(yeastModel.getTemperatureMax(), 1) + " °C");
	}

}
