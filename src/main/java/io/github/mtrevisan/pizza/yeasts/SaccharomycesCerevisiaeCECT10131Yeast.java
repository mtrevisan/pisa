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
 * Saccharomyces cerevisiae (strain CECT 10131) constants
 * <i>Centaurea alba</i> flower (Spain)
 *
 * @see <a href="https://aem.asm.org/content/aem/77/7/2292.full.pdf">Temperature adaptation markedly determines evolution within the genus Saccharomyces. 2011.</a>
 */
public class SaccharomycesCerevisiaeCECT10131Yeast extends YeastModelAbstract{

	@Override
	public final double getTemperatureMin(){
		//± 0.10 °C
		return 0.74;
	}

	@Override
	final double getTemperatureOpt(){
		//± 0.61 °C
		return 32.8;
	}

	@Override
	public final double getTemperatureMax(){
		//± 0.10 °C
		return 45.9;
	}

	@Override
	final double getMaximumSpecificGrowthRate(){
		//base is pH 5.4±0.1, 20 mg/l glucose
		//± 0.009 °C
		return 0.449;
	}

}
