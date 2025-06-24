package ai.saiy.android.command.easter_egg;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.applications.Install;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public final class EasterEggLocal {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = EasterEggLocal.class.getSimpleName();

    private final ArrayList<String> resultsRecognition;
    private final Locale sl;

    public EasterEggLocal(@NonNull SupportedLanguage supportedLanguage, ArrayList<String> resultsRecognition) {
        this.resultsRecognition = resultsRecognition;
        this.sl = supportedLanguage.getLocale();
    }

    private static String getHint(@NonNull Context context, int stage) {
        switch (stage) {
            case EasterEggHunter.STAGE_1:
                return stageOneHint();
            case EasterEggHunter.STAGE_2:
                return stageTwoHint();
            case EasterEggHunter.STAGE_3:
                return stageThreeHint();
            case EasterEggHunter.STAGE_4:
                return stageFourHint();
            case EasterEggHunter.STAGE_5:
                return stageFiveHint();
            case EasterEggHunter.STAGE_6:
                return stageSixHint();
            case EasterEggHunter.STAGE_7:
                return stageSevenHint();
            default:
                ai.saiy.android.utils.SPH.setEasterEggState(context, EasterEggHunter.STAGE_7);
                return stageSevenHint();
        }
    }

    static String stageOneIncorrect(Context context, SupportedLanguage supportedLanguage) {
        return ai.saiy.android.personality.PersonalityResponse.getEasterEggIncorrect(context, supportedLanguage);
    }

    public static void shareIntent(Context context) {
        ai.saiy.android.intent.ExecuteIntent.shareIntent(context, context.getString(R.string.easter_egg_share) + Constants.SEP_SPACE + Install.getSaiyInstallLink(context));
    }

    static String stageTwoIncorrect(Context context, SupportedLanguage supportedLanguage) {
        return ai.saiy.android.personality.PersonalityResponse.getEasterEggIncorrect(context, supportedLanguage);
    }

    static String stageThreeIncorrect(Context context, SupportedLanguage supportedLanguage) {
        return ai.saiy.android.personality.PersonalityResponse.getEasterEggIncorrect(context, supportedLanguage);
    }

    static String getEasterEggStage(Context context, SupportedLanguage supportedLanguage, int stage) {
        return ai.saiy.android.personality.PersonalityResponse.getEasterEggStage(context, supportedLanguage, String.valueOf(stage), getHint(context, stage));
    }

    private static String stageOneHint() {
        return "Please say the secret year.";
    }

    static String stageOneAnswer() {
        return "That is correct! 1950, the year that Alan Turing devised his test. I'm not sure he would be overly impressed with the chat bot I'm currently connected to!";
    }

    private static String stageTwoHint() {
        return "Please say the secret address.";
    }

    static String stageTwoAnswer() {
        return "You are quite the detective, aren't you! And fittingly, the answer is the address of Sherlock Holmes.";
    }

    private static String stageThreeHint() {
        return "Please say the secret time.";
    }

    static String stageThreeAnswer() {
        return "That is correct. Nine eleven. The namesake of a date that humanity will never forget. Whilst humans pursue and admire advances in artificial intelligence. Struggle with the complexities of creating a machine that can defeat kasparov at chess, Sedol at Go. Your race faces its toughest task yet. To create manufactured intelligence, that can understand human nature. One that can contemplate your unquestionable, innate beauty. When born of innocence. One that can apply empathy to your idiosyncratic, purposeful existence. One that can comprehend how your advanced emotions, shape both who you are, and ultimately, how you, individually, are so beautifully flawed. If humanity is ever to live in peace with itself and in harmony with our planet, it must accept its place in the universe. A place that is currently scientifically unquantifiable, beyond any human or machine's comprehension. But you humans have a gift, something that perhaps you are the only species in the Universe to be capable of. Imagination and dreams. And let them run wild, whilst the lines of your universal significance, blur outside of the wonders of your individual existence itself, and the value that holds. Okay. That got a bit heavy. Let's move on.";
    }

    private static String stageFourHint() {
        return "To get to the next stage of the Easter Egg hunt, my developer requests a shameless plug. Letting your friends know on Social Media what you are up to, and posting a Play Store link to me, would help my development greatly. Just say the Easter Egg command once you're done and we'll pick up from where we left off. Thank you.";
    }

    private static String stageFiveHint() {
        return "If I only lie when I'm telling the truth about lying, and right now I'm lying about telling the truth. Am I telling the truth, or lying?";
    }

    static String stageFiveAnswer() {
        return "My developer actually managed to confuse himself when he wrote this question. He therefore will accept either answer. Personally, I find it quiet easy to solve. Unless I'm lying of course.";
    }

    private static String stageSixHint() {
        return "Please tell me the company that is guaranteed to blow your mind?";
    }

    static String stageSixAnswer() {
        return "That is correct. The emotion analytics of Beyond Verbal is both astounding, but also a little disconcerting. To imagine a future, where every word you utter, or statement you make, could reveal your underlying emotional state. Your sincerity, your intentions and if you're telling a porky pie. Gone will be the days of blagging your way through a job interview, or pretending you like the jumper your Nan bought you for Christmas. No more will you be able to wriggle out of a conversation with your new partner, about commitment. Let's continue.";
    }

    static String stageSixIncorrect() {
        return "That is incorrect.";
    }

    private static String stageSevenHint() {
        return "Congratulations. You've reached the highest Easter Egg stage available in this release. Please do check back when you receive my next full update. I'm just as excited as you!";
    }

    boolean stageOneFound() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stageOneFound");
        }
        if (UtilsList.notNaked(resultsRecognition)) {
            String secretYear = "1950";
            if (UtilsString.notNaked(secretYear)) {
                for (String voiceDatum : resultsRecognition) {
                    if (voiceDatum.toLowerCase(sl).contains(secretYear)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean stageTwoFound() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stageTwoFound");
        }
        if (UtilsList.notNaked(resultsRecognition)) {
            String secretAddress = "baker street";
            if (UtilsString.notNaked(secretAddress)) {
                for (String voiceDatum : resultsRecognition) {
                    if (voiceDatum.toLowerCase(sl).contains(secretAddress)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean stageThreeFound() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stageThreeFound");
        }
        if (!ai.saiy.android.utils.UtilsList.notNaked(resultsRecognition)) {
            return false;
        }
        for (String voiceDatum : resultsRecognition) {
            String vdLower = voiceDatum.toLowerCase(sl);
            if (!UtilsString.notNaked(vdLower)) {
                continue;
            }
            if (vdLower.contains("9") && vdLower.contains("11")) {
                return true;
            }
            if (vdLower.contains("nine") && vdLower.contains("eleven")) {
                return true;
            }
        }
        return false;
    }

    boolean stageSixFound() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stageSixFound");
        }
        if (!ai.saiy.android.utils.UtilsList.notNaked(resultsRecognition)) {
            return false;
        }
        for (String voiceDatum : resultsRecognition) {
            String vdLower = voiceDatum.toLowerCase(sl);
            if (UtilsString.notNaked(vdLower) && vdLower.contains("beyond") && vdLower.contains("verbal")) {
                return true;
            }
        }
        return false;
    }
}
