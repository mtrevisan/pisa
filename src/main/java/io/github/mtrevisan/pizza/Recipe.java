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


public class Recipe{

	/** Flour quantity [g]. */
	private double flour;
	/** Water quantity [g]. */
	private double water;
	/** Water temperature [°C]. */
	private Double waterTemperature;
	/** Yeast quantity [g]. */
	private double yeast;
	/** Sugar quantity [g]. */
	private double sugar;
	/** Fat quantity [g]. */
	private double fat;
	/** Salt quantity [g]. */
	private double salt;

	/** Time to start making the dough. */
	private LocalTime doughMakingInstant;
	/** List of couples start-end times for each stage. */
	private LocalTime[][] stageStartEndInstants;
	/** Time to start seasoning the pizza. */
	private LocalTime seasoningInstant;


	public static final Recipe create(){
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

	@Override
	public String toString(){
		final StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < stageStartEndInstants.length; i ++){
			sb.append(stageStartEndInstants[i][0]).append("-").append(stageStartEndInstants[i][1]);

			if(i < stageStartEndInstants.length - 1)
				sb.append(", ");
		}
		sb.append("]");
		return "flour: " + Helper.round(flour, 1) + " g"
			+ ", water: " + Helper.round(water, 1) + " g"
			+ (waterTemperature != null? " at " + Helper.round(waterTemperature, 1) + " °C": "")
			+ ", yeast: " + Helper.round(yeast, 2) + " g"
			+ ", sugar: " + Helper.round(sugar, 2) + " g"
			+ ", fat: " + Helper.round(fat, 2) + " g"
			+ ", salt: " + Helper.round(salt, 2) + " g"
			+ ", dough making: " + doughMakingInstant
			+ ", stages: " + sb
			+ ", seasoning: " + seasoningInstant
			;
	}

}
