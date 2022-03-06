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

import java.time.LocalTime;
import java.util.StringJoiner;


public final class Recipe{

	/** Flour quantity [g]. */
	private double flour;
	/** Water quantity [g]. */
	private double water;
	/** Water temperature [°C]. */
	private Double waterTemperature;
	/** Sugar quantity [g]. */
	private double sugar;
	/** Fat quantity [g]. */
	private double fat;
	/** Salt quantity [g]. */
	private double salt;
	/** Yeast quantity [g]. */
	private double yeast;

	/** Time to start making the dough. */
	private LocalTime doughMakingInstant;
	/** List of couples start-end times for each stage. */
	private LocalTime[][] stageStartEndInstants;
	/** List of starting times for stretch & fold phases. */
	private LocalTime[] stretchAndFoldStartInstants;
	/** Time to start seasoning the pizza. */
	private LocalTime seasoningInstant;


	public static Recipe create(){
		return new Recipe();
	}

	private Recipe(){}

	/**
	 * @param flour	Flour quantity [g].
	 * @return	The instance.
	 */
	public Recipe withFlour(final double flour){
		this.flour = flour;

		return this;
	}

	/**
	 * @return	Flour quantity [g].
	 */
	public double getFlour(){
		return flour;
	}

	/**
	 * @param water	Water quantity [g].
	 * @return	The instance.
	 */
	public Recipe withWater(final double water){
		return withWater(water, null);
	}

	/**
	 * @param water	Water quantity [g].
	 * @param waterTemperature	Water temperature [°C].
	 * @return	The instance.
	 */
	public Recipe withWater(final double water, final Double waterTemperature){
		this.water = water;
		this.waterTemperature = waterTemperature;

		return this;
	}

	/**
	 * @param waterTemperature	Water temperature [°C].
	 * @return	The instance.
	 */
	public Recipe withWaterTemperature(final Double waterTemperature){
		this.waterTemperature = waterTemperature;

		return this;
	}

	/**
	 * @return	Water quantity [g].
	 */
	public double getWater(){
		return water;
	}

	/**
	 * @return	Water temperature [°C].
	 */
	public Double getWaterTemperature(){
		return waterTemperature;
	}

	/**
	 * @param sugar	Sugar quantity [g].
	 * @return	The instance.
	 */
	public Recipe withSugar(final double sugar){
		this.sugar = sugar;

		return this;
	}

	/**
	 * @return	Sugar quantity [g].
	 */
	public double getSugar(){
		return sugar;
	}

	/**
	 * @param fat	Fat quantity [g].
	 * @return	The instance.
	 */
	public Recipe withFat(final double fat){
		this.fat = fat;

		return this;
	}

	/**
	 * @return	Fat quantity [g].
	 */
	public double getFat(){
		return fat;
	}

	/**
	 * @param salt	Salt quantity [g].
	 * @return	The instance.
	 */
	public Recipe withSalt(final double salt){
		this.salt = salt;

		return this;
	}

	/**
	 * @return	Salt quantity [g].
	 */
	public double getSalt(){
		return salt;
	}

	/**
	 * @param yeast	Yeast quantity [g].
	 * @return	The instance.
	 */
	public Recipe withYeast(final double yeast){
		this.yeast = yeast;

		return this;
	}

	/**
	 * @return	Yeast quantity [g].
	 */
	public double getYeast(){
		return yeast;
	}


	/**
	 * @param doughMakingInstant	Time to start making the dough.
	 * @return	The instance.
	 */
	public Recipe withDoughMakingInstant(final LocalTime doughMakingInstant){
		this.doughMakingInstant = doughMakingInstant;

		return this;
	}

	/**
	 * @return	Time to start making the dough.
	 */
	public LocalTime getDoughMakingInstant(){
		return doughMakingInstant;
	}

	/**
	 * @param stageStartEndInstants	List of couples start-end times for each stage.
	 * @return	The instance.
	 */
	public Recipe withStageStartEndInstants(final LocalTime[][] stageStartEndInstants){
		this.stageStartEndInstants = stageStartEndInstants;

		return this;
	}

	/**
	 * @return	List of couples start-end times for each stage.
	 */
	public LocalTime[][] getStageStartEndInstants(){
		return stageStartEndInstants;
	}

	/**
	 * @param stretchAndFoldStartInstants	List of starting times for stretch & fold phases.
	 * @return	The instance.
	 */
	public Recipe withStretchAndFoldStartInstants(final LocalTime[] stretchAndFoldStartInstants){
		this.stretchAndFoldStartInstants = stretchAndFoldStartInstants;

		return this;
	}

	/**
	 * @return	List of starting times for stretch & fold phases.
	 */
	public LocalTime[] getStretchAndFoldStartInstants(){
		return stretchAndFoldStartInstants;
	}

	/**
	 * @param seasoningInstant	Time to start seasoning the pizza.
	 * @return	The instance.
	 */
	public Recipe withSeasoningInstant(final LocalTime seasoningInstant){
		this.seasoningInstant = seasoningInstant;

		return this;
	}

	/**
	 * @return	Time to start seasoning the pizza.
	 */
	public LocalTime getSeasoningInstant(){
		return seasoningInstant;
	}


	/**
	 * @return	The total dough weight [g].
	 */
	public double doughWeight(){
		return flour + water + yeast + sugar + fat + salt;
	}

	/**
	 * @see <a href="https://www.academia.edu/2421508/Characterisation_of_bread_doughs_with_different_densities_salt_contents_and_water_levels_using_microwave_power_transmission_measurements">Campbell. Characterisation of bread doughs with different densities, salt contents and water levels using microwave power transmission measurements. 2005.</a>
	 * @see <a href="https://core.ac.uk/download/pdf/197306213.pdf">Kubota, Matsumoto, Kurisu, Sizuki, Hosaka. The equations regarding temperature and concentration of the density and viscosity of sugar, salt and skim milk solutions. 1980.</a>
	 * @see <a href="https://shodhganga.inflibnet.ac.in/bitstream/10603/149607/15/10_chapter%204.pdf">Density studies of sugar solutions</a>
	 * @see <a href="https://www.researchgate.net/publication/280063894_Mathematical_modelling_of_density_and_viscosity_of_NaCl_aqueous_solutions">Simion, Grigoras, Rosu, Gavrila. Mathematical modelling of density and viscosity of NaCl aqueous solutions. 2014.</a>
	 * @see <a href="https://www.researchgate.net/publication/233266779_Temperature_and_Concentration_Dependence_of_Density_of_Model_Liquid_Foods">Darros-Barbosa, Balaban, Teixeira.Temperature and concentration dependence of density of model liquid foods. 2003.</a>
	 *
	 * @param fatDensity	Density of the fat [kg/l].
	 * @param temperature	Temperature of the dough [°C].
	 * @param atmosphericPressure	Atmospheric pressure [hPa].
	 * @return	The density of the dough [g / ml].
	 */
	double density(final double fatDensity, final double temperature, final double atmosphericPressure){
		//TODO
		final double doughWeight = doughWeight();
		//density of flour + salt + sugar + water
		final double doughDensity = 1.41
			- 0.00006762 * atmosphericPressure
			+ 0.00640 * salt / doughWeight
//			+ 0.00746 * salt / doughWeight - 0.000411 * (doughTemperature + ABSOLUTE_ZERO)
//			+ 0.000426 * sugar / doughWeight - 0.000349 * (doughTemperature + ABSOLUTE_ZERO)
			- 0.00260 * water / doughWeight;

//		final double pureWaterDensity = 999.84259 + (6.793952e-2 + (-9.09529e-3 + (1.001685e-4 + (-1.120083e-6 + 6.536332e-9 * temperature)
//			* temperature) * temperature) * temperature) * temperature;

		//account for fat
		final double fraction = fat / doughWeight;
		return 1. / ((1. - fraction) / doughDensity + (fraction > 0.? fraction / fatDensity: 0.));
	}


	@Override
	public String toString(){
		final StringJoiner sj = new StringJoiner(", ");
		sj.add("flour: " + Helper.round(flour, 1) + " g");
		sj.add("water: " + Helper.round(water, 1) + " g"
			+ (waterTemperature != null? " at " + Helper.round(waterTemperature, 1) + " °C": ""));
		sj.add("yeast: " + Helper.round(yeast, 2) + " g");
		sj.add("sugar: " + Helper.round(sugar, 2) + " g");
		sj.add("fat: " + Helper.round(fat, 2) + " g");
		sj.add("salt: " + Helper.round(salt, 2) + " g");
		if(doughMakingInstant != null)
			sj.add("dough making: " + doughMakingInstant);
		final int stages = (stageStartEndInstants != null? stageStartEndInstants.length: 0);
		if(stages > 0){
			final StringBuilder sb = new StringBuilder("[");
			for(int i = 0; i < stages; i ++){
				sb.append(stageStartEndInstants[i][0]).append("-").append(stageStartEndInstants[i][1]);

				if(i < stageStartEndInstants.length - 1)
					sb.append(", ");
			}
			sb.append("]");
			sj.add("stages: " + sb);
		}
		if(seasoningInstant != null)
			sj.add("seasoning: " + seasoningInstant);
		return sj.toString();
	}

}
