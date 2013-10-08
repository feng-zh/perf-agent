package com.hp.ts.perf.agent.expsrc.shared;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

public class JavaClassReader {

	private StackTraceElement frame;

	public JavaClassReader(StackTraceElement frame) {
		this.frame = frame;
	}

	public String loadCode() {
		StringWriter sw = new StringWriter();
		DecompilerSettings settings = new DecompilerSettings();
		settings.setLanguage(new JavaMethod(frame.getMethodName()));
		Decompiler.decompile(frame.getClassName().replace('.', '/'),
				new PlainTextOutput(new PrintWriter(sw)), settings);
		return sw.toString();
	}
}
