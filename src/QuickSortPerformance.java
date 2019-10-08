import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class QuickSortPerformance {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    static long MAXVALUE = 2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 50;                   // adjust numberOfTrials and MAXINPUTSIZE based on available
    static int MAXINPUTSIZE = (int) Math.pow(2, 14);  // time, processor speed, and available memory
    static int MININPUTSIZE = 1;

    static String ResultsFolderPath = "/home/steve/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("QuickSort-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("QuickSort-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("QuickSort-Exp3.txt");

        // verify that the algorithm works
        System.out.println("");
        System.out.println("----Verification Test----");
        checkSortCorrectness();
        long[] verificationList = createRandomIntegerList(1000);
        long[] verificationList2 = createRandomIntegerList(10000);
        System.out.println("-------------------------");
        System.out.println("Array of 1000 sorted?: " + verifySorted(verificationList));
        System.out.println("Array of 10000 sorted?: " + verifySorted(verificationList2));
        System.out.println("Sorting...");
        quickSort(verificationList, 0, verificationList.length - 1);
        quickSort(verificationList2, 0, verificationList2.length - 1);
        System.out.println("Array of 1000 sorted?: " + verifySorted(verificationList));
        System.out.println("Array of 10000 sorted?: " + verifySorted(verificationList2));
    }

    static void runFullExperiment(String resultsFileName) {
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize *= 2) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of random integers in random order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            //System.out.print("    Generating test data...");
            //long[] testList = createRandomIntegerList(inputSize);
            //System.out.println("...done.");
            //System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();

            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopWatch methods themselves
            //BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random list of integers each trial
                long[] testList = createRandomIntegerList(inputSize);

                // generate a random key to search in the range of a the min/max numbers in the list
                // long testSearchKey = (long) (0 + Math.random() * (testList[testList.length - 1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                quickSort(testList, 0, testList.length - 1);
                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n", inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    // Quick Sort algorithm modified from www.geeksforgeeks.com
    // This is a "randomized" quick sort algorithm. The NaiveQuickSort
    // algorithm was re-used to implement this. The modification is to
    // randomly select an index between the lowIndex and highIndex (inclusive)
    // and then simply swap that value with the lowIndex value. This
    // effectively "ramndomizes" the pivot point.
    public static long partition(long arr[], int lowIndex, int highIndex){
        // pick a random index between lowIndex and highIndex
        Random rand = new Random();
        int randomSelection = lowIndex + rand.nextInt(highIndex - lowIndex);
        // swap randomSelection with the lowIndex value
        long temp1=arr[randomSelection];
        arr[randomSelection]=arr[lowIndex];
        arr[lowIndex]=temp1;

        long pivot = arr[lowIndex];  // choose first element as pivot
        int i = (highIndex+1); // index at larger element
        for (int j=highIndex; j>lowIndex; j--){
            // If current element is smaller than the pivot swap arr[i] and arr[j]
            if (arr[j] > pivot){
                i--;
                long temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        // swap arr[i+1] and arr[high] (or pivot)
        long temp = arr[i-1];
        arr[i-1] = arr[lowIndex];
        arr[lowIndex] = temp;
        return i-1;
    }

    public static void quickSort(long arr[], int lowIndex, int highIndex){
        if (lowIndex < highIndex){
            long partitionIndex = partition(arr, lowIndex, highIndex);
            // Recursively sort elements before partition and after partition
            quickSort(arr, lowIndex, (int)partitionIndex - 1);
            quickSort(arr, (int)partitionIndex + 1, highIndex);
        }
    }

    // generate a random list of values to test against
    public static long[] createRandomIntegerList(int size) {
        long[] newList = new long[size];
        // randomly picks a value between -1000000000 and 1000000000
        for (int i = 0; i < size; i++) {
            newList[i] = (long) ((2000000000 * Math.random()) - 1000000000);
        }
        return newList;  // return the list to caller
    }

    // verifies that an array is sorted in ascending (low to high) order
    private static boolean verifySorted(long[] list) {
        for (int i = 0; i < list.length - 1; i++){
            if (list[i] > list[i+1])
                return false;
        }
        return true;
    }

    // verifies that an array is sorted in ascending (low to high) order
    private static void checkSortCorrectness(){
        long[] testArray = createRandomIntegerList(10);
        System.out.print("Unsorted Small Random Test Array: ");
        System.out.println(Arrays.toString(testArray));
        System.out.println("The array is sorted: " + verifySorted(testArray));
        System.out.println("Sorting...");
        quickSort(testArray, 0, testArray.length - 1);
        System.out.print("Small Random Test Array After Sort: ");
        System.out.println(Arrays.toString(testArray));
        System.out.println("The array is sorted: " + verifySorted(testArray));
    }
}