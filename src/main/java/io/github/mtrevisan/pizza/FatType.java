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


/**
 * @see <a href="https://www.tandfonline.com/doi/full/10.1080/10942910701586273">Viscosity and Specific Heat of Vegetable Oils as a Function of Temperature: 35°C to 180°C</a>
 */
public enum FatType{
	OLIVE(1.715, 2.025),
	PEANUT(3.677, 2.449),
	SAFFLOWER(2.832, 2.181),
	SESAME(3.043, 2.446),
	SUNFLOWER(3.477, 2.566);


	//coefficients for the linear interpolation of specific heat
	private final double m;
	private final double b;


	FatType(final double m, final double b){
		this.m = m;
		this.b = b;
	}

	public double specificHeat(final double temperature){
		return m * temperature + b;
	}

}
