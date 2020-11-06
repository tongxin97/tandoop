package com.github.tongxin97.tandoop.util;

import java.util.regex.Pattern;

public class Str {
  private static String regex = "/^[a-z]\\w*(\\.[a-z]\\w*)+$/i";
  private static Pattern pattern = Pattern.compile(regex);

  public static String getLastElementAfterSplit(String s, String delim) {
    String l[] = s.split(delim);
    return l[l.length-1];
  }

  public static boolean isQualifiedType(String type) {
    return pattern.matcher(type).find();
  }
}
