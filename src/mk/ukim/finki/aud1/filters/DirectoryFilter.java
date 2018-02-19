package mk.ukim.finki.aud1.filters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Kostadin Krstev
 */
public class DirectoryFilter implements FilenameFilter {
    private String filter;

    public DirectoryFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public boolean accept(File dir, String name) {
        return new File(name).getName().contains(filter);
    }
}
