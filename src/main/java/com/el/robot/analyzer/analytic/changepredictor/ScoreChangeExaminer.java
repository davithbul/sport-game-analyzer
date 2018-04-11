package com.el.robot.analyzer.analytic.changepredictor;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.Team;
import com.el.robot.analyzer.analytic.changepredictor.model.ScoreChangeJudgement;

/**
 * Judges score changes and puts the probabilities for those changes.
 */
public interface ScoreChangeExaminer {

    ScoreChangeJudgement estimateNextScore(Score<Integer> currentScore, String category, String league, String team, Team.Side side);
}
