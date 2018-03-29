package mk.ukim.finki.kol1_vezbi;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Kostadin Krstev
 */
public class TancSoStudentite2_0 {
    // TODO: Definicija na globalni promenlivi i semafori
    private Semaphore menSemaphore;
    private Semaphore womenSemaphore;
    private Semaphore childSemaphore;
    private Semaphore danceSemaphore;
    private Semaphore readyWoman;

    private Lock mLock;
    private Lock zLock;

    private int menCounter;

    public void init() {
        // TODO: da se implementira
        menSemaphore = new Semaphore(10);
        womenSemaphore = new Semaphore(10);
        childSemaphore = new Semaphore(10);
        danceSemaphore = new Semaphore(3);
        readyWoman = new Semaphore(0);

        zLock = new ReentrantLock();
        mLock = new ReentrantLock();

        menCounter = 0;
    }

    class Masko extends Thread {
        // TODO: Definicija  na promenlivi za sostojbata

        public void ucestvo() throws InterruptedException {
            // TODO: Kod za mashkite
            menSemaphore.acquire();
            show.presobleci();
            menSemaphore.release();

            mLock.lock();
            menCounter++;
            mLock.unlock();

            readyWoman.acquire();
            danceSemaphore.acquire();
            show.tancuvaj();
            danceSemaphore.release();
        }

        @Override
        public void run() {
            try {
                ucestvo();
            } catch (InterruptedException e) {
                // Do nothing
            } catch (Exception e) {
                exception = e;
                hasException = true;
            }
        }

        @Override
        public String toString() {
            return String.format("m\t%d", getId());
        }

        public Exception exception = null;
    }

    class Zensko extends Thread {
        // TODO: Definicija  na promenlivi za sostojbata

        public void ucestvo() throws InterruptedException {
            // TODO: Kod za zenskite
            womenSemaphore.acquire();

            show.presobleci();
            zLock.lock();
            readyWoman.release();
            zLock.unlock();
            womenSemaphore.release();
        }

        @Override
        public void run() {
            try {
                ucestvo();
            } catch (InterruptedException e) {
                // Do nothing
            } catch (Exception e) {
                exception = e;
                hasException = true;
            }
        }

        @Override
        public String toString() {
            return String.format("z\t%d", getId());
        }

        public Exception exception = null;
    }

    class Dete extends Thread {
        // TODO: Definicija  na promenlivi za sostojbata

        public void ucestvo() throws InterruptedException {
            // TODO: Kod za decata
            childSemaphore.acquire();
            show.presobleci();
            childSemaphore.release();

            mLock.lock();
            if (menCounter < 10)  // ako ima slobodni maski, bidi zensko
                readyWoman.release();
            else if (menCounter == 10) { // ako nema slobodni maski, bidi masko
                readyWoman.acquire();

                danceSemaphore.acquire();
                // Go povikuvaat samo deca sto ste pretstavuvaat kako masko
                show.tancuvaj();
                danceSemaphore.release();
            }
            mLock.unlock();
        }

        @Override
        public void run() {
            try {
                ucestvo();
            } catch (InterruptedException e) {
                // Do nothing
            } catch (Exception e) {
                exception = e;
                hasException = true;
            }
        }

        @Override
        public String toString() {
            return String.format("t\t%d", getId());
        }

        public Exception exception = null;
    }

    public static void main(String[] args) {
        try {
            TancSoStudentite2_0 environment = new TancSoStudentite2_0();
            environment.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start() throws Exception {
        show = new Show();
        init();
        HashSet<Thread> threads = new HashSet<>();
        Scanner scanner = new Scanner(System.in);
        int BROJ_INSTANCI = scanner.nextInt();
        scanner.close();

        for (int i = 0; i < BROJ_INSTANCI; i++) {
            Zensko z = new Zensko();
            Masko m = new Masko();
            threads.add(z);
            threads.add(m);
            Dete t = new Dete();
            threads.add(t);
            t = new Dete();
            threads.add(t);
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            if (!hasException) {
                t.join();
            } else {
                t.interrupt();
            }
        }
        show.printStatus();
    }

    public class Show {
        public static final int BROJ_GARDEROBA = 10;
        public static final int BROJ_TEREN = 3;
        public static final int TYPE_MASKO = 1;
        public static final int TYPE_ZENSKO = 2;
        public static final int TYPE_UNKNOWN = -1;

        public int brojMaskiGarderoba = 0;
        public int brojZenskiGarderoba = 0;
        public int brojDetskaGarderoba = 0;
        public int brojTancuvanja = 0;
        public int maxMaskiGarderoba = 0;
        public int maxZenskiGarderoba = 0;
        public int maxDetskaGarderoba = 0;
        public int maxTancuvanja = 0;

        public Show() {
        }

        public void presobleci() throws RuntimeException {
            log(null, "presobleci start");
            Thread t = Thread.currentThread();
            if (t instanceof Masko) {
                synchronized (RANDOM) {
                    brojMaskiGarderoba++;
                    if (brojMaskiGarderoba > 10) {
                        exception("Ne moze da ima poveke od 10 maski vo maskata garderoba.");
                    }
                    if (brojMaskiGarderoba > maxMaskiGarderoba) {
                        maxMaskiGarderoba = brojMaskiGarderoba;
                    }
                }
                waitRandom();
                synchronized (RANDOM) {
                    brojMaskiGarderoba--;
                }
            } else if (t instanceof Zensko) {
                synchronized (RANDOM) {
                    brojZenskiGarderoba++;
                    if (brojZenskiGarderoba > 10) {
                        exception("Ne moze da ima poveke od 10 zenski vo zenskata garderoba.");
                    }
                    if (brojZenskiGarderoba > maxZenskiGarderoba) {
                        maxZenskiGarderoba = brojZenskiGarderoba;
                    }
                }
                waitRandom();
                synchronized (RANDOM) {
                    brojZenskiGarderoba--;
                }
            } else {
                synchronized (RANDOM) {
                    brojDetskaGarderoba++;
                    if (brojDetskaGarderoba > 10) {
                        exception("Ne moze da ima poveke od 10 deca vo detskata garderoba.");
                    }
                    if (brojDetskaGarderoba > maxDetskaGarderoba) {
                        maxDetskaGarderoba = brojDetskaGarderoba;
                    }
                }
                waitRandom();
                synchronized (RANDOM) {
                    brojDetskaGarderoba--;
                }
            }
            log(null, "presobleci kraj");
        }

        public void tancuvaj() throws RuntimeException {
            log(null, "tancuvaj start");
            synchronized (RANDOM) {
                brojTancuvanja++;
                if (brojTancuvanja > BROJ_TEREN) {
                    exception("Ne moze paralelno da tancuvaat poveke od 3 para.");
                }

                if (brojTancuvanja > maxTancuvanja) {
                    maxTancuvanja = brojTancuvanja;
                }
            }
            waitRandom();
            synchronized (RANDOM) {
                brojTancuvanja--;
            }
            log(null, "tancuvaj kraj");
        }

        private void waitRandom() {
            try {
                int r;
                synchronized (RANDOM) {
                    r = RANDOM.nextInt(RANDOM_RANGE);
                }
                Thread.sleep(r);
            } catch (Exception e) {
                //do nothing
            }
        }

        private void exception(String message) {
            RuntimeException e = new RuntimeException(message);
            log(e, null);
            hasError = true;
            throw e;
        }

        public int getType() {
            Thread t = Thread.currentThread();
            if (t instanceof Masko) {
                return TYPE_MASKO;
            } else if (t instanceof Zensko) {
                return TYPE_ZENSKO;
            } else {
                return TYPE_UNKNOWN;
            }
        }

        private synchronized void log(RuntimeException e, String action) {
            Thread t = Thread.currentThread();
            if (e == null) {
                actions.add(t.toString() + "\t(a): " + action);
            } else {
                actions.add(t.toString() + "\t(e): " + e.getMessage());
            }
        }

        public synchronized void printLog() {
            System.out.println("Poradi konkurentnosta za pristap za pecatenje, mozno e nekoja od porakite da ne e na soodvetnoto mesto.");
            System.out.println("Log na izvrsuvanje na akciite:");
            System.out.println("=========================");
            System.out.println("(tip m<=>Masko, tip z<=>Zensko)");
            System.out.println("tip\tid\takcija/error");
            System.out.println("=========================");
            for (String l : actions) {
                System.out.println(l);
            }
        }

        public void printStatus() {
            if (!hasError) {
                int poeni = 25;
                System.out.println("Procesot e uspesno sinhroniziran");
                if (show.maxMaskiGarderoba == 1 || show.maxZenskiGarderoba == 1 || show.maxDetskaGarderoba == 1) {
                    System.out.println("\t-no ima maksimum eden ucesnik vo garderobata.");
                    poeni -= 5;
                }
                if (show.maxTancuvanja == 1) {
                    System.out.println("\t-no ima maksimum edna proverka vo eden moment.");
                    poeni -= 5;
                }

                System.out.println("Osvoeni poeni: " + poeni);

            } else {
                System.out.println("Procesot ne e sinhroniziran spored uslovite na zadacata");
                show.printLog();
                System.out.println("Maksimum mozni poeni: 15");
            }

        }

        private List<String> actions = new ArrayList<>();
        private boolean hasError = false;
    }

    private static final Random RANDOM = new Random();
    private static final int RANDOM_RANGE = 3;
    // Instanca od bafferot
    private Show show;
    private boolean hasException = false;
}
