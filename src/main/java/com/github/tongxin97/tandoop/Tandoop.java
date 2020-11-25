package com.github.tongxin97.tandoop;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.net.URLClassLoader;
import java.net.URL;

import com.github.tongxin97.tandoop.parser.MethodParser;
import com.github.tongxin97.tandoop.method.MethodInfo;
import com.github.tongxin97.tandoop.method.MethodPool;
import com.github.tongxin97.tandoop.util.ClassUtils;
import com.github.tongxin97.tandoop.util.Rand;
import com.github.tongxin97.tandoop.util.Str;
import com.github.tongxin97.tandoop.value.*;
import com.github.tongxin97.tandoop.sequence.Sequence;

/**
 * Main logic for Tandoop
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class Tandoop {
    public static URLClassLoader classLoader;
    public static Map<String, Set<String>> inheritanceMap;
    private Map<String, TypedValuePool> valuePool;
    private MethodPool methodPool;

    private Set<Sequence> errorSeqs;
    private Set<Sequence> nonErrorSeqs;

    private final int maxRepetition = 100;
    private final double repetitionProb = 0.1;

    private String srcDir;
    private String prjDir;

    public CoverageAnalyzer coverageAnalyzer;
    public PrintStream coverageInfoOut;

    final public static String tandoopTestFile = "src/test/java/com/github/tongxin97/tandoop/TandoopTest.java";

    public Tandoop(String srcDir, String prjDir) throws Exception {
        if (srcDir == null || prjDir == null) {
            throw new IllegalArgumentException(
                String.format("Parameters can't be null: srcDir=%s, prjDir=%s", srcDir, prjDir)
            );
        }
        // init error/non-error method sequences, and method/value pool
        errorSeqs = new LinkedHashSet<>();
        nonErrorSeqs = new LinkedHashSet<>();
        methodPool = new MethodPool();
        inheritanceMap = new HashMap<>();
        valuePool = new HashMap<>();

        this.srcDir = srcDir;
        this.prjDir = prjDir;

        // load target project dependencies
        File dir = new File(prjDir + "/target/dependency");
        File[] dirListing = dir.listFiles();
        URL[] urls = new URL[dirListing.length + 1];
        for (int i = 0; i < dirListing.length; ++i) {
            urls[i] = dirListing[i].toURI().toURL();
        }
        urls[dirListing.length] = new File(prjDir + "/target/classes").toURI().toURL();
        classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

        // parse all accessible class methods in the target project
        MethodParser.parseAndResolveDirectory(srcDir, prjDir, methodPool);
//        System.out.println("inheritance map: \n" + inheritanceMap);
        try {
            String filename = "inheritance.txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(inheritanceMap.toString());
            writer.close();
        } catch (Exception e) {
            System.err.println("Failed to write inheritanceMap: " + e.getMessage());
            e.printStackTrace();
        }

        this.initPrimitiveValuePool();
        // System.out.println("ValuePool:\n" + valuePool);

        // setup coverage analyzer
        coverageInfoOut = new PrintStream(new FileOutputStream("coverageInfo", true));
        coverageInfoOut.printf("prjDir: %s, ", prjDir);
        coverageAnalyzer = new CoverageAnalyzer(prjDir, coverageInfoOut, classLoader);
    }

    private void setExtensibleFlag(Sequence newSeq, MethodInfo method, VarInfo var, Object result) {
        // if runtime value is null, set extensible to false and return
        if (result.toString().startsWith("[Tandoop] F: ")) {
            var.Extensible = false;
            return;
        }
        // otherwise, if runtime value equals to an old value, set extensible flag to false
        String returnType = method.getReturnType();
        var.Val = result;

        boolean equalsToOldValue = this.valuePool.containsKey(returnType) && this.valuePool.get(returnType).contains(var.Val);
        var.Extensible = !equalsToOldValue;
    }

    private void initPrimitiveValuePool() {
        // boolean type
        String booleanType = boolean.class.getName();
        this.valuePool.put(booleanType, new TypedValuePool(booleanType, Arrays.asList(true, false)));
        inheritanceMap.put(booleanType, new HashSet<>(Arrays.asList(booleanType)));
        ClassUtils.collectSubClassInfo(booleanType, inheritanceMap, classLoader);
        // char type
        String charType = char.class.getName();
        this.valuePool.put(charType, new TypedValuePool(charType, Arrays.asList(
                'a', 'z', 'B', '\t'
        )));
        inheritanceMap.put(charType, new HashSet<>(Arrays.asList(charType)));

        // byte type
        String byteType = byte.class.getName();
        this.valuePool.put(byteType, new TypedValuePool(byteType, Arrays.asList(
                -128, 0, 127
        )));
        inheritanceMap.put(byteType, new HashSet<>(Arrays.asList(byteType)));

        // int type
        String intType = int.class.getName();
        this.valuePool.put(intType, new TypedValuePool(intType, Arrays.asList(
                0, 1, -1, 1000, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE
        )));
        inheritanceMap.put(intType, new HashSet<>(Arrays.asList(intType)));

        // short type
        String shortType = short.class.getName();
        this.valuePool.put(shortType, new TypedValuePool(shortType, Arrays.asList(
                0, 1, -1, 100, -100, Short.MAX_VALUE, Short.MIN_VALUE
        )));
        inheritanceMap.put(shortType, new HashSet<>(Arrays.asList(shortType)));

        // long type
        String longType = long.class.getName();
        this.valuePool.put(longType, new TypedValuePool(longType, Arrays.asList(
                0, 1, -1, 100000, -100000, Long.MAX_VALUE, Long.MIN_VALUE
        )));
        inheritanceMap.put(longType, new HashSet<>(Arrays.asList(longType)));

        // float type
        String floatType = float.class.getName();
        this.valuePool.put(floatType, new TypedValuePool(floatType, Arrays.asList(
                0.0, 3.14, -100, Float.MAX_VALUE, Float.MIN_VALUE, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY
        )));
        inheritanceMap.put(floatType, new HashSet<>(Arrays.asList(floatType)));

        // double type
        String doubleType = double.class.getName();
        this.valuePool.put(doubleType, new TypedValuePool(doubleType, Arrays.asList(
                0.0, 3.14, -100, Double.MAX_VALUE, Double.MIN_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        )));
        inheritanceMap.put(doubleType, new HashSet<>(Arrays.asList(doubleType)));

        // null type
        this.valuePool.put("null", new TypedValuePool("null", null));
        inheritanceMap.put("null", new HashSet<>(Arrays.asList("null")));
    }

    private ValueInfo getRandomExtensibleValFromSequences(Set<Sequence> inputSeqs, Set<Sequence> outputSeqs, String type) {
        // filter inputSeqs by whether a seq has extensible values of the given type
        List<Sequence> seqsWithGivenType = inputSeqs.stream()
            .filter(s -> s.hasExtensibleValOfType(type, inheritanceMap.get(type)))
            .collect(Collectors.toList());

//        System.out.printf("seqsWithGivenType: %s, %s, %d\n", type, seqsWithGivenType, seqsWithGivenType.size());

        if (seqsWithGivenType.size() > 0) {
            Sequence s = Rand.getRandomCollectionElement(seqsWithGivenType);
            outputSeqs.add(s); // add s to output seqs set
            return s.getRandomExtensibleValOfType(type, inheritanceMap.get(type));
        }
        return null;
    }

    private int getRandomSeqsAndVals(Set<Sequence> seqs, List<ValueInfo> vals, final List<String> types) {
//        System.out.println("types: " + types);
        for (String type: types) {
            if (ClassUtils.isPrimitiveType(type)) {
                vals.add(new ValueInfo(type, valuePool.get(type).getRandomValue()));
            } else {
                // 3 possible choices for v
                // 1) v = null
                ValueInfo v = null;
                // 2) use a value v from a sequence that is already in seqs
                v = this.getRandomExtensibleValFromSequences(seqs, seqs, type);
                // 3) select a (possibly duplicate) sequence from nonErrorSeqs, add it to seqs, and use a value from it
                if (v == null) {
                    v = this.getRandomExtensibleValFromSequences(this.nonErrorSeqs, seqs, type);
                }
                vals.add(v);
            }
        }
        return 0;
    }

    /**
     * Decides how many times to repeatedly append the new statement to the end of
     * the new sequence.
     * @return the number of times to repeat.
     */
    private int getNumOfRepetition() {
        int numRounds = (int) Math.round(1.0/this.repetitionProb); // 10 by default
        int i = Rand.getRandomInt(numRounds);
        if (i > 0) {
            return 1; // do not repeat with (1-repetitionProb) probability
        }
        // repeat [0, maxRepetition) times with repetitionProb
        return Rand.getRandomInt(this.maxRepetition);
    }

    /**
     * Generate a new statement to append to the end of the new sequence.
     * The new statement may be repeated up to maxRepetition-1 times.
     * @return the new statement as a string.
     */
    private String genNewStatements(MethodInfo method, Sequence newSeq, VarInfo var, List<ValueInfo> vals) {
        // if method is constructor, sentence = Type Type1 = new methodName(p0,p1,...);\n
        // otherwise, sentence = Type Type1 = p0.methodName(p1,p2,...);\n
        StringBuilder b = new StringBuilder();
        if (method.returnType == null || !method.returnType.equals("void")) {
            b.append(String.format("      %s %s = ", method.getReturnType(), var.getContent()));
            newSeq.NewVar = var.getContent();
        }
        String typeDeclaration = b.toString();

        int start;
        if (method.IsConstructor()) {
            b.append(String.format("new %s(", method.getFullyQualifiedMethodName()));
            start = 0;
        } else {
            b.append(String.format("%s.%s(", vals.get(0).getContent(), method.Name));
            start = 1;
        }
        for (int i = start; i < vals.size(); ++i) {
            if (i > start) {
                b.append(',');
            }
            if (vals.get(i) == null) {
                b.append(String.format("(%s) null", method.getParameterTypeAtIdx(i)));
                newSeq.InputParamsWithNull = true;
            } else {
                b.append(vals.get(i).getContent());
            }
        }
        b.append(");\n");

        // skip constructor for statement repetition
        if (method.IsConstructor()) {
            return b.toString();
        }

        int repeat = this.getNumOfRepetition();
        if (repeat == 0) {
            return "";
        }
        if (repeat == 1) {
            return b.toString();
        }
        // create string for repeated statement
        StringBuilder repeatedStatement = new StringBuilder(typeDeclaration + "null;\n");
        repeatedStatement.append(String.format("      for (int i=0; i<%d; i++) {\n", repeat));
        String methodCall = b.toString().substring(typeDeclaration.length());
        repeatedStatement.append(String.format("        %s = %s", var.getContent(), methodCall));
        repeatedStatement.append("      }\n");

        return repeatedStatement.toString();
    }

    public Sequence extend(MethodInfo method, VarInfo var, Set<Sequence> seqs, List<ValueInfo> vals) {
        Sequence newSeq = new Sequence();
        // merge seqs to one seq: methods, vals, and executable sequence string
        // Q: how to generate equivalent sequences & how to set modulo variable names?
        for (Sequence seq: seqs) {
            newSeq.addMethods(seq.Methods);
            newSeq.addImports(seq.Imports);
            for (Map.Entry<String, List<ValueInfo>> entry: seq.Vals.entrySet()) {
                newSeq.addVals(entry.getKey(), entry.getValue());
            }
            newSeq.ExcSeq += seq.ExcSeq;
        }
        // add new method to newSeq
        newSeq.addMethod(method);

        String newStatements = this.genNewStatements(method, newSeq, var, vals);
        System.out.println("New statements:\n " + newStatements);
        newSeq.ExcSeq += newStatements;
        return newSeq;
    }

    private void writeSeqsToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("err_seqs.txt"));
            writer.write(this.errorSeqs.toString());
            writer.close();
            writer = new BufferedWriter(new FileWriter("non_err_seqs.txt"));
            writer.write(this.nonErrorSeqs.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateSequences(long timeLimits) throws Exception {
        this.coverageInfoOut.printf("timeLimits: %d s\n", timeLimits);
        timeLimits *= 1000;
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        while (elapsedTime < timeLimits) {
            elapsedTime = (new Date()).getTime() - startTime;
            MethodInfo method;
            try {
                method = methodPool.getRandomMethod();
            } catch (Exception e) {
                System.err.println("getRandomMethod exception: " + e.getMessage());
                break;
            }
            // System.out.printf("Selected random method: %s.%s\n", method.ClassName, method.Name);
            Set<Sequence> seqs = new LinkedHashSet<>();
            List<ValueInfo> vals = new ArrayList<>();
            if (this.getRandomSeqsAndVals(seqs, vals, method.getParameterTypes()) < 0) {
                continue;
            }
            // sanity check: instance val (vals[0]) can't be null when method is not constructor
            if (!method.IsConstructor() && vals.get(0) == null) {
//                 System.out.printf("Instance val is null: %s.%s\n", method.ClassName, method.Name);
                continue;
            }
            // Skip method if any of its associated types is generic (for now)
            boolean hasGenericType = false;
            for (String paramType: method.getParameterTypes()) {
                hasGenericType |= Str.parseNestedTypes(paramType, null);
            }
            hasGenericType |= Str.parseNestedTypes(method.getReturnType(), null);
            if (hasGenericType) {
                continue;
            }

            VarInfo var = new VarInfo(method.getSimpleReturnType());
            Sequence newSeq = this.extend(method, var, seqs, vals);
            // Check if newSeq is duplicate
            if (this.errorSeqs.contains(newSeq) || this.nonErrorSeqs.contains(newSeq)) {
                // System.out.println("Duplicate: " + newSeq.ExcSeq);
                continue;
            }
            // System.out.println(newSeq.ExcSeq);

            newSeq.generateTest();
            Object result = newSeq.runTest(this.prjDir, this.coverageAnalyzer);
            if (result.toString().startsWith("[Tandoop] E: ") || result.toString().startsWith("[Tandoop] C: ")) {
                newSeq.generateJUnitTest(
                        String.format("%s/src/test/java/", this.prjDir),
                        String.format("TandoopErrTest%d", errorSeqs.size())
                );
                if (result.toString().startsWith("[Tandoop] C: ")) {
                    errorSeqs.add(newSeq);
                }
            } else {
//                newSeq.generateJUnitTest(
//                        String.format("%s/src/test/java/", this.prjDir),
//                        String.format("TandoopRegTest%d", nonErrorSeqs.size())
//                );
                nonErrorSeqs.add(newSeq);
                setExtensibleFlag(newSeq, method, var, result);
                if (var.Extensible) {
                    String returnType = method.getReturnType();
                    newSeq.addVal(returnType, var);
                    if (this.valuePool.containsKey(returnType)) {
                        this.valuePool.get(returnType).addValue(var.Val);
                    } else {
                        boolean isPrimitiveType = result.getClass().isPrimitive();
                        this.valuePool.put(returnType, new TypedValuePool(returnType, isPrimitiveType, Arrays.asList(var.Val)));
                    }
                    System.out.println("Added extensible val:" + var.Val);
                } else {
                    System.out.printf("Non-extensible val: %s\n ", var.Val);
                }
            }
            System.out.println("-----------------------");
        }
        writeSeqsToFile();
        // remove TandoopTest.java
        File testFile = new File(tandoopTestFile);
        testFile.delete();

        this.coverageAnalyzer.end();
    }
}
