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
 * Saccharomyces cerevisiae (strain CEN.PK113-7D) constants
 *
 * @see <a href="https://www.sciencedirect.com/science/article/pii/S2215017X20300175">Selection and subsequent physiological characterization of industrial Saccharomyces cerevisiae strains during continuous growth at sub- and- supra optimal temperatures. 2020.</a>
 */
public class SaccharomycesCerevisiaeCEN_PK113_7DYeast extends YeastModelAbstract{

	@Override
	public final double getTemperatureMin(){
		//± 0.018 °C
		return 0.368;
	}

	@Override
	final double getTemperatureOpt(){
		//± 0.85 °C
		return 30.03;
	}

	@Override
	public final double getTemperatureMax(){
		//± 0.81 °C
		return 41.21;
	}

	@Override
	final double getMaximumSpecificVolumeGrowthRate(){
		//base is pH 5.3, 22 mg/l glucose
		//± 0.018 °C
		return 0.368;
	}

}
