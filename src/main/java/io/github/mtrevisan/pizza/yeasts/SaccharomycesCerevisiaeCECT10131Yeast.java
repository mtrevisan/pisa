package io.github.mtrevisan.pizza.yeasts;


//Saccharomyces cerevisiae (strain CECT 10131) constants:
//https://aem.asm.org/content/aem/77/7/2292.full.pdf
public class SaccharomycesCerevisiaeCECT10131Yeast extends YeastModelAbstract{

	@Override
	public double getTemperatureMax(){
		return 45.9;
	}

	@Override
	double getTemperatureOpt(){
		return 32.8;
	}

	@Override
	public double getTemperatureMin(){
		return 0.74;
	}

	@Override
	double getMuOpt(){
		return 0.449;
	}

}
