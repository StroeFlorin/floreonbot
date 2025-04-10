package dev.stroe.floreonbot.exception;

public class CurrencyConversionException extends RuntimeException {
    private final String fromCurrency;
    private final String toCurrency;
    private final double amount;
    private final String errorType;

    public CurrencyConversionException(String message, String fromCurrency, String toCurrency, 
                                      double amount, String errorType, Throwable cause) {
        super(message, cause);
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
        this.errorType = errorType;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public double getAmount() {
        return amount;
    }
    
    public String getErrorType() {
        return errorType;
    }
}