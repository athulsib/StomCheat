package me.athulsib.stomcheat.utils.math;

import java.util.Collections;
import java.util.List;

public class MathUtil {

    public static double giniCoefficient(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        if (values.size() == 1) return 0.0;

        // Sort the values
        Collections.sort(values);

        // Calculate cumulative sums
        double cumulativeSum = 0.0;
        double totalSum = 0.0;
        double weightedSum = 0.0;

        for (int i = 0; i < values.size(); i++) {
            double value = values.get(i);
            cumulativeSum += value;
            weightedSum += (i + 1) * value;
            totalSum += value;
        }

        if (totalSum == 0) return 0.0;

        // Gini coefficient formula
        double n = values.size();
        double gini = (2.0 * weightedSum) / (n * totalSum) - (n + 1.0) / n;

        return gini;
    }
}
