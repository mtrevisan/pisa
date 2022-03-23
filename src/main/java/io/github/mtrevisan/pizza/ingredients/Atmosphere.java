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


public final class Atmosphere{

	/** Standard ambient pressure [hPa]. */
	private static final double STANDARD_AMBIENT_PRESSURE = 1013.25;


	/** Atmospheric pressure [hPa]. */
	public final double pressure;
	/** Relative humidity of the air [% w/w]. */
	public final Double relativeHumidity;


	/**
	 * @return	The instance.
	 * @throws DoughException	If there are errors in the parameters' values.
	 */
	public static Atmosphere create() throws DoughException{
		return new Atmosphere(STANDARD_AMBIENT_PRESSURE);
	}

	/**
	 * @param pressure	Atmospheric pressure [hPa].
	 * @return	The instance.
	 * @throws DoughException	If there are errors in the parameters' values.
	 */
	public static Atmosphere create(final double pressure) throws DoughException{
		if(pressure <= 0.)
			throw DoughException.create("Pressure must be positive");

		return new Atmosphere(pressure);
	}

	/**
	 * @param pressure	Atmospheric pressure [hPa].
	 * @param relativeHumidity	Relative humidity of the air [% w/w].
	 * @return	The instance.
	 * @throws DoughException	If there are errors in the parameters' values.
	 */
	public static Atmosphere create(final double pressure, final double relativeHumidity) throws DoughException{
		if(pressure <= 0.)
			throw DoughException.create("Pressure must be positive");
		if(relativeHumidity <= 0.)
			throw DoughException.create("Relative humidity must be positive");

		return new Atmosphere(pressure, relativeHumidity);
	}

	private Atmosphere(final double pressure){
		this.pressure = pressure;
		this.relativeHumidity = null;
	}

	private Atmosphere(final double pressure, final double relativeHumidity){
		this.pressure = pressure;
		this.relativeHumidity = relativeHumidity;
	}

}
