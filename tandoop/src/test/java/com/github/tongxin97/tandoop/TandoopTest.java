import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import com.google.gson.Gson;
import com.github.tongxin97.tandoop.value.VarInfo;

public class TandoopTest {
  @Test
  public void test() {
    boolean extensible = true;
    String output = "";
    try {
      try {
        VarInfo VarInfo0 = new VarInfo("a");

        try {
          assertTrue(VarInfo0.equals(VarInfo0));
          VarInfo0.hashCode();
          VarInfo0.toString();
        } catch (Exception e) {
					 System.err.println("e1: " + e);
					 fail();				 }
        output = new Gson().toJson(VarInfo0);
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