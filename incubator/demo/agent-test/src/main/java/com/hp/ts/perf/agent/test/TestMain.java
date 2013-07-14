package com.hp.ts.perf.agent.test;

import java.util.ArrayList;
import java.util.List;

public class TestMain {
	
	public static TestMain instance = new TestMain();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("starting "+instance);
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < 5; i++) {
			list.add(new TestObject(String.valueOf(i)));
		}
		System.out.println(list);
		System.out.println("end");
	}

}
