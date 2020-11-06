import org.junit.Test;
import static org.junit.Assert.*;
import com.github.tongxin97.tandoop.parser.MethodParser;
import com.github.tongxin97.tandoop.method.MethodPool;

public class MethodParserTest {
  @Test
  public void test() {
    try {
      MethodPool mp = new MethodPool();
      MethodParser.parseAndResolveDirectory("./src/main/java/", mp);
      System.out.println(mp);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      fail();
    }
  }
}
