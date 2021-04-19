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
	private double Ta;
	//ambient temperature
	private double T0;
	//ambient humidity ratio
	private double Ha;
	private double HS;

	//?
	private double thetaB = 0.;


	ThermalDescriptionODE(final double cheeseLayerThickness, final double tomatoSauceLayerThickness, final double doughLayerThickness,
			final OvenType ovenType, final double bakingTemperature, final double ambientTemperature, final double ambientHumidityRatio){
		this.ovenType = ovenType;

		Lc = cheeseLayerThickness;
		Lt = tomatoSauceLayerThickness;
		Lp = doughLayerThickness;

		//FIXME
		//191 204 218 0.27e-9 0.28e-9 0.29e-9
		//191 204 218 0.13e-10 0.14e-10 0.15e-10
		Dmt = (ovenType == OvenType.FORCED_AIR? 0.29e-9: 0.15e-10);
		//FIXME
		//191 204 218 0.12e-8 0.13e-8 0.15e-8
		//191 204 218 0.59e-9 0.60e-9 0.62e-9
		Dmp = (ovenType == OvenType.FORCED_AIR? 0.15e-8: 0.62e-9);

		//FIXME
		//191 204 218 1.07 1.22 1.28
		//191 204 218 1 1.06 1.21
		Kmc = (ovenType == OvenType.FORCED_AIR? 1.28: 1.21);

		Ta = bakingTemperature;
		T0 = ambientTemperature;
		Ha = ambientHumidityRatio;

		//heat transfer coeff:
		if(ovenType == OvenType.FORCED_AIR)
			//air speed: 1 m/s
			h_rc = 1697.7 + (-9.66 + 0.02544 * bakingTemperature) * bakingTemperature;
		else
			h_rc = 8066.6 + (-76.01 + 0.19536 * bakingTemperature) * bakingTemperature;
		HS = 0.1837 + (-0.0014607 + 0.000004477 * bakingTemperature) * bakingTemperature;
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

		//node 9, cheese layer (c)
		extracted(y, yDot, CS, thetaS);

		setTheta(8, yDot, 4. / (rho_t * c_pt * Lt + rho_c * c_pc * Lc) * (Kt / Lt * (getTheta(7, y) - getTheta(8, y)) - Kc / Lc * (getTheta(8, y) - getTheta(9, y))));
		setC(8, yDot, 4. / (Lt + Lc) * (Dmt / Lt * (getC(7, y) - getC(8, y)) - Dmc / Lc * (getC(8, y) - getC(9, y))));

		//node 7, tomato paste layer (t)
		setTheta(7, yDot, 4. * alpha_t / (Lt * Lt) * (getTheta(6, y) - 2. * getTheta(7, y) + getTheta(8, y)));
		setC(7, yDot, 4. * Dmt / (Lt * Lt) * (getC(6, y) - 2. * getC(7, y) + getC(8, y)));

		setTheta(6, yDot, 20. / (rho_p * c_pp * Lp + 5. * rho_t * c_pt * Lt) * (5. * Kp / Lp * (getTheta(5, y) - getTheta(6, y)) - Kt / Lt * (getTheta(6, y) - getTheta(7, y))));
		setC(6, yDot, 20. / (Lp + 5. * Lt) * (5. * Dmp / Lp * (getC(5, y) - getC(6, y)) - Dmt / Lt * (getC(6, y) - getC(7, y))));

		//node 5, surface of the pizza layer (p)
		setTheta(5, yDot, 100. * alpha_p / (3. * Lp * Lp) * (getTheta(4, y) - 3. * getTheta(5, y) + 2. * getTheta(6, y)));
		setC(5, yDot, 100. * Dmp / (3. * Lp * Lp) * (getC(4, y) - 3. * getC(5, y) + 2. * getC(6, y)));

		//node 4 to 2, pizza
		setTheta(4, yDot, 25. * alpha_p / (Lp * Lp) * (getTheta(3, y) - 2. * getTheta(4, y) + getTheta(5, y)));
		setC(4, yDot, 25. * Dmp / (Lp * Lp) * (getC(3, y) - 2. * getC(4, y) + getC(5, y)));

		setTheta(3, yDot, 5. * alpha_p / (Lp * Lp) * (getTheta(2, y) - 2. * getTheta(3, y) + getTheta(4, y)));
		setC(3, yDot, 25. * Dmp / (Lp * Lp) * (getC(2, y) - 2. * getC(3, y) + getC(4, y)));

		setTheta(2, yDot, 25. * alpha_p / (Lp * Lp) * (getTheta(2, y) - 2. * getTheta(2, y) + getTheta(3, y)));
		setC(2, yDot, 25. * Dmp / (Lp * Lp) * (getC(1, y) - 2. * getC(2, y) + getC(3, y)));

		//node 1, pizza in contact with heated tray (p)
		setTheta(1, yDot, 100. * alpha_p / (3. * Lp * Lp) * (thetaB - 3. * getTheta(1, y) + 2. * getTheta(2, y)));
		setC(1, yDot, 50. * Dmp / (Lp * Lp) * (getC(2, y) - getC(1, y)));
	}

	private void extracted(double[] y, double[] yDot, double CS, double thetaS){
		setTheta(9, yDot, 4. * alpha_c / (Lc * Lc) * (getTheta(8, y) - 2. * getTheta(9, y) + thetaS));
		setC(9, yDot, 4. * Dmc / (Lc * Lc) * (getC(8, y) - 2. * getC(9, y) + CS));
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
