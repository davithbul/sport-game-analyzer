package com.el.robot.analyzer.model;

/**
 * Represents the change which happen during game.
 * The change might be - new goal, or corner or red card, etc.
 */
public class EventChange {

    /**
     * The probability of the change.
     */
    private double price;

    public EventChange(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "EventChange{" +
                "price=" + price +
                '}';
    }
}
