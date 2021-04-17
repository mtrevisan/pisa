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


public class Ingredients{

	/** Total dough weight [g]. */
	final double dough;
	/** [g] */
	final double doughPrecision;
	/** Temperature of ingredients [°C]. */
	double ingredientsTemperature;
	/** Desired dough temperature [°C]. */
	double doughTemperature;

	/** Whether to correct for ingredients' content in fat/salt/water. */
	boolean correctForIngredients;
	/** Whether to correct for humidity in the flour. */
	boolean correctForHumidity;
	/** Relative humidity of the air [% w/w]. */
	double airRelativeHumidity;

	/** Chlorine dioxide in water [mg/l]. */
	double waterChlorineDioxide;
	/**
	 * Calcium carbonate (CaCO3) in water [mg/l] = [°F * 10] = [°I * 7] = [°dH * 5.6].
	 *
	 * TODO Generally, water of medium hardness, with about 100 to 150 ppm of minerals, is best suited to bread baking. The minerals in water provide food for the yeast, and therefore can benefit fermentation. However, if the water is excessively hard, there will be a tightening effect on the gluten, as well as a decrease in the fermentation rate (the minerals make water absorption more difficult for the proteins in the flour). On the other hand, if water is excessively soft, the lack of minerals will result in a dough that is sticky and slack. Generally speaking, most water is not extreme in either direction, and if water is potable, it is suitable for bread baking.
	 */
	double waterCalciumCarbonate;
	/**
	 * pH in water.
	 * <p>
	 * Hard water is more alkaline than soft water, and can decrease the activity of yeast.
	 * Water that is slightly acid (pH a little below 7) is preferred for bread baking.
	 * </p>
	 */
	double waterPH = Dough.PURE_WATER_PH;
	/**
	 * Fixed residue in water [mg/l].
	 * TODO
	 */
	double waterFixedResidue;

	Flour flour;

	YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	double rawYeast = 1.;

	SugarType sugarType;
	/** Raw sugar content [% w/w]. */
	double sugarContent = 1.;
	/** Water content in sugar [% w/w]. */
	double sugarWaterContent;

	/** Raw fat content [% w/w]. */
	double fatContent = 1.;
	/** Water content in fat [% w/w]. */
	double fatWaterContent;
	/** Salt content in fat [% w/w]. */
	double fatSaltContent;


	public static Ingredients create(final double dough, final double doughPrecision){
		return new Ingredients(dough, doughPrecision);
	}

	private Ingredients(final double dough, final double doughPrecision){
		this.dough = dough;
		this.doughPrecision = doughPrecision;
	}

}
