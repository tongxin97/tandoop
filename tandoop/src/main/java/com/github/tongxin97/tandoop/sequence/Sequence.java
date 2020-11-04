package com.github.tongxin97.tandoop.sequence;


import java.io.*;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
	public Set<MethodInfo> Methods;
	// map type to list of extensible vals
	public Map<String, List<ValueInfo>> Vals;
	public String ExcSeq;
	public String NewVar;
	public Set<String> Imports;
	public boolean InputParamsWithNull;

	public Sequence() {
		this.Methods = new HashSet<>();
		this.Vals = new HashMap<>();
		this.ExcSeq = "";
		this.NewVar = "";
		this.Imports = new HashSet<>();
		this.InputParamsWithNull = false;
	}

	public void addVal(String type, ValueInfo v) {
		if (!this.Vals.containsKey(type)) {
			this.Vals.put(type, new ArrayList<>());
		}
		this.Vals.get(type).add(v);
	}

	public void addVals(String type, List<ValueInfo> vals) {
		if (vals.size() == 0) {
			return;
		}
		if (!this.Vals.containsKey(type)) {
			this.Vals.put(type, new ArrayList<>());
		}
		this.Vals.get(type).addAll(vals);
	}

	public void addMethod(MethodInfo method) {
		this.Methods.add(method);
	}

	public void addMethods(Set<MethodInfo> methods) {
		this.Methods.addAll(methods);
	}

	public void addImport(String newImport) {
		this.Imports.add(newImport);
	}

	public void addImports(Set<String> oldImports) {
		this.Imports.addAll(oldImports);
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
		List<ValueInfo> l = this.Vals.get(type);
		return l.get(Rand.getRandomInt(l.size()));
	}

	public boolean hasExtensibleValOfType(String type) {
		return this.Vals.containsKey(type) && this.Vals.get(type).get(0) != null;
	}

	/**
	 * Generate a test and writes it to testDir/TandoopTest.java
	 * Add contract checking into the test itself.
	 */
	public void generateTest(String testDir) throws Exception {
		StringBuilder testString = new StringBuilder("import org.junit.Test;\n");
		testString.append("import static org.junit.Assert.*;\n");
		testString.append("import java.io.*;\n");
		testString.append("import com.google.gson.Gson;\n");

		for (String s: this.Imports) {
			testString.append(s);
		}
		testString.append("\npublic class TandoopTest {\n  @Test\n  public void test() {\n");
		testString.append("    boolean extensible = true;\n");
		testString.append("    String output = \"\";\n");
		testString.append("    try {\n");
		testString.append("      try {\n");
		testString.append(this.ExcSeq);
		testString.append("\n");
		testString.append("        try {\n");
		testString.append("          assertTrue(" + this.NewVar + ".equals(" + this.NewVar + "));\n");
		testString.append("          " + this.NewVar + ".hashCode();\n");
		testString.append("          " + this.NewVar + ".toString();\n");
		testString.append("        } catch (Exception e) {\n");
		testString.append("					 System.err.println(\"e1: \" + e);\n");
		testString.append("					 fail();");
		testString.append("				 }\n");
		testString.append("        output = new Gson().toJson(" + this.NewVar + ");\n");
		testString.append("        System.out.println(output);\n");
		testString.append("      } catch (Throwable t) { \n");
		testString.append("        System.err.println(\"e2: \" + t);\n");
		testString.append("        extensible = false;\n");
		testString.append("      }\n");
		testString.append("      File f = new File(\"../tandoop/testOutput.json\");\n");
		testString.append("      f.createNewFile();\n");
		testString.append("      if (!extensible) { return; }\n");
		testString.append("			PrintWriter p = new PrintWriter(new FileWriter(f));\n");
		testString.append("      p.write(output);\n");
		testString.append("      p.close();\n");
		// testString.append("      f.close();\n");
		testString.append("    } catch (Exception e) { System.err.println(\"e3: \" + e);}\n");
		testString.append("  }\n");
		testString.append("}");

		String filename = testDir + "/TandoopTest.java";
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write(testString.toString());
		writer.close();
	}

	public int runTest(String prjDir) throws Exception {
		int returnVal = 1;
		try {
			String cmd = "mvn test -Dtest=TandoopTest";
			Process p = Runtime.getRuntime().exec(cmd, null, new File(prjDir));
			// BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// String s;
        	// while ((s = out.readLine()) != null) {
			// 	System.out.println("line: " + s);
			// }
			// while ((s = err.readLine()) != null) {
			// 	System.out.println("line: " + s);
			// }
			returnVal = p.waitFor();
		} catch (Exception e) {
			System.out.println(e);
		}
		return returnVal;
	}
}
