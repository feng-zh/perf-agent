package com.hp.ts.perf.agent.demo;

import java.lang.instrument.Instrumentation;

public class AgentMain {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new ConstructorTracking(), false);
		// try {
		// if (inst.isRetransformClassesSupported()) {
		// for (Class<?> clz : inst.getAllLoadedClasses()) {
		// if (inst.isModifiableClass(clz)) {
		// try {
		// inst.retransformClasses(clz);
		// } catch (UnmodifiableClassException e) {
		// System.err.println("cannot transform " + clz);
		// }
		// }
		// }
		// }
		// } catch (Throwable t) {
		// t.printStackTrace();
		// }
	}

}
