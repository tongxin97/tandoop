package com.github.tongxin97.tandoop;

import com.github.javaparser.ast.type.Type;
import java.util.List;
import java.util.ArrayList;

public class MethodInfo {
    public String ClassName;
    public String Name;
    public List<String> ParameterTypes;
    public String ReturnType;

    public MethodInfo(String name, String className) {
        this.Name = name;
        this.ClassName = className;
        this.ParameterTypes = new ArrayList<>();
    }

    public List<String> GetParameterTypes() {
        return this.ParameterTypes;
    }

    public boolean IsConstructor() {
        String last = this.ClassName.substring(this.ClassName.lastIndexOf('.') + 1);
        return last.equals(this.Name);
    }

    @Override
    public String toString() {
        return String.format("Class name: %s\tMethod name: %s\tParam types: %s\tReturn type:%s", this.ClassName, this.Name, this.ParameterTypes, this.ReturnType);
    }
}
