package ai.saiy.android.tutorial;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class TutorialHelper {
    private static String address_me_as;
    private static String addressed_as;
    private static String my_name_is;
    private static String be_called;
    private static String be_known_as;

    private static String sausage;
    private static String bean;
    private static String bean_sic;
    private static String chip;
    private static String one;
    private static String two;
    private static String three;
    private static String four;
    private static String five;
    private static String numeric_1_2_3_4_5;
    private static String male;
    private static String male_sic;
    private static String female;
    private static String boy;
    private static String girl;
    private static String woman;
    private static String man;
    private static String lady;
    private static String gentleman;
    private static String hermaphrodite;
    private static String both;
    private static String ladyBoy;
    private static String lady_boy;
    private static String call_me;
    private final ArrayList<String> resultsRecognition;
    private final Locale locale;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = TutorialHelper.class.getSimpleName();

    public TutorialHelper(Context context, SupportedLanguage supportedLanguage, ArrayList<String> arrayList) {
        this.resultsRecognition = arrayList;
        this.locale = supportedLanguage.getLocale();
        if (sausage != null) {
            if (this.DEBUG) {
                MyLog.i(this.CLS_NAME, "strings initialised");
            }
        } else {
            if (this.DEBUG) {
                MyLog.i(this.CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources aVar) {
        sausage = aVar.getString(R.string.sausage);
        bean = aVar.getString(R.string.bean);
        bean_sic = aVar.getString(R.string.bean_sic);
        chip = aVar.getString(R.string.chip);
        one = aVar.getString(R.string.one);
        two = aVar.getString(R.string.two);
        three = aVar.getString(R.string.three);
        four = aVar.getString(R.string.four);
        five = aVar.getString(R.string.five);
        numeric_1_2_3_4_5 = aVar.getString(R.string.numeric_1_2_3_4_5);
        male = aVar.getString(R.string.male);
        male_sic = aVar.getString(R.string.male_sic);
        female = aVar.getString(R.string.female);
        boy = aVar.getString(R.string.boy);
        girl = aVar.getString(R.string.girl);
        woman = aVar.getString(R.string.woman);
        man = aVar.getString(R.string.man);
        lady = aVar.getString(R.string.lady);
        gentleman = aVar.getString(R.string.gentleman);
        hermaphrodite = aVar.getString(R.string.hermaphrodite);
        both = aVar.getString(R.string.both);
        ladyBoy = aVar.getString(R.string.ladyboy);
        lady_boy = aVar.getString(R.string.lady_boy);
        call_me = aVar.getString(R.string.call_me_);
        address_me_as = aVar.getString(R.string.address_me_as_);
        addressed_as = aVar.getString(R.string.addressed_as_);
        my_name_is = aVar.getString(R.string.my_name_is_);
        be_called = aVar.getString(R.string.be_called_);
        be_known_as = aVar.getString(R.string.be_known_as_);
    }

    public Pair<Boolean, Boolean> getAnswerOneResult() {
        if (this.DEBUG) {
            MyLog.i(this.CLS_NAME, "getAnswerOneResult");
        }
        if (UtilsList.notNaked(this.resultsRecognition)) {
            for (String s : this.resultsRecognition) {
                String lowerCase = s.toLowerCase(this.locale);
                if (lowerCase.contains(sausage) || lowerCase.contains(bean_sic) || lowerCase.contains(bean) || lowerCase.contains(chip)) {
                    return (lowerCase.contains(sausage) && (lowerCase.contains(bean_sic) || lowerCase.contains(bean)) && lowerCase.contains(chip)) ? new Pair<>(true, true) : new Pair<>(true, false);
                }
            }
        } else if (this.DEBUG) {
            MyLog.w(this.CLS_NAME, "voice data naked");
        }
        return new Pair<>(false, false);
    }

    public Pair<Boolean, Boolean> getAnswerTwoResult() {
        if (this.DEBUG) {
            MyLog.i(this.CLS_NAME, "getAnswerTwoResult");
        }
        if (UtilsList.notNaked(this.resultsRecognition)) {
            String trim = numeric_1_2_3_4_5.replaceAll("\\s", "").trim();
            for (String s : this.resultsRecognition) {
                String lowerCase = s.toLowerCase(this.locale);
                if (lowerCase.contains(one) || lowerCase.contains(two) || lowerCase.contains(three) || lowerCase.contains(four) || lowerCase.contains(five) || lowerCase.contains(numeric_1_2_3_4_5) || lowerCase.contains(trim)) {
                    return ((lowerCase.contains(one) && lowerCase.contains(two) && lowerCase.contains(three) && lowerCase.contains(four) && lowerCase.contains(five)) || lowerCase.contains(numeric_1_2_3_4_5) || lowerCase.contains(trim)) ? new Pair<>(true, true) : new Pair<>(true, false);
                }
            }
        } else if (this.DEBUG) {
            MyLog.w(this.CLS_NAME, "voice data naked");
        }
        return new Pair<>(false, false);
    }

    public Pair<Gender, Boolean> getAnswerThreeResult() {
        if (this.DEBUG) {
            MyLog.i(this.CLS_NAME, "getAnswerThreeResult");
        }
        if (UtilsList.notNaked(this.resultsRecognition)) {
            for (String s : this.resultsRecognition) {
                String lowerCase = s.toLowerCase(this.locale);
                if (lowerCase.contains(hermaphrodite) || lowerCase.contains(ladyBoy) || lowerCase.contains(lady_boy) || lowerCase.contains(both)) {
                    return new Pair<>(Gender.UNDEFINED, true);
                }
                if (lowerCase.contains(female) || lowerCase.contains(girl) || lowerCase.contains(lady) || lowerCase.contains(woman)) {
                    return new Pair<>(Gender.FEMALE, false);
                }
                if (lowerCase.contains(male) || lowerCase.contains(male_sic) || lowerCase.contains(boy) || lowerCase.contains(gentleman) || lowerCase.contains(man)) {
                    return new Pair<>(Gender.MALE, false);
                }
            }
        } else if (this.DEBUG) {
            MyLog.w(this.CLS_NAME, "voice data naked");
        }
        return new Pair<>(Gender.UNDEFINED, false);
    }

    public String detectCallable() {
        if (UtilsList.notNaked(this.resultsRecognition)) {
            for (String s : this.resultsRecognition) {
                String trim = s.toLowerCase(this.locale).trim();
                if (trim.contains(call_me)) {
                    String[] split = trim.split(call_me);
                    if (split.length > 1) {
                        return split[1].trim();
                    }
                } else if (trim.contains(address_me_as)) {
                    String[] split2 = trim.split(address_me_as);
                    if (split2.length > 1) {
                        return split2[1].trim();
                    }
                } else if (trim.contains(my_name_is)) {
                    String[] split3 = trim.split(my_name_is);
                    if (split3.length > 1) {
                        return split3[1].trim();
                    }
                } else if (trim.contains(addressed_as)) {
                    String[] split4 = trim.split(addressed_as);
                    if (split4.length > 1) {
                        return split4[1].trim();
                    }
                } else if (trim.contains(be_called)) {
                    String[] split5 = trim.split(be_called);
                    if (split5.length > 1) {
                        return split5[1].trim();
                    }
                } else if (trim.contains(be_known_as)) {
                    String[] split6 = trim.split(be_known_as);
                    if (split6.length > 1) {
                        return split6[1].trim();
                    }
                }
            }
        }
        return null;
    }
}
