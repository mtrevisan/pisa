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
import io.github.mtrevisan.pizza.ingredients.Carbohydrate;
import io.github.mtrevisan.pizza.ingredients.Water;
import io.github.mtrevisan.pizza.ingredients.Yeast;
import io.github.mtrevisan.pizza.utils.Helper;


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

	/** [mg/l] */
	private static final double WATER_CHLORINE_DIOXIDE_MAX = 1. / 0.0931;
	/** [mg/l] */
	private static final double WATER_FIXED_RESIDUE_MAX = 1500.;


	static final double DOUGH_WEIGHT_ACCURACY = 0.01;

	static final int VOLUME_PERCENT_ACCURACY_DIGITS = 2;
	static final int WEIGHT_ACCURACY_DIGITS = 2;
	static final int HEAVY_WEIGHT_ACCURACY_DIGITS = 1;
	static final int TEMPERATURE_ACCURACY_DIGITS = 1;


	Flour flour;

	/** Total water quantity w.r.t. flour [% w/w]. */
	double waterQuantity;
	Water water;

	/** Total sugar (glucose) quantity w.r.t. flour [% w/w]. */
	double carbohydrateQuantity;
	Carbohydrate carbohydrate;

	/** Total fat quantity w.r.t. flour [% w/w]. */
	double fatQuantity;
	Fat fat;

	/** Total salt quantity w.r.t. flour [% w/w]. */
	double saltQuantity;

	/** Yeast quantity [% w/w]. */
	double yeastQuantity;
	final Yeast yeast;

	Atmosphere atmosphere;


	/** Whether to correct for ingredients' content in fat/salt/water. */
	boolean correctForIngredients;


	public static DoughCore create(final Yeast yeast) throws DoughException{
		return new DoughCore(yeast);
	}


	private DoughCore(final Yeast yeast) throws DoughException{
		if(yeast == null)
			throw DoughException.create("Missing yeast data");

		this.yeast = yeast;
	}


	/**
	 * @param flour	Flour data.
	 * @return	The instance.
	 */
	public DoughCore withFlour(final Flour flour) throws DoughException{
		if(flour == null)
			throw DoughException.create("Missing flour data");

		this.flour = flour;

		return this;
	}


	/**
	 * @param waterQuantity	Water quantity w.r.t. flour [% w/w].
	 * @param water	Water data.
	 * @return	This instance.
	 * @throws DoughException	If water is too low, or chlorine dioxide is too low or too high, or fixed residue is too low or too high.
	 */
	public DoughCore addWater(final double waterQuantity, final Water water) throws DoughException{
		if(waterQuantity < 0.)
			throw DoughException.create("Hydration [% w/w] cannot be less than zero");
		if(water == null)
			throw DoughException.create("Missing water data");
		if(water.chlorineDioxide >= WATER_CHLORINE_DIOXIDE_MAX)
			throw DoughException.create("Chlorine dioxide [mg/l] in water must be between 0 and {} mg/l",
				Helper.round(WATER_CHLORINE_DIOXIDE_MAX, 2));
		if(water.fixedResidue >= WATER_FIXED_RESIDUE_MAX)
			throw DoughException.create("Fixed residue [mg/l] of water must be between 0 and {} mg/l",
				Helper.round(WATER_FIXED_RESIDUE_MAX, 2));

		this.waterQuantity += waterQuantity;
		this.water = water;

		return this;
	}


	/**
	 * @param sugarQuantity	Sugar quantity w.r.t. flour [% w/w].
	 * @param carbohydrate	Sugar data.
	 * @return	This instance.
	 * @throws DoughException	If sugar is too low or too high.
	 */
	public DoughCore addCarbohydrate(final double sugarQuantity, final Carbohydrate carbohydrate) throws DoughException{
		if(sugarQuantity < 0. || sugarQuantity >= SUGAR_MAX)
			throw DoughException.create("Sugar [% w/w] must be between 0 and {} % w/w",
				Helper.round(SUGAR_MAX, VOLUME_PERCENT_ACCURACY_DIGITS));
		if(carbohydrate == null)
			throw DoughException.create("Missing sugar data");
		if(this.carbohydrate != null)
			throw DoughException.create("Sugar was already set");

		this.carbohydrateQuantity += carbohydrate.type.factor * sugarQuantity * carbohydrate.carbohydrate;
		addWater(sugarQuantity * carbohydrate.water, Water.createPure());
		this.carbohydrate = carbohydrate;

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
		if(fat == null)
			throw DoughException.create("Missing fat data");
		if(this.fat != null)
			throw DoughException.create("Fat was already set");

		this.fatQuantity += fatQuantity * fat.fat;
		addWater(fatQuantity * fat.water, Water.createPure());
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
	 * @param atmosphere	Atmosphere data.
	 * @return	This instance.
	 * @throws DoughException	If pressure is negative or above maximum.
	 */
	public DoughCore withAtmosphere(final Atmosphere atmosphere) throws DoughException{
		if(atmosphere == null)
			throw DoughException.create("Missing atmosphere data");
		if(atmosphere.pressure < 0.)
			throw DoughException.create("Atmospheric pressure [hPa] must be positive");

		this.atmosphere = atmosphere;

		return this;
	}


	public DoughCore withCorrectForIngredients(){
		correctForIngredients = true;

		return this;
	}


	double totalFraction(){
		return 1. + waterQuantity + carbohydrateQuantity + fatQuantity + saltQuantity + yeastQuantity;
	}

}
