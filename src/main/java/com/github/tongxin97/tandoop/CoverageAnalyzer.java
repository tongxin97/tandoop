package com.github.tongxin97.tandoop;

import java.io.*;
import java.util.Set;
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
	public InstrumentedClassLoader classLoader;
	public Instrumenter instr;
	private ExecutionDataStore executionData;
	private SessionInfoStore sessionInfos;
	private RuntimeData data;
	private IRuntime runtime;
	private String prefix;
    
	public CoverageAnalyzer(String prjDir, PrintStream out, URLClassLoader classLoader) throws Exception{
        this.out = out;
		this.targetClasses = new File(prjDir + "/target/classes");
		this.classLoader = new InstrumentedClassLoader((ClassLoader) classLoader);

        // For instrumentation and runtime we need a IRuntime instance to collect execution data
		this.runtime = new LoggerRuntime();

		this.instr = new Instrumenter(runtime);

		this.prefix = prjDir + "/target/classes/";
		loadTargetClasses(this.targetClasses);
		
		this.data = new RuntimeData();
		this.runtime.startup(data);
		
		this.executionData = new ExecutionDataStore();
		this.sessionInfos = new SessionInfoStore();
	}

	private void loadTargetClasses(File dir) throws Exception {
		// walk through dir to find all .class file
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				loadTargetClasses(file); 
			} else {
                if (file.getName().endsWith(".class")) {
					InputStream original = new FileInputStream(file);
					String className = file.getPath().replaceFirst(prefix, "").replace("/", ".").split(".class")[0];
					byte[] instrumented = instr.instrument(original, className);
					original.close();
					// System.out.println(className);
					this.classLoader.addDefinition(className, instrumented);
        	    }
			}
		}
	}

	// At the end of test execution we collect execution data and shutdown the runtime
	public void collect() throws Exception {
		data.collect(this.executionData, this.sessionInfos, false);
	}

	public void end() throws Exception {
        runtime.shutdown();

		// Together with the original class definition we can calculate coverage information
		CoverageBuilder coverageBuilder = new CoverageBuilder();
		Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeAll(targetClasses);
        
        printCoverageInfo(coverageBuilder);
	}
    
    private void printCoverageInfo(CoverageBuilder coverageBuilder) {
		int instruction_total = 0;
		int instruction_missed = 0;
		int branch_total = 0;
		int branch_missed = 0;
		int line_total = 0;
		int line_missed = 0;
		int method_total = 0;
		int method_missed = 0;
        // Let's dump some metrics and line coverage information:
		for (IClassCoverage cc : coverageBuilder.getClasses()) {
			// out.printf("Coverage of class %s%n", cc.getName());

			// printCounter("instructions", cc.getInstructionCounter());
			// printCounter("branches", cc.getBranchCounter());
			// printCounter("lines", cc.getLineCounter());
			// printCounter("methods", cc.getMethodCounter());
			// printCounter("complexity", cc.getComplexityCounter());

			instruction_missed += cc.getInstructionCounter().getMissedCount();
			instruction_total += cc.getInstructionCounter().getTotalCount();
			
			branch_missed += cc.getBranchCounter().getMissedCount();
			branch_total += cc.getBranchCounter().getTotalCount();

			line_missed += cc.getLineCounter().getMissedCount();
			line_total += cc.getLineCounter().getTotalCount();

			method_missed += cc.getMethodCounter().getMissedCount();
			method_total += cc.getMethodCounter().getTotalCount();
		}

		out.printf("instruction: missed %d, total %d, coverage %f\n", instruction_missed, instruction_total, (1 - 1.0 * instruction_missed / instruction_total) * 100);
		out.printf("branch: missed %d, total %d, coverage %f\n", branch_missed, branch_total, (1 - 1.0 * branch_missed / branch_total) * 100);
		out.printf("line: missed %d, total %d, coverage %f\n", line_missed, line_total, (1 - 1.0 * line_missed / line_total) * 100);
		out.printf("method: missed %d, total %d, coverage %f\n\n", method_missed, method_total, (1 - 1.0 * method_missed / method_total) * 100);
		out.close();
    }

    private void printCounter(String unit, ICounter counter) {
		Integer missed = Integer.valueOf(counter.getMissedCount());
		Integer total = Integer.valueOf(counter.getTotalCount());
		out.printf("%s of %s %s missed%n", missed, total, unit);
	}
}