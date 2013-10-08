package com.hp.ts.perf.agent.expsrc.shared;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class JavaSourceReader {

	private StackTraceElement frame;

	private static File jdkSrcZipFile = getSrcZipFile();

	private static final String[] DefaultSourceFolder = new String[] { "src",
			"src/main/java", "src/test/java", "." };

	public JavaSourceReader(StackTraceElement frame) {
		this.frame = frame;
	}

	public String loadCode() {
		String sourceFile = getSourceFile();
		if (sourceFile == null) {
			return null;
		}
		// try jdk source first
		if (jdkSrcZipFile != null) {
			ZipFile jdkSrcZip = null;
			try {
				jdkSrcZip = new ZipFile(jdkSrcZipFile);
				ZipEntry zipEntry = jdkSrcZip.getEntry(sourceFile);
				if (zipEntry != null) {
					return readCode(jdkSrcZip.getInputStream(zipEntry),
							frame.getLineNumber(), sourceFile,
							frame.getMethodName());
				}
			} catch (IOException e) {
				// TODO debug
				e.printStackTrace();
			} finally {
				if (jdkSrcZip != null) {
					try {
						jdkSrcZip.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
		// try default file structure
		for (String folder : DefaultSourceFolder) {
			File file = new File(folder, sourceFile);
			if (file.exists()) {
				FileInputStream fileInput = null;
				try {
					fileInput = new FileInputStream(file);
					return readCode(fileInput, frame.getLineNumber(),
							sourceFile, frame.getMethodName());
				} catch (IOException e) {
					// TODO debug
					e.printStackTrace();
				} finally {
					close(fileInput);
				}
			}
		}
		return null;
	}

	private static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException ignored) {
			}
		}
	}

	private String readCode(InputStream inputStream, int lineNumber,
			String sourceFile, String methodName) throws IOException {
		int startLine = Math.max(1, lineNumber - 5);
		int endLine = lineNumber + 1;
		int lineNo = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = "";
		for (int i = 1; i <= startLine; i++) {
			line = reader.readLine();
			lineNo++;
			if (line == null) {
				// finished earlier
				return null;
			}
		}
		StringBuilder builder = new StringBuilder();
		boolean methodHead = false;
		for (int i = startLine; i <= endLine; i++) {
			if (line != null) {
				builder.append("\n\t").append(lineNo)
						.append(lineNumber == lineNo ? "> " : ": ")
						.append(line);
			}
			lineNo++;
			line = reader.readLine();
			if (line == null) {
				endLine = i;
				break;
			}
			if (!methodHead && line.indexOf(methodName) >= 0) {
				// not accurate, especially for constructor
				builder = new StringBuilder();
				methodHead = true;
				startLine = i + 1;
			}
		}
		return String.format("\t[Source from %s, Line %s-%s, Method '%s']%s",
				sourceFile, startLine, endLine, methodName, builder);
	}

	private String getSourceFile() {
		String fileName = frame.getFileName();
		if (fileName == null || frame.getLineNumber() <= 0) {
			return null;
		}
		String className = frame.getClassName();
		int packageIndex = className.lastIndexOf('.');
		if (packageIndex > 0) {
			className = className.substring(packageIndex + 1);
		}
		if (fileName.endsWith(".java")) {
			return frame.getClassName().substring(0, packageIndex)
					.replace('.', '/')
					+ "/" + fileName;
		} else {
			return null;
		}
	}

	private static File getSrcZipFile() {
		File javaHome = new File(System.getProperty("java.home"));
		File srcZipFile = new File(javaHome, "src.zip");
		if (!srcZipFile.exists()) {
			srcZipFile = new File(javaHome.getParentFile(), "src.zip");
		}
		if (!srcZipFile.exists()) {
			return null;
		} else {
			return srcZipFile;
		}
	}

}
