package com.el.robot.analyzer.analytic.manager;

import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.robot.crawler.common.StatisticQueryBuilder;
import com.el.robot.crawler.db.v3.EventStatisticManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventStatisticProvider {

    @Qualifier("eventStatisticManager")
    @Autowired
    private EventStatisticManager eventStatisticManager;

    public List<EventStatistic> getTeamStatistics(@Nullable String category, @Nullable String league, String teamName, @Nullable Team.Side side, boolean sort) {
        return getTeamStatistics(category, league, teamName, side, sort, true);
    }

    public List<EventStatistic> getTeamStatistics(@Nullable String category, @Nullable String league, String teamName, @Nullable Team.Side side, boolean sort, boolean excludeGoalsStats) {
        return getTeamStatistics(category, league, teamName, side, sort, -1, excludeGoalsStats, null);
    }

    public List<EventStatistic> getTeamStatistics(@Nullable String category, @Nullable String league, String teamName, @Nullable Team.Side side, boolean sort, int limit, boolean excludeGoalsStats, TimeRange<LocalDateTime> gameStartTimeRange) {
        final Query query = StatisticQueryBuilder.create()
                .setCategory(category)
                .setLeague(league)
                .setTeamName(teamName)
                .setSide(side)
                .setSort(sort)
                .setLimit(limit)
                .setExcludeGoalStatistics(excludeGoalsStats)
                .setGameStartTimeRange(gameStartTimeRange)
                .buildTeamStatsQuery();
        return eventStatisticManager.find(query);
    }

    public List<EventStatistic> getStatistics(String homeTeam, String awayTeam, boolean ignoreSides, boolean sort) {
        return getStatistics(null, null, homeTeam, awayTeam, ignoreSides, sort);
    }

    public List<EventStatistic> getStatistics(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, boolean ignoreSides, boolean sort) {
        return getStatistics(category, league, homeTeam, awayTeam, ignoreSides, sort, true, null);
    }

    public List<EventStatistic> getStatistics(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, boolean ignoreSides, boolean sort, boolean excludeGoalStatistics, TimeRange<LocalDateTime> gameStartTimeRange) {
        return getStatistics(category, league, homeTeam, awayTeam, ignoreSides, sort, -1, excludeGoalStatistics, gameStartTimeRange);
    }

    public List<EventStatistic> getStatistics(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, boolean ignoreSides, boolean sort, int limit, boolean excludeGoalStatistics) {
        return getStatistics(category, league, homeTeam, awayTeam, ignoreSides, sort, limit, excludeGoalStatistics, null);
    }

    public List<EventStatistic> getStatistics(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, boolean ignoreSides, boolean sort, int limit, boolean excludeGoalStatistics, TimeRange<LocalDateTime> gameStartTimeRange) {
        final Query query = StatisticQueryBuilder.create()
                .setCategory(category)
                .setLeague(league)
                .setHomeTeam(homeTeam)
                .setAwayTeam(awayTeam)
                .setIgnoreSides(ignoreSides)
                .setSort(sort)
                .setLimit(limit)
                .setExcludeGoalStatistics(excludeGoalStatistics)
                .setGameStartTimeRange(gameStartTimeRange)
                .buildGameStatsQuery();
        return eventStatisticManager.find(query);
    }

    public List<EventStatistic> getLeagueStats(@Nullable String category, String league, int gameCount, TimeRange<LocalDateTime> gameStartTimeRange) {
        Query query = new Query();
        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category));
        }
        query.addCriteria(Criteria.where("league").is(league));
        query.with(new Sort(Sort.Direction.DESC, "startTime"));
        query.limit(gameCount);
        query.fields().exclude("goalStatistics");

        if (gameStartTimeRange != null) {
            Criteria startTimeCriteria = Criteria.where("startTime");
            if (gameStartTimeRange.getFrom() != null) {
                startTimeCriteria.gte(gameStartTimeRange.getFrom());
            }

            if (gameStartTimeRange.getTo() != null) {
                startTimeCriteria.lte(gameStartTimeRange.getTo());
            }
            query.addCriteria(startTimeCriteria);
        }

        return eventStatisticManager.find(query);
    }
}
