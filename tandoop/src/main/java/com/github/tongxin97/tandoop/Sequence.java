package com.github.tongxin97.tandoop;


import java.io.BufferedWriter;
import java.io.FileWriter;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The class that stores a sequence
 *
 * @author Tangrizzly tongxin97
 * on 2020/10/5
 */

public class Sequence {
	public List<MethodInfo> Methods;
	public Map<String, List<ReturnVal>> returnVals;

	public Sequence() {
		this.Methods = new ArrayList<>();
		this.returnVals = new HashMap<>();
	}

	public Object getReturnValOfType(String type) {
		if (this.returnVals.containsKey(type)) {
			// get list of extensible values from this.returnVals
			List<Object> extensibleVals = this.returnVals.get(type).stream()
			.filter(v -> v.Extensible)
			.map(v -> v.Val)
			.collect(Collectors.toList());
			return Utils.getRandomInNonEmptyList(extensibleVals);
		}
		return null;
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
