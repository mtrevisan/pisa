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
import io.github.mtrevisan.pizza.bakingpans.BakingPanMaterial;
import io.github.mtrevisan.pizza.bakingpans.CircularBakingPan;
import io.github.mtrevisan.pizza.bakingpans.RectangularBakingPan;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;


class DoughTest{

	@Test
	void singleStageNoYeastPossible() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 2.,
			0,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		Assertions.assertThrows(YeastException.class, () -> dough.calculateYeast(procedure),
			"No yeast quantity will ever be able to produce the given expansion ratio");
	}

	@Test
	void singleStage() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 2.,
			0,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.011_83, dough.yeast, 0.000_01);
	}

	@Test
	void twoStages() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.006_31, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesEarlyExit() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
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

		Assertions.assertEquals(0.011_83, yeast1, 0.000_01);
		Assertions.assertEquals(yeast2, yeast1, 0.000_01);
	}

	@Test
	void twoStagesSameTemperature() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.011_83, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesInnerVolumeDecrease() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l))
			.withVolumeDecrease(0.20);
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.95,
			1,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.055_96, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesWithStretchAndFolds() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, 0., 0., Dough.PURE_WATER_PH, 0.);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l));
		final LeaveningStage stage2 = LeaveningStage.create(25., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.10);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.25);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(15l))
			.withVolumeDecrease(0.30);
		final StretchAndFoldStage[] stretchAndFoldStages = {safStage1, safStage2, safStage3};
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ZERO, Duration.ZERO, LocalTime.NOON)
			.withStretchAndFoldStages(stretchAndFoldStages);
		dough.calculateYeast(procedure);

		Assertions.assertEquals(0.008_41, dough.yeast, 0.000_01);
	}

	@Test
	void twoStagesWithStretchAndFoldsReal20210502() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.003, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.015)
			.addFat(0.014, 0.913, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260.))
			.withIngredientsTemperature(20.6)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1004.1);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(7l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 1.8,
			0,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(20, 15))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(
				RectangularBakingPan.create(23., 25., BakingPanMaterial.ALUMINIUM, 0.02),
				CircularBakingPan.create(22.5, BakingPanMaterial.ALUMINIUM, 0.02));
		final double bakingPansTotalArea = bakingInstruments.getBakingPansTotalArea();
		//FIXME
		final double doughWeight = bakingPansTotalArea * 0.76222;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);

		Assertions.assertEquals(440.4, recipe.getFlour(), 0.1);
		Assertions.assertEquals(286.3, recipe.getWater(), 0.1);
		Assertions.assertEquals(37.2, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(1.32, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.58, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.61, recipe.getSalt(), 0.01);
		Assertions.assertEquals(6.17, recipe.getFat(), 0.01);
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
	void twoStagesWithStretchAndFoldsReal20210508() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.003, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.015)
			.addFat(0.014, 0.913, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260.))
			.withIngredientsTemperature(20.6)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1004.1);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8,
			0,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(20, 0))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(CircularBakingPan.create(24., BakingPanMaterial.ALUMINIUM, 0.02));
		final double bakingPansTotalArea = bakingInstruments.getBakingPansTotalArea();
		//FIXME
		final double doughWeight = bakingPansTotalArea * 0.76222;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);

		Assertions.assertEquals(204.8, recipe.getFlour(), 0.1);
		Assertions.assertEquals(133.1, recipe.getWater(), 0.1);
		Assertions.assertEquals(37.2, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(0.61, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.27, recipe.getYeast(), 0.01);
		Assertions.assertEquals(3.07, recipe.getSalt(), 0.01);
		Assertions.assertEquals(2.87, recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
		Assertions.assertEquals(344.8, doughWeight * bakingInstruments.bakingPans[0].area() / bakingPansTotalArea, 0.1);
		Assertions.assertEquals(LocalTime.of(12, 20), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 05), LocalTime.of(13, 35),
			LocalTime.of(14, 5)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 35), LocalTime.of(18, 35)},
				new LocalTime[]{LocalTime.of(18, 45), LocalTime.of(19, 45)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(19, 45), recipe.getSeasoningInstant());
	}

	@Test
	void twoStagesWithStretchAndFoldsRealAccountForIngredients() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.003, SugarType.SUCROSE, 0.998, 0.0005)
			.addSalt(0.015)
			.addFat(0.014, 0.913, 0., 0.002)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(230., 0.001, 0.0008))
			.withIngredientsTemperature(16.9)
			.withCorrectForIngredients()
			.withAtmosphericPressure(1007.1);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l))
			.withAfterStageWork(Duration.ofMinutes(10));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l))
			.withVolumeDecrease(0.05);
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

		Assertions.assertEquals(441.0, recipe.getFlour(), 0.1);
		Assertions.assertEquals(286.6, recipe.getWater(), 0.1);
		Assertions.assertEquals(1.33, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.45, recipe.getYeast(), 0.01);
		Assertions.assertEquals(6.19, recipe.getSalt(), 0.01);
		Assertions.assertEquals(5.79, recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
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
		dough.addWater(0.6, Dough.WATER_CHLORINE_DIOXIDE_MAX / 2., 0., Dough.PURE_WATER_PH,
			0.);
		final double factor = dough.waterChlorineDioxideFactor();

		Assertions.assertEquals(0.812_500, factor, 0.000_001);
	}

	@Test
	void chlorineDioxideFactorMax() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		dough.addWater(0.6, Dough.WATER_CHLORINE_DIOXIDE_MAX * 0.99, 0., Dough.PURE_WATER_PH,
			0.);
		final double factor = dough.waterChlorineDioxideFactor();

		Assertions.assertEquals(0.628_750, factor, 0.000_001);
	}


	@Test
	void airPressureFactor1atm() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.atmosphericPressureFactor(Dough.ONE_ATMOSPHERE);

		Assertions.assertEquals(1., factor, 0.000_001);
	}

	@Test
	void airPressureFactor10000atm() throws DoughException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast());
		final double factor = dough.atmosphericPressureFactor(Dough.ONE_ATMOSPHERE * 10_000.);

		Assertions.assertEquals(0.986_037, factor, 0.000_001);
	}

}
