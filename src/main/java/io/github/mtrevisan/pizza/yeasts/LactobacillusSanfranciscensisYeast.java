package io.github.mtrevisan.pizza.yeasts;


//Lactobacillus sanfranciscensis constants:
//https://aem.asm.org/content/aem/77/7/2292.full.pdf
public class LactobacillusSanfranciscensisYeast implements YeastModelInterface{

	@Override
	public double getTemperatureMax(){
		//± 0.1 °C
		return 41.0;
	}

	@Override
	public double getTemperatureOpt(){
		//± 0.5 °C
		return 32.5;
	}

	@Override
	public double getTemperatureMin(){
		//± 1.5 °C
		return 4.5;
	}

	@Override
	public double getMuOpt(){
		return 0.71;
	}

	@Override
	public double getLambda(){
		return 8_081.;
	}
}
