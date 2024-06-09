package ai.saiy.android.amazon.listener;

import ai.saiy.android.amazon.AmazonCredentials;

public interface IAlexaToken {
    void onSuccess(AmazonCredentials credentials);

    void onFailure(Exception e);
}
