package com.hp.ts.perf.agent.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class MethodInvokeInstrumentation implements ClassFileTransformer, Opcodes {

	private String packagePrefix;

	public MethodInvokeInstrumentation(String packagePrefix) {
		this.packagePrefix = packagePrefix == null ? "" : packagePrefix
				.replace('.', '/');
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if (loader == null) {
			return classfileBuffer;
		}
		if (!className.startsWith(packagePrefix)) {
			return classfileBuffer;
		}
		ClassReader cr = new ClassReader(classfileBuffer);
		cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);
		cr.accept(new ClassVisitor(ASM4, cw) {

			private boolean classLevel = false;

			private boolean needInstrument = false;

			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				if (!needInstrument
						&& InstrumentUtils
								.isSameClass(Instrumented.class, desc)) {
					needInstrument = true;
					classLevel = true;
				}
				return super.visitAnnotation(desc, visible);
			}

			@Override
			public MethodVisitor visitMethod(final int access, String name,
					String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc,
						signature, exceptions);
				return new MethodVisitor(api, mv) {

					private boolean needMethodInstument = classLevel
							&& ((access & ACC_PUBLIC) != 0);

					@Override
					public AnnotationVisitor visitAnnotation(String desc,
							boolean visible) {
						if (!needMethodInstument
								&& InstrumentUtils.isSameClass(
										Instrumented.class, desc)) {
							needMethodInstument = true;
						}
						return super.visitAnnotation(desc, visible);
					}

					@Override
					public void visitCode() {
						// TODO Auto-generated method stub
						super.visitCode();
					}
					
					

				};
			}

		}, 0);
		return cw.toByteArray();
	}
}
