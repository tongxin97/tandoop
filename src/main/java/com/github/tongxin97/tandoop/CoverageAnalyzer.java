package com.github.tongxin97.tandoop;

import java.io.*;
import java.net.URLClassLoader;

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
    
	public CoverageAnalyzer(String prjDir, PrintStream out, URLClassLoader classLoader) throws Exception {
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
		int instruction_covered = 0;
		int branch_total = 0;
		int branch_covered = 0;
		int line_total = 0;
		int line_covered = 0;
		int method_total = 0;
		int method_covered = 0;
    // Let's dump some metrics and line coverage information:
		for (IClassCoverage cc : coverageBuilder.getClasses()) {

			instruction_covered += cc.getInstructionCounter().getCoveredCount();
			instruction_total += cc.getInstructionCounter().getTotalCount();
			
			branch_covered += cc.getBranchCounter().getCoveredCount();
			branch_total += cc.getBranchCounter().getTotalCount();

			line_covered += cc.getLineCounter().getCoveredCount();
			line_total += cc.getLineCounter().getTotalCount();

			method_covered += cc.getMethodCounter().getCoveredCount();
			method_total += cc.getMethodCounter().getTotalCount();
		}

		out.printf("instruction: covered %d, total %d, coverage %f\n", instruction_covered, instruction_total, 100.0 * instruction_covered / instruction_total);
		out.printf("branch: covered %d, total %d, coverage %f\n", branch_covered, branch_total, 100.0 * branch_covered / branch_total);
		out.printf("line: covered %d, total %d, coverage %f\n", line_covered, line_total, 100.0 * line_covered / line_total);
		out.printf("method: covered %d, total %d, coverage %f\n\n", method_covered, method_total, 100.0 * method_covered / method_total);
		out.close();
  }
}
