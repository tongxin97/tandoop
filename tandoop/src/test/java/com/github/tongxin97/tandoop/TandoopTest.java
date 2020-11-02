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
import com.github.tongxin97.tandoop.value.TypedValuePool;

public class TandoopTest {
  @Test
  public void test() {
    byte b = 0x01;
    File f = new File("../tandoop/sharedFile");
    byte[] bytes = null;
    try {
      try {
        TypedValuePool TypedValuePool0 = new TypedValuePool("a",null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(Visibility.ANY).withGetterVisibility(Visibility.NONE).withSetterVisibility(Visibility.NONE).withCreatorVisibility(Visibility.NONE));
        bytes = objectMapper.writeValueAsBytes(TypedValuePool0);
        try {
          assertTrue(TypedValuePool0.equals(TypedValuePool0));
          TypedValuePool0.hashCode();
          TypedValuePool0.toString();
        } catch (Exception e) { fail(); }
      } catch (Throwable t) { 
        System.err.println(t);
        b = 0x02;
      }
      FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
      MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
			 if (!mappedByteBuffer.isLoaded()) { mappedByteBuffer.load();}
      mappedByteBuffer.put(b);
      if (b == 0x01) { mappedByteBuffer.put(bytes); }
      mappedByteBuffer.put((byte) 0x00);
    } catch (Exception e) { System.err.println(e); }
  }
}