package plant_village.exception;

/**
 * İş mantığı doğrulaması başarısız olduğunda throw edilecek exception
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
