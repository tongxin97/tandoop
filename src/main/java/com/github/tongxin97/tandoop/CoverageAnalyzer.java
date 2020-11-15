package com.github.tongxin97.tandoop;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.net.URLClassLoader;
import java.lang.reflect.Method;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

public class CoverageAnalyzer {
    private PrintStream out;
	private File targetClasses;
	private URLClassLoader classLoader;
	private Set<String> targetClasseNames;
	private Instrumenter instr;
    
	public CoverageAnalyzer(String prjDir, PrintStream out, URLClassLoader classLoader, Set<String> targetClasseNames) {
        this.out = out;
		this.targetClasses = new File(prjDir + "/target/classes");
		this.classLoader = new InstrumentedClassLoader((ClassLoader) classLoader);
		this.targetClasseNames = targetClasseNames;

        // For instrumentation and runtime we need a IRuntime instance to collect execution data
		IRuntime runtime = new LoggerRuntime();

		this.instr = new Instrumenter(runtime);

		loadTargetClasses();
		
		RuntimeData data = new RuntimeData();
        runtime.startup(data);
	}

	private loadTargetClasses() {
		for (String className: this.targetClasseNames) {
			InputStream original = this.classLoader.getTargetClass(className);
			byte[] instrumented = instr.instrument(original, testClassName);
			original.close();
			this.classLoader.addDefinition(className, instrumented);
		}
	}
	
	public static class InstrumentedClassLoader extends ClassLoader {
		private InstrumentedClassLoader(ClassLoader parent) {
			super(parent);
		}

		private Map<String, byte[]> definitions = new HashMap<String, byte[]>();

		public void addDefinition(String name, final byte[] bytes) {
			definitions.put(name, bytes);
		}
		
		protected Class<?> loadClass(String name, byte[] bytes)
				throws ClassNotFoundException {
			definitions.put(name, bytes);
			return defineClass(name, bytes, 0, bytes.length);
		}

		@Override
		protected Class<?> loadClass(final String name, final boolean resolve)
				throws ClassNotFoundException {
			final byte[] bytes = definitions.get(name);
			if (bytes != null) {
				return defineClass(name, bytes, 0, bytes.length);
			}
			return super.loadClass(name, resolve);
		}

		private InputStream getTargetClass(String name) {
			String resource = '/' + name.replace('.', '/') + ".class";
			return getResourceAsStream(resource);
		}
	}

	public void execute() throws Exception {
		String testClassName = TempTest.class.getName();

        // The Instrumenter creates a modified version of our test target class that contains additional probes for execution data recording
        Instrumenter instr = new Instrumenter(runtime);
        InputStream original = getTargetClass(testClassName);
        byte[] instrumented = instr.instrument(original, testClassName);
        original.close();

		Class<?> testClass = new InstrumentedClassLoader((ClassLoader) this.classLoader).loadClass(testClassName, instrumented);

        Method method = testClass.getMethod("test");
        try {
            Object result = method.invoke(null);
            System.out.println("Result: " + result.toString());
        } catch (Exception e) {
            System.out.println(e);
        }

        // At the end of test execution we collect execution data and shutdown the runtime
        ExecutionDataStore executionData = new ExecutionDataStore();
		SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
	}

	public void end() {
        runtime.shutdown();

		// Together with the original class definition we can calculate coverage information
		CoverageBuilder coverageBuilder = new CoverageBuilder();
		Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeAll(targetClasses);
        
        printCoverageInfo(coverageBuilder);
	}
    
    private void printCoverageInfo(CoverageBuilder coverageBuilder) {
        // Let's dump some metrics and line coverage information:
		for (IClassCoverage cc : coverageBuilder.getClasses()) {
			out.printf("Coverage of class %s%n", cc.getName());

			printCounter("instructions", cc.getInstructionCounter());
			printCounter("branches", cc.getBranchCounter());
			printCounter("lines", cc.getLineCounter());
			printCounter("methods", cc.getMethodCounter());
			printCounter("complexity", cc.getComplexityCounter());
		}
    }

    private void printCounter(String unit, ICounter counter) {
		Integer missed = Integer.valueOf(counter.getMissedCount());
		Integer total = Integer.valueOf(counter.getTotalCount());
		out.printf("%s of %s %s missed%n", missed, total, unit);
	}
}