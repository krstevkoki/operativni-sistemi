package mk.ukim.finki.aud2.threads;

import mk.ukim.finki.aud2.SharedResource;

/**
 * @author Kostadin Krstev
 */
public class MyRunnableThread implements Runnable {
    private SharedResource resource;

    public MyRunnableThread(SharedResource resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 9000; ++i) {
            resource.increase();
            System.out.println(resource);
        }
    }
}
