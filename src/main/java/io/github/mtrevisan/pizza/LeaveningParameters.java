package io.github.mtrevisan.pizza;


public class LeaveningParameters{

	//can be one of ['olive oil', 'lard']
	String fatType;
	//fat density [kg/l]
	double fatDensity;
	//target area [cm^2]
	double targetArea;
	//target height [cm]
	double targetHeight;

	//target volume over the maximum attainable volume [% v/v]
	double targetVolume;
	//volume at each stage [% v/v]
	double[] volume;
	//final volume ratio at the end of all the stages
	double finalVolumeRatio;


	public String validate(){
		//FIXME
//		targetVolume = getTargetVolume(params);

		if(targetHeight <= 0.)
			return "targetHeight [cm] cannot be less than or equal to zero";
		//FIXME
//		if(targetVolume <= 0. || targetVolume > 1.)
//			return "targetVolume [% v/v] cannot be less than or equal to zero or greater than or equal to one";

		return null;
	}

}
