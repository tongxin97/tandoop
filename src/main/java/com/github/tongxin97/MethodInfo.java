package com.github.tongxin97;

import com.github.javaparser.ast.type.Type;
import java.util.List;
import java.util.ArrayList;

public class MethodInfo {
    public String ClassName;
    public String Name;
    public List<Type> ParameterTypes;
    public Type ReturnType;

    public MethodInfo(String name, String className) {
        this.Name = name;
        this.ClassName = className;
        this.ParameterTypes = new ArrayList<>();
    }

    public List<Type> getParameterTypes() {
        return ParameterTypes;
    }

    @Override
    public String toString() {
        return String.format("Class name: %s\nMethod name: %s\nParam types: %s\nReturn type:%s", this.ClassName, this.Name, this.ParameterTypes, this.ReturnType);
    }
}
