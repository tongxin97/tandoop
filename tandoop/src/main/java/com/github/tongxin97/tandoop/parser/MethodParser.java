package com.github.tongxin97.tandoop.parser;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
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
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;

import java.util.List;
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

  public MethodParser(String filename, String srcDir) throws Exception {
    if (filename == null || srcDir == null) {
      throw new IllegalArgumentException(String.format("Parameters can't be null: filename=%s, srcDir=%s", filename, srcDir));
    }
    this.cu = StaticJavaParser.parse(new FileInputStream(filename));
  }

  public MethodParser(CompilationUnit cu, String srcDir) throws Exception {
    if (cu == null || srcDir == null) {
      throw new IllegalArgumentException(String.format("Parameters can't be null: cu=%s, srcDir=%s", cu, srcDir));
    }
    this.cu = cu;
  }

  public static void parseAndResolveDirectory(String srcDir, String prjDir, MethodPool methodPool) throws Exception {
    CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    // add reflection solver
    combinedTypeSolver.add(new ReflectionTypeSolver());
    // add source file solver
    combinedTypeSolver.add(new JavaParserTypeSolver(new File(srcDir)));
    // add solver for each dependency jar
    File dir = new File(prjDir + "/target/dependency");
    File[] dirListing = dir.listFiles();
    if (dirListing == null) {
      System.err.println("Invalid maven dependency dir: " + prjDir + "/target/dependency");
      System.exit(1);
    }
    for (File f: dirListing) {
      combinedTypeSolver.add(new JarTypeSolver(f));
    }
    // use combinedTypeSolver in parser
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
      if (isAbstractOrNonPublicClass(null)) {
        return; // skip if class is abstract or non-public
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
//       e.printStackTrace();
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
              if (!checkModifier(cd, Modifier.Keyword.PUBLIC)) { // return null if contructor is not public
                return null;
              }
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

  private static boolean checkModifier(NodeWithModifiers n, Modifier.Keyword k) {
    for (Object m : n.getModifiers()) {
      if (((Modifier)m).getKeyword().equals(k)) {
        return true;
      }
    }
    return false;
  }

  private boolean isAbstractOrNonPublicClass(ClassOrInterfaceDeclaration cid) throws Exception {
    if (this.cu == null) {
      throw new Exception("Can't determine if class is abstract: this.cu is null.");
    }
    if (cid == null) {
      for (TypeDeclaration td: this.cu.getTypes()) {
        if (td instanceof ClassOrInterfaceDeclaration) {
          cid = (ClassOrInterfaceDeclaration) td;
          break;
        }
      }
    }
    boolean isAbstractClass = checkModifier(cid, Modifier.Keyword.ABSTRACT);
    boolean isPublicClass = checkModifier(cid, Modifier.Keyword.PUBLIC);
    return isAbstractClass || !isPublicClass;
  }

  private class MethodCollector extends VoidVisitorAdapter<List<MethodInfo>> {
    @Override
    public void visit(MethodDeclaration md, List<MethodInfo> collector) {
      super.visit(md, collector);

      // Skip if method is private
      if (md.getModifiers().contains(Modifier.Keyword.PRIVATE)) {
        // System.out.println("Encountered private method: " + md.getNameAsString());
        return;
      }
      String methodName = md.getNameAsString();
      String className = null;
      Optional<Node> opt = md.getParentNode();
      if (opt.isPresent()) {
        try {
          TypeDeclaration t = (TypeDeclaration) opt.get();
          // if parent node is not public class, skip this method
          if (!(
            t instanceof ClassOrInterfaceDeclaration &&
            checkModifier((ClassOrInterfaceDeclaration) t, Modifier.Keyword.PUBLIC)
          )) {
            // System.out.printf("Encountered method in private inner class: %s.%s\n", t.getNameAsString(), md.getNameAsString());
            return;
          }
          className = t.getNameAsString();
        } catch (Exception e) {
          System.err.printf("Failed to get class name for method: %s\n", methodName);
          return;
        }
      }
      String packageName = getPackageName();
      MethodInfo info = new MethodInfo(methodName, className, packageName);
      // store return type
      String returnType = resolveType(md.getType());
      if (returnType == null) {
        System.err.printf("Unable to resolve return type %s at %s\n", md.getType(), methodName);
        return;
      }
      info.setReturnType(returnType);
      // store parameter type
      // NOTE: add instance type to parameterTypes since we need it to construct a new method call.
      info.addParameterType(packageName + "." + className);
      for (Parameter p : md.getParameters()) {
        String paramType = resolveType(p.getType());
        if (paramType == null) {
          System.err.printf("Unable to resolve parameter type %s at %s\n", p.getType(), methodName);
          return;
        }
        info.addParameterType(paramType);
      }
      collector.add(info);
      // System.out.println("info: " + info);
    }
  }
}
