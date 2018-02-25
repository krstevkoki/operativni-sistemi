package mk.ukim.finki.aud1.impl;

import mk.ukim.finki.aud1.interfaces.IOStreams;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Kostadin Krstev
 */
public class IOStreamsImpl implements IOStreams {
    private static final int MAX_SIZE = 100;

    @Override
    public void deleteFolder(File file) {
        if (file.exists()) {
            if (file.isFile())
                file.delete();
            else {
                File[] files = file.listFiles();
                assert files != null;
                for (File f : files) {
                    if (f.isFile())
                        f.delete();
                    if (f.isDirectory())
                        deleteFolder(f);

                }
                file.delete();
            }
        }
    }

    @Override
    public void copyFileByteByByte(File from, File to) throws IOException {
        if (from != null && from.exists() && from.isFile()) {
            try (InputStream fis = new FileInputStream(from);
                 OutputStream fos = new FileOutputStream(to)
            ) {
                int c;
                while ((c = fis.read()) != -1)
                    fos.write(c);
            }
        }
    }

    @Override
    public void badCopyFileByUsingBuffer(String from, String to) throws IOException {
        if (from != null && to != null) {
            try (InputStream fis = new FileInputStream(new File(from));
                 OutputStream fos = new FileOutputStream(new File(to))
            ) {
                byte[] buffer = new byte[100];
                fis.read(buffer);
                fos.write(buffer);
            }
        }
    }

    @Override
    public void correctCopyFileByUsingBuffer(String from, String to) throws IOException {
        if (from != null && to != null) {
            try (InputStream fis = new FileInputStream(new File(from));
                 OutputStream fos = new FileOutputStream(new File(to))
            ) {
                byte[] buffer = new byte[MAX_SIZE];
                int bytesRead;
                int totalRead = 0;
                while ((bytesRead = fis.read(buffer, totalRead, MAX_SIZE - totalRead)) != -1) {
                    fos.write(buffer, totalRead, bytesRead);
                    totalRead += bytesRead % MAX_SIZE;
                }
            }
        }
    }

    @Override
    public void readContentOfTextFile(File from, BufferedWriter to) throws IOException {
        if (from != null && from.exists() && from.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(from)))) {
                reader.lines()
                        .forEach(line -> {
                            try {
                                to.write(line);
                                to.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }

    @Override
    public void readContentFromStdInput(OutputStream to) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(to, StandardCharsets.UTF_8))
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

    @Override
    public void writeToTextFile(File to, String text, Boolean append) throws IOException {
        try (Writer writer = new FileWriter(to, append)) {
            writer.write(text);
        }
    }

    @Override
    public void memorySafeTextFileCopy(File from, File to) throws IOException {
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

    @Override
    public void readFileWithLineNumber(File from, OutputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(from)));
             Writer writer = new PrintWriter(is)
        ) {
            String line;
            int lineCount = 1;
            while ((line = reader.readLine()) != null && line.length() > 0)
                writer.write(String.format("%d: %s\n", lineCount++, line));
        }
    }

    @Override
    public void writeBinaryDataToBFile(File to, Object... objects) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(to))) {
            for (Object object : objects) {
                if (object instanceof Integer)
                    dos.writeInt((Integer) object);
                if (object instanceof Float)
                    dos.writeFloat((Float) object);
                if (object instanceof Double)
                    dos.writeDouble((Double) object);
                if (object instanceof String)
                    dos.writeUTF(object.toString());
            }
        }
    }

    @Override
    public void readBinaryDataFromBFile(File from, Object... objects) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(from))) {
            for (Object object : objects) {
                if (object instanceof Integer)
                    object = dis.readInt();
                if (object instanceof Float)
                    object = dis.readFloat();
                if (object instanceof Double)
                    object = dis.readDouble();
                if (object instanceof String)
                    object = dis.readUTF();
                System.out.println(object);
            }
        }
    }

    @Override
    public void readAndWriteFromRandomAccessFile(File from) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(from, "rw")) {
            System.out.println(raf.readInt());
            System.out.println(raf.readFloat());
            System.out.println(raf.readUTF());
            System.out.println(raf.readDouble());
            raf.seek(4);
            System.out.println(raf.readFloat());
            raf.seek(4 + 4);
            System.out.println(raf.readUTF());
            raf.seek(4 + 4 + 8);
            System.out.println(raf.readDouble());
            raf.seek(4 + 4 + 8 + 8);
            raf.writeUTF("Kraj");

            raf.seek(0);
            System.out.println(raf.readInt());
            System.out.println(raf.readFloat());
            System.out.println(raf.readUTF());
            System.out.println(raf.readDouble());
            System.out.println(raf.readUTF());

        }
    }

    @Override
    public void redirectStdInAndStdOut(InputStream input, PrintStream output) throws IOException {
        InputStream tempSystemIn = System.in;
        PrintStream tempSystemOut = System.out;
        System.setIn(input);
        System.setOut(output);
        System.setErr(output);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            reader.lines()
                    .forEach(System.out::println);
            System.err.println("error");
        }

        System.setIn(tempSystemIn);
        System.setOut(tempSystemOut);
        System.setErr(tempSystemOut);
    }
}
