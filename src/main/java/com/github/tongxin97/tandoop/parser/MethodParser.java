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
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;

import java.util.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.tongxin97.tandoop.Tandoop;
import com.github.tongxin97.tandoop.method.MethodInfo;
import com.github.tongxin97.tandoop.method.MethodPool;
import com.github.tongxin97.tandoop.util.ClassUtils;

public class MethodParser {
  private CompilationUnit cu;
  private String packageName;
  private ClassOrInterfaceDeclaration cd;

  public MethodParser(CompilationUnit cu, String srcDir) throws Exception {
    if (cu == null || srcDir == null) {
      throw new IllegalArgumentException(String.format("Parameters can't be null: cu=%s, srcDir=%s", cu, srcDir));
    }
    this.cu = cu;
    this.packageName = getPackageName();
    Tandoop.pkgs.add(this.packageName);
    cd = getPublicClassDeclaration();
  }

  public static void parseAndResolveDirectory(
          String srcDir,
          String prjDir,
          MethodPool methodPool
  ) throws Exception {
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
      if (cd == null) {
        return; // skip if class is non-public
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("collectMethodInfo exception: " + e.getMessage());
      return;
    }

//    System.out.printf("collectMethodInfo: %s\n", cd.getNameAsString());

    VoidVisitor<MethodPool> methodCollector = new MethodCollector();
    Set<MethodInfo> constructorInfo = new HashSet<>();
    this.getConstructorInfo(constructorInfo);

    ClassUtils.collectInheritanceInfo(getPackageName() + "." + cd.getNameAsString(), Tandoop.inheritanceMap, Tandoop.classLoader);

    // record a fully qualified classname in methodPool and visit other methods in this class
    for (MethodInfo m: constructorInfo) {
      methodPool.addMethod(m);
    }
    methodCollector.visit(this.cu, methodPool);
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
    if (simpleType.asString().contains("?")) {
      return null;
    }
    try {
      return simpleType.resolve().describe();
    } catch (Exception e) {
//       e.printStackTrace();
    }
    return null;
  }

  /**
   * getConstructorInfo creates MethodInfo instances for the public, non-abstract constructors.
   * As a side effect, it adds each of these classes' names to a set (used by the code coverage tool).
   * @return MethodInfo if a public constructor exists; null otherwise.
   */
  private void getConstructorInfo(Set<MethodInfo> infoList) {
    if (cd.isAbstract()) {
      return; // don't read constructor info for abstract class
    }
    for (TypeDeclaration td: this.cu.getTypes()) {
      List<BodyDeclaration> bds = td.getMembers();
      if(bds != null) {
        outerLoop:
        for (BodyDeclaration bd: bds) {
          if (bd instanceof ConstructorDeclaration) {
              ConstructorDeclaration cd = (ConstructorDeclaration) bd;
              // continue if constructor is abstract or non-public
              if (cd.isAbstract() || !cd.isPublic()) {
                continue;
              }
              String constructorName = cd.getNameAsString();
              MethodInfo info = new MethodInfo(constructorName, constructorName, packageName, true);
              for (Parameter p : cd.getParameters()) {
                String paramType = this.resolveType(p.getType());
                if (paramType == null) {
//                  System.err.printf("Unable to resolve parameter type %s at %s\n", p.getType(), constructorName);
                  break outerLoop;
                }
                info.addParameterType(paramType);
              }
              infoList.add(info);
//              System.out.println(info);
          }
        }
      }
    }
  }

  private ClassOrInterfaceDeclaration getPublicClassDeclaration() {
    for (TypeDeclaration td: this.cu.getTypes()) {
      if (td instanceof ClassOrInterfaceDeclaration) {
        if (!((ClassOrInterfaceDeclaration) td).isInterface() && td.isPublic()) {
          return (ClassOrInterfaceDeclaration) td;
        }
      }
    }
    return null;
  }

  // TODO: handle static method differently
  private class MethodCollector extends VoidVisitorAdapter<MethodPool> {
    @Override
    public void visit(MethodDeclaration md, MethodPool methodPool) {
      super.visit(md, methodPool);

      // Skip if method is abstract or non-public
      if (md.isAbstract() || !md.isPublic()) {
        // System.out.println("Encountered abstract πor non-public method: " + md.getNameAsString());
        return;
      }
      String methodName = md.getNameAsString();
      String className = null;
      Optional<Node> opt = md.getParentNode();
      if (opt.isPresent()) {
        try {
          TypeDeclaration t = (TypeDeclaration) opt.get();
          // if parent node is not public class, skip this method
          if (!(t instanceof ClassOrInterfaceDeclaration && t.isPublic())) {
            // System.out.printf("Encountered method in private inner class: %s.%s\n", t.getNameAsString(), md.getNameAsString());
            return;
          }
          className = t.getNameAsString();
        } catch (Exception e) {
//          System.err.println("[Error] MethodCollector: " + e.getMessage());
          return;
        }
      }
      MethodInfo info = new MethodInfo(methodName, className, packageName);
      // set if method is static
      info.isStatic = md.isStatic();
      // store return type
      String returnType = resolveType(md.getType());
      if (returnType == null) {
//        System.err.printf("Unable to resolve return type %s at %s\n", md.getType(), methodName);
        return;
      }
      info.setReturnType(returnType);
      ClassUtils.collectInheritanceInfo(returnType, Tandoop.inheritanceMap, Tandoop.classLoader);
      // store parameter type
      // NOTE: add instance type to parameterTypes since we need it to construct a new method call.
      if (!info.isStatic) {
        info.addParameterType(packageName + "." + className);
      }
      for (Parameter p : md.getParameters()) {
        String paramType = resolveType(p.getType());
        if (paramType == null) {
//          System.err.printf("Unable to resolve parameter type %s at %s\n", p.getType(), methodName);
          return;
        }
        info.addParameterType(paramType);
        ClassUtils.collectInheritanceInfo(paramType, Tandoop.inheritanceMap, Tandoop.classLoader);
      }
      methodPool.addMethod(info);
      // System.out.println("info: " + info);
    }
  }
}
