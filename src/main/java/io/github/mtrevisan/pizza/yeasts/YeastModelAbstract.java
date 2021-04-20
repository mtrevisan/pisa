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
package io.github.mtrevisan.pizza.yeasts;


public abstract class YeastModelAbstract{

	//Temperature below which no growth occurs [°C]
	public abstract double getTemperatureMin();

	//Temperature at which the maximum specific growth rate equals its optimal value [°C]
	abstract double getTemperatureOpt();

	//Temperature above which no growth occurs [°C]
	public abstract double getTemperatureMax();

	//Maximum specific growth rate [hrs^-1]
	abstract double getMaximumSpecificGrowthRate();


	/**
	 * Calculate maximum specific growth.
	 *
	 * @see <a href="https://books.google.it/books?id=tV7BAwAAQBAJ&pg=PT787&lpg=PT787&dq=%22maximum+relative+volume+expansion+ratio%22&source=bl&ots=EJHiXqlWjY&sig=ACfU3U3wsl5X9X293TK-9g4mnT3LUkN7CQ&hl=en&sa=X&ved=2ahUKEwj42NTXyevvAhUkM-wKHeo1CO8Q6AEwAHoECAEQAw#v=onepage&q=%22maximum%20relative%20volume%20expansion%20ratio%22&f=false">Bakery Products Science and Technology</a>
	 * @see <a href="https://aem.asm.org/content/aem/77/7/2292.full.pdf">Temperature adaptation markedly determines evolution within the genus Saccharomyces</a>
	 * @see <a href="https://storage.googleapis.com/plos-corpus-prod/10.1371/journal.pone.0178691/1/pone.0178691.pdf?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=wombat-sa%40plos-prod.iam.gserviceaccount.com%2F20210407%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=20210407T105230Z&X-Goog-Expires=3600&X-Goog-SignedHeaders=host&X-Goog-Signature=36c021ec5c4487f1b4891b2a52ecec7ba7623eecd8c491bfec1e3b3c72796d6b9b7da246efbee46d40c90b9fe6177fe93362d61aadd45557107f99b2900c7945027fc77e1c070df644fd69cc4a0b5cce94fad524239dedc3e350fc61c700b8d2e42562beaa0ce2e3420aaecf64bcc72b8fbc3ca4006470a244d138022d20d01fa30cb3cc709c5da219b75836a1d9e32ef8014e73c4b10be1d5d494db081285be533cdbc342d055efd13ff6b69c3194c3884601bf4bdb3a7a8a21aa798b8efd6e24078e494275dfd4c854ec3d2eb4e7d2bc26bd8267857d00566f21a23431937902f996aff428af6c83ecad7783be505dd29133c7c0f22d3307948d34306e3058">The use of Gompertz models in growth analyses, and new gompertz-model approach: An addition to the Unified-Richards family</a>
	 *
	 * @param temperature	Temperature [°C].
	 * @return	Volume expansion ratio.
	 */
	public double maximumSpecificGrowth(final double temperature){
		final double tMin = getTemperatureMin();
		final double tMax = getTemperatureMax();
		if(temperature <= tMin || tMax <= temperature)
			return 0.;

		final double tOpt = getTemperatureOpt();
		final double d = (temperature - tMax) * Math.pow(temperature - tMin, 2.);
		final double e = (tOpt - tMin) * ((tOpt - tMin) * (temperature - tOpt) - (tOpt - tMax) * (tOpt + tMin - 2. * temperature));
		return getMaximumSpecificGrowthRate() * (d / e);
	}

	/**
	 * Calculate volume expansion ratio.
	 *
	 * @see <a href="https://books.google.it/books?id=tV7BAwAAQBAJ&pg=PT787&lpg=PT787&dq=%22maximum+relative+volume+expansion+ratio%22&source=bl&ots=EJHiXqlWjY&sig=ACfU3U3wsl5X9X293TK-9g4mnT3LUkN7CQ&hl=en&sa=X&ved=2ahUKEwj42NTXyevvAhUkM-wKHeo1CO8Q6AEwAHoECAEQAw#v=onepage&q=%22maximum%20relative%20volume%20expansion%20ratio%22&f=false">Bakery Products Science and Technology</a>
	 * @see <a href="https://aem.asm.org/content/aem/77/7/2292.full.pdf">Temperature adaptation markedly determines evolution within the genus Saccharomyces</a>
	 * @see <a href="https://storage.googleapis.com/plos-corpus-prod/10.1371/journal.pone.0178691/1/pone.0178691.pdf?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=wombat-sa%40plos-prod.iam.gserviceaccount.com%2F20210407%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=20210407T105230Z&X-Goog-Expires=3600&X-Goog-SignedHeaders=host&X-Goog-Signature=36c021ec5c4487f1b4891b2a52ecec7ba7623eecd8c491bfec1e3b3c72796d6b9b7da246efbee46d40c90b9fe6177fe93362d61aadd45557107f99b2900c7945027fc77e1c070df644fd69cc4a0b5cce94fad524239dedc3e350fc61c700b8d2e42562beaa0ce2e3420aaecf64bcc72b8fbc3ca4006470a244d138022d20d01fa30cb3cc709c5da219b75836a1d9e32ef8014e73c4b10be1d5d494db081285be533cdbc342d055efd13ff6b69c3194c3884601bf4bdb3a7a8a21aa798b8efd6e24078e494275dfd4c854ec3d2eb4e7d2bc26bd8267857d00566f21a23431937902f996aff428af6c83ecad7783be505dd29133c7c0f22d3307948d34306e3058">The use of Gompertz models in growth analyses, and new gompertz-model approach: An addition to the Unified-Richards family</a>
	 *
	 * @param time	Time [hrs].
	 * @param lambda	Time during lag phase [hrs].
	 * @param alpha	Maximum relative volume expansion ratio.
	 * @param temperature	Temperature [°C].
	 * @param ingredientsFactor	Factor to account for other ingredients effects.
	 * @return	Volume expansion ratio (∆V / V).
	 */
	public double volumeExpansionRatio(final double time, final double lambda, final double alpha, final double temperature,
			final double ingredientsFactor){
		final double mu = ingredientsFactor * maximumSpecificGrowth(temperature);
		return (alpha > 0. && time > 0.? alpha * Math.exp(-Math.exp(mu * Math.E * (lambda - time) / alpha + 1.)): 0.);
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "{T(" + getTemperatureMin() + ", " + getTemperatureOpt() + ", " + getTemperatureMax()
			+ "), μ: " + getMaximumSpecificGrowthRate() + "}";
	}

}
