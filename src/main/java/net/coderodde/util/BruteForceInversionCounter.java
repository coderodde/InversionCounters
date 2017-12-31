package net.coderodde.util;

import java.util.Comparator;
import java.util.Objects;
import static net.coderodde.util.Utils.NATURAL_ORDER;
import static net.coderodde.util.Utils.checkIndices;

/**
 * This class implements a brute force inversion counting algorithm that runs in
 * quadratic time.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 30, 2017)
 */
public final class BruteForceInversionCounter {

    public static <T> int count(T[] array, 
                                int fromIndex,
                                int toIndex, 
                                Comparator<? super T> comparator) {
        Objects.requireNonNull(array);
        Objects.requireNonNull(comparator);
        checkIndices(array.length, fromIndex, toIndex);
        int inversions = 0;

        for (int i = fromIndex; i < toIndex; ++i) {
            for (int j = i + 1; j < toIndex; ++j) {
                if (comparator.compare(array[i], array[j]) > 0) {
                    inversions++;
                }
            }
        }

        return inversions;
    }

    public static <T> int count(T[] array, int fromIndex, int toIndex) {
        return count(array, fromIndex, toIndex, NATURAL_ORDER);
    }

    public static <T> int count(T[] array, Comparator<? super T> comparator) {
        Objects.requireNonNull(array);
        return count(array, 0, array.length, comparator);
    }

    public static <T> int count(T[] array) {
        return count(array, NATURAL_ORDER);
    }

    private BruteForceInversionCounter() {}
}
