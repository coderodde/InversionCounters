package net.coderodde.util;

/**
 * This unit test tests the correctness of the mergesort-based inversion 
 * counter.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 30, 2017)
 */
public class NaturalMergesortInversionCounterTest 
        extends AbstractInversionCounterTest {

    public NaturalMergesortInversionCounterTest() {
        super(NaturalMergesortInversionCounter::count);
    }
}
