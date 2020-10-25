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
    options.addOption("pkg", "pkgName", false, "Package name");
    options.addOption("src", "srcDir", false, "Project src directory");
    options.addOption("test", "testDir", false, "Project test directory");

    try {
      // parse cmdline arguments
      CommandLine cmd = parser.parse(options, args);
      if (!cmd.hasOption("pkg")) {
        System.err.println("Package name not provided.");
      }
      // args: String testDir, String pkgName
      Tandoop tandoop = new Tandoop(
        cmd.getOptionValue("pkg"),
        cmd.getOptionValue("src"),
        cmd.getOptionValue("test")
      );
      tandoop.generateSequence(20);

    } catch (ParseException e) {
      System.err.println( "Unexpected exception:" + e.getMessage() );
    }
  }
}