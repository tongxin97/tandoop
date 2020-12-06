package com.github.tongxin97.tandoop.value;

import com.github.tongxin97.tandoop.util.ClassUtils;
import org.apache.commons.text.StringEscapeUtils;

public class ValueInfo {
  public String Type; // type name
  public Object Val; // runtime value
  public boolean Extensible;

  public ValueInfo(String type) {
    this.Type = type;
  }

  public ValueInfo(String type, Object val) {
    this.Type = type;
    this.Val = val;
  }

  public ValueInfo(String type, Object val, boolean extensible) {
    this.Type = type;
    this.Val = val;
    this.Extensible = extensible;
  }

  public String getContent() {
    if (this.Val instanceof String) {
      return String.format("\"%s\"", StringEscapeUtils.escapeJava((String) Val));
    }
    if (this.Val instanceof Character) {
      return String.format("'%s'", Val);
    }
    return "(" + this.Type + ") " + this.Val.toString();
  }
}
