package mk.ukim.finki.aud1;

import mk.ukim.finki.aud1.filters.DirectoryFilter;

import java.io.File;

/**
 * @author Kostadin Krstev
 */
public class DirList {
    private static final String PATH = "/home/krstevkoki/ama_mjau/";

    public static void listFilesRecursively(String absolutePath, String prefix) {
        File file = new File(absolutePath);
        if (file.exists()) {
            File[] subfiles = file.listFiles();
            if (subfiles != null && subfiles.length > 0) {
                for (File subfile : subfiles) {
                    System.out.println(prefix + "" + subfile.getName() + " " + getPermissions(subfile));
                    if (subfile.isDirectory())
                        listFilesRecursively(subfile.getAbsolutePath(), "\t" + prefix);
                }
            }
        }
    }

    public static String getPermissions(File f) {
        return String.format("%s%s%s",
                f.canRead() ? "r" : "-",
                f.canWrite() ? "w" : "-",
                f.canExecute() ? "e" : "-");
    }

    public static void deleteFolderRecursively(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isFile())
                    f.delete();
                if (f.isDirectory()) {
                    deleteFolderRecursively(f);
                    f.delete();
                }
            }
            file.delete();
        }
    }

    public static void main(String[] args) {
        File file = new File(PATH);
        String[] list;
        if (args.length == 0)
            list = file.list();
        else
            list = file.list(new DirectoryFilter(args[0]));
//            list = file.list((dir, name) -> name.contains(args[0]));
        if (list != null)
            for (String str : list)
                System.out.println(str);

        System.out.println();

        listFilesRecursively(file.getAbsolutePath(), "|");

        deleteFolderRecursively(new File("/home/krstevkoki/Desktop/A"));
    }
}
