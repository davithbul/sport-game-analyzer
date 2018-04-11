package com.el.robot.analyzer.analytic.changepredictor.model;

import com.el.betting.sdk.v2.Score;

import java.util.HashMap;
import java.util.Map;

public class ScoreChangeJudgement {
    private double scoreWillRemainOdd;
    private double scoreWillBeChangedOdd;
    private final Map<Score<Integer>, Double> nextExpectedScores = new HashMap<>();
    //show the percent of cases when current score situation happened
    private double confidence;

    public double getScoreWillRemainOdd() {
        return scoreWillRemainOdd;
    }

    public void setScoreWillRemainOdd(double scoreWillRemainOdd) {
        this.scoreWillRemainOdd = scoreWillRemainOdd;
    }

    public double getScoreWillBeChangedOdd() {
        return scoreWillBeChangedOdd;
    }

    public void setScoreWillBeChangedOdd(double scoreWillBeChangedOdd) {
        this.scoreWillBeChangedOdd = scoreWillBeChangedOdd;
    }

    public void addNextExpectedScore(Score<Integer> score, double probability) {
        nextExpectedScores.put(score, probability);
    }

    public Map<Score<Integer>, Double> getNextExpectedScores() {
        return nextExpectedScores;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getConfidence() {
        return confidence;
    }
}
