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

import io.github.mtrevisan.pizza.utils.Helper;
import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaePedonYeast;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import java.time.Duration;
import java.time.LocalTime;


//effect of ingredients!! https://www.maltosefalcons.com/blogs/brewing-techniques-tips/yeast-propagation-and-maintenance-principles-and-practices
public final class Dough2{

	/** [% w/w] */
	private static final double SOLVER_YEAST_MAX = 0.2;
	private static final int SOLVER_EVALUATIONS_MAX = 100;

	//accuracy is ±0.001%
	private final BaseUnivariateSolver<UnivariateFunction> solverYeast = new BracketingNthOrderBrentSolver(0.000_01,
		5);


	private DoughCore core;


	public static Dough2 create(final DoughCore core, final Procedure procedure) throws DoughException, YeastException{
		return new Dough2(core, procedure);
	}


	private Dough2(final DoughCore core, final Procedure procedure) throws DoughException, YeastException{
		if(core == null)
			throw DoughException.create("Core data must be provided");

		this.core = core;

		calculateYeast(procedure);

		//true yeast quantity
		core.yeast /= core.rawYeast * core.yeastType.factor;
	}


	/**
	 * @param procedure	The recipe procedure.
	 * @return	The recipe.
	 */
	public void createRecipe(final Procedure procedure) throws YeastException{
		//TODO
	}

	public static void main(String[] args) throws DoughException, YeastException{
		DoughCore core = DoughCore.create(new SaccharomycesCerevisiaePedonYeast())
			.withFlourParameters(Flour.create(230., 0.001, 0.0008, 1.3))
			.addWater(0.65, 0.02, 0., 7.9, 237.)
			.addSugar(0.004, SugarType.SUCROSE, 0.998, 0.0005)
			.addFat(0.021, 0.913, 0.9175, 0., 0.002)
			.addSalt(0.016)
			.withYeastParameters(YeastType.INSTANT_DRY, 1.);
		LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(5l))
			.withAfterStageWork(Duration.ofMinutes(10l));
		LeaveningStage stage2 = LeaveningStage.create(20., Duration.ofHours(1l));
		Procedure procedure = Procedure.create(new LeaveningStage[]{stage1, stage2}, 2.,
			1,
			Duration.ofMinutes(15l), Duration.ofMinutes(15l),
			LocalTime.of(20, 15));
		Dough2 dough = Dough2.create(core, procedure);

		System.out.println("yeast = " + Helper.round(core.yeast, 5) + "%");
	}

	/**
	 * Find the initial yeast able to obtain a given volume expansion ratio after a series of consecutive stages at a given duration at
	 * temperature.
	 *
	 * @param procedure	Data for procedure.
	 */
	private void calculateYeast(final Procedure procedure) throws YeastException{
		//reset variable
		core.yeast = 0.;

		try{
			final UnivariateFunction f = yeast -> volumeExpansionRatioDifference(yeast, procedure);
			core.yeast = solverYeast.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_YEAST_MAX);
		}
		catch(final NoBracketingException nbe){
			throw YeastException.create("No amount of yeast will ever be able to produce the given expansion ratio", nbe);
		}
		catch(final IllegalArgumentException iae){
			throw YeastException.create("No amount of yeast will ever be able to produce the given expansion ratio due to the adverse environment in "
				+ iae.getMessage());
		}
		catch(final TooManyEvaluationsException tmee){
			throw YeastException.create("Cannot calculate yeast quantity, try increasing maximum number of evaluations in the solver",
				tmee);
		}
	}

	//https://www.mdpi.com/2076-2607/9/1/47/htm
	//https://www.researchgate.net/publication/318756298_Bread_Dough_and_Baker's_Yeast_An_Uplifting_Synergy
	private double volumeExpansionRatioDifference(final double yeast, final Procedure procedure) throws MathIllegalArgumentException{
		//lag phase duration [hrs]
		//TODO calculate lambda
		final double lambda = 0.5;
		//TODO calculate the factor
		final double aliveYeast = 0.95 * yeast;


		final double[] ingredientsFactors = new double[procedure.leaveningStages.length];
		for(int i = 0; i < procedure.leaveningStages.length; i ++){
			ingredientsFactors[i] = core.ingredientsFactor(yeast, procedure.leaveningStages[i].temperature);

			if(ingredientsFactors[i] == 0.)
				throw new IllegalArgumentException("stage " + (i + 1));
		}

		//consider multiple leavening stages
		LeaveningStage stage = procedure.leaveningStages[0];
		Duration ongoingDuration = stage.duration;
		double doughVolumeExpansionRatio = doughVolumeExpansionRatio(aliveYeast, lambda, stage.temperature, ongoingDuration);
		for(int i = 1; i <= procedure.targetVolumeExpansionRatioAtLeaveningStage; i ++){
			stage = procedure.leaveningStages[i];

			final double previousExpansionRatio = doughVolumeExpansionRatio(aliveYeast, lambda, stage.temperature, ongoingDuration);
			ongoingDuration = ongoingDuration.plus(stage.duration);
			final double currentExpansionRatio = doughVolumeExpansionRatio(aliveYeast, lambda, stage.temperature, ongoingDuration);

			doughVolumeExpansionRatio += currentExpansionRatio - previousExpansionRatio;
		}

		return doughVolumeExpansionRatio - procedure.targetDoughVolumeExpansionRatio;
	}

	//http://arccarticles.s3.amazonaws.com/webArticle/articles/jdfhs282010.pdf
	private double doughVolumeExpansionRatio(final double yeast, final double lambda, final double temperature, final Duration duration){
		//maximum relative volume expansion ratio
		final double alpha = maximumRelativeVolumeExpansionRatio(yeast);
		final double ingredientsFactor = core.ingredientsFactor(yeast, temperature);

		final double volumeExpansionRatio = core.yeastModel.volumeExpansionRatio(duration, lambda, alpha, temperature, ingredientsFactor);

		//correct for yeast quantity:
		//FIXME calculate k
		//k is so that 4% yeast in flour with 1.5% sugar and 60% water at 27-30 °C for 1 hrs gives a volume expansion ratio of 220%
		//that is, k = 1.2 / (yeastModel.volumeExpansionRatio(1., lambda, alpha, (27. + 30.) / 2., 1.) * 0.04)
		final double k = 13.7;
		return 1. + k * volumeExpansionRatio * yeast;
	}

	/**
	 * Maximum relative volume expansion ratio.
	 *
	 * @see <a href="https://mohagheghsho.ir/wp-content/uploads/2020/01/Description-of-leavening-of-bread.pdf">Description of leavening of bread dough with mathematical modelling</a>
	 *
	 * @param yeast	Quantity of yeast [% w/w].
	 * @return	The maximum relative volume expansion ratio (∆V / V).
	 */
	private double maximumRelativeVolumeExpansionRatio(final double yeast){
		//FIXME this formula is for 36±1 °C
		//vertex must be at 1.1%
		return (yeast < 0.011? 24_546. * (0.022 - yeast) * yeast: 2.97);
	}

}
