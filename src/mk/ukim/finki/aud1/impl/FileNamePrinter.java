package mk.ukim.finki.aud1.impl;

import mk.ukim.finki.aud1.interfaces.FileInfoPrinter;

import java.io.File;
import java.io.PrintStream;

/**
 * @author Kostadin Krstev
 */
public class FileNamePrinter implements FileInfoPrinter {
    @Override
    public void printInfo(PrintStream out, File file) {
        out.println(file.getAbsoluteFile());
    }
}
