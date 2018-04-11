package com.el.robot.analyzer.analytic.ml.model;

public class DateFeature extends Feature {
    private final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private final String dateFormat;


    public DateFeature(String name) {
        this(name, DEFAULT_DATE_FORMAT);
    }

    public DateFeature(String name, String dateFormat) {
        super(name);
        this.dateFormat = dateFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public String toString() {
        return "DateFeature{" +
                "dateFormat='" + dateFormat + '\'' +
                "} " + super.toString();
    }
}
