package start.service.exception;

/**
 * Exception pro nenalezené entity.
 * Používá se když entita (Customer, Order, Product, Cart, atd.) není nalezena v databázi.
 * Rozšiřuje BusinessException, protože "entity not found" je business chyba.
 */
public class NotFoundException extends BusinessException {
    
    public NotFoundException(String entityType, Object identifier) {
        super(String.format("%s not found: %s", entityType, identifier));
    }
}

