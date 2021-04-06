package io.github.mtrevisan.pizza.yeasts;


public interface YeastModelInterface{

	//[°C]
	double getTemperatureMax();

	//[°C]
	double getTemperatureOpt();

	//[°C]
	double getTemperatureMin();

	//[hrs^-1]
	double getMuOpt();

	//[hrs] (calculated to obtain a maximum specific grow rate of MU_OPT at T_MAX)
	double getLambda();

}
