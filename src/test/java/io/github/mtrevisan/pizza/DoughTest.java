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
			.addSugar(0.003, SugarType.SUCROSE, 0.998, 0.0005)
			.addSalt(0.015)
			.addFat(0.014, 0.913, 0.9175, 0., 0.002)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(230., 0.001, 0.0008, 1.3))
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


	@Test
	void pizza20210502() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.003, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.015)
			.addFat(0.014, 0.913, 0.9175, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 1.3))
			.withIngredientsTemperature(20.6)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1004.1);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(7l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 1.8,
			0,
				Duration.ofMinutes(15l), Duration.ofMinutes(15l),
				LocalTime.of(20, 15))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(
				RectangularBakingPan.create(23., 25., BakingPanMaterial.ALUMINIUM, 0.02),
				CircularBakingPan.create(22.5, BakingPanMaterial.ALUMINIUM, 0.02)
			);
		final double bakingPansTotalArea = bakingInstruments.getBakingPansTotalArea();
		//FIXME
		final double doughWeight = bakingPansTotalArea * 0.76222;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);

		Assertions.assertEquals(440.4, recipe.getFlour(), 0.1);
		Assertions.assertEquals(286.3, recipe.getWater(), 0.1);
		Assertions.assertEquals(37.2, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(1.32, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.63, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.61, recipe.getSalt(), 0.01);
		Assertions.assertEquals(6.16, recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
		Assertions.assertEquals(438.3, doughWeight * bakingInstruments.bakingPans[0].area() / bakingPansTotalArea, 0.1);
		Assertions.assertEquals(303.1, doughWeight * bakingInstruments.bakingPans[1].area() / bakingPansTotalArea, 0.1);
		Assertions.assertEquals(LocalTime.of(12, 45), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 30), LocalTime.of(14, 0),
			LocalTime.of(14, 30)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(13, 0), LocalTime.of(20, 0)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(20, 0), recipe.getSeasoningInstant());
	}

	@Test
	void pizza20210509() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			//45% water, 20% milk
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			//calls for 0%...
			.addSugar(0.003, SugarType.SUCROSE, 1., 0.)
			//calls for 2%...
			.addSalt(0.015)
			.addFat(0.021, 0.913, 0.9175, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(295., 1.3))
			.withIngredientsTemperature(20.3)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1015.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8,
			0,
				Duration.ofMinutes(15l), Duration.ofMinutes(15l),
				LocalTime.of(20, 15))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(
				RectangularBakingPan.create(23., 25., BakingPanMaterial.ALUMINIUM, 0.02),
				CircularBakingPan.create(24., BakingPanMaterial.ALUMINIUM, 0.02)
			);
		final double bakingPansTotalArea = bakingInstruments.getBakingPansTotalArea();
		final double sauceTomato = bakingPansTotalArea / 4.47;
		final double sauceMozzarella = bakingPansTotalArea / 2.85;
		final double sauceOregano = bakingPansTotalArea / 1400.;
		//FIXME
		final double doughWeight = bakingPansTotalArea * 0.70508;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);

		Assertions.assertEquals(428.5, recipe.getFlour(), 0.1);
		Assertions.assertEquals(278.6, recipe.getWater(), 0.1);
		Assertions.assertEquals(37.7, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(1.29, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.69, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.43, recipe.getSalt(), 0.01);
		Assertions.assertEquals(9., recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
		Assertions.assertEquals(405.4, doughWeight * bakingInstruments.bakingPans[0].area() / bakingPansTotalArea, 0.1);
		Assertions.assertEquals(319., doughWeight * bakingInstruments.bakingPans[1].area() / bakingPansTotalArea, 0.1);
		Assertions.assertEquals(LocalTime.of(12, 35), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 20), LocalTime.of(13, 50),
			LocalTime.of(14, 20)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 50), LocalTime.of(18, 50)},
				new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(20, 0), recipe.getSeasoningInstant());
		Assertions.assertEquals(230., sauceTomato, 1.);
		Assertions.assertEquals(360., sauceMozzarella, 1.);
		Assertions.assertEquals(0.73, sauceOregano, 0.01);
	}

	@Test
	void pizza20210516() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.003, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.016)
			.addFat(0.021, 0.913, 0.9175, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(295., 1.3))
			.withIngredientsTemperature(21.2)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1004.5);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8,
			0,
				Duration.ofMinutes(15l), Duration.ofMinutes(15l),
				LocalTime.of(20, 15))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(
				RectangularBakingPan.create(23., 25., BakingPanMaterial.ALUMINIUM, 0.02),
				CircularBakingPan.create(24., BakingPanMaterial.ALUMINIUM, 0.02)
			);
		final double bakingPansTotalArea = bakingInstruments.getBakingPansTotalArea();
		final double sauceOil = bakingPansTotalArea / 146.8;
		final double sauceTomato = bakingPansTotalArea / 4.47;
		final double sauceMozzarella = bakingPansTotalArea / 2.85;
		final double sauceOregano = bakingPansTotalArea / 1400.;
		//FIXME
		final double doughWeight = bakingPansTotalArea * 0.68;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);

		Assertions.assertEquals(413., recipe.getFlour(), 0.1);
		Assertions.assertEquals(268.5, recipe.getWater(), 0.1);
		Assertions.assertEquals(36.3, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(1.24, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.67, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.6, recipe.getSalt(), 0.01);
		Assertions.assertEquals(8.67, recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
		Assertions.assertEquals(391., doughWeight * bakingInstruments.bakingPans[0].area() / bakingPansTotalArea, 0.1);
		Assertions.assertEquals(307.6, doughWeight * bakingInstruments.bakingPans[1].area() / bakingPansTotalArea, 0.1);
		Assertions.assertEquals(LocalTime.of(12, 35), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 20), LocalTime.of(13, 50),
			LocalTime.of(14, 20)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 50), LocalTime.of(18, 50)},
				new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(20, 0), recipe.getSeasoningInstant());
		Assertions.assertEquals(740., dough.getMaxLeaveningDuration().toMinutes(), 0.1);
		Assertions.assertEquals(7., sauceOil, 1.);
		Assertions.assertEquals(230., sauceTomato, 1.);
		Assertions.assertEquals(360., sauceMozzarella, 1.);
		Assertions.assertEquals(0.73, sauceOregano, 0.01);
	}

	@Test
	void paninUeta20210516() throws DoughException, YeastException{
		final double waterInEgg = 0.58 * 0.74;
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.5 - waterInEgg, 0.02, 0., 7.9, 237.)
			//water in 58 g of egg (74% water content)
			.addWater(waterInEgg, 0., 0., 7.9, 0.)
			.addSugar(0.098, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.0049)
			.addFat(0.098, 0.81, 0.9175, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(295., 1.3))
			.withIngredientsTemperature(20.3)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1015.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8,
			0,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(20, 15))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final Recipe recipe = dough.createRecipe(procedure, 868.3);

		Assertions.assertEquals(510., recipe.getFlour(), 0.1);
		Assertions.assertEquals(255., recipe.getWater(), 0.1);
		Assertions.assertEquals(43.1, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(49.98, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.82, recipe.getYeast(), 0.01);
		Assertions.assertEquals(2.5, recipe.getSalt(), 0.01);
		Assertions.assertEquals(49.98, recipe.getFat(), 0.01);
		Assertions.assertEquals(LocalTime.of(12, 35), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 20), LocalTime.of(13, 50),
			LocalTime.of(14, 20)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 50), LocalTime.of(18, 50)},
				new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(20, 0), recipe.getSeasoningInstant());
	}

	@Test
	void paninUeta20210522() throws DoughException, YeastException{
		//water in 58 g of egg (76.15% water content) [%]
		final double waterInEgg = 58. * 0.7615 / 500.;
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.5 - waterInEgg, 0.02, 0., 7.9, 237.)
			.addWater(waterInEgg, 0., 0., 7.25, 0.)
			.addSugar(0.1, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.16, 0.81, 0.9175, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 1.3))
			.withIngredientsTemperature(21.4)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1015.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage3 = LeaveningStage.create(35., Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2, stage3}, 2.2,
			0,
				Duration.ofMinutes(15l), Duration.ofMinutes(15l),
				LocalTime.of(18, 45))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final Recipe recipe = dough.createRecipe(procedure, 442.1);

		Assertions.assertEquals(250., recipe.getFlour(), 1.);
		Assertions.assertEquals(125., recipe.getWater(), 1.);
		Assertions.assertEquals(41.2, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(25., recipe.getSugar(), 0.1);
		Assertions.assertEquals(0.82, recipe.getYeast(), 0.01);
		Assertions.assertEquals(1.25, recipe.getSalt(), 0.01);
		Assertions.assertEquals(40., recipe.getFat(), 0.1);
		Assertions.assertEquals(LocalTime.of(10, 25), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(11, 10), LocalTime.of(11, 40),
			LocalTime.of(12, 10)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(10, 40), LocalTime.of(16, 40)},
				new LocalTime[]{LocalTime.of(16, 50), LocalTime.of(17, 50)},
				new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(18, 30)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(18, 30), recipe.getSeasoningInstant());
	}

	@Test
	void paninUeta20210530() throws DoughException, YeastException{
		//water in 59 g of egg (76.15% water content and 12.5% shell) [%]
		final double waterInEgg = 59. * (1. - 0.125) * 0.7615 / 600.;
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.5 - waterInEgg, 0., 0., 6.65, 0.)
			.addWater(waterInEgg, 0., 0., 7.25, 0.)
			.addSugar(0.1, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.16, 0.81, 0.9175, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 1.3))
			.withIngredientsTemperature(21.7)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1016.1);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage3 = LeaveningStage.create(35., Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2, stage3}, 2.2,
			0,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(18, 45))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final Recipe recipe = dough.createRecipe(procedure, 530.8);

		Assertions.assertEquals(300., recipe.getFlour(), 1.);
		Assertions.assertEquals(140., recipe.getWater() - waterInEgg * recipe.getFlour() / 2., 1.);
		Assertions.assertEquals(40.5, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(30., recipe.getSugar(), 0.1);
		Assertions.assertEquals(1.23, recipe.getYeast(), 0.01);
		Assertions.assertEquals(1.5, recipe.getSalt(), 0.01);
		Assertions.assertEquals(48., recipe.getFat(), 0.1);
		Assertions.assertEquals(LocalTime.of(10, 25), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(11, 10), LocalTime.of(11, 40),
			LocalTime.of(12, 10)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(10, 40), LocalTime.of(16, 40)},
				new LocalTime[]{LocalTime.of(16, 50), LocalTime.of(17, 50)},
				new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(18, 30)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(18, 30), recipe.getSeasoningInstant());
	}

	@Test
	void paninUeta20210602() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addMilk(0.25, 6.6, 0.87, 0.037)
			.addEgg(60. / 300., 6., 0.125, 0.7615, 0.11)
			.addSugar(0.1, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.13, 0.815, 0.9175, 0.16, 0.025)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 1.3))
			.withIngredientsTemperature(22.7)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1015.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage3 = LeaveningStage.create(35., Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2, stage3}, 2.2,
			0,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(18, 45))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final Recipe recipe = dough.createRecipe(procedure, 448.4);

		Assertions.assertEquals(300., recipe.getFlour(), 1.);
		Assertions.assertEquals(76., recipe.getWater(), 1.);
		Assertions.assertEquals(48., recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(30., recipe.getSugar(), 0.1);
		Assertions.assertEquals(1.48, recipe.getYeast(), 0.01);
		Assertions.assertEquals(1.67, recipe.getSalt(), 0.01);
		Assertions.assertEquals(39, recipe.getFat(), 0.1);
		Assertions.assertEquals(LocalTime.of(10, 25), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(11, 10), LocalTime.of(11, 40),
			LocalTime.of(12, 10)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(10, 40), LocalTime.of(16, 40)},
				new LocalTime[]{LocalTime.of(16, 50), LocalTime.of(17, 50)},
				new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(18, 30)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(18, 30), recipe.getSeasoningInstant());
	}

	@Test
	void futurePaninUeta202106xx() throws DoughException, YeastException{
		final double flourWeight = 340.;
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addMilk(0.19, 6.6, 0.87, 0.037)
			.addEgg(2. * 66. / flourWeight, 6., 0.125, 0.7615, 0.11)
			.addSugar(0.1, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.13, 0.815, 0.9175, 0.16, 0.025)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 1.3))
			.withIngredientsTemperature(27.1)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1012.6);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage3 = LeaveningStage.create(35., Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2, stage3}, 2.2,
			0,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(18, 45))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final Recipe recipe = dough.createRecipe(procedure, 543.9);

		//340 g
		Assertions.assertEquals(flourWeight, recipe.getFlour(), 1.);
		Assertions.assertEquals(116., recipe.getWater(), 1.);
		Assertions.assertEquals(26.6, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(34., recipe.getSugar(), 0.1);
		Assertions.assertEquals(1.8, recipe.getYeast(), 0.01);
		Assertions.assertEquals(1.88, recipe.getSalt(), 0.01);
		Assertions.assertEquals(49.7, recipe.getFat(), 0.1);
		//üeta
		Assertions.assertEquals(68., recipe.getFlour() * 0.2, 1.);
		Assertions.assertEquals(LocalTime.of(10, 25), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(11, 10), LocalTime.of(11, 40),
			LocalTime.of(12, 10)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(10, 40), LocalTime.of(16, 40)},
				new LocalTime[]{LocalTime.of(16, 50), LocalTime.of(17, 50)},
				new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(18, 30)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(18, 30), recipe.getSeasoningInstant());
	}

	//https://www.utrechtinnovatielab.nl/uploads/media/5c754366395b3/poster-bioreactoren-qvq-studentenproject-2018.pdf
	@Test
	void futurePizza20210xxx() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.003, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.016)
			.addFat(0.021, 0.913, 0.9175, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(295., 1.3))
			.withIngredientsTemperature(21.2)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1004.5);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8,
			0,
				Duration.ofMinutes(15l), Duration.ofMinutes(15l),
				LocalTime.of(20, 15))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(
				RectangularBakingPan.create(23., 26., BakingPanMaterial.ALUMINIUM, 0.02),
				CircularBakingPan.create(24., BakingPanMaterial.ALUMINIUM, 0.02)
			);
		final double bakingPansTotalArea = bakingInstruments.getBakingPansTotalArea();
		final double sauceOil = bakingPansTotalArea / 171.;
		final double sauceTomato = bakingPansTotalArea / 4.47;
		final double sauceMozzarella = bakingPansTotalArea / 3.;
		final double sauceOregano = bakingPansTotalArea / 1400.;
		//FIXME
		final double doughWeight = bakingPansTotalArea * 0.68;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);

		Assertions.assertEquals(422.3, recipe.getFlour(), 0.1);
		Assertions.assertEquals(274.5, recipe.getWater(), 0.1);
		Assertions.assertEquals(36.3, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(1.27, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.68, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.76, recipe.getSalt(), 0.01);
		Assertions.assertEquals(8.87, recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
		Assertions.assertEquals(LocalTime.of(12, 35), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 20), LocalTime.of(13, 50),
			LocalTime.of(14, 20)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 50), LocalTime.of(18, 50)},
				new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(20, 0), recipe.getSeasoningInstant());
		Assertions.assertEquals(740., dough.getMaxLeaveningDuration().toMinutes(), 0.1);

		final double percent1 = bakingInstruments.bakingPans[0].area() / bakingPansTotalArea;
		final double percent2 = bakingInstruments.bakingPans[1].area() / bakingPansTotalArea;
		Assertions.assertEquals(406.6, doughWeight * percent1, 0.1);
		Assertions.assertEquals(307.6, doughWeight * percent2, 0.1);
		Assertions.assertEquals(3.5, sauceOil * percent1, 0.1);
		Assertions.assertEquals(2.6, sauceOil * percent2, 0.1);
		Assertions.assertEquals(235., sauceTomato, 1.);
		Assertions.assertEquals(350., sauceMozzarella, 1.);
		Assertions.assertEquals(0.75, sauceOregano, 0.01);
	}

}
