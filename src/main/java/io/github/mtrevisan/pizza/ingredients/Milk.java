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


public final class Milk{

	/** Raw carbohydrate content [% w/w]. */
	public final double fat;
	/** Water content [% w/w]. */
	public final double water;


	/**
	 * @param fat	Fat content [% w/w].
	 * @param water	Water content [% w/w].
	 * @return	The instance.
	 * @throws DoughException	If there are errors in the parameters' values.
	 */
	public static Milk create(final double fat, final double water) throws DoughException{
		if(fat < 0.)
			throw DoughException.create("Fat content must be non-negative");
		if(water < 0.)
			throw DoughException.create("Water content must be non-negative");

		return new Milk(fat, water);
	}

	private Milk(final double fat, final double water){
		this.fat = fat;
		this.water = water;
	}

}
