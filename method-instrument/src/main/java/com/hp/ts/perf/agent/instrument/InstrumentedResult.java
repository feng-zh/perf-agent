package com.hp.ts.perf.agent.instrument;

public class InstrumentedResult {

	private boolean classLevel;

	public void enableClassLevel() {
		this.classLevel = true;
	}
	
	public boolean isClassLevel() {
		return classLevel;
	}

}
