import org.junit.Test;
import com.github.tongxin97.tandoop.parser.MethodParser;

public class TandoopTest {
  @Test
  public void test() {
    MethodParser MethodParser0 = new MethodParser("a");
    boolean r = MethodParser0.equals(MethodParser0);
    if (r == false) { throw new Exception(); }
    MethodParser0.hashcode();
    MethodParser0.toString();
  }
}