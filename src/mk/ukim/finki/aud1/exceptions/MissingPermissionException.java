package mk.ukim.finki.aud1.exceptions;

/**
 * @author Kostadin Krstev
 */
public class MissingPermissionException extends RuntimeException {
    public MissingPermissionException(String permission, String absolutePath) {
        super(String.format("%s doesn't have %s permission!", absolutePath, permission));
    }
}
