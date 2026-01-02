package start.service.exception;

/**
 * Exception pro business logiku aplikace.
 * Používá se pro chyby související s business pravidly, jako jsou:
 * - Prázdný košík
 * - Nedostatek zboží na skladě
 * - Neplatný stav objednávky
 * - Neplatný přechod mezi stavy
 * - Objednávka nepatří uživateli
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
}

