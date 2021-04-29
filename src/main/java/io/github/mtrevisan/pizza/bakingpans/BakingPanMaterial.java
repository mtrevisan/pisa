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
package io.github.mtrevisan.pizza.bakingpans;


public enum BakingPanMaterial{
	CAST_IRON(560.548, 52., 0.64),

	ALUMINIUM(896.9, 237., 0.8),

	//https://inis.iaea.org/search/search.aspx?orig_q=RN:33040336
	STAINLESS_STEEL_304(490. - 530., 14. - 17., 0.32 - 0.38),
	//https://inis.iaea.org/search/search.aspx?orig_q=RN:33040336
	STAINLESS_STEEL_316(490. - 530., 13. - 17., 0.44 - 0.51),

	//https://www.electronics-cooling.com/1999/09/the-thermal-conductivity-of-ceramics/
	CERAMIC(850., 80. - 200., -1.),
	CLAY(0.33, 0.15 - 1.8, -1.),
	CORDIERITE_STONE(800. - 850., 3., 0.95);


	/** [J / (kg * K)] */
	public final double specificHeat;
	//https://www.cpp.edu/~lllee/TK3111heat.pdf pag 19
	//https://en.wikipedia.org/wiki/List_of_thermal_conductivities
	/** [W / (m * K)] */
	public final double thermalConductivity;
	public final double emissivity;


	BakingPanMaterial(final double specificHeat, final double thermalConductivity, final double emissivity){
		this.specificHeat = specificHeat;
		this.thermalConductivity = thermalConductivity;
		this.emissivity = emissivity;
	}

}
