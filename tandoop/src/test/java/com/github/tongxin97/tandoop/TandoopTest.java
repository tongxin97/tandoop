import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tongxin97.tandoop.sequence.Sequence;

public class TandoopTest {
  @Test
  public void test() {
    byte b = 0x01;
    File f = new File("../tandoop/sharedFile");
    byte[] bytes = null;
    try {
      try {
        Sequence Sequence0 = new Sequence();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(Visibility.ANY).withGetterVisibility(Visibility.NONE).withSetterVisibility(Visibility.NONE).withCreatorVisibility(Visibility.NONE));
        bytes = objectMapper.writeValueAsBytes(Sequence0);
        try {
          assertTrue(Sequence0.equals(Sequence0));
          Sequence0.hashCode();
          Sequence0.toString();
        } catch (Exception e) { fail(); }
      } catch (Throwable t) { 
        System.err.println(t);
        b = 0x02;
      }
      FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
      MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
      mappedByteBuffer.put(b);
      if (b == 0x01) { mappedByteBuffer.put(bytes); }
    } catch (Exception e) { System.err.println(e); }
  }
}