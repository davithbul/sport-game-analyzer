package com.el.robot.analyzer.analytic.changepredictor.model;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.TimeRange;
import com.el.robot.analyzer.model.ScoreChange;

import java.util.ArrayList;
import java.util.List;

public class NextScoreAnalyze {

    private double homeTeamWinOdd;
    private double awayTeamWinOdd;
    private double drawOdd;

    private final List<ScoreChange> nextExpectedScores = new ArrayList<>();

    public double getHomeTeamWinOdd() {
        return homeTeamWinOdd;
    }

    public void setHomeTeamWinOdd(double homeTeamWinOdd) {
        this.homeTeamWinOdd = homeTeamWinOdd;
    }

    public double getAwayTeamWinOdd() {
        return awayTeamWinOdd;
    }

    public void setAwayTeamWinOdd(double awayTeamWinOdd) {
        this.awayTeamWinOdd = awayTeamWinOdd;
    }

    public double getDrawOdd() {
        return drawOdd;
    }

    public void setDrawOdd(double drawOdd) {
        this.drawOdd = drawOdd;
    }

    public void addScore(Score<Integer> score, double odd) {
        nextExpectedScores.add(new ScoreChange(score, odd));
    }

    public void addScore(Score<Integer> score, double odd, TimeRange<Integer> minute) {
        nextExpectedScores.add(new ScoreChange(score, odd, minute));
    }

    public List<ScoreChange> getNextExpectedScores() {
        return nextExpectedScores;
    }
}
