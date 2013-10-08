package com.hp.it.perf.btrace.script;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;
import com.sun.btrace.annotations.Self;

@BTrace
public class OpenportalBTrace {

	@OnMethod(clazz = "+org.eclipse.persistence.internal.sessions.DatabaseSessionImpl", method = "logout")
	public static void logout() {
		BTraceUtils.jstack();
	}

	@OnMethod(clazz = "java.lang.Throwable", method = "<init>", location = @Location(Kind.RETURN))
	public static void saveException(@ProbeClassName String paramString1,
			@ProbeMethodName String paramString2, @Self Throwable paramThrowable) {
		BTraceUtils.print(paramString1);
		BTraceUtils.print(" -- ");
		BTraceUtils.println(paramThrowable);
	}
}
