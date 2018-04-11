package com.el.robot.analyzer.analytic.ml.exceptions;

public class MLEvaluationException extends MLException {

    public MLEvaluationException() {
    }

    public MLEvaluationException(String message) {
        super(message);
    }

    public MLEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MLEvaluationException(Throwable cause) {
        super(cause);
    }

    public MLEvaluationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
