package com.github.tongxin97.tandoop;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;
import java.io.*;

public class Main {
  public static void main(String[] args) throws Exception {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options();
    options.addOption("src", "srcDir", true, "Project src directory");
    options.addOption("prj", "projectDir", true, "Project directory");
    options.addOption("limit", "timeLimit", true, "Time limit to run Tandoop for in seconds");

    try {
      // parse cmdline arguments
      CommandLine cmd = parser.parse(options, args);
      if (!cmd.hasOption("src")) {
        System.err.println("Project src directory not provided.");
        return;
      }
      if (!cmd.hasOption("prj")) {
        System.err.println("Project directory not provided.");
        return;
      }
      if (!cmd.hasOption("limit")) {
        System.err.println("Time limit not provided.");
        return;
      }

      String prjDir = cmd.getOptionValue("prj");

      // copy target project dependencies from maven
      File directory = new File(prjDir + "/target/dependency");
      if (!directory.exists()){
        System.out.println("Mvn dependencies do not exist. Mvn installing...");
        String mvnCmd = "mvn clean install -DskipTests";
        Process p = Runtime.getRuntime().exec(new String[] {"bash", "-c", mvnCmd}, null, new File(prjDir));
        if (p.waitFor() != 0) {
          System.err.println("Mvn build error.");
          return;
        }
        System.out.println("Copying mvn dependencies...");
        mvnCmd = "mvn dependency:copy-dependencies";
        p = Runtime.getRuntime().exec(new String[] {"bash", "-c", mvnCmd}, null, new File(prjDir));
        if (p.waitFor() != 0) {
          System.err.println("Mvn copy dependencies error.");
          return;
        }
      }

      File testClassDir = new File("target/test-classes");
      testClassDir.mkdir();

      // remove previously genereated error tests
      File testOutputDir = new File(String.format("%s/src/test/java", prjDir));
      File[] files = testOutputDir.listFiles( new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return name.matches("^TandoopTest.*\\.java");
        }
      });
      if (files != null) {
        for (File f: files) {
          if (!f.delete()) {
            System.err.println("Can't remove " + f.getAbsolutePath());
          }
        }
      }
      testOutputDir.mkdir();

      Tandoop tandoop = new Tandoop(cmd.getOptionValue("src"), prjDir);
      int timeLimit = Integer.parseInt(cmd.getOptionValue("limit"));
      tandoop.generateSequences(timeLimit*1000);

    } catch (ParseException e) {
      System.err.println( "Unexpected exception:" + e.getMessage() );
    }
  }
}