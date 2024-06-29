package ai.saiy.android.amazon;

import java.util.Collections;

import ai.saiy.android.amazon.directives.Event;
import ai.saiy.android.amazon.directives.Header;
import ai.saiy.android.amazon.directives.Payload;
import ai.saiy.android.amazon.directives.StreamableEvent;
import ai.saiy.android.amazon.listener.StreamListener;
import ai.saiy.android.utils.MyLog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SpeechRecognizer {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String META_DATA = "metadata";
    private static final String AUDIO = "audio";

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SpeechRecognizer.class.getSimpleName();
    private final RequestBody requestBody;

    public SpeechRecognizer(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    private Event createEvent(Payload payload) {
        return new Event(createHeader(), payload);
    }

    private Header createHeader() {
        return new Header("SpeechRecognizer", "Recognize", UtilsNetwork.getMessageID(), "dialogRequest-321");
    }

    private MultipartBody.Builder createPartBuilder(StreamableEvent streamableEvent) {
        return new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(META_DATA, "metadata", RequestBody.create(MediaType.get(UtilsNetwork.JSON_UTF_8), streamableEvent.toJson())).addFormDataPart(AUDIO, "speech.wav", this.requestBody);
    }

    private Request.Builder createRequestBuilder(String url, String alexaAccessToken) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.addHeader(AUTHORIZATION, BEARER_ + alexaAccessToken);
        return builder;
    }

    private Payload createPayload(String url, String token) {
        Payload payload = new Payload(url, "AUDIO_L16_RATE_16000_CHANNELS_1", token);
        payload.setProfile("NEAR_FIELD");
        return payload;
    }

    private StreamableEvent createStreamableEvent(String url, String token) {
        return new StreamableEvent(createEvent(createPayload(url, token)), Collections.emptyList());
    }

    public void stream(String url, String alexaAccessToken, StreamListener listener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stream");
        }
        long nanoTime = System.nanoTime();
        try {
            Request.Builder requestBuilder = createRequestBuilder(url, alexaAccessToken);
            requestBuilder.post(createPartBuilder(createStreamableEvent(url, alexaAccessToken)).build());
            okhttp3.Call call = UtilsNetwork.getOkHttpClient().newCall(requestBuilder.build());
            if (listener != null) {
                listener.onSuccess(call);
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stream: Audio sent");
                MyLog.getElapsed("stream", nanoTime);
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "stream: Exception");
                e.printStackTrace();
            }
            if (listener != null) {
                listener.onError(e);
            }
        }
    }
}
