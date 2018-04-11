package com.el.robot.analyzer.mapreduce.util;

import com.el.betting.sdk.v2.BetOutcome;
import com.el.betting.sdk.v2.Team;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BetfairDataLine {
    private long sportId;
    private Long marketId;
    private String market;
    private LocalDateTime marketOpenTime;
    private LocalDateTime marketCloseTime;
    private String category;
    private String league;
    private String selection;
    private BigDecimal price;
    private long numberOfBets;
    private BigDecimal volumeMatched;
    private LocalDateTime lastTimeMatched;
    private LocalDateTime firstTimeMatched;
    private BetOutcome selectionOutcome;
    private long selectionId;
    private List<Team> teams;
    private String details;
    private LocalDateTime eventStartTime;
    private String description;

    public void setSportId(long sportId) {
        this.sportId = sportId;
    }

    public long getSportId() {
        return sportId;
    }

    public void setMarketName(String market) {
        this.market = market;
    }

    public String getMarketName() {
        return market;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public LocalDateTime getMarketCloseTime() {
        return marketCloseTime;
    }

    public void setMarketCloseTime(LocalDateTime marketCloseTime) {
        this.marketCloseTime = marketCloseTime;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getSelection() {
        return selection;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setNumberOfBets(long numberOfBets) {
        this.numberOfBets = numberOfBets;
    }

    public long getNumberOfBets() {
        return numberOfBets;
    }


    public void setVolumeMatched(BigDecimal volumeMatched) {
        this.volumeMatched = volumeMatched;
    }

    public BigDecimal getVolumeMatched() {
        return volumeMatched;
    }

    public void setLastTimeMatched(LocalDateTime lastTimeMatched) {
        this.lastTimeMatched = lastTimeMatched;
    }

    public LocalDateTime getLastTimeMatched() {
        return lastTimeMatched;
    }

    public void setFirstTimeMatched(LocalDateTime firstTimeMatched) {
        this.firstTimeMatched = firstTimeMatched;
    }

    public LocalDateTime getFirstTimeMatched() {
        return firstTimeMatched;
    }

    public BetOutcome getSelectionOutcome() {
        return selectionOutcome;
    }

    public void setSelectionOutcome(BetOutcome selectionOutcome) {
        this.selectionOutcome = selectionOutcome;
    }

    public void setSelectionId(long selectionId) {
        this.selectionId = selectionId;
    }

    public long getSelectionId() {
        return selectionId;
    }

    public LocalDateTime getMarketOpenTime() {
        return marketOpenTime;
    }

    public void setMarketOpenTime(LocalDateTime marketOpenTime) {
        this.marketOpenTime = marketOpenTime;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Team getHomeTeam() {
        return teams.stream()
                .filter(team -> team.getSide() == Team.Side.HOME)
                .findAny().get();
    }

    public Team getAwayTeam() {
        return teams.stream()
                .filter(team -> team.getSide() == Team.Side.AWAY)
                .findAny().get();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setEventStartTime(LocalDateTime eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public LocalDateTime getEventStartTime() {
        return eventStartTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
