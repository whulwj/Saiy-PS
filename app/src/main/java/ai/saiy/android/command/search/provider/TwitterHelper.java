package ai.saiy.android.command.search.provider;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Pair;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.defaults.notes.NoteProvider;
import ai.saiy.android.firebase.database.reference.TwitterReference;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = TwitterHelper.class.getSimpleName();

    public enum Type {
        GENERIC,
        HASHTAG
    }

    public static boolean isCredentialsValid(Context context) {
        final Pair<Boolean, Pair<String, String>> twitterPair = new TwitterReference().getCredentials(context);
        if (twitterPair.first) {
            try {
                final Twitter twitter = Twitter.newBuilder().oAuthConsumer(twitterPair.second.first, twitterPair.second.second)
                        .oAuthAccessToken(SPH.getTwitterToken(context), SPH.getTwitterSecret(context)).build();
                return twitter.v1().users().verifyCredentials() != null;
            } catch (TwitterException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "TwitterException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "Exception");
                    e.printStackTrace();
                }
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "twitterPair error");
        }
        return false;
    }

    public static boolean shareIntent(Context context, String str) {
        if (Installed.isPackageInstalled(context, Installed.PACKAGE_TWITTER)) {
            str = str.replaceAll("hash tags ", "\\%23").replaceAll("hash tag ", "\\%23").replaceAll("hashtags ", "\\%23").replaceAll("hashtag ", "\\%23");
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(NoteProvider.NOTE_MIME);
            intent.putExtra(Intent.EXTRA_TEXT, str);
            intent.setComponent(new ComponentName(Installed.PACKAGE_TWITTER, Installed.PACKAGE_TWITTER + ".composer.ComposerActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "shareIntent: ActivityNotFoundException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "shareIntent: Exception");
                    e.printStackTrace();
                }
            }
        }
        return shareWeb(context, str);
    }

    private static boolean shareWeb(Context context, String str) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(("https://twitter.com/intent/tweet?text=" + str.replaceAll("hash tags ", "\\%23").replaceAll("hash tag ", "\\%23").replaceAll("hashtags ", "\\%23").replaceAll("hashtag ", "\\%23")).replaceAll("\\s", "\\%20")));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "shareWeb ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "shareWeb Exception");
                e.printStackTrace();
            }
        }
        return false;
    }
}
