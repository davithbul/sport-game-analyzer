package com.el.robot.analyzer.analytic.ml.exceptions;

/**
 * {@link ClassifierBuildFailedException} throws when failing to
 * build classifier from given data.
 */
public class ClassifierBuildFailedException extends MLException {

    public ClassifierBuildFailedException() {
    }

    public ClassifierBuildFailedException(String message) {
        super(message);
    }

    public ClassifierBuildFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassifierBuildFailedException(Throwable cause) {
        super(cause);
    }

    public ClassifierBuildFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
