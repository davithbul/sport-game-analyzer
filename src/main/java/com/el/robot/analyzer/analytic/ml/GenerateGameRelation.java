package com.el.robot.analyzer.analytic.ml;

import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import com.el.robot.analyzer.analytic.ml.model.GameRelation;
import com.el.robot.crawler.db.v3.EventStatisticManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Generates data for weka classification.
 */
@Profile("reporting")
@Component
public class GenerateGameRelation {

    @Autowired
    private EventStatisticProvider eventStatisticProvider;

    @Autowired
    private EventStatisticManager eventStatisticManager;

    public Collection<GameRelation> generateLeaguePatterns(String category, String league) {
        ConcurrentHashMap.KeySetView<GameRelation, Boolean> gameRelationList = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap.KeySetView<String, Boolean> checkedEvents = ConcurrentHashMap.newKeySet();

        eventStatisticManager.getDistinctTeams(category, league).parallelStream().forEach(teamName -> {
            List<EventStatistic> homeTeamEventStats = eventStatisticProvider.getTeamStatistics(category, league, teamName, Team.Side.HOME, true, -1, true, null);

            //generate game relation for home team
            if (homeTeamEventStats.size() >= 11) {
                IntStream.range(0, homeTeamEventStats.size() - 10).forEach(index -> {
                    EventStatistic baseGame = homeTeamEventStats.get(index);
                    boolean newElement = checkedEvents.add(baseGame.getDescription());

                    //filter events which were already examined
                    if(!newElement) {
                        return;
                    }

                    String baseGameAwayTeam = baseGame.getAwayTeam();
                    List<EventStatistic> baseGameAwayTeam10G = eventStatisticProvider.getTeamStatistics(category, league, baseGameAwayTeam, Team.Side.AWAY, true, 10, true, TimeRange.before(baseGame.getStartTime().minusDays(1)));
                    if (baseGameAwayTeam10G.size() >= 10) {
                        List<EventStatistic> homeTeamLast10G = homeTeamEventStats.subList(index + 1, index + 11);
                        GameRelation gameRelation = measureGameRelation(baseGame, homeTeamLast10G, baseGameAwayTeam10G);
                        gameRelationList.add(gameRelation);
                    }
                });
            }
        });

        return gameRelationList;
    }


    /**
     * Generates game relations for previous 10 games for both home team and away team
     *
     * @return the list of games relations - ideally should be 18
     */
    public List<GameRelation> generateGameRelations(String category, String league, String homeTeam, String awayTeam) {
        List<EventStatistic> homeTeamEventStatsLast20G = eventStatisticProvider.getTeamStatistics(category, league, homeTeam, Team.Side.HOME, true, -1, true, null);

        List<GameRelation> gameRelationList = new ArrayList<>();

        //generate game relation for home team
        if (homeTeamEventStatsLast20G.size() >= 11) {
            IntStream.range(0, homeTeamEventStatsLast20G.size() - 10).forEach(index -> {
                EventStatistic baseGame = homeTeamEventStatsLast20G.get(index);
                String baseGameAwayTeam = baseGame.getAwayTeam();
                List<EventStatistic> baseGameAwayTeam10G = eventStatisticProvider.getTeamStatistics(category, league, baseGameAwayTeam, Team.Side.AWAY, true, 10, true, TimeRange.before(baseGame.getStartTime().minusDays(1)));
                if (baseGameAwayTeam10G.size() >= 10) {
                    List<EventStatistic> homeTeamLast10G = homeTeamEventStatsLast20G.subList(index + 1, index + 11);
                    GameRelation gameRelation = measureGameRelation(baseGame, homeTeamLast10G, baseGameAwayTeam10G);
                    gameRelationList.add(gameRelation);
                }
            });
        }

        //measure team relations for away team
        /*List<EventStatistic> awayTeamEventStatsLast20G = eventStatisticProvider.getTeamStatistics(category, league, awayTeam, Team.Side.AWAY, true, -1, true, null);
        if(awayTeamEventStatsLast20G.size() >= 11) {
            IntStream.range(0, awayTeamEventStatsLast20G.size() - 10).forEach(index -> {
                EventStatistic baseGame = awayTeamEventStatsLast20G.get(index);
                String baseGameHomeTeam = baseGame.getHomeTeam();
                List<EventStatistic> baseGameHomeTeam10G = eventStatisticProvider.getTeamStatistics(category, league, baseGameHomeTeam, Team.Side.HOME, true, 10, true, TimeRange.before(baseGame.getStartTime().minusDays(1)));
                if (baseGameHomeTeam10G.size() >= 10) {
                    List<EventStatistic> awayTeamLast10G = awayTeamEventStatsLast20G.subList(index + 1, index + 11);
                    GameRelation gameRelation = measureGameRelation(baseGame, baseGameHomeTeam10G, awayTeamLast10G);
                    gameRelationList.add(gameRelation);
                }
            });
        }*/
        return gameRelationList;
    }

    /**
     * @param baseGame      Base game represents the game where home and away team played with each other.
     * @param homeTeamStats represents 10 previous games before meeting with away team
     * @param awayTeamStats represents 10 previous games before playing against home team
     * @return statistics of 2 teams before matchign with each other
     */
    private GameRelation measureGameRelation(EventStatistic baseGame, List<EventStatistic> homeTeamStats, List<EventStatistic> awayTeamStats) {
        GameRelation gameRelation = new GameRelation();
        gameRelation.setStartTime(baseGame.getStartTime());
        gameRelation.setHomeTeamGoalCount(baseGame.getHomeTeamGoalCount());
        gameRelation.setAwayTeamGoalCount(baseGame.getAwayTeamGoalCount());

        //calculate stats for home team
        List<EventStatistic> homeTeamPrevious10G = homeTeamStats.subList(5, 10);
        List<EventStatistic> homeTeamPrevious5G = homeTeamStats.subList(1, 5);
        int homeTeamScoredGoalCount10G = homeTeamPrevious10G.stream()
                .mapToInt(EventStatistic::getHomeTeamGoalCount).sum();
        int homeTeamConcedeGoalCount10G = homeTeamPrevious10G.stream()
                .mapToInt(EventStatistic::getAwayTeamGoalCount).sum();

        int homeTeamScoredGoalCount5G = homeTeamPrevious5G.stream()
                .mapToInt(EventStatistic::getHomeTeamGoalCount).sum();
        int homeTeamConcedeGoalCount5G = homeTeamPrevious5G.stream()
                .mapToInt(EventStatistic::getAwayTeamGoalCount).sum();

        int homeTeamScoreGoalCountLastG = homeTeamStats.get(0).getHomeTeamGoalCount();
        int homeTeamConcedeGoalCountLastG = homeTeamStats.get(0).getAwayTeamGoalCount();

        gameRelation.setHomeTeamScoreLast10G(homeTeamScoredGoalCount10G);
        gameRelation.setHomeTeamConcedeLast10G(homeTeamConcedeGoalCount10G);
        gameRelation.setHomeTeamScoreLast5G(homeTeamScoredGoalCount5G);
        gameRelation.setHomeTeamConcedeLast5G(homeTeamConcedeGoalCount5G);
        gameRelation.setHomeTeamScoreLastG(homeTeamScoreGoalCountLastG);
        gameRelation.setHomeTeamConcedeLastG(homeTeamConcedeGoalCountLastG);


        //calculate stats for away team
        List<EventStatistic> awayTeamPrevious10G = awayTeamStats.subList(5, 10);
        List<EventStatistic> awayTeamPrevious5G = awayTeamStats.subList(1, 5);
        int awayTeamScoredGoalCount10G = awayTeamPrevious10G.stream()
                .mapToInt(EventStatistic::getAwayTeamGoalCount).sum();
        int awayTeamConcedeGoalCount10G = awayTeamPrevious10G.stream()
                .mapToInt(EventStatistic::getHomeTeamGoalCount).sum();

        int awayTeamScoredGoalCount5G = awayTeamPrevious5G.stream()
                .mapToInt(EventStatistic::getAwayTeamGoalCount).sum();
        int awayTeamConcedeGoalCount5G = awayTeamPrevious5G.stream()
                .mapToInt(EventStatistic::getHomeTeamGoalCount).sum();

        int awayTeamScoreGoalCountLastG = awayTeamStats.get(0).getAwayTeamGoalCount();
        int awayTeamConcedeGoalCountLastG = awayTeamStats.get(0).getHomeTeamGoalCount();

        gameRelation.setAwayTeamScoreLast10G(awayTeamScoredGoalCount10G);
        gameRelation.setAwayTeamConcedeLast10G(awayTeamConcedeGoalCount10G);
        gameRelation.setAwayTeamScoreLast5G(awayTeamScoredGoalCount5G);
        gameRelation.setAwayTeamConcedeLast5G(awayTeamConcedeGoalCount5G);
        gameRelation.setAwayTeamScoreLastG(awayTeamScoreGoalCountLastG);
        gameRelation.setAwayTeamConcedeLastG(awayTeamConcedeGoalCountLastG);

        return gameRelation;
    }
}