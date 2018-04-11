package com.el.robot.analyzer.analytic.ml.exceptions;

public class ClassificationFailedException extends MLException {

    public ClassificationFailedException() {
        super();
    }

    public ClassificationFailedException(String message) {
        super(message);
    }

    public ClassificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassificationFailedException(Throwable cause) {
        super(cause);
    }

    public ClassificationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
