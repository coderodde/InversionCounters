package net.coderodde.util;

import java.util.Comparator;

/**
 * This class contains generic facilities.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 30, 2017)
 */
public final class Utils {

    public static final Comparator NATURAL_ORDER = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);    
        }
    };

    public static void checkIndices(int arrayLength,
                                    int fromIndex, 
                                    int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException(
                    "fromIndex(" + fromIndex + ") < 0");
        }

        if (toIndex > arrayLength) {
            throw new IndexOutOfBoundsException(
                    "toIndex(" + toIndex + ") > " + 
                    "arrayLength(" + arrayLength + ")");
        }

        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException(
                    "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
    }
}
