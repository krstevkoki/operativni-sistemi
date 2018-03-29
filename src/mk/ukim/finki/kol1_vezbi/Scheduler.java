package mk.ukim.finki.kol1_vezbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Kostadin Krstev
 */
public class Scheduler {
    public static Random random = new Random();
    static List<Process> scheduled = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // TODO: kreirajte 100 Process thread-ovi i registrirajte gi

        // TODO: kreirajte Scheduler i startuvajte go negovoto pozadinsko izvrsuvanje

        // TODO: Cekajte 20000ms za Scheduler-ot da zavrsi

        // TODO: ispisete go statusot od izvrsuvanjeto
    }

    public static void register(Process process) {
        scheduled.add(process);
    }

    public Process next() {
        if (!scheduled.isEmpty())
            return scheduled.remove(0);
        return null;
    }

    public void run() {
        try {
            while (!scheduled.isEmpty()) {
                Thread.sleep(100);
                System.out.print(".");

                // TODO: zemete go naredniot proces

                // TODO: povikajte go negoviot execute() metod

                // TODO: cekajte dodeka ne zavrsi negovoto pozadinsko izvrsuvanje

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done scheduling!");
    }
}

class Process {
    Integer duration;

    public Process() throws InterruptedException {
        this.duration = Scheduler.random.nextInt(1000);
    }


    public void execute() {
        System.out.println("Executing[" + this + "]: " + duration);
        // TODO: startuvajte go pozadinskoto izvrsuvanje
    }
}
