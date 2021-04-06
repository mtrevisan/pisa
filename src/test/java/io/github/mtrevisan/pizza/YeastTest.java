package io.github.mtrevisan.pizza;

import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class YeastTest{

	//[hPa]
	private static final double ATMOSPHERE = 1013.25;


	@Test
	void maximumSpecificGrowthRateMin(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.maximumSpecificGrowthRate(0.52, 0.);

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void maximumSpecificGrowthRateHalfway(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.maximumSpecificGrowthRate(0.52, 32.86);

		Assertions.assertEquals(0.449, factor, 0.000_1);
	}

	@Test
	void maximumSpecificGrowthRateTwoStages(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());


		double fy = yeast.backtrackStage(16.7, 21., 10., 26.);
//		//last stage: 26 hours at 10 °C
//		double fy = Math.pow(26 / 0.0665, 1. / -0.7327);
//		double base = yeast.maximumSpecificGrowthRate(fy, 10.);
//		//preceded
//		double speed = yeast.maximumSpecificGrowthRate(fy, 16.7);
//		//find hours at second-to-last stage temperature
//		double t2 = 0.0665 * Math.pow(fy * speed / base, -0.7327);
//		//add second-to-last stage duration
//		t2 += 21;
//		//find fy at t2
//		fy = Math.pow(t2 / 0.0665, 1. / -0.7327);

		final double factor = yeast.maximumSpecificGrowthRate(0.52, 32.86);

		Assertions.assertEquals(0.449, factor, 0.000_1);
	}

	@Test
	void maximumSpecificGrowthRateMax(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.maximumSpecificGrowthRate(0.52, 50.);

		Assertions.assertEquals(0., factor, 0.000_001);
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

		Assertions.assertEquals(1.25, factor, 0.000_001);
	}

	@Test
	void saltFactorHalfway(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.saltFactor(0.05);

		Assertions.assertEquals(0.132_122, factor, 0.000_001);
	}

	@Test
	void saltFactorMax(){
		final Yeast yeast = new Yeast(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = yeast.saltFactor(0.1);

		Assertions.assertEquals(0.000_528, factor, 0.000_001);
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
