package com.hp.ts.perf.agent.expsrc.shared;

import java.util.concurrent.atomic.AtomicInteger;

public class ThrowableSourceHolder {

	private static ThreadLocal<Object> inPrintStackTrace = new ThreadLocal<Object>();

	private static String getLastSource(Throwable th) {
		StackTraceElement[] stackTrace = th.getStackTrace();
		if (stackTrace.length > 0) {
			StackTraceElement callTrace = stackTrace[0];
			JavaSourceReader sourceReader = new JavaSourceReader(callTrace);
			String code = sourceReader.loadCode();
			return code;
		} else {
			return null;
		}
	}

	public static void enterPrintStackTrace() {
		AtomicInteger printLevel = (AtomicInteger) inPrintStackTrace.get();
		if (printLevel == null) {
			printLevel = new AtomicInteger();
			inPrintStackTrace.set(printLevel);
		} else {
			printLevel.incrementAndGet();
		}
	}

	public static void exitPrintStackTrace() {
		AtomicInteger printLevel = (AtomicInteger) inPrintStackTrace.get();
		if (printLevel != null) {
			if (printLevel.intValue() > 0) {
				printLevel.decrementAndGet();
			} else {
				inPrintStackTrace.remove();
			}
		}
	}

	public static String throwableToString(String toStringMsg, Throwable th) {
		AtomicInteger printLevel = (AtomicInteger) inPrintStackTrace.get();
		if (printLevel == null) {
			return toStringMsg;
		}
		String lastSource = getLastSource(th);
		if (lastSource != null) {
			return toStringMsg
					+ "\n\t------------------------------------------------------------------------"
					+ "\n"
					+ lastSource
					+ "\n\t------------------------------------------------------------------------";
		} else {
			return toStringMsg;
		}
	}

}
