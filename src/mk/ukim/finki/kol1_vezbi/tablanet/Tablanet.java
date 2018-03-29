package mk.ukim.finki.kol1_vezbi.tablanet;

import java.util.*;

/**
 * @author Kostadin Krstev
 */
public class Tablanet {


    public static void init() {
    }

    public static class Dealer extends TemplateThread {
        public Dealer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            state.dealCards();
            state.nextGroup();
        }
    }

    public static class Player extends TemplateThread {
        public Player(int numRuns) {
            super(numRuns);
        }


        @Override
        public void execute() throws InterruptedException {
            state.playerSeat();
            state.play();
            state.endCycle();
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    public static void run() {
        try {
            int numCycles = 10;
            int numIterations = 20;

            HashSet<Thread> threads = new HashSet<>();

            Dealer d = new Dealer(50);
            threads.add(d);
            for (int i = 0; i < numIterations; i++) {
                Player c = new Player(numCycles);
                threads.add(c);
            }

            init();

            ProblemExecution.start(threads, state);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static TablanetState state = new TablanetState();

}

class TablanetState extends AbstractState {
    private static final int ALL_GROUP_HASENT_FINISH_POINTS = 7;
    private static final String ALL_GROUP_HASENT_FINISH = "Ne se zavrseni site grupi so igranje";

    private static final int THREADS_IN_PROCESS_POINTS = 7;
    private static final String THREADS_IN_PROCESS = "Site igraci ne zavrsile so igranje";

    private static final int PROCESS_NOT_PARALLEL_POINTS = 7;
    private static final String PROCESS_NOT_PARALLEL = "Procesot na igranje ne e paralelen";

    private static final int MAXIMUM_GROUP_THREADS_POINTS = 7;
    private static final String MAXIMUM_GROUP_THREADS = "Nema mesto na masata vo tekovnata grupa";

    private static final int NOT_ENOUGH_GROUP_THREADS_POINTS = 7;
    private static final String NOT_ENOUGH_GROUP_THREADS = "Nema dovolno igraci za da se sostavi grupa";

    public static final int DUPLICATE_THREAD_IN_CYCLE_POINTS = 7;
    public static final String DUPLICATE_THREAD_IN_CYCLE = "Igracot sednuva po vtor pat vo ist ciklus";

    public static final int THREADS_HASNT_FINISHED_THE_CYCLE_POINTS = 7;
    public static final String THREADS_HASNT_FINISHED_THE_CYCLE = "Ima igraci koi ne igrale vo ovoj ciklus";

    public static final int NO_CALL_TO_PRODUCER_POINTS = 7;
    public static final String NO_CALL_TO_PRODUCER = "Ne se podeleni kartite.";

    public static final String THREAD_READY = "Igrac sednuva";
    public static final String THREAD_IN_PROCESS = "Igracot zapocnuva so igra";
    public static final String GROUP_THREADS_FINISHED_PROCESS = "Tekovnata grupa zavrsi so igranje.";

    public static final String FINISHED_CYCLE = "Site grupi zavrsija so igranje vo tekovniot ciklus.";

    public static final int GROUP_SIZE = 4;
    public static final int TOTAL_THREADS = 20;

    private BoundCounterWithRaceConditionCheck threadsPrepared;
    private BoundCounterWithRaceConditionCheck threadsInProcess;
    private BoundCounterWithRaceConditionCheck threadsFinishedProcess;
    private BoundCounterWithRaceConditionCheck threadsFinishedRound;

    private HashSet preparedThreads = new HashSet();
    private boolean dealedCards = false;

    public TablanetState() {
        threadsPrepared = new BoundCounterWithRaceConditionCheck(
                0,
                GROUP_SIZE,
                MAXIMUM_GROUP_THREADS_POINTS,
                MAXIMUM_GROUP_THREADS,
                null,// NO MINIMUM CHECK
                0,
                null
        );

        threadsInProcess = new BoundCounterWithRaceConditionCheck(0);
        threadsFinishedProcess = new BoundCounterWithRaceConditionCheck(0);
        threadsFinishedRound = new BoundCounterWithRaceConditionCheck(0);
    }

    public void playerSeat() {
        synchronized (TablanetState.class) {
            Thread current = getThread();
            if (preparedThreads.contains(current.getId())) {
                throw new PointsException(DUPLICATE_THREAD_IN_CYCLE_POINTS, DUPLICATE_THREAD_IN_CYCLE);
            }
            preparedThreads.add(current.getId());
        }
        PointsException e = threadsPrepared.incrementWithMax(false);
        logException(e);
        log(e, THREAD_READY);
        Switcher.forceSwitch(5);
    }


    public void dealCards() {
        logException(threadsPrepared.assertEquals(GROUP_SIZE, NOT_ENOUGH_GROUP_THREADS_POINTS,
                NOT_ENOUGH_GROUP_THREADS));
        Switcher.forceSwitch(3);
        synchronized (TablanetState.class) {
            dealedCards = true;
        }

    }

    public void play() {
        synchronized (TablanetState.class) {
            if (dealedCards == false) {
                logException(new PointsException(NO_CALL_TO_PRODUCER_POINTS, NO_CALL_TO_PRODUCER));
            }
        }
        log(threadsInProcess.incrementWithMax(false), THREAD_IN_PROCESS);
        Switcher.forceSwitch(10);
        log(threadsInProcess.decrementWithMin(false), null);
        threadsFinishedProcess.incrementWithMax(false);
        threadsFinishedRound.incrementWithMax(false);
    }


    public void nextGroup() {
        logException(threadsFinishedProcess.assertEquals(GROUP_SIZE, ALL_GROUP_HASENT_FINISH_POINTS,
                ALL_GROUP_HASENT_FINISH));
        logException(threadsInProcess.assertEquals(0, THREADS_IN_PROCESS_POINTS, THREADS_IN_PROCESS));
        log(null, GROUP_THREADS_FINISHED_PROCESS);
        synchronized (TablanetState.class) {
            // reset round
            threadsPrepared.setValue(0);
            threadsFinishedProcess.setValue(0);
            dealedCards = false;
        }
        Switcher.forceSwitch(3);
    }

    public void endCycle() {
        logException(threadsFinishedRound.assertEquals(TOTAL_THREADS, THREADS_HASNT_FINISHED_THE_CYCLE_POINTS,
                THREADS_HASNT_FINISHED_THE_CYCLE));
        log(null, FINISHED_CYCLE);
        synchronized (TablanetState.class) {
            threadsFinishedRound.setValue(0);
            preparedThreads.clear();
        }
    }

    @Override
    public void finalize() {
        if (threadsInProcess.getMax() == 1) {
            logException(new PointsException(PROCESS_NOT_PARALLEL_POINTS, PROCESS_NOT_PARALLEL));
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
