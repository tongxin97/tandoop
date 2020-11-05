package com.github.tongxin97.tandoop.method;

import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
import java.lang.IllegalArgumentException;
import com.github.javaparser.resolution.types.ResolvedType;

public class MethodInfo {
    public String PackageName;
    public String ClassName;
    public String Name;
    public List<ResolvedType> ParameterTypes;
    public ResolvedType ReturnType;

    public MethodInfo(String name, String className, String packageName) throws IllegalArgumentException {
        if (name == null || className == null || packageName == null) {
            throw new IllegalArgumentException("Null argument to MethodInfo()");
        }
        this.Name = name;
        this.ClassName = className;
        this.PackageName = packageName;
        this.ParameterTypes = new ArrayList<>();
    }

    public void addParameterType(ResolvedType t) {
        this.ParameterTypes.add(t);
    }

    public void setReturnType(ResolvedType t) {
        this.ReturnType = t;
    }

    // public List<String> GetParameterTypes() {
    //     return this.ParameterTypes;
    // }

    public boolean IsConstructor() {
        return this.ClassName.equals(this.Name);
    }

    public String getFullReturnType() {
        return this.PackageName + '.' + this.ReturnType;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("Class name: %s\tMethod name: %s\n", this.ClassName, this.Name));
        b.append("Param types: ");
        for (ResolvedType t: this.ParameterTypes) {
            b.append(t.describe() + ", ");
        }
        if (this.ReturnType != null) {
            b.append(String.format("\nReturn type: %s\n", this.ReturnType.describe()));
        }
        return b.toString();
    }
}
