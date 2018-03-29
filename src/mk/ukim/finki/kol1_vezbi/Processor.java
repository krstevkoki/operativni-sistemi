package mk.ukim.finki.kol1_vezbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * @author Kostadin Krstev
 */
public class Processor extends Thread {
    public static Random random = new Random();
    static List<EventGenerator> scheduled = new ArrayList<>();
    private final static int TIME_TO_WAIT = 20000;
    static Semaphore generatingSemaphore = new Semaphore(5);
    static Semaphore signalizeProcessorSemaphore = new Semaphore(0);

    public static void main(String[] args) throws InterruptedException {
        // TODO: kreirajte Processor i startuvajte go negovoto pozadinsko izvrsuvanje
        Processor processor = new Processor();
        processor.start();

        for (int i = 0; i < 100; i++) {
            EventGenerator eventGenerator = new EventGenerator();
            register(eventGenerator);
            // TODO: startuvajte go eventGenerator-ot
            eventGenerator.start();
        }

        // TODO: Cekajte 20000ms za Processor-ot da zavrsi
        processor.join(TIME_TO_WAIT);
        // TODO: ispisete go statusot od izvrsuvanjeto
        if (processor.isAlive()) {
            processor.interrupt();
            System.out.println("Terminated scheduling");
        } else
            System.out.println("Finished scheduling");
    }

    public static void register(EventGenerator generator) {
        scheduled.add(generator);
    }

    /**
     * Ne smee da bide izvrsuvano paralelno so generate() metodot
     */
    public static void process() {
        System.out.println("processing event");
    }

    public void run() {
        while (!scheduled.isEmpty()) {
            // TODO: cekanje  na nov generiran event
            try {
                signalizeProcessorSemaphore.acquire();
                // TODO: povikajte go negoviot process() metod
                generatingSemaphore.acquire(5);

                process();
                synchronized (this) {
                    scheduled.remove(0);
                }

                generatingSemaphore.release(5);
            } catch (InterruptedException e) {
                /* DO NOTHING */
            }
        }
        System.out.println("Done scheduling!");
    }
}

class EventGenerator extends Thread {
    Integer duration;

    public EventGenerator() throws InterruptedException {
        this.duration = Processor.random.nextInt(1000);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.duration);
            Processor.generatingSemaphore.acquire();

            generate();
            Processor.signalizeProcessorSemaphore.release();

            Processor.generatingSemaphore.release();
        } catch (InterruptedException e) {
            /* DO NOTHING */
        }
    }

    /**
     * Ne smee da bide povikan paralelno kaj poveke od 5 generatori
     */
    public static void generate() {
        System.out.println("Generating event: ");
    }
}
