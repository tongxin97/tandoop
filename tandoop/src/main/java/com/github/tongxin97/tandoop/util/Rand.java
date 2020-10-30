package com.github.tongxin97.tandoop.util;

import java.util.Random;
import java.util.List;

public class Rand {
  static Random rand = new Random();

  public static int getRandomInt(int upperBound) {
    return rand.nextInt(upperBound);
  }
  public static Object getRandomInNonEmptyList(List<Object> values) {
    int i = getRandomInt(values.size());
    return values.get(i);
  }
}
