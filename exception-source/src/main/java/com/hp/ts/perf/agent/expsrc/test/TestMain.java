package com.hp.ts.perf.agent.expsrc.test;

public class TestMain {

	public static class InnerClass {

		public InnerClass(Object dummy) {
		}

		public static void error() {
			try {
				Class.forName(
						"com.hp.ts.perf.agent.expsrc.test.TestMain$InnerClass")
						.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("start main");
		try {
			InnerClass.error();
		} catch (Throwable th) {
			th.printStackTrace(System.out);
		} finally {
			System.out.println("end main");
		}
	}
}
