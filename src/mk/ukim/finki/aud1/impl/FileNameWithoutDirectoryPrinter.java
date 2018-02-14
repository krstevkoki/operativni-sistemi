package mk.ukim.finki.aud1.impl;

import mk.ukim.finki.aud1.interfaces.FileInfoPrinter;

import java.io.File;
import java.io.PrintStream;

/**
 * @author Kostadin Krstev
 */
public class FileNameWithoutDirectoryPrinter implements FileInfoPrinter {
    @Override
    public void printInfo(PrintStream out, File file) {
        if (file.isFile())
            out.println(file.getAbsolutePath());
    }
}
