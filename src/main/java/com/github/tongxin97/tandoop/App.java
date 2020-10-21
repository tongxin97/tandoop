package com.github.tongxin97.tandoop;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }

    public int testReturnInt() {
        return 0;
    }

    private void myPrivateMethod() {
        return;
    }

    private class Tmp {
        public void tmpMethod() {
            System.out.println("tmp method");
        }
    }
}
