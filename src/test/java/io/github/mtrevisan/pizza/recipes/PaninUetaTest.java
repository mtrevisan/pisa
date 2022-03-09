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

import io.github.mtrevisan.pizza.Dough;
import io.github.mtrevisan.pizza.DoughException;
import io.github.mtrevisan.pizza.ingredients.Flour;
import io.github.mtrevisan.pizza.LeaveningStage;
import io.github.mtrevisan.pizza.Procedure;
import io.github.mtrevisan.pizza.Recipe;
import io.github.mtrevisan.pizza.StretchAndFoldStage;
import io.github.mtrevisan.pizza.ingredients.Sugar;
import io.github.mtrevisan.pizza.YeastException;
import io.github.mtrevisan.pizza.ingredients.Yeast;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaeCECT10131Yeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;


class PaninUetaTest{

	@Test
	void paninUeta20210516() throws DoughException, YeastException{
		final double waterInEgg = 0.58 * 0.74;
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.5 - waterInEgg, 0.02, 0., 7.9, 237.)
			//water in 58 g of egg (74% water content)
			.addWater(waterInEgg, 0., 0., 7.9, 0.)
			.addSugar(0.098, Sugar.SugarType.SUCROSE, 1., 0.)
			.addSalt(0.0049)
			.addFat(0.098, 0.81, 0.9175, 0., 0.)
			.withYeast(Yeast.YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(295., 0., 0., 1.3, 0., 0., 0.))
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
			.addSugar(0.1, Sugar.SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.16, 0.81, 0.9175, 0., 0.)
			.withYeast(Yeast.YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 0., 0., 1.3, 0., 0., 0.))
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
			.addSugar(0.1, Sugar.SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.16, 0.81, 0.9175, 0., 0.)
			.withYeast(Yeast.YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 0., 0., 1.3, 0., 0., 0.))
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
			.addSugar(0.1, Sugar.SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.13, 0.815, 0.9175, 0.16, 0.025)
			.withYeast(Yeast.YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 0., 0., 1.3, 0., 0., 0.))
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
	void paninUeta20210621() throws DoughException, YeastException{
		final double flourWeight = 170.;
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addMilk(0.19, 6.6, 0.87, 0.037)
			.addEgg(63. / flourWeight, 6., 0.125, 0.7615, 0.11)
			.addSugar(0.1, Sugar.SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.13, 0.815, 0.9175, 0.16, 0.025)
			.withYeast(Yeast.YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 0., 0., 1.3, 0., 0., 0.))
			.withIngredientsTemperature(28.6)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1005.1);
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
		final Recipe recipe = dough.createRecipe(procedure, 243.6);

		//170 g
		Assertions.assertEquals(flourWeight, recipe.getFlour(), 1.);
		Assertions.assertEquals(33., recipe.getWater(), 1.);
		Assertions.assertEquals(16.9, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(17., recipe.getSugar(), 0.1);
		Assertions.assertEquals(0.9, recipe.getYeast(), 0.01);
		Assertions.assertEquals(1., recipe.getSalt(), 0.01);
		Assertions.assertEquals(21.2, recipe.getFat(), 0.1);
		//üeta
		Assertions.assertEquals(34., recipe.getFlour() * 0.2, 1.);
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
	void futurePaninUeta202107xx() throws DoughException, YeastException{
		final double flourWeight = 170.;
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addMilk(0.2, 6.6, 0.87, 0.037)
			.addEgg(63. / flourWeight, 6., 0.125, 0.7615, 0.11)
			.addSugar(0.1, Sugar.SugarType.SUCROSE, 1., 0.)
			.addSalt(0.005)
			.addFat(0.13, 0.815, 0.9175, 0.16, 0.025)
			.withYeast(Yeast.YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260., 0., 0., 1.3, 0., 0., 0.))
			.withIngredientsTemperature(28.6)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1005.1);
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
		final Recipe recipe = dough.createRecipe(procedure, 245.3);

		//170 g
		Assertions.assertEquals(flourWeight, recipe.getFlour(), 1.);
		Assertions.assertEquals(35., recipe.getWater(), 1.);
		Assertions.assertEquals(17.4, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(17., recipe.getSugar(), 0.1);
		Assertions.assertEquals(0.89, recipe.getYeast(), 0.01);
		Assertions.assertEquals(1., recipe.getSalt(), 0.01);
		Assertions.assertEquals(21.3, recipe.getFat(), 0.1);
		//üeta
		Assertions.assertEquals(34., recipe.getFlour() * 0.2, 1.);
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

}
