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

import java.util.Objects;


public final class Carbohydrate{

	/**
	 * Factor as ratio of specific growth rate w.r.t. glucose for aerobic cultures.
	 *
	 * @see <a href="https://academic.oup.com/femsyr/article/16/1/fov107/2467713">van Dijken, Bauer, Brambilla, Duboc, Francois, Gancedo, Giuseppin, Heijen, Hoare, Lange. An interlaboratory comparison of physiological and genetic properties of four Saccharomyces cerevisiae strains. 2000.</a>
	 * @see <a href="https://hal.insa-toulouse.fr/hal-02559361/file/b_b_vanDijken2000.pdf">Marques, Raghavendran, Stambuk, Gombert. Sucrose and Saccharomyces cerevisiae: a relationship most sweet. 2016.</a>
	 */
	public enum CarbohydrateType{
		@SuppressWarnings("PointlessArithmeticExpression")
		GLUCOSE(0.41 / 0.41, 12.0107 * 6. + 1.00784 * 12. + 15.9994 * 6.),
		//maltose is a disaccharide, formed by glucose and glucose
		MALTOSE(0.40 / 0.41, 12.0107 * 12. + 1.00784 * 22. + 15.9994 * 11.),
		//sucrose is a disaccharide, formed by roughly 50 % glucose and 50% fructose (honey is 30% glucose and 21-43% fructose, with a ratio of 0.4-1.6+)
		SUCROSE(0.38 / 0.41, 12.0107 * 12. + 1.00784 * 22. + 15.9994 * 11.),
		//lactose is a disaccharide, formed by glucose and galactose
		LACTOSE(0.28 / 0.41, 12.0107 * 6. + 1.00784 * 12. + 15.9994 * 6.);


		/** Equivalent quantity in glucose to obtain the same maximum volume expansion ratio. */
		public final double factor;
		public final double molecularWeight;


		CarbohydrateType(final double factor, final double molecularWeight){
			this.factor = factor;
			this.molecularWeight = molecularWeight;
		}

	}


	public final CarbohydrateType type;
	/** Raw carbohydrate content [% w/w]. */
	public final double carbohydrate;
	/** Water content [% w/w]. */
	public final double water;


	/**
	 * @param type	Sugar type.
	 * @param carbohydrate	Carbohydrate content [% w/w].
	 * @param water	Water content [% w/w].
	 * @return	The instance.
	 * @throws DoughException   If there are errors in the parameters' values.
	 */
	public static Carbohydrate create(final CarbohydrateType type, final double carbohydrate, final double water) throws DoughException{
		Objects.requireNonNull(type, "Type must be non null");
		if(carbohydrate < 0.)
			throw DoughException.create("Carbohydrate content must be non-negative");
		if(water < 0.)
			throw DoughException.create("Water content must be non-negative");

		return new Carbohydrate(type, carbohydrate, water);
	}

	private Carbohydrate(final CarbohydrateType type, final double carbohydrate, final double water){
		this.type = type;
		this.carbohydrate = carbohydrate;
		this.water = water;
	}

}
