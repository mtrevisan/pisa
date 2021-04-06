package io.github.mtrevisan.pizza.yeasts;


//Candida milleri constants:
//https://aem.asm.org/content/aem/77/7/2292.full.pdf
public class CandidaMilleriYeast implements YeastModelInterface{

	@Override
	public double getTemperatureMax(){
		//± 0.3 °C
		return 35.9;
	}

	@Override
	public double getTemperatureOpt(){
		return 27.;
	}

	@Override
	public double getTemperatureMin(){
		//± 1 °C
		return 8.;
	}

	@Override
	public double getMuOpt(){
		return 0.42;
	}

	@Override
	public double getLambda(){
		return 60_702.;
	}
}
