package mk.ukim.finki.aud3;

/**
 * @author Kostadin Krstev
 */
public class IntegerWrapper {
    private int counter;

    public IntegerWrapper() {
        this.counter = 0;
    }

    public IntegerWrapper(int counter) {
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    public void increment() {
        this.counter++;
    }

    @Override
    public String toString() {
        return String.valueOf(counter);
    }
}
