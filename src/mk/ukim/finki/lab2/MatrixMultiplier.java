package mk.ukim.finki.lab2;

import java.util.HashSet;
import java.util.Random;

/**
 * @author Kostadin Krstev
 */
public class MatrixMultiplier {
    private int[][] matrix;
    private int[][] newMatrix;

    public MatrixMultiplier(int[][] matrix) {
        this.matrix = matrix;
        newMatrix = new int[this.matrix.length][this.matrix[0].length];
    }

    private class WorkerThread implements Runnable {
        private int[] row;
        private int[] col;
        private int indexRow;
        private int indexCol;

        public WorkerThread(int[] row, int[] col, int indexRow, int indexCol) {
            this.row = row;
            this.col = col;
            this.indexRow = indexRow;
            this.indexCol = indexCol;
        }

        // Find the maximum value in our particular piece of the array
        @Override
        public void run() {
            int element = 0;
            for (int i = 0; i < row.length; ++i)
                element += row[i] * col[i];
            newMatrix[indexRow][indexCol] = element;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MatrixMultiplier matrixMultiplier = new MatrixMultiplier(getBigHairyMatrix());
        // matrixMultiplier.printMatrix(matrixMultiplier.matrix);  // use with caution
        matrixMultiplier.start();
    }

    public void start() throws InterruptedException {
        HashSet<Thread> threads = new HashSet<>();
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                int[] row = matrix[i];
                int[] col = getCol(j);
                threads.add(new Thread(new WorkerThread(row, col, i, j)));
            }
        }

        for (Thread thread : threads)
            thread.start();

        for (Thread thread : threads)
            thread.join();

        // printMatrix(newMatrix);  // use with caution
    }

    private void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j)
                System.out.print(matrix[i][j] + "\t");
            System.out.println();
        }
    }

    private int[] getCol(int index) {
        int[] col = new int[matrix[index].length];
        for (int i = 0; i < matrix.length; ++i)
            col[i] = matrix[i][index];
        return col;
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
