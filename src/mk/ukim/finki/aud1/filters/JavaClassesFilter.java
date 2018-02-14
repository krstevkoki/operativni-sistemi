package mk.ukim.finki.aud1.filters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Kostadin Krstev
 */
public class JavaClassesFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        return file.isDirectory() || (file.isFile() && name.endsWith(".java"));
    }
}
