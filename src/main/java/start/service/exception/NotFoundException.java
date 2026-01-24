package start.service.exception;

public class NotFoundException extends BusinessException {
    
    public NotFoundException(String entityType, Object identifier) {
        super(String.format("%s not found: %s", entityType, identifier));
    }
}

