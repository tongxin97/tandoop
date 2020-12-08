package com.github.tongxin97.tandoop;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.agent.rt.RT;

public class CoverageAnalyzer {
	private File targetClasses;
	private ExecutionDataStore executionData;

	/**
   * Map from method name to uncovered branch ratio (in Jacoco terms, the "missed ratio"). In cases
   * where a method's total branches is zero, the uncovered branch ratio is NaN, and this map uses
   * zero instead.
   */
  public Map<String, Double> branchCoverageMap = new HashMap<>();

	public CoverageAnalyzer(String prjDir) throws Exception {
		this.targetClasses = new File(prjDir + "/target/classes");
		this.executionData = new ExecutionDataStore();
	}

	public void printCoverageInfo(PrintStream out) throws Exception {
		CoverageBuilder coverageBuilder = collectCoverage();
		int[] converageInfo = getCoverageInfo(coverageBuilder);
		printCoverageInfo(out, converageInfo);
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
    
  private void printCoverageInfo(PrintStream out, int[] coverageInfo) {
	  out.printf("instruction: covered %d, total %d, coverage %f\n", coverageInfo[0], coverageInfo[1], 100.0 * coverageInfo[0] / coverageInfo[1]);
	  out.printf("branch: covered %d, total %d, coverage %f\n", coverageInfo[2], coverageInfo[3], 100.0 * coverageInfo[2] / coverageInfo[3]);
	  out.printf("line: covered %d, total %d, coverage %f\n", coverageInfo[4], coverageInfo[5], 100.0 * coverageInfo[4] / coverageInfo[5]);
	  out.printf("method: covered %d, total %d, coverage %f\n\n", coverageInfo[6], coverageInfo[7], 100.0 * coverageInfo[6] / coverageInfo[7]);
	}
	
	public CoverageBuilder collectCoverage() throws IOException {
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
		this.updateBranchCoverageMap(coverageBuilder);
		return coverageBuilder;
	}

	private void updateBranchCoverageMap(CoverageBuilder coverageBuilder) {
    // For each method under test, copy its branch coverage information from the coverageBuilder to
    // branchCoverageMap.
    // Sorting is to make diagnostic output deterministic.
    ArrayList<IClassCoverage> classes = new ArrayList<>(coverageBuilder.getClasses());
    for (IClassCoverage cc : classes) {
      ArrayList<IMethodCoverage> methods = new ArrayList<>(cc.getMethods());
      for (IMethodCoverage cm : methods) {
        // cc is in internal form because Jacoco uses class names in internal form.
//        @SuppressWarnings("signature") // Jacoco is not annotated
//        @InternalForm
				String methodName = cc.getName().replace("/", ".");
				// Randoop uses fully-qualified class names, with only periods as delimiters.
				if (!cm.getName().equals("<init>")) {
					methodName += "." + cm.getName();
				}
				
        // In cases where a method's total branches is zero, the Jacoco missed ratio is NaN,
        // but use zero as the uncovRatio instead.
        double uncovRatio = cm.getBranchCounter().getMissedRatio();
        uncovRatio = Double.isNaN(uncovRatio) ? 0 : uncovRatio;
        branchCoverageMap.put(methodName, uncovRatio);
      }
    }
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