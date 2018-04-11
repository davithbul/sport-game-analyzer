package com.el.robot.analyzer.analytic.ml.exceptions;

/**
 * Represents machine learning exception.
 */
public class MLException extends RuntimeException {

    public MLException() {
    }

    public MLException(String message) {
        super(message);
    }

    public MLException(String message, Throwable cause) {
        super(message, cause);
    }

    public MLException(Throwable cause) {
        super(cause);
    }

    public MLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
