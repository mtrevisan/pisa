package io.github.mtrevisan.pizza;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class A243491{

	private static final Logger LOGGER = LoggerFactory.getLogger(A243491.class);

	//[°C]
	public static final double ABSOLUTE_ZERO = 273.15;

	private static final int INDEX_PROTEIN = 0;
	private static final int INDEX_FAT = 1;
	private static final int INDEX_CARBOHYDRATE = 2;
	private static final int INDEX_FIBER = 3;
	private static final int INDEX_ASH = 4;
	private static final int INDEX_WATER = 5;


	public void doThings(int shellNodes, double initialDoughThickness, double finalShellThickness,
			double initialDoughProtein, double initialDoughFat, double initialDoughCarbohydrate, double initialDoughFiber,
			double initialDoughAsh, double initialDoughMoisture, double initialPizzaTemperature,
			double panDiameter, double finalCrustThickness, double initialDoughWeight,
			double bakedShellAndTomatoWeight, double moistureAbsorbedFromToppingsByShellWeight,
			double initialTomatoWeight, double initialTomatoMoistureContent, double initialOilWeight, double finalOilWeight,
			int ovenCookingZones,
			double[] ovenZoneLength, double[] ovenTemperatureZone, double[] convectiveHeatTransferCoefficientCookingZone,
			double totalCookingTime,
			double patThermalConductivity, double panThickness, double panEmissivity, double panDensity, double panSpecificHeat,
			double crustEffectiveDiffusivity,
			double finalTemperatureShellTop){
		shellNodes ++;
		double initialDoughThicknessOverFinalDoughThickness = initialDoughThickness / finalShellThickness;
		double doughSliceThickness = (finalShellThickness / 1000.) / (shellNodes - 1);
		double[][] doughComponent = new double[6][shellNodes];
		double[][] doughTemperature = new double[2][shellNodes];
		for(int index = 0; index < shellNodes; index ++){
			doughComponent[INDEX_PROTEIN][index] = initialDoughProtein;
			doughComponent[INDEX_FAT][index] = initialDoughFat;
			doughComponent[INDEX_CARBOHYDRATE][index] = initialDoughCarbohydrate;
			doughComponent[INDEX_FIBER][index] = initialDoughFiber;
			doughComponent[INDEX_ASH][index] = initialDoughAsh;
			doughComponent[INDEX_WATER][index] = initialDoughMoisture;
			doughTemperature[0][index] = initialPizzaTemperature;
		}
		double outsidePanTemperatureAtT;
		double insidePanTemperatureAtT;
		double initialDoughVolume = (initialDoughThickness / 1000.) * Math.PI * Math.pow(panDiameter / 2000., 2.);
		final double finalCrustVolume = (finalCrustThickness / 1000.) * Math.PI * Math.pow(panDiameter / 2000., 2.);
		//[%]
		double doughVoidSpace = (initialDoughVolume - ((initialDoughWeight / 1000.) / doughDensity(initialPizzaTemperature, doughComponent,
			0))) / initialDoughVolume;
		double totalMoistureLossDuringCooking = initialDoughWeight - bakedShellAndTomatoWeight + moistureAbsorbedFromToppingsByShellWeight
			+ initialTomatoWeight * initialTomatoMoistureContent - (initialOilWeight - finalOilWeight);
		final double initialCrustMoisture = finalCrustVolume * (initialDoughWeight / (initialDoughVolume * (1. - doughVoidSpace)))
			* initialDoughMoisture;
		final double initialCrustProtein = finalCrustVolume * (initialDoughWeight / (initialDoughVolume * (1. - doughVoidSpace)))
			* initialDoughProtein;
		final double initialCrustFat = finalCrustVolume * (initialDoughWeight / (initialDoughVolume * (1. - doughVoidSpace)))
			* initialDoughFat;
		final double initialCrustCarbohydrates = finalCrustVolume * (initialDoughWeight / (initialDoughVolume * (1. - doughVoidSpace)))
			* initialDoughCarbohydrate;
		final double initialCrustFiber = finalCrustVolume * (initialDoughWeight /(initialDoughVolume * (1. - doughVoidSpace)))
			* initialDoughFiber;
		final double initialCrustAsh = finalCrustVolume * (initialDoughWeight / (initialDoughVolume * (1. - doughVoidSpace)))
			* initialDoughAsh;
		final double finalCrustMoisture = initialCrustMoisture - totalMoistureLossDuringCooking;
		final double finalCrustFat = initialCrustFat + (initialOilWeight - finalOilWeight);
		//[%]
		final double finalCrustMoisturePercent = finalCrustMoisture / (finalCrustMoisture + finalCrustFat + initialCrustProtein
			+ initialCrustCarbohydrates + initialCrustFiber + initialCrustAsh);
		//[%]
		final double finalCrustFatPercent = finalCrustFat / (finalCrustMoisture + finalCrustFat + initialCrustProtein
			+ initialCrustCarbohydrates + initialCrustFiber + initialCrustAsh);
		//[%]
		final double finalCrustProteinPercent = initialCrustProtein / (finalCrustMoisture + finalCrustFat + initialCrustProtein
			+ initialCrustCarbohydrates + initialCrustFiber + initialCrustAsh);
		//[%]
		final double finalCrustCarbohydratesPercent = initialCrustCarbohydrates / (finalCrustMoisture + finalCrustFat + initialCrustProtein
			+ initialCrustCarbohydrates + initialCrustFiber + initialCrustAsh);
		//[%]
		final double finalCrustFiberPercent = initialCrustFiber / (finalCrustMoisture + finalCrustFat + initialCrustProtein
			+ initialCrustCarbohydrates + initialCrustFiber + initialCrustAsh);
		//[%]
		final double finalCrustAshPercent = initialCrustAsh / (finalCrustMoisture + finalCrustFat + initialCrustProtein
			+ initialCrustCarbohydrates + initialCrustFiber + initialCrustAsh);
		//[%]
		double[][] oil = new double[6][1];
		oil[1][0] = 1.;
		//steam in void of dough slice `index`
		double[] steamInVoid = new double[shellNodes];
		//initial moisture content in a slice of dough
		final double initialMoistureContent = ((initialDoughWeight / 1000.) / initialDoughVolume) * (1. - doughVoidSpace)
			* (doughSliceThickness * initialDoughThicknessOverFinalDoughThickness) * initialDoughMoisture;
		//moisture in slice of dough at node `index`
		double[] moistureContent = new double[shellNodes];
		for(int index = 0; index < shellNodes; index ++)
			moistureContent[index] = (index == 0 || index == shellNodes - 1? initialMoistureContent / 2.: initialMoistureContent);
		final double oilAbsorbedByCrustInDT = ((initialOilWeight - finalOilWeight) / 1000.) / (finalCrustThickness / 1000.)
			* doughSliceThickness;
		//moisture in slice of dough once it becomes crust
		final double moistureContentCrust = initialMoistureContent - ((totalMoistureLossDuringCooking / 1000.)
			/ (finalCrustThickness / 1000.) * doughSliceThickness);
		//mass of steam that fills the voids in the a slice of dough
		final double steamMass = 0.5228 * doughSliceThickness;
		double ovenTotalLength = 0.;
		for(int index = 0; index < ovenCookingZones; index ++)
			ovenTotalLength += ovenZoneLength[index];
		final double beltSpeed = totalCookingTime / ovenTotalLength;
		Double doughConductivityCorrectionFactor = null;
		while(true){
			int doughSlicesExperiencingMoistureLoss = 1;
			double totalMoistureLossAtTimeT = 0.;
			//mass of oil in pan at time t
			double oilInPan = initialOilWeight / 1000.;
			//excess heat due to steam condensation passed to next dough slice
			double excessHeat = 0.;
			outsidePanTemperatureAtT = initialPizzaTemperature;
			insidePanTemperatureAtT = initialPizzaTemperature;
			Double doughSpecificHeat = null;
			for(int cookingTime = 0; cookingTime < totalCookingTime; cookingTime ++){
				double lengthTraveledThroughAtTimeT = 0.;
				Double Zh = null;
				Double ZONETEMP = null;
				for(int index = 0; index < ovenCookingZones; index ++){
					lengthTraveledThroughAtTimeT += ovenZoneLength[index];
					if(cookingTime <= lengthTraveledThroughAtTimeT * beltSpeed){
						Zh = convectiveHeatTransferCoefficientCookingZone[index];
						ZONETEMP = ovenTemperatureZone[index];
						break;
					}
				}
				final double outsidePanTemperatureAtTPlusDT = outsidePanTemperatureAtT + (Zh * (ZONETEMP - outsidePanTemperatureAtT)
					- ((patThermalConductivity / (panThickness / 1000.)) * (outsidePanTemperatureAtT - insidePanTemperatureAtT))
					+ (panEmissivity * 5.67e-8 * (Math.pow(ZONETEMP + ABSOLUTE_ZERO, 4.)
					- Math.pow(outsidePanTemperatureAtT + ABSOLUTE_ZERO, 4.)))) * (2. / ((panThickness / 1000.) * panDensity * panSpecificHeat));
				final double oilTemperature = (insidePanTemperatureAtT + doughTemperature[0][0]) / 2.;
				final double oilDensity = doughDensity(oilTemperature, oil, 0);
				final double oilLayerThicknessAtT = (oilInPan / oilDensity) / (Math.PI * Math.pow(panDiameter / 2000., 2.));
				if(oilLayerThicknessAtT <= 0.){
					crustEffectiveDiffusivity *= 2.;
					break;
				}
				//inside pan temperature at time t plus delta t
				final double insidePanTemperatureAtTPlusDT = insidePanTemperatureAtT + ((patThermalConductivity / (panThickness / 1000.))
					* (outsidePanTemperatureAtT - insidePanTemperatureAtT) - (doughConductivity(oilTemperature, oil, 1)
					/ oilLayerThicknessAtT) * (insidePanTemperatureAtT - doughTemperature[0][0])) / ((panThickness / 1000.) * panDensity
					* panSpecificHeat + (oilLayerThicknessAtT / 2.) * oilDensity * specificHeat(oilTemperature, oil, 0));
				Double doughDensity = null;
				if(doughTemperature[0][0] < 100.){
					final double oilThermalConductivity = doughConductivity(oilTemperature, oil, 0);
					final double oilSpecificHeat = specificHeat(oilTemperature, oil, 0);
					doughDensity = doughDensity(doughTemperature[0][0], doughComponent, 0) * (1. - doughVoidSpace);
					final double doughThermalConductivity = doughConductivity(doughTemperature[0][0], doughComponent, 0)
						* (1. - doughVoidSpace) * doughConductivityCorrectionFactor;
					doughSpecificHeat = specificHeat(doughTemperature[0][0], doughComponent, 0) * (1. - doughVoidSpace);
					doughTemperature[1][0] = ((oilThermalConductivity / oilLayerThicknessAtT)
						* (insidePanTemperatureAtT - doughTemperature[0][0]) - (doughThermalConductivity / (doughSliceThickness
						* initialDoughThicknessOverFinalDoughThickness)) * (doughTemperature[0][0] - doughTemperature[0][1]))
						/ (((doughSliceThickness * initialDoughThicknessOverFinalDoughThickness) / 2.) * doughDensity * doughSpecificHeat);
				}
				else
					excessHeat = 0.;
				if(doughTemperature[1][0] > 100.){
					final double heat = (doughTemperature[1][0] - 100.) * ((doughSliceThickness
						* initialDoughThicknessOverFinalDoughThickness) / 2.) * doughDensity * doughSpecificHeat;
					doughTemperature[1][0] = 100.;
					final double MSP = heat / 2444900.;
					if(MSP + steamInVoid[0] > steamMass){
						if(moistureContent[0] - (MSP + steamInVoid[0] - steamMass) < moistureContentCrust){
							excessHeat = (moistureContentCrust - (moistureContent[0] - (MSP + steamInVoid[0] - steamMass))) * 2444900.;
							totalMoistureLossAtTimeT += moistureContent[0] - moistureContentCrust;
							moistureContent[0] = moistureContentCrust;
							oilInPan -= oilAbsorbedByCrustInDT / 2.;
							doughSlicesExperiencingMoistureLoss = 2;
							doughComponent[INDEX_PROTEIN][0] = finalCrustProteinPercent;
							doughComponent[INDEX_FAT][0] = finalCrustFatPercent;
							doughComponent[INDEX_CARBOHYDRATE][0] = finalCrustCarbohydratesPercent;
							doughComponent[INDEX_FIBER][0] = finalCrustFiberPercent;
							doughComponent[INDEX_ASH][0] = finalCrustAshPercent;
							doughComponent[INDEX_WATER][0] = finalCrustMoisturePercent;
						}
						else{
							final double PCMC = (moistureContent[0] - (MSP + steamInVoid[0] - steamMass)) / moistureContent[0];
							totalMoistureLossAtTimeT += MSP + steamInVoid[0] - steamMass;
							moistureContent[0] -= MSP + steamInVoid[0] - steamMass;
							rescale(PCMC, doughComponent, 0);
							steamInVoid[0] = steamMass;
							excessHeat = 0.;
						}
					}
					else{
						steamInVoid[0] += MSP;
						excessHeat = 0.;
					}
				}
				if(doughTemperature[0][0] == 100.){
					final double oilThermalConductivity = doughConductivity(oilTemperature, oil, 0);
					final double oilSpecificHeat = specificHeat(oilTemperature, oil, 0);
					doughDensity = doughDensity(doughTemperature[0][0], doughComponent, 0) * (1. - doughVoidSpace);
					final double doughThermalConductivity = doughConductivity(doughTemperature[0][0], doughComponent, 0)
						* (1. - doughVoidSpace) * doughConductivityCorrectionFactor;
					doughSpecificHeat = specificHeat(doughTemperature[0][0], doughComponent, 0) * (1. - doughVoidSpace);
					doughTemperature[1][0] = ((oilThermalConductivity / oilLayerThicknessAtT)
						* (insidePanTemperatureAtT - doughTemperature[0][0]) - (doughThermalConductivity
						/ (doughSliceThickness * initialDoughThicknessOverFinalDoughThickness)) * (doughTemperature[0][0]
						- doughTemperature[0][1])) / (((doughSliceThickness * initialDoughThicknessOverFinalDoughThickness) / 2.)
						* doughDensity * doughSpecificHeat);
					if(moistureContent[0] > moistureContentCrust){
						final double heat = (doughTemperature[1][0] - 100.) *((doughSliceThickness
							* initialDoughThicknessOverFinalDoughThickness) / 2.) * doughDensity * doughSpecificHeat;
						doughTemperature[1][0] = 100.;
						final double MSP = heat / 2444900.;
						if(MSP + steamInVoid[0] > steamMass){
							if(moistureContent[0] - (MSP + steamInVoid[0] - steamMass) < moistureContentCrust){
								excessHeat = (moistureContentCrust - (moistureContent[0] - (MSP + steamInVoid[0] - steamMass))) * 2444900.;
								totalMoistureLossAtTimeT += moistureContent[0] - moistureContentCrust;
								moistureContent[0] = moistureContentCrust;
								oilInPan -= oilAbsorbedByCrustInDT / 2.;
								doughSlicesExperiencingMoistureLoss = 2;
								doughComponent[INDEX_PROTEIN][0] = finalCrustProteinPercent;
								doughComponent[INDEX_FAT][0] = finalCrustFatPercent;
								doughComponent[INDEX_CARBOHYDRATE][0] = finalCrustCarbohydratesPercent;
								doughComponent[INDEX_FIBER][0] = finalCrustFiberPercent;
								doughComponent[INDEX_ASH][0] = finalCrustAshPercent;
								doughComponent[INDEX_WATER][0] = finalCrustMoisturePercent;
							}
							else{
								final double PCMC = (moistureContent[0] - (MSP + steamInVoid[0] - steamMass)) / moistureContent[0];
								totalMoistureLossAtTimeT += (MSP + steamInVoid[0] - steamMass);
								moistureContent[0] -= MSP + steamInVoid[0] - steamMass;
								rescale(PCMC, doughComponent, 0);
								steamInVoid[0] = steamMass;
								excessHeat = 0.;
							}
						}
						else{
							steamInVoid[0] = MSP;
							excessHeat = 0.;
						}
					}
					else
						excessHeat = 0.;
				}
				for(int m = 0; m < shellNodes - 1; m ++){
					doughDensity = doughDensity(doughTemperature[0][m], doughComponent, m) * (1. - doughVoidSpace);
					final double doughThermalConductivity = doughConductivity(doughTemperature[0][m], doughComponent, m) * (1. - doughVoidSpace)
						* doughConductivityCorrectionFactor;
					doughSpecificHeat = specificHeat(doughTemperature[0][m], doughComponent, m) * (1. - doughVoidSpace);
					doughTemperature[1][m] = doughTemperature[0][m] + ((doughThermalConductivity / (doughSliceThickness
						* initialDoughThicknessOverFinalDoughThickness)) * (doughTemperature[0][m - 1] + doughTemperature[0][m + 1]
						- 2. * doughTemperature[0][m]) + excessHeat) / ((doughSliceThickness * initialDoughThicknessOverFinalDoughThickness)
						* doughDensity * doughSpecificHeat);
					if(doughSlicesExperiencingMoistureLoss != m){
						if(doughTemperature[1][m] > 100.){
							final double heat = (doughTemperature[1][m] - 100.) * doughSliceThickness * doughDensity * doughSpecificHeat;
							doughTemperature[1][m] = 100.;
							final double MSP = heat / 2444900.;
							if(MSP + steamInVoid[m] > steamMass){
								excessHeat = ((MSP + steamInVoid[m]) - steamMass) * 2444900.;
								steamInVoid[m] = steamMass;
							}
							else{
								excessHeat = 0.;
								steamInVoid[m] = steamInVoid[m] + MSP;
							}
						}
						else
							excessHeat = 0.;
					}
					else if(doughTemperature[1][m] > 100.){
						final double heat = (doughTemperature[1][m] - 100.) * doughSliceThickness * doughDensity * doughSpecificHeat;
						doughTemperature[1][m] = 100.;
						final double MSP = heat / 2444900.;
						if(steamInVoid[m] + MSP > steamMass){
							//moisture lost from slice
							double sliceMoistureLost = crustEffectiveDiffusivity * (steamInVoid[m] + MSP)
								/ Math.pow(doughSliceThickness * (m - 1.), 2.);
							if(moistureContent[m] - sliceMoistureLost < moistureContentCrust){
								sliceMoistureLost = moistureContent[m] - moistureContentCrust;
								totalMoistureLossAtTimeT += sliceMoistureLost;
								moistureContent[m] = moistureContentCrust;
								doughSlicesExperiencingMoistureLoss ++;
								doughComponent[INDEX_PROTEIN][m] = finalCrustProteinPercent;
								doughComponent[INDEX_FAT][m] = finalCrustFatPercent;
								doughComponent[INDEX_CARBOHYDRATE][m] = finalCrustCarbohydratesPercent;
								doughComponent[INDEX_FIBER][m] = finalCrustFiberPercent;
								doughComponent[INDEX_ASH][m] = finalCrustAshPercent;
								doughComponent[INDEX_WATER][m] = finalCrustMoisturePercent;
								doughTemperature[1][m] = 100.;
								excessHeat = (steamInVoid[m] + MSP - sliceMoistureLost - steamMass) * 2444900.;
								oilInPan -= oilAbsorbedByCrustInDT;
								steamInVoid[m] = steamMass;
							}
							else{
								totalMoistureLossAtTimeT += sliceMoistureLost;
								final double PCMC = (moistureContent[m] - (MSP + steamInVoid[m] - steamMass)) / moistureContent[m];
								moistureContent[m] -= sliceMoistureLost;
								excessHeat = (MSP + steamInVoid[m] - steamMass - sliceMoistureLost) * 2444900.;
								steamInVoid[m] = steamMass;
								rescale(PCMC, doughComponent, m);
							}
						}
						else
							steamInVoid[m] += MSP;
					}
					else
						excessHeat = 0.;
				}
				doughDensity = doughDensity(doughTemperature[0][shellNodes], doughComponent, shellNodes - 1) * (1. - doughVoidSpace);
				final double doughThermalConductivity = doughConductivity(doughTemperature[0][shellNodes], doughComponent, shellNodes - 1)
					* (1. - doughVoidSpace) * doughConductivityCorrectionFactor;
				doughSpecificHeat = specificHeat(doughTemperature[0][shellNodes], doughComponent, shellNodes - 1)
					* (1. - doughVoidSpace);
				doughTemperature[1][shellNodes] = doughTemperature[0][shellNodes] + (doughThermalConductivity / (doughSliceThickness
					* initialDoughThicknessOverFinalDoughThickness)) * (doughTemperature[0][shellNodes - 1] - doughTemperature[0][shellNodes])
					/ (doughSliceThickness * initialDoughThicknessOverFinalDoughThickness * doughDensity * doughSpecificHeat);
				excessHeat = 0.;
				for(int index = 0; index < shellNodes; index ++)
					doughTemperature[0][index] = doughTemperature[1][index];
				outsidePanTemperatureAtT = outsidePanTemperatureAtTPlusDT;
				insidePanTemperatureAtT = insidePanTemperatureAtTPlusDT;
			}
			totalMoistureLossAtTimeT *= Math.PI * Math.pow(panDiameter / 2000., 2.);
			if(Math.abs(totalMoistureLossAtTimeT - totalMoistureLossDuringCooking) / totalMoistureLossDuringCooking > 0.01){
				crustEffectiveDiffusivity *= totalMoistureLossDuringCooking / totalMoistureLossAtTimeT;
				LOGGER.debug("Calculated moisture content: {}%, Calculated final temperature: {} °C", totalMoistureLossAtTimeT,
					doughTemperature[0][shellNodes]);
			}
			else if(Math.abs(doughTemperature[0][shellNodes] - finalTemperatureShellTop) / finalTemperatureShellTop > 0.01)
				doughConductivityCorrectionFactor = finalTemperatureShellTop / doughTemperature[0][shellNodes];
			else
				break;
		}
	}

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

	private double convectiveHeatTransferCoefficient(final double airSpeed, final double temperature, final double[][] airParams,
			final double pizzaDiameter){
		//FIXME find index x on airParams whose temperature is airParams[x - 1][0] < temperature <= airParams[x][0]
		int x = -1;
		while(temperature <= airParams[++ x][0])
			if(x == 9){
				x = 10;
				break;
			}
		final int x1 = Math.max(x - 2, 0);
		final int x2 = Math.max(x - 1, 1);

		final double airDensity = airParams[x2][1] - (airParams[x2][1] - airParams[x1][1]) * (airParams[x2][0] - temperature)
			/ (airParams[x2][0] - airParams[x1][0]);
		final double airViscosity = airParams[x2][2] - (airParams[x2][2] - airParams[x1][2]) * (airParams[x2][0] - temperature)
			/ (airParams[x2][0] - airParams[x1][0]);
		final double airConductivity = airParams[x2][3] - (airParams[x2][3] - airParams[x1][3]) * (airParams[x2][0] - temperature)
			/ (airParams[x2][0] - airParams[x1][0]);
		final double prandtlNumber = airParams[x2][4] - (airParams[x2][4] - airParams[x1][4]) * (airParams[x2][0] - temperature)
			/ (airParams[x2][0] - airParams[x1][0]);
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

	private double specificHeat(final double temperature, final double[][] params, final int index){
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
