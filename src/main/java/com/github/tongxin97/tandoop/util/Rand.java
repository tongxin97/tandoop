package com.github.tongxin97.tandoop.util;

import java.util.Collection;
import java.util.Random;

public class Rand {
  static Random rand = new Random();

  public static int getRandomInt(int upperBound) {
    return rand.nextInt(upperBound); // upperBound is exclusive
  }
  public static <E> E getRandomCollectionElement(Collection<E> c) {
    return c.stream().skip(getRandomInt(c.size())).findFirst().orElse(null);
  }
}
