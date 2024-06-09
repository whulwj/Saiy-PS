package ai.saiy.android.amazon.listener;

import okhttp3.Call;

public interface StreamListener {
    void onError(Exception e);

    void onSuccess(Call call);
}
