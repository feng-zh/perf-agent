package com.hp.ts.perf.agent.instrument;

class InstrumentUtils {

	public static boolean isSameClass(Class<?> clz, String clsDesc) {
		System.out.println("compare: " + clz + " - " + clsDesc);
		return clz.getName().equals(clsDesc.replace('.', '/'));
	}

}
