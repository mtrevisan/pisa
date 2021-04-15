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


public class Recipe{

	/** [g] */
	double flour;
	/** [g] */
	double water;
	/** [°C] */
	double waterTemperature;
	/** [g] */
	double yeast;
	/** [g] */
	double sugar;
	/** [g] */
	double fat;
	/** [g] */
	double salt;


	@Override
	public String toString(){
		return "flour: " + Helper.round(flour, 1) + " g"
			+ ", water: " + Helper.round(water, 1) + " g"
			+ (waterTemperature > 0.? " at " + Helper.round(waterTemperature, 1) + " °C": "")
			+ ", yeast: " + Helper.round(yeast, 2) + " g"
			+ ", sugar: " + Helper.round(sugar, 2) + " g"
			+ ", fat: " + Helper.round(fat, 2) + " g"
			+ ", salt: " + Helper.round(salt, 2) + " g";
	}

}
