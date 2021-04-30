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


class OvenTest{

	@Test
	void twoStagesWithHeightReal() throws DoughException, YeastException, OvenException{
		final Dough dough = Dough.create(new SaccharomycesCerevisiaeCECT10131Yeast())
			.addWater(0.65, 0.02, 0., Dough.PURE_WATER_PH, 0.)
			.addSugar(0.003, SugarType.SUCROSE, 1., 0.)
			.addSalt(0.016)
			.addFat(0.016, 0.913, 0., 0.)
			.withYeast(YeastType.INSTANT_DRY, 1.)
			.withFlour(Flour.create(260.))
			.withIngredientsTemperature(16.7)
			.withDoughTemperature(27.)
			.withAirRelativeHumidity(0.55);
		final LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6));
		final LeaveningStage stage2 = LeaveningStage.create(35., Duration.ofHours(1));
		final StretchAndFoldStage safStage1 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage2 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage safStage3 = StretchAndFoldStage.create(Duration.ofMinutes(30))
			.withVolumeDecrease(0.05);
		final StretchAndFoldStage[] stretchAndFoldStages = {safStage1, safStage2, safStage3};
		final Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 1.8, 0,
			Duration.ofMinutes(10), new Duration[]{Duration.ofMinutes(10), Duration.ZERO}, Duration.ofMinutes(15), LocalTime.of(20, 0))
			.withStretchAndFoldStages(stretchAndFoldStages);
		final Oven oven = Oven.create(OvenType.FORCED_CONVECTION)
			.withDistanceHeaterTop(0.1)
			.withDistanceHeaterBottom(0.1);
		final BakingInstruments bakingInstruments = new BakingInstruments()
			.withBakingPans(new BakingPanAbstract[]{
				RectangularBakingPan.create(23., 25., BakingPanMaterial.CAST_IRON, 0.02),
				CircularBakingPan.create(22.5, BakingPanMaterial.ALUMINIUM, 0.02)});
		final double totalBakingPansArea = bakingInstruments.getBakingPansTotalArea();
		//FIXME
		final double doughWeight = totalBakingPansArea * 0.76222;
		final Recipe recipe = dough.createRecipe(procedure, doughWeight);
		final BakingInstructions instructions = oven.bakeRecipe(dough, recipe, 2.4, bakingInstruments);

		Assertions.assertEquals(439.6, recipe.getFlour(), 0.1);
		Assertions.assertEquals(285.7, recipe.getWater(), 0.1);
		Assertions.assertEquals(43.4, recipe.getWaterTemperature(), 0.1);
		Assertions.assertEquals(1.32, recipe.getSugar(), 0.01);
		Assertions.assertEquals(0.69, recipe.getYeast(), 0.01);
		Assertions.assertEquals(7.04, recipe.getSalt(), 0.01);
		Assertions.assertEquals(7.03, recipe.getFat(), 0.01);
		Assertions.assertEquals(doughWeight, recipe.doughWeight(), 0.01);
		Assertions.assertEquals(LocalTime.of(12, 25), recipe.getDoughMakingInstant());
		Assertions.assertArrayEquals(new LocalTime[][]{
				new LocalTime[]{LocalTime.of(12, 35), LocalTime.of(18, 35)},
				new LocalTime[]{LocalTime.of(18, 45), LocalTime.of(19, 45)}
			},
			recipe.getStageStartEndInstants());
		Assertions.assertEquals(LocalTime.of(19, 45), recipe.getSeasoningInstant());
		Assertions.assertEquals(220.0, instructions.getBakingTemperature(), 0.1);
		//FIXME should be around 12 min, not 281 s = 4 min 41 s
		Assertions.assertEquals(281., instructions.getBakingDuration().getSeconds(), 1.);
	}

}
