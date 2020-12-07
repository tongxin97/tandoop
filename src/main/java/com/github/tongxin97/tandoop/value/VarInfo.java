package com.github.tongxin97.tandoop.value;

import java.util.Map;
import java.util.HashMap;
import com.github.tongxin97.tandoop.util.Str;

public class VarInfo extends ValueInfo{
  public int Idx; // the index number of this variable appearing in vars of the same type across sequences, generated based on varNums
  static Map<String, Integer> varNums = new HashMap<>(); // the number of variables in each type

  public VarInfo(String type) {
    super(type);
    this.Idx = varNums.getOrDefault(type, 0);
    varNums.put(type, this.Idx + 1);
  }

  @Override
  public String getContent() {
    return Str.sanitizeTypeString(Type) + this.Idx;
  }
}
