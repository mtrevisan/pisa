package io.github.mtrevisan.pizza;


public class A243491{

	private static final int INDEX_PROTEIN = 0;
	private static final int INDEX_FAT = 1;
	private static final int INDEX_CARBOHYDRATE = 2;
	private static final int INDEX_FIBER = 3;
	private static final int INDEX_ASH = 4;
	private static final int INDEX_WATER = 5;


	private void rescale(final double moisture, final double[][] params, final int index){
		final double scale = params[INDEX_PROTEIN][index] + params[INDEX_FAT][index] + params[INDEX_CARBOHYDRATE][index]
			+ params[INDEX_FIBER][index] + params[INDEX_ASH][index] + params[INDEX_WATER][index] * moisture;
		params[INDEX_PROTEIN][index] = params[INDEX_PROTEIN][index] / scale;
		params[INDEX_FAT][index] = params[INDEX_FAT][index] / scale;
		params[INDEX_CARBOHYDRATE][index] = params[INDEX_CARBOHYDRATE][index] / scale;
		params[INDEX_FIBER][index] = params[INDEX_FIBER][index] / scale;
		params[INDEX_ASH][index] = params[INDEX_ASH][index] / scale;
		params[INDEX_WATER][index] = params[INDEX_WATER][index] / scale;
	}

	private double convectiveHeatTransferCoefficient(final double airSpeed, final double temperature, final double[][] airParams, final double pizzaDiameter){
		//FIXME find index x on airParams whose temperature is airParams[x - 1][0] < temperature <= airParams[x][0]
		int x = -1;
		while(temperature <= airParams[++ x][0])
			if(x == 9){
				x = 10;
				break;
			}
		final int x1 = Math.max(x - 2, 0);
		final int x2 = Math.max(x - 1, 1);

		final double airDensity = airParams[x2][1] - (airParams[x2][1] - airParams[x1][1]) * (airParams[x2][0] - temperature) / (airParams[x2][0] - airParams[x1][0]);
		final double airViscosity = airParams[x2][2] - (airParams[x2][2] - airParams[x1][2]) * (airParams[x2][0] - temperature) / (airParams[x2][0] - airParams[x1][0]);
		final double airConductivity = airParams[x2][3] - (airParams[x2][3] - airParams[x1][3]) * (airParams[x2][0] - temperature) / (airParams[x2][0] - airParams[x1][0]);
		final double prandtlNumber = airParams[x2][4] - (airParams[x2][4] - airParams[x1][4]) * (airParams[x2][0] - temperature) / (airParams[x2][0] - airParams[x1][0]);
		final double reynoldsNumber = airDensity * airSpeed * pizzaDiameter / airViscosity;
		return (airConductivity / pizzaDiameter) * 0.228 * Math.pow(reynoldsNumber, 0.731) * Math.pow(prandtlNumber, 0.333);
	}

	private double doughConductivity(final double temperature, final double[][] params, final int index){
		final double protein = 0.17881 + (0.0011958 - 2.7178e-6 * temperature) * temperature;
		final double fat = 0.18071 + (-2.7604e-4 - 1.7749e-7 * temperature) * temperature;
		final double carbohydrate = 0.20141 + (0.0013874 - 4.3312e-6 * temperature) * temperature;
		final double fiber = 0.18331 + (0.0012497 - 3.1683e-6 * temperature) * temperature;
		final double ash = 0.32962 + (0.0014011 - 2.9069e-6 * temperature) * temperature;
		final double water = 0.57109 + (0.0017625 - 6.7036e-6 * temperature) * temperature;
		return protein * params[INDEX_PROTEIN][index]
			+ fat * params[INDEX_FAT][index]
			+ carbohydrate * params[INDEX_CARBOHYDRATE][index]
			+ fiber * params[INDEX_FIBER][index]
			+ ash * params[INDEX_ASH][index]
			+ water * params[INDEX_WATER][index];
	}

	private double doughDensity(final double temperature, final double[][] params, final int index){
		final double protein = 1329.9 - 0.5184 * temperature;
		final double fat = 925.59 - 0.41757 * temperature;
		final double carbohydrate = 1599.1 - 0.31046 * temperature;
		final double fiber = 1311.5 - 0.36589 * temperature;
		final double ash = 2423.8 - 0.28063 * temperature;
		final double water = 997.18 + (0.0031439 - 0.0037575 * temperature) * temperature;
		return protein * params[INDEX_PROTEIN][index]
			+ fat * params[INDEX_FAT][index]
			+ carbohydrate * params[INDEX_CARBOHYDRATE][index]
			+ fiber * params[INDEX_FIBER][index]
			+ ash * params[INDEX_ASH][index]
			+ water * params[INDEX_WATER][index];
	}

	private double doughSpecificHeat(final double temperature, final double[][] params, final int index){
		final double protein = 2.0082 + (0.0012089 - 1.3129e-6 * temperature) * temperature;
		final double fat = 1.9842 + (0.0014733 - 4.8008e-6 * temperature) * temperature;
		final double carbohydrate = 1.5488 + (0.0019625 - 5.9399e-6 * temperature) * temperature;
		final double fiber = 1.8459 + (0.0018306 - 4.6509e-6 * temperature) * temperature;
		final double ash = 1.0926 + (0.0018896 - 3.6817e-6 * temperature) * temperature;
		final double water = 4.1289 + (-9.0864e-5 + 5.4761e-6 * temperature) * temperature;
		return 1000. * (protein * params[INDEX_PROTEIN][index]
			+ fat * params[INDEX_FAT][index]
			+ carbohydrate * params[INDEX_CARBOHYDRATE][index]
			+ fiber * params[INDEX_FIBER][index]
			+ ash * params[INDEX_ASH][index]
			+ water * params[INDEX_WATER][index]);
	}

}
