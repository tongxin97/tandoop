import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.tongxin97.tandoop.value.VarInfo;

public class TempTest {
  @Test
  public void test() {
    byte b = 0x01;
    File f = new File("../tandoop/sharedFile");
    try {
      FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
      MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
      try {
        VarInfo VarInfo0 = new VarInfo("int");
  
        ObjectMapper objectMapper = new ObjectMapper();
        mappedByteBuffer.put(objectMapper.writeValueAsBytes(VarInfo0));

        try {
          assertTrue(VarInfo0.equals(VarInfo0));
          VarInfo0.hashCode();
          VarInfo0.toString();
        } catch (Exception e) { fail(); }
      } catch (Throwable t) { 
        System.err.println(t); 
        b = 0x02;
      }
      mappedByteBuffer.put((byte) 0x00);
      mappedByteBuffer.put(b);
    } catch (Exception e) { System.err.println(e); }
  }
}