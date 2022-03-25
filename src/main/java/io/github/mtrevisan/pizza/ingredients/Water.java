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
package io.github.mtrevisan.pizza.ingredients;

import io.github.mtrevisan.pizza.DoughException;


public final class Water{

	/** For milk is around 85.5–89.5%. */
	public static final double PURE_WATER_CONTENT = 1.;
	private static final double PURE_WATER_PH = 5.4;


	/** Raw water content [% w/w]. */
	public final double water;

	/** Chlorine dioxide in water [mg/l]. */
	public final double chlorineDioxide;
	/**
	 * Generally, water of medium hardness, with about 50 to 100 ppm of minerals (1 mg/l = 1.001142303 ppm), is best suited to bread baking.
	 * The minerals in water provide food for the yeast, and therefore can benefit fermentation. However, if the water is excessively hard,
	 * there will be a tightening effect on the gluten, as well as a decrease in the fermentation rate (the minerals make water absorption
	 * more difficult for the proteins in the flour). On the other hand, if water is excessively soft, the lack of minerals will result in a
	 * dough that is sticky and slack. Generally speaking, most water is not extreme in either direction, and if water is potable, it is
	 * suitable for bread baking.
	 * Soft water (less than 50 ppm carbonates) has a shortage of those salts, which tends to result in a soft, sticky dough because there’s
	 * less gluten-tightening effect from minerals. To counteract stickiness, reduce the water portion by about 2%. It can also help to
	 * increase the salt portion up to 2.5% of flour weight. On the baked pizza, the soft water tends to produce a crust texture and color
	 * that’s less than optimum.
	 * Hard water (over 100 ppm carbonates) has too much of the salts. This toughens gluten excessively, which retards the fermentation or
	 * rise of dough. To counteract that, increase the yeast level and, if it’s used, adjust the amount of yeast food. Also, adding malt or
	 * malted flour might help.
	 *
	 * Calcium carbonate (CaCO₃) in water [mg/l] = [°F · 10] = [°E · 6.99] = [°D · 5.62] = [°dH · 5.6].
	 *
	 * NOTE: Water hardness is expressed in German degree (°D is 1 mg CaO (or 0.719 mg MgO) dissolved in 100 ml of water), French degree
	 * (°F is 1 mg CaCO₃ dissolved in 100 ml of water) or English degree (1 °E is 1 g CaCO₃ dissolved in 700 ml of water).
	 * Water with hardness of 0 and 5 °D is regarded as soft water; if the hardness is between 5 and 12 °D, then the water is weak hard;
	 * for the hardness of 12–30 °D, water is hard and over 30 °D water is very hard.
	 */
	public final double calciumCarbonate;
	/**
	 * pH of water.
	 * <p>
	 * Hard water is more alkaline than soft water, and can decrease the activity of yeast.
	 * Water that is slightly acid (pH a little below 7) is preferred for bread baking.
	 * </p>
	 */
	public final double pH;
	/** Fixed residue in water [mg/l]. */
	public final double fixedResidue;


	/**
	 * @return	The instance.
	 */
	public static Water createPure(){
		return new Water(PURE_WATER_CONTENT, 0., 0., 0., PURE_WATER_PH);
	}

	/**
	 * @param water	Water content [% w/w].
	 * @param chlorineDioxide	Chlorine dioxide content [mg/l].
	 * @param calciumCarbonate	Calcium carbonate content [mg/l].
	 * @param fixedResidue	Fixed residue [mg/l].
	 * @param pH	pH.
	 * @return	The instance.
	 * @throws DoughException	If there are errors in the parameters' values.
	 */
	public static Water create(final double water, final double chlorineDioxide, final double calciumCarbonate,
			final double fixedResidue, final double pH) throws DoughException{
		if(chlorineDioxide < 0.)
			throw DoughException.create("Chlorine dioxide content must be non-negative");
		if(calciumCarbonate < 0.)
			throw DoughException.create("Calcium carbonate must be non-negative");
		if(pH < 0. || pH > 14.)
			throw DoughException.create("pH of water must be between 0 and 14");
		if(fixedResidue < 0.)
			throw DoughException.create("Fixed residue must be non-negative");

		return new Water(water, chlorineDioxide, calciumCarbonate, fixedResidue, pH);
	}

	private Water(final double water, final double chlorineDioxide, final double calciumCarbonate, final double fixedResidue,
			final double pH){
		this.water = water;
		this.chlorineDioxide = chlorineDioxide;
		this.calciumCarbonate = calciumCarbonate;
		this.pH = pH;
		this.fixedResidue = fixedResidue;
	}

}
