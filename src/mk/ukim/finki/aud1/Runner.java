package mk.ukim.finki.aud1;

import mk.ukim.finki.aud1.filters.JavaClassesFilter;
import mk.ukim.finki.aud1.impl.FileManagerImpl;
import mk.ukim.finki.aud1.impl.FileNamePrinter;
import mk.ukim.finki.aud1.interfaces.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * @author Kostadin Krstev
 */
public class Runner {
    public static void main(String[] args) throws IOException {
        FileManager manager = new FileManagerImpl();
        System.out.println(manager.getWorkingDirectoryAbsolutePath());

        File currentDirectory = manager.getWorkingDirectoryAsFile();
        System.out.println(currentDirectory);

        boolean status = manager.createDirectoryWithItsParents("test-io/asd");
        System.out.println("directory created? " + status);

        /*status = manager.createFile("test-io/asd/bsd.txt");
        System.out.println("file created? " + status);*/

        /*status = manager.deleteDirectoryRecursively(new File("test-io"));
        System.out.println("directory deleted? " + status);*/

        File tester = new File("test-io/asd/bsd.txt");
        if (tester.exists() && tester.isDirectory())
            manager.printFilteredDirectoryContentRecursively(
                    tester,
                    new JavaClassesFilter(),
                    new FileNamePrinter(),
                    System.out);
        else System.out.println("Given directory might not exists or its a file");

        /*status = manager.moveAndRenameFile(tester, "test-io", "bsd.txt");
        System.out.println(status);*/

    }
}
