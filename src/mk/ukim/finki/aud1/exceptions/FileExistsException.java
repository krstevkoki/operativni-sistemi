package mk.ukim.finki.aud1.exceptions;

/**
 * @author Kostadin Krstev
 */
public class FileExistsException extends RuntimeException {
    public FileExistsException(String absolutePath) {
        super(String.format("%s already exists", absolutePath));
    }
}
