package com.github.tongxin97;

import com.github.javaparser.ast.type.Type;
import java.util.List;
import java.util.ArrayList;

public class MethodInfo {
  public String Name;
  public List<Type> ParameterTypes;
  public Type ReturnType;

  public MethodInfo() {
    this.ParameterTypes = new ArrayList<>();
  }

  @Override
  public String toString() {
    return String.format("Method name: %s\nParam types: %s\nReturn type:%s", this.Name, this.ParameterTypes, this.ReturnType);
  }
}
