package com.ai.anomaly.anomalydetectionservice.util;
import java.util.List;

public class StatisticsUtil {

    public static double average(List<Double> values) {

        return values.stream().mapToDouble(Double::doubleValue).
                average().orElse(0);
    }

    public static double standardDeviation(List<Double> values, double average) {
        if (values.isEmpty()) {
            return 0;
        }

        double variance = values.stream().
                mapToDouble(value -> Math.pow(value - average, 2)).
                average().orElse(0);

        return Math.sqrt(variance);
    }
}
