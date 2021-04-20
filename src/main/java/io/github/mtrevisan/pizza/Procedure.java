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
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;


public final class Procedure{

	private static final Logger LOGGER = LoggerFactory.getLogger(Procedure.class);


	final LeaveningStage[] leaveningStages;
	final double targetDoughVolumeExpansionRatio;
	final int targetVolumeExpansionRatioAtLeaveningStage;
	StretchAndFoldStage[] stretchAndFoldStages;

	final Duration doughMaking;
	final Duration[] stagesWork;
	final Duration seasoning;
	final LocalTime timeToBake;


	/**
	 * @param leaveningStages	Data for stages.
	 * @param targetVolumeExpansionRatio	Maximum target volume expansion ratio to reach.
	 * @param targetVolumeExpansionRatioAtLeaveningStage	Leavening stage in which to reach the given volume expansion ratio (index
	 * 	between 0 and `leaveningStages.length`).
	 * @param doughMaking	Duration of dough making.
	 * @param stagesWork	Duration done at the end of each stage.
	 * @param seasoning	Duration of seasoning.
	 * @param timeToBake	Time to start baking.
	 */
	public static Procedure create(final LeaveningStage[] leaveningStages, final double targetVolumeExpansionRatio,
			final int targetVolumeExpansionRatioAtLeaveningStage, final Duration doughMaking, final Duration[] stagesWork,
			final Duration seasoning, final LocalTime timeToBake) throws DoughException{
		return new Procedure(leaveningStages, targetVolumeExpansionRatio, targetVolumeExpansionRatioAtLeaveningStage,
			doughMaking, stagesWork, seasoning, timeToBake);
	}

	private Procedure(final LeaveningStage[] leaveningStages, final double targetDoughVolumeExpansionRatio,
			final int targetVolumeExpansionRatioAtLeaveningStage, final Duration doughMaking, final Duration[] stagesWork,
			final Duration seasoning, final LocalTime timeToBake) throws DoughException{
		if(targetVolumeExpansionRatioAtLeaveningStage < 0 || targetVolumeExpansionRatioAtLeaveningStage >= leaveningStages.length)
			throw DoughException.create("Target volume expansion ratio at leavening stage must be between 0 and {}",
				(leaveningStages.length - 1));
		Objects.requireNonNull(doughMaking, "Time to make the dough not set");
		Objects.requireNonNull(seasoning, "Time to season not set");
		Objects.requireNonNull(timeToBake, "Time to bake not set");
		if(stagesWork == null || stagesWork.length != leaveningStages.length)
			throw DoughException.create("Number of work at each stage does not match number of leavening stages");

		this.leaveningStages = leaveningStages;
		this.targetDoughVolumeExpansionRatio = targetDoughVolumeExpansionRatio;
		this.targetVolumeExpansionRatioAtLeaveningStage = targetVolumeExpansionRatioAtLeaveningStage;
		this.timeToBake = timeToBake;
		this.doughMaking = doughMaking;
		this.stagesWork = stagesWork;
		this.seasoning = seasoning;
	}

	/**
	 * @param stretchAndFoldStages	Stretch & Fold stages.
	 * @return	This instance.
	 */
	public Procedure withStretchAndFoldStages(final StretchAndFoldStage[] stretchAndFoldStages){
		if(stretchAndFoldStages != null){
			Duration totalLeaveningDuration = Duration.ZERO;
			for(final LeaveningStage leaveningStage : leaveningStages)
				totalLeaveningDuration = totalLeaveningDuration.plus(leaveningStage.duration);
			Duration totalStretchAndFoldDuration = Duration.ZERO;
			for(final StretchAndFoldStage stretchAndFoldStage : stretchAndFoldStages)
				totalStretchAndFoldDuration = totalStretchAndFoldDuration.plus(stretchAndFoldStage.lapse);
			if(totalStretchAndFoldDuration.compareTo(totalLeaveningDuration) > 0)
				LOGGER.warn("Duration of overall stretch & fold phases is longer than duration of leavening stages by {} hrs",
					Helper.round(totalStretchAndFoldDuration.minus(totalLeaveningDuration).toMinutes() / 60., 2));
		}

		this.stretchAndFoldStages = stretchAndFoldStages;

		return this;
	}

	public void validate(final YeastModelAbstract yeastModel) throws DoughException{
		if(leaveningStages == null)
			throw DoughException.create("Missing leavening stage(s)");
		for(final LeaveningStage stage : leaveningStages)
			if(stage.temperature < yeastModel.getTemperatureMin() || stage.temperature > yeastModel.getTemperatureMax())
				throw DoughException.create("Stage temperature [°C] must be between {} and {} °C, was {} °C",
					Helper.round(yeastModel.getTemperatureMin(), 1), Helper.round(yeastModel.getTemperatureMax(), 1),
					Helper.round(stage.temperature, 1));
		if(targetDoughVolumeExpansionRatio <= 0.)
			throw DoughException.create("Target volume expansion ratio [% v/v] must be positive");
	}

}
