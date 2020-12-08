
public class TandoopTest<V,K> {
  public Object test() {
    try {
      java.util.Map.Entry<K, V> Entry60K4432V0 = org.apache.commons.collections4.CollectionUtils.get((java.util.Map<K, V>) null,(int) 0.333);
      if (Entry60K4432V0 == null) { return "[Tandoop] F: null"; }
      try {
        assert(Entry60K4432V0.equals(Entry60K4432V0));
        Entry60K4432V0.hashCode();
        Entry60K4432V0.toString();
      } catch (Exception e) { return "[Tandoop] C: " + e; }
       return Entry60K4432V0;
    }
    catch (AssertionError e) { return "[Tandoop] C: " + e; }
    catch (Exception e) { return "[Tandoop] F: " + e; }
  }
}