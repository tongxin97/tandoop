package com.github.tongxin97.tandoop.value;

import com.github.tongxin97.tandoop.util.ClassUtils;
import org.apache.commons.text.StringEscapeUtils;

public class ValueInfo {
  public String Type; // type name
  public Object Val; // runtime value
  public boolean Extensible = false;

  public ValueInfo(String type) {
    Type = type;
  }

  public ValueInfo(String type, Object val) {
    Type = type;
    Val = val;
  }

  public String getContent() {
    if (this.Val instanceof String) {
      return String.format("\"%s\"", StringEscapeUtils.escapeJava((String) Val));
    }
    if (this.Val instanceof Character) {
      return String.format("'%s'", Val);
    }
    if (!Type.equals(Number.class.getName())) {
      return "(" + this.Type + ") " + this.Val.toString();
    }
    return this.Val.toString();
  }
}
