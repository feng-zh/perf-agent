/*
 * JavaLanguage.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.hp.ts.perf.agent.expsrc.shared;

import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.java.ast.AstBuilder;

public class JavaMethod extends Language {
	private final String _name;
	private String methodName;

	public JavaMethod(String methodName) {
		_name = "JavaMethod";
		this.methodName = methodName;
	}

	@Override
	public final String getName() {
		return _name;
	}

	@Override
	public final String getFileExtension() {
		return ".java";
	}

	@Override
	public void decompileType(final TypeDefinition type,
			final ITextOutput output, final DecompilationOptions options) {
		final DecompilerSettings settings = options.getSettings();
		final DecompilerContext context = new DecompilerContext();
		context.setCurrentType(type);
		context.setSettings(settings);
		final AstBuilder builder = new AstBuilder(context);
		builder.addType(type);
		builder.generateCode(output);
	}

}
