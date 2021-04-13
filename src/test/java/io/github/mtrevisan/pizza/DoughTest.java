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
	void twoStages() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., 5.);
		final LeaveningStage stage2 = LeaveningStage.create(25., 1.);
		final double yeast = dough.backtrackStages(stage1, stage2);

		Assertions.assertEquals(0.006_38, yeast, 0.000_01);
	}

	@Test
	void twoStagesSameTemperature() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., 5.);
		final LeaveningStage stage2 = LeaveningStage.create(35., 1.);
		final double yeast = dough.backtrackStages(stage1, stage2);

		Assertions.assertEquals(0.006_14, yeast, 0.000_01);
	}

	@Test
	void singleStageNoYeastPossible() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., 1.);
		Assertions.assertThrows(YeastException.class, () -> dough.backtrackStages(stage1),
			"No yeast quantity will ever be able to produce the given expansion ratio");
	}

	@Test
	void singleStage() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., 5.);
		final double yeast = dough.backtrackStages(stage1);

		Assertions.assertEquals(0.011_86, yeast, 0.000_01);
	}


	@Test
	void sugarFactorMin() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void sugarFactorHalfway() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSugar(Dough.SUGAR_MAX / 2., 1., 0.);
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(0.279_338, factor, 0.000_001);
	}

	@Test
	void sugarFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSugar(Dough.SUGAR_MAX, 1., 0.);
		final double factor = dough.sugarFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void saltFactorMin() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.saltFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void saltFactorHalfway() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSalt(Dough.SALT_MAX / 2.);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(0.834_505, factor, 0.000_001);
	}

	@Test
	void saltFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSalt(Dough.SALT_MAX * 0.99);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(0.026_662, factor, 0.000_001);
	}


	@Test
	void waterFactorMin() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(Dough.HYDRATION_MIN);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void waterFactorHalfway() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration((Dough.HYDRATION_MIN + Dough.HYDRATION_MAX) / 2.);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(1.048_900, factor, 0.000_001);
	}

	@Test
	void waterFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addHydration(Dough.HYDRATION_MAX);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void chlorineDioxideFactorMin() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorHalfway() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.withChlorineDioxide(Dough.CHLORINE_DIOXIDE_MAX / 2.);
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(0.5, factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.withChlorineDioxide(Dough.CHLORINE_DIOXIDE_MAX * 0.99);
		final double factor = dough.chlorineDioxideFactor();

		Assertions.assertEquals(0.01, factor, 0.000_001);
	}


	@Test
	void airPressureFactor1atm() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.atmosphericPressureFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void airPressureFactor10000atm() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.withAtmosphericPressure(Dough.ONE_ATMOSPHERE * 10_000.);
		final double factor = dough.atmosphericPressureFactor();

		Assertions.assertEquals(0.986_037, factor, 0.000_001);
	}

}
