/**
 * Copyright (c) 2022 Mauro Trevisan
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

import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaePedonYeast;

import java.time.Duration;
import java.time.LocalTime;


//effect of ingredients!! https://www.maltosefalcons.com/blogs/brewing-techniques-tips/yeast-propagation-and-maintenance-principles-and-practices
public final class Dough2{

	private DoughCore core;


	public static Dough2 create(final DoughCore core) throws DoughException, YeastException{
		return new Dough2(core);
	}


	private Dough2(final DoughCore core) throws DoughException, YeastException{
		if(core == null)
			throw DoughException.create("Core data must be provided");

		this.core = core;
	}


	public static void main(String[] args) throws DoughException, YeastException{
		DoughCore core = DoughCore.create(new SaccharomycesCerevisiaePedonYeast())
			.withFlourParameters(Flour.create(230., 0., 0.0008, 1.3, 0., 0., 0.001))
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.004, SugarType.SUCROSE, 0.998, 0.0005)
			.addFat(0.021, FatType.OLIVE_OIL, 0.913, 0.9175, 0., 0.002)
			.addSalt(0.016)
			.withYeastParameters(YeastType.INSTANT_DRY, 1.)
			.withAtmosphericPressure(1015.6)
			.withAirRelativeHumidity(0.55);
		LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		LeaveningStage stage2 = LeaveningStage.create(20., Duration.ofHours(1l));
		Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(20, 15));
		Dough2 dough = Dough2.create(core);
		Recipe recipe = dough.createRecipe(procedure, 767.55, 18., 27.);

//		System.out.println("yeast = " + Helper.round(recipe.getYeast(), 5) + "%");
		System.out.println(recipe);
	}

	private Recipe createRecipe(final Procedure procedure, final double doughWeight, final Double ingredientsTemperature,
			final Double doughTemperature) throws YeastException{
		return core.createRecipe(procedure, doughWeight, ingredientsTemperature, doughTemperature);
	}

}
