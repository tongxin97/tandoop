package com.github.tongxin97;

public class Main {
  public static void main(String[] args) throws Exception {
    MethodParser methodParser = new MethodParser(args[0]);
    MethodPool methodPool = new MethodPool();
    methodParser.CollectMethodInfo(methodPool);

    Sequence sequence = new Sequence();
    sequence.generateTest();
    sequence.runTest();
  }
}