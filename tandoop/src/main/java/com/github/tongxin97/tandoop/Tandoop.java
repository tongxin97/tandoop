package com.github.tongxin97.tandoop;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.github.javaparser.ast.type.Type;

/**
 * Main logic for Tandoop
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class Tandoop {
    private Map<Type, TypedValuePool> valuePool;
    private MethodPool methodPool;

    private List<Sequence> errorSeqs;
    private List<Sequence> nonErrorSeqs;

    public Tandoop(String projectDirectory) throws Exception {
        // init error/non-error method sequences, and method/value pool
        this.errorSeqs = new ArrayList<>();
        this.nonErrorSeqs = new ArrayList<>();
        this.methodPool = new MethodPool();
        this.valuePool = new HashMap<>();

        // TODO parse given directory
        MethodParser methodParser = new MethodParser(projectDirectory);
        methodParser.CollectMethodInfo(this.methodPool);
        System.out.println("MethodPool:\n" + this.methodPool);
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
