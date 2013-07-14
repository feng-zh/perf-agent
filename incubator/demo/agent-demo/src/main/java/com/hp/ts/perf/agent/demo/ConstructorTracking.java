package com.hp.ts.perf.agent.demo;

import static org.objectweb.asm.Opcodes.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class ConstructorTracking implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, final String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);
		ClassVisitor cv = new ClassVisitor(ASM4, cw) {

			@Override
			public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc,
						signature, exceptions);
				return new MethodVisitor(api, mv) {

					@Override
					public void visitTypeInsn(int opcode, String type) {
						if (opcode == NEW) {
							beforeObjectNew(type);
						}
						super.visitTypeInsn(opcode, type);
						if (opcode == NEW) {
							afterObjectNew(type);
						}
					}

					private void afterObjectNew(String type) {
						println(className + ": after allocating an object of "
								+ type);
					}

					private void beforeObjectNew(String type) {
						println("before allocating an object of " + type);
					}

					private void println(String msg) {
						visitFieldInsn(GETSTATIC, "java/lang/System", "out",
								"Ljava/io/PrintStream;");
						visitLdcInsn(msg);
						visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
								"println", "(Ljava/lang/String;)V");
					}

				};
			}

		};
		cr.accept(cv, 0);
		System.out.println("transform: " + className);
		return cw.toByteArray();
	}
}
