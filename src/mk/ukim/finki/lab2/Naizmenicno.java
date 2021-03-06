package mk.ukim.finki.lab2;

import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/**
 * @author Kostadin Krstev
 */
public class Naizmenicno {
    public static int NUM_RUNS = 1000;
    private int f1count;
    private int f2count;
    private int maxDifference = 0;
    private int count;

    private static Semaphore semaphore1;
    private static Semaphore semaphore2;
    private static final Object mutex = new Object();

    public Naizmenicno() {
    }

    /**
     * Metod koj treba da gi inicijalizira vrednostite na semaforite i
     * ostanatite promenlivi za sinhronizacija.
     */
    public void init(int count) {
        // da se implementira
        this.count = count;
        this.maxDifference = count;
        semaphore1 = new Semaphore(count);
        semaphore2 = new Semaphore(0);
    }

    class F1Thread extends Thread {
        public F1Thread() {
        }

        public void executeF1() throws InterruptedException {
            synchronized (mutex) {
                if (f1count == 0) {  // prviot thread da izvrse count+1 pat f1()
                    semaphore1.acquire(count);
                    for (int i = 1; i <= count; ++i) {
                        f1();
                    }
                    f1();
                } else {
                    semaphore1.acquire();
                    f1();
                }
            }
            semaphore2.release();
        }

        @Override
        public void run() {
            try {
                executeF1();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class F2Thread extends Thread {
        public F2Thread() {
        }

        public void executeF2() throws InterruptedException {
            // da se implementira
            semaphore2.acquire();
            f2();
            semaphore1.release();
        }

        @Override
        public void run() {
            try {
                executeF2();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void f1() {
        System.out.println("f1()");
        f1count++;

        if (f1count - f2count > maxDifference)
            maxDifference = f1count - f2count;
    }

    public void f2() {
        System.out.println("f2()");
        f2count++;

        if (f1count - f2count > maxDifference)
            maxDifference = f1count - f2count;
    }

    public static void main(String[] args) {
        try {
            Naizmenicno environment = new Naizmenicno();
            environment.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start() throws Exception {
        System.out.println("Vnesete za kolku poveke sakate da se izvrsi f1()");
        Scanner s = new Scanner(System.in);
        int n = s.nextInt();
        if (n < 1) {
            System.err.println("Vnesete broj pogolem od 0");
            System.exit(-1);
        }
        init(n);

        HashSet<Thread> threads = new HashSet<>();
        for (int i = 0; i < NUM_RUNS; ++i) {
            F1Thread f1 = new F1Thread();
            F2Thread f2 = new F2Thread();
            threads.add(f1);
            threads.add(f2);
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }
        System.out.println("F1count: " + f1count);
        System.out.println("F2count: " + f2count);
        System.out.println("maxDifference: " + maxDifference);
        System.out.println("Status: " + (maxDifference <= n));
    }
}
