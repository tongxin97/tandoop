package com.github.tongxin97.tandoop;

public abstract class ValInfo {
  public String Type; // type name
  public boolean Extensible;

  public ValInfo(String type) {
    this.Type = type;
  }

  abstract public String getContent();
}
