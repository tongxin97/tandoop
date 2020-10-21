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
        String sequence = "package com.github.tongxin97.tandoop;\n"
        		+ "\n"
        		+ "import static org.junit.Assert.assertTrue;\n"
        		+ "\n"
        		+ "import org.junit.Test;\n"
        		+ "\n"
        		+ "/**\n"
        		+ " * Unit test for simple App.\n"
        		+ " */\n"
        		+ "public class AppTest \n"
        		+ "{\n"
        		+ "    /**\n"
        		+ "     * Rigorous Test :-)\n"
        		+ "     */\n"
        		+ "    @Test\n"
        		+ "    public void shouldAnswerWithTrue()\n"
        		+ "    {\n"
        		+ "        assertTrue( true );\n"
        		+ "    }\n"
        		+ "}\n";
        String filename = "src/test/java/com/github/tongxin97/tandoop/AppTest.java";
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(sequence);
        writer.close();
    }

    public void runTest() {
			JUnitCore.main("com.github.tongxin97.tandoop.AppTest");
    }
}
