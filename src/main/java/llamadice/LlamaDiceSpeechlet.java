/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package llamadice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import java.util.Map;
import llamadice.model.DiceRoller;
import llamadice.model.RollResult;
import llamadice.model.SkillIntent;

public class LlamaDiceSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(LlamaDiceSpeechlet.class);

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        logHeaderStuff("onSessionStarted", "ReqID: " + request.getRequestId(), "SessionId" + session.getSessionId());

        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {

        String speechOutput
                = "Welcome to the Llama Dice roller! You can ask the llama dice to do something like "
                + "please roll 5 6 sided dice ...... now what can I roll for you today?";
        // If the user either does not reply to the welcome message or says
        // something that is not understood, they will be prompted again with this text.
        String repromptText = "For instructions on what you can roll, please say help me.";

        // Here we are prompting the user for input
        return newAskResponse(speechOutput, repromptText);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {

        logHeaderStuff("INTENT REQUEST", "ReqID: " + request.getRequestId(), "SessionId" + session.getSessionId());
        
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        logStuff("Intent found: " + intentName);
        logSlots(intent.getSlots());

        if (null != intent) {
            switch (SkillIntent.fromString(intentName)) {
                case ROLL_ONE:
                    return rollOne(request);
                case ROLL_SOME:
                    return rollSome(request);
                case HELP:
                    return getHelp();
                case STOP:
                case CANCEL:

                    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                    outputSpeech.setText("Goodbye");
                    return SpeechletResponse.newTellResponse(outputSpeech);

            }
        }

        throw new SpeechletException("Invalid Intent");
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        logHeaderStuff("onSessionEnded", "ReqID: " + request.getRequestId(), "SessionId" + session.getSessionId());

        // any cleanup logic goes here
    }

    private boolean checkSideValid(Slot slot) {
        return slot != null && slot.getValue() != null;
    }


    private static final String NUM_DIE_SLOT = "NumDie";
    private static final String NUM_SIDES_SLOT = "DieSides";

    private SpeechletResponse rollOne(IntentRequest req) {
        Intent intent = req.getIntent();
        Slot numSidesSlot = intent.getSlot(NUM_SIDES_SLOT);
        
        if (checkSideValid(numSidesSlot)) {

            try {
                int numSides = Integer.parseInt(numSidesSlot.getValue());
                RollResult result = DiceRoller.roll(numSides, 1);
                
                PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                String resultText = result.rollToText();
                outputSpeech.setText(resultText);

                SimpleCard card = new SimpleCard();
                card.setTitle("Rolls for " + 1 + " " + numSides + " sided die.");
                card.setContent(resultText);

                return SpeechletResponse.newTellResponse(outputSpeech, card);

            } catch (NumberFormatException e) {
                return confusedByRequest();
            }

        } else {
            // There was no item in the intent so return the help prompt.
            return getHelp();
        }
    }
    
    private SpeechletResponse rollSome(IntentRequest req) {
        Intent intent = req.getIntent();
        Slot numSidesSlot = intent.getSlot(NUM_SIDES_SLOT);
        Slot numDieSlot = intent.getSlot(NUM_DIE_SLOT);

        if (checkSideValid(numDieSlot) && checkSideValid(numSidesSlot)) {

            try {
                int numSides = Integer.parseInt(numSidesSlot.getValue());
                int numDie = Integer.parseInt(numDieSlot.getValue());
                RollResult result = DiceRoller.roll(numSides, numDie);
                
                PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                String resultText = result.rollsToText();
                outputSpeech.setText(resultText);

                SimpleCard card = new SimpleCard();
                card.setTitle("Rolls for " + numDie + " " + numSides + " sided die.");
                card.setContent(resultText);

                return SpeechletResponse.newTellResponse(outputSpeech, card);

            } catch (NumberFormatException e) {
                logHeaderStuff("Had an exception", e.getStackTrace().toString());
                return confusedByRequest();
            }

        } else {
            // There was no item in the intent so return the help prompt.
            return getHelp();
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the HelpIntent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelp() {
        String speechOutput
                = "You can ask me to roll all sorts of dice such as, "
                + "roll 5 dice with 20 sides, or, you can say exit... "
                + "Now, what can I help you with?";
        String repromptText
                = "You can say things like, roll me one five sided die, "
                + "or you can say exit... Now, what can I help you with?";
        return newAskResponse(speechOutput, repromptText);
    }

    private SpeechletResponse confusedByRequest() {
        return newAskResponse("I'm sorry, I didn't quite catch that...", "Could you please repeat your request?");
    }

    /**
     * Wrapper for creating the Ask response. The OutputSpeech and {@link Reprompt} objects are created from the input strings.
     *
     * @param stringOutput the output to be spoken
     * @param repromptText the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
       
        logHeaderStuff("RETURNING ASK RESPONSE", stringOutput, repromptText);
        
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);

        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

    
    private void logHeaderStuff(String header, String... stuff){
        log.debug("*****************************"+  header + " *****************************");
        for(String x : stuff){
            log.debug(x);
        }
    }
    
    private void logStuff(String... stuff){
        for(String x : stuff){
            log.debug(x);
        }
    }
    
    private void logSlots(Map<String, Slot> slots){
        for(String slotName : slots.keySet()){
            logStuff("Slot(" + slotName + ") : " + slots.get(slotName).getValue());
        }
    }
    
//    private void logRequest(String reqType, String requestId, String sessionId) {
//        log.debug("*****************************"+  reqType + " REQUEST(" + requestId + ") : SESS(" + sessionId + " ) *****************************");
//    }
}
