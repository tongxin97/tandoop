package com.github.tongxin97.tandoop.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.HashSet;

public class Str {

  static String specialSymbols = "[]<>, ";

  public static boolean isGenericType(String type) {
    return type.trim().length() == 1 && StringUtils.isAllUpperCase(type.trim());
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
   * @return true if some of the parsed types are generic
   */
  public static boolean parseNestedTypes(String type, Set<String> generics) {
    boolean containsGenericType = false;
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < type.length(); i++){
      char c = type.charAt(i);
      if (c == '<' || c == ',') {
        containsGenericType |= isGenericType(b.toString());
        if (isGenericType(b.toString()) && generics != null) {
          generics.add(b.toString().trim());
        }
        b.setLength(0); // reset
      } else {
        if (c != '>') {
          b.append(c);
        }
      }
    }
    containsGenericType |= isGenericType(b.toString());
    if (isGenericType(b.toString()) && generics != null) {
      generics.add(b.toString().trim());
    }
    return containsGenericType;
  }
}
