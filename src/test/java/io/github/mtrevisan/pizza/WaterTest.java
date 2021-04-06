package io.github.mtrevisan.pizza;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class WaterTest{

	//[hPa]
	private static final double ATMOSPHERE = 1013.25;


	@Test
	void boilingTemperatureAmbient(){
		final Water water = new Water();
		final double temperature = water.boilingTemperature(0., ATMOSPHERE);

		Assertions.assertEquals(100.0, temperature, 0.1);
	}

	@Test
	void boilingTemperatureAmbientWithSalt(){
		final Water water = new Water();
		final double temperature = water.boilingTemperature(0.0524, ATMOSPHERE);

		Assertions.assertEquals(100.9, temperature, 0.1);
	}

	@Test
	void boilingTemperatureHalfway(){
		final Water water = new Water();
		final double temperature = water.boilingTemperature(0.0052, ATMOSPHERE * 2);

		Assertions.assertEquals(5_211.3, temperature, 0.1);
	}


	@Test
	void densityPureAmbient(){
		final Water water = new Water();
		final double density = water.density(0., 50., ATMOSPHERE);

		Assertions.assertEquals(0.988_1, density, 0.000_1);
	}

	@Test
	void densityAmbient(){
		final Water water = new Water();
		final double density = water.density(0.52, 50., ATMOSPHERE);

		Assertions.assertEquals(0.988_5, density, 0.000_1);
	}

}
