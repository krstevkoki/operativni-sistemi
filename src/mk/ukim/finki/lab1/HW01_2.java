package mk.ukim.finki.lab1;

import java.io.*;

/**
 * @author Kostadin Krstev
 */
public class HW01_2 {
    private static final String source = "/home/krstevkoki/Desktop/izvor.txt";
    private static final String dest = "/home/krstevkoki/Desktop/destinacija.txt";

    private static void writeToFile(File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) {
            try (Writer writer = new FileWriter(file)) {
                writer.write("Operativni sistemi\n");
                writer.write("Algoritmi i podatocni strukturi\n");
                writer.write("Napredno programiranje");
//                writer.write("Оперативни системи");
            }
        }
    }

    private static void createSource() throws IOException {
        File file = new File(source);
        if (!file.exists()) {
            if (file.createNewFile()) {
                System.out.println("File " + file.getName() + " created.");
                writeToFile(file);
            }
        }
    }

    private static void createDest() throws IOException {
        File file = new File(dest);
        if (!file.exists()) {
            if (file.createNewFile()) {
                System.out.println("File " + file.getName() + " created.");
            }
        }
    }

    private static void reverseContentFromFile(File source, File dest) throws IOException {
        if (source != null && dest != null && source.exists() && source.isFile()) {
            try (RandomAccessFile raf = new RandomAccessFile(source, "r");
                 OutputStream output = new FileOutputStream(dest)
            ) {
                long bytesRemaining = raf.length();
                raf.seek(bytesRemaining);

                while (true) {
                    int b = raf.read();
                    if (b != -1)
                        output.write(b);
                    --bytesRemaining;
                    if (bytesRemaining < 0)
                        break;
                    else
                        raf.seek(bytesRemaining);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        createSource();
        createDest();
        reverseContentFromFile(new File(source), new File(dest));
    }
}
