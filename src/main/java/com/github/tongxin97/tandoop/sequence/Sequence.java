package com.github.tongxin97.tandoop.sequence;

import java.lang.IllegalArgumentException;
import java.lang.reflect.Method;
import java.util.*;
import java.io.*;

import com.github.tongxin97.tandoop.util.ClassUtils;
import com.github.tongxin97.tandoop.util.Rand;
import com.github.tongxin97.tandoop.value.ValueInfo;
import com.github.tongxin97.tandoop.Tandoop;
import com.github.tongxin97.tandoop.CoverageAnalyzer;
import java.net.URLClassLoader;
import java.net.URL;
import com.github.tongxin97.tandoop.value.VarInfo;

/**
 * The class that stores a sequence
 *
 * @author Tangrizzly tongxin97
 * on 2020/10/5
 */

public class Sequence {
	// map type to list of extensible vals
	public LinkedHashSet<String> statements;
	public Map<String, List<ValueInfo>> Vals;
	public String ExcSeq;
	public VarInfo NewVar;
	public boolean InputParamsWithNull;
	public Set<String> genericTypes;

	public Sequence() {
		this.Vals = new HashMap<>();
		this.statements = new LinkedHashSet<>();
		this.ExcSeq = "";
		this.InputParamsWithNull = false;
		genericTypes = new HashSet<>();
	}

	private boolean hasGenericTypes() {
		return genericTypes.size() > 0;
	}

	public void addStatements(Sequence seq) {
		for (String s: seq.statements) {
			addStatement(s);
		}
	}

	public void addStatement(String s) {
		if (statements.contains(s)) {
			return;
		}
		statements.add(s);
		ExcSeq += s;
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

	public ValueInfo getRandomExtensibleValOfType(String type, final Map<String, Set<String>> inheritanceMap, int matchType) throws IllegalArgumentException {
		Set<String> availableTypes = new HashSet<>(Vals.keySet());
		switch (matchType) {
			case 0:
				return Rand.getRandomCollectionElement(this.Vals.get(type));
			case 1:
				availableTypes.retainAll(inheritanceMap.get(type));
				String subType = Rand.getRandomCollectionElement(availableTypes);
				return Rand.getRandomCollectionElement(this.Vals.get(subType));
			case 2:
				Set<String> subColTypes = ClassUtils.getSubCollectionsTypes(type, inheritanceMap);
				availableTypes.retainAll(subColTypes);
				String subColType = Rand.getRandomCollectionElement(availableTypes);
				return Rand.getRandomCollectionElement(this.Vals.get(subColType));
			default:
				throw new IllegalArgumentException("Sequence doesn't have extensible values of type " + type);
		}
	}

	/**
	 * Checks whether this sequence contains values of the given type or its subtype/sub-collection-type.
	 * @param type target type
	 * @param inheritanceMap
	 * @param useStrictType
	 * @return 0 if exact match on type; 1 if subtype match; 2 if sub-collection-type match; -1 if no match.
	 */
	public int hasExtensibleValOfType(String type, final Map<String, Set<String>> inheritanceMap, boolean useStrictType) {
		// exact match
		if (this.Vals.containsKey(type)) {
			return 0;
		}
		if (!useStrictType) {
			// match a subtype
			Set<String> subTypes = inheritanceMap.get(type);
			if (subTypes != null && !Collections.disjoint(this.Vals.keySet(), subTypes)) {
				return 1;
			}
			// match a sub-collection-type
			Set<String> subColTypes = ClassUtils.getSubCollectionsTypes(type, inheritanceMap);
			if (subColTypes!= null && !Collections.disjoint(this.Vals.keySet(), subColTypes)) {
				return 2;
			}
		}
		return -1;
	}

	private void appendTestClassHeader(StringBuilder testString, String testClassName) {
		testString.append("\npublic class " +  testClassName);
		if (hasGenericTypes()) {
			testString.append("<");
			for (String g: genericTypes) {
				testString.append(g + ",");
			}
			testString.replace(testString.length()-1, testString.length(), ">");
		}
		testString.append(" {\n");
	}

	/**
	 * Generate a test and writes it to testDir/TandoopTest.java
	 * Add contract checking into the test itself.
	 */
	public void generateTest() {
		StringBuilder testString = new StringBuilder("");
		appendTestClassHeader(testString, "TandoopTest");
		testString.append("  public Object test() {\n");
		testString.append("    try {\n");
		testString.append(this.ExcSeq);
		String newVarContent = NewVar.getContent();
		if (newVarContent != "" && !NewVar.hasPrimitiveType()) {
			testString.append("      if (" + newVarContent + " == null) { return \"[Tandoop] F: null\"; }\n");
			testString.append("      try {\n");
			testString.append("        assert(" + newVarContent + ".equals(" + newVarContent + "));\n");
			testString.append("        " + newVarContent + ".hashCode();\n");
			testString.append("        " + newVarContent + ".toString();\n");
			testString.append("      } catch (Exception e) { return \"[Tandoop] C: \" + e; }\n");
		}
		testString.append("      return " + newVarContent+ ";\n");
		testString.append("    }\n");
		testString.append("    catch (AssertionError e) { return \"[Tandoop] C: \" + e; }\n");
		if (!InputParamsWithNull) {
			testString.append("    catch (NullPointerException e) { return \"[Tandoop] C: \" + e; }\n");
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
		testString.append("import static org.junit.Assert.fail;\n");
		appendTestClassHeader(testString, testClass);
		testString.append("  @Test\n  public void test() {\n    try {\n");
		testString.append(this.ExcSeq);
		String newVarContent = NewVar.getContent();
		if (newVarContent != "" && !NewVar.hasPrimitiveType()) {
			testString.append("      if (" + newVarContent + " == null) { System.out.println(\"" + newVarContent + " is " +
					"null.\\n\"); return; }\n");
			testString.append("      try {\n");
			testString.append("      	assertTrue(" + newVarContent + ".equals(" + newVarContent + "));\n");
			testString.append("      	" + newVarContent + ".hashCode();\n");
			testString.append("      	" + newVarContent + ".toString();\n");
			testString.append("      } catch (Exception e) { e.printStackTrace(); fail(e.getMessage()); } \n");
		}
		testString.append("   }\n");
		testString.append("    catch (AssertionError e) { e.printStackTrace(); fail(e.getMessage()); }\n");
		if (!this.InputParamsWithNull) {
			testString.append("    catch (NullPointerException e) { e.printStackTrace(); fail(e.getMessage()); }\n");
		}
		testString.append("    catch (Exception e) { e.printStackTrace(); }\n");
		testString.append("  }\n");
		testString.append("}");

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

	public Object runTest(String prjDir, CoverageAnalyzer coverageAnalyzer) throws Exception {
		try {
			String cmd = "javac -cp '" + prjDir + "/target/dependency/*':" + prjDir + "/target/classes:target/dependency/gson-2.8.6.jar -d target/test-classes src/test/java/com/github/tongxin97/tandoop/TandoopTest.java";
			// System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(new String[] {"bash", "-c", cmd});

			String s;
			BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
           	while ((s = out.readLine()) != null) {
				System.out.println("line: " + s);
			}
			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
           	while ((s = err.readLine()) != null) {
				System.out.println("line: " + s);
			}

			int cmdReturnValue = p.waitFor();
			System.out.println("javacompile: " + cmdReturnValue);
			if (cmdReturnValue != 0) {
				return "[Tandoop] E: java compiler error";
			}
			
			URLClassLoader classLoader = new URLClassLoader(new URL[]{ new File("target/test-classes/").toURI().toURL() }, this.getClass().getClassLoader());
			Class testClass = Class.forName("TandoopTest", false, classLoader);
			Method method = testClass.getMethod("test");
			try {
				Object result = method.invoke(testClass.getConstructor().newInstance());
				// System.out.println("Result: " + result.toString());
				return result;
			} catch (Exception e) {
				System.out.println("Wrapper invoke exception: " + e);
				System.out.println("Underlying invoke exception: " + e.getCause());
				e.printStackTrace();
				return "[Tandoop] E: " + e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "[Tandoop] E: " + e;
		}
	}
}
