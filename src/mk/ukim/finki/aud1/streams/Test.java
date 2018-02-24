package mk.ukim.finki.aud1.streams;

import java.io.*;

/**
 * @author Kostadin Krstev
 */
public class Test {
    private final static String to = "/home/krstevkoki/Desktop/out.txt";
    private final static String from = "/home/krstevkoki/Desktop/in.txt";
    private final static String target = "/home/krstevkoki/Desktop/target.txt";
    private final static String binaryDat = "/home/krstevkoki/Desktop/binary.dat";

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        int c;
        while ((c = in.read()) != -1)
            out.write(c);
    }

    public static void badReading(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[30];
        in.read(buffer);
        out.write(buffer);
    }

    public static String readTextFile(File from) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (from.exists() && from.isFile()) {
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(new FileInputStream(from), "UTF-8"))) {
                reader.lines()
                        .forEach(line -> sb.append(line).append("\n"));
            }
        }
        return sb.toString();
    }

    public static void copyTextFile(File from, File to) throws IOException {
        if (from.exists() && from.isFile() && to.exists() && to.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(from)));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(to)))
            ) {
                reader.lines()
                        .forEach(line -> {
                            try {
                                writer.write(line);
                                writer.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }

    public static void stdinReading() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null && line.length() > 0)
                System.out.println(line);
        }
    }

    public static void stdinReading(File to) throws IOException {
        if (to.exists() && to.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(to)))
            ) {
                String line;
                while ((line = reader.readLine()) != null && line.length() > 0) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    public static void fileOutputWithLineNumbers(File from, File to) throws IOException {
        if (from.exists() && from.isFile() && to.exists() && to.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(from));
                 PrintWriter writer = new PrintWriter(new FileWriter(to))
            ) {
                int lineCount = 1;
                String line;
                while ((line = reader.readLine()) != null && line.length() > 0)
                    writer.write(String.format("%d. %s\n", lineCount++, line));
            }
        }
    }

    public static void dataReadWrite(File target) throws IOException {
        if (target.exists() && target.isFile()) {
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(target)));
                 DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(target)))
            ) {
                dos.writeDouble(Math.PI);
                dos.writeUTF("That was PI");
                dos.writeDouble(Math.sqrt(2));
                dos.writeUTF("That was sqare root of 2");
                dos.flush();
                System.out.println(dis.readDouble());
                System.out.println(dis.readUTF());
                System.out.println(dis.readDouble());
                System.out.println(dis.readUTF());
            }
        }
    }

    public static void display(File binaryFile) throws IOException {
        if (binaryFile.exists() && binaryFile.isFile()) {
            try (RandomAccessFile raf = new RandomAccessFile(binaryFile, "r")) {
                for (int i = 1; i <= 7; ++i)
                    System.out.println(String.format("Value %d: %f", i, raf.readDouble()));
                System.out.println(raf.readUTF());
            }
        }
    }

    public static void randomAccess(File binaryFile) throws IOException {
        if (binaryFile.exists() && binaryFile.isFile()) {
            try (RandomAccessFile raf = new RandomAccessFile(binaryFile, "rw")) {
                for (int i = 1; i <= 7; ++i)
                    raf.writeDouble(i * Math.sqrt(2));
                raf.writeUTF("End of the file");
                display(binaryFile);
            }
            try (RandomAccessFile raf = new RandomAccessFile(binaryFile, "rw")) {
                raf.seek(5 * 8);  // prints Value 6: 8.485281374238571
                System.out.println(raf.readDouble());
                raf.seek(3 * 8);
                System.out.println(raf.readDouble());
                raf.seek(6 * 8);
                System.out.println(raf.readDouble());
                raf.seek(7 * 8);
                System.out.println(raf.readUTF());
                raf.seek(12 * 8);
                raf.writeDouble(Math.PI);
                raf.seek(450);
                raf.writeUTF("haha");
            }
        }
    }

    public static void writeToFile(File to) throws IOException {
        if (!to.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(to)))) {
                for (int i = 0; i < 51; ++i) {
                    writer.write(String.format("%d", i));
                    if (i != 50)
                        writer.newLine();
                }
            }
        }
    }

    public static void redirect(File from, File to) throws IOException {
        InputStream tempSystemIn = System.in;
        PrintStream tempSystemOut = System.out;
        try (InputStream in = new BufferedInputStream(new FileInputStream(from));
             PrintStream out = new PrintStream(new FileOutputStream(to));

        ) {
            System.setIn(in);
            System.setOut(out);
            System.setErr(out);
            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
            bf.lines()
                    .forEach(System.out::println);
            System.err.println("Error");
            System.setIn(tempSystemIn);
            System.setOut(tempSystemOut);
            System.setErr(tempSystemOut);
        }
    }

    public static void main(String[] args) throws IOException {
        writeToFile(new File(from));
        try (InputStream in = new FileInputStream(new File(from));
             OutputStream out = new FileOutputStream(new File(to))
        ) {
            copyStream(in, out);
            badReading(in, out);
        }
        System.out.println(readTextFile(new File(from)));
        copyTextFile(new File(from), new File(to));
        stdinReading();
        stdinReading(new File(to));
        fileOutputWithLineNumbers(new File(from), new File(to));
        dataReadWrite(new File(target));
        randomAccess(new File(binaryDat));
        redirect(new File(from), new File(to));
        System.out.println("hello");
        System.err.println("Error");
    }
}
