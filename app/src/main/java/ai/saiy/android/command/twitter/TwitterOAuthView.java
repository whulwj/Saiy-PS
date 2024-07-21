package ai.saiy.android.command.twitter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ai.saiy.android.utils.MyLog;
import twitter4j.AccessToken;
import twitter4j.OAuthAuthorization;
import twitter4j.RequestToken;
import twitter4j.TwitterException;

/**
 * A {@link WebView} subclass dedicated to Twitter OAuth on Android,
 * using <a href="http://twitter4j.org/">twitter4j</a>.
 *
 * <p>
 * As this class is implemented as a subclass of {@code View}, it can be
 * integrated into the Android layout system seamlessly. This fact
 * makes this class an easily-reusable UI component.
 * </p>
 *
 * <p>
 * To use this class, it is not necessary to review the flow of
 * OAuth handshake. Just implement {@link TwitterOAuthView.Listener}
 * and call {@link #start(String, String, String, boolean, Listener)
 * start()} method. The result of OAuth handshake is reported via
 * either of the listener's methods, {@link
 * Listener#onSuccess(TwitterOAuthView, AccessToken) onSuccess()} or
 * {@link Listener#onFailure(TwitterOAuthView, TwitterOAuthView.Result)
 * onFailure()}.
 * </p>
 */
public class TwitterOAuthView extends WebView {
    /**
     * Internal flag for debug logging. Change the value to {@code true}
     * to turn on debug logging.
     */
    private static final boolean DEBUG = MyLog.DEBUG;
    /**
     * Tag for logging.
     */
    private final String CLS_NAME = TwitterOAuthView.class.getSimpleName();

    /**
     * Twitter OAuth task that has been invoked by {@link
     * #start(String, String, String, boolean, Listener) start} method.
     */
    private TwitterOAuthTask twitterOAuthTask;
    /**
     * Flag to call cancel() from within onDetachedFromWindow().
     */
    private boolean cancelOnDetachedFromWindow = true;

    /**
     * Result code of Twitter OAuth process.
     *
     * @author Takahiko Kawasaki
     */
    public enum Result {
        /**
         * The application has been authorized by the user and
         * got an access token successfully.
         */
        SUCCESS,
        /**
         * Twitter OAuth process was cancelled. This result code
         * is generated when the internal {@link AsyncTask}
         * subclass was cancelled for some reasons.
         */
        CANCELLATION,
        /**
         * Twitter OAuth process was not even started due to
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
     * Listener to be notified of Twitter OAuth process result.
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
        void onFailure(TwitterOAuthView view, Result result);
        /**
         * Called when the application has been authorized by the user
         * and got an access token successfully.
         *
         * @param view
         * @param accessToken
         */
        void onSuccess(TwitterOAuthView view, AccessToken accessToken);
    }

    private class TwitterOAuthTask extends AsyncTask<Object, Void, Result> {
        private String callbackUrl;
        private boolean dummyCallbackUrl;
        private Listener listener;
        private OAuthAuthorization oAuth;
        private RequestToken requestToken;
        private volatile boolean authorizationDone;
        private volatile String verifier;
        private AccessToken accessToken;

        private class LocalWebViewClient extends WebViewClient {
            private LocalWebViewClient() {
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB || !shouldOverrideUrlLoading(view, url)) {
                    return;
                }
                stopLoading();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Something wrong happened during the authorization step.
                MyLog.e(CLS_NAME, "onReceivedError: [" + errorCode + "] " + description);

                // Stop the authorization step.
                notifyAuthorization();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                // Check if the given URL is the callback URL.
                if (!url.startsWith(callbackUrl)) {
                    // The URL is not the callback URL.
                    return false;
                }
                // This web view is about to be redirected to the callback URL.
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Detected the callback URL: " + url);
                }
                // Convert String to Uri.
                final Uri uri = Uri.parse(url);

                // Get the value of the query parameter "oauth_verifier".
                // A successful response should contain the parameter.
                verifier = uri.getQueryParameter("oauth_verifier");
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "oauth_verifier = " + verifier);
                }
                // Notify that the the authorization step was done.
                notifyAuthorization();

                // Whether the callback URL is actually accessed or not
                // depends on the value of dummyCallbackUrl. If the
                // value of dummyCallbackUrl is true, the callback URL
                // is not accessed.
                return dummyCallbackUrl;
            }
        }

        private TwitterOAuthTask() {
        }

        private void fireOnSuccess() {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Calling Listener.onResetSuccess");
            }
            // Call onSuccess() method of the listener.
            listener.onSuccess(TwitterOAuthView.this, accessToken);
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
            synchronized (TwitterOAuthView.this) {
                if (twitterOAuthTask == this) {
                    TwitterOAuthView.this.twitterOAuthTask = null;
                }
            }
        }

        private void fireOnFailure(Result result) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Calling Listener.onFailure, result = " + result);
            }
            // Call onFailure() method of the listener.
            listener.onFailure(TwitterOAuthView.this, result);
        }

        private RequestToken getRequestToken() {
            try {
                // Get a request token. This triggers network access.
                final RequestToken token = oAuth.getOAuthRequestToken(callbackUrl);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Got a request token.");
                }
                return token;
            } catch (TwitterException e) {
                // Failed to get a request token.
                if (DEBUG) {
                    e.printStackTrace();
                    MyLog.e(CLS_NAME, "Failed to get a request token.");
                }

                // No request token.
                return null;
            }
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

        private AccessToken getAccessToken() {
            try {
                // Get an access token. This triggers network access.
                final AccessToken token = oAuth.getOAuthAccessToken(requestToken, verifier);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Got an access token for " + token.getScreenName());
                }
                return token;
            } catch (TwitterException e) {
                // Failed to get an access token.
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "Failed to get an access token.");
                    e.printStackTrace();
                }

                // No access token.
                return null;
            }
        }

        @Override
        protected Result doInBackground(Object... args) {
            // Check if this task has been cancelled.
            if (checkCancellation("doInBackground() [on entry]")) {
                return Result.CANCELLATION;
            }
            // Name the arguments.
            String consumerKey = (String) args[0];
            String consumerSecret = (String) args[1];
            this.callbackUrl = (String) args[2];
            this.dummyCallbackUrl = (Boolean) args[3];
            this.listener = (Listener) args[4];
            // Create an OAuthAuthorization instance with the given pair of
            // consumer key and consumer secret.
            this.oAuth = OAuthAuthorization.newBuilder().oAuthConsumer(consumerKey, consumerSecret).build();

            // Get a request token. This triggers network access.
            this.requestToken = getRequestToken();
            if (requestToken == null) {
                // Failed to get a request token.
                return Result.REQUEST_TOKEN_ERROR;
            }

            // Access Twitter's authorization page. After the user's
            // operation, this web view is redirected to the callback
            // URL, which is caught by shouldOverrideUrlLoading() of
            // LocalWebViewClient.
            authorize();

            // Wait until the authorization step is done.
            final boolean cancelled = waitForAuthorization();
            if (cancelled) {
                // Cancellation was detected while waiting.
                return Result.CANCELLATION;
            }

            // If the authorization has succeeded, 'verifier' is not null.
            if (verifier == null) {
                return Result.AUTHORIZATION_ERROR;
            }

            // Check if this task has been cancelled.
            if (checkCancellation("doInBackground() [before getAccessToken()]")) {
                return Result.CANCELLATION;
            }

            // The authorization succeeded. The last step is to get
            // an access token using the verifier.
            this.accessToken = getAccessToken();
            if (accessToken == null) {
                // Failed to get an access token.
                return Result.ACCESS_TOKEN_ERROR;
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

            // Set null to twitterOAuthTask if appropriate.
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
            String url = requestToken.getAuthorizationURL();
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

            // Set null to twitterOAuthTask if appropriate.
            clearTaskReference();
        }

        @Override
        protected void onPreExecute() {
            // Set up a WebViewClient on the UI thread.
            setWebViewClient(new LocalWebViewClient());
        }
    }

    /**
     * A constructor that calls {@link WebView#WebView(Context) super}(context).
     *
     * @param context
     */
    public TwitterOAuthView(Context context) {
        super(context);
        // Additional initialization.
        init();
    }

    /**
     * A constructor that calls {@link WebView#WebView(Context, AttributeSet)
     * super}(context, attrs).
     *
     * @param context
     * @param attrs
     */
    public TwitterOAuthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Additional initialization.
        init();
    }

    /**
     * A constructor that calls {@link WebView#WebView(Context, AttributeSet, int)
     * super}(context, attrs, defStyle).
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public TwitterOAuthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Additional initialization.
        init();
    }

    private void cancelTask(TwitterOAuthTask task) {
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
    @SuppressLint("SetJavaScriptEnabled")
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
     * Cancel the Twitter OAuth process.
     *
     * <p>
     * The main purpose of this method is to cancel an AsyncTask
     * that may be running. The current implementation of this
     * method does not call {@link #stopLoading()}, so call it
     * yourself if necessary.
     * </p>
     */
    public void cancel() {
        TwitterOAuthTask task;

        synchronized (this) {
            // Get the reference of the running task.
            task = twitterOAuthTask;
            this.twitterOAuthTask = null;
        }

        // Cancel a task, if not null.
        cancelTask(task);
    }

    /**
     * Start Twitter OAuth process.
     *
     * <p>
     * This method does the following in the background.
     * </p>
     *
     * <ol>
     * <li>Get a request token using the given pair of consumer key
     *     and consumer secret.
     * <li>Load the authorization URL that the obtained request token
     *     points to into this {@code TwitterOAuthView} instance.
     * <li>Wait for the user to finish the authorization process at
     *     Twitter's authorization site. This {@code TwitterOAuthView}
     *     instance is redirected to the callback URL as a result.
     * <li>Detect the redirection to the callback URL and retrieve
     *     the value of the {@code oauth_verifier} parameter from the URL.
     *     If and only if {@code dummyCallbackUrl} is {@code false},
     *     the callback URL is actually accessed.
     * <li>Get an access token using the {@code oauth_verifier}.
     * <li>Call {@link Listener#onSuccess(TwitterOAuthView, AccessToken)
     *     onSuccess()} method of the {@link Listener listener} on the
     *     UI thread.
     * </ol>
     *
     * <p>
     * If an error occurred during the above steps, {@link
     * Listener#onFailure(TwitterOAuthView, TwitterOAuthView.Result)
     * onFailure()} of the {@link Listener listener} is called.
     * </p>
     *
     * <p>
     * This method cancels a running {@code AsyncTask} that may have
     * been invoked by the previous call of this method before invoking
     * a new {@code AsyncTask}.
     * </p>
     *
     * @param consumerKey
     * @param consumerSecret
     * @param callbackUrl
     * @param dummyCallbackUrl
     * @param listener
     *
     * @throws IllegalArgumentException
     *         At least one of {@code consumerKey}, {@code consumerSecret},
     *         {@code callbackUrl} or {@code listener} is null.
     */
    public void start(String consumerKey, String consumerSecret, String callbackUrl, boolean dummyCallbackUrl, Listener listener) {
        // Check the given arguments.
        if (consumerKey == null || consumerSecret == null || callbackUrl == null || listener == null) {
            throw new IllegalArgumentException();
        }

        // Convert the boolean parameter to a Boolean object to pass it
        // as an argument of AsyncTask.execute().
        Boolean dummy = Boolean.valueOf(dummyCallbackUrl);

        TwitterOAuthTask oldTask;
        TwitterOAuthTask newTask;

        synchronized (this) {
            // Renew Twitter OAuth task.
            oldTask = twitterOAuthTask;
            newTask = new TwitterOAuthTask();
            this.twitterOAuthTask = newTask;
        }

        // Cancel an old running task, if not null.
        cancelTask(oldTask);

        // Execute the new task.
        newTask.execute(consumerKey, consumerSecret, callbackUrl, dummy, listener);
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
     * The implementation of this method of {@code TwitterOAuthView}
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
        cancelOnDetachedFromWindow = enabled;
    }
}
