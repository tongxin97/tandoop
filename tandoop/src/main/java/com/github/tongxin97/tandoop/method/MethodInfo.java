package com.github.tongxin97.tandoop.method;

import com.github.javaparser.ast.type.Type;
import java.util.List;
import java.util.ArrayList;
import java.lang.IllegalArgumentException;

public class MethodInfo {
    public String PackageName;
    public String ClassName;
    public String Name;
    public List<String> ParameterTypes;
    public String ReturnType;

    public MethodInfo(String name, String className, String packageName) throws IllegalArgumentException {
        if (name == null || className == null || packageName == null) {
            throw new IllegalArgumentException("Null argument to MethodInfo()");
        }
        this.Name = name;
        this.ClassName = className;
        this.PackageName = packageName;
        this.ParameterTypes = new ArrayList<>();
    }

    public List<String> GetParameterTypes() {
        return this.ParameterTypes;
    }

    public boolean IsConstructor() {
        return this.ClassName.equals(this.Name);
    }

    @Override
    public String toString() {
        return String.format("Class name: %s\tMethod name: %s\tParam types: %s\tReturn type:%s", this.ClassName, this.Name, this.ParameterTypes, this.ReturnType);
    }
}
