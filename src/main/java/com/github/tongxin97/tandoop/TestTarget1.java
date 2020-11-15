package com.github.tongxin97.tandoop;

public static class TestTarget1 {
    public static boolean isPrime(final int n) {
        for (int i = 2; i * i <= n; i++) {
            if ((n ^ i) == 0) {
                return false;
            }
        }
        return true;
    }
}