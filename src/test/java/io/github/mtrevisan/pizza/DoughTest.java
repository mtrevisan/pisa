package io.github.mtrevisan.pizza;

import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class DoughTest{

	//[hPa]
	private static final double ATMOSPHERE = 1013.25;


	@Test
	void test(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final DoughParameters params = DoughParameters.create(0., 0., 0., 0.6, 0., 1013.25);
		dough.backtrackStages(params, LeaveningStage.create(35., 1.), LeaveningStage.create(25., 5.));
	}


	@Test
	void gasProduction(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final DoughParameters params = DoughParameters.create(0., 0., 0., 0.6, 0., 1013.25);
		//25 hrs?
		final double gas = dough.volumeExpansionRatio(0.004, 25., params, 6.);

		Assertions.assertEquals(1.477_229, gas, 0.000_001);
	}


	@Test
	void sugarFactorMin(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.sugarFactor(0.);

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void sugarFactorHalfway(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.sugarFactor(Dough.SUGAR_MAX / 2.);

		Assertions.assertEquals(0.279_338, factor, 0.000_001);
	}

	@Test
	void sugarFactorMax(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.sugarFactor(Dough.SUGAR_MAX);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void saltFactorMin(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.saltFactor(0.);

		Assertions.assertEquals(0.95, factor, 0.000_001);
	}

	@Test
	void saltFactorHalfway(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.saltFactor(0.05);

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void saltFactorMax(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.saltFactor(0.1);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void waterFactorMin(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.waterFactor(Dough.HYDRATION_MIN);

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void waterFactorHalfway(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.waterFactor((Dough.HYDRATION_MIN + Dough.HYDRATION_MAX) / 2.);

		Assertions.assertEquals(1.048_900, factor, 0.000_001);
	}

	@Test
	void waterFactorMax(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.waterFactor(Dough.HYDRATION_MAX);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void chlorineDioxideFactorMin(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.chlorineDioxideFactor(0.);

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorHalfway(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.chlorineDioxideFactor(Dough.CHLORINE_DIOXIDE_MAX / 2.);

		Assertions.assertEquals(0.5, factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorMax(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.chlorineDioxideFactor(Dough.CHLORINE_DIOXIDE_MAX);

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void airPressureFactor1atm(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.airPressureFactor(ATMOSPHERE);

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void airPressureFactor10000atm(){
		final Dough dough = new Dough(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.airPressureFactor(ATMOSPHERE * 10_000.);

		Assertions.assertEquals(0.986_037, factor, 0.000_001);
	}

}
