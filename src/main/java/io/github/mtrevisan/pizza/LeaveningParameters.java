package io.github.mtrevisan.pizza;


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

}
