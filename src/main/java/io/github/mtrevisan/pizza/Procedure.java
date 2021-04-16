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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Procedure{

	private static final Logger LOGGER = LoggerFactory.getLogger(Dough.class);


	LeaveningStage[] leaveningStages;
	double targetVolumeExpansionRatio;
	int targetVolumeExpansionRatioAtLeaveningStage;
	StretchAndFoldStage[] stretchAndFoldStages;


	/**
	 * @param leaveningStages	Data for stages.
	 * @param targetVolumeExpansionRatio	Maximum target volume expansion ratio to reach.
	 * @param targetVolumeExpansionRatioAtLeaveningStage	Leavening stage in which to reach the given volume expansion ratio (index
	 * 	between 0 and `leaveningStages.length`).
	 */
	public static Procedure create(final LeaveningStage[] leaveningStages, final double targetVolumeExpansionRatio,
			final int targetVolumeExpansionRatioAtLeaveningStage) throws DoughException{
		return new Procedure(leaveningStages, targetVolumeExpansionRatio, targetVolumeExpansionRatioAtLeaveningStage);
	}

	private Procedure(final LeaveningStage[] leaveningStages, final double targetVolumeExpansionRatio,
			final int targetVolumeExpansionRatioAtLeaveningStage) throws DoughException{
		if(targetVolumeExpansionRatioAtLeaveningStage < 0 || targetVolumeExpansionRatioAtLeaveningStage >= leaveningStages.length)
			throw DoughException.create("Target volume expansion ratio at leavening stage must be between 0 and "
				+ (leaveningStages.length - 1));

		this.leaveningStages = leaveningStages;
		this.targetVolumeExpansionRatio = targetVolumeExpansionRatio;
		this.targetVolumeExpansionRatioAtLeaveningStage = targetVolumeExpansionRatioAtLeaveningStage;
	}

	/**
	 * @param stretchAndFoldStages	Stretch & Fold stages.
	 * @return	This instance.
	 */
	public Procedure withStretchAndFoldStages(final StretchAndFoldStage[] stretchAndFoldStages){
		if(stretchAndFoldStages != null){
			double totalLeaveningDuration = 0.;
			for(final LeaveningStage leaveningStage : leaveningStages)
				totalLeaveningDuration += leaveningStage.duration;
			double totalStretchAndFoldDuration = 0.;
			for(final StretchAndFoldStage stretchAndFoldStage : stretchAndFoldStages)
				totalStretchAndFoldDuration += stretchAndFoldStage.lapse;
			if(totalStretchAndFoldDuration > totalLeaveningDuration)
				LOGGER.warn("Duration of overall stretch & fold phases is longer than duration of leavening stages by "
					+ Helper.round(totalStretchAndFoldDuration - totalLeaveningDuration, 2) + " hrs");
		}

		this.stretchAndFoldStages = stretchAndFoldStages;

		return this;
	}

}
