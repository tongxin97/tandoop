import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import com.google.gson.Gson;
import com.github.tongxin97.tandoop.value.ValueInfo;

public class TandoopTest {
  @Test
  public void test() {
    boolean extensible = true;
    String output = "";
    try {
      try {
        ValueInfo ValueInfo0 = new ValueInfo("0");

        try {
          assertTrue(ValueInfo0.equals(ValueInfo0));
          ValueInfo0.hashCode();
          ValueInfo0.toString();
        } catch (Exception e) {
					 System.err.println("e1: " + e);
					 fail();				 }
        output = new Gson().toJson(ValueInfo0);
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