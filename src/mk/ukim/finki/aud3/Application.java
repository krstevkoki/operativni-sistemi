package mk.ukim.finki.aud3;

import java.util.HashSet;

/**
 * @author Kostadin Krstev
 */
public class Application {
    public static void main(String[] args) {
        IntegerWrapper iw = new IntegerWrapper();

        /* ===== ONLY ONE THREAD =====
        ExampleThread thread = new ExampleThread(iw, 0);
        thread.start();

        for (int i = 0; i < 100; ++i) {
            // System.out.println("HERE");
            // thread.privateFieldIncrement();  // not allowed, private method
            // thread.publicFieldIncrement();
            // thread.publicFieldSafeIncrement();
            // thread.wrapperIncrement();
            thread.wrapperSafeIncrement();
            // thread.badLocking();
            /* Circular DEADLOCK *//*
            // thread.methodA();
            // thread.methodB();
            /* Scheduler Dependant DEADLOCK *//*
            // thread.schedulerDependantDeadlock_A();
            // thread.schedulerDependantDeadlock_B();
        }*/

        /* ===== MULTIPLE THREADS =====*/
        HashSet<ExampleThread> threads = new HashSet<>();
        for (int i = 1; i <= 10; ++i)
            threads.add(new ExampleThread(iw, 0));

        for (ExampleThread thread : threads)
            thread.start();

        for (ExampleThread thread : threads) {
            // System.out.println("HERE");
            // thread.privateFieldIncrement();  // not allowed, private method
            // thread.publicFieldIncrement();
            // thread.publicFieldSafeIncrement();
            // thread.wrapperIncrement();
            thread.wrapperSafeIncrement();
            // thread.badLocking();
            //* Circular DEADLOCK *//*
            // thread.methodA();
            // thread.methodB();
            //* Scheduler Dependant DEADLOCK *//*
            // thread.schedulerDependantDeadlock_A();
            // thread.schedulerDependantDeadlock_B();
        }
    }
}
