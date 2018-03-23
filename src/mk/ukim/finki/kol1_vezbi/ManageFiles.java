package mk.ukim.finki.kol1_vezbi;

import java.io.*;

/**
 * @author Kostadin Krstev
 */
public class ManageFiles {
    private final static String pathIn = "/home/krstevkoki/Desktop/in";
    private final static String pathOut = "/home/krstevkoki/Desktop/out";
    private final static String pathResources = "/home/krstevkoki/Desktop/in/resources/writable-content.txt";

    private static void manage(String in, String out) throws IOException {
        File folderIn = new File(in);
        File folderOut = new File(out);

        if (!folderIn.exists())
            throw new DirectoryNotFoundException("ne postoi");
        if (!folderIn.isDirectory())
            throw new NotDirectoryException("ne e folder");
        if (folderOut.exists())
            removeContent(folderOut);

        File[] files = folderIn.listFiles((dir, name) -> name.endsWith(".dat"));
        assert files != null;
        for (File file : files) {
            if (file.isHidden()) {
                file.delete();
                log("zbunet sum: " + file.getAbsolutePath());
            } else {
                if (file.canWrite()) {
                    if (!folderOut.exists()) {
                        folderOut.mkdirs();
                    }
                    String path = folderOut.getAbsolutePath() + "/" + file.getName();
                    File newLocation = new File(path);
                    file.renameTo(newLocation);
                    log("pomestuvam: " + file.getAbsolutePath());
                } else {
                    try (FileWriter writer = new FileWriter(new File(pathResources), true);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
                    ) {
                        String line;
                        while ((line = reader.readLine()) != null && line.length() > 0) {
                            writer.write(line);
                            writer.write("\n");
                        }
                    }
                    log("dopisuvam: " + file.getAbsolutePath());
                }
            }
        }
    }

    private static void removeContent(File folder) {
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            file.delete();
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) throws IOException {
        manage(pathIn, pathOut);
    }
}

class NotDirectoryException extends RuntimeException {
    public NotDirectoryException(String message) {
        super(message);
    }
}

class DirectoryNotFoundException extends RuntimeException {
    public DirectoryNotFoundException(String message) {
        super(message);
    }
}
