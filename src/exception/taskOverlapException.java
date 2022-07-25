package exception;

public class taskOverlapException extends RuntimeException {
    public taskOverlapException(String message) {
        super(message);
    }
}
