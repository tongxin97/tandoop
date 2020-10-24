package com.github.tongxin97.tandoop;

import java.util.Map;

public class VarInfo extends ValInfo{
  public int Idx; // the index number of this variable appearing in vars of the same type across sequences, generated based on varNums
  static Map<String, Integer> varNums; // the number of variables in each type

  public VarInfo(String type) {
    super(type);
    this.Idx = varNums.getOrDefault(type, 0);
    varNums.put(type, varNums.get(type) + 1);
  }

  public String getContent() {
    return Type + String.valueOf(this.Idx);
  }
}
