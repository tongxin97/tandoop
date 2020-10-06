package com.github.tongxin97;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.body.Parameter;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.util.List;


public class MethodParser {
  public static void main(String[] args) throws Exception
    {
      CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(args[0]));

      List<MethodInfo> methodInfo = new ArrayList<>();
      VoidVisitor<List<MethodInfo>> methodInfoCollector = new MethodInfoCollector();
      methodInfoCollector.visit(cu, methodInfo);
      methodInfo.forEach(info -> System.out.println("=====\n" + info));
    }

  private static class MethodInfoCollector extends VoidVisitorAdapter<List<MethodInfo>> {
    @Override
    public void visit(MethodDeclaration md, List<MethodInfo> collector) {
      super.visit(md, collector);

      // Skip if method is private
      if (this.isPrivateMethod(md)) {
        System.out.println("Encountered private method: " + md.getNameAsString());
        return;
      }

      MethodInfo info = new MethodInfo();
      info.Name = md.getNameAsString();
      for (Parameter p : md.getParameters()) {
        info.ParameterTypes.add(p.getType());
      }
      info.ReturnType = md.getType();
      collector.add(info);
    }

    private boolean isPrivateMethod(MethodDeclaration md) {
      for (Modifier m : md.getModifiers()) {
        if (m.getKeyword().equals(Modifier.Keyword.PRIVATE)) {
          return true;
        }
      }
      return false;
    }
  }
}
