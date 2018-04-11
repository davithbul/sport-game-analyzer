package com.el.robot.analyzer.analytic.ml;

import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import com.el.robot.analyzer.analytic.ml.model.ScoreRelation;
import com.el.robot.crawler.db.v3.EventStatisticManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Profile("reporting")
@Component
public class ScoreRelationGenerator {

    @Autowired
    private EventStatisticProvider eventStatisticProvider;

    @Autowired
    private EventStatisticManager eventStatisticManager;

    public Collection<ScoreRelation> generateScoreRelations(String category, String league, int gameCount) {
        List<String> distinctTeams = eventStatisticManager.getDistinctTeams(category, league);
        return generateScoreRelations(category, league, distinctTeams, Collections.emptyList(), gameCount);
    }

    public Collection<ScoreRelation> generateHomeTeamScoreRelations(String category, String league, int gameCount, String homeTeam, String... homeTeams) {
        ArrayList<String> homeTeamList = new ArrayList<>();
        homeTeamList.add(homeTeam);
        Arrays.stream(homeTeams).forEach(homeTeamList::add);
        return generateScoreRelations(category, league, homeTeamList, Collections.emptyList(), gameCount);
    }

    public Collection<ScoreRelation> generateAwayTeamScoreRelations(String category, String league, int gameCount, String awayTeam, String... awayTeams) {
        ArrayList<String> awayTeamList = new ArrayList<>();
        awayTeamList.add(awayTeam);
        Arrays.stream(awayTeams).forEach(awayTeamList::add);
        return generateScoreRelations(category, league, Collections.emptyList(), awayTeamList, gameCount);
    }

    public Collection<ScoreRelation> generateScoreRelations(String category, String league, List<String> homeTeams, List<String> awayTeams, int gameCount) {
        ConcurrentHashMap.KeySetView<ScoreRelation, Boolean> scoreRelations = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap.KeySetView<String, Boolean> checkedEvents = ConcurrentHashMap.newKeySet();

        homeTeams.parallelStream().forEach(teamName -> {
            List<EventStatistic> homeTeamEventStats = eventStatisticProvider.getTeamStatistics(category, league, teamName, Team.Side.HOME, true, -1, true, null);

            //generate game relation for home team
            if (homeTeamEventStats.size() >= gameCount + 1) {
                IntStream.range(0, homeTeamEventStats.size() - gameCount).forEach(index -> {
                    EventStatistic baseGame = homeTeamEventStats.get(index);
                    boolean newElement = checkedEvents.add(baseGame.getDescription());

                    //filter events which were already examined
                    if (!newElement) {
                        return;
                    }

                    String awayTeam = baseGame.getAwayTeam();
                    List<EventStatistic> awayTeamEventStats = eventStatisticProvider.getTeamStatistics(category, league, awayTeam, Team.Side.AWAY, true, gameCount, true, TimeRange.before(baseGame.getStartTime().minusDays(1)));
                    if (awayTeamEventStats.size() >= gameCount) {
                        List<EventStatistic> homeTeamLastGames = homeTeamEventStats.subList(index + 1, index + gameCount + 1);
                        ScoreRelation scoreRelation = measureRecentScoreRelation(baseGame, homeTeamLastGames, awayTeamEventStats);
                        scoreRelations.add(scoreRelation);
                    }
                });
            }
        });

        //generate for away teams
        awayTeams.parallelStream().forEach(teamName -> {
            List<EventStatistic> awayTeamEventStats = eventStatisticProvider.getTeamStatistics(category, league, teamName, Team.Side.AWAY, true, -1, true, null);

            //generate game relation for home team
            if (awayTeamEventStats.size() >= gameCount + 1) {
                IntStream.range(0, awayTeamEventStats.size() - gameCount).forEach(index -> {
                    EventStatistic baseGame = awayTeamEventStats.get(index);
                    boolean newElement = checkedEvents.add(baseGame.getDescription());

                    //filter events which were already examined
                    if (!newElement) {
                        return;
                    }

                    String homeTeam = baseGame.getHomeTeam();
                    List<EventStatistic> homeTeamEventStats = eventStatisticProvider.getTeamStatistics(category, league, homeTeam, Team.Side.HOME, true, gameCount, true, TimeRange.before(baseGame.getStartTime().minusDays(1)));
                    if (homeTeamEventStats.size() >= gameCount) {
                        List<EventStatistic> awayTeamLastGames = awayTeamEventStats.subList(index + 1, index + gameCount + 1);
                        ScoreRelation scoreRelation = measureRecentScoreRelation(baseGame, homeTeamEventStats, awayTeamLastGames);
                        scoreRelations.add(scoreRelation);
                    }
                });
            }
        });

        return scoreRelations;
    }

    private ScoreRelation measureRecentScoreRelation(EventStatistic baseGame, List<EventStatistic> homeTeamRecentStats, List<EventStatistic> awayTeamRecentStats) {
        ScoreRelation scoreRelation = new ScoreRelation();
        scoreRelation.setStartTime(baseGame.getStartTime());
        scoreRelation.setHomeTeamGoalCount(baseGame.getHomeTeamGoalCount());
        scoreRelation.setAwayTeamGoalCount(baseGame.getAwayTeamGoalCount());
        homeTeamRecentStats.stream().forEach(eventStatistic -> {
            scoreRelation.addScore(eventStatistic.getHomeTeamGoalCount());
            scoreRelation.addScore(eventStatistic.getAwayTeamGoalCount());
        });

        awayTeamRecentStats.stream().forEach(eventStatistic -> {
            scoreRelation.addScore(eventStatistic.getAwayTeamGoalCount());
            scoreRelation.addScore(eventStatistic.getHomeTeamGoalCount());
        });

        return scoreRelation;
    }

    public String generateWekaHeader(int gameCount) {
        StringBuilder header = new StringBuilder();
        header.append("@relation game\n")
                .append("\n")
                .append("@attribute startTime date \"yyyy-MM-dd'T'HH:mm\"\n");

        for (int i = 0; i < gameCount; i++) {
            String number = (i == 0 ? "" : "" + (i + 1));
            header.append(String.format("@attribute homeTeamScoreRecent%sG numeric\n", number))
                    .append(String.format("@attribute homeTeamConcedeRecent%sG numeric\n", number));
        }

        for (int i = 0; i < gameCount; i++) {
            String number = (i == 0 ? "" : "" + (i + 1));
            header.append(String.format("@attribute awayTeamScoreRecent%sG numeric\n", number))
                    .append(String.format("@attribute awayTeamConcedeRecent%sG numeric\n", number));
        }

        return header.toString();
    }
}
