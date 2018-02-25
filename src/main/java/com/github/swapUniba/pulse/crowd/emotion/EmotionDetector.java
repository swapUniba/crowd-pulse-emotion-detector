package com.github.swapUniba.pulse.crowd.emotion;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.spi.IPlugin;
import com.github.frapontillo.pulse.spi.IPluginConfig;
import com.github.frapontillo.pulse.spi.PluginConfigHelper;
import com.google.gson.JsonElement;
import org.apache.logging.log4j.Logger;
import rx.Observable.Operator;
import rx.observers.SafeSubscriber;
import rx.Subscriber;

import java.util.Random;

import static com.github.frapontillo.pulse.util.PulseLogger.*;

/**
 * Emotion Detector plugin class.
 *
 * @author Cosimo Lovascio
 *
 */
public class EmotionDetector extends IPlugin<Message, Message, EmotionDetector.EmotionDetectorConfig> {

    private static final String PLUGIN_NAME = "emotion-detector";
    private final static Logger logger = getLogger(EmotionDetector.class);

    // emotions constants
    private static final String ANGER = "anger";
    private static final String JOY = "joy";
    private static final String FEAR = "fear";
    private static final String DISGUST = "disgust";
    private static final String SURPRISE = "surprise";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public EmotionDetectorConfig getNewParameter() {
        return new EmotionDetectorConfig();
    }

    @Override
    protected Operator<Message, Message> getOperator(EmotionDetectorConfig params) {
        return subscriber -> new SafeSubscriber<>(new Subscriber<Message>() {

            @Override
            public void onCompleted() {
                reportPluginAsCompleted();
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                reportPluginAsErrored();
                subscriber.onError(e);
            }

            @Override
            public void onNext(Message message) {
                reportElementAsStarted(message.getId());

                if (params != null) {

                    switch (params.getCalculate()) {

                        case EmotionDetectorConfig.ALL:
                            message.setEmotion(calculateEmotions(message.getText()));
                            break;

                        case EmotionDetectorConfig.NEW:
                            if (message.getEmotion() == null) {
                                message.setEmotion(calculateEmotions(message.getText()));
                            } else {
                                logger.info("Message skipped (emotion calculated)");
                            }
                            break;

                        default:
                            message.setEmotion(calculateEmotions(message.getText()));
                            break;
                    }

                } else {
                    message.setEmotion(calculateEmotions(message.getText()));
                }

                reportElementAsEnded(message.getId());
                subscriber.onNext(message);
            }
        });
    }

    /**
     * Calculate the emotions for the specified message text.
     * @param text the message text
     * @return the emotion
     */
    private String calculateEmotions(String text) {

        // TODO replace code here with real-world one
        String emotions[] = {ANGER, JOY, FEAR, DISGUST, SURPRISE};
        return emotions[new Random().nextInt(emotions.length)];
    }

    /**
     * Plugin configuration class.
     */
    class EmotionDetectorConfig implements IPluginConfig<EmotionDetectorConfig> {

        /**
         * Get the emotions of all messages coming from the stream.
         */
        public static final String ALL = "all";

        /**
         * Get the emotions of the messages with no emotions (property is null).
         */
        public static final String NEW = "new";


        /**
         * Accepted values: NEW, ALL.
         */
        private String calculate;

        @Override
        public EmotionDetectorConfig buildFromJsonElement(JsonElement jsonElement) {
            return PluginConfigHelper.buildFromJson(jsonElement, EmotionDetectorConfig.class);
        }

        public String getCalculate() {
            return calculate;
        }

        public void setCalculate(String calculate) {
            this.calculate = calculate;
        }
    }

}