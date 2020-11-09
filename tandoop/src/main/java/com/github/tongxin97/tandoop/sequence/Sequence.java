package com.github.tongxin97.tandoop.sequence;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import java.lang.IllegalArgumentException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.*;

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
		System.out.printf("Added val %s of type %s\n", this.Vals.get(type), type);
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
		return "\nSequence: \n" + this.ExcSeq;
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
		return this.Vals.containsKey(type);
	}

	/**
	 * Generate a test and writes it to testDir/TandoopTest.java
	 * Add contract checking into the test itself.
	 */
	public void generateTest() throws Exception {
		StringBuilder testString = new StringBuilder("");
		testString.append("import com.google.gson.Gson;\n");
		for (String s: this.Imports) {
			testString.append(s);
		}
		testString.append("\npublic class TandoopTest {\n");
		testString.append("  public static String test() {\n");
		testString.append("    try {\n");
		testString.append(this.ExcSeq);
		testString.append("      try {\n");
		testString.append("        assert(" + this.NewVar + ".equals(" + this.NewVar + "));\n");
		testString.append("        " + this.NewVar + ".hashCode();\n");
		testString.append("        " + this.NewVar + ".toString();\n");
		testString.append("      } catch (Exception e) { return \"C: \" + e; }\n");
		testString.append("      if (null == " + this.NewVar + ") { return \"F: null\"; }\n");
		testString.append("      return new Gson().toJson(" + this.NewVar + ");\n");
		testString.append("    }\n");
		testString.append("    catch (AssertionError e) { return \"C: \" + e; }");
		if (!InputParamsWithNull) {
			testString.append("    catch (NullPointerException e) { return \"C\" + e; }\n");
		}
		testString.append("    catch (Exception e) { return \"F: \" + e; }\n");
		testString.append("  }\n");
		testString.append("}");

		try {
			String filename = "src/test/java/com/github/tongxin97/tandoop/TandoopTest.java";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(testString.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public String runTest() throws Exception {
		try {
			String cmd = "javac -cp 'target/dependency/*':target/classes -d target/test-classes src/test/java/com/github/tongxin97/tandoop/TandoopTest.java";
			Process p = Runtime.getRuntime().exec(new String[] {"bash", "-c", cmd});

			String s;
//			BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            while ((s = out.readLine()) != null) {
//				System.out.println("line: " + s);
//			}
//			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//            while ((s = err.readLine()) != null) {
//				System.out.println("line: " + s);
//			}
		
			int cmdReturnValue = p.waitFor();
			System.out.println("javacompile: " + cmdReturnValue);
			assert(cmdReturnValue == 0);

			URLClassLoader classLoader = new URLClassLoader(new URL[]{ new File("target/test-classes/").toURI().toURL() }, this.getClass().getClassLoader());
			Class testClass = Class.forName("TandoopTest", false, classLoader);
			
			Method method = testClass.getMethod("test");
			Object result = method.invoke(null);
			System.out.println("Result: " + result);
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "E: " + e;
		}
	}
}
