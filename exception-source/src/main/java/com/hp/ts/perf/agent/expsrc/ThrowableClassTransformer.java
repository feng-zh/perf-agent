package com.hp.ts.perf.agent.expsrc;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import com.hp.ts.perf.agent.expsrc.shared.ThrowableSourceHolder;

class ThrowableClassTransformer implements ClassFileTransformer {

	private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
	private static final String THROWABLE_INTERNAL_NAME = THROWABLE_TYPE
			.getInternalName();

	private static final String FILL_IN_STACK_TRACE_METHOD_NAME = "fillInStackTrace";
	private static final String FILL_IN_STACK_TRACE_METHOD_DESC = Type
			.getMethodDescriptor(THROWABLE_TYPE);

	private static final Type THROWABLE_SOURCE_HOLDER_TYPE = Type
			.getType(ThrowableSourceHolder.class);
	private static final String THROWABLE_SOURCE_HOLDER_INTERNAL_NAME = THROWABLE_SOURCE_HOLDER_TYPE
			.getInternalName();

	private static final String SET_STACK_TRACE_METHOD_NAME = "setStackTrace";
	private static final String SET_STACK_TRACE_METHOD_DESC = Type
			.getMethodDescriptor(Type.VOID_TYPE, THROWABLE_TYPE);

	private ThrowableClassTransformer() {
	}

	public byte[] transform(ClassLoader loader, final String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if (Throwable.class != classBeingRedefined) {
			return classfileBuffer;
		}
		try {
			ClassReader cr = new ClassReader(classfileBuffer);
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES
					| ClassWriter.COMPUTE_MAXS);
			ClassVisitor cv = cw;
			// cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
			cv = new ClassVisitor(ASM4, cv) {
				@Override
				public MethodVisitor visitMethod(int access, String name,
						String desc, String signature, String[] exceptions) {
					// if ("<init>".equals(name)) {
					// // constructor method
					// MethodVisitor mv = super.visitMethod(access, name,
					// desc, signature, exceptions);
					// return new MethodVisitor(api, mv) {
					//
					// @Override
					// public void visitMethodInsn(int opcode,
					// String owner, String name, String desc) {
					// if (opcode == INVOKEVIRTUAL
					// && owner.equals(THROWABLE_INTERNAL_NAME)
					// && name.equals(FILL_IN_STACK_TRACE_METHOD_NAME)
					// && desc.equals(FILL_IN_STACK_TRACE_METHOD_DESC)) {
					// super.visitMethodInsn(opcode, owner, name,
					// desc);
					// super.visitVarInsn(ALOAD, 0);
					// super.visitMethodInsn(
					// INVOKESTATIC,
					// THROWABLE_SOURCE_HOLDER_INTERNAL_NAME,
					// SET_STACK_TRACE_METHOD_NAME,
					// SET_STACK_TRACE_METHOD_DESC);
					// } else {
					// super.visitMethodInsn(opcode, owner, name,
					// desc);
					// }
					// }
					//
					// };
					// }
					if ("toString".equals(name) && (ACC_PUBLIC & access) != 0
							&& "()Ljava/lang/String;".equals(desc)) {
						MethodVisitor mv = super.visitMethod(access, name,
								desc, signature, exceptions);
						return new MethodVisitor(api, mv) {

							@Override
							public void visitInsn(int opcode) {
								if (opcode == ARETURN) {
									super.visitVarInsn(ALOAD, 0);
									super.visitMethodInsn(
											INVOKESTATIC,
											THROWABLE_SOURCE_HOLDER_INTERNAL_NAME,
											"throwableToString",
											"(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/lang/String;");
									super.visitInsn(opcode);
								} else {
									super.visitInsn(opcode);
								}
							}
						};
					}
					if ("printStackTrace".equals(name)
							&& (ACC_PUBLIC & access) != 0
							&& ("(Ljava/io/PrintStream;)V".equals(desc) || "(Ljava/io/PrintWriter;)V"
									.equals(desc))) {
						MethodVisitor mv = super.visitMethod(access, name,
								desc, signature, exceptions);
						return new AdviceAdapter(api, mv, access, name, desc) {

							@Override
							protected void onMethodEnter() {
								visitMethodInsn(INVOKESTATIC,
										THROWABLE_SOURCE_HOLDER_INTERNAL_NAME,
										"enterPrintStackTrace", "()V");
							}

							@Override
							protected void onMethodExit(int opcode) {
								visitMethodInsn(INVOKESTATIC,
										THROWABLE_SOURCE_HOLDER_INTERNAL_NAME,
										"exitPrintStackTrace", "()V");
							}

						};
					}
					return super.visitMethod(access, name, desc, signature,
							exceptions);
				}
			};
			cr.accept(cv, 0);
			System.out.println("transform: " + className);
			return cw.toByteArray();
		} catch (Throwable th) {
			th.printStackTrace();
			return classfileBuffer;
		}
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		String className = ThrowableSourceHolder.class.getName().replace('.',
				'/')
				+ ".class";
		Enumeration<URL> resources;
		try {
			resources = ThrowableSourceHolder.class.getClassLoader()
					.getResources(className);
		} catch (IOException e) {
			System.err.println("Fail to append bootstrap path due to " + e);
			return;
		}
		boolean added = false;
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			if (isJarUrl(url)) {
				try {
					File bootstrapFile = new File(
							extractBaseURL(url, className).toURI());
					inst.appendToBootstrapClassLoaderSearch(new JarFile(
							bootstrapFile));
					System.out.println("Append to bootstrap loader: "
							+ bootstrapFile);
					added = true;
				} catch (Exception e) {
					System.err.println("Fail to append bootstrap path due to "
							+ e);
					return;
				}
			}
		}
		if (!added) {
			System.err
					.println("Fail to append bootstrap path due to no boot jar file detected");
			return;
		}
		inst.addTransformer(new ThrowableClassTransformer(), true);
		try {
			inst.retransformClasses(Throwable.class);
		} catch (UnmodifiableClassException e) {
			System.err.println(Throwable.class + " cannot be retransfromed");
		}
	}

	private static URL extractJarURL(URL fullURL) {
		String fullString = fullURL.getFile();
		if (fullString != null && fullString.endsWith("!/")) {
			try {
				return new URL(fullString.substring(0, fullString.length() - 2));
			} catch (MalformedURLException ignored) {
			}
		}
		return fullURL;
	}

	private static URL extractParentURL(URL fullURL, String name) {
		String fullFilePath = fullURL.getFile();
		if (fullFilePath.endsWith(name)) {
			boolean absolutePath = name.startsWith("/");
			try {
				return new URL(fullURL.getProtocol(), fullURL.getHost(),
						fullURL.getPort(), fullFilePath.substring(0,
								fullFilePath.length() - name.length()
										+ (absolutePath ? 1 : 0)));
			} catch (MalformedURLException ignored) {
			}
		}
		return fullURL;
	}

	private static URL extractBaseURL(URL url, String name) {
		URL resourceBaseURL = extractParentURL(url, name);
		if (isJarUrl(url)) {
			resourceBaseURL = extractJarURL(resourceBaseURL);
		}
		return resourceBaseURL;
	}

	private static boolean isJarUrl(URL url) {
		return "jar".equals(url.getProtocol());
	}

}
