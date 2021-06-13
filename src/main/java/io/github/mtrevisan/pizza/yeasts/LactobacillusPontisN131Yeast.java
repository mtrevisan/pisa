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
package io.github.mtrevisan.pizza.yeasts;


/**
 * Lactobacillus pontis constants
 *
 * @see <a href="https://sfamjournals.onlinelibrary.wiley.com/doi/epdf/10.1111/j.1365-2672.2010.04904.x">Mihhalevski, Sarand, Viiard, Salumets, Paalme. Growth characterization of individual rye sourdough bacteria by isothermal microcalorimetry. 2010.</a>
 */
public class LactobacillusPontisN131Yeast extends YeastModelAbstract{

	@Override
	public final double getTemperatureMin(){
		return 0.0;
	}

	@Override
	public final double getTemperatureOpt(){
		return 0.0;
	}

	@Override
	public final double getTemperatureMax(){
		return 0.0;
	}

	@Override
	public final double getMaximumSpecificVolumeGrowthRate(){
		//± 0.01 hrs^-1
		return 0.45;
	}

}
