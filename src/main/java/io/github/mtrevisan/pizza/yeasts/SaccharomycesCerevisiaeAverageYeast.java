package io.github.mtrevisan.pizza.yeasts;


//Saccharomyces cerevisiae (average) constants:
//https://aem.asm.org/content/aem/77/7/2292.full.pdf
public class SaccharomycesCerevisiaeAverageYeast extends YeastModelAbstract{

	@Override
	public double getTemperatureMax(){
		return 45.39;
	}

	@Override
	double getTemperatureOpt(){
		return 32.27;
	}

	@Override
	public double getTemperatureMin(){
		return 2.84;
	}

	@Override
	double getMuOpt(){
		return 0.368;
	}

}
