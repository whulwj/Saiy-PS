package ai.saiy.android.algorithms.contact;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.utils.MyLog;

public class ContactNameHelper {
    public static final double DEFAULT_DRIFT = 0.0d;
    public static final double DRIFT_ONE = -0.004d;
    public static final double DRIFT_TWO = -0.003d;
    public static final double DRIFT_THREE = -0.002d;
    public static final double DRIFT_FOUR = -0.001d;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ContactNameHelper.class.getSimpleName();

    private final ArrayList<Pair<String, String>> oneWordGroup = new ArrayList<>();
    private final ArrayList<Pair<String, String>> twoWordsGroup = new ArrayList<>();
    private final ArrayList<Pair<String, String>> threeWordsGroup = new ArrayList<>();
    private final ArrayList<Pair<String, String>> fourWordsGroup = new ArrayList<>();

    public void buildGroups(@NonNull ArrayList<String> inputData) {
        final long then = System.nanoTime();
        String[] split;
        boolean notInGroup;
        for (String vd : inputData) {
            split = vd.split(XMLResultsHandler.SEP_SPACE);
            int length = split.length;
            switch (length) {
                case 0:
                    break;
                case 1:
                    notInGroup = true;
                    for (Pair<String, String> stringPair : oneWordGroup) {
                        if (stringPair.first.matches(vd)) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        oneWordGroup.add(new Pair<>(vd, null));
                    }
                    break;
                case 2:
                    notInGroup = true;
                    for (Pair<String, String> stringPair : oneWordGroup) {
                        if (stringPair.first.matches(split[0])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        oneWordGroup.add(new Pair<>(split[0], split[1]));
                    }
                    twoWordsGroup.add(new Pair<>(vd, null));
                    break;
                case 3:
                    notInGroup = true;
                    for (Pair<String, String> stringPair : oneWordGroup) {
                        if (stringPair.first.matches(split[0])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        oneWordGroup.add(new Pair<>(split[0], split[1] + XMLResultsHandler.SEP_SPACE + split[2]));
                    }
                    notInGroup = true;
                    for (Pair<String, String> stringPair : twoWordsGroup) {
                        if (stringPair.first.matches(split[0] + XMLResultsHandler.SEP_SPACE + split[1])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        twoWordsGroup.add(new Pair<>(split[0] + XMLResultsHandler.SEP_SPACE + split[1], split[2]));
                    }
                    threeWordsGroup.add(new Pair<>(vd, null));
                    break;
                case 4:
                    notInGroup = true;
                    for (Pair<String, String> stringPair : oneWordGroup) {
                        if (stringPair.first.matches(split[0])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        oneWordGroup.add(new Pair<>(split[0], split[1] + XMLResultsHandler.SEP_SPACE + split[2] + XMLResultsHandler.SEP_SPACE + split[3]));
                    }
                    notInGroup = true;
                    for (Pair<String, String> stringPair : twoWordsGroup) {
                        if (stringPair.first.matches(split[0] + XMLResultsHandler.SEP_SPACE + split[1])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        twoWordsGroup.add(new Pair<>(split[0] + XMLResultsHandler.SEP_SPACE + split[1], split[2] + XMLResultsHandler.SEP_SPACE + split[3]));
                    }
                    notInGroup = true;
                    for (Pair<String, String> stringPair : threeWordsGroup) {
                        if (stringPair.first.matches(split[0] + XMLResultsHandler.SEP_SPACE + split[1] + XMLResultsHandler.SEP_SPACE + split[2])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        threeWordsGroup.add(new Pair<>(split[0] + XMLResultsHandler.SEP_SPACE + split[1] + XMLResultsHandler.SEP_SPACE + split[2], split[3]));
                    }
                    fourWordsGroup.add(new Pair<>(vd, null));
                    break;
                default:
                    notInGroup = true;
                    for (Pair<String, String> stringPair : oneWordGroup) {
                        if (stringPair.first.matches(split[0])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(split[1]);
                        for (int i = 2; i < length; i++) {
                            stringBuilder.append(XMLResultsHandler.SEP_SPACE).append(split[i]);
                        }
                        oneWordGroup.add(new Pair<>(split[0], stringBuilder.toString().trim()));
                    }
                    notInGroup = true;
                    for (Pair<String, String> stringPair : twoWordsGroup) {
                        if (stringPair.first.matches(split[0] + XMLResultsHandler.SEP_SPACE + split[1])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(split[2]);
                        for (int i = 3; i < length; i++) {
                            stringBuilder.append(XMLResultsHandler.SEP_SPACE).append(split[i]);
                        }
                        twoWordsGroup.add(new Pair<>(split[0] + XMLResultsHandler.SEP_SPACE + split[1], stringBuilder.toString().trim()));
                    }
                    notInGroup = true;
                    for (Pair<String, String> stringPair : threeWordsGroup) {
                        if (stringPair.first.matches(split[0] + XMLResultsHandler.SEP_SPACE + split[1] + XMLResultsHandler.SEP_SPACE + split[2])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(split[3]);
                        for (int i = 4; i < length; i++) {
                            stringBuilder.append(XMLResultsHandler.SEP_SPACE).append(split[i]);
                        }
                        threeWordsGroup.add(new Pair<>(split[0] + XMLResultsHandler.SEP_SPACE + split[1] + XMLResultsHandler.SEP_SPACE + split[2], stringBuilder.toString().trim()));
                    }
                    notInGroup = true;
                    for (Pair<String, String> stringPair : fourWordsGroup) {
                        if (stringPair.first.matches(split[0] + XMLResultsHandler.SEP_SPACE + split[1] + XMLResultsHandler.SEP_SPACE + split[2] + XMLResultsHandler.SEP_SPACE + split[3])) {
                            notInGroup = false;
                        }
                    }
                    if (notInGroup) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(split[4]);
                        for (int i = 5; i < length; i++) {
                            stringBuilder.append(XMLResultsHandler.SEP_SPACE).append(split[i]);
                        }
                        fourWordsGroup.add(new Pair<>(split[0] + XMLResultsHandler.SEP_SPACE + split[1] + XMLResultsHandler.SEP_SPACE + split[2] + XMLResultsHandler.SEP_SPACE + split[3], stringBuilder.toString().trim()));
                    }
                    break;
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
    }

    public @NonNull ArrayList<Pair<String, String>> getOneWordGroup() {
        return oneWordGroup;
    }

    public @NonNull ArrayList<Pair<String, String>> getThreeWordsGroup() {
        return threeWordsGroup;
    }

    public @NonNull ArrayList<Pair<String, String>> getTwoWordsGroup() {
        return twoWordsGroup;
    }

    public @NonNull ArrayList<Pair<String, String>> getFourWordsGroup() {
        return fourWordsGroup;
    }
}
