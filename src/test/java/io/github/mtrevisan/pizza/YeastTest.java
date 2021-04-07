package io.github.mtrevisan.pizza;

import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class YeastTest{

	//[hPa]
	private static final double ATMOSPHERE = 1013.25;


	@Test
	void gasProduction() throws Exception{
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		//25 hrs?
		final double gas = yeast.volumeExpansionRatio(0.0003, 25., 0., 0., 0., 0.60,
			0., 1013.25, 6.);

		Assertions.assertEquals(0.045_940, gas, 0.000_001);
	}


	@Test
	void sugarFactorMin(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.sugarFactor(0.);

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void sugarFactorHalfway(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.sugarFactor(Yeast.SUGAR_MAX / 2.);

		Assertions.assertEquals(0.279_338, factor, 0.000_001);
	}

	@Test
	void sugarFactorMax(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.sugarFactor(Yeast.SUGAR_MAX);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void saltFactorMin(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.saltFactor(0.);

		Assertions.assertEquals(0.95, factor, 0.000_001);
	}

	@Test
	void saltFactorHalfway(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.saltFactor(0.05);

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void saltFactorMax(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.saltFactor(0.1);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void waterFactorMin(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.waterFactor(Yeast.HYDRATION_MIN);

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void waterFactorHalfway(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.waterFactor((Yeast.HYDRATION_MIN + Yeast.HYDRATION_MAX) / 2.);

		Assertions.assertEquals(1.048_900, factor, 0.000_001);
	}

	@Test
	void waterFactorMax(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.waterFactor(Yeast.HYDRATION_MAX);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void chlorineDioxideFactorMin(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.chlorineDioxideFactor(0.);

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorHalfway(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.chlorineDioxideFactor(Yeast.CHLORINE_DIOXIDE_MAX / 2.);

		Assertions.assertEquals(0.5, factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorMax(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.chlorineDioxideFactor(Yeast.CHLORINE_DIOXIDE_MAX);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void airPressureFactor1atm(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.airPressureFactor(ATMOSPHERE);

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void airPressureFactor10000atm(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.airPressureFactor(ATMOSPHERE * 10_000.);

		Assertions.assertEquals(0.986_037, factor, 0.000_001);
	}

}
