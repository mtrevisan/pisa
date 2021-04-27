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

	/** Standard atmosphere [hPa]. */
	static final double ONE_ATMOSPHERE = 1013.25;


	/** Temperature of ingredients [°C]. */
	Double ingredientsTemperature;
	/** Desired dough temperature [°C]. */
	Double doughTemperature;

	/** Whether to correct for ingredients' content in fat/salt/water. */
	boolean correctForIngredients;
	/** Whether to correct for humidity in the flour. */
	boolean correctForFlourHumidity;
	/** Relative humidity of the air [% w/w]. */
	Double airRelativeHumidity;
	/** Atmospheric pressure [hPa]. */
	double atmosphericPressure = ONE_ATMOSPHERE;

	Flour flour;



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

	public Ingredients withCorrectForIngredients(){
		correctForIngredients = true;

		return this;
	}

	public Ingredients withCorrectForFlourHumidity(){
		correctForFlourHumidity = true;

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
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 * @return	This instance.
	 * @throws DoughException	If pressure is negative or above maximum.
	 */
	public Ingredients withAtmosphericPressure(final double atmosphericPressure) throws DoughException{
		if(atmosphericPressure < 0. || atmosphericPressure >= Dough.ATMOSPHERIC_PRESSURE_MAX)
			throw DoughException.create("Atmospheric pressure [hPa] must be between 0 and {} hPa",
				Helper.round(Dough.ATMOSPHERIC_PRESSURE_MAX, 1));

		this.atmosphericPressure = atmosphericPressure;

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

	void validate(final YeastModelAbstract yeastModel) throws DoughException{
		if(flour == null)
			throw DoughException.create("Missing flour");
		if(ingredientsTemperature != null && (ingredientsTemperature <= yeastModel.getTemperatureMin()
				|| ingredientsTemperature >= yeastModel.getTemperatureMax()))
			throw DoughException.create("Ingredients temperature [°C] must be between {} and {} °C",
				Helper.round(yeastModel.getTemperatureMin(), 1), Helper.round(yeastModel.getTemperatureMax(), 1));
		if(doughTemperature != null && (doughTemperature <= yeastModel.getTemperatureMin()
				|| doughTemperature >= yeastModel.getTemperatureMax()))
			throw DoughException.create("Dough temperature [°C] must be between {} and {} °C",
				Helper.round(yeastModel.getTemperatureMin(), 1), Helper.round(yeastModel.getTemperatureMax(), 1));
	}

}
