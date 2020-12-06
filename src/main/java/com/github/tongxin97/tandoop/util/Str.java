package com.github.tongxin97.tandoop.util;

import java.util.Set;
import java.util.HashSet;

public class Str {

  static String specialSymbols = "[]<>";

  public static boolean isGenericType(String type) {
    return !type.contains(".");
  }

  public static String getLastElementAfterSplit(String s, String delim) {
    String l[] = s.split(delim);
    return l[l.length-1];
  }

  /**
   * replace all occurences of a special symbol (defined in specialSymbols) by its ascii code.
   * @param type
   * @return
   */
  public static String sanitizeTypeString(String type) {
    String res = type;
    for (int i = 0; i < specialSymbols.length(); i++) {
      res = res.replaceAll(String.format("\\%c", specialSymbols.charAt(i)), String.valueOf((int) specialSymbols.charAt(i)));
    }
    return res;
  }

  /**
   *
   * @param type potentially nested type, eg. java.util.Set<java.lang.String>>
   * @param res set to store parsed types (could be null)
   * @return true if some of the parsed types are generic
   */
  public static boolean parseNestedTypes(String type, Set<String> res) {
    boolean containsGenericType = false;
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < type.length(); i++){
      char c = type.charAt(i);
      if (c == '<') {
        containsGenericType |= isGenericType(b.toString());
        if (res != null) {
          res.add(b.toString());
        }
        b.setLength(0); // reset
      } else {
        if (c != '>') {
          b.append(c);
        }
      }
    }
    if (res != null) {
      res.add(b.toString()); // add last
    }
    containsGenericType |= isGenericType(b.toString());
    return containsGenericType;
  }
}
