package net.coderodde.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import static net.coderodde.util.Utils.checkIndices;

/**
 * This class implements a modification of the natural mergesort that counts 
 * inversion in the input array range.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 30, 2017)
 */
public final class NaturalMergesortInversionCounter {
    
    public static <T> int count(T[] array, 
                                int fromIndex, 
                                int toIndex, 
                                Comparator<? super T> comparator) {
        Objects.requireNonNull(array);
        Objects.requireNonNull(comparator);
        checkIndices(array.length, fromIndex, toIndex);
        int rangeLength = toIndex - fromIndex;
        
        if (rangeLength < 2) {
            return 0;
        }
        
        RunLengthQueueBuilder<T> runLengthQueueBuilder =
                new RunLengthQueueBuilder<>(array,
                                            fromIndex,
                                            toIndex,
                                            comparator);
        RunLengthQueue runLengthQueue = new
        
        T[] aux = Arrays.copyOfRange(array, fromIndex, toIndex);
        T[] sourceArray;
        T[] targetArray;
        int sourceOffset;
        int targetOffset;
        int mergePasses = getNumberOfMergePasses()
    }
    
    /**
     * This static inner class implements an algorithm for scanning the input
     * array range and constructing a run length queue.
     * 
     * @param <T> the array component type.
     */
    private static final class RunLengthQueueBuilder<T> {
        
        /**
         * The array holding the range to sort.
         */
        private final T[] inputArray;
        
        /**
         * The starting inclusive index into the array range to scan for runs.
         */
        private final int fromIndex;
        
        /**
         * The ending exclusive index into the array range to scan for runs.
         */
        private final int toIndex;
        
        /**
         * The array component comparator.
         */
        private final Comparator<? super T> comparator;
        
        /**
         * The index to the first array component belonging to the run currently
         * being scanned.
         */
        private int head;
        
        /**
         * The smaller index into the array component pair currently scanned.
         */
        private int left;
        
        /**
         * The larger index into the array component pair currently scanned.
         */
        private int right;
        
        /**
         * The index (inclusive) of the very last array component in the input 
         * array range.
         */
        private final int last;
        
        /**
         * Indicates whether the previous run was descending. We need this since
         * after reversing a  descending run, it may turn out that this run may
         * be simply extended by the following run.
         */
        private boolean previousRunWasDescending = false;
        
        /**
         * The run length queue being built.
         */
        private final RunLengthQueue runLengthQueue;
        
        RunLengthQueueBuilder(T[] inputArray,
                              int fromIndex,
                              int toIndex,
                              Comparator<? super T> comparator) {
            this.inputArray = inputArray;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.comparator = comparator;
            this.left = fromIndex;
            this.right = fromIndex + 1;
            this.last = toIndex - 1;
            
            int rangeLength = toIndex - fromIndex;
            this.runLengthQueue = new RunLengthQueue((rangeLength >>> 1) + 1);
        }
        
        RunLengthQueue run() {
            while (left < last) {
                head = left;
                
                if (comparator.compare(inputArray[left++],
                                       inputArray[right++]) <= 0) {
                    // The next run is ascending:
                    scanAscendingRun();
                } else {
                    // The next run is descending:
                    scanDescendingRun();
                }
                
                ++left;
                ++right;
            }
            
            handleLastElement();
            return runLengthQueue;
        }
        
        private void scanAscendingRun() {
            while (left != last && comparator.compare(inputArray[left],
                                                     inputArray[right]) <= 0) {
                ++left;
                ++right;
            }
            
            addRun();
            previousRunWasDescending = false;
        }
        
        private void scanDescendingRun() {
            while (left != last &&)
        }
    }
    
    /**
     * This static inner class implements a simple queue of integers used to 
     * represent the run sequence in the array to sort.
     */
    private static final class RunLengthQueue {
        
        /**
         * The minimum capacity of the storage array.
         */
        private static final int MINIMUM_CAPACITY = 256;
        
        /**
         * Stores the run lengths.
         */
        private final int[] storage;
        
        /**
         * The index of the array component that will be dequeued next.
         */
        private int head;
        
        /**
         * The index of the array component to which the next run length will
         * be set.
         */
        private int tail;
        
        /**
         * The current number of run lengths stored in this queue.
         */
        private int size;
        
        /**
         * A bit mask used for simpler modulo calculation (at least at the level
         * of hardware).
         */
        private final int mask;
        
        /**
         * Creates a run length queue large enough to hold maximum of 
         * {@code capacity} elements.
         * 
         * @param capacity the requested capacity, may be increased in the 
         *                 constructor.
         */
        RunLengthQueue(int capacity) {
            capacity = ceilPowerOfTwo(capacity);
            this.mask = capacity - 1;
            this.storage = new int[capacity];
        }
        
        /**
         * Enqueues a given run length to the tail of this queue.
         * 
         * @param runLength the run length to enqueue.
         */
        void enqueue(int runLength) {
            storage[tail] = runLength;
            tail = (tail + 1) & mask;
            size++;
        }
        
        /**
         * Dequeues the run length from the head of this queue.
         * 
         * @return the run length stored in the head of this queue.
         */
        int dequeue() {
            int ret = storage[head];
            head = (head + 1) & mask;
            size--;
            return ret;
        }
        
        /**
         * Returns the number of run lengths stored in this queue.
         * 
         * @return the number of run lengths.
         */
        int size() {
            return size;
        }
        
        /**
         * Returns a smallest power of two no less than {@code number}.
         * 
         * @param number the number to ceil.
         * @return a smallest power of two no less than {@code number}.
         */
        private static int ceilPowerOfTwo(int number) {
            int ret = Integer.highestOneBit(number);
            return ret != number ? (ret << 1) : ret;
        }
    }
    
    private NaturalMergesortInversionCounter() {}
}
