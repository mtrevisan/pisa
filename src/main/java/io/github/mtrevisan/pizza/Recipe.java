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
	double flour;
	/** Water quantity [g]. */
	double water;
	/** Water temperature [°C]. */
	Double waterTemperature;
	/** Yeast quantity [g]. */
	double yeast;
	/** Sugar quantity [g]. */
	double sugar;
	/** Fat quantity [g]. */
	double fat;
	/** Salt quantity [g]. */
	double salt;

	LocalTime doughMaking;
	LocalTime[][] stagesStartEnd;
	LocalTime seasoning;


	@Override
	public String toString(){
		final StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < stagesStartEnd.length; i ++){
			sb.append(stagesStartEnd[i][0]).append("-").append(stagesStartEnd[i][1]);

			if(i < stagesStartEnd.length - 1)
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
			+ ", dough making: " + doughMaking
			+ ", stages: " + sb
			+ ", seasoning: " + seasoning
			;
	}

}
