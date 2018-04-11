package com.el.robot.analyzer.analytic.changepredictor.behaviour;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.TeamStrategy;
import com.el.robot.analyzer.analytic.changepredictor.model.NextScoreAnalyze;
import com.el.robot.crawler.db.v3.TeamStrategyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.el.betting.sdk.v3.common.OddsUtils.sumUpOdds;

/**
 * Predicts games scenario based on current situation and team usual
 * response to similar scenarios.
 */
@SuppressWarnings("unchecked")
@Service
public class BehaviourAnalyzer {

    @Autowired
    private TeamStrategyManager teamStrategyManager;

    public NextScoreAnalyze analyzeNextScores(Score<Integer> currentScore, String category, String league, String homeTeam, String awayTeam) {
        TeamStrategy homeTeamStrategy = teamStrategyManager.find(category, league, homeTeam);
        TeamStrategy awayTeamStrategy = teamStrategyManager.find(category, league, awayTeam);

        if(homeTeamStrategy == null || awayTeamStrategy == null) {
            return new NextScoreAnalyze();
        }

        NextScoreAnalyze nextScoreAnalyze;

        //now we check different scenario, let's say current score is 0-0
        if (currentScore.getHomeSideScore() == 0 && currentScore.getAwaySideScore() == 0) {
            nextScoreAnalyze = analyze0_0(homeTeamStrategy, awayTeamStrategy);
        } else if (currentScore.getHomeSideScore() == 1 && currentScore.getAwaySideScore() == 0) { //if score is 1-0
            nextScoreAnalyze = analyze1_0(homeTeamStrategy, awayTeamStrategy);
        } else if (currentScore.getHomeSideScore() == 1 && currentScore.getAwaySideScore() == 0) { //if score is 0-1
            nextScoreAnalyze = analyze0_1(homeTeamStrategy, awayTeamStrategy);
        } else {
            return new NextScoreAnalyze();
        }

        return nextScoreAnalyze;
    }

    private NextScoreAnalyze analyze0_0(TeamStrategy homeTeamStrategy, TeamStrategy awayTeamStrategy) {
        NextScoreAnalyze nextScoreAnalyze = new NextScoreAnalyze();
        TimeRange<Integer> firstGoalScoreMinuteAtHome = homeTeamStrategy.getFirstGoalScoreMinuteAtHome();
        TimeRange<Integer> firstGoalConcedeMinuteAtHome = homeTeamStrategy.getFirstGoalConcedeMinuteAtHome();

        TimeRange<Integer> firstGoalScoreMinuteAway = awayTeamStrategy.getFirstGoalScoreMinuteAway();
        TimeRange<Integer> firstGoalConcedeMinuteAway = awayTeamStrategy.getFirstGoalConcedeMinuteAway();

        double scoresFirstPercentAtHome = homeTeamStrategy.getScoresFirstPercentAtHome();
        double concedeFirstPercentAtHome = homeTeamStrategy.getConcedeFirstPercentAtHome();

        double scoresFirstPercentAway = awayTeamStrategy.getScoresFirstPercentAway();
        double concedeFirstPercentAway = awayTeamStrategy.getConcedeFirstPercentAway();

        double firstScoreHomeTeamPercent = sumUpOdds(scoresFirstPercentAtHome, concedeFirstPercentAway);
        double firstScoreAwayTeamPercent = sumUpOdds(concedeFirstPercentAtHome, scoresFirstPercentAway);

        //team 1 will score between [earliestMinuteAwayTeamScore, earliestMinuteAwayTeamScore]
        TimeRange<Integer> scoreMinuteRange  = findAvgRange(firstGoalScoreMinuteAtHome, firstGoalConcedeMinuteAway);
        nextScoreAnalyze.addScore(new Score<>(1, 0), firstScoreHomeTeamPercent, scoreMinuteRange);

        //team 2 will score between [earliestMinuteAwayTeamScore, avgMinuteAwayTeamScore]
        scoreMinuteRange = findAvgRange(firstGoalScoreMinuteAway, firstGoalConcedeMinuteAtHome);
        nextScoreAnalyze.addScore(new Score<>(0, 1), firstScoreAwayTeamPercent, scoreMinuteRange);
        return nextScoreAnalyze;
    }

    private NextScoreAnalyze analyze1_0(TeamStrategy homeTeamStrategy, TeamStrategy awayTeamStrategy) {
        NextScoreAnalyze nextScoreAnalyze = new NextScoreAnalyze();

        //calculate home Team win chances
        double winsAfterScoringFirstAtHome = homeTeamStrategy.getWinsAfterScoringFirstAtHome();
        double losesAfterConcedingFirstAway = awayTeamStrategy.getLosesAfterConcedingFirstAway();
        double homeTeamWin = sumUpOdds(winsAfterScoringFirstAtHome, losesAfterConcedingFirstAway);
        nextScoreAnalyze.setHomeTeamWinOdd(homeTeamWin);

        //calculate away team win chances
        double losesAfterScoringFirstAtHome = homeTeamStrategy.getLosesAfterScoringFirstAtHome();
        double winsAfterConcedingFirstAway = awayTeamStrategy.getWinsAfterConcedingFirstAway();
        double awayTeamWin = sumUpOdds(losesAfterScoringFirstAtHome, winsAfterConcedingFirstAway);
        nextScoreAnalyze.setAwayTeamWinOdd(awayTeamWin);

        //draw chances
        double drawsAfterScoringFirstAtHome = homeTeamStrategy.getDrawsAfterScoringFirstAtHome();
        double drawsAfterConcedingFirstAway = awayTeamStrategy.getDrawsAfterConcedingFirstAway();
        double drawChance = sumUpOdds(drawsAfterScoringFirstAtHome, drawsAfterConcedingFirstAway);
        nextScoreAnalyze.setDrawOdd(drawChance);

        //calculate 1-0 chances
        double defendsAfterScoringFirstPercentAtHome = homeTeamStrategy.getDefendsAfterScoringFirstPercentAtHome();
        double defendsAfterConcedingFirstPercentAway = awayTeamStrategy.getDefendsAfterConcedingFirstPercentAway();
        double nextScore1_0Odd = sumUpOdds(defendsAfterScoringFirstPercentAtHome, defendsAfterConcedingFirstPercentAway);
        nextScoreAnalyze.addScore(new Score<>(1, 0), nextScore1_0Odd);

        //calculate 2-0 chances
        double doublesAfterScoringPercentAtHome = homeTeamStrategy.getDoublesAfterScoringPercentAtHome();
        double concedesAfterConcedingPercentAway = awayTeamStrategy.getConcedesAfterConcedingPercentAway();
        double nextScore2_0Odd = sumUpOdds(doublesAfterScoringPercentAtHome, concedesAfterConcedingPercentAway);
        nextScoreAnalyze.addScore(new Score<>(2, 0), nextScore2_0Odd);

        //calculate 1-1 chances
        double concedeAfterScoringPercentAtHome = homeTeamStrategy.getConcedeAfterScoringPercentAtHome();
        double scoresAfterConcedingPercentAway = awayTeamStrategy.getScoresAfterConcedingPercentAway();
        double nextScore1_1Odd = sumUpOdds(concedeAfterScoringPercentAtHome, scoresAfterConcedingPercentAway);
        nextScoreAnalyze.addScore(new Score<>(1, 1), nextScore1_1Odd);

        return nextScoreAnalyze;
    }


    private NextScoreAnalyze analyze0_1(TeamStrategy homeTeamStrategy, TeamStrategy awayTeamStrategy) {
        NextScoreAnalyze nextScoreAnalyze = new NextScoreAnalyze();

        //calculate away Team win chances
        double losesAfterConcedingFirstAtHome = homeTeamStrategy.getLosesAfterConcedingFirstAtHome();
        double winsAfterScoringFirstAway = awayTeamStrategy.getWinsAfterScoringFirstAway();
        double awayTeamWin = sumUpOdds(winsAfterScoringFirstAway, losesAfterConcedingFirstAtHome);
        nextScoreAnalyze.setHomeTeamWinOdd(awayTeamWin);

        //calculate home team win chances
        double winsAfterConcedingFirstAtHome = homeTeamStrategy.getWinsAfterConcedingFirstAtHome();
        double losesAfterScoringFirstAway = awayTeamStrategy.getLosesAfterScoringFirstAway();
        double homeTeamWin = sumUpOdds(winsAfterConcedingFirstAtHome, losesAfterScoringFirstAway);
        nextScoreAnalyze.setHomeTeamWinOdd(homeTeamWin);

        //draw chances
        double drawsAfterConcedingFirstAtHome = homeTeamStrategy.getDrawsAfterConcedingFirstAtHome();
        double drawsAfterScoringFirstAway = awayTeamStrategy.getDrawsAfterScoringFirstAway();
        double drawChance = sumUpOdds(drawsAfterScoringFirstAway, drawsAfterConcedingFirstAtHome);
        nextScoreAnalyze.setDrawOdd(drawChance);

        //calculate 0-1 chances
        double defendsAfterConcedingFirstPercentAtHome = homeTeamStrategy.getDefendsAfterConcedingFirstPercentAtHome();
        double defendsAfterScoringFirstPercentAway = awayTeamStrategy.getDefendsAfterScoringFirstPercentAway();
        double nextScore0_1Odd = sumUpOdds(defendsAfterScoringFirstPercentAway, defendsAfterConcedingFirstPercentAtHome);
        nextScoreAnalyze.addScore(new Score<>(0, 1), nextScore0_1Odd);

        //calculate 0-2 chances
        double concedesAfterConcedingPercentAtHome = homeTeamStrategy.getConcedesAfterConcedingPercentAtHome();
        double doublesAfterScoringPercentAway = awayTeamStrategy.getDoublesAfterScoringPercentAway();
        double nextScore0_2Odd = sumUpOdds(concedesAfterConcedingPercentAtHome, doublesAfterScoringPercentAway);
        nextScoreAnalyze.addScore(new Score<>(2, 0), nextScore0_2Odd);

        //calculate 1-1 chances
        double scoresAfterConcedingPercentAtHome = homeTeamStrategy.getScoresAfterConcedingPercentAtHome();
        double concedeAfterScoringPercentAway = awayTeamStrategy.getConcedeAfterScoringPercentAway();
        double nextScore1_1Odd = sumUpOdds(scoresAfterConcedingPercentAtHome, concedeAfterScoringPercentAway);
        nextScoreAnalyze.addScore(new Score<>(1, 1), nextScore1_1Odd);

        return nextScoreAnalyze;
    }

    private TimeRange findAvgRange(TimeRange<Integer> timeRange1, TimeRange<Integer> timeRange2) {
        TimeRange<Integer> scoreMinuteRange = null;
        if(timeRange1 != null && timeRange2 != null) {
            Integer earliestMinuteHomeTeamScore = null;
            Integer avgMinuteHomeTeamScore = null;
            if(timeRange1.getFrom() != null && timeRange2.getFrom() != null) {
                earliestMinuteHomeTeamScore = Math.min(timeRange1.getFrom(), timeRange2.getFrom());
            }
            if(timeRange1.getTo() != null && timeRange2.getTo() != null) {
                avgMinuteHomeTeamScore = (timeRange1.getTo() + timeRange2.getTo()) / 2;
            }
            scoreMinuteRange = TimeRange.of(earliestMinuteHomeTeamScore, avgMinuteHomeTeamScore);
        }

        return scoreMinuteRange;
    }
}
