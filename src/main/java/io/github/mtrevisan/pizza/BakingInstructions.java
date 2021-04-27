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

import java.time.Duration;


public final class BakingInstructions{

	/** Baking temperature [°C]. */
	private Double bakingTemperature;
	private Duration bakingDuration;


	public static BakingInstructions create(){
		return new BakingInstructions();
	}

	private BakingInstructions(){}

	/**
	 * @param bakingTemperature	Baking temperature [°C].
	 * @return	The instance.
	 */
	public BakingInstructions withBakingTemperature(final double bakingTemperature){
		this.bakingTemperature = bakingTemperature;

		return this;
	}

	/**
	 * @return	Baking temperature [°C].
	 */
	public Double getBakingTemperature(){
		return bakingTemperature;
	}

	/**
	 * @param bakingDuration	Baking duration.
	 * @return	The instance.
	 */
	public BakingInstructions withBakingDuration(final Duration bakingDuration){
		this.bakingDuration = bakingDuration;

		return this;
	}

	/**
	 * @return	Baking temperature [°C].
	 */
	public Duration getBakingDuration(){
		return bakingDuration;
	}


	@Override
	public String toString(){
		return (bakingTemperature != null? "baking at " + Helper.round(bakingTemperature, 1) + " °C"
				+ (bakingDuration != null? " for " + bakingDuration.toSeconds() + " s": ""): "");
	}

}
