/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llamadice.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ahill
 */
@Getter
@Setter
public class RollResult {

    private static final Logger log = LoggerFactory.getLogger(DiceRoller.class);

    private Map<Integer, Integer> counts = new TreeMap<>();
    private List<Integer> rolls = new ArrayList<>();
    private Set<Integer> distinct = new TreeSet<>();

    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    private int sum = 0;

    private int dieSides;

    public void updateResult(int roll) {
//        log.debug("new roll: " + roll);
//        log.debug("rolls: " + rolls);
//        log.debug("distinct: " + distinct);
        
        rolls.add(roll);
        distinct.add(roll);

        this.updateSum(roll);
        this.updateMinMax(roll);
        this.updateCounts(roll);
    }

    public void updateSum(int roll) {
        this.sum += roll;
//        log.debug("new sum: " + sum);
    }

    public void updateMinMax(int roll) {
        min = roll < min ? roll : min;
        max = roll > max ? roll : max;

//        log.debug("min/max: " + min + "/" + max);
    }

    public void updateCounts(int roll) {

        Integer count = counts.get(roll);

        if (count != null && count > 0) {
            counts.put(roll, count + 1);
        } else {
            counts.put(roll, 1);
        }

        log.debug("Counts: " + counts);
    }

    public String rollToText() {
        return "You rolled " + EnglishNumberToWords.convertLessThanOneThousand(rolls.size())
                + " " + dieSides + " sided die and got a " + sum + "!";
        
//        log.debug(text);
//        return text;
    }

    public String rollsToText() {
        StringBuilder text = new StringBuilder();
        List<Integer> nums = distinct.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());

        for (int i = 0; i < nums.size(); i++) {
            int roll = nums.get(i);
            int timesRolled = counts.get(roll);

            if (roll >= max) {
                text.append(" and ");
            }

            text.append(EnglishNumberToWords.convertLessThanOneThousand(timesRolled));
            text.append(" ");
            text.append(timesRolled > 1 ? roll + "s" : roll);

            if (roll < max) {
                text.append(", ");
            } else {
                text.append("!");
            }

        }

        return "You rolled " + EnglishNumberToWords.convertLessThanOneThousand(rolls.size())
                + " " + dieSides + " sided die for a total of " + sum + " ... "
                + text.toString();
//        log.debug(finalText);
//        return finalText;
    }

}
