package com.el.robot.analyzer.analytic.measure;

import com.el.betting.common.MathUtils;
import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;

import static com.el.betting.common.ArrayUtils.copyWithExclusion;

/**
 * Estimates probability based on median distance from the mean.
 * If median > mean, it means mass data are concentrated on right side of median,
 * otherwise left side. If equal, than the data is normal distributed and graph has no
 * tendency.
 *
 * NOTE:
 * If mean is bigger than median, it means the distribution is heavily skewed right (right numbers are far right from median)
 * than otherwise. Which itself means that limiting right border would be very hard, since it might be
 * very far.
 * If mean > median, and as big is difference as far right border is.
 * If mean and median are close than distribution is close to normal, but we can't conclude how spread are
 * numbers from that.
 * If mean < median, and number skewed right, it means the right border is not very far and not very unexpected.
 */
public class MedianEstimator {

    /**
     *
     * This method returns the wideness of spread, if distribution skewed right and mean > median, it means
     * the right border is far right, as big is the difference (mean - median) as unexpected are the right border.
     * Basically this method measures the unexpectability of borders.
     * If mean is close to median, it means distribution is normal, but it doesn't say anything about borders, it
     * just means that right and left borders are pretty equal far from the median / mean.
     *
     * Returns how many times the goal count over / under the mean is higher than the opposite side of mean
     * The wideness only makes sense only along with skewness, if numbers skewed right and this method returns
     * big negative number, it means right border is unexpectedly far.
     * Return double value between [-1; +1]
     */
    public static double measureBorderWideness(GoalsDescriptiveStats goalsDescriptiveStats) {
        if(goalsDescriptiveStats.getMean() > goalsDescriptiveStats.getMedian()) {//has more data on the left side of mean
            //the sign will be minus (-) because under the mean there are more data than over the mean
            return (goalsDescriptiveStats.getMedian() - goalsDescriptiveStats.getMean()) / goalsDescriptiveStats.getMean();
        } else { //has more data on the right side of mean
            return (goalsDescriptiveStats.getMedian() - goalsDescriptiveStats.getMean()) / goalsDescriptiveStats.getMean();
        }
    }

/*    public static double measureTendeny(GoalsDescriptiveStats goalsDescriptiveStats) {
        int[] values = MathUtils.isInteger(goalsDescriptiveStats.getMedian()) ?
                copyWithExclusion(goalsDescriptiveStats.getValues(), ((Double)goalsDescriptiveStats.getMedian()).intValue()) :
                goalsDescriptiveStats.getValues();


        if(goalsDescriptiveStats.getMean() > goalsDescriptiveStats.getMedian()) {//has more data on the left side of mean
            //the sign will be minus (-) because under the mean there are more data than over the mean
            return (goalsDescriptiveStats.getMedian() - goalsDescriptiveStats.getMean()) / goalsDescriptiveStats.getMean();
        } else { //has more data on the right side of mean
            return (goalsDescriptiveStats.getMedian() - goalsDescriptiveStats.getMean()) / goalsDescriptiveStats.getMean();
        }
    }*/
}
