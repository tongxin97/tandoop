package com.github.tongxin97.tandoop;

public class Main {
  public static void main(String[] args) throws Exception {
    Tandoop tandoop = new Tandoop(args[0]);

    tandoop.generateSequence(3);
  }
}