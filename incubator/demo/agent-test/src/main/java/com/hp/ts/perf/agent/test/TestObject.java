package com.hp.ts.perf.agent.test;

import java.util.Date;

public class TestObject {

	private TestSubObject subObject;

	private String string;

	private Date datetime;
	
	private static TestObject instance;
	
	static {
		instance = new TestSubObject();
	}
	
	public TestObject() {}

	public TestObject(String msg) {
		subObject = new TestSubObject();
		string = msg + this;
		datetime = new Date();
	}
}
