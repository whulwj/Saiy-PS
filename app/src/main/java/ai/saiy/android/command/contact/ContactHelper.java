package ai.saiy.android.command.contact;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.command.call.CallHelper;
import ai.saiy.android.contacts.UtilsContact;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ContactHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ContactHelper.class.getSimpleName();

    private final Context mContext;
    private final CommandContactValues commandContactValues;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;

    public ContactHelper(Context context, CommandContactValues commandContactValues, SupportedLanguage supportedLanguage, Locale vrLocale, Locale ttsLocale) {
        this.mContext = context;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = supportedLanguage;
        this.commandContactValues = commandContactValues;
    }

    public ContactHelper(Context context, Bundle bundle, Locale vrLocale, Locale ttsLocale) {
        this.mContext = context;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = (SupportedLanguage) bundle.getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);
        this.commandContactValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
    }

    private void sleep() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sleep");
        }
        Schedulers.computation().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "timerTask: action contact");
                }
                if (commandContactValues != null && commandContactValues.getType() == CommandContactValues.Type.CALL) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "CALL");
                    }
                    LocalRequest localRequest;
                    switch (commandContactValues.getCallType()) {
                        case HOME:
                        case WORK:
                        case MOBILE:
                            SPH.setLastContactUpdate(mContext, System.currentTimeMillis() - 170000);
                            if (CallHelper.callNumber(mContext, commandContactValues.getContact().getNumber())) {
                                break;
                            }
                            localRequest = new LocalRequest(mContext);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getCallingNumberError(mContext, sl, mContext.getString(R.string.calling)));
                            localRequest.setQueueType(TextToSpeech.QUEUE_ADD);
                            localRequest.execute();
                            UtilsContact.displayContact(mContext, commandContactValues.getContact().getID());
                            break;
                        case NUMBER:
                            SPH.setLastContactUpdate(mContext, System.currentTimeMillis() - 170000);
                            if (CallHelper.callNumber(mContext, commandContactValues.getRequiredNumber())) {
                                break;
                            }
                            localRequest = new LocalRequest(mContext);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getCallingNumberError(mContext, sl, mContext.getString(R.string.dialing)));
                            localRequest.setQueueType(TextToSpeech.QUEUE_ADD);
                            localRequest.execute();
                            break;
                        case SKYPE:
                            if (UtilsContact.skypeContact(mContext, commandContactValues.getContact().getNumber())) {
                                break;
                            }
                            localRequest = new LocalRequest(mContext);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getCallingNumberError(mContext, sl, mContext.getString(R.string.skyping)));
                            localRequest.setQueueType(TextToSpeech.QUEUE_ADD);
                            localRequest.execute();
                            UtilsContact.displayContact(mContext, commandContactValues.getContact().getID());
                            break;
                        default:
                            break;
                    }
                }
            }
        }, commandContactValues.getContact() != null ? 3000L : 5500L, TimeUnit.MILLISECONDS);
    }

    public void perform(boolean shouldAction) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "perform");
        }
        switch (commandContactValues.getType()) {
            case CALL:
                sleep();
                if (shouldAction) {
                    final ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, commandContactValues.getActionUtterance());
                    localRequest.execute();
                }
                break;
            case UNKNOWN:
            case DISPLAY:
            case EDIT:
            case NAVIGATE:
            case TEXT:
            case EMAIL:
            default:
                break;
        }
    }
}
