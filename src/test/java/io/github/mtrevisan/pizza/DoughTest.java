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

import io.github.mtrevisan.pizza.bakingpans.BakingPanAbstract;
import io.github.mtrevisan.pizza.bakingpans.CircularBakingPan;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;


class DoughTest{

	@Test
	void singleStageNoYeastPossible() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(1));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 2., 0,
			Duration.ZERO, new Duration[]{Duration.ZERO}, Duration.ZERO, LocalTime.NOON);
		Assertions.assertThrows(YeastException.class, () -> dough.calculateYeast(procedure),
			"No yeast quantity will ever be able to produce the given expansion ratio");
	}

	@Test
	void singleStage() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 2., 0,
			Duration.ZERO, new Duration[]{Duration.ZERO}, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.011_83, dough.yeast, 0.000_01);
	}

	@Test
	void twoStages() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2., 1,
			Duration.ZERO, new Duration[]{Duration.ZERO, Duration.ZERO}, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.006_38, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesEarlyExit() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1));
		Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2., 0,
			Duration.ZERO, new Duration[]{Duration.ZERO, Duration.ZERO}, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);
		final double yeast1 = dough.yeast;
		procedure = Procedure.create(new LeaveningStage[]{stage1}, 2., 0,
			Duration.ZERO, new Duration[]{Duration.ZERO}, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);
		final double yeast2 = dough.yeast;

		Assertions.assertEquals(0.011_83, yeast1, 0.000_01);
		Assertions.assertEquals(yeast2, yeast1, 0.000_01);
	}

	@Test
	void twoStagesSameTemperature() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2., 1,
			Duration.ZERO, new Duration[]{Duration.ZERO, Duration.ZERO}, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.006_14, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesInnerVolumeDecrease() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5))
			.withVolumeDecrease(0.20);
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2., 1,
			Duration.ZERO, new Duration[]{Duration.ZERO, Duration.ZERO}, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.043_27, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesWithStretchAndFolds() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(0.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.10);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.25);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(15))
			.withVolumeDecrease(0.30);
		final StretchAndFoldStage[] stretchAndFoldStages = new StretchAndFoldStage[]{safStage1, safStage2, safStage3};
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2., 1,
			Duration.ZERO, new Duration[]{Duration.ZERO, Duration.ZERO}, Duration.ZERO, LocalTime.NOON)
			.withStretchAndFoldStages(stretchAndFoldStages);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.010_13, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesWithStretchAndFoldsReal() throws DoughException, YeastException{
		final Ingredients ingredients = Ingredients.create(741.3, 0.001)
			.withIngredientsTemperature(16.9)
			.withDoughTemperature(27.)
			.withWater(0.02)
			.withFlour(Flour.create(260.))
			.withYeast(YeastType.INSTANT_DRY)
			.withSugar(SugarType.SUCROSE)
			.withFat(0.913);
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, ingredients)
			.addSugar(0.003, ingredients)
			.addSalt(0.015)
			.addFat(0.014, ingredients)
			.withAtmosphericPressure(1012.8);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage[] stretchAndFoldStages = new StretchAndFoldStage[]{safStage1, safStage2, safStage3};
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8, 0,
			Duration.ofMinutes(10), new Duration[]{Duration.ofMinutes(10), Duration.ZERO}, Duration.ofMinutes(15), LocalTime.of(20, 0))
			.withStretchAndFoldStages(stretchAndFoldStages);
		Recipe recipe = dough.createRecipe(ingredients, procedure, new BakingPanAbstract[]{CircularBakingPan.create(24.)});

		Assertions.assertEquals(440.3, recipe.getFlour(), 0.1);
		Assertions.assertEquals(286.2, recipe.getWater(), 0.1);
		Assertions.assertEquals(43.1, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(1.33, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.69, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.61, recipe.getSalt(), 0.01);
		Assertions.assertEquals(6.17, recipe.getFat(), 0.01);
		Assertions.assertEquals(ingredients.dough, recipe.getFlour() + recipe.getWater() + recipe.getSugar() + recipe.getYeast()
			+ recipe.getSalt() + recipe.getFat(), 0.1);
		Assertions.assertEquals(LocalTime.of(12, 25), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 35), LocalTime.of(18, 35)},
				new LocalTime[]{LocalTime.of(18, 45), LocalTime.of(19, 45)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(19, 45), recipe.getSeasoningInstant());
	}

	@Test
	void twoStagesWithStretchAndFoldsRealAccountForIngredients() throws DoughException, YeastException{
		final Ingredients ingredients = Ingredients.create(741.3, 0.001)
			.withCorrectForIngredients()
			.withIngredientsTemperature(16.9)
			.withFlour(Flour.create(230., 0.001, 0.0008))
			.withWater(0.02, 0., 237., 7.9)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withSugar(SugarType.SUCROSE, 0.998, 0.0005)
			.withFat(0.913, 0., 0.002);

		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, ingredients)
			.addSugar(0.003, ingredients)
			.addSalt(0.015)
			.addFat(0.014, ingredients)
			.withAtmosphericPressure(1007.1);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage[] stretchAndFoldStages = new StretchAndFoldStage[]{safStage1, safStage2, safStage3};
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.46, 0,
			Duration.ofMinutes(10), new Duration[]{Duration.ofMinutes(10), Duration.ZERO}, Duration.ofMinutes(15), LocalTime.of(20, 0))
			.withStretchAndFoldStages(stretchAndFoldStages);
		final Recipe recipe = dough.createRecipe(ingredients, procedure, new BakingPanAbstract[]{CircularBakingPan.create(24.)});

		Assertions.assertEquals(440.9, recipe.getFlour(), 0.1);
		Assertions.assertEquals(286.6, recipe.getWater(), 0.1);
		Assertions.assertEquals(1.33, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.45, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.19, recipe.getSalt(), 0.01);
		Assertions.assertEquals(5.79, recipe.getFat(), 0.01);
		Assertions.assertEquals(ingredients.dough, recipe.getFlour() + recipe.getWater() + recipe.getSugar() + recipe.getYeast()
			+ recipe.getSalt() + recipe.getFat(), 0.1);
	}


	@Test
	void sugarFactorMin() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.sugarFactor(35.);

		Assertions.assertEquals(1.000_236, factor, 0.000_001);
	}

	@Test
	void sugarFactorHalfway() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSugar(Dough.SUGAR_MAX / 2., SugarType.SUCROSE, 1., 0.);
		final double factor = dough.sugarFactor(35.);

		Assertions.assertEquals(0.708_084, factor, 0.000_001);
	}

	@Test
	void sugarFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSugar(Dough.SUGAR_MAX, SugarType.SUCROSE, 1., 0.);
		final double factor = dough.sugarFactor(35.);

		Assertions.assertEquals(0.465_287, factor, 0.000_001);
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

		Assertions.assertEquals(0.946_612, factor, 0.000_001);
	}

	@Test
	void saltFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addSalt(Dough.SALT_MAX);
		final double factor = dough.saltFactor();

		Assertions.assertEquals(0.694_137, factor, 0.000_001);
	}


	@Test
	void waterFactorMin() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(Dough.HYDRATION_MIN);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}

	@Test
	void waterFactorHalfway() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater((Dough.HYDRATION_MIN + Dough.HYDRATION_MAX) / 2.);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(1.048_900, factor, 0.000_001);
	}

	@Test
	void waterFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addPureWater(Dough.HYDRATION_MAX);
		final double factor = dough.waterFactor();

		Assertions.assertEquals(0., factor, 0.000_001);
	}


	@Test
	void chlorineDioxideFactorMin() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.waterChlorineDioxideFactor();

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorHalfway() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, Dough.WATER_CHLORINE_DIOXIDE_MAX / 2., 0., 0.);
		final double factor = dough.waterChlorineDioxideFactor();

		Assertions.assertEquals(0.812_500, factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, Dough.WATER_CHLORINE_DIOXIDE_MAX * 0.99, 0., 0.);
		final double factor = dough.waterChlorineDioxideFactor();

		Assertions.assertEquals(0.628_750, factor, 0.000_001);
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
