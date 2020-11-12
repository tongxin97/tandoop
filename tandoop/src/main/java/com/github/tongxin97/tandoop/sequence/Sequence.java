package com.github.tongxin97.tandoop.sequence;

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
import com.github.tongxin97.tandoop.Tandoop;

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
	public void generateTest() {
		StringBuilder testString = new StringBuilder("");
//		for (String s: this.Imports) {
//			testString.append(s);
//		}
		testString.append("\npublic class TandoopTest {\n");
		testString.append("  public static Object test() {\n");
		testString.append("    try {\n");
		testString.append(this.ExcSeq);
		testString.append("      try {\n");
		testString.append("        assert(" + this.NewVar + ".equals(" + this.NewVar + "));\n");
		testString.append("        " + this.NewVar + ".hashCode();\n");
		testString.append("        " + this.NewVar + ".toString();\n");
		testString.append("      } catch (Exception e) { return \"[Tandoop] C: \" + e; }\n");
		testString.append("      if (null == " + this.NewVar + ") { return \"[Tandoop] F: null\"; }\n");
		testString.append("      return " + this.NewVar + ";\n");
		testString.append("    }\n");
		testString.append("    catch (AssertionError e) { return \"[Tandoop] C: \" + e; }\n");
		if (!InputParamsWithNull) {
			testString.append("    catch (NullPointerException e) { return \"[Tandoop] C\" + e; }\n");
		}
		testString.append("    catch (Exception e) { return \"[Tandoop] F: \" + e; }\n");
		testString.append("  }\n");
		testString.append("}");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(Tandoop.tandoopTestFile));
			writer.write(testString.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateJUnitTest(String testDir, String testClass) {
		StringBuilder testString = new StringBuilder("");
		testString.append("import org.junit.Test;\n");
		testString.append("import static org.junit.Assert.assertTrue;\n");
//		for (String s: this.Imports) {
//			testString.append(s);
//		}
		testString.append("\npublic class " + testClass + "{\n  @Test\n  public void test() {\n    try {\n");

		StringBuilder postTestString = new StringBuilder("");
		postTestString.append("      assertTrue(" + this.NewVar + ".equals(" + this.NewVar + "));\n");
		postTestString.append("      " + this.NewVar + ".hashCode();\n");
		postTestString.append("      " + this.NewVar + ".toString();\n");
		postTestString.append("    }\n");
		if (!this.InputParamsWithNull) {
			postTestString.append("    catch (NullPointerException e) {}\n");
		}
		postTestString.append("    catch (Throwable t) { System.out.println(t.toString()); }\n");
		postTestString.append("  }\n}");

		testString.append(this.ExcSeq);
		testString.append(postTestString);

		try {
			String filename = testDir + testClass + ".java";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(testString.toString());
			writer.close();
		} catch (Exception e) {
			System.err.println("Failed to write junit test: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Object runTest(String prjDir, ClassLoader parentClassLoader) throws Exception {
		try {
			String cmd = "javac -cp '" + prjDir + "/target/dependency/*':" + prjDir + "/target/classes:target/dependency/gson-2.8.6.jar -d target/test-classes src/test/java/com/github/tongxin97/tandoop/TandoopTest.java";
			// System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(new String[] {"bash", "-c", cmd});

//			String s;
//			BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
//           	while ((s = out.readLine()) != null) {
//				System.out.println("line: " + s);
//			}
//			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//           	while ((s = err.readLine()) != null) {
//				System.out.println("line: " + s);
//			}

			int cmdReturnValue = p.waitFor();
			System.out.println("javacompile: " + cmdReturnValue);
			if (cmdReturnValue != 0) {
				return "[Tandoop] E: java compiler error";
			}
			
			URLClassLoader classLoader = new URLClassLoader(new URL[]{ new File("target/test-classes/").toURI().toURL() }, parentClassLoader);
			Class testClass = Class.forName("TandoopTest", false, classLoader);
			
			Method method = testClass.getMethod("test");
			try {
				Object result = method.invoke(null);
				// System.out.println("Result: " + result.toString());
				return result;
			} catch (Exception e) {
				System.out.println("Wrapper invoke exception: " + e);
				System.out.println("Underlying invoke exception: " + e.getCause());
				return "[Tandoop] E: " + e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "[Tandoop] E: " + e;
		}
	}
}
