package mk.ukim.finki.lab2;

/**
 * @author Kostadin Krstev
 */
public class TwoThreads {
    public static class ThreadAB implements Runnable {
        private String string1;
        private String string2;
//        private boolean printAlphabet;

//        public ThreadAB(boolean printAlphabet) {
//            this.printAlphabet = printAlphabet;
//        }

        public ThreadAB(String string1, String string2) {
            this.string1 = string1;
            this.string2 = string2;
        }

        @Override
        public void run() {
            System.out.println(string1);
            System.out.println(string2);

//            if (printAlphabet)
//                for (int i = 65; i <= 90; ++i)
//                    System.out.println((char) i);
//            else
//                for (int i = 1; i <= 26; ++i)
//                    System.out.println(i);
        }
    }


    public static void main(String[] args) {
        new Thread(new ThreadAB("A", "B")).start();
        new Thread(new ThreadAB("1", "2")).start();
//        new Thread(new ThreadAB(true)).start();
//        new Thread(new ThreadAB(false)).start();
    }
}
