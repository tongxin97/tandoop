package com.github.tongxin97.tandoop;

import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.util.stream.Collectors;

import com.github.tongxin97.tandoop.parser.MethodParser;
import com.github.tongxin97.tandoop.method.MethodInfo;
import com.github.tongxin97.tandoop.method.MethodPool;
import com.github.tongxin97.tandoop.util.Rand;
import com.github.tongxin97.tandoop.value.*;
import com.github.tongxin97.tandoop.sequence.Sequence;

/**
 * Main logic for Tandoop
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class Tandoop {
    private Map<String, TypedValuePool> valuePool;
    private MethodPool methodPool;

    private Set<Sequence> errorSeqs;
    private Set<Sequence> nonErrorSeqs;

    private final int maxRepetition = 100;
    private final double repetitionProb = 1;

    String srcDir;
    String testDir;
    String prjDir;

    public Tandoop(String srcDir, String testDir, String prjDir) throws Exception {
        // init error/non-error method sequences, and method/value pool
        this.errorSeqs = new LinkedHashSet<>();
        this.nonErrorSeqs = new LinkedHashSet<>();
        this.methodPool = new MethodPool();
        this.valuePool = new HashMap<>();

        this.srcDir = srcDir;
        this.testDir = testDir;
        this.prjDir = prjDir;

        // Note: need to use the directory of source code (e.g., ../joda-time/src/main)
        // There are errors when trying to parse java test classes
        walkThroughDirectory(new File(srcDir));

        this.initPrimitiveValuePool();
        // System.out.println("ValuePool:\n" + this.valuePool);
    }

    private void walkThroughDirectory(File dir) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) {
            System.err.printf("%s is not a directory.\n", dir.getPath());
            return;
        }
        // System.out.println(dir.getPath());
        for (File file: files) {
            if (file.isDirectory()) {
                walkThroughDirectory(file);
            } else {
                // System.out.println(file.getPath());
                if (file.getName().endsWith(".java")) {
                    parseFile(file.getPath());
                }
            }
        }
    }

    private void parseFile(String file) throws Exception {
        MethodParser methodParser = new MethodParser(file);
        methodParser.CollectMethodInfo(this.methodPool);
        // System.out.println("MethodPool:\n" + this.methodPool);
    }

    private void initPrimitiveValuePool() {
        // int type
        TypedValuePool intValuePool = new TypedValuePool("int", Arrays.asList(
            0, 1, -1, 1000, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE
        ));
        this.valuePool.put("int", intValuePool);
        // String type
        TypedValuePool stringValuePool = new TypedValuePool("String", Arrays.asList(
            "", "a", "abc", "0", "3.14", "\n"
        ));
        this.valuePool.put("String", stringValuePool);
        // null type
        TypedValuePool nullValueSingleton = new TypedValuePool("null", null);
        this.valuePool.put("null", nullValueSingleton);

        // TODO add more initial primitive types
    }

    private ValueInfo getRandomExtensibleValFromSequences(Set<Sequence> inputSeqs, Set<Sequence> outputSeqs, String type) {
        // filter inputSeqs by whether a seq has extensible values of the given type
        List<Sequence> seqsWithGivenType = inputSeqs.stream()
            .filter(s -> s.hasExtensibleValOfType(type))
            .collect(Collectors.toList());

        if (seqsWithGivenType.size() > 0) {
            int i = Rand.getRandomInt(seqsWithGivenType.size());
            Sequence s = seqsWithGivenType.get(i);
            outputSeqs.add(s); // add s to output seqs set
            return s.getRandomExtensibleValOfType(type);
        }
        return null;
    }

    private void getRandomSeqsAndVals(Set<Sequence> seqs, List<ValueInfo> vals, List<String> types) {
        for (String type: types) {
            if (this.valuePool.containsKey(type)) {
                vals.add(new PrimitiveInfo(type, this.valuePool.get(type).getRandomValue()));
            } else {
                ValueInfo v = null;
                // 3 possible choices
                int r = Rand.getRandomInt(3);
                switch(r) {
                case 0: // use a value v from a sequence that is already in seqs
                    v = this.getRandomExtensibleValFromSequences(seqs, seqs, type);
                    break;
                case 1: // select a (possibly duplicate) sequence from nonErrorSeqs, add it to seqs, and use a value from it
                    v = this.getRandomExtensibleValFromSequences(this.nonErrorSeqs, seqs, type);
                default: // use null
                }
                vals.add(v);
            }
        }
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
        if (method.ReturnType != "void") {
            b.append(String.format("      %s %s = ", method.ReturnType, var.getContent()));
            newSeq.NewVar = var.getContent();
        }
        String typeDeclaration = b.toString();

        int start;
        if (method.IsConstructor()) {
            b.append(String.format("new %s(", method.Name));
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
                b.append("null");
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

    public Sequence extend(MethodInfo method, Set<Sequence> seqs, List<ValueInfo> vals) {
        Sequence newSeq = new Sequence();
        // merge seqs to one seq: methods, vals, and executable sequence string
        // Q: how to generate equivalent sequences & how to set modulo variable names?
        for (Sequence seq: seqs) {
            newSeq.addMethods(seq.Methods);
            newSeq.addImports(seq.Imports);
            for (Map.Entry<String, List<List<ValueInfo>>> entry: seq.Vals.entrySet()) {
                newSeq.addVals(entry.getKey(), entry.getValue().get(0));
                newSeq.addVals(entry.getKey(), entry.getValue().get(1));
            }
            newSeq.ExcSeq += seq.ExcSeq;
        }
        // add new method to newSeq
        newSeq.addMethod(method);
        VarInfo var = new VarInfo(method.ReturnType);
        // TODO: set extensible flag and add new return Value to Vals. Q: Is new generated value extensible?
        var.Extensible = true;
        newSeq.addVal(method.ReturnType, var);
        newSeq.addImport(String.format("import %s.%s;\n", method.PackageName, method.ClassName));

        String newStatements = this.genNewStatements(method, newSeq, var, vals);
        System.out.println("New statements:\n " + newStatements);
        newSeq.ExcSeq += newStatements;
        return newSeq;
    }

    // TODO add arguments: contracts, filters, timeLimits
    public Sequence generateSequence(int timeLimits) throws Exception {
        while (timeLimits > 0) {
            MethodInfo method;
            try {
                method = methodPool.getRandomMethod();
            } catch (Exception e) {
                System.err.println("Uncaught exception: " + e.getMessage());
                break;
            }
            // System.out.printf("Selected random method: %s.%s\n", method.ClassName, method.Name);
            Set<Sequence> seqs = new LinkedHashSet<>();
            List<ValueInfo> vals = new ArrayList<>();
            List<String> types = method.GetParameterTypes();
            if (!method.IsConstructor()) {
                // prepend class name to  types list since we need instance type when method is not constructor
                types.add(0, method.ClassName);
            }
            this.getRandomSeqsAndVals(seqs, vals, types);
            // System.out.printf("Seqs: %s, Vals %s\n", seqs, vals);
            // sanity check: instance val (vals[0]) can't be null when method is not constructor
            if (!method.IsConstructor() && vals.get(0) == null) {
                --timeLimits;
                // System.out.println("Instance val is null");
                continue;
            }

            Sequence newSeq = this.extend(method, seqs, vals);
            // Check if newSeq is duplicate
            if (this.errorSeqs.contains(newSeq) || this.nonErrorSeqs.contains(newSeq)) {
                // System.out.println("Duplicate: " + newSeq.ExcSeq);
                continue;
            }
            // System.out.println(newSeq.ExcSeq);

            newSeq.generateTest(this.testDir);
            int returnVal = newSeq.runTest(this.prjDir);

            // TODO check contracts
            // TODO apply filters and add to err/nonerr sets

            System.out.println("Return Val: " + returnVal);
            if (returnVal == 0) {
                nonErrorSeqs.add(newSeq);
                // setExtensibleFlags(newSeq, filter, runtimevalues)
            } else {
                errorSeqs.add(newSeq);
            }
            // System.out.println("nonErrorSeqs: " + nonErrorSeqs);
            --timeLimits;
        }
        return new Sequence();
    }
}
