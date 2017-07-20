/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llamadice.model;

/**
 *
 * @author ahill
 */
public enum SkillIntent {
    HELP("AMAZON.HelpIntent"), STOP("AMAZON.StopIntent"), CANCEL("AMAZON.CancelIntent"),
    ROLL_ONE("RollOneIntent"), ROLL_SOME("RollSomeIntent"), ROLL_AGAIN("RollAgainIntent");

    private final String text;

    SkillIntent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static SkillIntent fromString(String text) {
        for (SkillIntent b : SkillIntent.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

}
