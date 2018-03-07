package mk.ukim.finki.lab1;

import java.io.*;

/**
 * @author Kostadin Krstev
 */
public class HW01_3 {
    private static final String source = "/home/krstevkoki/Desktop/izvor.txt";
    private static final String dest = "/home/krstevkoki/Desktop/destinacija.txt";

    private static void writeToFile(File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) {
            try (Writer writer = new FileWriter(file)) {
                writer.write("Оперативни системи\n");
                writer.write("Алгоритми и податични структури\n");
                writer.write("Напредно програмирање");
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source)));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest)))
            ) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null && line.length() > 0)
                    sb.append(line).append("\n");
                writer.write(sb.reverse().toString().substring(1));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        createSource();
        createDest();
        reverseContentFromFile(new File(source), new File(dest));
    }


}
