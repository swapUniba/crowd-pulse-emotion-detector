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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


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
                            try
                            {
                                message.setEmotion(calculateEmotions(message.getText(), params.getLang()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case EmotionDetectorConfig.NEW:
                            if (message.getEmotion() == null) {
                                try
                                {
                                    message.setEmotion(calculateEmotions(message.getText(), params.getLang()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                logger.info("Message skipped (emotion calculated)");
                            }
                            break;

                        default:
                            try
                            {
                                message.setEmotion(calculateEmotions(message.getText(), params.getLang()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }

                } else {
                    try
                    {
                        message.setEmotion(calculateEmotions(message.getText(), "it"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
    private String calculateEmotions(String text, String lang) throws IOException {

        HttpURLConnection con = null;
        String url = "http://90.147.170.25:8080/emotion-labeling/rest/analyze/emotionalLabeling";

        String urlParameters = "text=" + text + "&lang=" + lang;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

        try
        {

            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream()))
            {
                wr.write(postData);
            }

            StringBuilder content;


            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

            if (content.toString().contains("{\"error\":\"not found\"}"))
                return "none";
            else return content.toString();

        }
        finally
        {

            con.disconnect();
        }
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

        /**
         * Accepted values: it, en.
         */
        private String lang;

        @Override
        public EmotionDetectorConfig buildFromJsonElement(JsonElement jsonElement) {
            return PluginConfigHelper.buildFromJson(jsonElement, EmotionDetectorConfig.class);
        }


        public String getLang() {
            return lang;
        }

        public void setLang(String calculate) {
            this.lang = lang;
        }


        public String getCalculate() {
            return calculate;
        }

        public void setCalculate(String calculate) {
            this.calculate = calculate;
        }
    }

}