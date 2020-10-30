package com.github.tongxin97.tandoop;

public class PrimitiveInfo extends ValInfo {
  public Object Val;

  public PrimitiveInfo(String type, Object val) {
    super(type);
    this.Val = val;
  }

  public String getContent() {
    if (Val instanceof String) {
      return String.format("\"%s\"", Val);
    }
    return String.valueOf(Val);
  }
}
