package com.github.tongxin97.tandoop.sequence;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.github.tongxin97.tandoop.method.MethodInfo;
import com.github.tongxin97.tandoop.util.Rand;
import com.github.tongxin97.tandoop.value.ValueInfo;

/**
 * The class that stores a sequence
 *
 * @author Tangrizzly tongxin97
 * on 2020/10/5
 */

public class Sequence {
	public List<MethodInfo> Methods;
	// map type to list(list of extensible vals, list of non-extensible vals))
	public Map<String, List<List<ValueInfo>>> Vals;
	public String ExcSeq;
	public String newVar = "";

	public Sequence() {
		this.Methods = new ArrayList<>();
		this.Vals = new HashMap<>();
		this.ExcSeq = "";
	}

	public void addVal(String type, ValueInfo v) {
		int idx = v.Extensible? 0: 1;
		if (!this.Vals.containsKey(type)) {
			this.Vals.put(type, new ArrayList<>());
			for (int i = 0; i < 2; i++) { // init extensible/non-extensible lists
				this.Vals.get(type).add(new ArrayList<>());
			}
		}
		this.Vals.get(type).get(idx).add(v);
	}

	public void addVals(String type, List<ValueInfo> vals) {
		if (vals.size() == 0) {
			return;
		}
		int idx = vals.get(0).Extensible? 0: 1;
		if (!this.Vals.containsKey(type)) {
			this.Vals.put(type, new ArrayList<>());
			for (int i = 0; i < 2; i++) { // init extensible/non-extensible lists
				this.Vals.get(type).add(new ArrayList<>());
			}
		}
		this.Vals.get(type).get(idx).addAll(vals);
	}

	@Override
	public String toString() {
		return this.ExcSeq;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return this.ExcSeq.equals(((Sequence)obj).ExcSeq);
	}

	@Override
	public int hashCode() {
		return this.ExcSeq.hashCode();
	}

	public ValueInfo getRandomExtensibleValOfType(String type) throws IllegalArgumentException {
		if (!this.hasExtensibleValOfType(type)) {
			throw new IllegalArgumentException("Sequence doesn't have extensible values of type " + type);
		}
		List<ValueInfo> l = this.Vals.get(type).get(0);
		return l.get(Rand.getRandomInt(l.size()));
	}

	public boolean hasExtensibleValOfType(String type) {
		return this.Vals.containsKey(type) && this.Vals.get(type).get(0) != null;
	}

	public void generateTest(String pkgName, String testDir) throws Exception {
		String preTestString = "package " + pkgName + ";\n\n"
			+ "import static org.junit.Assert.assertEquals;\n"
			+ "import org.junit.Test;\n"
			+ "\n"
			+ "public class TandoopTest {\n"
			+ "  @Test\n"
			+ "  public void test() {\n"
			+ "    ";
		String contracts = "";
		if (this.newVar != "") {
			contracts = "    boolean r = " + this.newVar +  ".equals(" + this.newVar + ")\n"
				+ "    if (r == false) { return false; }";
		}
		String postTestString = "  }\n"
			+ "}";
		String testString = preTestString + this.ExcSeq + contracts + postTestString;
		String filename = testDir + "/TandoopTest.java";
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write(testString);
		writer.close();
	}

	public int runTest(String prjDir) throws Exception {
		int returnVal = 1;
		try {
			String cmd = "cd " + prjDir + "; mvn test -Dtest=TandoopTest";
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s;
            while ((s = br.readLine()) != null)
                System.out.println("line: " + s);
			p.waitFor();
			returnVal = p.exitValue();
			// System.out.println("Test exit value: " + p.exitValue());
			p.destroy();
		} catch (Exception e) {}
		return returnVal;
	}
}
