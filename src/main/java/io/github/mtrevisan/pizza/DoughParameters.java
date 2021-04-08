package io.github.mtrevisan.pizza;

import io.github.mtrevisan.pizza.utils.Helper;


public class DoughParameters{

	//[%]
	double sugar;
	//[%]
	double fat;
	//[%]
	double salinity;
	//[%]
	double hydration;
	//[mg/l]
	double chlorineDioxide;
	//[hPa]
	double atmosphericPressure;


	public static DoughParameters create(final double sugar, final double fat, final double salinity, final double hydration,
			final double chlorineDioxide, final double atmosphericPressure){
		return new DoughParameters(sugar, fat, salinity, hydration, chlorineDioxide, atmosphericPressure);
	}

	private DoughParameters(final double sugar, final double fat, final double salinity, final double hydration,
			final double chlorineDioxide, final double atmosphericPressure){
		this.sugar = sugar;
		this.fat = fat;
		this.salinity = salinity;
		this.hydration = hydration;
		this.chlorineDioxide = chlorineDioxide;
		this.atmosphericPressure = atmosphericPressure;
	}

	public String validate(){
		if(hydration < 0.)
			return "hydration [%] cannot be less than zero";
		if(chlorineDioxide < 0.)
			return "chlorine dioxide [mg/l] cannot be less than zero";
		if(salinity < 0.)
			return "salt [%] cannot be less than zero";
		if(fat < 0.)
			return "fat [%] cannot be less than zero";
		if(sugar < 0.)
			return "sugar [%] cannot be less than zero";
		if(atmosphericPressure <= 0. || atmosphericPressure >= Dough.MINIMUM_INHIBITORY_PRESSURE)
			return "Atmospheric pressure [hPa] must be between 0 and " + Helper.round(Dough.MINIMUM_INHIBITORY_PRESSURE, 0)
				+ " hPa";
		return null;
	}

}
