package com.github.tongxin97.tandoop;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import com.google.gson.Gson;
import com.github.tongxin97.tandoop.method.MethodPool;

public class TandoopTest {
  @Test
  public void test() {
    boolean extensible = true;
    String output = "";
    try {
      try {
        MethodPool MethodPool0 = new MethodPool();

        try {
          assertTrue(MethodPool0.equals(MethodPool0));
          MethodPool0.hashCode();
          MethodPool0.toString();
        } catch (Exception e) {
					 System.err.println("e1: " + e);
					 fail();
				 }
        output = new Gson().toJson(MethodPool0);
        System.out.println(output);
      } catch (Throwable t) { 
        System.err.println("e2: " + t);
        extensible = false;
      }
      File f = new File("../tandoop/testOutput.json");
      f.createNewFile();
      if (!extensible) { return; }
			PrintWriter p = new PrintWriter(new FileWriter(f));
      p.write(output);
      p.close();
    } catch (Exception e) { System.err.println("e3: " + e);}
  }
}