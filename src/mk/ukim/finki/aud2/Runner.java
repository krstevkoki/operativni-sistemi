package mk.ukim.finki.aud2;

import mk.ukim.finki.aud2.threads.MyRunnableThread;
import mk.ukim.finki.aud2.threads.MyThread;

/**
 * @author Kostadin Krstev
 */
public class Runner {
    public static void main(String[] args) throws InterruptedException {
        SharedResource resource = new SharedResource();
        MyThread thread1 = new MyThread(resource);
        // Thread thread2 = new Thread(() -> System.out.println("HELLO WORLD from RUNNABLE THREAD"));
        Thread thread2 = new Thread(new MyRunnableThread(resource));
        thread1.start();
        // thread1.join();
        thread2.start();
    }
}
