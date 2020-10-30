import org.junit.Test;
import com.github.tongxin97.tandoop.value.PrimitiveInfo;

public class TandoopTest {
  @Test
  public void test() {
    PrimitiveInfo PrimitiveInfo2 = new PrimitiveInfo("",null);
    boolean r = PrimitiveInfo2.equals(PrimitiveInfo2);
    if (r == false) { throw new Exception(); }
    PrimitiveInfo2.hashcode();
    PrimitiveInfo2.toString();
  }
}