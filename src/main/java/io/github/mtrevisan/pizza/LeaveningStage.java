package io.github.mtrevisan.pizza;


public class LeaveningStage{

	//[°C]
	double temperature;
	//[hrs]
	double duration;


	public static LeaveningStage create(final double temperature, final double duration){
		return new LeaveningStage(temperature, duration);
	}

	private LeaveningStage(final double temperature, final double duration){
		this.temperature = temperature;
		this.duration = duration;
	}

}
