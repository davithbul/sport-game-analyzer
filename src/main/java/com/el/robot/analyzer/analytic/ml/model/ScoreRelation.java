package com.el.robot.analyzer.analytic.ml.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ScoreRelation {
    private LocalDateTime startTime;
    private int homeTeamGoalCount;
    private int awayTeamGoalCount;
    private LinkedList<Integer> lastScores = new LinkedList<>();

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getHomeTeamGoalCount() {
        return homeTeamGoalCount;
    }

    public void setHomeTeamGoalCount(int homeTeamGoalCount) {
        this.homeTeamGoalCount = homeTeamGoalCount;
    }

    public int getAwayTeamGoalCount() {
        return awayTeamGoalCount;
    }

    public void setAwayTeamGoalCount(int awayTeamGoalCount) {
        this.awayTeamGoalCount = awayTeamGoalCount;
    }

    public LinkedList<Integer> getLastScores() {
        return lastScores;
    }

    public void addScore(int score) {
        lastScores.add(score);
    }
}
