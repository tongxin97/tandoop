import org.junit.Test;
import static org.junit.Assert.*;
import com.github.tongxin97.tandoop.parser.MethodParser;
import com.github.tongxin97.tandoop.method.MethodPool;

public class MethodParserTest {
  @Test
  public void test() {
    try {
      MethodParser p = new MethodParser(
        "./src/main/java/com/github/tongxin97/tandoop/sequence/Sequence.java",
        "./src/main/java/"
      );
      MethodPool mp = new MethodPool();
      p.collectMethodInfo(mp);
      System.out.println(mp);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail();
    }
  }
}
