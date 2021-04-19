package io.github.mtrevisan.pizza;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;


public class ThermalDescriptionODE implements FirstOrderDifferentialEquations{

	//https://www.tandfonline.com/doi/pdf/10.1081/JFP-120015599
	private final OvenType ovenType;

	/**  [m] */
	private double cheeseLayerThickness;
	/**  [m] */
	private double tomatoLayerThickness;
	/**  [m] */
	private double doughLayerThickness;

	private final double bakingTemperatureTop;
	private final double bakingTemperatureBottom;
	private final double ambientTemperature;

	//ambient humidity ratio
	private double Ha;
	private double HS;

	//moisture diffusivity [m^2/s]
	private double Dmc, Dmt, Dmp;

	private double h_rc, h_r;

	/** K [W / (m * K)] */
	private final double thermalConductivityCheese = 0.380;
	/** K [W / (m * K)] */
	private final double thermalConductivityTomato = 0.546;
	/** K [W / (m * K)] */
	private final double thermalConductivityDough = 0.416;

	//surface mass transfer coefficient [kgH20 / (m^2 * s)]
	private double Kmc;
	/** [kg/m^3] */
	private final double densityCheese = 1140.;
	/** [kg/m^3] */
	private final double densityTomato = 1073.;
	/** [kg/m^3] */
	private final double densityDough = 862.;
	/** [J / (kg * K)] */
	private final double specificHeatCheese = 2864.;
	/** [J / (kg * K)] */
	private final double specificHeatTomato = 2930.;
	/** [J / (kg * K)] */
	private final double specificHeatDough = 3770.;
	/** Lv [J/kg] */
	private final double vaporizationLatentHeat = 2256.9e3;
	/** (0.47 to 0.55) [db?] */
	private final double moistureContentCheese0 = 0.826;
	/** [db?] */
	private final double moistureContentTomato0 = 3.73;
	/** [db?] */
	private final double moistureContentDough0 = 0.5;
	/** alpha [m^2/s] */
	private final double thermalDiffusivityCheese = 1.164e-7;
	/** alpha [m^2/s] */
	private final double thermalDiffusivityTomato = 1.737e-7;
	/** alpha [m^2/s] */
	private final double thermalDiffusivityDough = 0.128e-6;


	ThermalDescriptionODE(final double cheeseLayerThickness, final double tomatoLayerThickness, final double doughLayerThickness,
			final OvenType ovenType, final double bakingTemperatureTop, final double bakingTemperatureBottom, final double ambientTemperature,
			final double ambientHumidityRatio){
		this.ovenType = ovenType;

		this.cheeseLayerThickness = cheeseLayerThickness;
		this.tomatoLayerThickness = tomatoLayerThickness;
		this.doughLayerThickness = doughLayerThickness;

		this.bakingTemperatureTop = bakingTemperatureTop;
		this.bakingTemperatureBottom = bakingTemperatureBottom;
		this.ambientTemperature = ambientTemperature;

		//for tomato paste
		Dmc = 0.7e-10;
		//FIXME depends on current temperature?
		Dmt = (ovenType == OvenType.FORCED_AIR? 9.9646e-10 * Math.exp(-605.93 / ambientTemperature): 1.7738e-10 * Math.exp(-1212.71 / ambientTemperature));
		//for dough
		//FIXME depends on current temperature?
		Dmp = (ovenType == OvenType.FORCED_AIR? 7.0582e-8 * Math.exp(-1890.68 / ambientTemperature): 1.4596e-9 * Math.exp(-420.34 / ambientTemperature));

		//FIXME depends on current temperature?
		Kmc = (ovenType == OvenType.FORCED_AIR? 4.6332 * Math.exp(-277.5 / ambientTemperature): 4.5721 * Math.exp(-292.8 / ambientTemperature));

		Ha = ambientHumidityRatio;

		//heat transfer coeff:
		if(ovenType == OvenType.FORCED_AIR)
			//air speed: 1 m/s
			h_rc = 1697.7 + (-9.66 + 0.02544 * bakingTemperatureTop) * bakingTemperatureTop;
		else
			h_rc = 8066.6 + (-76.01 + 0.19536 * bakingTemperatureTop) * bakingTemperatureTop;
		HS = 0.1837 + (-0.0014607 + 0.000004477 * bakingTemperatureTop) * bakingTemperatureTop;
	}

	@Override
	public int getDimension(){
		return 18;
	}

	//y is a list of theta and C from layer 9 to layer 1
	//yDot is a list of dTheta/dt and dC/dt from layer 9 to layer 1
	//theta is (T - ambientTemperature) / (bakingTemperatureTop - ambientTemperature)
	//C is m / m_p0
	//psi is x / L
	@Override
	public void computeDerivatives(final double t, final double[] y, final double[] yDot) throws MaxCountExceededException,
			DimensionMismatchException{
		//finite difference equations:

		//at pizza surface
		final double CS = getC(9, y) - Kmc / (Dmc * densityCheese) * (HS - Ha) * cheeseLayerThickness / (2. * moistureContentDough0);
		double thetaS;
		if(ovenType == OvenType.FORCED_AIR)
			thetaS = 1. / (h_rc + 2. * thermalConductivityCheese / cheeseLayerThickness) * (h_rc + 2. * thermalConductivityCheese * getTheta(9, y) / cheeseLayerThickness - 2. * Dmc * densityCheese * vaporizationLatentHeat * moistureContentDough0 / (cheeseLayerThickness * (bakingTemperatureTop - ambientTemperature)) * (getC(9, y) - CS));
		else
			thetaS = 1. / (h_r + 2. * thermalConductivityCheese / cheeseLayerThickness) * (h_r + 2. * thermalConductivityCheese * getTheta(9, y) / cheeseLayerThickness - 2. * Dmc * densityCheese * vaporizationLatentHeat * moistureContentDough0 / (cheeseLayerThickness * (bakingTemperatureTop - ambientTemperature)) * (getC(9, y) - CS));
		final double thetaB = (bakingTemperatureBottom - ambientTemperature) / (bakingTemperatureTop - ambientTemperature);

		//node 9, cheese layer (c)
		calculateUpperLayer(9, y, yDot, thetaS, CS);

		calculateInnerCheeseLayer(8, y, yDot);

		//node 7, tomato paste layer (t)
		calculateTomatoCheeseInterfaceLayer(7, y, yDot);

		calculateInnerTomatoLayer(6, y, yDot);

		//node 5, surface of the pizza layer (p)
		calculateDoughTomatoInterfaceLayer(5, y, yDot);

		//node 4 to 2, pizza
		calculateInnerDoughLayer(4, y, yDot);

		calculateInnerDoughLayer(3, y, yDot);

		calculateInnerDoughLayer(2, y, yDot);

		//node 1, pizza in contact with heated tray (p)
		calculateBottomLayer(1, y, yDot, thetaB);
	}

	private void calculateUpperLayer(final int layer, final double[] y, final double[] yDot, final double thetaS, final double CS){
		setTheta(layer, yDot, 4. * thermalDiffusivityCheese / (cheeseLayerThickness * cheeseLayerThickness) * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + thetaS));
		setC(layer, yDot, 4. * Dmc / (cheeseLayerThickness * cheeseLayerThickness) * (getC(layer - 1, y) - 2. * getC(layer, y) + CS));
	}

	private void calculateInnerCheeseLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 4. / (densityTomato * specificHeatTomato * tomatoLayerThickness + densityCheese * specificHeatCheese * cheeseLayerThickness) * (thermalConductivityTomato / tomatoLayerThickness * (getTheta(layer - 1, y) - getTheta(layer, y)) - thermalConductivityCheese / cheeseLayerThickness * (getTheta(layer, y) - getTheta(layer + 1, y))));
		setC(layer, yDot, 4. / (tomatoLayerThickness + cheeseLayerThickness) * (Dmt / tomatoLayerThickness * (getC(layer - 1, y) - getC(layer, y)) - Dmc / cheeseLayerThickness * (getC(layer, y) - getC(layer + 1, y))));
	}

	private void calculateTomatoCheeseInterfaceLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 4. * thermalDiffusivityTomato / (tomatoLayerThickness * tomatoLayerThickness) * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)));
		setC(layer, yDot, 4. * Dmt / (tomatoLayerThickness * tomatoLayerThickness) * (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)));
	}

	private void calculateInnerTomatoLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 20. / (densityDough * specificHeatDough * doughLayerThickness + 5. * densityTomato * specificHeatTomato * tomatoLayerThickness) * (5. * thermalConductivityDough / doughLayerThickness * (getTheta(layer - 1, y) - getTheta(layer, y)) - thermalConductivityTomato / tomatoLayerThickness * (getTheta(layer, y) - getTheta(layer + 1, y))));
		setC(layer, yDot, 20. / (doughLayerThickness + 5. * tomatoLayerThickness) * (5. * Dmp / doughLayerThickness * (getC(layer - 1, y) - getC(layer, y)) - Dmt / tomatoLayerThickness * (getC(layer, y) - getC(layer + 1, y))));
	}

	private void calculateDoughTomatoInterfaceLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 100. * thermalDiffusivityDough / (3. * doughLayerThickness * doughLayerThickness) * (getTheta(layer - 1, y) - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)));
		setC(layer, yDot, 100. * Dmp / (3. * doughLayerThickness * doughLayerThickness) * (getC(layer - 1, y) - 3. * getC(layer, y) + 2. * getC(layer + 1, y)));
	}

	private void calculateInnerDoughLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 25. * thermalDiffusivityDough / (doughLayerThickness * doughLayerThickness) * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)));
		setC(layer, yDot, 25. * Dmp / (doughLayerThickness * doughLayerThickness) * (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)));
	}

	private void calculateBottomLayer(final int layer, final double[] y, final double[] yDot, final double thetaB){
		setTheta(layer, yDot, 100. * thermalDiffusivityDough / (3. * doughLayerThickness * doughLayerThickness) * (thetaB - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)));
		setC(layer, yDot, 50. * Dmp / (doughLayerThickness * doughLayerThickness) * (getC(layer + 1, y) - getC(layer, y)));
	}

	private double getTheta(final int layer, final double[] array){
		return array[(layer - 1) * 2];
	}

	private void setTheta(final int layer, final double[] array, final double value){
		array[(layer - 1) * 2] = value;
	}

	private double getC(final int layer, final double[] array){
		return array[(layer - 1) * 2 + 1];
	}

	private void setC(final int layer, final double[] array, final double value){
		array[(layer - 1) * 2 + 1] = value;
	}

}
