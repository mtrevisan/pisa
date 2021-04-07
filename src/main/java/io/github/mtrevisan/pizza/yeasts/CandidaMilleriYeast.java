package io.github.mtrevisan.pizza.yeasts;


//Candida milleri constants:
//https://aem.asm.org/content/aem/77/7/2292.full.pdf
public class CandidaMilleriYeast extends YeastModelAbstract{

	@Override
	public double getTemperatureMax(){
		//± 0.3 °C
		return 35.9;
	}

	@Override
	double getTemperatureOpt(){
		return 27.;
	}

	@Override
	public double getTemperatureMin(){
		//± 1 °C
		return 8.;
	}

	@Override
	double getMuOpt(){
		return 0.42;
	}

}
