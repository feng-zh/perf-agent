package com.hp.ts.perf.agent.instrument;

import java.lang.instrument.Instrumentation;

public class MethodInstrumentMain {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new MethodInvokeInstrumentation(agentArgs), false);
	}

}
