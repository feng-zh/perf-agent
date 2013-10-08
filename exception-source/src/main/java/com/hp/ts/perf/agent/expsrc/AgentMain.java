package com.hp.ts.perf.agent.expsrc;

import java.lang.instrument.Instrumentation;

public class AgentMain {

	public static void premain(String agentArgs, Instrumentation inst) {
		ThrowableClassTransformer.premain(agentArgs, inst);
	}

}
