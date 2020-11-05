import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import com.google.gson.Gson;
import com.github.tongxin97.tandoop.sequence.Sequence;

public class TandoopTest {
  @Test
  public void test() {
    boolean extensible = true;
    String output = "";
    try {
      try {
        Sequence Sequence0 = new Sequence();

        try {
          assertTrue(Sequence0.equals(Sequence0));
          Sequence0.hashCode();
          Sequence0.toString();
        } catch (Exception e) {
					 System.err.println("e1: " + e);
					 fail();				 }
        output = new Gson().toJson(Sequence0);
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