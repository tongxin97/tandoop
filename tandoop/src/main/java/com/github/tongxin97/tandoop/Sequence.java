package com.github.tongxin97.tandoop;


import java.io.BufferedWriter;
import java.io.FileWriter;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * The class that stores a sequence
 *
 * @author Tangrizzly tongxin97
 * on 2020/10/5
 */

public class Sequence {
	public List<MethodInfo> Methods;
	// map type to list(list of extensible vals, list of non-extensible vals))
	public Map<String, List<List<ValInfo>>> Vals;
	public String ExcSeq;

	public Sequence() {
		this.Methods = new ArrayList<>();
		this.Vals = new HashMap<>();
		this.ExcSeq = "";
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

	public ValInfo getRandomExtensibleValOfType(String type) throws IllegalArgumentException {
		if (!this.hasExtensibleValOfType(type)) {
			throw new IllegalArgumentException("Sequence doesn't have extensible values of type " + type);
		}
		List<ValInfo> l = this.Vals.get(type).get(0);
		return l.get(Utils.GetRandomInt(l.size()));
	}

	public boolean hasExtensibleValOfType(String type) {
		return this.Vals.containsKey(type) && this.Vals.get(type).get(0) != null;
	}

	public void generateTest() throws Exception {
			String sequence = "import static org.junit.Assert.assertEquals;\n"
					+ "import org.junit.Test;\n"
					+ "\n"
					+ "public class CalculatorTest {\n"
					+ "  @Test\n"
					+ "  public void evaluatesExpression() {\n"
					+ "    Calculator calculator = new Calculator();\n"
					+ "    int sum = calculator.evaluate(\"1+2+3\");\n"
					+ "    assertEquals(6, sum);\n"
					+ "  }\n"
					+ "}";
			String filename = "../calculator/CalculatorTest.class";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(sequence);
			writer.close();
	}

	public void runTest() {
		/*
		cd ../calculator
		javac Calculator.java
		javac -cp .:junit-4.13.1.jar:hamcrest-core-1.3.jar CalculatorTest.java
		java -cp .:junit-4.13.1.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore CalculatorTest
		*/
	}
}
