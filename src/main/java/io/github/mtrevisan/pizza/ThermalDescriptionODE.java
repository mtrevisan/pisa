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

import io.github.mtrevisan.pizza.bakingpans.BakingPanAbstract;
import io.github.mtrevisan.pizza.utils.Helper;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * @see <a href="https://www.tandfonline.com/doi/pdf/10.1081/JFP-120015599">Dumas, Mittal. Heat and mass transfer properties of pizza during baking. 2007.</a>
 * @see <a href="https://en.wikipedia.org/wiki/Latent_heat">Latent heat</a>
 * @see <a href="https://en.wikipedia.org/wiki/Thermal_diffusivity">Thermal diffusivity</a>
 *
 * https://www.engineeringtoolbox.com/conductive-heat-transfer-d_428.html
 * https://www.researchgate.net/publication/280735585_One-Dimensional_Solar_Heat_Load_Simulation_Model_for_a_Parked_Car
 *
 * https://www.witpress.com/Secure/elibrary/papers/9781853129322/9781853129322008FU1.pdf
 *
 * https://math.okstate.edu/people/binegar/4233/4233-l12.pdf
 * http://www.physics.emory.edu/faculty/brody/Advanced%20Lab/phys%20222%20lecture%20notes.pdf
 * http://dma.dima.uniroma1.it/users/lsa_adn/MATERIALE/FDheat.pdf
 *
 * FIXME thermal conductivity depends on temperature!
 * https://cecs.wright.edu/~sthomas/htchapter02.pdf
 *
 * https://flothesof.github.io/heat-equation-cook-my-meat.html
 * https://arxiv.org/pdf/1109.0664.pdf
 *
 * https://arxiv.org/ftp/arxiv/papers/1806/1806.08790.pdf
 * https://apps.dtic.mil/dtic/tr/fulltext/u2/a243491.pdf
 * https://www.researchgate.net/publication/275153085_Heat_and_Mass_Balance_for_Baking_Process
 */
public class ThermalDescriptionODE implements FirstOrderDifferentialEquations{

	//Stefan-Boltzmann constant [W / (m² · K⁴)]
	private static final double SIGMA = 5.670374419e-8;

	/**  [m] */
	private final double layerThicknessMozzarella;
	/**  [m] */
	private final double layerThicknessTomato;
	/**  [m] */
	private final double layerThicknessDough;

	private final OvenType ovenType;

	/** [°C] */
	private final double bakingTemperatureTop;
	/** [m] */
	private final double distanceHeaterTop;
	/** [°C] */
	private final double bakingTemperatureBottom;
	/** [m] */
	private final double distanceHeaterBottom;
	private final double ambientTemperature;

	//ambient humidity ratio
	private final double humidityRatioAmbient;
	//surface humidity ratio
	private final double humidityRatioSurface;

	private final double heatTransferCoefficient;

	/** [kg / m³] */
	private final double densityMozzarella = 1140.;
	/** [kg / m³] */
	private final double densityTomato = 1073.;
	/** [kg / m³] */
	private final double densityDough = 862.;
	/** [J / (kg · K)] */
	private final double specificHeatMozzarella = 2864.;
	/** [J / (kg · K)] */
	private final double specificHeatTomato = 2930.;
	/** [J / (kg · K)] */
	private final double specificHeatDough = 3770.;
	/** Initial moisture content (0.47 to 0.55) [%] */
	private final double moistureContentMozzarella0 = 0.4435;
	/** Initial moisture content [%] */
	private final double moistureContentTomato0 = 0.91489;
	/** Initial moisture content [%] */
	private final double moistureContentDough0 = 0.65;

	private final BakingPanAbstract bakingPan;

	private final BiFunction<Double, Double, Double> thermalConductivityMozzarella;
	private final BiFunction<Double, Double, Double> thermalConductivityTomato;
	private final BiFunction<Double, Double, Double> thermalConductivityDough;
	private final BiFunction<Double, Double, Double> physicalDensityDough;


/*
https://www.sciencedirect.com/topics/engineering/convection-heat-transfer
https://ocw.mit.edu/courses/aeronautics-and-astronautics/16-050-thermal-energy-fall-2002/lecture-notes/10_part3.pdf
https://www.cantorsparadise.com/the-heat-equation-a76d7773a0b5
https://www.sfu.ca/~mbahrami/ENSC%20388/Notes/

general formula:
DQ = (Ts - Tinf) / R	[W]
where
conduction:
R = L / (k · A)	[K / W]
k = thermal conductivity
convection:
R = 1 / (h · A)	[K / W]
h = convective heat transfer coefficient
radiation:
R = 1 / (eps · sigma · (Ts² + Tinf²) · (Ts + Tinf) · A)	[K / W]
sigma = 5.67e-8 [W / (m² · K⁴)] Stefan-Boltzmann constant
eps = emissivity

heat must be constant, so Qin = Qout
but the pizza must be heated, so heat must be absorbed by the pizza, so the heat is entering the pizza (exiting by moisture evaporation: about 20%)

---

if one slab of constant material has Toven on both sides:
(T(x) - Toven) / (α · L² / k) = (x / L - x² / L²) / 2

---

heat equation:
dT/dt = k / (ρ · c) · d²T/dx² = α · d²T/dx²
where
k	thermal conductivity
ρ	density
c	specific heat capacity
α	diffusivity

one solution is (where T(0, t) = T(L, t) = 0)
T(x, t) = sum(n=1 to inf, An · sin(n · pi · x / L) · e^(-k · n² · pi² / (ρ · c* L²)))
where the coefficients An are chosen such that it satisfies the initial conditions:
T(x, 0) = sum(n=1 to inf, An · sin(n · pi · x / L))
that is a Fourier sine series expansion with An = (2 / L) · int(0 to L, T(x, 0) · sin(n · pi · x / L), dx)
*/

/*
conduction:
dq/dt = k / x · A · (T2 - T1) = k · A · dT/dx
where
q	heat transferred
k	thermal conductivity
x	distance between T1 and T2
A	cross sectional area
T2	higher temperature
T1	lower temperature

convection:
q = A · h · (T2 - T1)
where
h	convective heat transfer coefficient
A	cross sectional area
T2	higher temperature
T1	lower temperature

radiation:
q = A1 · Css · e · (T2⁴ - T1⁴)
where
A1	area exposed to the radiation
Css	Stefan-Boltzmann constant
e	pizza emissivity
T2	higher temperature
T1	lower temperature

heat balance:
qk_in + qh_in + qs_in + qr_in = qk_out + qs_out + q_ret

where

qk_in = k/x · (t_T_n-1 - t_T_n)	heat moving in by conduction
qh_in = h · (Tair - t_T_n)	heat moving in by air convection
qs_in = ms_n-1 · Hf	heat moving in by steam convection
qr_in = Css · e · (Toven⁴ - t_T_n⁴)	heat moving in by radiation

qk_out = k/x · (t_T_n - t_T_n+1)	heat moving out by conduction
qs_out = ms_n · Hf	heat moving out by steam conduction and diffusion

q_ret = ρ · cp · x · (t+1_T_n - t_T_n) + ms_ret	heat retained

//system pizza + (pan + (baking sheet))
q_in_top = (airThermalConductivity / roofDistance + airConvectiveHeatTransfer) · (TT - TpT(t))
	+ Css · e · (TT⁴ - TpT(t)⁴)
	+ steamMassIn · steamConvectiveHeatTransfer
q_in_tomato = (mozzarellaThermalConductivity / mozzarellaThickness) · (TpT(t) - TcT(t)) + steamMassIn · steamConvectiveHeatTransfer
q_in_dough = (doughThermalConductivity / doughThickness) · (TcT(t) - TdT(t)) + steamMassIn · steamConvectiveHeatTransfer
q_in_bottom = (airThermalConductivity / floorDistance + airConvectiveHeatTransfer) · (TB - TpB(t)) + Css · e · (TB⁴ - TpB(t)⁴)
qk_out = airThermalConductivity / x · (t_T_n - t_T_n+1)
q_out_top = steamMassOut · steamConvectiveHeatTransfer
q_ret = ρ · cp · x · (t+1_T_n - t_T_n) + ms_ret

k	pizza conductivity
x	pizza thickness

t_T_n-1	temperature at time t of the previous node
t_T_n	temperature at time t of the node
t_T_n+1	temperature at time t of the next node
t+1_T_n	temperature of the next node after the next segment of baking time has passed
Toven	oven temperature
h	convective-heat heat transfer coefficient
ms_n-1	mass of steam from the previous node that condenses giving its heat to node n
ms_n	mass of steam from node n that condenses giving its heat to next node
ms_ret	mass of steam retained
Hf	heat of vaporization
ρ	dough density
cp	dough specific heat
*/

	ThermalDescriptionODE(final double layerThicknessMozzarella, final double layerThicknessTomato, final double layerThicknessDough,
			final OvenType ovenType, final double bakingTemperatureTop, final double distanceHeaterTop, final double bakingTemperatureBottom, final double distanceHeaterBottom,
			final double ambientTemperature, final double airPressure, final double airRelativeHumidity, final BakingPanAbstract bakingPan){
		this.layerThicknessMozzarella = layerThicknessMozzarella;
		this.layerThicknessTomato = layerThicknessTomato;
		//TODO consider expansion during baking due to Charles-Gay Lussac law
		this.layerThicknessDough = layerThicknessDough;

		this.ovenType = ovenType;

		this.bakingTemperatureTop = bakingTemperatureTop;
		this.distanceHeaterTop = distanceHeaterTop;
		this.bakingTemperatureBottom = bakingTemperatureBottom;
		this.distanceHeaterBottom = distanceHeaterBottom;
		this.ambientTemperature = ambientTemperature;

		this.humidityRatioAmbient = airRelativeHumidity;

		//heat transfer coefficient:
		heatTransferCoefficient = ovenType.heatTransferCoefficient(bakingTemperatureTop);
		humidityRatioSurface = 0.1837 + (-0.0014607 + 0.000004477 * bakingTemperatureTop) * bakingTemperatureTop;


		final double thermalConductivityAir = OvenType.calculateAirThermalConductivity(ambientTemperature);
		final double specificHeatAir = OvenType.calculateAirSpecificHeat(ambientTemperature);
		final double densityAir = OvenType.calculateAirDensity(ambientTemperature, airPressure, airRelativeHumidity);
		final double thermalDiffusivityAir = calculateThermalDiffusivity(thermalConductivityAir, specificHeatAir, densityAir);

		this.bakingPan = bakingPan;

		thermalConductivityMozzarella = (temperature, water)
			-> calculateThermalConductivity(temperature, 0.2651, 0.2386, 0.0196, 0., 0.0332, water);
		thermalConductivityTomato = (temperature, water)
			-> calculateThermalConductivity(temperature, 0.013, 0.002, 0.07, 0., 0.00011, water);
		thermalConductivityDough = (temperature, water)
			-> calculateThermalConductivity(temperature, 0.013, 0.011, 0.708, 0.019, 0.05, water);

		physicalDensityDough = (temperature, water)
			-> doughDensity(temperature, 0.13, 0.011, 0.013, 0.019, 0.05, water);

		//TODO
		//[m]
		final double minLayerThickness = 0.0001;
	}

	private double massTransferSurface(final double temperature){
		return (ovenType == OvenType.FORCED_CONVECTION?
			4.6332 * Math.exp(-277.5 / temperature):
			4.5721 * Math.exp(-292.8 / temperature));
	}

	/**
	 * @param fourierTemperature	Temperature [°C].
	 * @return	Moisture diffusivity in mozzarella layer [m² / s].
	 */
	private double moistureDiffusivityMozzarella(final double fourierTemperature){
		return 7.e-11;
	}

	/**
	 * @param fourierTemperature	Fourier temperature.
	 * @return	Moisture diffusivity in tomato layer [m² / s].
	 */
	private double moistureDiffusivityTomato(final double fourierTemperature){
		final double temperature = calculateInverseFourierTemperature(fourierTemperature, bakingTemperatureTop, ambientTemperature);

		//https://www.researchgate.net/publication/50863959_Effective_Moisture_Diffusivity_and_Activation_Energy_of_Tomato_in_Thin_Layer_Dryer_during_Hot_Air_Drying
		return (ovenType == OvenType.FORCED_CONVECTION?
			9.9646e-10 * Math.exp(-605.93 / temperature):
			1.7738e-10 * Math.exp(-1212.71 / temperature));
	}

	/**
	 * @param fourierTemperature	Temperature [°C].
	 * @return	Moisture diffusivity in dough layer [m² / s].
	 */
	private double moistureDiffusivityDough(final double fourierTemperature){
		final double temperature = calculateInverseFourierTemperature(fourierTemperature, bakingTemperatureTop, ambientTemperature);

		return (ovenType == OvenType.FORCED_CONVECTION?
			7.0582e-8 * Math.exp(-1890.68 / temperature):
			1.4596e-9 * Math.exp(-420.34 / temperature));
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @param protein	Protein content [%].
	 * @param fat	Fat content [%].
	 * @param carbohydrate	Carbohydrate content [%].
	 * @param fiber	Fiber content [%].
	 * @param ash	Ash content [%].
	 * @param water	Water content [%].
	 * @return	Thermal conductivity [W / (m · K)].
	 */
	private double calculateThermalConductivity(final double temperature, final double protein, final double fat, final double carbohydrate,
			final double fiber, final double ash, final double water){
		final double proteinFactor = 0.17881 + (0.0011958 - 2.7178e-6 * temperature) * temperature;
		final double fatFactor = 0.18071 + (-2.7604e-4 - 1.7749e-7 * temperature) * temperature;
		final double carbohydrateFactor = 0.20141 + (0.0013874 - 4.3312e-6 * temperature) * temperature;
		final double fiberFactor = 0.18331 + (0.0012497 - 3.1683e-6 * temperature) * temperature;
		final double ashFactor = 0.32962 + (0.0014011 - 2.9069e-6 * temperature) * temperature;
		final double waterFactor = 0.57109 + (0.0017625 - 6.7036e-6 * temperature) * temperature;
		return proteinFactor * protein
			+ fatFactor * fat
			+ carbohydrateFactor * carbohydrate
			+ fiberFactor * fiber
			+ ashFactor * ash
			+ waterFactor * water;
	}

	/**
	 *
	 * @param thermalConductivity	Thermal conductivity [W / (m · K)].
	 * @param specificHeat	Specific heat [J / (kg · K)].
	 * @param density	Density [kg / m³].
	 * @return The thermal diffusivity [m² / s].
	 */
	private double calculateThermalDiffusivity(final double thermalConductivity, final double specificHeat, final double density){
		return thermalConductivity / (specificHeat * density);
	}

	@Override
	public final int getDimension(){
		return 18;
	}

	public final double[] getInitialState(){
		//array of initial temperature (as Fourier temperature) and moisture content
		//by column
		return new double[]{
			//node 1, dough in contact with heated tray
			calculateFourierTemperature(bakingTemperatureBottom, bakingTemperatureTop, ambientTemperature), 1.,
			//node 2, dough
			0., 1.,
			//node 3, dough
			0., 1.,
			//node 4, dough
			0., 1.,
			//node 5, surface of the dough layer
			0., (moistureContentDough0 + moistureContentTomato0) / (2. * moistureContentDough0),
			0., moistureContentTomato0 / moistureContentDough0,
			//node 7, tomato paste layer
			0., (moistureContentTomato0 + moistureContentMozzarella0) / (2. * moistureContentDough0),
			0., moistureContentMozzarella0 / moistureContentDough0,
			//node 9, mozzarella layer
			0., humidityRatioAmbient
		};
	}

/*
@see <a href="https://www.ndt.net/article/apcndt2006/papers/38.pdf">Chiang, Pan, Liaw, Chi, Chu. Modeling of heat transfer in a multi-layered system for infrared inspection of a building wall. 2006.</a>
@see <a href="https://www.cpp.edu/~lllee/TK3111heat.pdf">Nguyen. Transport IID - Lecture notes. 2014.</a>
math symbols: https://www.compart.com/en/unicode/

Heat transfer (unsteady state conduction):
∂Q/dτ + ∇ · V = 0
where
Q is the heat, ρ · Cp · T [J]
ρ is the density [kg / m³]
Cp is the specific heat capacity [J / (kg · K)]
T is the temperature [K]
V is the vector field giving the heat flow, (-k · ∂T/∂x) · x
k is the thermal conductivity [W / (m · K)]
x is the unit vector

substituting is
ρ · Cp · ∂T/dτ - k · ∇ · (∂T/∂x) · x = 0
that is
dT/dτ = α · d²T/dx²
where (at constant pressure)
α is the thermal diffusivity, k / (ρ · Cp) [m² / s]

The general solution is
T(x, t) = sum(n=1 to inf, A_n · sin(n · π · x / L) · e^(-k · n² · π² · t / (ρ · Cp · L²)))
where
A_n = (2 / L) · int(x=0 to L, T(x, 0) · sin(n · π · x / L) · dx)

Discretized is
(1) (T[m](t+1) - T[m](t)) / dτ = α · (T[m-1](t) - 2 · T[m](t) + T[m+1](t)) / Δx²
where
T[m](t) is the temperature at node m and time t

The temperature variation at the boundary of layer is calculated using the relation
(2) k · (T[m-1](t) - T[m](t)) / Δx + σ · ε · (T∞⁴ - T[m](t)⁴) + h · (T∞ - T[m](t)) = ρ · Cp · (Δx / 2) · (T[m](t+1) - T[m](t)) / dτ
where
the first term is the Fourier's law for the conduction
the second term is the radiation
the third term is the Newton's law for the convection
T∞ is the ambient temperature [K]
σ is the Stephan-Boltzmann constant [W / (m² · K⁴)]
ε is the thermal emissivity
h is the convection coefficient/heat transfer coefficient [W / (m² · K)]

The temperature variation across the internal layers A and B is calculated using the relation
(3) kA · (T[m-1](t) - T[m](t)) / ΔxA + kB · (T[m+1](t) - T[m](t)) / ΔxB = (ρA · CpA · ΔxA + ρB · CpB · ΔxB) / 4 · (T[m](t+1) - T[m](t)) / dτ

let
θ = (T - T∞) / (Ti - T∞)
𝜓 = x / L
then
(1') dθ[m]/dτ = α · (θ[m-1] - 2 · θ[m] + θ[m+1]) / d𝜓²
(2') dθ[m]/dτ = 2 · (k · (θ[m-1] - θ[m]) / d𝜓 + σ · ε · (T∞⁴ - θ[m]⁴) + h · (T∞ - θ[m])) / (ρ · Cp · d𝜓)
(3') dθ[m]/dτ = 4 · (kA · (θ[m-1] - θ[m]) / LA + kB · (θ[m+1] - θ[m]) / LB) / (ρA · CpA · LA + ρB · CpB · LB)

The layers are:
- air (convection + radiation top)
- mozzarella
- tomato
- dough
- (baking parchment paper)
- tray
- air (convection + radiation bottom)
*/

/*
moisture transfer:
dm/dt = Dm · d²m/dt²

at the surface:
hr · (Ta - TS) = Kc · dT/dx|x=S + Dm_cS · ρ_c · Lv · dm/dx|x=S
where hr is the heat transfer coefficient [W / (m² · K)]
where K is the surface mass transfer coefficient [kg H₂O / (m² · s)]
where Dm is the moisture diffusivity [m² / s]
where ρ is the density [kg / m³]
where Lv is the latent heat of vaporization [J / kg]

heat transfer at the interface between the dough and the tomato layer:
Kd · dT/dx|x=5-6 - Kt · dT/dx|x=6-7 = dT6/dt · (ρ_d · cp_d · Δx5-6 + ρ_t · cp_t · Δx6-7) / 2

heat transfer at the interface between the tomato and the mozzarella layer:
Kt · dT/dx|x=7-8 - Kc · dT/dx|x=8-9 = dT8/dt · (ρ_t · cp_t · Δx7-8 + ρ_c · cp_c · Δx8-9) / 2

moisture transfer at the top surface:
Dm_cS · ρ_c · dm/dx|x=S = Km_c · (Hs - Ha)
where Hs is the pizza surface humidity ratio [kg H₂O / kg dry air]
where Ha is the air humidity ratio [kg H₂O / kg dry air]

moisture transfer at the interface between the tomato and the mozzarella layer:
Dm_tc · dm/dx|x=7-8 - Dm_cS · dm/dx|x=8-9 = dm8/dt · (Δx7-8 + Δx8-9) / 2

moisture transfer at the interface between the dough and the tomato paste:
Dm_dt · dm/dx|x=5-6 - Dm_tc · dm/dx|x=6-7 = dm6/dt · (Δx5-6 + Δx6-7) / 2


let:
C = m / mp0
θ = (T - T0) / (Ta - T0)
𝜓 = x / L
L = Ld + Lt + Lc

moisture transfer becomes:
dC/dt = Dm / L² · d²C/d𝜓²
heat transfer becomes:
dθ/dt = α / L² · d²θ/d𝜓²
at the surface becomes:
hr · (1 - θS) = Kc / L · dθS/d𝜓 + Dm_cS · ρ_c · Lv · md0 · / (L · (Ta - T0)) · dCS/d𝜓

boundary conditions, θ(𝜓, t) and C(𝜓, t):
θ(𝜓, 0) = 0
C(0 < 𝜓 < Ld / L, 0) = 1
C(Ld / L, 0) = (md0 + mt0) / (2 · md0)
C(Ld / L < 𝜓 < (Lt + Ld) / L, 0) = mt0 / md0
C((Ld + Ld) / L, 0) = (mt0 + mc0) / md0
C((Ld + Lt) / L < 𝜓 < 1, 0) = mc0 / md0
θB = (Tb - T0) / (Ta - T0)

at the surface of mozzarella layer:
Dm_cS · ρ_c / L · dCS/d𝜓 · mp0 = Km_c · (HS - Ha)

at the interface node 8 (tomato-mozzarella):
Kt / L · dθ/d𝜓|7-8 - Kc / L · dθ/d𝜓|8-9 = dθ8/dt · (ρ_t · cp_t · Δx_7-8 + ρ_c · cp_c · Δx_8-9) / 2
Dm_tc / L · dC/d𝜓|7-8 - Dm_cS / L · dC/d𝜓|8-9 = dC8/dt · (Δx_7-8 + Δx_8-9) / 2

at the interface node 6 (dough-tomato):
Kd / L · dθ/d𝜓|5-6 - Kt / L · dθ/d𝜓|6-7 = dθ6/dt · (ρ_d · cp_d · Δx_5-6 + ρ_t · cp_t · Δx_6-7) / 2
Dm_dt / L · dC/d𝜓|5-6 - Dm_tc / L · dC/d𝜓|6-7 = dC6/dt · (Δx_5-6 + Δx_6-7) / 2

at the bottom:
dC/d𝜓|𝜓=0 = 0


9, mozzarella-surface layer (central difference approximation of the second derivative):
dθ9/dt = 4 · α_c / Lc² · (θ8 - 2 · θ9 + θS)
dC9/dt = 4 · Dm_c / Lc² · (C8 - 2 · C9 + CS)

7, tomato-mozzarella layer:
dθ7/dt = 4 · α_t / Lt² · (θ6 - 2 · θ7 + θ8)
dC7/dt = 4 · Dm_t / Lt² · (C6 - 2 · C7 + C8)

5, dough-tomato layer:
dθ5/dt = 100 · α_d / (3 · Ld²) · (θ4 - 3 · θ5 + 2 · θ6)
dC5/dt = 100 · Dm_d / (3 · Ld²) · (C4 - 3 · C5 + 2 · C6)

4-2, dough layer:
dθ_i/dt = 25 · α_d / Ld² · (θ_i-1 - 2 · θ_i + θ_i+1)
dC_i/dt = 25 · Dm_d / Ld² · (C_i-1 - 2 · C_i + C_i+1)

1, bottom layer:
dθ1/dt = 100 · α_d / (3 · Ld²) · (θB - 3 · θ1 + θ2)
*/

	//y is a list of θ and C from layer 9 to layer 1
	//dydt is a list of dθ/dt and dC/dt from layer 9 to layer 1
	@Override
	public final void computeDerivatives(final double t, final double[] y, final double[] dydt) throws MaxCountExceededException,
			DimensionMismatchException{
		//node 9, mozzarella layer
		calculateTopLayer(8, y, dydt);

		calculateTomatoMozzarellaInterfaceLayer(7, y, dydt);

		//node 7, tomato paste layer
		calculateInnerTomatoLayer(6, y, dydt);

		calculateDoughTomatoInterfaceLayer(5, y, dydt);

		//node 5, surface of the dough layer
//		calculateDoughLayer(4, y, dydt);

		//node 4 to 2, dough
		calculateInnerDoughLayer(4, y, dydt, 4.);
		calculateInnerDoughLayer(3, y, dydt, 4.);
		calculateInnerDoughLayer(2, y, dydt, 4.);
		calculateInnerDoughLayer(1, y, dydt, 4.);

		//TODO add contact layer between dough and baking parchment paper
		//TODO add contact layer between baking parchment paper and pan
		//TODO consider layer 1 as convection and irradiation, not only convection

		//node 1, dough in contact with heated tray
		calculateBottomLayer(0, y, dydt);
	}

	//FIXME recipe.density(densityFat, dough.ingredientsTemperature, dough.atmosphericPressure)
	private double doughDensity(final double fourierTemperature, final double protein, final double fat, final double carbohydrate,
			final double fiber, final double ash, final double water){
		final double temperature = calculateInverseFourierTemperature(fourierTemperature, bakingTemperatureTop, ambientTemperature);

		final double proteinFactor = 1329.9 - 0.5184 * temperature;
		final double fatFactor = 925.59 - 0.41757 * temperature;
		final double carbohydrateFactor = 1599.1 - 0.31046 * temperature;
		final double fiberFactor = 1311.5 - 0.36589 * temperature;
		final double ashFactor = 2423.8 - 0.28063 * temperature;
		final double waterFactor = 997.18 + (0.0031439 - 0.0037575 * temperature) * temperature;
		return proteinFactor * protein
			+ fatFactor * fat
			+ carbohydrateFactor * carbohydrate
			+ fiberFactor * fiber
			+ ashFactor * ash
			+ waterFactor * water;
	}

	private void calculateTopLayer(final int layer, final double[] y, final double[] dydt){
		//FIXME if distanceHeaterTop is zero, there is heating anyway if forced convection air oven is used and distanceHeaterBottom is non zero
//		final double thetaS = calculateFourierTemperature((distanceHeaterTop > 0.? bakingTemperatureTop: ambientTemperature),
//			ambientTemperature, bakingTemperatureTop);

		//at pizza surface

		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y), bakingTemperatureTop, ambientTemperature);
		//FIXME calculate actual moistureContentMozzarella
		final double conductivityMozzarella = thermalConductivityMozzarella.apply(layerTemperature, moistureContentMozzarella0);
		final double thermalDiffusivityMozzarella = calculateThermalDiffusivity(conductivityMozzarella, specificHeatMozzarella, densityMozzarella);
		final double vaporizationLatentHeatWater = calculateVaporizationLatentHeatWater(layerTemperature);

		//surface mass transfer coefficient [kg H₂O / (m² · s)]
		final double massTransferSurface = massTransferSurface(layerTemperature);
		final double moistureDiffusivityMozzarella = moistureDiffusivityMozzarella(getTheta(layer, y));
		final double moistureContentSurface = getC(layer, y) - massTransferSurface / (moistureDiffusivityMozzarella * densityMozzarella)
			* (humidityRatioSurface - humidityRatioAmbient) * layerThicknessMozzarella / (2. * moistureContentDough0);
		final double thetaS = 1. / (heatTransferCoefficient + 2. * conductivityMozzarella / layerThicknessMozzarella)
			* (heatTransferCoefficient + 2. * conductivityMozzarella * getTheta(layer, y) / layerThicknessMozzarella
			- 2. * moistureDiffusivityMozzarella * densityMozzarella * vaporizationLatentHeatWater * moistureContentDough0
			/ (layerThicknessMozzarella * (bakingTemperatureTop - ambientTemperature)) * (getC(layer, y) - moistureContentSurface));

		final double tmp = 4. / Math.pow(layerThicknessMozzarella, 2.);
		setTheta(layer, dydt, tmp * thermalDiffusivityMozzarella * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + thetaS));

		setC(layer, dydt, tmp * moistureDiffusivityMozzarella * (getC(layer - 1, y) - 2. * getC(layer, y)
			+ moistureContentSurface));
	}

	private static final double[] WATER_VAPORIZATION_LATENT_HEAT_LOW_COEFFICIENTS = {2500.9, -2.36719, 1.246e-4, -5.17e-6, -5.e-8, 1.45e-10, -2.7e-13};
	private static final double[] WATER_VAPORIZATION_LATENT_HEAT_HIGH_COEFFICIENTS = {-16456273.5, 322865.7917, -2632.707957, 11.42226714, -0.02781080181, 3.6031127e-5, -1.94069959e-8};

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Vaporization latent heat of water, Lv [J / kg].
	 */
	private double calculateVaporizationLatentHeatWater(final double temperature){
		return 100. * (temperature <= 260.?
			Helper.evaluatePolynomial(WATER_VAPORIZATION_LATENT_HEAT_LOW_COEFFICIENTS, temperature):
			Helper.evaluatePolynomial(WATER_VAPORIZATION_LATENT_HEAT_HIGH_COEFFICIENTS, temperature));
	}

	private void calculateTomatoMozzarellaInterfaceLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y), bakingTemperatureTop, ambientTemperature);
		//FIXME calculate actual moistureContentMozzarella
		final double conductivityMozzarella = thermalConductivityMozzarella.apply(layerTemperature, moistureContentMozzarella0);
		//FIXME calculate actual moistureContentTomato
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, moistureContentTomato0);

		setTheta(layer, dydt, 4. / (densityTomato * specificHeatTomato * layerThicknessTomato
			+ densityMozzarella * specificHeatMozzarella * layerThicknessMozzarella)
			* (conductivityTomato * (getTheta(layer - 1, y) - getTheta(layer, y)) / layerThicknessTomato
			- conductivityMozzarella * (getTheta(layer, y) - getTheta(layer + 1, y)) / layerThicknessMozzarella));

		final double moistureDiffusivityTomato = moistureDiffusivityTomato(getTheta(layer, y));
		final double moistureDiffusivityMozzarella = moistureDiffusivityMozzarella(getTheta(layer, y));
		setC(layer, dydt, 4. / (layerThicknessTomato + layerThicknessMozzarella)
			* (moistureDiffusivityTomato * (getC(layer - 1, y) - getC(layer, y)) / layerThicknessTomato
			- moistureDiffusivityMozzarella * (getC(layer, y) - getC(layer + 1, y)) / layerThicknessMozzarella));
	}

	private void calculateInnerTomatoLayer(final int layer, final double[] y, final double[] dydt){
//		setTheta(layer, dydt, 4. * thermalDiffusivityTomato
//			* (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)) / Math.pow(layerThicknessTomato, 2.));
//
//		final double moistureDiffusivityTomato = moistureDiffusivityTomato(getTheta(layer, y));
//		setC(layer, dydt, 4. * moistureDiffusivityTomato
//			* (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)) / Math.pow(layerThicknessTomato, 2.));

		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y), bakingTemperatureTop, ambientTemperature);
		//FIXME calculate actual moistureContentTomato
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, moistureContentTomato0);
		final double thermalDiffusivityTomato = calculateThermalDiffusivity(conductivityTomato, specificHeatTomato, densityTomato);
		setTheta(layer, dydt, thermalDiffusivityTomato
			* (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)) / Math.pow(layerThicknessTomato, 2.));

		final double moistureDiffusivityTomato = moistureDiffusivityTomato(getTheta(layer, y));
		setC(layer, dydt, moistureDiffusivityTomato
			* (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)) / Math.pow(layerThicknessTomato, 2.));
	}

	private void calculateDoughTomatoInterfaceLayer(final int layer, final double[] y, final double[] dydt){
//		setTheta(layer, dydt, 20. / (densityDough * specificHeatDough * layerThicknessDough
//			+ 5. * densityTomato * specificHeatTomato * layerThicknessTomato)
//			* (5. * thermalConductivityDough * (getTheta(layer - 1, y) - getTheta(layer, y)) / layerThicknessDough
//			- thermalConductivityTomato * (getTheta(layer, y) - getTheta(layer + 1, y)) / layerThicknessTomato));
//
//		final double moistureDiffusivityTomato = moistureDiffusivityTomato(getTheta(layer, y));
//		final double moistureDiffusivityDough = moistureDiffusivityDough(getTheta(layer, y));
//		setC(layer, dydt, 20. / (layerThicknessDough + 5. * layerThicknessTomato)
//			* (5. * moistureDiffusivityDough / layerThicknessDough * (getC(layer - 1, y) - getC(layer, y))
//			- moistureDiffusivityTomato / layerThicknessTomato * (getC(layer, y) - getC(layer + 1, y))));

		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y), bakingTemperatureTop, ambientTemperature);
		//FIXME calculate actual moistureContentTomato
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, moistureContentTomato0);
		//FIXME calculate actual moistureContentDough
		final double conductivityDough = thermalConductivityDough.apply(layerTemperature, moistureContentDough0);

		setTheta(layer, dydt, 4. / (densityDough * specificHeatDough * layerThicknessDough
			+ densityTomato * specificHeatTomato * layerThicknessTomato)
			* (conductivityDough * (getTheta(layer - 1, y) - getTheta(layer, y)) / layerThicknessDough
			- conductivityTomato * (getTheta(layer, y) - getTheta(layer + 1, y)) / layerThicknessTomato));

		final double moistureDiffusivityTomato = moistureDiffusivityTomato(getTheta(layer, y));
		final double moistureDiffusivityDough = moistureDiffusivityDough(getTheta(layer, y));
		setC(layer, dydt, 4. / (layerThicknessDough + layerThicknessTomato)
			* (moistureDiffusivityDough * (getC(layer - 1, y) - getC(layer, y)) / layerThicknessDough
			- moistureDiffusivityTomato * (getC(layer, y) - getC(layer + 1, y)) / layerThicknessTomato));
	}

	private void calculateDoughLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y), bakingTemperatureTop, ambientTemperature);
		//FIXME calculate actual moistureContentDough
		final double conductivityDough = thermalConductivityDough.apply(layerTemperature, moistureContentDough0);
		final double thermalDiffusivityDough = calculateThermalDiffusivity(conductivityDough, specificHeatDough, densityDough);
		setTheta(layer, dydt, (100./3.) * thermalDiffusivityDough
			* (getTheta(layer - 1, y) - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)) / Math.pow(layerThicknessDough, 2.));

		final double moistureDiffusivityDough = moistureDiffusivityDough(getTheta(layer, y));
		setC(layer, dydt, (100./3.) * moistureDiffusivityDough
			* (getC(layer - 1, y) - 3. * getC(layer, y) + 2. * getC(layer + 1, y)) / Math.pow(layerThicknessDough, 2.));
	}

	private void calculateInnerDoughLayer(final int layer, final double[] y, final double[] dydt, final double layers){
//		setTheta(layer, dydt, 25. * thermalDiffusivityDough
//			* (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)) / Math.pow(layerThicknessDough, 2.));
//
//		final double moistureDiffusivityDough = moistureDiffusivityDough(getTheta(layer, y));
//		setC(layer, dydt, 25. * moistureDiffusivityDough
//			* (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)) / Math.pow(layerThicknessDough, 2.));

		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y), bakingTemperatureTop, ambientTemperature);
		//FIXME calculate actual moistureContentDough
		final double conductivityDough = thermalConductivityDough.apply(layerTemperature, moistureContentDough0);
		final double thermalDiffusivityDough = calculateThermalDiffusivity(conductivityDough, specificHeatDough, densityDough);

		final double tmp = Math.pow(layerThicknessDough / layers, 2.);
		setTheta(layer, dydt, thermalDiffusivityDough
			* (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)) / tmp);

		final double moistureDiffusivityDough = moistureDiffusivityDough(getTheta(layer, y));
		setC(layer, dydt, moistureDiffusivityDough
			* (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)) / tmp);
	}

	private void calculateBottomLayer(final int layer, final double[] y, final double[] dydt){
//		//FIXME if distanceHeaterBottom is zero, there is heating anyway if forced convection air oven is used and distanceHeaterTop is non zero
//		final double thetaB = calculateFourierTemperature((distanceHeaterBottom > 0.? bakingTemperatureBottom: ambientTemperature),
//			ambientTemperature, bakingTemperatureTop);
//		final double tmp = 50. / (layerThicknessDough * layerThicknessDough);
//		setTheta(layer, dydt, (2./3.) * tmp * thermalDiffusivityDough
//			* (thetaB - 3. * getTheta(layer, y) + 2. * getTheta(layer + 1, y)));
//
//		//at the bottom: dC/d𝜓|𝜓=0 = 0, where 𝜓 = x / L
//		final double moistureDiffusivityDough = moistureDiffusivityDough(getTheta(layer, y));
//		setC(layer, dydt, tmp * moistureDiffusivityDough * (getC(layer + 1, y) - getC(layer, y)));

		final double thetaB = calculateFourierTemperature((distanceHeaterBottom > 0.? bakingTemperatureBottom: ambientTemperature),
			ambientTemperature, bakingTemperatureTop);
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y), bakingTemperatureTop, ambientTemperature);
		//FIXME calculate actual moistureContentDough
		final double doughDensity = physicalDensityDough.apply(layerTemperature, moistureContentDough0);
		final double conductivityDough = thermalConductivityDough.apply(layerTemperature, moistureContentDough0);
		final double thermalDiffusivityDough = calculateThermalDiffusivity(conductivityDough, specificHeatDough, densityDough);
		setTheta(layer, dydt, 2. * (
			thermalDiffusivityDough * (thetaB - getTheta(layer, y)) / layerThicknessDough
			+ SIGMA * bakingPan.material.emissivity * (Math.pow(thetaB, 4.) - Math.pow(getTheta(layer, y), 4.))
			+ heatTransferCoefficient * (thetaB - getTheta(layer, y))) / (doughDensity * specificHeatDough * layerThicknessDough)
		);

		//at the bottom: dC/d𝜓|𝜓=0 = 0, where 𝜓 = x / L
		final double moistureDiffusivityDough = moistureDiffusivityDough(getTheta(layer, y));
		setC(layer, dydt, 2. * moistureDiffusivityDough * (getC(layer + 1, y) - getC(layer, y))
			/ Math.pow(layerThicknessDough, 2.));
	}

	/**
	 * Calculate θ = (T - T∞) / (Ti - T∞).
	 *
	 * @param temperature	The temperature to transform.
	 * @param initialTemperature	The initial temperature.
	 * @param finalTemperature	The final temperature.
	 * @return	The Fourier temperature.
	 */
	private double calculateFourierTemperature(final double temperature, final double initialTemperature, final double finalTemperature){
		return (temperature - initialTemperature) / (finalTemperature - initialTemperature);
	}

	/**
	 * Calculate T = θ · (Ti - T∞) + T∞.
	 *
	 * @param fourierTemperature	The Fourier temperature to transform.
	 * @param initialTemperature	The initial temperature.
	 * @param finalTemperature	The final temperature.
	 * @return	The temperature.
	 */
	private double calculateInverseFourierTemperature(final double fourierTemperature, final double initialTemperature,
			final double finalTemperature){
		return fourierTemperature * (finalTemperature - initialTemperature) + initialTemperature;
	}

	private double getTheta(final int layer, final double[] array){
		return array[layer * 2];
	}

	private void setTheta(final int layer, final double[] array, final double value){
		array[layer * 2] = value;
	}

	private double getC(final int layer, final double[] array){
		return array[layer * 2 + 1];
	}

	private void setC(final int layer, final double[] array, final double value){
		array[layer * 2 + 1] = value;
	}

}
