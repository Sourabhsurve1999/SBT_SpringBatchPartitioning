package com.sourabhsurve.SBT_SpringBatchPartitioning.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class ColumnRangePartitioner implements Partitioner {
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int min = 1;
        int max = 10;
        int targetSize = (max - min) / gridSize + 1;//5
        System.out.println("targetSize : " + targetSize);
        Map<String, ExecutionContext> result = new HashMap<>();

        int number = 0;
        int start = min;
        int end = start + targetSize - 1;
        //1 to 5
        // 6 to 10
        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }
            value.putInt("minValue", start);
            value.putInt("maxValue", end);
            start += targetSize;
            end += targetSize;
            number++;
        }
        System.out.println("partition result:" + result.toString());
        return result;
    }

}
