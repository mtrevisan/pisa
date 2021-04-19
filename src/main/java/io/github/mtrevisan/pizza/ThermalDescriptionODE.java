package io.github.mtrevisan.pizza;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;


public class ThermalDescriptionODE implements FirstOrderDifferentialEquations{

	//https://www.tandfonline.com/doi/pdf/10.1081/JFP-120015599
	private final OvenType ovenType;
	//cheese layer thickness [m]
	private double Lc = 0.002;
	//tomato layer thickness [m]
	private double Lt = 0.002;
	//dough layer thickness [m]
	private double Lp = 0.01;
	//total pizza thickness: L = 0.014 [m]
	//moisture diffusivity [m^2/s]
	private double Dmc = 0.7e-10;

	private double Dmt, Dmp;

	private double h_rc, h_r;

	//thermal conductivity [W / (m * K)]
	private final double Kc = 0.380;
	private final double Kt = 0.546;
	private final double Kp = 0.416;

	//surface mass transfer coefficient [kgH20 / (m^2 * s)]
	private double Kmc;
	//density [kg/m^3]
	private final double rho_c = 1140.;
	private final double rho_t = 1073.;
	private final double rho_p = 862.;
	//specific heat [J / (kg * K)]
	private final double c_pc = 2864.;
	private final double c_pt = 2930.;
	private final double c_pp = 3770.;
	//latent heat of vaporization [J/kg]
	private final double Lv = 2256.9e3;
	//moisture content (0.47 to 0.55) [db?]
	private final double m_p0 = 0.5;
	private final double m_t0 = 3.73;
	private final double m_c0 = 0.826;
	//thermal diffusivity [m^2/s]
	private final double alpha_p = 0.128e-6;
	private final double alpha_t = 1.737e-7;
	private final double alpha_c = 1.164e-7;
	//baking temperature
	private double Ta, Tb;
	//ambient temperature
	private double T0;
	//ambient humidity ratio
	private double Ha;
	private double HS;


	ThermalDescriptionODE(final double cheeseLayerThickness, final double tomatoSauceLayerThickness, final double doughLayerThickness,
			final OvenType ovenType, final double bakingTemperatureTop, final double bakingTemperatureBottom, final double ambientTemperature,
			final double ambientHumidityRatio){
		this.ovenType = ovenType;

		Lc = cheeseLayerThickness;
		Lt = tomatoSauceLayerThickness;
		Lp = doughLayerThickness;

		//for tomato paste
		//FIXME depends on current temperature?
		Dmt = (ovenType == OvenType.FORCED_AIR? 9.9646e-10 * Math.exp(-605.93 / T0): 1.7738e-10 * Math.exp(-1212.71 / T0));
		//for dough
		//FIXME depends on current temperature?
		Dmp = (ovenType == OvenType.FORCED_AIR? 7.0582e-8 * Math.exp(-1890.68 / T0): 1.4596e-9 * Math.exp(-420.34 / T0));

		//FIXME depends on current temperature?
		//FIXME
		//191 204 218 1.07 1.22 1.28
		//191 204 218 1 1.06 1.21
		Kmc = (ovenType == OvenType.FORCED_AIR? 1.28: 1.21);

		Ta = bakingTemperatureTop;
		Tb = bakingTemperatureBottom;
		T0 = ambientTemperature;
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
	@Override
	public void computeDerivatives(final double t, final double[] y, final double[] yDot) throws MaxCountExceededException,
			DimensionMismatchException{
		//finite difference equations:

		//at pizza surface
		final double CS = getC(9, y) - Kmc / (Dmc * rho_c) * (HS - Ha) * Lc / (2. * m_p0);
		double thetaS;
		if(ovenType == OvenType.FORCED_AIR)
			thetaS = 1. / (h_rc + 2. * Kc / Lc) * (h_rc + 2. * Kc * getTheta(9, y) / Lc - 2. * Dmc * rho_c * Lv * m_p0 / (Lc * (Ta - T0)) * (getC(9, y) - CS));
		else
			thetaS = 1. / (h_r + 2. * Kc / Lc) * (h_r + 2. * Kc * getTheta(9, y) / Lc - 2. * Dmc * rho_c * Lv * m_p0 / (Lc * (Ta - T0)) * (getC(9, y) - CS));
		final double thetaB = (Tb - T0) / (Ta - T0);

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
		setTheta(layer, yDot, 4. * alpha_c / (Lc * Lc) * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + thetaS));
		setC(layer, yDot, 4. * Dmc / (Lc * Lc) * (getC(layer - 1, y) - 2. * getC(layer, y) + CS));
	}

	private void calculateInnerCheeseLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 4. / (rho_t * c_pt * Lt + rho_c * c_pc * Lc) * (Kt / Lt * (getTheta(layer - 1, y) - getTheta(layer, y)) - Kc / Lc * (getTheta(layer, y) - getTheta(layer + 1, y))));
		setC(layer, yDot, 4. / (Lt + Lc) * (Dmt / Lt * (getC(layer - 1, y) - getC(layer, y)) - Dmc / Lc * (getC(layer, y) - getC(layer + 1, y))));
	}

	private void calculateTomatoCheeseInterfaceLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 4. * alpha_t / (Lt * Lt) * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)));
		setC(layer, yDot, 4. * Dmt / (Lt * Lt) * (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)));
	}

	private void calculateInnerTomatoLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 20. / (rho_p * c_pp * Lp + 5. * rho_t * c_pt * Lt) * (5. * Kp / Lp * (getTheta(layer - 1, y) - getTheta(layer, y)) - Kt / Lt * (getTheta(layer, y) - getTheta(layer + 1, y))));
		setC(layer, yDot, 20. / (Lp + 5. * Lt) * (5. * Dmp / Lp * (getC(layer - 1, y) - getC(layer, y)) - Dmt / Lt * (getC(layer, y) - getC(layer + 1, y))));
	}

	private void calculateDoughTomatoInterfaceLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 100. * alpha_p / (3. * Lp * Lp) * (getTheta(layer - 1, y) - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)));
		setC(layer, yDot, 100. * Dmp / (3. * Lp * Lp) * (getC(layer - 1, y) - 3. * getC(layer, y) + 2. * getC(layer + 1, y)));
	}

	private void calculateInnerDoughLayer(final int layer, final double[] y, final double[] yDot){
		setTheta(layer, yDot, 25. * alpha_p / (Lp * Lp) * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)));
		setC(layer, yDot, 25. * Dmp / (Lp * Lp) * (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)));
	}

	private void calculateBottomLayer(final int layer, final double[] y, final double[] yDot, final double thetaB){
		setTheta(layer, yDot, 100. * alpha_p / (3. * Lp * Lp) * (thetaB - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)));
		setC(layer, yDot, 50. * Dmp / (Lp * Lp) * (getC(layer + 1, y) - getC(layer, y)));
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
