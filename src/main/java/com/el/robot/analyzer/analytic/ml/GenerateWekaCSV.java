package com.el.robot.analyzer.analytic.ml;

import com.el.robot.analyzer.analytic.ml.model.GameRelation;
import com.el.robot.analyzer.analytic.ml.model.ScoreRelation;
import com.el.robot.analyzer.common.CsvFileWriter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;

public class GenerateWekaCSV {

    public static void generateCsv(Collection<GameRelation> gameRelationList, String fileName) throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(fileName);
        gameRelationList.stream().forEach(gameRelation -> {
            try {
                csvFileWriter.writeLine(
                        gameRelation.getStartTime(),
                        gameRelation.getHomeTeamScoreLast10G(),
                        gameRelation.getHomeTeamConcedeLast10G(),
                        gameRelation.getAwayTeamScoreLast10G(),
                        gameRelation.getAwayTeamConcedeLast10G(),
                        gameRelation.getHomeTeamScoreLast5G(),
                        gameRelation.getHomeTeamConcedeLast5G(),
                        gameRelation.getAwayTeamScoreLast5G(),
                        gameRelation.getAwayTeamConcedeLast5G(),
                        gameRelation.getHomeTeamScoreLastG(),
                        gameRelation.getHomeTeamConcedeLastG(),
                        gameRelation.getAwayTeamScoreLastG(),
                        gameRelation.getAwayTeamConcedeLastG(),
                        gameRelation.getHomeTeamGoalCount() + gameRelation.getAwayTeamGoalCount() > 2
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void generateScoreRelationCsv(Collection<ScoreRelation> scoreRelationList, String fileName) throws IOException {
        generateScoreRelationCsv(scoreRelationList, null, fileName);
    }

    public static void generateScoreRelationCsv(Collection<ScoreRelation> scoreRelationList, @Nullable String header, String fileName) throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(fileName);
        if(StringUtils.isNoneBlank(header)) {
            csvFileWriter.writeLine(header);
        }

        scoreRelationList.stream().forEach(scoreRelation -> {
            try {
                Object[] objects = new Object[scoreRelation.getLastScores().size() + 5];
                objects[0] = scoreRelation.getStartTime();
                Object[] scoresArray = scoreRelation.getLastScores().toArray(new Object[scoreRelation.getLastScores().size()]);
                System.arraycopy(scoresArray, 0, objects, 1, scoresArray.length);
//                objects[objects.length - 1] = scoreRelation.getHomeTeamGoalCount() + scoreRelation.getAwayTeamGoalCount() > 2;
                int index = scoreRelation.getLastScores().size() + 1;
                objects[index] = scoreRelation.getHomeTeamGoalCount() > 0;
                objects[index + 1] = scoreRelation.getHomeTeamGoalCount() > 1;
                objects[index + 2] = scoreRelation.getHomeTeamGoalCount() > 2;
                objects[index + 3] = scoreRelation.getHomeTeamGoalCount() > 3;
                csvFileWriter.writeLine(
                        objects
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
