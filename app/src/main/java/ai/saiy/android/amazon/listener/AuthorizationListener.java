package ai.saiy.android.amazon.listener;

public interface AuthorizationListener {
    void onSuccess();

    void onError(Exception e);

    void onCancel();
}
