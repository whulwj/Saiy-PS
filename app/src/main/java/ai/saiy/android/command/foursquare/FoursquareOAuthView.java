package ai.saiy.android.command.foursquare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class FoursquareOAuthView extends WebView {
    /**
     * Internal flag for debug logging. Change the value to {@code true}
     * to turn on debug logging.
     */
    private static final boolean DEBUG = MyLog.DEBUG;
    /**
     * Tag for logging.
     */
    private final String CLS_NAME = FoursquareOAuthView.class.getSimpleName();
    /**
     * Foursquare OAuth task that has been invoked by {@link
     * #start(String, String, boolean, FoursquareOAuthView.Listener) start} method.
     */
    private FoursquareOAuthTask foursquareOAuthTask;
    /**
     * Flag to call cancel() from within onDetachedFromWindow().
     */
    private boolean cancelOnDetachedFromWindow = true;

    /**
     * Result code of Foursquare OAuth process.
     */
    public enum Result {
        /**
         * The application has been authorized by the user and
         * got an access token successfully.
         */
        SUCCESS,
        /**
         * Foursquare OAuth process was cancelled. This result code
         * is generated when the internal {@link AsyncTask}
         * subclass was cancelled for some reasons.
         */
        CANCELLATION,
        /**
         * Foursquare OAuth process was not even started due to
         * failure of getting a request token. The pair of
         * consumer key and consumer secret was wrong or some
         * kind of network error occurred.
         */
        REQUEST_TOKEN_ERROR,
        /**
         * The application has not been authorized by the user,
         * or a network error occurred during the OAuth handshake.
         */
        AUTHORIZATION_ERROR,
        /**
         * The application has been authorized by the user but
         * failed to get an access token.
         */
        ACCESS_TOKEN_ERROR
    }

    /**
     * Listener to be notified of Foursquare OAuth process result.
     *
     * <p>
     * The methods of this listener are called on the UI thread.
     * </p>
     */
    public interface Listener {
        /**
         * Called when the OAuth process was not completed successfully.
         *
         * @param view
         * @param result
         */
        void onFailure(FoursquareOAuthView view, Result result);

        /**
         * Called when the application has been authorized by the user
         * and got an access token successfully.
         *
         * @param view
         * @param accessToken
         */
        void onSuccess(FoursquareOAuthView view, String accessToken);
    }

    private class FoursquareOAuthTask extends AsyncTask<Object, Void, Result> {
        private String clientId;
        private String callbackUrl;
        private boolean dummyCallbackUrl;
        private Listener listener;
        private volatile boolean authorizationDone;
        private String accessToken;

        private class LocalWebViewClient extends WebViewClient {
            private LocalWebViewClient() {
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onPageStarted: " + url);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Something wrong happened during the authorization step.
                MyLog.e(CLS_NAME, "onReceivedError: [" + errorCode + "] " + description);

                // Stop the authorization step.
                notifyAuthorization();
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler handler, SslError sslError) {
                handler.cancel();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "shouldOverrideUrlLoading: " + url);
                }
                // Check if the given URL is the callback URL.
                if (!url.startsWith(callbackUrl)) {
                    return false;
                }
                // This web view is about to be redirected to the callback URL.
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "shouldOverrideUrlLoading; Detected the callback URL: " + url);
                }
                // Convert String to Uri.
                final Uri uri = Uri.parse(url);

                // Get the value of the path parameter "access_token".
                // A successful response should contain the parameter.
                final String fragment = uri.getFragment();
                if (UtilsString.notNaked(fragment)) {
                    String token = fragment.replaceFirst("access_token=", "");
                    if (UtilsString.notNaked(token)) {
                        FoursquareOAuthTask.this.accessToken = token;
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "shouldOverrideUrlLoading: accessToken = " + accessToken);
                        }
                    } else if (DEBUG) {
                        MyLog.d(CLS_NAME, "shouldOverrideUrlLoading: fragment stripped naked");
                    }
                } else if (DEBUG) {
                    MyLog.d(CLS_NAME, "shouldOverrideUrlLoading: fragment naked");
                }
                // Notify that the the authorization step was done.
                notifyAuthorization();
                return dummyCallbackUrl;
            }
        }

        private FoursquareOAuthTask() {
        }

        private void fireOnSuccess() {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Calling Listener.onResetSuccess");
            }
            // Call onSuccess() method of the listener.
            this.listener.onSuccess(FoursquareOAuthView.this, this.accessToken);
        }

        /**
         * Check whether this task has been cancelled or not.
         *
         * @return
         *         {@code true} if this task has been cancelled.
         */
        private boolean checkCancellation(String context) {
            if (!isCancelled()) {
                return false;
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Cancellation was detected in the context of " + context);
            }
            return true;
        }

        private void clearTaskReference() {
            synchronized (FoursquareOAuthView.this) {
                if (FoursquareOAuthView.this.foursquareOAuthTask == this) {
                    FoursquareOAuthView.this.foursquareOAuthTask = null;
                }
            }
        }

        private void fireOnFailure(Result result) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Calling Listener.onFailure, result = " + result);
            }
            // Call onFailure() method of the listener.
            listener.onFailure(FoursquareOAuthView.this, result);
        }

        private void authorize() {
            // WebView.loadUrl() needs to be called on the UI thread,
            // so trigger onProgressUpdate().
            publishProgress();
        }

        private boolean waitForAuthorization() {
            while (!authorizationDone) {
                // Check if this task has been cancelled.
                if (checkCancellation("waitForAuthorization()")) {
                    // Cancelled.
                    return true;
                }
                synchronized (this) {
                    try {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "Waiting for the authorization step to be done.");
                        }
                        // Wait until interrupted.
                        wait();
                    } catch (InterruptedException e) {
                        // Interrupted.
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "Interrupted while waiting for the authorization step to be done.");
                        }
                    }
                }
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Finished waiting for the authorization step to be done.");
            }

            // Not cancelled.
            return false;
        }

        private void notifyAuthorization() {
            // The authorization step was done.
            this.authorizationDone = true;
            synchronized (this) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Notifying that the authorization step was done.");
                }
                // Notify to interrupt the loop in waitForAuthorization().
                notify();
            }
        }

        @Override
        protected Result doInBackground(Object... args) {
            // Check if this task has been cancelled.
            if (checkCancellation("doInBackground() [on entry]")) {
                return Result.CANCELLATION;
            }
            this.clientId = (String) args[0];
            this.callbackUrl = (String) args[1];
            this.dummyCallbackUrl = ((Boolean) args[2]).booleanValue();
            this.listener = (Listener) args[3];

            // Access Foursquare's authorization page. After the user's
            // operation, this web view is redirected to the callback
            // URL, which is caught by shouldOverrideUrlLoading() of
            // LocalWebViewClient.
            authorize();

            // Wait until the authorization step is done.
            boolean cancelled = waitForAuthorization();
            if (cancelled) {
                // Cancellation was detected while waiting.
                return Result.CANCELLATION;
            }

            // If the authorization has succeeded, 'access_token' is not null.
            if (accessToken == null) {
                return Result.AUTHORIZATION_ERROR;
            }

            // Check if this task has been cancelled.
            if (checkCancellation("doInBackground() [before getAccessToken()]")) {
                return Result.CANCELLATION;
            }

            // All the steps were done successfully.
            return Result.SUCCESS;
        }

        @Override
        protected void onPostExecute(Result result) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "onPostExecute: result = " + result);
            }
            if (result == null) {
                // Probably cancelled.
                result = Result.CANCELLATION;
            }
            if (result == Result.SUCCESS) {
                // Call onSuccess() method of the listener.
                fireOnSuccess();
            } else {
                // Call onFailure() method of the listener.
                fireOnFailure(result);
            }

            // Set null to foursquareOAuthTask if appropriate.
            clearTaskReference();
        }

        @Override
        protected void onProgressUpdate(Void... voidArr) {
            // Check if this task has been cancelled.
            if (checkCancellation("onProgressUpdate()")) {
                // Not load the authorization URL.
                return;
            }
            // In this implementation, onProgressUpdate() is called
            // only from authorize().

            // The authorization URL.
            final String url = "https://foursquare.com/oauth2/authenticate?client_id=" + clientId + "&response_type=token&redirect_uri=" + callbackUrl;
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Loading the authorization URL: " + url);
            }
            // Load the authorization URL on the UI thread.
            loadUrl(url);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Call onFailure() method of the listener.
            fireOnFailure(Result.CANCELLATION);

            // Set null to foursquareOAuthTask if appropriate.
            clearTaskReference();
        }

        @Override
        protected void onPreExecute() {
            // Set up a WebViewClient on the UI thread.
            setWebViewClient(new LocalWebViewClient());
        }
    }

    public FoursquareOAuthView(Context context) {
        super(context);
        // Additional initialization.
        init();
    }

    public FoursquareOAuthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Additional initialization.
        init();
    }

    public FoursquareOAuthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Additional initialization.
        init();
    }

    private void cancelTask(FoursquareOAuthTask task) {
        // If the given argument is null.
        if (task == null) {
            // No task to cancel. Nothing to do.
            return;
        }

        // If the task has not been cancelled yet.
        if (!task.isCancelled()) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Cancelling a task.");
            }
            // Cancel the task.
            task.cancel(true);
        }
        synchronized (task) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Notifying a task of cancellation.");
            }

            // Notify to interrupt the loop of waitForAuthorization().
            task.notify();
        }
    }

    /**
     * Initialization common for all constructors.
     */
    @SuppressLint({"SetJavaScriptEnabled"})
    private void init() {
        WebSettings settings = getSettings();
        // Not use cache.
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // Enable JavaScript.
        settings.setJavaScriptEnabled(true);
        // Enable zoom control.
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // Scroll bar
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    /**
     * Cancel the Foursquare OAuth process.
     *
     * <p>
     * The main purpose of this method is to cancel an AsyncTask
     * that may be running. The current implementation of this
     * method does not call {@link #stopLoading()}, so call it
     * yourself if necessary.
     * </p>
     */
    public void cancel() {
        FoursquareOAuthTask task;
        synchronized (this) {
            // Get the reference of the running task.
            task = this.foursquareOAuthTask;
            this.foursquareOAuthTask = null;
        }

        // Cancel a task, if not null.
        cancelTask(task);
    }

    /**
     * Start Foursquare OAuth process.
     *
     * <p>
     * This method does the following in the background.
     * </p>
     *
     * <ol>
     * <li>Load the authorization URL that the obtained request token
     *     points to into this {@code FoursquareOAuthView} instance.
     * <li>Wait for the user to finish the authorization process at
     *     Foursquare's authorization site. This {@code FoursquareOAuthView}
     *     instance is redirected to the callback URL as a result.
     * <li>Detect the redirection to the callback URL and retrieve
     *     the value of the {@code access_token} parameter from the URL.
     *     If and only if {@code dummyCallbackUrl} is {@code false},
     *     the callback URL is actually accessed.
     * <li>Get an access token using the {@code access_token}.
     * <li>Call {@link FoursquareOAuthView.Listener#onSuccess(FoursquareOAuthView, String)
     *     onSuccess()} method of the {@link FoursquareOAuthView.Listener listener} on the
     *     UI thread.
     * </ol>
     *
     * <p>
     * If an error occurred during the above steps, {@link
     * FoursquareOAuthView.Listener#onFailure(FoursquareOAuthView, FoursquareOAuthView.Result)
     * onFailure()} of the {@link FoursquareOAuthView.Listener listener} is called.
     * </p>
     *
     * <p>
     * This method cancels a running {@code AsyncTask} that may have
     * been invoked by the previous call of this method before invoking
     * a new {@code AsyncTask}.
     * </p>
     *
     * @param clientId
     * @param callbackUrl
     * @param dummyCallbackUrl
     * @param listener
     *
     * @throws IllegalArgumentException
     *         At least one of {@code clientId}, {@code callbackUrl} or {@code listener} is null.
     */
    public void start(String clientId, String callbackUrl, boolean dummyCallbackUrl, Listener listener) {
        // Check the given arguments.
        if (clientId == null || callbackUrl == null || listener == null) {
            throw new IllegalArgumentException();
        }
        FoursquareOAuthTask oldTask;
        FoursquareOAuthTask newTask;
        synchronized (this) {
            // Renew Foursquare OAuth task.
            oldTask = this.foursquareOAuthTask;
            newTask = new FoursquareOAuthTask();
            this.foursquareOAuthTask = newTask;
        }

        // Cancel an old running task, if not null.
        cancelTask(oldTask);

        // Execute the new task.
        newTask.execute(clientId, callbackUrl, Boolean.valueOf(dummyCallbackUrl), listener);
    }

    /**
     * Check if cancellation is executed on {@code onDetachedFromWindow()}.
     * The default value is {@code true}.
     *
     * @return
     *         {@code true} if {@link #cancel()} is called from within
     *         {@code onDetachedFromWindow()}.
     */
    public boolean isCancelOnDetachedFromWindow() {
        return cancelOnDetachedFromWindow;
    }

    /**
     * Called when this view is detached from the window.
     *
     * <p>
     * The implementation of this method of {@code FoursquareOAuthView}
     * calls {@code super.onDetachedFromWindow()}, and then {@link #cancel()}
     * if {@link #isCancelOnDetachedFromWindow()} returns {@code true}.
     * </p>
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isCancelOnDetachedFromWindow()) {
            cancel();
        }
    }

    /**
     * Change the configuration to call {@link #cancel()} on
     * {@code onDetachedFromWindow()}.
     *
     * @param enabled
     *         {@code true} to let this instance call {@link #cancel()}
     *         automatically from within {@code onDetachedFromWindow}.
     *         {@code false} to disable the automatic call.
     */
    public void setCancelOnDetachedFromWindow(boolean enabled) {
        this.cancelOnDetachedFromWindow = enabled;
    }
}
