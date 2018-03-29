package mk.ukim.finki.kol1_vezbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Kostadin Krstev
 */
public class Processor {
    public static Random random = new Random();
    static List<EventGenerator> scheduled = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // TODO: kreirajte Processor i startuvajte go negovoto pozadinsko izvrsuvanje


        for (int i = 0; i < 100; i++) {
            EventGenerator eventGenerator = new EventGenerator();
            register(eventGenerator);
            //TODO: startuvajte go eventGenerator-ot

        }

        // TODO: Cekajte 20000ms za Processor-ot da zavrsi

        // TODO: ispisete go statusot od izvrsuvanjeto
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

            // TODO: povikajte go negoviot process() metod
            process();
        }

        System.out.println("Done scheduling!");
    }
}

class EventGenerator {
    Integer duration;

    public EventGenerator() throws InterruptedException {
        this.duration = Processor.random.nextInt(1000);
    }

    /**
     * Ne smee da bide povikan paralelno kaj poveke od 5 generatori
     */
    public static void generate() {
        System.out.println("Generating event: ");
    }
}
