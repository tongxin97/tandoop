package com.github.tongxin97.tandoop;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;

import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.util.Optional;


public class MethodParser {
  private CompilationUnit CU;

  public MethodParser(String filename) throws Exception {
    this.CU = StaticJavaParser.parse(new FileInputStream(filename));
  }

  private String getPackageName() {
    if (this.CU == null) {
      return null;
    }
    Optional<PackageDeclaration> opt = this.CU.getPackageDeclaration();
    if (opt.isPresent()) {
      return opt.get().getNameAsString();
    }
    return null;
  }

  private MethodInfo getConstructorInfo() {
    for (TypeDeclaration td: this.CU.getTypes()) {
        List<BodyDeclaration> bds = td.getMembers();
        if(bds != null) {
            for (BodyDeclaration bd: bds) {
                if (
                  bd instanceof ConstructorDeclaration
                ) {
                    ConstructorDeclaration cd = (ConstructorDeclaration) bd;
                    String classNameWithPackage = this.getPackageName() + "." + cd.getNameAsString();
                    MethodInfo info = new MethodInfo(cd.getNameAsString(), classNameWithPackage);
                    info.ReturnType = classNameWithPackage;
                    for (Parameter p : cd.getParameters()) {
                      info.ParameterTypes.add(p.getType().toString());
                    }
                    return info;
                }
            }
        }
    }
    return null;
  }

  private static boolean isPrivateMethod(MethodDeclaration md) {
    for (Modifier m : md.getModifiers()) {
      if (m.getKeyword().equals(Modifier.Keyword.PRIVATE)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isPublicClass(ClassOrInterfaceDeclaration cd) {
    for (Modifier m : cd.getModifiers()) {
      if (m.getKeyword().equals(Modifier.Keyword.PUBLIC)) {
        return true;
      }
    }
    return false;
  }


  public void CollectMethodInfo(MethodPool methodPool) {
    VoidVisitor<List<MethodInfo>> methodCollector = new MethodCollector();
    MethodInfo constructorInfo = this.getConstructorInfo();
    if (this.getConstructorInfo() != null) {
      methodPool.MethodInfoList.add(constructorInfo);
    }
    methodCollector.visit(this.CU, methodPool.MethodInfoList);
  }

  private class MethodCollector extends VoidVisitorAdapter<List<MethodInfo>> {
    @Override
    public void visit(MethodDeclaration md, List<MethodInfo> collector) {
      super.visit(md, collector);

      // Skip if method is private
      if (MethodParser.isPrivateMethod(md)) {
        // System.out.println("Encountered private method: " + md.getNameAsString());
        return;
      }

      String className = "";

      Optional<Node> opt = md.getParentNode();
      if (opt.isPresent()) {
        TypeDeclaration t = (TypeDeclaration) opt.get();
        // if parent node is not public class, skip this method
        if (!(
          t instanceof ClassOrInterfaceDeclaration &&
          MethodParser.isPublicClass((ClassOrInterfaceDeclaration) t)
        )) {
          // System.out.printf("Encountered method in private inner class: %s.%s\n", t.getNameAsString(), md.getNameAsString());
          return;
        }

        // prepend package name to class name
        String packageName = getPackageName();
        if (packageName != null) {
          className += packageName + ".";
        }
        // append class name
        className += t.getNameAsString();
      }

      MethodInfo info = new MethodInfo(md.getNameAsString(), null);
      info.ClassName = className;
      // TODO: add package name to types?
      for (Parameter p : md.getParameters()) {
        info.ParameterTypes.add(p.getType().toString());
      }
      info.ReturnType = md.getType().toString();
      collector.add(info);
    }
  }
}
