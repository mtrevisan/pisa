package io.github.mtrevisan.pizza;

import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class DoughTest{

	//[hPa]
	private static final double ATMOSPHERE = 1013.25;


	@Test
	void stages(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE);
		final LeaveningStage stage1 = LeaveningStage.create(35., 1.);
		final LeaveningStage stage2 = LeaveningStage.create(25., 5.);
		final double yeast = dough.backtrackStages(stage1, stage2);

		Assertions.assertEquals(0.004_87, yeast, 0.000_01);
	}

	@Test
	void singleStage(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE);
		final LeaveningStage stage1 = LeaveningStage.create(35., 1.);
		final double yeast = dough.backtrackStages(stage1);

		Assertions.assertEquals(0.038_9, yeast, 0.000_01);
	}


	@Test
	void gasProduction(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE);
		final double gas = dough.volumeExpansionRatio(0.004, 25., 6.);

		Assertions.assertEquals(1.512_967, gas, 0.000_001);
	}


	@Test
	void sugarFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE);
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void sugarFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			Dough.SUGAR_MAX / 2., 0., 0., 0.6, 0., ATMOSPHERE);
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(0.279_338, factor, 0.000_001);
	}

	@Test
	void sugarFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			Dough.SUGAR_MAX, 0., 0., 0.6, 0., ATMOSPHERE);
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void saltFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void saltFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., Dough.SALT_MAX / 2., 0.6, 0., ATMOSPHERE);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(0.834_505, factor, 0.000_001);
	}

	@Test
	void saltFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., Dough.SALT_MAX, 0.6, 0., ATMOSPHERE);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(0.000_1, factor, 0.000_001);
	}


	@Test
	void waterFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., Dough.HYDRATION_MIN, 0., ATMOSPHERE);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void waterFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., (Dough.HYDRATION_MIN + Dough.HYDRATION_MAX) / 2., 0., ATMOSPHERE);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(1.048_900, factor, 0.000_001);
	}

	@Test
	void waterFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., Dough.HYDRATION_MAX, 0., ATMOSPHERE);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void chlorineDioxideFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE);
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, Dough.CHLORINE_DIOXIDE_MAX / 2., ATMOSPHERE);
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(0.5, factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, Dough.CHLORINE_DIOXIDE_MAX, ATMOSPHERE);
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void airPressureFactor1atm(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE);
		final double factor = dough.atmosphericPressureFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void airPressureFactor10000atm(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast(),
			0., 0., 0., 0.6, 0., ATMOSPHERE * 10_000.);
		final double factor = dough.atmosphericPressureFactor();

		Assertions.assertEquals(0.986_037, factor, 0.000_001);
	}

}
