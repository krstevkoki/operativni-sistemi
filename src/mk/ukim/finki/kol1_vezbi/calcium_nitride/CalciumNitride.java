package mk.ukim.finki.kol1_vezbi.calcium_nitride;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Kostadin Krstev
 */
public class CalciumNitride {
    static Semaphore calciumSemaphore;
    static Semaphore nitrogenSemaphore;
    static Semaphore bondingReady;
    static Semaphore calciumHereSemaphore;
    static Semaphore bondingDoneSemaphore;
    static Semaphore validationDoneSemaphore;

    static Lock lock;

    static int nitrogenCounter;

    public static void init() {
        calciumSemaphore = new Semaphore(3);
        nitrogenSemaphore = new Semaphore(2);
        bondingReady = new Semaphore(0);
        calciumHereSemaphore = new Semaphore(0);
        bondingDoneSemaphore = new Semaphore(0);
        validationDoneSemaphore = new Semaphore(0);

        lock = new ReentrantLock();

        nitrogenCounter = 0;
    }

    public static class Calcium extends TemplateThread {

        public Calcium(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            calciumSemaphore.acquire();
            calciumHereSemaphore.release();

            bondingReady.acquire();
            state.bond();
            bondingDoneSemaphore.release();
            validationDoneSemaphore.acquire();
        }
    }

    public static class Nitrogen extends TemplateThread {

        public Nitrogen(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            nitrogenSemaphore.acquire();

            lock.lock();
            nitrogenCounter++;
            if (nitrogenCounter == 2) {  // monitor
                nitrogenCounter = 0;
                lock.unlock();

                calciumHereSemaphore.acquire(3);
                bondingReady.release(4);

                state.bond();

                bondingDoneSemaphore.acquire(4);
                state.validate();
                validationDoneSemaphore.release(4);

                nitrogenSemaphore.release(2);
                calciumSemaphore.release(3);
            } else {
                lock.unlock();

                bondingReady.acquire();
                state.bond();
                bondingDoneSemaphore.release();
                validationDoneSemaphore.acquire();
            }


        }
    }

    static CalciumNitrideState state = new CalciumNitrideState();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    public static void run() {
        try {
            Scanner s = new Scanner(System.in);
            int numRuns = 1;
            int numIterations = 100;
            s.close();

            HashSet<Thread> threads = new HashSet<>();

            for (int i = 0; i < numIterations; i++) {
                Nitrogen n = new Nitrogen(numRuns);
                threads.add(n);
                Calcium ca = new Calcium(numRuns);
                threads.add(ca);
                ca = new Calcium(numRuns);
                threads.add(ca);
                n = new Nitrogen(numRuns);
                threads.add(n);
                ca = new Calcium(numRuns);
                threads.add(ca);
            }

            init();

            ProblemExecution.start(threads, state);
            //System.out.println(new Date().getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class CalciumNitrideState extends AbstractState {
    private static final String BONDING_NOT_PARALLEL = "The bonding is not done in parallel!";
    private static final String MOLECULE_NOT_BOUNDED_COMPLITELY = "The previous molecule has not bonded completely.";
    private static final String MAXIMUM_3_CALCIUM = "Maximum 3 Calcium atoms for bonding are allowed.";
    private static final String MAXIMUM_2_NITROGEN = "Maximum 2 Nitrogen atoms for bonding are allowed.";
    private static final int MAXIMUM_2_NITROGEN_POINTS = 5;
    private static final int MAXIMUM_3_CALCIUM_POINTS = 5;
    private static final int MOLECULE_NOT_BOUNDED_COMPLITELY_POINTS = 10;
    private static final int BONDING_NOT_PARALLEL_POINTS = 5;

    int numAtoms = 0;
    private BoundCounterWithRaceConditionCheck Ca;
    private BoundCounterWithRaceConditionCheck N;

    public CalciumNitrideState() {
        Ca = new BoundCounterWithRaceConditionCheck(0, 3,
                MAXIMUM_3_CALCIUM_POINTS, MAXIMUM_3_CALCIUM, null, 0, null);
        N = new BoundCounterWithRaceConditionCheck(0, 3,
                MAXIMUM_2_NITROGEN_POINTS, MAXIMUM_2_NITROGEN, null, 0, null);
    }

    public void bond() {
        synchronized (this) {
            numAtoms++;
        }
        Switcher.forceSwitch(3);
        if (getThread() instanceof CalciumNitride.Calcium) {
            log(Ca.incrementWithMax(false), "Calcium bonding");
        } else if (getThread() instanceof CalciumNitride.Nitrogen) {
            log(N.incrementWithMax(false), "Nitrogen bonding");
        }
    }

    public void validate() {
        synchronized (this) {
            if (numAtoms == 5) {
                if (Ca.getValue() == 3 && N.getValue() == 2) {
                    reset();
                    log(null, "Ca3N3 molecule is formed.");
                } else {
                    log(new PointsException(
                            MOLECULE_NOT_BOUNDED_COMPLITELY_POINTS,
                            MOLECULE_NOT_BOUNDED_COMPLITELY), null);

                }
            }
        }
    }

    private synchronized void reset() {
        Ca.setValue(0);
        N.setValue(0);
        numAtoms = 0;
    }

    @Override
    public synchronized void finalize() {
        if (Ca.getMax() == 1 && N.getMax() == 1) {
            logException(new PointsException(BONDING_NOT_PARALLEL_POINTS,
                    BONDING_NOT_PARALLEL));
        }
    }
}

abstract class AbstractState {
    /**
     * Method called after threads ended their execution to validate the
     * correctness of the scenario
     */
    public abstract void finalize();

    /**
     * List of logged actions
     */
    private List<String> actions = new ArrayList<String>();

    /**
     * @return if the current thread is instance of TemplateThread it is
     * returned, and otherwise null is returned
     */
    protected TemplateThread getThread() {
        Thread current = Thread.currentThread();
        if (current instanceof TemplateThread) {
            TemplateThread t = (TemplateThread) current;
            return t;
        } else {
            return null;
        }
    }

    /**
     * Log this exception or action
     *
     * @param e      occurred exception (null if no exception)
     * @param action Description of the occurring action
     */
    public synchronized void log(PointsException e, String action) {
        TemplateThread t = (TemplateThread) Thread.currentThread();
        if (e != null) {
            t.setException(e);
            actions.add(t.toString() + "\t(e): " + e.getMessage());
        } else if (action != null) {
            actions.add(t.toString() + "\t(a): " + action);
        }
    }

    /**
     * Logging exceptions
     *
     * @param e
     */
    protected synchronized void logException(PointsException e) {
        Thread t = Thread.currentThread();
        if (e != null) {
            if (t instanceof TemplateThread) {
                ((TemplateThread) t).setException(e);
            }
            TemplateThread.hasException = true;
            actions.add("\t(e): " + e.getMessage());
        }
    }

    /**
     * Printing of the actions and exceptions that has occurred
     */
    public synchronized void printLog() {
        System.out
                .println("Poradi konkurentnosta za pristap za pecatenje, mozno e nekoja od porakite da ne e na soodvetnoto mesto.");
        System.out.println("Log na izvrsuvanje na akciite:");
        System.out.println("=========================");
        System.out.println("tip\tid\titer\takcija/error");
        System.out.println("=========================");
        for (String l : actions) {
            System.out.println(l);
        }
    }

    /**
     * Prints the status of the execution, with the exceptions that has occur
     */
    public void printStatus() {
        finalize();
        if (!TemplateThread.hasException) {
            int poeni = 25;
            if (PointsException.getTotalPoints() == 0) {
                System.out
                        .println("Procesot e uspesno sinhroniziran. Osvoeni 25 poeni.");
            } else {
                poeni -= PointsException.getTotalPoints();
                PointsException.printErrors();
                System.out.println("Maksimalni osvoeni poeni: " + poeni);
            }

        } else {
            System.out
                    .println("Procesot ne e sinhroniziran spored uslovite na zadacata");
            printLog();
            System.out
                    .println("====================================================");
            PointsException.printErrors();
            int total = (25 - PointsException.getTotalPoints());
            if (total < 0) {
                total = 0;
            }
            System.out.println("Maksimum Poeni: " + total);
        }

    }
}

class BoundCounterWithRaceConditionCheck {
    private static final int RACE_CONDITION_POINTS = 25;
    private static final String RACE_CONDITION_MESSAGE = "Race condition occured";
    private int value;
    private Integer maxAllowed;
    private Integer minAllowed;
    private int maxErrorPoints;
    private int minErrorPoints;
    private String maxErrorMessage;
    private String minErrorMessage;

    private int max;

    /**
     * @param value
     */
    public BoundCounterWithRaceConditionCheck(int value) {
        super();
        this.value = value;
        this.max = value;
    }

    /**
     * @param value           initial value
     * @param maxAllowed      upper bound of the value
     * @param maxErrorPoints  how many points are lost with the max value constraint
     *                        violation
     * @param maxErrorMessage message shown when the upper bound constrain is violated
     * @param minAllowed      lower bound of the value
     * @param minErrorPoints  how many points are lost with the min value constraint
     *                        violation
     * @param minErrorMessage message shown when the lower bound constrain is violated
     */
    public BoundCounterWithRaceConditionCheck(int value, Integer maxAllowed, int maxErrorPoints,
                                              String maxErrorMessage, Integer minAllowed, int minErrorPoints,
                                              String minErrorMessage) {
        super();
        this.value = value;
        this.max = value;
        this.maxAllowed = maxAllowed;
        this.minAllowed = minAllowed;
        this.maxErrorPoints = maxErrorPoints;
        this.minErrorPoints = minErrorPoints;
        this.maxErrorMessage = maxErrorMessage;
        this.minErrorMessage = minErrorMessage;
    }

    /**
     * @return the maximum value of the integer variable that occurred at some
     * point of the execution
     */
    public int getMax() {
        return max;
    }

    /**
     * @return the current value
     */
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Testing for race condition. NOTE: there are no guarantees that the race
     * condition will be detected
     *
     * @return
     */
    public PointsException checkRaceCondition() {
        return checkRaceCondition(3, RACE_CONDITION_MESSAGE);
    }

    /**
     * Testing for race condition. NOTE: there are no guarantees that the race
     * condition will be detected, but higher the time argument is, the
     * probability for race condition occurrence is higher
     *
     * @return
     */
    public PointsException checkRaceCondition(int time, String message) {
        int val;

        synchronized (this) {
            val = value;
        }
        Switcher.forceSwitch(time);
        if (val != value) {
            PointsException e = new PointsException(RACE_CONDITION_POINTS,
                    message);
            return e;
        }
        return null;

    }

    public PointsException incrementWithMax() {
        return incrementWithMax(true);
    }

    public PointsException incrementWithMax(boolean checkRaceCondition) {
        if (checkRaceCondition) {
            PointsException raceCondition = checkRaceCondition();
            if (raceCondition != null) {
                return raceCondition;
            }
        }
        synchronized (this) {
            value++;

            if (value > max) {
                max = value;
            }
            if (maxAllowed != null) {
                if (value > maxAllowed) {
                    PointsException e = new PointsException(maxErrorPoints,
                            maxErrorMessage);
                    return e;
                }
            }
        }

        return null;
    }

    public PointsException decrementWithMin() {
        return decrementWithMin(true);
    }

    public PointsException decrementWithMin(boolean checkRaceCondition) {
        if (checkRaceCondition) {
            PointsException raceCondition = checkRaceCondition();
            if (raceCondition != null) {
                return raceCondition;
            }
        }

        synchronized (this) {
            value--;
            if (minAllowed != null) {
                if (value < minAllowed) {
                    PointsException e = new PointsException(minErrorPoints,
                            minErrorMessage);
                    return e;
                }
            }
        }
        return null;
    }
}

class PointsException extends RuntimeException {
    private static HashMap<String, PointsException> exceptions = new HashMap<String, PointsException>();
    private int points;

    public PointsException(int points, String message) {
        super(message);
        this.points = points;
        exceptions.put(message, this);
    }

    public static int getTotalPoints() {
        int sum = 0;
        for (PointsException e : exceptions.values()) {
            sum += e.getPoints();
        }
        return sum;
    }

    public static void printErrors() {
        System.out.println("Gi imate slednite greski: ");
        for (Map.Entry<String, PointsException> e : exceptions.entrySet()) {
            System.out.println(String.format("[%s] : (-%d)", e.getKey(), e.getValue().getPoints()));
        }
    }

    public int getPoints() {
        return points;
    }
}

abstract class ProblemExecution {

    public static void start(HashSet<Thread> threads, AbstractState state)
            throws Exception {


        // start the threads
        for (Thread t : threads) {
            t.start();
        }

        // wait threads to finish
        for (Thread t : threads) {
            t.join(1000);
        }

        // check for deadlock
        for (Thread t : threads) {
            if (t.isAlive()) {
                t.interrupt();
                if (t instanceof TemplateThread) {
                    TemplateThread tt = (TemplateThread) t;
                    tt.setException(new PointsException(25, "DEADLOCK"));
                }
            }
        }

        // print the status
        state.printStatus();
    }

}

class Switcher {
    private static final Random RANDOM = new Random();

    /*
     * This method pauses the current thread i.e. changes its state to be
     * Blocked. This should force thread switch if there are threads waiting
     */
    public static void forceSwitch(int range) {
        try {
            Thread.sleep(RANDOM.nextInt(range));
        } catch (InterruptedException e) {
        }
    }
}

abstract class TemplateThread extends Thread {
    static boolean hasException = false;
    int numRuns = 1;
    public int iteration = 0;
    protected Exception exception = null;

    public TemplateThread(int numRuns) {
        this.numRuns = numRuns;
    }

    public abstract void execute() throws InterruptedException;

    @Override
    public void run() {
        try {
            for (int i = 0; i < numRuns && !hasException; i++) {
                execute();
                iteration++;
            }
        } catch (InterruptedException e) {
            // Do nothing
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
            hasException = true;
        }
    }

    public void setException(Exception exception) {
        this.exception = exception;
        hasException = true;
    }

    @Override
    public String toString() {
        Thread current = Thread.currentThread();
        if (numRuns > 1) {
            return String.format("%s\t%d\t%d", "" + current.getClass().getSimpleName().charAt(0), getId(), iteration);
        } else {
            return String.format("%s\t%d\t", "" + current.getClass().getSimpleName().charAt(0), getId());
        }
    }
}
