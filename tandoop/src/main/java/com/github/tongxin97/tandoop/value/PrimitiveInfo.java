package com.github.tongxin97.tandoop.value;

import org.apache.commons.text.StringEscapeUtils;

public class PrimitiveInfo extends ValueInfo {
  public Object Val;

  public PrimitiveInfo(String type, Object val) {
    super(type);
    this.Val = val;
  }

  public String getContent() {
    if (Val instanceof String) {
      return String.format("\"%s\"", StringEscapeUtils.escapeJava((String) Val));
    }
    return String.valueOf(Val);
  }
}
