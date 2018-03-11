package mk.ukim.finki.lab2;

import java.util.Random;

/**
 * @author Kostadin Krstev
 */
public class TenThreads {
    private static class WorkerThread implements Runnable {
        private static int max = Integer.MIN_VALUE;
        private int[] ourArray;

        public WorkerThread(int[] ourArray) {
            this.ourArray = ourArray;
        }

        // Find the maximum value in our particular piece of the array
        @Override
        public void run() {
            for (int i = 0; i < ourArray.length; ++i)
                max = Math.max(max, ourArray[i]);
        }

        public static int getMax() {
            return max;
        }
    }

    public static void main(String[] args) {
        WorkerThread[] workerThreads = new WorkerThread[20];
        Thread[] threads = new Thread[20];
        int[][] bigMatrix = getBigHairyMatrix();
//        int max = Integer.MIN_VALUE;

        // Give each thread a slice of the matrix to work with
        for (int i = 0; i < 20; ++i) {
//            workerThreads[i] = new WorkerThread(bigMatrix[i]);
//            threads[i] = new Thread(workerThreads[i]);
            threads[i] = new Thread(new WorkerThread(bigMatrix[i]));
            threads[i].start();
        }

        // Wait for each thread to finish
        try {
            for (int i = 0; i < 20; ++i) {
                threads[i].join(); // why is this needed
//                max = Math.max(max, /*workerThreads[i].getMax()*/);  // no need if max property is static
            }
        } catch (InterruptedException e) {
            // fall through
        }

        System.out.println("Maximum value was " + /*max*/ WorkerThread.getMax());
    }

    private static int[][] getBigHairyMatrix() {
        int x = 100;
        int y = 100;

        int[][] matrix = new int[x][y];
        Random rnd = new Random();

        for (int i = 0; i < x; ++i)
            for (int j = 0; j < y; ++j)
                matrix[i][j] = rnd.nextInt();

        return matrix;
    }
}
