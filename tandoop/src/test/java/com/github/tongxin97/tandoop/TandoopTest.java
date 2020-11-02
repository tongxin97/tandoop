import org.junit.Test;
import static org.junit.Assert.*;
import com.github.tongxin97.tandoop.method.MethodPool;

public class TandoopTest {
  @Test
  public void test() {
    try {
      MethodPool MethodPool0 = new MethodPool();
      try {
        assertTrue(MethodPool0.equals(MethodPool0));
        MethodPool0.hashCode();
        MethodPool0.toString();
      } catch (Exception e) { fail(); }    }
    catch (Throwable t) { System.out.println(t.toString()); }
  }
}