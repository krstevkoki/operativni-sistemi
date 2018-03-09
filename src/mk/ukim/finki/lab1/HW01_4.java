package mk.ukim.finki.lab1;

import java.io.*;

/**
 * @author Kostadin Krstev
 */
public class HW01_4 {
    private static int countOccurrences(String path, String word) throws IOException {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            int counter = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                String line;
                while ((line = reader.readLine()) != null && line.length() > 0) {
                    String[] lineParts = line.split("\\W+");
                    for (String linePart : lineParts) {
                        if (linePart.equals(word))
                            ++counter;
                    }
                }
            }
            return counter;
        } else {
            System.err.println("The given file is a directory or does not exists.");
            return -1;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("At least 2 arguments must be provided.");
            System.exit(-1);
        }
        System.out.println(countOccurrences(args[0], args[1]));
    }
}
