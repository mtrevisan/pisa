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
import io.github.mtrevisan.pizza.Flour;
import io.github.mtrevisan.pizza.LeaveningStage;
import io.github.mtrevisan.pizza.Procedure;
import io.github.mtrevisan.pizza.Recipe;
import io.github.mtrevisan.pizza.StretchAndFoldStage;
import io.github.mtrevisan.pizza.SugarType;
import io.github.mtrevisan.pizza.YeastException;
import io.github.mtrevisan.pizza.YeastType;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaePedonYeast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;


class FeteBiskotadeTest{

	@Test
	void feteBiskotade20210628() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaePedonYeast())
			.addWater(41. / 300., 0.02, 0., 7.9, 237.)
			.addMilk(41. / 300., 6.6, 0.87, 0.037)
			//zucchero di canna
			.addSugar(0.1, SugarType.SUCROSE, 0.97, 0.)
			//mièl
			.addSugar(4. / 300., SugarType.SUCROSE, 0.801, 0.1919)
			.addSalt(0.01)
			//olio di semi di girasole
			.addFat(0.05, 0.92, 0.923, 0.08, 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(295., 1.3))
			.withIngredientsTemperature(29.5)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1007.9);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(2l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8,
			0,
			Duration.ofMinutes(15l), Duration.ZERO,
			LocalTime.of(21, 0))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final Recipe recipe = dough.createRecipe(procedure, 441.3);

		Assertions.assertEquals(300., recipe.getFlour(), 0.1);
		Assertions.assertEquals(80.2, recipe.getWater(), 0.1);
		Assertions.assertEquals(15.7, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(40.33, recipe.getSugar(), 0.01);
		Assertions.assertEquals(1.92, recipe.getYeast(), 0.01);
		Assertions.assertEquals(3., recipe.getSalt(), 0.01);
		Assertions.assertEquals(15.9, recipe.getFat(), 0.01);
		Assertions.assertEquals(LocalTime.of(12, 35), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 20), LocalTime.of(13, 50),
			LocalTime.of(14, 20)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 50), LocalTime.of(18, 50)},
				new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(21, 0)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(21, 0), recipe.getSeasoningInstant());
	}

	@Test
	void futureFeteBiskotade202106xx() throws DoughException, YeastException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaePedonYeast())
			.addWater(41. / 300., 0.02, 0., 7.9, 237.)
			.addMilk(41. / 300., 6.6, 0.87, 0.037)
			//zucchero di canna
			.addSugar(0.08, SugarType.SUCROSE, 0.97, 0.)
			//mièl
			.addSugar(3. / 300., SugarType.SUCROSE, 0.801, 0.1919)
			.addSalt(0.01)
			//olio di semi di girasole
			.addFat(0.05, 0.92, 0.923, 0.08, 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(295., 1.3))
			.withIngredientsTemperature(29.5)
			.withDoughTemperature(27.)
			.withAtmosphericPressure(1007.9);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(2l));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30l));
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8,
			0,
			Duration.ofMinutes(15l), Duration.ZERO,
			LocalTime.of(21, 0))
			.withStretchAndFoldStages(new StretchAndFoldStage[]{safStage1, safStage2, safStage3});
		final Recipe recipe = dough.createRecipe(procedure, 434.7);

		Assertions.assertEquals(300., recipe.getFlour(), 0.1);
		Assertions.assertEquals(81.8, recipe.getWater(), 0.1);
		Assertions.assertEquals(16.2, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(32.07, recipe.getSugar(), 0.01);
		Assertions.assertEquals(1.92, recipe.getYeast(), 0.01);
		Assertions.assertEquals(3., recipe.getSalt(), 0.01);
		Assertions.assertEquals(15.9, recipe.getFat(), 0.01);
		Assertions.assertEquals(LocalTime.of(12, 35), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[]{LocalTime.of(13, 20), LocalTime.of(13, 50),
			LocalTime.of(14, 20)}, recipe.getStretchAndFoldStartInstants());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 50), LocalTime.of(18, 50)},
				new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(21, 0)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(21, 0), recipe.getSeasoningInstant());
	}

}
