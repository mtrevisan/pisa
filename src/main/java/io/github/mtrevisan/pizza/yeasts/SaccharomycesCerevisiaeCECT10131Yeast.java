package io.github.mtrevisan.pizza.yeasts;


//Saccharomyces cerevisiae (strain CECT 10131) constants:
//https://aem.asm.org/content/aem/77/7/2292.full.pdf
public class SaccharomycesCerevisiaeCECT10131Yeast implements YeastModelInterface{

	@Override
	public double getTemperatureMax(){
		return 45.9;
	}

	@Override
	public double getTemperatureOpt(){
		return 32.8;
	}

	@Override
	public double getTemperatureMin(){
		return 0.74;
	}

	@Override
	public double getMuOpt(){
		return 0.449;
	}

	@Override
	public double getLambda(){
		return 11_844.;
	}
}
