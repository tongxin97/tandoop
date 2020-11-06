package com.github.tongxin97.tandoop.util;

public class Str {
  public static String getLastElementAfterSplit(String s, String delim) {
    String l[] = s.split(delim);
    return l[l.length-1];
  }
}
