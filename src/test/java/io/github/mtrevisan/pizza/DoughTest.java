/**
 * Copyright (c) 2019-2020 Mauro Trevisan
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.pizza;

import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class DoughTest{

	@Test
	void stages(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., 1.);
		final LeaveningStage stage2 = LeaveningStage.create(25., 5.);
		final double yeast = dough.backtrackStages(stage1, stage2);

		Assertions.assertEquals(0.004_87, yeast, 0.000_01);
	}

	@Test
	void singleStage(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., 1.);
		final double yeast = dough.backtrackStages(stage1);

		Assertions.assertEquals(0.038_9, yeast, 0.000_01);
	}


	@Test
	void gasProduction(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(0.6);
		final double gas = dough.volumeExpansionRatio(0.004, 25., 6.);

		Assertions.assertEquals(1.512_967, gas, 0.000_001);
	}


	@Test
	void sugarFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void sugarFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSugar(Dough.SUGAR_MAX / 2., 1., 0.);
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(0.279_338, factor, 0.000_001);
	}

	@Test
	void sugarFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSugar(Dough.SUGAR_MAX, 1., 0.);
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void saltFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.saltFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void saltFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSalt(Dough.SALT_MAX / 2.);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(0.834_505, factor, 0.000_001);
	}

	@Test
	void saltFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSalt(Dough.SALT_MAX * 0.99);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(0.026_662, factor, 0.000_001);
	}


	@Test
	void waterFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(Dough.HYDRATION_MIN);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void waterFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration((Dough.HYDRATION_MIN + Dough.HYDRATION_MAX) / 2.);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(1.048_900, factor, 0.000_001);
	}

	@Test
	void waterFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(Dough.HYDRATION_MAX);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void chlorineDioxideFactorMin(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorHalfway(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.withChlorineDioxide(Dough.CHLORINE_DIOXIDE_MAX / 2.);
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(0.5, factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorMax(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.withChlorineDioxide(Dough.CHLORINE_DIOXIDE_MAX * 0.99);
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(0.01, factor, 0.000_001);
	}


	@Test
	void airPressureFactor1atm(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.atmosphericPressureFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void airPressureFactor10000atm(){
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.withAtmosphericPressure(Dough.ONE_ATMOSPHERE * 10_000.);
		final double factor = dough.atmosphericPressureFactor();

		Assertions.assertEquals(0.986_037, factor, 0.000_001);
	}

}
