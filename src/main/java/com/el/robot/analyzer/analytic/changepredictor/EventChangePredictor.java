package com.el.robot.analyzer.analytic.changepredictor;

import com.el.betting.sdk.v2.Event;import com.el.betting.sdk.v4.*;
import com.el.betting.sdk.v2.Score;
import com.el.robot.analyzer.model.EventChange;

/**
 * Predicts next change happening during game.
 * It will return next change along with the probability of the change.
 * Change means scored goal, card, corner or any other event happening
 * during game.
 */
public interface EventChangePredictor<T extends EventChange> {

    T predictNextChange(Score<Integer> currentScore, Event<? extends Participant> event);

}
