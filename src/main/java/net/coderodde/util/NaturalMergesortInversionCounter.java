package net.coderodde.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import static net.coderodde.util.Utils.NATURAL_ORDER;
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
                                int toIndex) {
        return count(array, fromIndex, toIndex, NATURAL_ORDER);
    }
    
    public static <T> int count(T[] array) {
        Objects.requireNonNull(array);
        return count(array, 0, array.length);
    }
    
    public static <T> int count(T[] array, Comparator<? super T> comparator) {
        Objects.requireNonNull(array);
        return count(array, 0, array.length, comparator);
    }
    
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
        RunLengthQueue runLengthQueue = runLengthQueueBuilder.run();
        
        T[] bufferArray = Arrays.copyOfRange(array, fromIndex, toIndex);
        T[] sourceArray;
        T[] targetArray;
        int sourceOffset;
        int targetOffset;
        int mergePasses = getNumberOfMergePasses(runLengthQueue.size());
        
        if ((mergePasses & 1) == 1) {
            // Odd amount of merge passes over the entire input array range.
            // Set the buffer array as the source array so that the sorted 
            // result ends in in the input array.
            sourceArray = bufferArray;
            targetArray = array;
            sourceOffset = 0;
            targetOffset = fromIndex;
        } else {
            sourceArray = array;
            targetArray = bufferArray;
            sourceOffset = fromIndex;
            targetOffset = 0;
        }
        
        int runsLeftInCurrentMergePass = runLengthQueue.size();
        int offset = 0;
        int inversions = 0;
        
        // While there are runs to merge, iterate:
        while (runLengthQueue.size() > 1) {
            int leftRunLength  = runLengthQueue.dequeue();
            int rightRunLength = runLengthQueue.dequeue();
            
            inversions += merge(sourceArray,
                                targetArray,
                                sourceOffset + offset,
                                targetOffset + offset,
                                leftRunLength,
                                rightRunLength,
                                comparator);
            
            runLengthQueue.enqueue(leftRunLength + rightRunLength);
            runsLeftInCurrentMergePass -= 2;
            offset += leftRunLength + rightRunLength;
            
            switch (runsLeftInCurrentMergePass) {
                case 1:
                    int lastRunLength = runLengthQueue.dequeue();
                    // In the target array, this 'unmarried' run might be
                    // in the form of two unmerged runs.
                    System.arraycopy(sourceArray,
                                     sourceOffset + offset, 
                                     targetArray,
                                     targetOffset + offset,
                                     lastRunLength);
                    runLengthQueue.enqueue(lastRunLength);
                    // FALL THROUGH!
                    
                case 0:
                    runsLeftInCurrentMergePass = runLengthQueue.size();
                    offset = 0;
                    
                    T[] tmpArray = sourceArray;
                    sourceArray = targetArray;
                    targetArray = tmpArray;
                    
                    int tmpOffset = sourceOffset;
                    sourceOffset = targetOffset;
                    targetOffset = tmpOffset;
                    break;
            }
        }
        
        return inversions;
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
        
        /**
         * Builds an entire run length queue over the input array range.
         * 
         * @return a run length queue.
         */
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
        
        // Adds a recently scanned run to the run queue. 
        private void addRun() {
            if (previousRunWasDescending) {
                if (comparator.compare(inputArray[head - 1], 
                                       inputArray[head]) <= 0) {
                    runLengthQueue.extendLastRun(right - head);
                } else {
                    runLengthQueue.enqueue(right - head);
                }
            } else {
                runLengthQueue.enqueue(right - head);
            }
        }
        
        // Scans an ascending run.
        private void scanAscendingRun() {
            while (left != last && comparator.compare(inputArray[left],
                                                      inputArray[right]) <= 0) {
                ++left;
                ++right;
            }
            
            addRun();
            previousRunWasDescending = false;
        }
        
        // Scans a strictly descendign run. We require strictness in order to
        // sort stably. If we were not, the reversal of a descending run would 
        // reorder two possible adjacent array components.
        private void scanDescendingRun() {
            while (left != last && comparator.compare(inputArray[left],
                                                      inputArray[right]) > 0) {
                ++left;
                ++right;
            }
            
            reverseRun();
            addRun();
            previousRunWasDescending = true;
        }
        
        /**
         * Reverses the recently scanned (descending) run.
         */
        private void reverseRun() {
            for (int i = head, j = left; i < j; i++, j--) {
                T tmp = inputArray[i];
                inputArray[i] = inputArray[j];
                inputArray[j] = tmp;
            }
        }
        
        // Handles a possible leftover component.
        private void handleLastElement() {
            if (left == last) {
                // Once here, we have a leftover component.
                if (comparator.compare(inputArray[last - 1],
                                       inputArray[last]) <= 0) {
                    runLengthQueue.extendLastRun(1);
                } else {
                    runLengthQueue.enqueue(1);
                }
            }
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
        
        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('[');
            String separator = "";
            
            for (int i = 0; i < size; ++i) {
                stringBuilder.append(separator);
                separator = ", ";
                stringBuilder.append(storage[i]);
            }
            
            stringBuilder.append(']');
            return stringBuilder.toString();
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
         * Extends the last run length in the queue by {@code length} units.
         * 
         * @param length the length of the extension.
         */
        void extendLastRun(int length) {
            storage[(tail - 1) & mask] += length;
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
    
    /**
     * Computes the required number of merge passes needed to sort an input
     * array range containing {@code runs} runs.
     * 
     * @param runs the number of runs in the input array range.
     * @return the number of required merge passes.
     */
    private static int getNumberOfMergePasses(int runs) {
        return 32 - Integer.numberOfLeadingZeros(runs - 1);
    }
    
    private static <T> int merge(T[] sourceArray,
                                 T[] targetArray,
                                 int sourceOffset,
                                 int targetOffset,
                                 int leftRunLength,
                                 int rightRunLength,
                                 Comparator<? super T> comparator) {
        int leftRunIndex = sourceOffset;
        int rightRunIndex = leftRunIndex + leftRunLength;
        int leftRunEndIndex = rightRunIndex;
        int rightRunEndIndex = rightRunIndex + rightRunLength;
        int targetIndex = targetOffset;
        int inversions = 0;
        
        while (leftRunIndex != leftRunEndIndex 
                && rightRunIndex != rightRunEndIndex) {
            if (comparator.compare(sourceArray[rightRunIndex],
                                   sourceArray[leftRunIndex]) <0) {
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
    
    private NaturalMergesortInversionCounter() {}
}
