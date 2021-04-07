package io.github.mtrevisan.pizza.yeasts;


/**
 * Candida milleri constants
 *
 * @see <a href="https://aem.asm.org/content/aem/77/7/2292.full.pdf">Temperature adaptation markedly determines evolution within the genus Saccharomyces</a>
 */
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
	double getMaximumSpecificGrowthRate(){
		return 0.42;
	}

}
