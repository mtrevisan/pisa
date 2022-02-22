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

import io.github.mtrevisan.pizza.yeasts.SaccharomycesCerevisiaePedonYeast;
import io.github.mtrevisan.pizza.yeasts.YeastModelAbstract;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BaseUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;


//effect of ingredients!! https://www.maltosefalcons.com/blogs/brewing-techniques-tips/yeast-propagation-and-maintenance-principles-and-practices
public final class Dough2{

	private static final Logger LOGGER = LoggerFactory.getLogger(Dough2.class);


	/** [% w/w] */
	private static final double SOLVER_YEAST_MAX = 0.2;
	private static final int SOLVER_EVALUATIONS_MAX = 100;

	private static final double DOUGH_WEIGHT_PRECISION = 0.001;

	//accuracy is ±0.001%
	private final BaseUnivariateSolver<UnivariateFunction> solverYeast = new BracketingNthOrderBrentSolver(0.000_01,
		5);

	private final YeastModelAbstract yeastModel;

	/** Yeast quantity [% w/w]. */
	double yeast;
	private YeastType yeastType;
	/** Raw yeast content [% w/w]. */
	private double rawYeast = 1.;


	public static Dough2 create(final YeastModelAbstract yeastModel) throws DoughException{
		return new Dough2(yeastModel);
	}


	private Dough2(final YeastModelAbstract yeastModel) throws DoughException{
		if(yeastModel == null)
			throw DoughException.create("A yeast model must be provided");

		this.yeastModel = yeastModel;
	}


	/**
	 * @param yeastType	Yeast type.
	 * @param rawYeast	Raw yeast content [% w/w].
	 * @return	The instance.
	 */
	public Dough2 withYeast(final YeastType yeastType, final double rawYeast) throws DoughException{
		if(yeastType == null)
			throw DoughException.create("Missing yeast type");
		if(rawYeast <= 0. || rawYeast > 1.)
			throw DoughException.create("Raw yeast quantity must be between 0 and 1");

		this.yeastType = yeastType;
		this.rawYeast = rawYeast;

		return this;
	}

	/**
	 * @param procedure	The recipe procedure.
	 * @return	The recipe.
	 */
	public void createRecipe(final Procedure procedure) throws YeastException{
		calculateYeast(procedure);
	}

	public static void main(String[] args) throws DoughException, YeastException{
		Dough2 dough = Dough2.create(new SaccharomycesCerevisiaePedonYeast())
			.withYeast(YeastType.FRESH, 1.);
		LeaveningStage stage1 = LeaveningStage.create(35., Duration.ofHours(6l));
		Procedure procedure = Procedure.create(new LeaveningStage[]{stage1}, 0.9,
				0,
				Duration.ofMinutes(15l), Duration.ofMinutes(15l),
				LocalTime.of(20, 15));
		dough.calculateYeast(procedure);
	}

	/**
	 * Find the initial yeast able to obtain a given volume expansion ratio after a series of consecutive stages at a given duration at
	 * temperature.
	 *
	 * @param procedure	Data for procedure.
	 */
	@SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
	void calculateYeast(final Procedure procedure) throws YeastException{
		//reset variable
		yeast = 0.;

		try{
			final UnivariateFunction f = yeast -> volumeExpansionRatioDifference(yeast, procedure);
			yeast = solverYeast.solve(SOLVER_EVALUATIONS_MAX, f, 0., SOLVER_YEAST_MAX);
		}
		catch(final NoBracketingException e){
			throw YeastException.create("No yeast quantity will ever be able to produce the given expansion ratio");
		}
		catch(final TooManyEvaluationsException e){
			throw YeastException.create("Cannot calculate yeast quantity, try increasing maximum number of evaluations in the solver");
		}
	}

	//https://www.mdpi.com/2076-2607/9/1/47/htm
	//https://www.researchgate.net/publication/318756298_Bread_Dough_and_Baker's_Yeast_An_Uplifting_Synergy
	//https://www.sciencedirect.com/science/article/pii/S2221169117309802
	//https://ojs.library.ubc.ca/index.php/expedition/article/view/196129
	//http://arccarticles.s3.amazonaws.com/webArticle/articles/jdfhs282010.pdf
	double volumeExpansionRatioDifference(final double yeast, final Procedure procedure){
		final LeaveningStage currentStage = procedure.leaveningStages[0];
		//lag phase duration [hrs]
		//TODO calculate lambda
		final double lambda = 0.5;

		final double volumeExpansionRatio = volumeExpansionRatio(yeast, lambda, currentStage.temperature, toHours(currentStage.duration));

		final double difference = volumeExpansionRatio - procedure.targetDoughVolumeExpansionRatio;
		//0.5% yeast = 10 v/v with 28% sugar at 35 °C
		//1.67% yeast = 23.3 v/v with 28% sugar at 35 °C
		//2.96% ADY in H₂O with 1.69% sugar
		//	20 °C = 0 ml
		//	27 °C = 423.1 ml
		//	35 °C = 691.02 ml
		return difference;
	}

	private double volumeExpansionRatio(final double yeast, final double lambda, final double temperature, final double duration){
		//maximum relative volume expansion ratio
		final double alpha = maximumRelativeVolumeExpansionRatio(yeast);
		//TODO calculate ingredientsFactor

		final double volumeExpansionRatio = yeastModel.volumeExpansionRatio(duration, lambda, alpha, temperature, 1.);

		//correct for yeast quantity:
		//http://arccarticles.s3.amazonaws.com/webArticle/articles/jdfhs282010.pdf
		//adjust k so that 4% yeast in flour with 1.5% sugar and 60% water at 27-30 °C for 1 hrs: raise 220%
		//that is, k = 2.2 / (yeastModel.volumeExpansionRatio(1., lambda, alpha, (27. + 30.) / 2., 1.) * 0.04)
		final double k = 25.2;
		return k * volumeExpansionRatio * yeast;
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

	private static double toHours(final Duration duration){
		return duration.toMinutes() / 60.;
	}

}
