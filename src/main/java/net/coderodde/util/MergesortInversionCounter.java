package net.coderodde.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import static net.coderodde.util.Utils.NATURAL_ORDER;
import static net.coderodde.util.Utils.checkIndices;

/**
 * This class implements a modification of merge sort that sorts an input array 
 * range and returns the number of inversions in the input range.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 30, 2017)
 */
public final class MergesortInversionCounter {


    public static <T> int count(T[] array, 
                                int fromIndex, 
                                int toIndex, 
                                Comparator<? super T> comparator) {
        Objects.requireNonNull(array);
        checkIndices(array.length, fromIndex, toIndex);
        int rangeLength = toIndex - fromIndex;

        if (rangeLength < 2) {
            return 0;
        }

        T[] aux = Arrays.copyOfRange(array, fromIndex, toIndex);
        return count(aux, array, 0, fromIndex, rangeLength, comparator);
    }

    private static <T> int count(T[] sourceArray,
                                 T[] targetArray,
                                 int sourceOffset,
                                 int targetOffset,
                                 int rangeLength,
                                 Comparator<? super T> comparator) {
        if (rangeLength < 2) {
            return 0;
        }

        int halfRangeLength = rangeLength >>> 1;
        int inversions = count(targetArray,
                               sourceArray,
                               targetOffset,
                               sourceOffset,
                               halfRangeLength,
                               comparator);

        inversions += count(targetArray,
                            sourceArray,
                            targetOffset + halfRangeLength,
                            sourceOffset + halfRangeLength,
                            rangeLength - halfRangeLength,
                            comparator);

        return inversions + merge(sourceArray,
                                  targetArray,
                                  sourceOffset,
                                  targetOffset,
                                  halfRangeLength,
                                  rangeLength - halfRangeLength,
                                  comparator);
    }

    public static <T> int count(T[] array, int fromIndex, int toIndex) {
        return count(array, fromIndex, toIndex, NATURAL_ORDER);
    }

    public static <T> int count(T[] array, Comparator<? super T> comparator) {
        Objects.requireNonNull(array);
        return count(array, 0, array.length);
    }

    public static <T> int count(T[] array) {
        return count(array, NATURAL_ORDER);
    }

    private static <T> int merge(T[] sourceArray,
                                 T[] targetArray,
                                 int sourceOffset,
                                 int targetOffset,
                                 int leftRunLength,
                                 int rightRunLength,
                                 Comparator<? super T> comparator) {
        int inversions       = 0;
        int leftRunIndex     = sourceOffset;
        int leftRunEndIndex  = sourceOffset + leftRunLength;
        int rightRunIndex    = sourceOffset + leftRunLength;
        int rightRunEndIndex = rightRunIndex + rightRunLength;
        int targetIndex      = targetOffset;

        while (leftRunIndex < leftRunEndIndex 
                && rightRunIndex < rightRunEndIndex) {
            if (comparator.compare(sourceArray[rightRunIndex], 
                                   sourceArray[leftRunIndex]) < 0) {
                inversions += leftRunEndIndex - leftRunIndex;
                targetArray[targetIndex++] = sourceArray[rightRunIndex++];
            } else {
                targetArray[targetIndex++] = sourceArray[leftRunIndex++];
            }
        }

        System.arraycopy(sourceArray, 
                         leftRunIndex, 
                         targetArray,
                         targetIndex, 
                         leftRunEndIndex - leftRunIndex);
        System.arraycopy(sourceArray,
                         rightRunIndex,
                         targetArray, 
                         targetIndex, 
                         rightRunEndIndex - rightRunIndex);
        return inversions;
    }

    private MergesortInversionCounter() {}
}
