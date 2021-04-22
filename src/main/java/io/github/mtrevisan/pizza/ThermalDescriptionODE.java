package io.github.mtrevisan.pizza;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;


/**
 * @see <a href="https://www.tandfonline.com/doi/pdf/10.1081/JFP-120015599">Dumas, Mittal. Heat and mass transfer properties of pizza during baking. 2007.</a>
 * @see <a href="https://en.wikipedia.org/wiki/Latent_heat">Latent heat</a>
 * @see <a href="https://en.wikipedia.org/wiki/Thermal_diffusivity">Thermal diffusivity</a>
 *
 * https://www.engineeringtoolbox.com/conductive-heat-transfer-d_428.html
 * https://www.researchgate.net/publication/280735585_One-Dimensional_Solar_Heat_Load_Simulation_Model_for_a_Parked_Car
 *
 * https://www.witpress.com/Secure/elibrary/papers/9781853129322/9781853129322008FU1.pdf
 */
public class ThermalDescriptionODE implements FirstOrderDifferentialEquations{

	/**  [m] */
	private final double cheeseLayerThickness;
	/**  [m] */
	private final double tomatoLayerThickness;
	/**  [m] */
	private final double doughLayerThickness;

	private final double bakingTemperatureTop;
	private final double bakingTemperatureBottom;
	private final double ambientTemperature;

	//ambient humidity ratio
	private final double ambientHumidityRatio;
	//surface humidity ratio
	private final double surfaceHumidityRatio;

	//moisture diffusivity [m^2/s]
	private final double moistureDiffusivityCheese, moistureDiffusivityTomato, moistureDiffusivityDough;

	private final double heatTransferCoeff;

	/** K [W / (m * K)] */
	private final double thermalConductivityCheese = 0.380;
	/** K [W / (m * K)] */
	private final double thermalConductivityTomato = 0.546;
	/** K [W / (m * K)] */
	private final double thermalConductivityDough = 0.416;

	//surface mass transfer coefficient [kgH20 / (m^2 * s)]
	private final double surfaceMassTransfer;
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
	/** Initial moisture content (0.47 to 0.55) [%] */
	private final double moistureContentCheese0 = 0.826;
	/** Initial moisture content [%] */
	private final double moistureContentTomato0 = 3.73;
	/** Initial moisture content [%] */
	private final double moistureContentDough0 = 0.65;
	/** alpha = thermal_conductivity / (rho * specific_heat_capacity) [m^2/s] */
	private final double thermalDiffusivityCheese = 1.164e-7;
	/** alpha = thermal_conductivity / (rho * specific_heat_capacity) [m^2/s] */
	private final double thermalDiffusivityTomato = 1.737e-7;
	/** alpha = thermal_conductivity / (rho * specific_heat_capacity) [m^2/s] */
	private final double thermalDiffusivityDough = 0.128e-6;


	ThermalDescriptionODE(final double cheeseLayerThickness, final double tomatoLayerThickness, final double doughLayerThickness,
			final OvenType ovenType, final double bakingTemperatureTop, final double bakingTemperatureBottom, final double ambientTemperature,
			final double ambientHumidityRatio){
		this.cheeseLayerThickness = cheeseLayerThickness;
		this.tomatoLayerThickness = tomatoLayerThickness;
		//TODO consider expansion during baking due to Charles-Gay Lussac law
		this.doughLayerThickness = doughLayerThickness;

		this.bakingTemperatureTop = bakingTemperatureTop;
		this.bakingTemperatureBottom = bakingTemperatureBottom;
		this.ambientTemperature = ambientTemperature;

		this.ambientHumidityRatio = ambientHumidityRatio;

		moistureDiffusivityCheese = 0.7e-10;
		//for tomato paste
		//FIXME depends on current temperature?
		moistureDiffusivityTomato = (ovenType == OvenType.FORCED_AIR?
			9.9646e-10 * Math.exp(-605.93 / ambientTemperature):
			1.7738e-10 * Math.exp(-1212.71 / ambientTemperature));
		//for dough
		//FIXME depends on current temperature?
		moistureDiffusivityDough = (ovenType == OvenType.FORCED_AIR?
			7.0582e-8 * Math.exp(-1890.68 / ambientTemperature):
			1.4596e-9 * Math.exp(-420.34 / ambientTemperature));

		//FIXME depends on current temperature?
		surfaceMassTransfer = (ovenType == OvenType.FORCED_AIR?
			4.6332 * Math.exp(-277.5 / ambientTemperature):
			4.5721 * Math.exp(-292.8 / ambientTemperature));

		//heat transfer coefficient:
		if(ovenType == OvenType.FORCED_AIR)
			//convective air speed: 1 m/s
			heatTransferCoeff = 1697.7 + (-9.66 + 0.02544 * bakingTemperatureTop) * bakingTemperatureTop;
		else
			heatTransferCoeff = 8066.6 + (-76.01 + 0.19536 * bakingTemperatureTop) * bakingTemperatureTop;
		surfaceHumidityRatio = 0.1837 + (-0.0014607 + 0.000004477 * bakingTemperatureTop) * bakingTemperatureTop;
	}

	@Override
	public int getDimension(){
		return 18;
	}

	public double[] getInitialState(){
		//array of initial temperature (as (T - ambientTemperature) / (bakingTemperatureTop - ambientTemperature)) and moisture content
		//by column
		return new double[]{
			//node 9, cheese layer
			0., ambientHumidityRatio,
			0., moistureContentCheese0 / moistureContentDough0,
			//node 7, tomato paste layer
			0., (moistureContentTomato0 + moistureContentCheese0) / (2. * moistureContentDough0),
			0., moistureContentTomato0 / moistureContentDough0,
			//node 5, surface of the dough layer
			0., (moistureContentDough0 + moistureContentTomato0) / (2. * moistureContentDough0),
			//node 4, dough
			0., 1.,
			//node 3, dough
			0., 1.,
			//node 2, dough
			0., 1.,
			//node 1, dough in contact with heated tray
			(bakingTemperatureBottom - ambientTemperature) / (bakingTemperatureTop - ambientTemperature), 1.};
	}

/*
moisture transfer:
dm/dt = Dm * d^2m/dt^2
heat transfer:
dT/dt = alpha * d^2T/dtx2

at the surface:
hr * (Ta - TS) = Kc * dT/dx|x=S + Dm_cS * rho_c * Lv * dm/dx|x=S
where hr is the heat transfer coefficient [W / (m^2 * K)]
where K is the surface mass transfer coefficient [kg H2O / (m^2 * s)]
where Dm is the moisture diffusivity [m^2 / s]
where rho is the density [kg / m^3]
where Lv is the latent heat of vaporization [J / kg]

heat transfer at the interface between the dough and the tomato layer:
Kd * dT/dx|x=5-6 - Kt * dT/dx|x=6-7 = dT6/dt * (rho_d * cp_d * delta_x5-6 / 2 + rho_t * cp_t * delta_x6-7 / 2)

heat transfer at the interface between the tomato and the cheese layer:
Kt * dT/dx|x=7-8 - Kc * dT/dx|x=8-9 = dT8/dt * (rho_t * cp_t * delta_x7-8 / 2 + rho_c * cp_c * delta_x8-9 / 2)

moisture transfer at the top surface:
Dm_cS * rho_c * dm/dx|x=S = Km_c * (Hs - Ha)
where Hs is the pizza surface humidity ratio [kg H2O / kg dry air]
where Ha is the air humidity ratio [kg H2O / kg dry air]

moisture transfer at the interface between the tomato and the cheese layer:
Dm_tc * dm/dx|x=7-8 - Dm_cS * dm/dx|x=8-9 = dm8/dt * (delta_x7-8 + delta_x8-9) / 2

moisture transfer at the interface between the dough and the tomato paste:
Dm_dt * dm/dx|x=5-6 - Dm_tc * dm/dx|x=6-7 = dm6/dt * (delta_x5-6 + delta_x6-7) / 2

C = m / mp0
theta = (T - T0) / (Ta - T0)
psi = x / L
L = Ld + Lt + Lc

moisture transfer becomes:
dC/dt = Dm / L^2 * d^2C/dpsi^2
heat transfer becomes:
dtheta_dt = alpha / L^2 * d^2theta/dpsi^2
at the surface becomes:
hr * (1 - thetaS) = Kc / L * dthetaS/dpsi + Dm_cS * rho_c * Lv * md0 * / (L * (Ta - T0)) * dCS/dpsi

boundary conditions, theta(psi, t) and C(psi, t):
theta(psi, 0) = 0
C(0 < psi < Ld / L, 0) = 1
C(Ld / L, 0) = (md0 + mt0) / (2 * md0)
C(Ld / L < psi < (Lt + Ld) / L, 0) = mt0 / md0
C((Ld + Ld) / L, 0) = (mt0 + mc0) / md0
C((Ld + Lt) / L < psi < 1, 0) = mc0 / md0
thetaB = (Tb - T0) / (Ta - T0)

at the surface of cheese layer:
Dm_cS * rho_c / L * dCS/dpsi * mp0 = Km_c * (HS - Ha)

at the interface node 8 (tomato-cheese):
Kt / L * dtheta/dpsi|7-8 - Kc / L * dtheta/dpsi|8-9 = dtheta8/dt * (rho_t * cp_t * delta_x_7-8 + rho_c * cp_c * delta_x_8-9) / 2
Dm_tc / L * dC/dpsi|7-8 - Dm_cS / L * dC/dpsi|8-9 = dC8/dt * (delta_x_7-8 + delta_x_8-9) / 2

at the interface node 6 (dough-tomato):
Kd / L * dtheta/dpsi|5-6 - Kt / L * dtheta/dpsi|6-7 = dtheta6/dt * (rho_d * cp_d * delta_x_5-6 + rho_t * cp_t * delta_x_6-7) / 2
Dm_dt / L * dC/dpsi|5-6 - Dm_tc / L * dC/dpsi|6-7 = dC6/dt * (delta_x_5-6 + delta_x_6-7) / 2

at the bottom:
dC/dpsi|psi=0 = 0
*/

	//y is a list of theta and C from layer 9 to layer 1
	//dydt is a list of dTheta/dt and dC/dt from layer 9 to layer 1
	@Override
	public void computeDerivatives(final double t, final double[] y, final double[] dydt) throws MaxCountExceededException,
			DimensionMismatchException{
		//finite difference equations:

		//at pizza surface
		final double moistureContentSurface = getC(9, y) - surfaceMassTransfer / (moistureDiffusivityCheese * densityCheese)
			* (surfaceHumidityRatio - ambientHumidityRatio) * cheeseLayerThickness / (2. * moistureContentDough0);
		final double thetaS = 1. / (heatTransferCoeff + 2. * thermalConductivityCheese / cheeseLayerThickness)
			* (heatTransferCoeff + 2. * thermalConductivityCheese * getTheta(9, y) / cheeseLayerThickness
			- 2. * moistureDiffusivityCheese * densityCheese * vaporizationLatentHeat * moistureContentDough0
			/ (cheeseLayerThickness * (bakingTemperatureTop - ambientTemperature)) * (getC(9, y) - moistureContentSurface));
		final double thetaB = (bakingTemperatureBottom - ambientTemperature) / (bakingTemperatureTop - ambientTemperature);

		//node 9, cheese layer
		calculateTopLayer(9, y, dydt, thetaS, moistureContentSurface);

		calculateTomatoCheeseInterfaceLayer(8, y, dydt);

		//node 7, tomato paste layer
		calculateInnerTomatoLayer(7, y, dydt);

		calculateDoughTomatoInterfaceLayer(6, y, dydt);

		//node 5, surface of the dough layer
		calculateDoughLayer(5, y, dydt);

		//node 4 to 2, dough
		calculateInnerDoughLayer(4, y, dydt);
		calculateInnerDoughLayer(3, y, dydt);
		calculateInnerDoughLayer(2, y, dydt);

		//TODO add contact layer between dough and sheet
		//TODO add contact layer between sheet and pan
		//TODO consider layer 1 as convection and irradiation, not only convection

		//node 1, dough in contact with heated tray
		calculateBottomLayer(1, y, dydt, thetaB);
	}

	private void calculateTopLayer(final int layer, final double[] y, final double[] dydt, final double thetaS, final double CS){
		final double tmp = 4. / (cheeseLayerThickness * cheeseLayerThickness);
		setTheta(layer, dydt, tmp * thermalDiffusivityCheese * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + thetaS));
		setC(layer, dydt, tmp * moistureDiffusivityCheese * (getC(layer - 1, y) - 2. * getC(layer, y) + CS));
	}

	private void calculateTomatoCheeseInterfaceLayer(final int layer, final double[] y, final double[] dydt){
		setTheta(layer, dydt, 4. / (densityTomato * specificHeatTomato * tomatoLayerThickness
			+ densityCheese * specificHeatCheese * cheeseLayerThickness)
			* (thermalConductivityTomato / tomatoLayerThickness * (getTheta(layer - 1, y) - getTheta(layer, y))
			- thermalConductivityCheese / cheeseLayerThickness * (getTheta(layer, y) - getTheta(layer + 1, y))));
		setC(layer, dydt, 4. / (tomatoLayerThickness + cheeseLayerThickness)
			* (moistureDiffusivityTomato / tomatoLayerThickness * (getC(layer - 1, y) - getC(layer, y))
			- moistureDiffusivityCheese / cheeseLayerThickness * (getC(layer, y) - getC(layer + 1, y))));
	}

	private void calculateInnerTomatoLayer(final int layer, final double[] y, final double[] dydt){
		final double tmp = 4. / (tomatoLayerThickness * tomatoLayerThickness);
		setTheta(layer, dydt, tmp * thermalDiffusivityTomato
			* (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)));
		setC(layer, dydt, tmp * moistureDiffusivityTomato
			* (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)));
	}

	private void calculateDoughTomatoInterfaceLayer(final int layer, final double[] y, final double[] dydt){
		setTheta(layer, dydt, 20. / (densityDough * specificHeatDough * doughLayerThickness
			+ 5. * densityTomato * specificHeatTomato * tomatoLayerThickness)
			* (5. * thermalConductivityDough / doughLayerThickness * (getTheta(layer - 1, y) - getTheta(layer, y))
			- thermalConductivityTomato / tomatoLayerThickness * (getTheta(layer, y) - getTheta(layer + 1, y))));
		setC(layer, dydt, 20. / (doughLayerThickness + 5. * tomatoLayerThickness)
			* (5. * moistureDiffusivityDough / doughLayerThickness * (getC(layer - 1, y) - getC(layer, y))
			- moistureDiffusivityTomato / tomatoLayerThickness * (getC(layer, y) - getC(layer + 1, y))));
	}

	private void calculateDoughLayer(final int layer, final double[] y, final double[] dydt){
		final double tmp = 100. / (3. * doughLayerThickness * doughLayerThickness);
		setTheta(layer, dydt, tmp * thermalDiffusivityDough
			* (getTheta(layer - 1, y) - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)));
		setC(layer, dydt, tmp * moistureDiffusivityDough
			* (getC(layer - 1, y) - 3. * getC(layer, y) + 2. * getC(layer + 1, y)));
	}

	private void calculateInnerDoughLayer(final int layer, final double[] y, final double[] dydt){
		final double tmp = 25. / (doughLayerThickness * doughLayerThickness);
		setTheta(layer, dydt, tmp * thermalDiffusivityDough
			* (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)));
		setC(layer, dydt, tmp * moistureDiffusivityDough
			* (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)));
	}

	private void calculateBottomLayer(final int layer, final double[] y, final double[] dydt, final double thetaB){
		final double tmp = 50. / (doughLayerThickness * doughLayerThickness);
		setTheta(layer, dydt, (2./3.) * tmp * thermalDiffusivityDough
			* (thetaB - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)));
		setC(layer, dydt, tmp * moistureDiffusivityDough * (getC(layer + 1, y) - getC(layer, y)));
	}

	private double getTheta(final int layer, final double[] array){
		return array[(9 - layer) * 2];
	}

	private void setTheta(final int layer, final double[] array, final double value){
		array[(9 - layer) * 2] = value;
	}

	private double getC(final int layer, final double[] array){
		return array[(9 - layer) * 2 + 1];
	}

	private void setC(final int layer, final double[] array, final double value){
		array[(9 - layer) * 2 + 1] = value;
	}

}
