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
    options.addOption("pkg", "pkgName", true, "Package name");
    options.addOption("src", "srcDir", true, "Project src directory");
    options.addOption("test", "testDir", true, "Project test directory");

    try {
      // parse cmdline arguments
      CommandLine cmd = parser.parse(options, args);
      if (!cmd.hasOption("pkg")) {
        System.err.println("Package name not provided.");
      }
      if (!cmd.hasOption("src")) {
        System.err.println("Project src directory not provided.");
      }
      if (!cmd.hasOption("test")) {
        System.err.println("Project test directory not provided.");
      }
      // args: String testDir, String pkgName
      Tandoop tandoop = new Tandoop(
        cmd.getOptionValue("test"),
        cmd.getOptionValue("pkg")
      );
      tandoop.generateSequence(20);

    } catch (ParseException e) {
      System.err.println( "Unexpected exception:" + e.getMessage() );
    }
  }
}