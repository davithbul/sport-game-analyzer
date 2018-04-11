package com.el.robot.analyzer.analytic.changepredictor;

import com.el.betting.common.CollectionUtil;
import com.el.betting.common.TeamUtils;
import com.el.betting.sdk.v2.Event;import com.el.betting.sdk.v4.*;
import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.Team;
import com.el.robot.analyzer.analytic.changepredictor.model.ScoreChangeJudgement;
import com.el.robot.analyzer.model.ScoreChange;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.el.betting.sdk.v3.common.OddsUtils.sumUpOdds;

/**
 * Predicts next score change.
 */
@Service
public class ScoreChangePredictor implements EventChangePredictor<ScoreChange> {

    private ScoreChangeExaminer scoreChangeExaminer;

    @Inject
    public ScoreChangePredictor(ScoreChangeExaminer scoreChangeExaminer) {
        this.scoreChangeExaminer = scoreChangeExaminer;
    }

    /**
     * It uses {@link ScoreChangeExaminer} for a judgement of possible
     * available next scores, for both home team and away team.
     * After having estimation about possible next score it does
     * sum up of 2 opinions and returns the one which has higher
     * probability.
     */
    @Override
    public ScoreChange predictNextChange(Score<Integer> currentScore, Event<? extends Participant> event) {
        String category = (String) event.getProperty("category");
        String league = (String) event.getProperty("league");
        List<Team> teams = ((Event<Team>)event).getParticipants();
        Team homeTeam = TeamUtils.getHomeTeam(teams);
        Team awayTeam = TeamUtils.getAwayTeam(teams);

        //and get the most frequently repetitive scenario if the score is the same as current
        ScoreChangeJudgement scoreJudgementHomeTeam = scoreChangeExaminer.estimateNextScore(currentScore, category, league, homeTeam.getName(), Team.Side.HOME);
        ScoreChangeJudgement scoreJudgementAwayTeam = scoreChangeExaminer.estimateNextScore(currentScore, category, league, awayTeam.getName(), Team.Side.AWAY);

        //if any of judgements miss the data, than return nothing
        if (scoreJudgementAwayTeam.getConfidence() < 50 && scoreJudgementAwayTeam.getNextExpectedScores().isEmpty() ||
                scoreJudgementHomeTeam.getConfidence() < 50 && scoreJudgementHomeTeam.getNextExpectedScores().isEmpty()) {
            return new ScoreChange(currentScore, null, 999);
        }

        //now since we have judgements for both score changes, we can find the most possible score change
        ScoreChangeJudgement scoreChangeJudgement = sumUpScoreChances(scoreJudgementHomeTeam, scoreJudgementAwayTeam);

        Map<Score<Integer>, Double> nextExpectedScores = scoreChangeJudgement.getNextExpectedScores();
        nextExpectedScores = CollectionUtil.sortByValue(nextExpectedScores, false);

        Map.Entry<Score<Integer>, Double> highestPossibleScore = nextExpectedScores.entrySet().iterator().next();

        if (highestPossibleScore.getValue() >= scoreChangeJudgement.getScoreWillRemainOdd()) {
            return new ScoreChange(currentScore, highestPossibleScore.getKey(), highestPossibleScore.getValue());
        } else {
            return new ScoreChange(currentScore, currentScore, scoreChangeJudgement.getScoreWillRemainOdd());
        }
    }


    private ScoreChangeJudgement sumUpScoreChances(ScoreChangeJudgement homeTeam, ScoreChangeJudgement awayTeam) {
        ScoreChangeJudgement scoreChangeJudgement = new ScoreChangeJudgement();
        scoreChangeJudgement.setScoreWillBeChangedOdd(sumUpOdds(homeTeam.getScoreWillBeChangedOdd(), awayTeam.getScoreWillBeChangedOdd()));
        scoreChangeJudgement.setScoreWillRemainOdd(sumUpOdds(homeTeam.getScoreWillRemainOdd(), awayTeam.getScoreWillRemainOdd()));
        scoreChangeJudgement.setConfidence(sumUpOdds(homeTeam.getConfidence(), awayTeam.getConfidence()));

        Map<Score<Integer>, Double> awayTeamNextExpectedScores = awayTeam.getNextExpectedScores();

        //now find the possibility of next scores
        for (Map.Entry<Score<Integer>, Double> scoreDoubleEntry : homeTeam.getNextExpectedScores().entrySet()) {
            boolean found = false;
            Iterator<Map.Entry<Score<Integer>, Double>> iterator = awayTeamNextExpectedScores.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Score<Integer>, Double> awayTeamScoreEntry = iterator.next();
                if (scoreDoubleEntry.getKey().equals(awayTeamScoreEntry.getKey())) {
                    found = true;
                    iterator.remove();
                    scoreChangeJudgement.addNextExpectedScore(scoreDoubleEntry.getKey(),
                            sumUpOdds(scoreDoubleEntry.getValue(), awayTeamScoreEntry.getValue()));
                    break;
                }
            }

            if (!found) {
                scoreChangeJudgement.addNextExpectedScore(scoreDoubleEntry.getKey(), sumUpOdds(2, scoreDoubleEntry.getValue()));
            }
        }

        awayTeamNextExpectedScores.entrySet().forEach(entry -> scoreChangeJudgement.addNextExpectedScore(entry.getKey(), sumUpOdds(2, entry.getValue())));

        return scoreChangeJudgement;
    }
}
