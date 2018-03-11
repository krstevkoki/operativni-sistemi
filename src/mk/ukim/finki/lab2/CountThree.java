package mk.ukim.finki.lab2;

import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/**
 * @author Kostadin Krstev
 */
public class CountThree {
    public static int NUM_RUNS = 100;
    /**
     * Promenlivata koja treba da go sodrzi brojot na pojavuvanja na elementot 3
     */
    private int count = 0;
    private Semaphore semaphore;

    /**
     * TODO: definirajte gi potrebnite elementi za sinhronizacija
     */
    public void init() {
        semaphore = new Semaphore(1);
    }

    class Counter extends Thread {
        private int[] data;

        public Counter(int[] data) {
            this.data = data;
        }

        public void count(int[] data) throws InterruptedException {
            // da se implementira
            int counter = 0;
            for (int num : data)
                if (num == 3)
                    ++counter;
            semaphore.acquire();
            count += counter;
            semaphore.release();
        }

        @Override
        public void run() {
            try {
                count(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            CountThree environment = new CountThree();
            environment.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start() throws Exception {
        init();
        HashSet<Thread> threads = new HashSet<>();
        Scanner s = new Scanner(System.in);
        int total = s.nextInt();
        if (total < 10) {
            System.err.println("Vnesete vrednost >= 10");
            return;
        }
        Random random = new Random();

        for (int i = 0; i < NUM_RUNS; ++i) {
            int[] data = new int[total];
            for (int j = 0; j < total; ++j) {
//                data[j] = s.nextInt();
                data[j] = random.nextInt(10);
            }
            Counter c = new Counter(data);
            threads.add(c);
        }

        for (Thread t : threads)
            t.start();

        for (Thread t : threads)
            t.join();

        System.out.println(count);
        s.close();
    }
}
