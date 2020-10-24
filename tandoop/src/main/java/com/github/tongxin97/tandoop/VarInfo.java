package com.github.tongxin97.tandoop;

public class VarInfo extends ValInfo{
  public int Idx; // the index number of this variable appearing in vars of the same type across sequences, generated based on varNums
  static Map<String, int> varNums; // the number of variables in each type

  public VarInfo(String type, int idx) {
    super(type);
    this.Idx = varNums.getOrDefault(type, 0);
    ++varNums.getKey(type);
  }
}
