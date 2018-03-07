package mk.ukim.finki.lab1;

import java.io.File;

/**
 * @author Kostadin Krstev
 */
public class HW01_1 {
    private static int totalLength = 0;
    private static int totalFiles = 0;

    private static void averageFolderSize(File folder) {
        File[] files = folder.listFiles((dir, name) -> {
            File file = new File(dir, name);
            return file.isDirectory() || (file.isFile() && name.endsWith(".txt"));
        });
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                totalLength += file.length();
                ++totalFiles;
            }
            if (file.isDirectory())
                averageFolderSize(file);
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("At least 1 argument must be provided!");
            System.exit(-1);
        }
        File folder = new File(args[0]);
        if (folder.exists() && folder.isDirectory()) {
            averageFolderSize(folder);
            if (totalFiles != 0)
                System.out.println(totalLength / (double) totalFiles);
            else
                System.out.println(0);
        } else {
            System.err.println("The given folder might not exists or is a file!");
            System.exit(-1);
        }
    }
}
