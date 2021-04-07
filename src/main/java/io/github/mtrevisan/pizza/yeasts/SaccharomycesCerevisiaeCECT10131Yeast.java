package io.github.mtrevisan.pizza.yeasts;


/**
 * Saccharomyces cerevisiae (strain CECT 10131) constants
 *
 * @see <a href="https://aem.asm.org/content/aem/77/7/2292.full.pdf">Temperature adaptation markedly determines evolution within the genus Saccharomyces</a>
 */
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
	double getMaximumSpecificGrowthRate(){
		return 0.449;
	}

}
