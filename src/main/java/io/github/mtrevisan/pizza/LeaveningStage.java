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


public class LeaveningStage{

	//V_after = V_prior * (1 - VOLUME_DECREASE_CUT) [%]
	public static final double VOLUME_DECREASE_CUT_DEFAULT = 0.4187;


	/** Leavening temperature [°C]. */
	final double temperature;
	/** Leavening duration [hrs]. */
	final double duration;
	/** Volume decrease after leavening stage [%]. */
	double volumeDecrease;


	public static LeaveningStage create(final double temperature, final double duration){
		return new LeaveningStage(temperature, duration);
	}

	private LeaveningStage(final double temperature, final double duration){
		this.temperature = temperature;
		this.duration = duration;
	}

	public LeaveningStage withVolumeDecrease(final double volumeReduction){
		this.volumeDecrease = volumeReduction;

		return this;
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "{" + temperature + " °C for " + duration + " hrs"
			+ (volumeDecrease > 0.? ", volume decrease " + Helper.round(volumeDecrease * 100., 1) + "%": "")
			+ "}";
	}

}
