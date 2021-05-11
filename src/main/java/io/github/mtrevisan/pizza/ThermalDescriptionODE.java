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

	private static final double[] WATER_EVAPORATION_LATENT_HEAT_LOW_COEFFICIENTS = {2500.9, -2.36719, 1.246e-4, -5.17e-6, -5.e-8, 1.45e-10, -2.7e-13};
	private static final double[] WATER_EVAPORATION_LATENT_HEAT_HIGH_COEFFICIENTS = {-16456273.5, 322865.7917, -2632.707957, 11.42226714, -0.02781080181, 3.6031127e-5, -1.94069959e-8};

	private static final double EMISSIVITY_NI_CR_WIRE = 0.87;
	private static final double EMISSIVITY_FE_CR_AL_WIRE = 0.70;
	private static final double EMISSIVITY_PIZZA = 0.5;

	//[m]
	private static final double MIN_LAYER_THICKNESS = 0.001;


	private final int layersMozzarella;
	private final int layersTomato;
	private final int layersOil;
	private final int layersDough;
	private final int layersPan;
	/**  [m] */
	private final double layerThicknessMozzarella;
	/**  [m] */
	private final double layerThicknessTomato;
	/**  [m] */
	private final double layerThicknessOil;
	/**  [m] */
	private final double layerThicknessDough;
	/**  [m] */
	private final double layerThicknessPan;

	private final OvenType ovenType;

	/** [°C] */
	private final double bakingTemperatureTop;
	/** [m] */
	private final double distanceHeaterTop;
	/** [°C] */
	private final double bakingTemperatureBottom;
	/** [m] */
	private final double distanceHeaterBottom;
	/** [°C] */
	private final double ambientTemperature;
	/** [°C] */
	private final double fourierMaximumTemperature;
	/** Air pressure [hPa]. */
	private final double airPressure;
	/** Air relative humidity [%]. */
	private final double airRelativeHumidity;
	//surface humidity ratio
	private final double humidityRatioSurface;

	private final double heatTransferCoefficient;

	/** [kg / m³] */
	private final double densityMozzarella = 1140.;
	/** [kg / m³] */
	private final double densityTomato = 1073.;
	/** [kg / m³] */
	private final double densityOil = 917.;
	/** [J / (kg · K)] */
	private final double specificHeatMozzarella = 2864.;
	/** [J / (kg · K)] */
	private final double specificHeatTomato = 2930.;
	/** [J / (kg · K)] */
	private final double specificHeatOil = 2000.;
	/** [J / (kg · K)] */
	private final double specificHeatDough = 3770.;
	/** Initial moisture content (0.47 to 0.55) [%] */
	private final double moistureContentMozzarella0 = 0.4435;
	/** Initial moisture content [%] */
	private final double moistureContentTomato0 = 0.91489;
	/** Initial moisture content [%] (usually between 0.03% kg/kg and 0.2% kg/kg) */
	private final double moistureContentOil0 = 0.01;
	/** Initial moisture content [%] */
	private final double moistureContentDough0 = 0.65;

	private final BakingPanAbstract bakingPan;

	private final BiFunction<Double, double[], Double> thermalConductivityMozzarella;
	private final BiFunction<Double, double[], Double> thermalConductivityTomato;
	private final BiFunction<Double, double[], Double> thermalConductivityOil;
	private final BiFunction<Double, double[], Double> thermalConductivityDough;
	private final BiFunction<Double, double[], Double> physicalDensityDough;


/*
https://www.sciencedirect.com/topics/engineering/convection-heat-transfer
https://ocw.mit.edu/courses/aeronautics-and-astronautics/16-050-thermal-energy-fall-2002/lecture-notes/10_part3.pdf
https://www.cantorsparadise.com/the-heat-equation-a76d7773a0b5
https://www.sfu.ca/~mbahrami/ENSC%20388/Notes/

general formula:
DQ = (Ts - T∞) / R	[W]
where
conduction:
R = L / (k · A)	[K / W]
k = thermal conductivity
convection:
R = 1 / (h · A)	[K / W]
h = convective heat transfer coefficient
radiation:
R = 1 / (σ · ε · (Ts² + T∞²) · (Ts + T∞) · A)	[K / W]
σ = 5.67e-8 [W / (m² · K⁴)] Stefan-Boltzmann constant
ε = emissivity

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
Hf	heat of evaporation
ρ	dough density
cp	dough specific heat
*/

	ThermalDescriptionODE(final double layerThicknessMozzarella, final double layerThicknessTomato, final double layerThicknessOil,
			final double layerThicknessDough,
			final OvenType ovenType, final double bakingTemperatureTop, final double distanceHeaterTop, final double bakingTemperatureBottom, final double distanceHeaterBottom,
			final double ambientTemperature, final double airPressure, final double airRelativeHumidity, final BakingPanAbstract bakingPan){
		layersMozzarella = (int)Math.ceil(layerThicknessMozzarella / MIN_LAYER_THICKNESS);
		layersTomato = (int)Math.ceil(layerThicknessTomato / MIN_LAYER_THICKNESS);
		layersOil = (int)Math.ceil(layerThicknessOil / MIN_LAYER_THICKNESS);
		layersDough = (int)Math.ceil(layerThicknessDough / MIN_LAYER_THICKNESS);
		layersPan = (int)Math.ceil(bakingPan.thickness / (1000. * MIN_LAYER_THICKNESS));

		this.layerThicknessMozzarella = layerThicknessMozzarella / layersMozzarella;
		this.layerThicknessTomato = layerThicknessTomato / layersTomato;
		this.layerThicknessOil = layerThicknessOil / layersOil;
		//TODO consider expansion during baking due to Charles-Gay Lussac law
		this.layerThicknessDough = layerThicknessDough / layersDough;
		this.layerThicknessPan = bakingPan.thickness / layersPan;

		this.ovenType = ovenType;

		this.bakingTemperatureTop = bakingTemperatureTop;
		this.distanceHeaterTop = distanceHeaterTop;
		this.bakingTemperatureBottom = bakingTemperatureBottom;
		this.distanceHeaterBottom = distanceHeaterBottom;
		this.ambientTemperature = ambientTemperature;
		fourierMaximumTemperature = Math.max(bakingTemperatureTop, bakingTemperatureBottom);
		this.airPressure = airPressure;
		this.airRelativeHumidity = airRelativeHumidity;

		//heat transfer coefficient:
		heatTransferCoefficient = ovenType.heatTransferCoefficient(bakingTemperatureTop);
		humidityRatioSurface = 0.1837 + (-0.0014607 + 0.000004477 * bakingTemperatureTop) * bakingTemperatureTop;

		this.bakingPan = bakingPan;

		thermalConductivityMozzarella = (temperature, y) -> {
			double water = 0.;
			final int offset = getDimension() / 2 - 2;
			for(int i = 0; i < layersMozzarella; i ++)
				water += getC(offset - i, y);
			water *= moistureContentDough0;
			return calculateThermalConductivity(temperature, 0.2651, 0.2386, 0.0196, 0., 0.0332, water);
		};
		thermalConductivityTomato = (temperature, y) -> {
			double water = 0.;
			final int offset = getDimension() / 2 - 2 - layersMozzarella - 1;
			for(int i = 0; i < layersTomato; i ++)
				water += getC(offset - i, y);
			water *= moistureContentDough0;
			return calculateThermalConductivity(temperature, 0.013, 0.002, 0.07, 0., 0.00011, water);
		};
		thermalConductivityOil = (temperature, y) -> {
			double water = 0.;
			final int offset = getDimension() / 2 - 2 - layersMozzarella - 1 - layersTomato - 1;
			for(int i = 0; i < layersOil; i ++)
				water += getC(offset - i, y);
			water *= moistureContentDough0;
			return calculateThermalConductivity(temperature, 0., 0.913, 0., 0., 0., water);
		};
		thermalConductivityDough = (temperature, y) -> {
			double water = 0.;
			final int offset = getDimension() / 2 - 2 - layersMozzarella - 1 - layersTomato - 1 - layersOil - 1;
			for(int i = 0; i < layersDough; i ++)
				water += getC(offset - i, y);
			water *= moistureContentDough0;
			return calculateThermalConductivity(temperature, 0.013, 0.011, 0.708, 0.019, 0.05, water);
		};

		physicalDensityDough = (temperature, y) -> {
			double water = 0.;
			final int offset = getDimension() / 2 - 2 - layersMozzarella - 1 - layersTomato - 1 - layersOil - 1;
			for(int i = 0; i < layersDough; i ++)
				water += getC(offset - i, y);
			water *= moistureContentDough0;
			return doughDensity(temperature, 0.13, 0.011, 0.013, 0.019, 0.05, water);
		};
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Moisture diffusivity in mozzarella layer [m² / s].
	 */
	private double moistureDiffusivityMozzarella(final double temperature){
		return 7.e-11;
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Moisture diffusivity in tomato layer [m² / s].
	 */
	private double moistureDiffusivityOil(final double temperature){
		//TODO
		return 7.e-11;
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Moisture diffusivity in tomato layer [m² / s].
	 */
	private double moistureDiffusivityTomato(final double temperature){
		//https://www.researchgate.net/publication/50863959_Effective_Moisture_Diffusivity_and_Activation_Energy_of_Tomato_in_Thin_Layer_Dryer_during_Hot_Air_Drying
		return (ovenType == OvenType.FORCED_CONVECTION?
			9.9646e-10 * Math.exp(-605.93 / temperature):
			1.7738e-10 * Math.exp(-1212.71 / temperature));
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Moisture diffusivity in dough layer [m² / s].
	 */
	private double moistureDiffusivityDough(final double temperature){
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
		return (1 + layersMozzarella + 1 + layersTomato + 1 + layersOil + 1 + layersDough + 1 + layersPan) * 2;
	}

	public final double[] getInitialState(){
		//array of initial temperature (as Fourier temperature) and moisture content
		final double[] state = new double[getDimension()];
		int offset = layersPan + 1;
		//dough layers
		for(int i = 0; i < layersDough; i ++)
			setC(i + offset, state, 1.);
		offset += layersDough;
		setC(offset, state, (moistureContentDough0 + moistureContentTomato0) / (2. * moistureContentDough0));
		//oil layers
		offset ++;
		for(int i = 0; i < layersOil; i ++)
			setC(i + offset, state, moistureContentOil0 / moistureContentDough0);
		offset += layersOil;
		setC(offset, state, (moistureContentOil0 + moistureContentTomato0) / (2. * moistureContentDough0));
		//tomato layers
		offset ++;
		for(int i = 0; i < layersTomato; i ++)
			setC(i + offset, state, moistureContentTomato0 / moistureContentDough0);
		offset += layersTomato;
		setC(offset, state, (moistureContentTomato0 + moistureContentMozzarella0) / (2. * moistureContentDough0));
		//mozzarella layers
		offset ++;
		for(int i = 0; i < layersMozzarella; i ++)
			setC(i + offset, state, moistureContentMozzarella0 / moistureContentDough0);
		offset += layersMozzarella;
		setC(offset, state, airRelativeHumidity);
		return state;
	}

	/**
	 * @param state	State array.
	 * @return	Minimum food temperature [°C].
	 */
	public final double getMinimumFoodTemperature(final double[] state){
		//dough layers
		int offset = 1 + layersPan;
		double min = getTheta(offset, state);
		for(int i = 1; i < layersDough; i ++)
			min = Math.min(getTheta(i + offset, state), min);
		//oil layers
		offset += 1 + layersOil;
		for(int i = 0; i < layersOil; i ++)
			min = Math.min(getTheta(i + offset, state), min);
		//tomato layers
		offset += 1 + layersDough;
		for(int i = 0; i < layersTomato; i ++)
			min = Math.min(getTheta(i + offset, state), min);
		//mozzarella layers
		offset += 1 + layersTomato;
		for(int i = 0; i < layersMozzarella; i ++)
			min = Math.min(getTheta(i + offset, state), min);
		return calculateInverseFourierTemperature(min);
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
- pan
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
where Lv is the latent heat of evaporation [J / kg]

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
		int index = getDimension() / 2;

		//top layer
		calculateTopLayer(-- index, y, dydt);

		//mozzarella layers
		for(int i = 0; i < layersMozzarella; i ++)
			calculateInnerMozzarellaLayer(-- index, y, dydt);

		calculateMozzarellaTomatoInterfaceLayer(-- index, y, dydt);

		//tomato layers
		for(int i = 0; i < layersTomato; i ++)
			calculateInnerTomatoLayer(-- index, y, dydt);

		calculateTomatoOilInterfaceLayer(-- index, y, dydt);

		//oil layers
		for(int i = 0; i < layersOil; i ++)
			calculateInnerOilLayer(-- index, y, dydt);

		calculateOilDoughInterfaceLayer(-- index, y, dydt);

		//dough layers
		for(int i = 0; i < layersDough; i ++)
			calculateInnerDoughLayer(-- index, y, dydt);

		//TODO add contact layer between dough and baking parchment paper
		//TODO add contact layer between baking parchment paper and pan
		calculateDoughPanInterfaceLayer(-- index, y, dydt);

		//pan layers
		for(int i = 0; i < layersPan - 1; i ++)
			calculateInnerPanLayer(-- index, y, dydt);

		//bottom layer, dough in contact with heated tray
		calculateBottomLayer(-- index, y, dydt);
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @param protein	Protein content [%].
	 * @param fat	Fat content [%].
	 * @param carbohydrate	Carbohydrate content [%].
	 * @param fiber	Fiber content [%].
	 * @param ash	Ash content [%].
	 * @param water	Water content [%].
	 * @return	The density of the dough [kg / m³].
	 */
	private double doughDensity(final double temperature, final double protein, final double fat, final double carbohydrate,
			final double fiber, final double ash, final double water){
		//FIXME consider charles-guy lussac gas expansion while cooking
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
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityMozzarella = thermalConductivityMozzarella.apply(layerTemperature, y);

		final double moistureDiffusivityMozzarella = moistureDiffusivityMozzarella(layerTemperature);

		calculateBoundaryTopLayer(layer, y, dydt,
			densityMozzarella, specificHeatMozzarella, conductivityMozzarella, layerThicknessMozzarella,
			moistureDiffusivityMozzarella);
	}

	private void calculateInnerMozzarellaLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityMozzarella = thermalConductivityMozzarella.apply(layerTemperature, y);

		final double moistureDiffusivityMozzarella = moistureDiffusivityMozzarella(layerTemperature);

		calculateInnerLayer(layer, y, dydt,
			densityMozzarella, specificHeatMozzarella, conductivityMozzarella, layerThicknessMozzarella, moistureDiffusivityMozzarella);
	}

	private void calculateMozzarellaTomatoInterfaceLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityMozzarella = thermalConductivityMozzarella.apply(layerTemperature, y);
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, y);

		final double moistureDiffusivityTomato = moistureDiffusivityTomato(layerTemperature);
		final double moistureDiffusivityMozzarella = moistureDiffusivityMozzarella(layerTemperature);

		calculateInterfaceLayer(layer, y, dydt,
			densityMozzarella, specificHeatMozzarella, conductivityMozzarella, layerThicknessMozzarella, moistureDiffusivityMozzarella,
			densityTomato, specificHeatTomato, conductivityTomato, layerThicknessTomato, moistureDiffusivityTomato);
	}

	private void calculateInnerTomatoLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, y);

		final double moistureDiffusivityTomato = moistureDiffusivityTomato(layerTemperature);

		calculateInnerLayer(layer, y, dydt,
			densityTomato, specificHeatTomato, conductivityTomato, layerThicknessTomato, moistureDiffusivityTomato);
	}

	private void calculateTomatoOilInterfaceLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, y);
		final double conductivityOil = thermalConductivityOil.apply(layerTemperature, y);

		final double moistureDiffusivityOil = moistureDiffusivityOil(layerTemperature);
		final double moistureDiffusivityTomato = moistureDiffusivityTomato(layerTemperature);

		calculateInterfaceLayer(layer, y, dydt,
			densityTomato, specificHeatTomato, conductivityTomato, layerThicknessTomato, moistureDiffusivityTomato,
			densityOil, specificHeatOil, conductivityOil, layerThicknessOil, moistureDiffusivityOil);
	}

	private void calculateInnerOilLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, y);

		final double moistureDiffusivityTomato = moistureDiffusivityTomato(layerTemperature);

		calculateInnerLayer(layer, y, dydt,
			densityTomato, specificHeatTomato, conductivityTomato, layerThicknessTomato, moistureDiffusivityTomato);
	}

	private void calculateOilDoughInterfaceLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityTomato = thermalConductivityTomato.apply(layerTemperature, y);
		final double densityDough = physicalDensityDough.apply(layerTemperature, y);
		final double conductivityDough = thermalConductivityDough.apply(layerTemperature, y);

		final double moistureDiffusivityTomato = moistureDiffusivityTomato(layerTemperature);
		final double moistureDiffusivityDough = moistureDiffusivityDough(layerTemperature);

		calculateInterfaceLayer(layer, y, dydt,
			densityTomato, specificHeatTomato, conductivityTomato, layerThicknessTomato, moistureDiffusivityTomato,
			densityDough, specificHeatDough, conductivityDough, layerThicknessDough, moistureDiffusivityDough);
	}

	private void calculateInnerDoughLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double densityDough = physicalDensityDough.apply(layerTemperature, y);
		final double conductivityDough = thermalConductivityDough.apply(layerTemperature, y);

		final double moistureDiffusivityDough = moistureDiffusivityDough(layerTemperature);

		calculateInnerLayer(layer, y, dydt,
			densityDough, specificHeatDough, conductivityDough, layerThicknessDough, moistureDiffusivityDough);
	}

	private void calculateDoughPanInterfaceLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double densityDough = physicalDensityDough.apply(layerTemperature, y);
		final double conductivityDough = thermalConductivityDough.apply(layerTemperature, y);
		final double conductivityPan = bakingPan.material.thermalConductivity(layerTemperature);

		final double moistureDiffusivityDough = moistureDiffusivityDough(layerTemperature);

		calculateInterfaceLayer(layer, y, dydt,
			densityDough, specificHeatDough, conductivityDough, layerThicknessDough, moistureDiffusivityDough,
			bakingPan.material.density, bakingPan.material.specificHeat, conductivityPan, layerThicknessPan, 0.);
	}

	private void calculateInnerPanLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityPan = bakingPan.material.thermalConductivity(layerTemperature);

		calculateInnerLayer(layer, y, dydt,
			bakingPan.material.density, bakingPan.material.specificHeat, conductivityPan, layerThicknessPan, 0.);
	}

	private void calculateBottomLayer(final int layer, final double[] y, final double[] dydt){
		final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
		final double conductivityPan = bakingPan.material.thermalConductivity(layerTemperature);

		calculateBoundaryBottomLayer(layer, y, dydt,
			bakingPan.material.density, bakingPan.material.specificHeat, conductivityPan, layerThicknessPan);
	}

	//dθ[m]/dτ = α · (θ[m-1] - 2 · θ[m] + θ[m+1]) / L²
	private void calculateInnerLayer(final int layer, final double[] y, final double[] dydt,
			final double density, final double specificHeat, final double conductivity, final double layerThickness,
			final double moistureDiffusivity){
		final double thermalDiffusivity = calculateThermalDiffusivity(conductivity, specificHeat, density);

		setTheta(layer, dydt, thermalDiffusivity
			* (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + getTheta(layer + 1, y)) / Math.pow(layerThickness, 2.));

		setC(layer, dydt, moistureDiffusivity
			* (getC(layer - 1, y) - 2. * getC(layer, y) + getC(layer + 1, y)) / Math.pow(layerThickness, 2.));
	}

	//dθ[m]/dτ = 4 · (kA · (θ[m-1] - θ[m]) / LA + kB · (θ[m+1] - θ[m]) / LB) / (ρA · CpA · LA + ρB · CpB · LB)
	private void calculateInterfaceLayer(final int layer, final double[] y, final double[] dydt,
			final double densityTop, final double specificHeatTop, final double conductivityTop, final double layerThicknessTop,
			final double moistureDiffusivityTop,
			final double densityBottom, final double specificHeatBottom, final double conductivityBottom, final double layerThicknessBottom,
			final double moistureDiffusivityBottom){
		final double volumetricHeatCapacityBottom = densityBottom * specificHeatBottom;
		final double volumetricHeatCapacityTop = densityTop * specificHeatTop;
		setTheta(layer, dydt, 4. / (volumetricHeatCapacityBottom * layerThicknessBottom + volumetricHeatCapacityTop * layerThicknessTop)
			* (conductivityBottom * (getTheta(layer - 1, y) - getTheta(layer, y)) / layerThicknessBottom
			- conductivityTop * (getTheta(layer, y) - getTheta(layer + 1, y)) / layerThicknessTop));

		setC(layer, dydt, 4. / Math.pow(layerThicknessBottom, 2.)
			* (moistureDiffusivityBottom * (getC(layer - 1, y) - getC(layer, y)) / layerThicknessBottom
			- moistureDiffusivityTop * (getC(layer, y) - getC(layer + 1, y)) / layerThicknessTop));
	}

	//https://www.comsol.com/blogs/how-to-model-heat-and-moisture-transport-in-air-with-comsol/
	//https://cran.r-project.org/web/packages/humidity/vignettes/humidity-measures.html
	//dθ[m]/dτ = 4 · (kA · (θ[m-1] - θ[m]) / LA + kB · (θ[m+1] - θ[m]) / LB) / (ρA · CpA · LA + ρB · CpB · LB)
	private void calculateBoundaryTopLayer(final int layer, final double[] y, final double[] dydt,
			final double density, final double specificHeat, final double conductivity, final double layerThickness,
			final double moistureDiffusivity){
		if(distanceHeaterTop > 0. || ovenType == OvenType.FORCED_CONVECTION){
			final double layerTemperature = calculateInverseFourierTemperature(getTheta(layer, y));
			final double thermalDiffusivity = calculateThermalDiffusivity(conductivity, specificHeat, density);
			final double evaporationLatentHeatWater = calculateEvaporationLatentHeatWater(layerTemperature);

			//surface mass transfer coefficient [kg H₂O / (m² · s)]
			final double massTransferSurface = massTransferSurface(layerTemperature);
			final double moistureContentSurface = getC(layer, y) - massTransferSurface / (moistureDiffusivity * density)
				* (humidityRatioSurface - airRelativeHumidity) * layerThickness / (2. * moistureContentDough0);
			final double thetaS = 1. / (heatTransferCoefficient + 2. * conductivity / layerThickness)
				* (heatTransferCoefficient + 2. * conductivity * getTheta(layer, y) / layerThickness
				- 2. * moistureDiffusivity * density * evaporationLatentHeatWater * moistureContentDough0
				/ (layerThickness * (bakingTemperatureTop - ambientTemperature)) * (getC(layer, y) - moistureContentSurface));

//			setTheta(layer, dydt, 4. / Math.pow(layerThickness, 2.)
//				* thermalDiffusivity * (getTheta(layer - 1, y) - 2. * getTheta(layer, y) + thetaS));

			final double thermalConductivityAir = OvenType.calculateAirThermalConductivity(ambientTemperature);
			final double specificHeatAir = OvenType.calculateAirSpecificHeat(ambientTemperature);
			final double densityAir = OvenType.calculateAirDensity(ambientTemperature, airPressure, airRelativeHumidity);
			final double thermalDiffusivityAir = calculateThermalDiffusivity(thermalConductivityAir, specificHeatAir, densityAir);

			final double viewFactor = 0.87;
			final double temperature = (ovenType == OvenType.NATURAL_CONVECTION? bakingTemperatureTop:
				(bakingTemperatureTop + bakingTemperatureBottom) / 2.);
			final double theta = calculateFourierTemperature(temperature);
			final double thetaTop = calculateFourierTemperature(bakingTemperatureTop);
			final double radiationFactor = calculateRadiationFactor(EMISSIVITY_PIZZA, bakingPan.area(), viewFactor);
			final double layerTheta = getTheta(layer, y);
			final double volumetricHeatCapacity = density * specificHeat;
			setTheta(layer, dydt, 2. * (
					thermalDiffusivity * (theta - layerTheta) / layerThickness
						+ (distanceHeaterTop > 0.? SIGMA * radiationFactor * (Math.pow(thetaTop, 4.) - Math.pow(layerTheta, 4.)): 0.)
						+ heatTransferCoefficient * (theta - layerTheta)
				) / (volumetricHeatCapacity * layerThickness)
			);


//			final double volumetricHeatCapacityAir = densityAir * specificHeatAir;
//			setTheta(layer, dydt, 4. / (volumetricHeatCapacity * layerThickness + volumetricHeatCapacityAir * layerThicknessAirTop)
//				* (conductivity * (getTheta(layer - 1, y) - layerTheta) / layerThickness
//				- conductivityAirTop * (layerTheta - getTheta(layer + 1, y)) / layerThicknessAirTop));
//
//			setC(layer, dydt, 4. / Math.pow(layerThickness, 2.)
//				* (moistureDiffusivity * (getC(layer - 1, y) - getC(layer, y)) / layerThickness
//				- moistureDiffusivityAirTop * (getC(layer, y) - getC(layer + 1, y)) / layerThicknessAirTop));


//			setC(layer, dydt, 4. / Math.pow(layerThickness, 2.)
//				* moistureDiffusivity * (getC(layer - 1, y) - 2. * getC(layer, y) + moistureContentSurface));
		}
	}

	/**
	 * @param temperature	Temperature [°C].
	 * @return	Evaporation latent heat of water, Lv [J / kg].
	 */
	private double calculateEvaporationLatentHeatWater(final double temperature){
		return 1000. * (temperature <= 260.?
			Helper.evaluatePolynomial(WATER_EVAPORATION_LATENT_HEAT_LOW_COEFFICIENTS, temperature):
			Helper.evaluatePolynomial(WATER_EVAPORATION_LATENT_HEAT_HIGH_COEFFICIENTS, temperature));
	}

	private double massTransferSurface(final double temperature){
		return (ovenType == OvenType.FORCED_CONVECTION?
			4.6332 * Math.exp(-277.5 / temperature):
			4.5721 * Math.exp(-292.8 / temperature));
	}

	//dθ[m]/dτ = 2 · (k · (θ[m-1] - θ[m]) / L + σ · ε · (T∞⁴ - θ[m]⁴) + h · (T∞ - θ[m])) / (ρ · Cp · L)
	private void calculateBoundaryBottomLayer(final int layer, final double[] y, final double[] dydt,
			final double density, final double specificHeat, final double conductivity, final double layerThickness){
		if(distanceHeaterBottom > 0. || ovenType == OvenType.FORCED_CONVECTION){
			final double viewFactor = 0.87;
			final double temperature = (ovenType == OvenType.NATURAL_CONVECTION? bakingTemperatureBottom:
				(bakingTemperatureTop + bakingTemperatureBottom) / 2.);
			final double theta = calculateFourierTemperature(temperature);
			final double thetaBottom = calculateFourierTemperature(bakingTemperatureBottom);
			final double thermalDiffusivity = calculateThermalDiffusivity(conductivity, specificHeat, density);
			final double radiationFactor = calculateRadiationFactor(bakingPan.material.emissivity, bakingPan.area(), viewFactor);
			final double layerTheta = getTheta(layer, y);
			final double volumetricHeatCapacity = density * specificHeat;
			setTheta(layer, dydt, 2. * (
				thermalDiffusivity * (theta - layerTheta) / layerThickness
					+ (distanceHeaterBottom > 0.? SIGMA * radiationFactor * (Math.pow(thetaBottom, 4.) - Math.pow(layerTheta, 4.)): 0.)
					+ heatTransferCoefficient * (theta - layerTheta)
				) / (volumetricHeatCapacity * layerThickness)
			);

			//at the bottom: dC/d𝜓|𝜓=0 = 0, where 𝜓 = x / L
		}
	}

	private double calculateRadiationFactor(final double emissivity, final double area, final double viewFactor){
		return 1. / (
			1. / (viewFactor * area)
			+ (1. - EMISSIVITY_NI_CR_WIRE) / (EMISSIVITY_NI_CR_WIRE * area)
			+ (1. - emissivity) / (emissivity * area)
		);
	}

	/**
	 * Calculate θ = (T - T∞) / (Ti - T∞).
	 *
	 * @param temperature	The temperature to transform.
	 * @return	The Fourier temperature.
	 */
	private double calculateFourierTemperature(final double temperature){
		return (temperature - ambientTemperature) / (fourierMaximumTemperature - ambientTemperature);
	}

	/**
	 * Calculate T = θ · (Ti - T∞) + T∞.
	 *
	 * @param fourierTemperature	The Fourier temperature to transform.
	 * @return	The temperature.
	 */
	private double calculateInverseFourierTemperature(final double fourierTemperature){
		return fourierTemperature * (fourierMaximumTemperature - ambientTemperature) + ambientTemperature;
	}

	private double getTheta(final int layer, final double[] array){
		return array[layer * 2];
	}

	private void setTheta(final int layer, final double[] array, final double value){
		array[layer * 2] = value;
	}

	private double getC(final int layer, final double[] array){
		return (layer * 2 + 1 >= 0? array[layer * 2 + 1]: 0.);
	}

	private void setC(final int layer, final double[] array, final double value){
		array[layer * 2 + 1] = value;
	}

}
