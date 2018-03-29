package mk.ukim.finki.kol1_vezbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Kostadin Krstev
 */
public class Scheduler extends Thread {
    public static Random random = new Random();
    static List<Process> scheduled = new ArrayList<>();
    private final static int TIME_TO_WAIT = 20000;
    private final static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        // TODO: kreirajte 100 Process thread-ovi i registrirajte gi
        for (int i = 0; i < 100; ++i) {
            Scheduler.register(new Process());
        }

        // TODO: kreirajte Scheduler i startuvajte go negovoto pozadinsko izvrsuvanje
        Scheduler scheduler = new Scheduler();
        scheduler.start();
        // TODO: Cekajte 20000ms za Scheduler-ot da zavrsi
        scheduler.join(TIME_TO_WAIT);
        // TODO: ispisete go statusot od izvrsuvanjeto
        if (scheduler.isAlive()) {
            scheduler.interrupt();
            System.out.println("Terminated scheduling");
        } else
            System.out.println("Finished scheduling");
    }

    public static void register(Process process) {
        scheduled.add(process);
    }

    public Process next() {
        synchronized (lock) {
            if (!scheduled.isEmpty())
                return scheduled.remove(0);
        }
        return null;
    }

    @Override
    public void run() {
        try {
            while (!scheduled.isEmpty()) {
                Thread.sleep(100);
                System.out.print(".");
                synchronized (lock) {
                    // TODO: zemete go naredniot proces
                    Process next = this.next();
                    if (next != null) {
                        // TODO: povikajte go negoviot execute() metod
                        next.execute();
                        // TODO: cekajte dodeka ne zavrsi negovoto pozadinsko izvrsuvanje
                        next.join();
                    }
                }
            }
            System.out.println("Done scheduling!");
        } catch (InterruptedException e) {
            /* DO NOTHING */
        }
    }
}

class Process extends Thread {
    Integer duration;

    public Process() throws InterruptedException {
        this.duration = Scheduler.random.nextInt(1000);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.duration);
        } catch (InterruptedException e) {
            /* DO NOTHING */
        }
    }

    public void execute() {
        System.out.println("Executing[" + this + "]: " + duration);
        // TODO: startuvajte go pozadinskoto izvrsuvanje
        this.start();
    }
}
