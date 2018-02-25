package mk.ukim.finki.aud1;

import mk.ukim.finki.aud1.impl.IOStreamsImpl;
import mk.ukim.finki.aud1.interfaces.IOStreams;

import java.io.*;

/**
 * @author Kostadin Krstev
 */
public class IOStreamsTest {
    private final static String fodlerPath = "/home/krstevkoki/Desktop/A";
    private final static String from = "/home/krstevkoki/Desktop/in.txt";
    private final static String to = "/home/krstevkoki/Desktop/out.txt";
    private final static String binaryFile = "/home/krstevkoki/Desktop/bin.dat";

    public static void main(String[] args) throws IOException {
        IOStreams ioStreams = new IOStreamsImpl();
        ioStreams.deleteFolder(new File(fodlerPath));
        ioStreams.copyFileByteByByte(new File(from), new File(to));
        ioStreams.badCopyFileByUsingBuffer(from, to);
        ioStreams.correctCopyFileByUsingBuffer(from, to);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(to)))) {
            ioStreams.readContentOfTextFile(new File(from), writer);
        }
        try (OutputStream fos = new FileOutputStream(new File(to))) {
            ioStreams.readContentFromStdInput(fos);
        }
        ioStreams.writeToTextFile(
                new File(to),
                "Denes e soncevo.\n" +
                        "Ve lazam.\n" +
                        "Vee sneg denes.\n",
                true);
        ioStreams.memorySafeTextFileCopy(new File(from), new File(to));
        try (OutputStream os = new FileOutputStream(new File(to))) {
            ioStreams.readFileWithLineNumber(new File(from), os);
        }
        ioStreams.writeBinaryDataToBFile(new File(binaryFile), 5, 6.2f, "hahaha", Math.PI);
        ioStreams.readBinaryDataFromBFile(new File(binaryFile), 5, 3f, "hihi", Math.E);
        ioStreams.readAndWriteFromRandomAccessFile(new File(binaryFile));
        try (InputStream is = new FileInputStream(new File(from));
             PrintStream ps = new PrintStream(new FileOutputStream(new File(to)))
        ) {
            ioStreams.redirectStdInAndStdOut(is, ps);
        }
        System.out.println("hello");
        System.err.println("Error");
    }
}
