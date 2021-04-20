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


public class Oven{


	OvenType ovenType;
	/** Whether this oven has top heating */
	boolean hasTopHeating;
	/** Whether this oven has bottom heating */
	boolean hasBottomHeating;

	Double bakingTemperatureTop;
	Double bakingTemperatureBottom;


	public static Oven create(){
		return new Oven();
	}

	private Oven(){}

	public Oven withOvenType(final OvenType ovenType) throws DoughException{
		if(ovenType == null)
			throw DoughException.create("Missing oven type");

		this.ovenType = ovenType;

		return this;
	}

	public Oven withHasTopHeating(){
		this.hasTopHeating = true;

		return this;
	}

	public Oven withHasBottomHeating(){
		this.hasBottomHeating = true;

		return this;
	}

	public Oven withBakingTemperatureTop(final double bakingTemperatureTop) throws DoughException{
		if(bakingTemperatureTop <= 0.)
			throw DoughException.create("Baking top temperature too low");

		this.bakingTemperatureTop = bakingTemperatureTop;

		return this;
	}

	public Oven withBakingTemperatureBottom(final double bakingTemperatureBottom) throws DoughException{
		if(bakingTemperatureBottom <= 0.)
			throw DoughException.create("Baking bottom temperature too low");

		this.bakingTemperatureBottom = bakingTemperatureBottom;

		return this;
	}

	public void validate() throws DoughException{
		if(ovenType == null)
			throw DoughException.create("Oven type must be given");
	}

}
