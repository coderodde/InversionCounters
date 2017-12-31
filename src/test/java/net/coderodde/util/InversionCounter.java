package net.coderodde.util;

import java.util.Comparator;

/**
 * Defines the most specific API for inversion counting algorithms.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 30, 2017)
 * @param <T> the array component type.
 */
@FunctionalInterface
public interface InversionCounter<T> {

    public int count(T[] array, 
                     int fromIndex,
                     int toIndex, 
                     Comparator<? super T> comparator);
}
