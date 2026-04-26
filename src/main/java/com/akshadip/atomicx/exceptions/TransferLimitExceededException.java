package com.akshadip.atomicx.exceptions;

/**
 * Exception thrown when a transfer amount exceeds the configured maximum transfer limit.
 * This exception is thrown before lock acquisition (fail-fast validation).
 */
public class TransferLimitExceededException extends RuntimeException {
    public TransferLimitExceededException(String message) {
        super(message);
    }

    public TransferLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

