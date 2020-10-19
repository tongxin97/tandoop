package com.github.tongxin97;

import java.util.HashMap;
import com.github.javaparser.ast.type.Type;

/**
 * Main logic for Tandoop
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class Tandoop {
    private HashMap<Type, ValuePool> valuePools;
    private MethodPool methodPool;

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
