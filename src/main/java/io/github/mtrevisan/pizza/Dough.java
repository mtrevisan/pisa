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


public class Dough{

	private final Water water = new Water();


	/**
	 * @param airRelativeHumidity	Relative humidity of air [%].
	 * @return	Dough umidity [%].
	 */
	public final double humidity(final double airRelativeHumidity){
		return Math.pow(0.035 * airRelativeHumidity, 2.);
	}

	/**
	 * @see <a href="https://shodhganga.inflibnet.ac.in/bitstream/10603/149607/15/10_chapter%204.pdf">Density studies of sugar solutions</a>
	 * @see <a href="https://core.ac.uk/download/pdf/197306213.pdf">Kubota, Matsumoto, Kurisu, Sizuki, Hosaka. The equations regarding temperature and concentration of the density and viscosity of sugar, salt and skim milk solutions. 1980.</a>
	 * @see <a href="https://www.researchgate.net/publication/280063894_Mathematical_modelling_of_density_and_viscosity_of_NaCl_aqueous_solutions">Simion, Grigoras, Rosu, Gavrila. Mathematical modelling of density and viscosity of NaCl aqueous solutions. 2014.</a>
	 * @see <a href="https://www.engineeringtoolbox.com/slurry-density-calculate-d_1188.html">Calculate density of a slurry</a>
	 * @see <a href="https://www.academia.edu/2421508/Characterisation_of_bread_doughs_with_different_densities_salt_contents_and_water_levels_using_microwave_power_transmission_measurements">Campbell. Characterisation of bread doughs with different densities, salt contents and water levels using microwave power transmission measurements. 2005.</a>
	 */
	public final double volume(final LeaveningParameters params){
		//convert salt to [g/l]
		//final double salt = params.salt * 1000 / params.hydration;
		//calculateDoughVolume = 1.41 - (0.0026 * params.water - 0.0064 * salt) - 0.0000676 * params.atmosphericPressure

		//true formula should be the following, but the salt is accounted next, so here it is zero
		//final double waterDensity = calculateWaterDensity(params.salt * 1000 / params.hydration, params.doughTemperature, params.atmosphericPressure);
		final double waterDensity = water.density(0, params.doughTemperature, params.atmosphericPressure);
		final double brineDensity = water.brineDensity(0, params.hydration, params.salt, params.sugar, params.doughTemperature);

		//density of flour + water + salt + sugar
		double doughDensity = 1.41 - (0.002611 * waterDensity * params.hydration - brineDensity) - 0.0000676 * params.atmosphericPressure;

		//account for fats (convert fat to [g/l])
		final double fat = params.fat * 1000 / params.hydration;
		doughDensity = ((params.dough - fat) * doughDensity + fat / params.fatDensity) / params.dough;

		return params.dough / doughDensity;
	}

}
