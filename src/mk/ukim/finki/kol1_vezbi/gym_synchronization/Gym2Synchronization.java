package mk.ukim.finki.kol1_vezbi.gym_synchronization;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Kostadin Krstev
 */
public class Gym2Synchronization {
    static Semaphore twelvePlayersSemaphore;
    static Semaphore groupFormSemaphore;
    static Semaphore cabinLastPlayerSemaphore;
    static Semaphore cabinSemaphore;
    static Semaphore sportingDoneSemaphore;

    static Lock lock;
    static Lock lock1;

    static int numPlayers;
    static int cabinOutCounter;

    public static void init() {
        twelvePlayersSemaphore = new Semaphore(12);
        groupFormSemaphore = new Semaphore(0);
        cabinSemaphore = new Semaphore(4);
        sportingDoneSemaphore = new Semaphore(0);
        cabinLastPlayerSemaphore = new Semaphore(0);

        lock = new ReentrantLock();
        lock1 = new ReentrantLock();

        numPlayers = 0;
        cabinOutCounter = 0;
    }


    public static class Player extends TemplateThread {
        public Player(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            twelvePlayersSemaphore.acquire();
            state.vlezi();
            lock.lock();
            numPlayers++;
            if (numPlayers == 12) {  // jas sum posledniot
                numPlayers = 0;
                lock.unlock();

                groupFormSemaphore.release(11);
                state.sportuvaj();
                sportingDoneSemaphore.release(11);

                cabinLastPlayerSemaphore.acquire(11);
                state.presobleci();
                state.slobodnaSala();  // samo posledniot go povikuva
                twelvePlayersSemaphore.release(12);

                /*cabinSemaphore.acquire();
                state.presobleci();
                cabinSemaphore.release();

                lock1.lock();
                cabinOutCounter++;
                if (cabinOutCounter == 12) {
                    cabinOutCounter = 0;
                    lock1.unlock();
                    state.slobodnaSala();
                    twelvePlayersSemaphore.release(12);
                } else {
                    lock1.unlock();
                }*/

            } else {
                lock.unlock();

                groupFormSemaphore.acquire();  //  x11 waiting
                state.sportuvaj();
                sportingDoneSemaphore.acquire();

                cabinSemaphore.acquire();
                state.presobleci();
                cabinSemaphore.release();
                cabinLastPlayerSemaphore.release();  // x11 releases

                /*lock1.lock();
                cabinOutCounter++;
                if (cabinOutCounter == 12) {
                    cabinOutCounter = 0;
                    lock1.unlock();
                    state.slobodnaSala();
                    twelvePlayersSemaphore.release(12);
                } else {
                    lock1.unlock();
                }*/
            }
        }
    }

    static Gym2State state = new Gym2State();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    public static void run() {
        try {
            Scanner s = new Scanner(System.in);
            int numRuns = 1;
            int numIterations = 1200;
            s.close();

            HashSet<Thread> threads = new HashSet<>();

            for (int i = 0; i < numIterations; i++) {
                Player h = new Player(numRuns);
                threads.add(h);
            }

            init();

            ProblemExecution.start(threads, state);
            //System.out.println(new Date().getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class Gym2State extends AbstractState {
    private static final int MAXIMUM_4_PLAYERS_POINTS = 8;
    private static final int MAXIMUM_12_PLAYERS_POINTS = 8;
    private static final int START_PLAY_POINTS = 10;
    private static final int FINISHED_PLAY_POINTS = 8;
    private static final int DRESSING_NOT_PARALLEL_POINTS = 8;
    private static final int PLAYING_NOT_PARALLEL_POINTS = 8;

    private static final String MAXIMUM_4_PLAYERS = "Poveke od 4 igraci se presoblekuvaat istovremeno!!!";
    private static final String MAXIMUM_12_PLAYERS = "Poveke od 12 igraci igraat istovremeno!!!";
    private static final String START_PLAY_MESSAGE = "Ne se prisutni 12 igraci za da zapocne igranjeto!!!";
    private static final String FINISHED_PLAY_MESSAGE = "Ne moze da se zatvori saalta. Seuste ima igraci vo nea!!!";
    private static final String DRESSING_NOT_PARALLEL = "Presoblekuvanjeto ne e paralelizirano!!!";
    private static final String PLAYING_NOT_PARALLEL = "Ne moze da se igra sam po sam!!!";

    private BoundCounterWithRaceConditionCheck dressingRoom;
    private BoundCounterWithRaceConditionCheck play;
    private BoundCounterWithRaceConditionCheck enteredPlayers;
    private BoundCounterWithRaceConditionCheck finishedPlayers;

    public Gym2State() {
        dressingRoom = new BoundCounterWithRaceConditionCheck(0, 4,
                MAXIMUM_4_PLAYERS_POINTS, MAXIMUM_4_PLAYERS, null, 0, null);
        play = new BoundCounterWithRaceConditionCheck(0, 12,
                MAXIMUM_12_PLAYERS_POINTS, MAXIMUM_12_PLAYERS, null, 0, null);

        enteredPlayers = new BoundCounterWithRaceConditionCheck(0);
        finishedPlayers = new BoundCounterWithRaceConditionCheck(0);

    }

    public void vlezi() {
        enteredPlayers.incrementWithMax(false);
    }

    /**
     * Treba da se presobleceni 12 igraci za da zapocne igrata
     */
    public void sportuvaj() {
        log(enteredPlayers.assertEquals(12, START_PLAY_POINTS,
                START_PLAY_MESSAGE), "zapocnuvam na sportuvam");
        log(play.incrementWithMax(false), null);
        Switcher.forceSwitch(10);
        log(play.decrementWithMin(false), null);
    }

    /**
     * Moze da se presoblekuvaat maksimum 4 paralelno. Ne treba eden po eden.
     */
    public void presobleci() {
        log(dressingRoom.incrementWithMax(false), "se presoblekuvam");
        Switcher.forceSwitch(10);
        log(dressingRoom.decrementWithMin(false), null);
        log(finishedPlayers.incrementWithMax(false), null);
    }

    /**
     * Treba site 12 igraci da zavrsile so igranjeto. Se povikuva samo od eden.
     */
    public void slobodnaSala() {
        log(finishedPlayers.assertEquals(12, FINISHED_PLAY_POINTS,
                FINISHED_PLAY_MESSAGE), "zatvoram sala");
        log(enteredPlayers.checkRaceCondition(), null);
        enteredPlayers.setValue(0);
        finishedPlayers.setValue(0);
    }

    @Override
    public void finalize() {
        if (dressingRoom.getMax() == 1) {
            logException(new PointsException(DRESSING_NOT_PARALLEL_POINTS,
                    DRESSING_NOT_PARALLEL));
        }

        if (play.getMax() == 1) {
            logException(new PointsException(PLAYING_NOT_PARALLEL_POINTS,
                    PLAYING_NOT_PARALLEL));
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
        try {
            finalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public BoundCounterWithRaceConditionCheck(int value, Integer maxAllowed,
                                              int maxErrorPoints, String maxErrorMessage, Integer minAllowed,
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
    public synchronized PointsException assertEquals(int val, int points,
                                                     String errorMessage) {
        if (this.value != val) {
            PointsException e = new PointsException(points, errorMessage);
            return e;
        } else {
            return null;
        }
    }

    public synchronized PointsException assertNotEquals(int val, int points,
                                                        String errorMessage) {
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

abstract class ProblemExecution {

    public static void start(HashSet<Thread> threads, AbstractState state)
            throws Exception {

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

    private static void checkDeadlock(HashSet<Thread> threads,
                                      AbstractState state) {
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
