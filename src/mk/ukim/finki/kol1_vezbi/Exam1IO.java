package mk.ukim.finki.kol1_vezbi;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kostadin Krstev
 */
public class Exam1IO {
    private static final String PATH_FROM = "/home/krstevkoki/Desktop/from";
    private static final String PATH_TO = "/home/krstevkoki/Desktop/to";
    private static final String SOURCE = "/home/krstevkoki/Desktop/data.ser";
    private static final String DESTINATION = "/home/krstevkoki/Desktop/destination.txt";
    private static final int ELEMENT_LENGTH = 5;

    private static void log(String message) {
        System.out.println(message);
    }

    private static void moveWritableTxtFiles(String from, String to) throws IOException {
        File folderFrom = new File(from);
        File folderTo = new File(to);

        if (!folderFrom.exists())
            throw new FileNotFoundException("Ne postoi");
        if (!folderFrom.isDirectory())
            throw new NotDirectoryException("Ne e folder");
        if (!folderTo.exists()) {
            if (!folderTo.mkdirs()) {
                log("Cannot create: " + folderTo.getAbsolutePath());
                return;
            }
            log("Created: " + folderTo.getAbsolutePath());
        }

        File[] files = folderFrom.listFiles((dir, name) -> {
            File f = new File(dir, name);
            return f.isDirectory() || (f.isFile() && f.getName().endsWith(".txt"));
        });
        assert files != null;

        for (File file : files) {
            if (file.isFile() && file.canWrite()) {
                String newPath = folderTo.getAbsolutePath() + "/" + file.getName();
                File newFile = new File(newPath);
                if (!file.renameTo(newFile))
                    log("Cannot move: " + file.getAbsolutePath());
                else
                    log("Moved: " + newFile.getAbsolutePath());
            }
            if (file.isDirectory())
                moveWritableTxtFiles(file.getAbsolutePath(), folderTo.getAbsolutePath());
        }
    }

    private static void deserializeData(String source, List<byte[]> data, long elementLength) throws IOException {
        try (InputStream inputStream = new FileInputStream(new File(source))) {
            byte[] buffer = new byte[(int) elementLength];
            int bytesRead;
            int totalRead = 0;
            while ((bytesRead = inputStream.read(buffer, totalRead, (int) elementLength - totalRead)) != -1) {
                if (bytesRead == elementLength)
                    data.add(buffer.clone());
                totalRead += (bytesRead % (int) elementLength);
            }
        }
    }

    private static void invertLargeFile(String source, String destination) throws IOException {
        File in = new File(source);
        File out = new File(destination);

        if (!in.exists())
            throw new FileNotFoundException("Ne postoi");
        if (!in.isFile())
            throw new IOException("Ne e fajl");

        try (RandomAccessFile raf = new RandomAccessFile(in, "r");
             OutputStream outputStream = new FileOutputStream(out)
        ) {
            long bytesRemaining = raf.length();
            raf.seek(bytesRemaining);
            while (true) {
                int c = raf.read();
                if (c != -1) {
                    outputStream.write(c);
                }
                bytesRemaining--;
                if (bytesRemaining < 0)
                    break;
                else
                    raf.seek(bytesRemaining);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        moveWritableTxtFiles(PATH_FROM, PATH_TO);
        List<byte[]> data = new ArrayList<>();
        deserializeData(SOURCE, data, ELEMENT_LENGTH);
        for (byte[] niza : data)
            System.out.println(Arrays.toString(niza));
        invertLargeFile(SOURCE, DESTINATION);
    }
}
