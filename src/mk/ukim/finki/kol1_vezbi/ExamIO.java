package mk.ukim.finki.kol1_vezbi;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Kostadin Krstev
 */
public class ExamIO {
    private static final String FROM_PATH = "/home/krstevkoki/Desktop/from";
    private static final String TO_PATH = "/home/krstevkoki/Desktop/result/to";
    private static final String DESTINATION = "/home/krstevkoki/Desktop/data.ser";
    private static final int ELEMENTS_LENGTH = 23;
    private static final int BYTE_LENGTH = 1;

    private static void copyLargeTxtFiles(String from, String to, long size) throws IOException {
        File folderFrom = new File(from);
        File folderTo = new File(to);

        if (!folderFrom.exists())
            throw new FileNotFoundException("Ne postoi");
        if (!folderFrom.isDirectory())
            throw new NotDirectoryException("Ne e folder");
        if (!folderTo.exists()) {
            if (!folderTo.mkdirs())
                return;
            log("Created: " + folderTo.getAbsolutePath());
        }

        File[] files = folderFrom.listFiles((dir, name) -> {
            File f = new File(dir, name);
            return f.getName().endsWith(".txt") && f.length() > size;
        });

        assert files != null;
        for (File file : files) {
            log("Copying: " + file.getAbsolutePath());
            String path = folderTo.getAbsolutePath() + "/" + file.getName();
            File newFile = new File(path);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)))
            ) {
                String line;
                while ((line = reader.readLine()) != null && line.length() > 0)
                    writer.write(line + "\n");
            }
            log("Copied: " + newFile.getAbsolutePath());
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void serializeData(String destination, List<byte[]> data) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(new File(destination))) {
            for (byte[] d : data) {
                /*for (int i = 0; i < ELEMENTS_LENGTH; ++i) {
                    outputStream.write(d[i]);
                }*/
                outputStream.write(d, 0, ELEMENTS_LENGTH);
            }
        }
    }

    private static byte[] deserializeDataAtPosition(String source, long position, long elementLength) throws IOException {
        byte[] b = new byte[(int) elementLength];
        try (RandomAccessFile raf = new RandomAccessFile(new File(source), "r")) {
            raf.seek(position);
            for (int i = 0; i < elementLength; ++i) {
                b[i] = raf.readByte();
            }
        }
        return b;
    }

    public static void main(String[] args) throws IOException {
        Random random = new Random();
        copyLargeTxtFiles(FROM_PATH, TO_PATH, random.nextLong() % 541);

        List<byte[]> data = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            byte[] temp = new byte[ELEMENTS_LENGTH];
            random.nextBytes(temp);
//            Arrays.fill(temp, (byte) (i + 1));
            data.add(temp);
        }
        serializeData(DESTINATION, data);
        byte[] b = deserializeDataAtPosition(DESTINATION, (ELEMENTS_LENGTH * BYTE_LENGTH) * 3, ELEMENTS_LENGTH);
        System.out.println(Arrays.toString(b));
    }
}
