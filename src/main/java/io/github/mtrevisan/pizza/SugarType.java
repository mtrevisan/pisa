package io.github.mtrevisan.pizza;


/**
 * @see <a href="https://hal.insa-toulouse.fr/hal-02559361/file/b_b_vanDijken2000.pdf">van Dijken, Bauer, Brambilla, Duboc, Francois, Gancedo, Giuseppin, Heijen, Hoare, Lange. An interlaboratory comparison of physiological and genetic properties of four Saccharomyces cerevisiae strains. 2000</a>
 */
public enum SugarType{
	GLUCOSE(0.41 / 0.41),
	SUCROSE(0.38 / 0.41),
	MALTOSE(0.40 / 0.41),
	GALACTOSE(0.28 / 0.41);


	double factor;


	SugarType(final double factor){
		this.factor = factor;
	}

}
