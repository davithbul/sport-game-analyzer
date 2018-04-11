package com.el.robot.analyzer.analytic.changepredictor.impl;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.betting.sdk.v3.statistic.GoalStatistic;
import com.el.robot.analyzer.analytic.changepredictor.ScoreChangeExaminer;
import com.el.robot.analyzer.analytic.changepredictor.model.ScoreChangeJudgement;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Estimates next possible scores based on how many time score has changed after
 * being on current score
 */
@Component
public class HistoricalScoreChangeExaminer implements ScoreChangeExaminer {


    private EventStatisticProvider eventStatisticProvider;

    @Inject
    public HistoricalScoreChangeExaminer(EventStatisticProvider eventStatisticProvider) {
        this.eventStatisticProvider = eventStatisticProvider;
    }

    @Override
    public ScoreChangeJudgement estimateNextScore(Score<Integer> currentScore, String category, String league, String team, Team.Side side) {
        List<EventStatistic> eventStatisticList = eventStatisticProvider.getTeamStatistics(category, league, team, side, true, 20, false, null);
        eventStatisticList = eventStatisticList.stream()
                .filter(stats -> stats.getGoalStatistics() != null &&
                        (!stats.getGoalStatistics().isEmpty() || stats.getGoalCount() == 0))
                .collect(Collectors.toList());

        if (eventStatisticList.size() < 5) { //not enough statistics
            return new ScoreChangeJudgement();
        }

        int scoreWillRemain = 0;
        int scoreWillBeChanged = 0;
        int scoreAlreadyChanged = 0;
        List<GoalStatistic> nextPossibleScores = new ArrayList<>();

        for (EventStatistic eventStatistic : eventStatisticList) {
            if (eventStatistic.getGoalCount() == 0) {
                if (currentScore.getAwaySideScore() + currentScore.getHomeSideScore() == 0) {
                    //good it means current score will remain
                    scoreWillRemain++;
                } else {
                    //it means the score already edged from expected behaviour
                    scoreAlreadyChanged++;
                }
                continue;
            }

            int teamGoalNumber = (side == Team.Side.HOME) ? currentScore.getHomeSideScore() : currentScore.getAwaySideScore();

            OptionalInt matchingIndex = currentScore.getHomeSideScore() + currentScore.getAwaySideScore() == 0 ?
                    OptionalInt.of(-1) :
                    IntStream.range(0, eventStatistic.getGoalStatistics().size())
                            .filter(index -> {
                                GoalStatistic goalStatistic = eventStatistic.getGoalStatistics().get(index);
                                return goalStatistic.getGoalNumber() == currentScore.getHomeSideScore() + currentScore.getAwaySideScore() &&
                                        (team.equals(goalStatistic.getTeamName()) ? goalStatistic.getTeamGoalNumber() == teamGoalNumber
                                                : (goalStatistic.getGoalNumber() - goalStatistic.getTeamGoalNumber()) == teamGoalNumber);
                            }).findFirst();

            if (!matchingIndex.isPresent()) {
                scoreAlreadyChanged++;
            } else {
                int index = matchingIndex.getAsInt();
                if (index == eventStatistic.getGoalStatistics().size() - 1) {
                    scoreWillRemain++;
                } else {
                    scoreWillBeChanged++;
                    nextPossibleScores.add(eventStatistic.getGoalStatistics().get(index + 1));
                }
            }
        }

        Map<String, List<Score<Integer>>> scoreGrouping = nextPossibleScores.stream()
                .map(goalStatistic -> convertGoalStatToScore(goalStatistic, team, side))
                .collect(Collectors.groupingBy(score -> score.getHomeSideScore() + " _ " + score.getAwaySideScore()));

        int outcomeCount = scoreWillRemain + scoreWillBeChanged;

        ScoreChangeJudgement scoreJudgement = new ScoreChangeJudgement();
        scoreJudgement.setScoreWillBeChangedOdd((double) scoreWillBeChanged / outcomeCount);
        scoreJudgement.setScoreWillRemainOdd((double) scoreWillRemain / outcomeCount);


        for (List<Score<Integer>> scores : scoreGrouping.values()) {
            Score<Integer> score = scores.get(0);
            scoreJudgement.addNextExpectedScore(score, (double) scores.size() / outcomeCount);
        }

        scoreJudgement.setConfidence((double) outcomeCount / eventStatisticList.size());

        return scoreJudgement;
    }

    private Score<Integer> convertGoalStatToScore(GoalStatistic goalStatistic, String team, Team.Side side) {
        Score<Integer> score;
        if (team.equals(goalStatistic.getTeamName()) && side == Team.Side.HOME ||
                !team.equals(goalStatistic.getTeamName()) && side == Team.Side.AWAY) { //home team scored
            score = new Score<>(goalStatistic.getTeamGoalNumber(),
                    goalStatistic.getGoalNumber() - goalStatistic.getTeamGoalNumber());
        } else { //away
            score = new Score<>(goalStatistic.getGoalNumber() - goalStatistic.getTeamGoalNumber(),
                    goalStatistic.getTeamGoalNumber());
        }

        return score;
    }
}
