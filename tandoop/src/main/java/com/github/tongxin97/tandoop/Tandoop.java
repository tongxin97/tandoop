package com.github.tongxin97.tandoop;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

/**
 * Main logic for Tandoop
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class Tandoop {
    private Map<String, TypedValuePool> valuePool;
    private MethodPool methodPool;

    private List<Sequence> errorSeqs;
    private List<Sequence> nonErrorSeqs;

    public Tandoop(String projectDirectory) throws Exception {
        // init error/non-error method sequences, and method/value pool
        this.errorSeqs = new ArrayList<>();
        this.nonErrorSeqs = new ArrayList<>();
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
        System.out.println("MethodPool:\n" + this.methodPool);

        this.initPrimitiveValuePool();
        System.out.println("ValuePool:\n" + this.valuePool);

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

    private void getRandomSeqsAndVals(List<Sequence> seqs, List<Object> vals, List<String> types) {
        for (String type: types) {
            if (this.valuePool.containsKey(type)) {
                vals.add(this.valuePool.get(type).getRandomValue());
            } else {
                Object v = null;
                // 3 possible choices
                int r = Utils.getRandomInt(3);
                switch(r) {
                case 0: // use a value v from a sequence that is already in seqs
                    for (Sequence s: seqs) {
                        v = s.getReturnValOfType(type);
                        if (v != null) {
                            break;
                        }
                    }
                    break;
                case 1: // select a (possibly duplicate) sequence from nonErrorSeqs, add it to seqs, and use a value from it
                    for (Sequence s: this.nonErrorSeqs) {
                        v = s.getReturnValOfType(type);
                        if (v != null) {
                            seqs.add(s);
                            break;
                        }
                        break;
                    }
                default: // use null
                }
                vals.add(v);
            }
        }
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
            List<Sequence> seqs = new ArrayList<>();
            List<Object> vals = new ArrayList<>();
            this.getRandomSeqsAndVals(seqs, vals, method.getParameterTypes());

            // TODO: newSeqs <- extend(m, seqs, vals)
            --timeLimits;
        }
        return new Sequence();
    }
}
