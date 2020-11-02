package com.github.tongxin97.tandoop.value;

public abstract class ValueInfo {
  public String Type; // type name
  public boolean Extensible;

  public ValueInfo () {}

  public ValueInfo(String type) {
    this.Type = type;
  }

  abstract public String getContent();
}
