package com.github.tongxin97.tandoop;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import com.google.gson.Gson;
import com.github.tongxin97.tandoop.Tandoop;

public class TandoopTest {
  @Test
  public void test() {
    boolean extensible = true;
    String output = "";
    try {
      try {
        Tandoop Tandoop0 = new Tandoop((String) null,(String) null);

        try {
          assertTrue(Tandoop0.equals(Tandoop0));
          Tandoop0.hashCode();
          Tandoop0.toString();
        } catch (Exception e) {
					 System.err.println("e1: " + e);
					 fail();
				 }
        output = new Gson().toJson(Tandoop0);
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