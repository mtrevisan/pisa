package io.github.mtrevisan.pizza;

import io.github.mtrevisan.pizza.utils.Helper;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;


public class LeaveningParameters{

	//[% w/w]
	double idy;
	//[% w/w]
	double sugar;
	//[% w/w]
	double fat;
	//can be one of ['olive oil', 'lard']
	String fatType;
	//fat density [kg/l]
	double fatDensity;
	//[% w/w]
	double salt;
	//[% w/w]
	double hydration;
	//[mg/l]
	double chlorineDioxide;
	//[hPa]
	double atmosphericPressure;
	//[°C]
	double ambientTemperature;
	//[hrs]
	double[] leaveningTime;
	//[°C]
	double[] temperature;
	//target area [cm^2]
	double targetArea;
	//target height [cm]
	double targetHeight;
	//final dough weight [g]
	double dough;
	//final dough temperature [°C]
	double doughTemperature;
	//baking temperature [°C]
	double bakingTemperature;

	//target volume over the maximum attainable volume [% v/v]
	double targetVolume;
	//volume at each stage [% v/v]
	double[] volume;
	//final volume ratio at the end of all the stages
	double finalVolumeRatio;


	public String validate(){
		final YeastModelAbstract yeastModel = new SaccharomycesCerevisiaeCECT10131Yeast();

		//FIXME
//		targetVolume = getTargetVolume(params);

		final double waterBoilingTemp = Water.boilingTemperature(salt * 1000. / hydration, atmosphericPressure);

		if(hydration < 0.)
			return "water [% w/w] cannot be less than zero";
		if(chlorineDioxide < 0.)
			return "chlorine dioxide [mg/l] cannot be less than zero";
		if(salt < 0.)
			return "salt [% w/w] cannot be less than zero";
		if(fat < 0.)
			return "fat [% w/w] cannot be less than zero";
		if(sugar < 0.)
			return "sugar [% w/w] cannot be less than zero";
		if(idy < 0.)
			return "yeast [% w/w] cannot be less than zero";
		if(atmosphericPressure <= 0. || atmosphericPressure >= Dough.ATMOSPHERIC_PRESSURE_MAX)
			return "Atmospheric pressure [hPa] must be between 0 and " + Helper.round(Dough.ATMOSPHERIC_PRESSURE_MAX, 0)
				+ " hPa";
		if(bakingTemperature <= waterBoilingTemp)
			return "Baking temperature [°C] must be greater than water boiling temperature (" + Helper.round(waterBoilingTemp, 1)
				+ " °C)";
		if(targetHeight <= 0.)
			return "targetHeight [cm] cannot be less than or equal to zero";
		//FIXME
//		if(targetVolume <= 0. || targetVolume > 1.)
//			return "targetVolume [% v/v] cannot be less than or equal to zero or greater than or equal to one";

		final int size = temperature.length;
		if(size != leaveningTime.length)
			return "temperature [°C] and leaveningTime [hrs] must have the same length";

		for(int index = 0; index < size; index ++)
			if(temperature[index] <= yeastModel.getTemperatureMin() || temperature[index] >= yeastModel.getTemperatureMax())
				return "temperature [°C] at stage " + index + " must be between " + Helper.round(yeastModel.getTemperatureMin(), 1)
					+ " °C and " + Helper.round(yeastModel.getTemperatureMax(), 1) + " °C";
		for(int index = 0; index < size; index ++)
			if(leaveningTime[index] < 0.)
				return "leavening time at stage " + index + " cannot be less than zero";
		return null;
	}

}
