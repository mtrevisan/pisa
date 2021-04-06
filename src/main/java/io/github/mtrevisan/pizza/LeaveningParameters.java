package io.github.mtrevisan.pizza;


import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import io.github.mtrevisan.pizza.yeasts.YeastModelInterface;


public class LeaveningParameters{

	//[%]
	double idy;
	//[%]
	double sugar;
	//can be one of ['sucrose', 'maltose', 'honey']
	String sugarType;
	//[%]
	double fat;
	//can be one of ['olive oil', 'lard']
	String fatType;
	//fat density [g/cm^3]
	double fatDensity;
	//[%]
	double salt;
	//[%]
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

	//target volume over the maximum attainable volume [%]
	double targetVolume;
	//volume at each stage [%]
	double[] volume;
	//final volume ratio at the end of all the stages
	double finalVolumeRatio;


	public String validate(){
		final YeastModelInterface yeastModel = new SaccharomycesCerevisiaeCECT10131Yeast();

		//FIXME
//		targetVolume = getTargetVolume(params);

		final Water water = new Water();
		final double waterBoilingTemp = water.boilingTemperature(salt * 1000 / hydration, atmosphericPressure);

		if(hydration < 0)
			return "hydration [%] cannot be less than zero";
		if(chlorineDioxide < 0)
			return "chlorine dioxide [mg/l] cannot be less than zero";
		if(salt < 0)
			return "salt [%] cannot be less than zero";
		if(fat < 0)
			return "fat [%] cannot be less than zero";
		if(sugar < 0)
			return "sugar [%] cannot be less than zero";
		if(idy < 0)
			return "IDY [%] cannot be less than zero";
		if(atmosphericPressure <= 0 || atmosphericPressure >= Yeast.MINIMUM_INHIBITORY_PRESSURE)
			return "Atmospheric pressure [hPa] must be between 0 and " + Helper.round(Yeast.MINIMUM_INHIBITORY_PRESSURE, 0)
				+ " hPa";
		if(doughTemperature <= yeastModel.getTemperatureMin() || doughTemperature >= yeastModel.getTemperatureMax())
			return "Dough temperature [°C] must be between " + Helper.round(yeastModel.getTemperatureMin(), 1) + " °C and "
				+ Helper.round(yeastModel.getTemperatureMax(), 1) + " °C";
		if(bakingTemperature <= waterBoilingTemp)
			return "Baking temperature [°C] must be greater than water boiling temperature (" + Helper.round(waterBoilingTemp, 1)
				+ " °C)";
		if(targetHeight <= 0)
			return "targetHeight [cm] cannot be less than or equal to zero";
		//FIXME
//		if(targetVolume <= 0 || targetVolume > 1)
//			return "targetVolume [%] cannot be less than or equal to zero or greater than or equal to one";

		final int size = temperature.length;
		if(size != leaveningTime.length)
			return "temperature [°C] and leaveningTime [hrs] must have the same length";

		for(int index = 0; index < size; index ++)
			if(temperature[index] <= yeastModel.getTemperatureMin() || temperature[index] >= yeastModel.getTemperatureMax())
				return "temperature [°C] at stage " + index + " must be between " + Helper.round(yeastModel.getTemperatureMin(), 1)
					+ " °C and " + Helper.round(yeastModel.getTemperatureMax(), 1) + " °C";
		for(int index = 0; index < size; index ++)
			if(leaveningTime[index] < 0)
				return "leavening time at stage " + index + " cannot be less than zero";
		return null;
	}

}
