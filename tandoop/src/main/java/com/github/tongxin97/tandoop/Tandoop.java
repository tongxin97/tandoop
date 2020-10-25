package com.github.tongxin97.tandoop;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.util.stream.Collectors;

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

    public Tandoop(String projectDirectory) throws Exception {
        // init error/non-error method sequences, and method/value pool
        this.errorSeqs = new LinkedHashSet<>();
        this.nonErrorSeqs = new LinkedHashSet<>();
        this.methodPool = new MethodPool();
        this.valuePool = new HashMap<>();

        // Note: need to use the directory of source code (e.g., ../joda-time/src/main)
        // There are errors when trying to parse java test classes
        walkThroughDirectory(new File(projectDirectory));
    }

    private void walkThroughDirectory(File dir) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) {
            System.err.printf("%s is not a directory.\n", dir.getPath());
            return;
        }
        System.out.println(dir.getPath());
        for (File file: files) {
            if (file.isDirectory()) {
                walkThroughDirectory(file);
            } else {
                System.out.println(file.getPath());
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

        this.initPrimitiveValuePool();
        // System.out.println("ValuePool:\n" + this.valuePool);

        this.generateSequence(1);
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

    private ValInfo getRandomExtensibleValFromSequences(Set<Sequence> inputSeqs, Set<Sequence> outputSeqs, String type) {
        // filter inputSeqs by whether a seq has extensible values of the given type
        List<Sequence> seqsWithGivenType = inputSeqs.stream()
            .filter(s -> s.hasExtensibleValOfType(type))
            .collect(Collectors.toList());

        if (seqsWithGivenType.size() > 0) {
            int i = Utils.GetRandomInt(seqsWithGivenType.size());
            Sequence s = seqsWithGivenType.get(i);
            outputSeqs.add(s); // add s to output seqs set
            return s.getRandomExtensibleValOfType(type);
        }
        return null;
    }

    private void getRandomSeqsAndVals(Set<Sequence> seqs, List<ValInfo> vals, List<String> types) {
        for (String type: types) {
            if (this.valuePool.containsKey(type)) {
                vals.add(new PrimitiveInfo(type, this.valuePool.get(type).getRandomValue()));
            } else {
                ValInfo v = null;
                // 3 possible choices
                int r = Utils.GetRandomInt(3);
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
        // System.out.printf("Values: ");
        // for (ValInfo val: vals) {
        //     if (val == null) {
        //         System.out.printf("null\t");
        //     } else {
        //         System.out.printf("%s\t", val.getContent());
        //     }
        //     System.out.println("");
        // }
    }

    public Sequence extend(MethodInfo method, Set<Sequence> seqs, List<ValInfo> vals) {
        Sequence newSeq = new Sequence();
        // merge seqs to one seq: methods, vals, and executable sequence string
        // Q: how to generate equivalent sequences & how to set modulo variable names?
        for (Sequence seq: seqs) {
            newSeq.Methods.addAll(seq.Methods);
            for (Map.Entry<String, List<List<ValInfo>>> entry: seq.Vals.entrySet()) {
                newSeq.AddVals(entry.getKey(), entry.getValue().get(0));
                newSeq.AddVals(entry.getKey(), entry.getValue().get(1));
            }
            newSeq.ExcSeq += seq.ExcSeq;
        }
        // add new method to newSeq
        newSeq.Methods.add(method);
        VarInfo var = new VarInfo(method.ReturnType);
        // TODO: set extensible flag and add new return Value to Vals. Q: Is new generated value extensible?
        var.Extensible = true;
        newSeq.AddVal(method.ReturnType, var);
        // TODO: diffrent construction rule for Constructors
        // e.g. Type Type1 = p0.methodName(p1,p2,...);\n
        String sentence = "    " + method.ReturnType + ' ' + var.getContent() + ' ';
        sentence += vals.get(0).getContent() + '.' + method.Name + '(';
        for (int i = 1; i < vals.size(); ++i) {
            if (i > 1) {
                sentence += ',';
            }
            if (vals.get(i) == null) {
                sentence += "null";
            } else {
                sentence += vals.get(i).getContent();
            }
        }
        sentence += ");\n";
        newSeq.ExcSeq += sentence;
        return newSeq;
    }

    // TODO add arguments: contracts, filters, timeLimits
    public Sequence generateSequence(int timeLimits) {
        while (timeLimits > 0) {
            MethodInfo method;
            try {
                method = methodPool.getRandomMethod();
            } catch (Exception e) {
                System.err.println("Uncaught exception: " + e.getMessage());
                break;
            }
            System.out.printf("Selected random method: %s.%s\n", method.ClassName, method.Name);
            Set<Sequence> seqs = new LinkedHashSet<>();
            List<ValInfo> vals = new ArrayList<>();
            List<String> types = method.GetParameterTypes();
            if (!method.IsConstructor()) {
                // prepend class name to list since we need instance type
                types.add(0, method.ClassName);
            }
            this.getRandomSeqsAndVals(seqs, vals, types);
            System.out.printf("Seqs: %s, Vals %s\n", seqs, vals);
            if (!method.IsConstructor() && vals.get(0) == null) { // sanity check: instance val can't be null
                --timeLimits;
                System.out.println("Instance val is null");
                continue;
            }

            Sequence newSeq = this.extend(method, seqs, vals);
            // TODO: check if newSeq is duplicate
            if (this.errorSeqs.contains(newSeq) || this.nonErrorSeqs.contains(newSeq)) {
                System.out.println("Duplicate: " + newSeq.ExcSeq);
                continue;
            }
            // System.out.println(newSeq.ExcSeq);
            // newSeq.generateTest()
            // TODO execute newSeq
            // TODO check contracts
            // TODO apply filters and add to err/nonerr sets
            nonErrorSeqs.add(newSeq);
            --timeLimits;
        }
        return new Sequence();
    }
}
