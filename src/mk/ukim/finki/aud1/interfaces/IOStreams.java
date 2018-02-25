package mk.ukim.finki.aud1.interfaces;

import java.io.*;

public interface IOStreams {
    void deleteFolder(File file);

    void copyFileByteByByte(File from, File to) throws IOException;

    void badCopyFileByUsingBuffer(String from, String to) throws IOException;

    void correctCopyFileByUsingBuffer(String from, String to) throws IOException;

    void readContentOfTextFile(File from, BufferedWriter to) throws IOException;

    void readContentFromStdInput(OutputStream to) throws IOException;

    void writeToTextFile(File to, String text, Boolean append) throws IOException;

    void memorySafeTextFileCopy(File from, File to) throws IOException;

    void readFileWithLineNumber(File from, OutputStream is) throws IOException;

    void writeBinaryDataToBFile(File to, Object... objects) throws IOException;

    void readBinaryDataFromBFile(File from, Object... objects) throws IOException;

    void readAndWriteFromRandomAccessFile(File from) throws IOException;

    void redirectStdInAndStdOut(InputStream input, PrintStream output) throws IOException;
}
