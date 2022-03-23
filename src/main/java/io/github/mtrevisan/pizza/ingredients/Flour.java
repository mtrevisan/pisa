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


public final class Flour{

	/** W. */
	public final double strength;
	/** Protein content [% w/w]. */
	final double protein;
	/** Fat content [% w/w]. */
	public final double fat;
	/** Carbohydrate content [% w/w]. */
	public final double carbohydrate;
	/** Fiber content [% w/w]. */
	final double fiber;
	/** Ash content [% w/w]. */
	final double ash;

	/** Salt content [% w/w]. */
	public final double salt;

	/** Whether to correct for humidity. */
	public boolean correctForHumidity;


	/**
	 * @param strength	Strength.
	 * @param protein	Protein content [% w/w].
	 * @param fat	Fat content [% w/w].
	 * @param carbohydrate	Sugar content [% w/w].
	 * @param fiber	Fiber content [% w/w].
	 * @param ash	Ash content [% w/w].
	 * @param salt	Salt content [% w/w].
	 * @return	The instance.
	 * @throws DoughException	If there are errors in the parameters' values.
	 */
	public static Flour create(final double strength, final double protein, final double fat, final double carbohydrate, final double fiber,
			final double ash, final double salt) throws DoughException{
		if(strength <= 0.)
			throw DoughException.create("Strength must be positive");
		if(protein < 0.)
			throw DoughException.create("Protein must be positive");
		if(fat < 0.)
			throw DoughException.create("Fat content must be non-negative");
		if(carbohydrate < 0.)
			throw DoughException.create("Carbohydrate content must be non-negative");
		if(fiber < 0.)
			throw DoughException.create("Fiber content must be non-negative");
		if(ash < 0.)
			throw DoughException.create("Ash content must be non-negative");
		if(salt < 0.)
			throw DoughException.create("Salt content must be non-negative");

		return new Flour(strength, protein, fat, carbohydrate, fiber, ash, salt);
	}


	private Flour(final double strength, final double protein, final double fat, final double carbohydrate, final double fiber,
			final double ash, final double salt){
		this.strength = strength;
		this.protein = protein;
		this.fat = fat;
		this.carbohydrate = carbohydrate;
		this.fiber = fiber;
		this.ash = ash;
		this.salt = salt;
	}


	public Flour withCorrectForHumidity(){
		correctForHumidity = true;

		return this;
	}

	//https://www.ksonfoodtech.com/files/L2.pdf
	public double estimateSpecificHeat(final double water, double temperature){
		temperature = celsiusToFahrenheit(temperature);
		if(temperature < -40. || temperature > 300.)
			throw new IllegalArgumentException("Temperature out of range [" + fahrenheitToCelsius(-40.) + ", "
				+ fahrenheitToCelsius(300.) + "]");

		double cp = 0;
		//protein
		cp += (0.47442 + (0.00016661 - 0.000000096784 * temperature) * temperature) * protein;
		//fat
		cp += (0.46730 + (0.00021815 - 0.00000035391 * temperature) * temperature) * fat;
		//carbohydrate
		cp += (0.36114 + (0.00028843 - 0.00000043788 * temperature) * temperature) * carbohydrate;
		//fiber
		cp += (0.43276 + (0.00026485 - 0.00000034285 * temperature) * temperature) * fiber;
		//ash
		cp += (0.25266 + (0.00026810 - 0.00000027141 * temperature) * temperature) * ash;
		//water
		if(temperature < 32.)
			cp += (1.0725 + (-0.0053992 + 0.000073361 * temperature) * temperature) * water;
		else
			cp += (0.99827 + (-0.000037879 + 0.00000040347 * temperature) * temperature) * water;
		return cp;
	}

	private double celsiusToFahrenheit(final double temperature){
		return temperature * 1.8 + 32.;
	}

	private double fahrenheitToCelsius(final double temperature){
		return (temperature - 32.) / 1.8;
	}

	/**
	 * @see <a href="https://www.research.manchester.ac.uk/portal/files/54543624/FULL_TEXT.PDF">Trinh. Gas cells in bread dough. 2013.</a>
	 *
	 * @param hydration	[% w/w].
	 * @return	Protein content (standard error is 0.0466) [% w/w].
	 */
	public static double estimatedMinimumProteinContent(final double hydration){
		return (hydration - 0.320) / 2.15;
	}

	/**
	 * @param airRelativeHumidity	[% w/w].
	 * @return	Flour humidity [% w/w].
	 */
	public static double estimatedHumidity(final double airRelativeHumidity){
		//13.5% at RH 70.62%
		return 0.121 + 0.000_044 * Math.exp(8.16 * airRelativeHumidity);
	}

}
