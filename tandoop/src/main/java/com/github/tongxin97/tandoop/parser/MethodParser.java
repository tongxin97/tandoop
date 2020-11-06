package com.github.tongxin97.tandoop.parser;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.io.FileInputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.tongxin97.tandoop.method.MethodInfo;
import com.github.tongxin97.tandoop.method.MethodPool;
import com.github.tongxin97.tandoop.util.Str;

public class MethodParser {
  private CompilationUnit cu;
  private Map<String, String> importedTypes; // map simple type to fully qualified type

  private void storeImportedTypes() {
    // store imported types
    this.importedTypes = new HashMap<>();
    for (ImportDeclaration importDeclaration : this.cu.getImports()) {
      String type = importDeclaration.getNameAsString();
      String simpleType = Str.getLastElementAfterSplit(type, "\\.");
      this.importedTypes.put(simpleType, type);
    }
    // System.out.println("imports: " + this.importedTypes);
  }

  public MethodParser(String filename, String srcDir) throws Exception {
    if (filename == null || srcDir == null) {
      throw new IllegalArgumentException(String.format("Parameters can't be null: filename=%s, srcDir=%s", filename, srcDir));
    }
    this.cu = StaticJavaParser.parse(new FileInputStream(filename));
    this.storeImportedTypes();
  }

  public MethodParser(CompilationUnit cu, String srcDir) throws Exception {
    if (cu == null || srcDir == null) {
      throw new IllegalArgumentException(String.format("Parameters can't be null: cu=%s, srcDir=%s", cu, srcDir));
    }
    this.cu = cu;
    this.storeImportedTypes();
  }

  public static void parseAndResolveDirectory(String srcDir, MethodPool methodPool) throws Exception {
    CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    combinedTypeSolver.add(new ReflectionTypeSolver());
    combinedTypeSolver.add(new JavaParserTypeSolver(new File(srcDir)));
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
    StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    // TODO: handle generic types

    Path rootPath = Paths.get(srcDir);
    ProjectRoot projectRoot = new SymbolSolverCollectionStrategy(
      StaticJavaParser.getConfiguration()
    ).collect(rootPath);

    for (SourceRoot sr: projectRoot.getSourceRoots()) {
      sr.tryToParse();
      List<CompilationUnit> compilations = sr.getCompilationUnits();
      for (CompilationUnit cu: compilations) {
          new MethodParser(cu, srcDir).collectMethodInfo(methodPool);
      }
    }
  }

  public void collectMethodInfo(MethodPool methodPool) {
    try {
      if (isAbstractClass()) {
        return; // skip if class is abstract
      }
    } catch (Exception e) {
      System.err.println("collectMethodInfo exception: " + e.getMessage());
      return;
    }

    VoidVisitor<List<MethodInfo>> methodCollector = new MethodCollector();
    MethodInfo constructorInfo = this.getConstructorInfo();
    if (constructorInfo == null) {
      return; // skip if no constructor info
    }
    methodPool.MethodInfoList.add(constructorInfo);
    methodCollector.visit(this.cu, methodPool.MethodInfoList);
  }

  private String getPackageName() {
    if (this.cu == null) {
      return null;
    }
    Optional<PackageDeclaration> opt = this.cu.getPackageDeclaration();
    if (opt.isPresent()) {
      return opt.get().getNameAsString();
    }
    return null;
  }

  private String resolveType(Type simpleType) {
    try {
      return simpleType.resolve().describe();
    } catch (Exception e) {
      // e.printStackTrace();
      if (this.importedTypes.containsKey(simpleType.toString())) {
        return this.importedTypes.get(simpleType.toString());
      }
    }
    return null;
  }

  /**
   * getConstructorInfo creates a MethodInfo instance for the constructor.
   * @return MethodInfo if a public constructor exists; null otherwise.
   */
  private MethodInfo getConstructorInfo() {
    for (TypeDeclaration td: this.cu.getTypes()) {

      List<BodyDeclaration> bds = td.getMembers();
      if(bds != null) {
        for (BodyDeclaration bd: bds) {
          if (bd instanceof ConstructorDeclaration) {
              ConstructorDeclaration cd = (ConstructorDeclaration) bd;
              String constructorName = cd.getNameAsString();
              MethodInfo info = new MethodInfo(
                constructorName,
                constructorName,
                this.getPackageName()
              );
              for (Parameter p : cd.getParameters()) {
                String paramType = this.resolveType(p.getType());
                if (paramType == null) {
                  System.err.printf("Unable to resolve parameter type %s at %s\n", p.getType(), constructorName);
                  return null;
                }
                info.addParameterType(paramType);
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
  private static boolean isAbstractClass(ClassOrInterfaceDeclaration cd) {
    for (Modifier m : cd.getModifiers()) {
      if (m.getKeyword().equals(Modifier.Keyword.ABSTRACT)) {
        return true;
      }
    }
    return false;
  }
  private boolean isAbstractClass() throws Exception {
    if (this.cu == null) {
      throw new Exception("Can't determine if class is abstract: this.cu is null.");
    }
    for (TypeDeclaration td: this.cu.getTypes()) {
      if (td instanceof ClassOrInterfaceDeclaration) {
        return isAbstractClass((ClassOrInterfaceDeclaration) td);
      }
    }
    throw new Exception("Can't determine if class is abstract.");
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

      String className = null;
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
        className = t.getNameAsString();
      }

      MethodInfo info = new MethodInfo(md.getNameAsString(), className, getPackageName());
      for (Parameter p : md.getParameters()) {
        String paramType = resolveType(p.getType());
        if (paramType == null) {
          System.err.printf("Unable to resolve parameter type %s at %s\n", p.getType(), md.getNameAsString());
          return;
        }
        info.addParameterType(paramType);
      }
      String returnType = resolveType(md.getType());
      if (returnType == null) {
        System.err.printf("Unable to resolve return type %s at %s\n", md.getType(), md.getNameAsString());
        return;
      }
      info.setReturnType(returnType);
      collector.add(info);
      // System.out.println("info: " + info);
    }
  }
}
