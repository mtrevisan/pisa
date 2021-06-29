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
package io.github.mtrevisan.pizza.recipes;

import io.github.mtrevisan.pizza.BakingInstruments;
import io.github.mtrevisan.pizza.Dough;
import io.github.mtrevisan.pizza.DoughException;
import io.github.mtrevisan.pizza.Flour;
import io.github.mtrevisan.pizza.LeaveningStage;
import io.github.mtrevisan.pizza.OvenException;
import io.github.mtrevisan.pizza.Procedure;
import io.github.mtrevisan.pizza.Recipe;
import io.github.mtrevisan.pizza.StretchAndFoldStage;
import io.github.mtrevisan.pizza.SugarType;
import io.github.mtrevisan.pizza.YeastException;
import io.github.mtrevisan.pizza.YeastType;
import io.github.mtrevisan.pizza.bakingpans.BakingPanMaterial;
import io.github.mtrevisan.pizza.bakingpans.CircularBakingPan;
import io.github.mtrevisan.pizza.bakingpans.RectangularBakingPan;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;


class PizzaTest{

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