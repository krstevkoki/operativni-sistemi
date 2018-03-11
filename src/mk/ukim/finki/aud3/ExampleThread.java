package mk.ukim.finki.aud3;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Kostadin Krstev
 */
public class ExampleThread extends Thread {
    private IntegerWrapper wrapper;
    private int localThreadField;
    public int publishedThreadField;

    private static Lock lock = new ReentrantLock();
    private static Semaphore binarySemaphore = new Semaphore(1);

    public ExampleThread() {
        this.wrapper = null;
        this.localThreadField = 0;
        this.publishedThreadField = 0;
    }

    public ExampleThread(IntegerWrapper wrapper, int localThreadField) {
        this.wrapper = wrapper;
        this.localThreadField = localThreadField;
        this.publishedThreadField = 0;
    }

    public void threadSleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
            /* DO NOTHING */
        }
    }

    // no need for synchronization, visible only to this thread
    private void privateFieldIncrement() {
        this.localThreadField++;
        int localVar = localThreadField;
        threadSleep(50);
        if (localVar != localThreadField)
            System.err.println(String.format("VALUES MISMATCH || %d - %d", localThreadField, localVar));
        else
            System.out.println(String.format("OK || %d - %d", localThreadField, localVar));
    }

    public void publicFieldIncrement() {
        this.publishedThreadField++;
        int localVar = publishedThreadField;
        threadSleep(50);
        if (localVar != publishedThreadField)
            System.err.println(String.format("VALUES MISMATCH || %d - %d", publishedThreadField, localVar));
        else
            System.out.println(String.format("OK || %d - %d", publishedThreadField, localVar));
    }

    public /*synchronized*/ void publicFieldSafeIncrement() {
        /* ==== THIS ==== */
//        synchronized (this) {
//            publicFieldIncrement();
//        }

        /* ==== OR THIS ==== */
        lock.lock();
        publicFieldIncrement();
        lock.unlock();

        /* ==== OR THIS ==== */
//        try {
//            binarySemaphore.acquire();
//            publicFieldIncrement();
//        } catch (InterruptedException e) {
//            /* DO NOTHING */
//        } finally {
//            binarySemaphore.release();
//        }

    }

    public void wrapperIncrement() {
        wrapper.increment();
        int localVar = wrapper.getCounter();
        threadSleep(50);
        if (localVar != wrapper.getCounter())
            System.err.println(String.format("VALUES MISMATCH || %s - %d", wrapper, localVar));
        else
            System.out.println(String.format("OK || %s - %d", wrapper, localVar));
    }

    public /*synchronized*/ void wrapperSafeIncrement() {
        /* ==== THIS ==== */
//        synchronized (this) {
//            wrapperIncrement();
//        }

        /* ==== OR THIS ==== */
//        lock.lock();
//        wrapperIncrement();
//        lock.unlock();

        try {
            binarySemaphore.acquire();
            wrapperIncrement();
        } catch (InterruptedException e) {
            /* DO NOTHING */
        } finally {
            binarySemaphore.release();
        }

    }

    public void badLocking() {
        /* ====== Not working correctly ====== */
//        if (wrapper.getCounter() <= 5) {
//            try {
//                // threadSleep(100);
//                binarySemaphore.acquire();
//                wrapper.increment();
//                System.out.println(String.format("[%d]: %d", this.getId(), wrapper.getCounter()));
//                binarySemaphore.release();
//            } catch (InterruptedException e) {
//                /* DO NOTHING */
//            }
//        }

        /* ====== DEADLOCK DO NOT PROCEED !!! ====== */
//        try {
//            binarySemaphore.acquire();
//            if (wrapper.getCounter() <= 5) {
//                wrapper.increment();
//                System.out.println(String.format("[%d]: %d", this.getId(), wrapper.getCounter()));
//                binarySemaphore.release();
//            }
//        } catch (InterruptedException e) {
//            /* DO NOTHING */
//        }

        /* ====== CORRECT ====== */
//        try {
//            binarySemaphore.acquire();
//            if (wrapper.getCounter() <= 5) {
//                wrapper.increment();
//                System.out.println(String.format("[%d]: %d", this.getId(), wrapper.getCounter()));
//                binarySemaphore.release();
//            } else
//                binarySemaphore.release();
//        } catch (InterruptedException e) {
//            /* DO NOTHING */
//        }

        /* ==== OR ==== */
//        try {
//            binarySemaphore.acquire();
//            if (wrapper.getCounter() <= 5) {
//                wrapper.increment();
//                System.out.println(String.format("[%d]: %d", this.getId(), wrapper.getCounter()));
//            }
//            binarySemaphore.release();
//        } catch (InterruptedException e) {
//            /* DO NOTHING */
//        }

        /* ===== ANOTHER DEADLOCK DO NOT PROCEED !!! ===== */
//        Semaphore xy = new Semaphore(0);
//        try {
//            binarySemaphore.acquire();
//            if (wrapper.getCounter() <= 5) {
//                wrapper.increment();
//                System.out.println(String.format("[%d]: %d", this.getId(), wrapper.getCounter()));
//                xy.acquire();
//            } else
//                xy.release();
//            binarySemaphore.release();
//        } catch (InterruptedException e) {
//            /* DO NOTHING */
//        }

        /* ====== CORRECT ====== */
        Semaphore xy = new Semaphore(1);
        try {
            binarySemaphore.acquire();
            if (wrapper.getCounter() <= 5) {
                wrapper.increment();
                System.out.println(String.format("[%d]: %d", this.getId(), wrapper.getCounter()));
                // RELEASE CRITICAL REGION BEFORE BLOCKING
                binarySemaphore.release();
                xy.acquire();
            } else {
                xy.release();
                binarySemaphore.release();
            }
        } catch (InterruptedException e) {
            /* DO NOTHING */
        }
    }

    /* ==== Circular Deadlock Scenario ==== */
    public Semaphore resA = new Semaphore(0);
    public Semaphore resB = new Semaphore(0);

    public void methodA() {
        try {
            resA.acquire();  // wait for resource A
            resB.release();  // signal that B is free
        } catch (InterruptedException e) {
            /*DO NOTHING */
        }
    }

    public void methodB() {
        try {
            resB.acquire();  // wait for resource B
            resA.release();  // signal that A is free
        } catch (InterruptedException e) {
            /*DO NOTHING */
        }
    }

    /* ==== Scheduler Dependant Deadlock Scenario ==== */
    private final Object monitor = new Object();

    public void schedulerDependantDeadlock_A() {
        synchronized (monitor) {
            wrapper.increment();  // read or modify shared object
        }
        // block outside of critical region
        try {
            resA.acquire();
        } catch (InterruptedException e) {
            /* DO NOTHING */
        }
    }

    public void schedulerDependantDeadlock_B() {
        synchronized (monitor) {
            resA.release();
            System.out.println(wrapper);  // shared object access
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; ++i) {
            // privateFieldIncrement();
            // publicFieldIncrement();
            // publicFieldSafeIncrement();
            // wrapperIncrement();
            wrapperSafeIncrement();
            // badLocking();
            /* Circular DEADLOCK */
            // methodA();
            // methodB();
            /* Scheduler Dependant DEADLOCK */
            // schedulerDependantDeadlock_A();
            // schedulerDependantDeadlock_B();
        }
        System.out.println(String.format("[%d]: END", this.getId()));
    }
}
