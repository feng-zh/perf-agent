package com.hp.ts.perf.agent.demo;

import java.lang.instrument.Instrumentation;

public class AgentMain {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new ConstructorTracking());
	}

}
