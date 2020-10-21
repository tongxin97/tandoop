package com.github.tongxin97.tandoop;


import java.io.BufferedWriter;
import java.io.FileWriter;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * The class that stores a sequnce
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class Sequence {

    public Sequence() {}

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
