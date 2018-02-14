package mk.ukim.finki.aud1.interfaces;

import java.io.File;
import java.io.PrintStream;

/**
 * @author Kostadin Krstev
 */
@FunctionalInterface
public interface FileInfoPrinter {
    void printInfo(PrintStream out, File file);
}
