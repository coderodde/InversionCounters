package net.coderodde.util;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This abstract test class implements all the actual unit tests.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 30, 2017)
 */
public abstract class AbstractInversionCounterTest {
    
    private static final int TEST_ITERATIONS = 100;
    private static final int MAXIMUM_ARRAY_LENGTH = 1000;
    
    protected final InversionCounter<Integer> inversionCounter;
    private final Random random = new Random();
    
    public AbstractInversionCounterTest(
            InversionCounter<Integer> inversionCounter) {
        this.inversionCounter = inversionCounter;
    }
    
    @Test
    public void test() {
        for (int iteration = 0; iteration < TEST_ITERATIONS; iteration++) {
            int length = random.nextInt(MAXIMUM_ARRAY_LENGTH + 1);
            int fromIndex = random.nextInt(Math.max(1, length / 10));
            int toIndex = 
                    length - random.nextInt(
                            Math.max(1, (length - fromIndex) / 10));
            
            Integer[] array1 = getRandomIntegerArray(length,
                                                     -length / 2 - 10,
                                                     +length / 2 + 10,
                                                     random);
            Integer[] array2 = array1.clone();
            
            assertEquals(BruteForceInversionCounter.count(array1, 
                                                          fromIndex, 
                                                          toIndex, 
                                                          Integer::compareTo),
                         inversionCounter.count(array2, 
                                                fromIndex, 
                                                toIndex, 
                                                Integer::compareTo));
            Arrays.sort(array1, fromIndex, toIndex);
            assertTrue(Arrays.equals(array1, array2));
        }
    }
    
    /**
     * Creates a random integer array.
     * 
     * @param length   the desired length of the array.
     * @param minValue the minimum integer value.
     * @param maxValue the maximum integer value.
     * @param random   the random number generator.
     * @return a randomly generated integer array.
     */
    private Integer[] getRandomIntegerArray(int length,
                                            int minValue,
                                            int maxValue,
                                            Random random) {
        Integer[] array = new Integer[length];
        
        for (int i = 0; i < length; ++i) {
            array[i] = randomValue(minValue, maxValue, random);
        }
        
        return array;
    }
    
    /**
     * Returns a random integer value from the range {@code minValue,
     * minValue + 1, ..., maxValue - 1, maxValue}, according to the uniform 
     * distribution.
     * 
     * @param minValue the minimum integer value.
     * @param maxValue the maximum integer value.
     * @param random   the random number generator.
     * @return a random integer value within the range.
     */
    private Integer randomValue(int minValue, int maxValue, Random random) {
        return minValue + random.nextInt(maxValue - minValue + 1);
    }
}
