package mk.ukim.finki.kol1_vezbi.concert;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * @author Kostadin Krstev
 */
public class Concert {
    private static Semaphore performerSemaphore;

    private static Semaphore baritonesSemaphore;  // x3
    private static Semaphore tenorsSemaphore;  // x3
    private static Semaphore baritoneHereSemaphore;
    private static Semaphore tenorHereSemaphore;

    private static Semaphore groupFormedSemaphore;
    private static Semaphore canPlaySemaphore;
    private static Semaphore performDoneSemaphore;
    private static Semaphore voteCompletedSemaphore;

    public static void init() {
        performerSemaphore = new Semaphore(1);

        baritonesSemaphore = new Semaphore(3);
        tenorsSemaphore = new Semaphore(3);
        baritoneHereSemaphore = new Semaphore(0);
        tenorHereSemaphore = new Semaphore(0);

        groupFormedSemaphore = new Semaphore(0);
        canPlaySemaphore = new Semaphore(0);
        performDoneSemaphore = new Semaphore(0);
        voteCompletedSemaphore = new Semaphore(0);
    }

    public static class Performer extends TemplateThread {
        public Performer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            performerSemaphore.acquire();

            groupFormedSemaphore.acquire(6);  // 3 baritones & 3 tenors

            canPlaySemaphore.release(6);  // 3 baritones & 3 tenors
            state.perform();

            performDoneSemaphore.acquire(6);  // 3 baritones & 3 tenors
            state.vote();
            voteCompletedSemaphore.release(6);  // 3 baritones & 3 tenors

            baritonesSemaphore.release(3);  // 3 baritones
            tenorsSemaphore.release(3); // 3 tenors

            performerSemaphore.release();
        }
    }

    public static class Baritone extends TemplateThread {
        public Baritone(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            baritonesSemaphore.acquire();

            baritoneHereSemaphore.release();  // bariton is here
            tenorHereSemaphore.acquire();  // waiting for tenor
            state.formBackingVocals();  // a group can be formed

            groupFormedSemaphore.release();  // x3 releasing

            canPlaySemaphore.acquire();  // x3 acquiring
            state.perform();
            performDoneSemaphore.release();  // x3 releasing

            voteCompletedSemaphore.acquire();  // x3 acquiring
        }
    }

    public static class Tenor extends TemplateThread {
        public Tenor(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            tenorsSemaphore.acquire();

            tenorHereSemaphore.release();  // tenor is here
            baritoneHereSemaphore.acquire();  // baritone is waiting
            state.formBackingVocals();  // a group can be formed

            groupFormedSemaphore.release(); // x3 releasing

            canPlaySemaphore.acquire();  // x3 acquiring
            state.perform();
            performDoneSemaphore.release();  // x3 releasing

            voteCompletedSemaphore.acquire();  // x3 acquiring
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    public static void run() {
        try {
            int numRuns = 1;
            int numScenarios = 300;

            HashSet<Thread> threads = new HashSet<>();

            for (int i = 0; i < numScenarios; i++) {
                Tenor t = new Tenor(numRuns);
                Baritone b = new Baritone(numRuns);
                threads.add(t);
                if (i % 3 == 0) {
                    Performer p = new Performer(numRuns);
                    threads.add(p);
                }
                threads.add(b);
            }

            init();

            ProblemExecution.start(threads, state);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static ConcertState state = new ConcertState();
}

class ConcertState extends AbstractState {
    private static final String VOTE_SHOULD_CALLED_ONCE = "The vote() method should be called only once per performance.";
    private static final String GROUP_FORMING_NOT_PARALLEL = "The group forming is not in parallel!";
    private static final String INCOMPLETE_PERFORMANCE = "The previous performance is incomplete.";
    private static final String GROUPS_ARE_NOT_PRESENT = "Not all backing groups are present.";
    private static final String MAXIMUM_3_BARITONES = "Maximum 3 Baritones for performance are allowed.";
    private static final String MAXIMUM_3_TENORS = "Maximum 3 Tenors for performance are allowed.";
    private static final String MAXIMUM_1_PERFORMER = "Maximum 1 Performer for performance is allowed.";
    private static final int MAXIMUM_1_PERFORMER_POINTS = 5;
    private static final int MAXIMUM_3_TENORS_POINTS = 5;
    private static final int MAXIMUM_3_BARITONES_POINTS = 5;
    private static final int GROUPS_ARE_NOT_PRESENT_POINTS = 5;
    private static final int INCOMPLETE_PERFORMANCE_POINTS = 5;
    private static final int GROUP_FORMING_NOT_PARALLEL_POINTS = 5;
    private static final int VOTE_SHOULD_CALLED_ONCE_POINTS = 5;

    int numParticipants = 0;
    private BoundCounterWithRaceConditionCheck baritone;
    private BoundCounterWithRaceConditionCheck tenor;
    private BoundCounterWithRaceConditionCheck performer;

    public ConcertState() {
        baritone = new BoundCounterWithRaceConditionCheck(0, 3,
                MAXIMUM_3_BARITONES_POINTS, MAXIMUM_3_BARITONES, null, 0, null);
        tenor = new BoundCounterWithRaceConditionCheck(0, 3,
                MAXIMUM_3_TENORS_POINTS, MAXIMUM_3_TENORS, null, 0, null);
        performer = new BoundCounterWithRaceConditionCheck(0, 1,
                MAXIMUM_1_PERFORMER_POINTS, MAXIMUM_1_PERFORMER, null, 0, null);
    }

    public void formBackingVocals() {

        Switcher.forceSwitch(3);
        if (getThread() instanceof Concert.Baritone) {
            log(baritone.incrementWithMax(false), "Baritone for backing group");
        } else if (getThread() instanceof Concert.Tenor) {
            log(tenor.incrementWithMax(false), "Tenor for backing group");
        }
    }

    public void perform() {
        synchronized (this) {
            // first check
            if (numParticipants == 0) {
                if (baritone.getValue() == 3 && tenor.getValue() == 3) {
                    baritone.setValue(0);
                    tenor.setValue(0);
                } else {
                    log(new PointsException(GROUPS_ARE_NOT_PRESENT_POINTS, GROUPS_ARE_NOT_PRESENT), null);
                }
            }
            numParticipants++;
        }
        Switcher.forceSwitch(3);
        if (getThread() instanceof Concert.Baritone) {
            log(baritone.incrementWithMax(false), "Baritone performed");
        } else if (getThread() instanceof Concert.Tenor) {
            log(tenor.incrementWithMax(false), "Tenor performed");
        } else {
            log(performer.incrementWithMax(false), "Performer performed");
        }
    }

    public void vote() {
        synchronized (this) {
            if (numParticipants == 7) {
                reset();
                log(null, "Voting started.");
            } else if (numParticipants != 0) {
                log(new PointsException(INCOMPLETE_PERFORMANCE_POINTS, INCOMPLETE_PERFORMANCE), null);
                reset();
            } else {
                log(new PointsException(VOTE_SHOULD_CALLED_ONCE_POINTS, VOTE_SHOULD_CALLED_ONCE), null);
            }
        }
    }

    private synchronized void reset() {
        baritone.setValue(0);
        tenor.setValue(0);
        performer.setValue(0);
        numParticipants = 0;
    }

    @Override
    public synchronized void finalize() {
        if (baritone.getMax() == 1 && tenor.getMax() == 1) {
            logException(new PointsException(GROUP_FORMING_NOT_PARALLEL_POINTS, GROUP_FORMING_NOT_PARALLEL));
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
    private List<String> actions = new ArrayList<>();

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
            throw e;
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
            throw e;
        }
    }

    /**
     * Printing of the actions and exceptions that has occurred
     */
    public synchronized void printLog() {
        System.out.println("Poradi konkurentnosta za pristap za pecatenje, " +
                "mozno e nekoja od porakite da ne e na soodvetnoto mesto.");
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
        try {
            finalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TemplateThread.hasException) {
            int poeni = 25;
            if (PointsException.getTotalPoints() == 0) {
                System.out.println("Procesot e uspesno sinhroniziran. Osvoeni 25 poeni.");
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

    public static int raceConditionDefaultTime = 3;

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
                                              String maxErrorMessage, Integer minAllowed,
                                              int minErrorPoints, String minErrorMessage) {
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
    public synchronized int getValue() {
        return value;
    }

    public synchronized void setValue(int value) {
        this.value = value;
    }

    /**
     * Throws exception when the val is different than the value of the counter.
     *
     * @param val
     * @param points
     * @param errorMessage
     * @return
     */
    public synchronized PointsException assertEquals(int val, int points, String errorMessage) {
        if (this.value != val) {
            PointsException e = new PointsException(points, errorMessage);
            return e;
        } else {
            return null;
        }
    }

    public synchronized PointsException assertNotEquals(int val, int points, String errorMessage) {
        if (this.value == val) {
            PointsException e = new PointsException(points, errorMessage);
            return e;
        } else {
            return null;
        }
    }

    /**
     * Testing for race condition. NOTE: there are no guarantees that the race
     * condition will be detected
     *
     * @return
     */
    public PointsException checkRaceCondition() {
        return checkRaceCondition(raceConditionDefaultTime,
                RACE_CONDITION_MESSAGE);
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

class PointsException extends RuntimeException {
    private static HashMap<String, PointsException> exceptions = new HashMap<>();
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
        if (!exceptions.isEmpty()) {
            System.out.println("Gi imate slednite greski: ");
            for (Map.Entry<String, PointsException> e : exceptions.entrySet()) {
                System.out.println(String.format("[%s] : (-%d)", e.getKey(), e
                        .getValue().getPoints()));
            }
        }
    }

    public int getPoints() {
        return points;
    }
}

abstract class ProblemExecution {
    public static void start(HashSet<Thread> threads, AbstractState state) throws Exception {
        startWithoutDeadlock(threads, state);

        checkDeadlock(threads, state);
    }

    public static void startWithoutDeadlock(HashSet<Thread> threads, AbstractState state) throws Exception {

        // start the threads
        for (Thread t : threads) {
            t.start();
        }

        // wait threads to finish
        for (Thread t : threads) {
            t.join(1000);
        }
    }

    private static void checkDeadlock(HashSet<Thread> threads, AbstractState state) {
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

abstract class TemplateThread extends Thread {
    static boolean hasException = false;
    public int iteration = 0;
    protected Exception exception = null;
    int numRuns = 1;

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
            return String.format("[%d]%s\t%d\t%d", new Date().getTime(), ""
                            + current.getClass().getSimpleName().charAt(0), getId(),
                    iteration);
        } else {
            return String.format("[%d]%s\t%d\t", new Date().getTime(), ""
                    + current.getClass().getSimpleName().charAt(0), getId());
        }
    }
}
