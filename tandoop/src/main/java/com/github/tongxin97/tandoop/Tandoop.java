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
        System.out.println(dir.getPath());
        File[] files = dir.listFiles();
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
        System.out.println(this.valuePool);
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

    public Sequence generateSequence(Integer timeLimits) throws Exception { // add arguments: contracts, filters, timeLimits
        while (timeLimits > 0) {
            MethodInfo method = methodPool.getRandomMethod();


            // TODO: <seqs, vals> <- randomSeqsAndVals();
            // TODO: newSeqs <- extend(m, seqs, vals)
            --timeLimits;
        }
        return new Sequence();
    }
}
