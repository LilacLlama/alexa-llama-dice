/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llamadice.model;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import llamadice.LlamaDiceSpeechlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ahill
 */
public class DiceRoller {

    private static final Logger log = LoggerFactory.getLogger(DiceRoller.class);
    
    public static void main(String[] args) {
        int sides = 20;
        int times = 25;
        RollResult result = roll(sides, times);
        List<Integer> rolls = result.getRolls();

        System.out.println(result.getRolls());

        System.out.println("Max: " + max(rolls));
        System.out.println("Min: " + min(rolls));
        System.out.println("Avg: " + avg(rolls));
        System.out.println("Sum: " + sum(rolls));

        System.out.println("Counts: " + counts(rolls));
        
        System.out.println(result.rollsToText());

    }

    private static Random r = new Random();

    public static int roll(int sides) {
        return r.nextInt(sides) + 1;
    }

    public static RollResult roll(int sides, int times) {
        log.debug("Rolling d"+sides+" x"+times);
        RollResult result = new RollResult();
        result.setDieSides(sides);
        
        for (int i = 0; i < times; i++) {
            int roll = roll(sides);
            log.debug("Got a :" + roll);
            result.updateResult(roll);
        }
        
        return result;
    }

    public static double avg(List<Integer> rolls) {
        return rolls.stream().mapToInt(Integer::intValue).average().getAsDouble();
    }

    public static int max(List<Integer> rolls) {
        return rolls.stream().max(Comparator.naturalOrder()).get();
    }

    public static int min(List<Integer> rolls) {
        return rolls.stream().min(Comparator.naturalOrder()).get();
    }

    public static int sum(List<Integer> rolls) {
        return rolls.stream().mapToInt(Integer::intValue).sum();
    }

    public static Map<Integer, Long> counts(List<Integer> rolls) {
        return rolls.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public static List<Integer> sortRolls(List<Integer> rolls) {
        return rolls.stream().distinct().sorted().collect(Collectors.toList());
    }

}
