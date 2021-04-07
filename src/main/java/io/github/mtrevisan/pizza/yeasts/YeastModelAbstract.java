package io.github.mtrevisan.pizza.yeasts;


public abstract class YeastModelAbstract{

	//Temperature above which no growth occurs [°C]
	public abstract double getTemperatureMax();

	//Temperature at which the maximum specific growth rate equals its optimal value [°C]
	abstract double getTemperatureOpt();

	//Temperature below which no growth occurs [°C]
	public abstract double getTemperatureMin();

	//Maximum specific growth rate [hrs^-1]
	abstract double getMuOpt();


	/**
	 * @see <a href="Bakery Products Science and Technology">https://books.google.it/books?id=tV7BAwAAQBAJ&pg=PT787&lpg=PT787&dq=%22maximum+relative+volume+expansion+ratio%22&source=bl&ots=EJHiXqlWjY&sig=ACfU3U3wsl5X9X293TK-9g4mnT3LUkN7CQ&hl=en&sa=X&ved=2ahUKEwj42NTXyevvAhUkM-wKHeo1CO8Q6AEwAHoECAEQAw#v=onepage&q=%22maximum%20relative%20volume%20expansion%20ratio%22&f=false</a>
	 *
	 * @param time	Time [hrs].
	 * @param lambda	Time during lag phase [hrs].
	 * @param alpha	Maximum relative volume expansion ratio.
	 * @param temperature	Temperature [°C].
	 * @param ingredientsFactor	Factor to account for other ingredients effects.
	 * @return	Volume expansion ratio.
	 */
	public double volumeExpansionRatio(final double time, final double lambda, final double alpha, final double temperature,
			final double ingredientsFactor){
		final double tMin = getTemperatureMin();
		final double tMax = getTemperatureMax();
		if(time == 0. || temperature <= tMin || tMax <= temperature)
			return 0.;

		final double tOpt = getTemperatureOpt();
		final double d = (temperature - tMax) * Math.pow(temperature - tMin, 2.);
		final double e = (tOpt - tMin) * ((tOpt - tMin) * (temperature - tOpt) - (tOpt - tMax) * (tOpt + tMin - 2. * temperature));
		final double mu = ingredientsFactor * getMuOpt() * (d / e);

		return alpha * Math.exp(-Math.exp(mu * Math.exp(1.) * (lambda - time) / alpha + 1.));
	}

}
