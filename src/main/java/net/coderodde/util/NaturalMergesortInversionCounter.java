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
        
        RunLengthQueue runLengthQueue = 
                buildRunLengthQueue(array, 
                                    fromIndex, 
                                    toIndex, 
                                    comparator);

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

    static <T> RunLengthQueue 
    buildRunLengthQueue(T[] inputArray, 
                        int fromIndex,
                        int toIndex,
                        Comparator<? super T> comparator) {
        int last = toIndex - 1;
        int left = fromIndex;
        int right = left + 1;
        RunLengthQueue runLengthQueue =
                new RunLengthQueue(toIndex - fromIndex);
        while (left < last) {
            int head = left;

            while (left < last 
                    && comparator.compare(inputArray[left],
                                          inputArray[right]) <= 0) {
                ++left;
                ++right;
            }

            ++left;
            ++right;

            runLengthQueue.enqueue(left - head);
        }

        if (left == last) {
            runLengthQueue.enqueue(1);
        }

        return runLengthQueue;
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
            capacity = ceilPowerOfTwo(Math.max(capacity, MINIMUM_CAPACITY));
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
