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
    options.addOption("numTests", "numTests", true, "Limit the number of generated tests.");
    options.addOption("basic", "basicFeatures", false, "If Tandoop should use all basic features.");
    options.addOption("all", "allFeatures", false, "If Tandoop should use all features.");
    options.addOption("noGT", "noGenericTypes", false, "If Tandoop should allow for generics types in generated test classes.");
    options.addOption("noMI", "noMethodInheritance", false, "If Tandoop should use method inheritance.");
    options.addOption("noCI", "noClassInheritance", false, "If Tandoop should use class inheritance.");
    options.addOption("cg", "coverageGuided", false, "If Tandoop uses coverage-guided method selection.");
    options.addOption("cp", "constructorPreference", false, "If Tandoop uses constructor selection preference.");
    options.addOption("odc", "onDemandConstruction", false, "If Tandoop uses on-demand construction of external types.");

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
      if (!cmd.hasOption("limit") && !cmd.hasOption("numTests")) {
        System.err.println("Time limit/numTests not provided.");
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
      File testOutputDir = new File(String.format("%s/src/test/java/", prjDir));
      File[] files = testOutputDir.listFiles((dir, name) -> name.matches("^TandoopErrTest*\\.java"));
      if (files != null) {
        for (File f: files) {
          if (!f.delete()) {
            System.err.println("Can't remove " + f.getAbsolutePath());
          }
        }
      }
      testOutputDir.mkdir();

      Tandoop tandoop = new Tandoop(cmd.getOptionValue("src"), prjDir);

      tandoop.allowGenerics = cmd.hasOption("basic") || !cmd.hasOption("noGT");
      tandoop.useMethodInheritance = cmd.hasOption("basic") || !cmd.hasOption("noMI");
      tandoop.useClassInheritance = cmd.hasOption("basic") || !cmd.hasOption("noCI");
      tandoop.useCovGuide = cmd.hasOption("all") || cmd.hasOption("cg");
      tandoop.useConstructorSelection = cmd.hasOption("all") || cmd.hasOption("cp");
      tandoop.useODConstruction = cmd.hasOption("all") || cmd.hasOption("odc");

      int numTests = cmd.hasOption("numTests")? Integer.parseInt(cmd.getOptionValue("numTests")): 0;
      int timeLimit = cmd.hasOption("limit")? Integer.parseInt(cmd.getOptionValue("limit")): 0;
      tandoop.generateSequences(numTests, timeLimit);

    } catch (ParseException e) {
      System.err.println( "Unexpected exception:" + e.getMessage() );
    }
  }
}