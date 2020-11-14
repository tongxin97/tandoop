package com.github.tongxin97.tandoop.method;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.lang.IllegalArgumentException;

import com.github.tongxin97.tandoop.util.Str;

public class MethodInfo {
    public String PackageName;
    public String ClassName;
    public String Name;
    public List<String> parameterTypes;
    public String returnType;

    public MethodInfo(String name, String className, String packageName) throws IllegalArgumentException {
        if (name == null || className == null || packageName == null) {
            throw new IllegalArgumentException("Null argument to MethodInfo()");
        }
        this.Name = name;
        this.ClassName = className;
        this.PackageName = packageName;
        this.parameterTypes = new ArrayList<>();
    }

    public String getFullyQualifiedMethodName() {
        if (this.ClassName != this.Name) {
            return this.PackageName + "." + this.ClassName + "." + this.Name;
        }
        return this.PackageName + "." + this.ClassName;
    }

    public void addParameterType(String t) {
        this.parameterTypes.add(t);
    }

    public void setReturnType(String t) {
        this.returnType = t;
    }

    public String getReturnType() {
        if (this.returnType == null) { // constructors don't have returnType
            return this.PackageName + "." + this.ClassName;
        }
        return this.returnType;
    }

    public String getSimpleReturnType() {
        if (this.returnType == null) { // constructors don't have returnType
            return this.ClassName;
        }
        return Str.getLastElementAfterSplit(this.returnType, "\\.").replaceAll(">", "");
    }

    public final List<String> getParameterTypes() {
        return this.parameterTypes;
    }

    public String getParameterTypeAtIdx(int i) {
        return this.parameterTypes.get(i);
    }

    public boolean IsConstructor() {
        return this.ClassName.equals(this.Name);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("Class name: %s\tMethod name: %s\n", this.ClassName, this.Name));
        if (this.parameterTypes != null) {
            b.append("Param types: ");
            for (String t: this.parameterTypes) {
                b.append(t + ", ");
            }
        }
        if (this.returnType != null) {
            b.append(String.format("\nReturn type: %s\n", this.returnType));
        }
        return b.toString();
    }
}
