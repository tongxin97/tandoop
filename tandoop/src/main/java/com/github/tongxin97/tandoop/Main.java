package com.github.tongxin97.tandoop;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;

public class Main {
  public static void main(String[] args) throws Exception {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options();
    options.addOption("src", "srcDir", true, "Project src directory");
    options.addOption("prj", "projectDir", true, "Project directory");

    // TODO:
    // mvn clean install
    // mvn dependency:copy-dependencies
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
      Tandoop tandoop = new Tandoop(
        cmd.getOptionValue("src"),
        cmd.getOptionValue("prj")
      );
      tandoop.generateSequence(10);

    } catch (ParseException e) {
      System.err.println( "Unexpected exception:" + e.getMessage() );
    }
  }
}