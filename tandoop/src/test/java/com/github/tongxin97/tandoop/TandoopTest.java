import org.junit.Test;
import static org.junit.Assert.assertTrue;
import com.github.tongxin97.tandoop.parser.MethodParser;

public class TandoopTest {
  @Test
  public void test() {
    try {
      MethodParser MethodParser0 = new MethodParser("3.14");
      assertTrue(MethodParser0.equals(MethodParser0));
      MethodParser0.hashCode();
      MethodParser0.toString();
    }
    catch (NullPointerException e) {}
    catch (Throwable t) { System.out.println(t.toString()); }
  }
}