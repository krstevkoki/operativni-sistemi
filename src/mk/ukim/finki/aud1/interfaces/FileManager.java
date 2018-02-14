package mk.ukim.finki.aud1.interfaces;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Kostadin Krstev
 */
public interface FileManager {
    String getWorkingDirectoryAbsolutePath();

    File getWorkingDirectoryAsFile();

    File getParentDirectory(File file);

    boolean deleteDirectoryRecursively(File directory);

    boolean createDirectoryOnlyIfParentExists(String path);

    boolean createDirectoryWithItsParents(String path);

    boolean createFile(String path) throws IOException;

    boolean renameFile(File file, String newName);

    boolean moveFile(File file, String newParent);

    boolean moveAndRenameFile(File file, String newParent, String newName);

    void printFilteredDirectoryContentRecursively(
            File directory,
            FilenameFilter filter,
            FileInfoPrinter printer,
            PrintStream out
    );
}
