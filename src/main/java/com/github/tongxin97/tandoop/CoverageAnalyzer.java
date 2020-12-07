package com.github.tongxin97.tandoop;

import java.io.*;
import java.net.URLClassLoader;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.agent.rt.RT;

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
		printCoverageInfo(this.out, coverageBuilder);
		out.close();
	}

	private int[] getCoverageInfo(CoverageBuilder coverageBuilder) {
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
		return new int[] {instruction_covered, instruction_total,
			branch_covered, branch_total,
			line_covered, line_total,
			method_covered, method_total
		};
	}
    
  private void printCoverageInfo(PrintStream out, CoverageBuilder coverageBuilder) {
		int[] coverageInfo = getCoverageInfo(coverageBuilder);

	  out.printf("instruction: covered %d, total %d, coverage %f\n", coverageInfo[0], coverageInfo[1], 100.0 * coverageInfo[0] / coverageInfo[1]);
	  out.printf("branch: covered %d, total %d, coverage %f\n", coverageInfo[2], coverageInfo[3], 100.0 * coverageInfo[2] / coverageInfo[3]);
	  out.printf("line: covered %d, total %d, coverage %f\n", coverageInfo[4], coverageInfo[5], 100.0 * coverageInfo[4] / coverageInfo[5]);
	  out.printf("method: covered %d, total %d, coverage %f\n\n", coverageInfo[6], coverageInfo[7], 100.0 * coverageInfo[6] / coverageInfo[7]);
	}
	
	public int[] collectCoverageInfo() throws IOException {
		try {
			// Retrieve the execution data from the Jacoco Java agent
			final InputStream execDataStream;
			try {
				execDataStream = new ByteArrayInputStream(RT.getAgent().getExecutionData(false));
			} catch (IllegalStateException e) {
				System.err.println("JaCoCo agent not started. Add '-Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar' to command line argument");
				throw (e);
			}
			final ExecutionDataReader reader = new ExecutionDataReader(execDataStream);

			reader.setSessionInfoVisitor(DummySessionInfoVisitor.instance);
			reader.setExecutionDataVisitor(
				new IExecutionDataVisitor() {
					@Override
					public void visitClassExecution(final ExecutionData data) {
						// Add the execution data for each class into the execution data store.
						executionData.put(data);
					}
				});
			reader.read();
			execDataStream.close();
		} catch (IOException e) {
      System.err.println("Error in Coverage Tracker in collecting coverage information.");
      e.printStackTrace(System.err);
      System.exit(1);
		}

		CoverageBuilder coverageBuilder = new CoverageBuilder();
		Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeAll(targetClasses);
		return getCoverageInfo(coverageBuilder);
	}

  private static class DummySessionInfoVisitor implements ISessionInfoVisitor {
    /** Singleton instance of this class. */
    public static final DummySessionInfoVisitor instance = new DummySessionInfoVisitor();

    /** Initializes the session info visitor. */
    private DummySessionInfoVisitor() {}

    /**
     * Required by the {@link ISessionInfoVisitor} but the session information is not used by this
     * class.
     *
     * @param info session information
     */
    @Override
		public void visitSessionInfo(final SessionInfo info) {}
	}
}