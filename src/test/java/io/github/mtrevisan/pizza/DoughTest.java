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

import io.github.mtrevisan.pizza.bakingpans.BakingPanMaterial;
import io.github.mtrevisan.pizza.bakingpans.CircularBakingPan;
import io.github.mtrevisan.pizza.bakingpans.RectangularBakingPan;
import io.github.mtrevisan.pizza.ingredients.Flour;
import io.github.mtrevisan.pizza.ingredients.Carbohydrate;
import io.github.mtrevisan.pizza.ingredients.Yeast;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;


class DoughTest{

	private static class YeastModelTest extends YeastModelAbstract{
		@Override
		public final double getTemperatureMin(){
			return 0.74;
		}

		@Override
		public final double getTemperatureOpt(){
			return 32.8;
		}

		@Override
		public final double getTemperatureMax(){
			return 45.9;
		}

		@Override
		public final double getMaximumSpecificVolumeGrowthRate(){
			return 0.449;
		}

		@Override
		public double getPHMin(){
			return 2.;
		}

		@Override
		public double getPHOpt(){
			return 6.;
		}

		@Override
		public double getPHMax(){
			return 9.;
		}
	}

	@Test
	void singleStageNoYeastPossible() throws DoughException{
		final Dough dough = Dough.create(new YeastModelTest())
			.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 2.,
			0,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		Assertions.assertThrows(YeastException.class, () -> dough.calculateYeast(procedure),
			"No yeast quantity will ever be able to produce the given expansion ratio");
	}

	@Test
	void singleStage() throws DoughException, YeastException{
		final Dough dough = Dough.create(new YeastModelTest())
			.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 2.,
			0,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.025_18, dough.yeast, 0.000_01);
	}

	@Test
	void twoStages() throws DoughException, YeastException{
		final Dough dough = Dough.create(new YeastModelTest())
			.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.006_900, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesEarlyExit() throws DoughException, YeastException{
		final Dough dough = Dough.create(new YeastModelTest())
			.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1l));
		Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			0,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);
		final double yeast1 = dough.yeast;
		procedure = Procedure.create(new LeaveningStage[]{stage1}, 2., 0,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);
		final double yeast2 = dough.yeast;

		Assertions.assertEquals(0.025_18, yeast1, 0.000_01);
		Assertions.assertEquals(yeast2, yeast1, 0.000_001);
	}

	@Test
	void twoStagesSameTemperature() throws DoughException, YeastException{
		final Dough dough = Dough.create(new YeastModelTest())
			.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.025_18, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesInnerVolumeDecrease() throws DoughException, YeastException{
		final Dough dough = Dough.create(new YeastModelTest())
			.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.95,
			1,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.006_36, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesWithStretchAndFolds() throws DoughException, YeastException{
		final Dough dough = Dough.create(new YeastModelTest())
			.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(15l));
		final StretchAndFoldStage[] stretchAndFoldStages = {safStage1, safStage2, safStage3};
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
				Duration.ZERO, Duration.ZERO, LocalTime.NOON)
			.withStretchAndFoldStages(stretchAndFoldStages);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.006_90, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesWithStretchAndFoldsRealAccountForIngredients() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.003, Carbohydrate.CarbohydrateType.SUCROSE, 0.998, 0.0005)
			.addSalt(0.015)
			.addFat(0.014, 0.913, 0.9175, 0., 0.002)
			.withYeast(Yeast.YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(230., 0., 0.0008, 1.3, 0., 0., 0.001))
			.withIngredientsTemperature(16.9)
			.withCorrectForIngredients()
			.withAtmosphericPressure(1007.1);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l))
			.withAfterStageWork(Duration.ofMinutes(10));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage[] stretchAndFoldStages = {safStage1, safStage2, safStage3};
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.46,
			0,
				Duration.ofMinutes(10l), Duration.ofMinutes(15l),
				LocalTime.of(20, 0))
			.withStretchAndFoldStages(stretchAndFoldStages);
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(
				RectangularBakingPan.create(23., 25., BakingPanMaterial.ALUMINIUM, 0.02),
				CircularBakingPan.create(22.5, BakingPanMaterial.ALUMINIUM, 0.02));
		//FIXME
		final double doughWeight = bakingInstruments.getBakingPansTotalArea() * 0.76222;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);

		Assertions.assertEquals(440.9, recipe.getFlour(), 0.1);
		Assertions.assertEquals(286.6, recipe.getWater(), 0.1);
		Assertions.assertEquals(1.33, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.58, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.17, recipe.getSalt(), 0.01);
		Assertions.assertEquals(5.79, recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
	}

}
