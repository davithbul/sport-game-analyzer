package com.el.robot.analyzer.model;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.TimeRange;
import org.springframework.data.annotation.PersistenceConstructor;

public class ScoreChange extends EventChange {
    private Score<Integer> currentScore;
    private Score<Integer> nextScore;
    private TimeRange<Integer> scoreChangeMinute;

    public ScoreChange(Score<Integer> nextScore, double price) {
        super(price);
        this.nextScore = nextScore;
    }

    public ScoreChange(Score<Integer> nextScore, double price, TimeRange<Integer> scoreChangeMinute) {
        super(price);
        this.nextScore = nextScore;
        this.scoreChangeMinute = scoreChangeMinute;
    }

    public ScoreChange(Score<Integer> currentScore, Score<Integer> nextScore, double price, TimeRange<Integer> scoreChangeMinute) {
        super(price);
        this.currentScore = currentScore;
        this.nextScore = nextScore;
        this.scoreChangeMinute = scoreChangeMinute;
    }

    @PersistenceConstructor
    public ScoreChange(Score<Integer> currentScore, Score<Integer> nextScore, double price) {
        super(price);
        this.currentScore = currentScore;
        this.nextScore = nextScore;
    }

    public Score<Integer> getCurrentScore() {
        return currentScore;
    }

    public Score<Integer> getNextScore() {
        return nextScore;
    }

    public TimeRange<Integer> getScoreChangeMinute() {
        return scoreChangeMinute;
    }

    @Override
    public String toString() {
        return "ScoreChange{" +
                "currentScore=" + currentScore +
                ", nextScore=" + nextScore +
                "} " + super.toString();
    }
}
