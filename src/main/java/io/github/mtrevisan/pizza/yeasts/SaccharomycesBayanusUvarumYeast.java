package io.github.mtrevisan.pizza.yeasts;


//Saccharomyces bayanus var. uvarum constants:
//https://aem.asm.org/content/aem/77/7/2292.full.pdf
public class SaccharomycesBayanusUvarumYeast extends YeastModelAbstract{

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
		return 0.295;
	}

}
