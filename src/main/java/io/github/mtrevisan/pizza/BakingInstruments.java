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


import io.github.mtrevisan.pizza.bakingpans.BakingPanAbstract;


public class BakingInstruments{

	BakingPanAbstract[] bakingPans;
	OvenType ovenType;
	double bakingTemperatureTop;
	double bakingTemperatureBottom;


	public BakingInstruments withBakingPans(final BakingPanAbstract[] bakingPans) throws DoughException{
		if(bakingPans == null || bakingPans.length == 0)
			throw DoughException.create("Missing baking pans");

		this.bakingPans = bakingPans;

		return this;
	}

	public BakingInstruments withOvenType(final OvenType ovenType) throws DoughException{
		if(ovenType == null)
			throw DoughException.create("Missing oven type");

		this.ovenType = ovenType;

		return this;
	}

	public double getBakingPansTotalArea(){
		double area = 0.;
		for(final BakingPanAbstract bakingPan : bakingPans)
			area += bakingPan.area();
		return area;
	}

}
