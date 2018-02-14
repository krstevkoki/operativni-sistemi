package mk.ukim.finki.aud1.impl;


import mk.ukim.finki.aud1.exceptions.FileExistsException;
import mk.ukim.finki.aud1.exceptions.MissingPermissionException;
import mk.ukim.finki.aud1.exceptions.NotDirectoryException;
import mk.ukim.finki.aud1.interfaces.FileInfoPrinter;
import mk.ukim.finki.aud1.interfaces.FileManager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * @author Kostadin Krstev
 */
public class FileManagerImpl implements FileManager {
    @Override
    public String getWorkingDirectoryAbsolutePath() {
        return new File(".").getAbsolutePath();
    }

    @Override
    public File getWorkingDirectoryAsFile() {
        return new File(".");
    }

    @Override
    public File getParentDirectory(File file) {
        return file.getParentFile();
    }

    @Override
    public boolean deleteDirectoryRecursively(File directory) {
        if (!directory.isDirectory())
            throw new NotDirectoryException(directory.getAbsolutePath());
        if (!directory.canWrite())
            throw new MissingPermissionException("write", directory.getAbsolutePath());

        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory())
                deleteDirectoryRecursively(file);
            else {
                boolean isDeleted = file.delete();
                if (!isDeleted)
                    System.out.println("Can't delete file: " + file.getAbsolutePath());
            }
        }
        return directory.delete();
    }

    @Override
    public boolean createDirectoryOnlyIfParentExists(String path) {
        File file = new File(path);
        return file.mkdir();
    }

    @Override
    public boolean createDirectoryWithItsParents(String path) {
        File file = new File(path);
        return file.mkdirs();
    }

    @Override
    public boolean createFile(String path) throws IOException {
        File file = new File(path);
        if (file.exists())
            throw new FileExistsException(file.getAbsolutePath());
        return file.createNewFile();
    }

    @Override
    public boolean renameFile(File file, String newName) {
        return moveAndRenameFile(file, file.getParent(), newName);
    }

    @Override
    public boolean moveFile(File file, String newParent) {
        return moveAndRenameFile(file, newParent, file.getName());
    }

    @Override
    public boolean moveAndRenameFile(File file, String newParent, String newName) {
        File parent = new File(newParent);
        if (!parent.isDirectory())
            throw new NotDirectoryException(parent.getAbsolutePath());
        File renamedFile = new File(parent, newName);
        if (renamedFile.exists())
            throw new FileExistsException(renamedFile.getAbsolutePath());
        return file.renameTo(renamedFile);
    }

    @Override
    public void printFilteredDirectoryContentRecursively(
            File directory,
            FilenameFilter filter,
            FileInfoPrinter printer,
            PrintStream out) {
        String[] fileNames;
        if (filter == null)
            fileNames = directory.list();
        else
            fileNames = directory.list(filter);
        assert fileNames != null;
        Arrays.sort(fileNames);
        for (String fileName : fileNames) {
            File child = new File(directory, fileName);
            if (child.isDirectory())
                printFilteredDirectoryContentRecursively(child, filter, printer, out);
            else
                printer.printInfo(out, child);
        }
    }
}
