package mk.ukim.finki.aud1.exceptions;

/**
 * @author Kostadin Krstev
 */
public class NotDirectoryException extends RuntimeException {
    public NotDirectoryException(String absolutePath) {
        super(String.format("%s is not a directory", absolutePath));
    }
}
