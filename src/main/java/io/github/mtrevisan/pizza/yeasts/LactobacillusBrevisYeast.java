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
 * Lactobacillus brevis constants
 *
 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5192527/">Munanga, Loiseau, Grabulos, Mestres. Modeling Lactic Fermentation of Gowé Using Lactobacillus Starter Culture. 2016.</a>
 */
public class LactobacillusBrevisYeast extends YeastModelAbstract{

	@Override
	public final double getTemperatureMin(){
		return 15.0;
	}

	@Override
	final double getTemperatureOpt(){
		return 44.6;
	}

	@Override
	public final double getTemperatureMax(){
		return 53.0;
	}

	@Override
	final double getMaximumSpecificVolumeGrowthRate(){
		//± 0.2 hrs^-1
		return 1.8;
	}

}
