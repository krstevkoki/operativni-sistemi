package mk.ukim.finki.aud2;

/**
 * @author Kostadin Krstev
 */
public class SharedResource {
    private int i;

    public SharedResource() {
        i = 0;
    }

    public synchronized void increase() {
        i++;
    }

    @Override
    public String toString() {
        return String.valueOf(i);
    }
}
