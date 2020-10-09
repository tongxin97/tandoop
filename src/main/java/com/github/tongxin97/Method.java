package com.github.tongxin97;

import com.github.javaparser.ast.type.Type;
import java.util.List;
import java.util.ArrayList;

public class Method {
    public String Name;
    public List<Type> ParameterTypes;
    public Type ReturnType;

    public Method() {
        this.ParameterTypes = new ArrayList<>();
    }

    public Method(String name) {
        this.Name = name;
        this.ParameterTypes = new ArrayList<>();
    }

    public List<Type> getParameterTypes() {
        return ParameterTypes;
    }

    @Override
    public String toString() {
        return String.format("Method name: %s\nParam types: %s\nReturn type:%s", this.Name, this.ParameterTypes, this.ReturnType);
    }
}
