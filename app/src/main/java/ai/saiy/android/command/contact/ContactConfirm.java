package ai.saiy.android.command.contact;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.SpeechRecognizer;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.command.sms.SmsHelper;
import ai.saiy.android.contacts.Contact;
import ai.saiy.android.contacts.UtilsContact;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.PositiveNegative;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ContactConfirm {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ContactConfirm.class.getSimpleName();

    private final Context mContext;
    private final Bundle bundle;
    private final ConfirmType confirmType;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final ArrayList<String> resultsRecognition;

    public enum ConfirmType {
        CALL_CONFIRM,
        CALL_TYPE,
        TEXT,
        TEXT_CONTENT,
        EMAIL,
        EMAIL_CONTENT,
        EMAIL_SUBJECT
    }

    public ContactConfirm(Context context, Bundle bundle, ConfirmType confirmType, Locale vrLocale, Locale ttsLocale) {
        this.mContext = context;
        this.bundle = bundle;
        this.confirmType = confirmType;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = (SupportedLanguage) bundle.getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);
        this.resultsRecognition = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    }

    private Choice getChoice(ArrayList<Choice> arrayList, int type) {
        Choice choice = null;
        for (Choice c : arrayList) {
            if (c.getType() == type) {
                choice = c;
                if (c.isPrimary()) {
                    break;
                }
            }
        }
        return choice;
    }

    private int getType() {
        final Locale locale = sl.getLocale();
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(mContext, sl);
        final String work = sr.getString(R.string.work);
        final String office = sr.getString(R.string.office);
        final String home = sr.getString(R.string.home);
        final String mobile = sr.getString(R.string.mobile);
        final String cancel_ = sr.getString(R.string.cancel_).trim();
        final Pattern pWork = Pattern.compile(".*\\b" + work + "\\b.*");
        final Pattern pOffice = Pattern.compile(".*\\b" + office + "\\b.*");
        final Pattern pHome = Pattern.compile(".*\\b" + home + "\\b.*");
        final Pattern pMobile = Pattern.compile(".*\\b" + mobile + "\\b.*");
        final Pattern pCancel_ = Pattern.compile(".*\\b" + cancel_ + "\\b.*");
        String vdLower;
        for (String s : resultsRecognition) {
            vdLower = s.toLowerCase(locale).trim();
            if (pWork.matcher(vdLower).matches() || pOffice.matcher(vdLower).matches()) {
                return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
            }
            if (pHome.matcher(vdLower).matches()) {
                return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
            }
            if (pMobile.matcher(vdLower).matches()) {
                return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            }
            if (pCancel_.matcher(vdLower).matches()) {
                return ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
            }
        }
        return ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
    }

    public void confirm() {
        PositiveNegative positiveNegative;
        PositiveNegative.Result result;
        CommandContactValues commandContactValues;
        Contact contact;
        ai.saiy.android.service.helper.LocalRequest localRequest;
        switch (confirmType) {
            case CALL_CONFIRM:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "CALL_CONFIRM");
                }
                positiveNegative = new PositiveNegative();
                result = positiveNegative.resolve(mContext, resultsRecognition, sl);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "result: " + result.name());
                    MyLog.i(CLS_NAME, "confidence: " + positiveNegative.getConfidence());
                }
                switch (result) {
                    case UNRESOLVED:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "UNRESOLVED");
                        }
                        if (bundle.containsKey(LocalRequest.EXTRA_VR_RETRY)) {
                            localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getCallConfirmationMisHeard(mContext, sl));
                            localRequest.execute();
                        } else {
                            bundle.putBoolean(LocalRequest.EXTRA_VR_RETRY, true);
                            localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, PersonalityResponse.getCallConfirmationRepeat(mContext, sl));
                            localRequest.execute();
                        }
                        break;
                    case POSITIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "POSITIVE");
                        }
                        new ContactHelper(mContext, bundle, vrLocale, ttsLocale).perform(true);
                        break;
                    case NEGATIVE:
                    case CANCEL:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "NEGATIVE/CANCEL");
                        }
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                        localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                        localRequest.execute();
                        break;
                    default:
                        break;
                }
                break;
            case CALL_TYPE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "CALL_TYPE");
                }
                commandContactValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                contact = commandContactValues.getContact();
                ArrayList<Choice> choiceArray = commandContactValues.getChoiceArray();
                switch (getType()) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        contact.setNumber(getChoice(choiceArray, ContactsContract.CommonDataKinds.Phone.TYPE_HOME).getNumber());
                        commandContactValues.setCallType(CommandContactValues.CallType.HOME);
                        commandContactValues.setActionUtterance(mContext.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.home));
                        bundle.putParcelable(LocalRequest.EXTRA_OBJECT, commandContactValues);
                        new ContactHelper(mContext, bundle, vrLocale, ttsLocale).perform(true);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        contact.setNumber(getChoice(choiceArray, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).getNumber());
                        commandContactValues.setCallType(CommandContactValues.CallType.MOBILE);
                        commandContactValues.setActionUtterance(mContext.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.mobile));
                        bundle.putParcelable(LocalRequest.EXTRA_OBJECT, commandContactValues);
                        new ContactHelper(mContext, bundle, vrLocale, ttsLocale).perform(true);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        contact.setNumber(getChoice(choiceArray, ContactsContract.CommonDataKinds.Phone.TYPE_WORK).getNumber());
                        commandContactValues.setCallType(CommandContactValues.CallType.WORK);
                        commandContactValues.setActionUtterance(mContext.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.work));
                        bundle.putParcelable(LocalRequest.EXTRA_OBJECT, commandContactValues);
                        new ContactHelper(mContext, bundle, vrLocale, ttsLocale).perform(true);
                        break;
                    default:
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                        localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                        localRequest.execute();
                        break;
                }
                break;
            case TEXT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TEXT");
                }
                positiveNegative = new PositiveNegative();
                result = positiveNegative.resolve(mContext, resultsRecognition, sl);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "result: " + result.name());
                    MyLog.i(CLS_NAME, "confidence: " + positiveNegative.getConfidence());
                }
                switch (result) {
                    case UNRESOLVED:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "UNRESOLVED");
                        }
                        if (bundle.containsKey(LocalRequest.EXTRA_VR_RETRY)) {
                            localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getTextConfirmationMisHeard(mContext, sl));
                            localRequest.execute();
                        } else {
                            bundle.putBoolean(LocalRequest.EXTRA_VR_RETRY, true);
                            localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, PersonalityResponse.getCallConfirmationRepeat(mContext, sl));
                            localRequest.execute();
                        }
                        break;
                    case POSITIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "POSITIVE");
                        }
                        commandContactValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                        contact = commandContactValues.getContact();
                        SmsHelper.sendSMS(mContext, contact.getNumber(), contact.getGeneric(), true);
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getMessageProofReadAcknowledge(mContext, sl));
                        localRequest.execute();
                        break;
                    case NEGATIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "NEGATIVE");
                        }
                        commandContactValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                        contact = commandContactValues.getContact();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getName: " + contact.getName());
                            MyLog.i(CLS_NAME, "getRequiredNumber: " + contact.getNumber());
                            MyLog.i(CLS_NAME, "getGeneric: " + contact.getGeneric());
                        }
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                        if (SmsHelper.sendSMS(mContext, contact.getNumber(), contact.getGeneric(), false)) {
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getMessageSentConfirmation(mContext, sl, contact.getName()));
                        } else {
                            SmsHelper.sendSMS(mContext, contact.getNumber(), contact.getGeneric(), true);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getNoPermissionForSms(mContext, sl));
                            Schedulers.computation().scheduleDirect(new Runnable() {
                                @Override
                                public void run() {
                                    ai.saiy.android.permissions.PermissionHelper.checkPhoneStatePermission(mContext);
                                }
                            }, 2000L, TimeUnit.MILLISECONDS);
                        }
                        localRequest.execute();
                        break;
                    case CANCEL:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "CANCEL");
                        }
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                        localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                        localRequest.execute();
                        break;
                    default:
                        break;
                }
                break;
            case TEXT_CONTENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TEXT_CONTENT");
                }
                if (new ai.saiy.android.command.cancel.Cancel(sl, new ai.saiy.android.localisation.SaiyResources(mContext, sl)).detectCancel(resultsRecognition)) {
                    localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                    localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                    localRequest.execute();
                    break;
                }
                commandContactValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                contact = commandContactValues.getContact();
                if (SmsHelper.isLengthWithinMax(resultsRecognition.get(0))) {
                    localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                    contact.setGeneric(UtilsString.convertProperCase(SmsHelper.replaceMarks(mContext, resultsRecognition.get(0)), sl.getLocale()));
                    commandContactValues.setContact(contact);
                    commandContactValues.setConfirmType(ConfirmType.TEXT);
                    localRequest.setParcelableObject(commandContactValues);
                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, PersonalityResponse.getSmsConfirmation(mContext, sl, contact.getName(), contact.getGeneric()));
                    localRequest.setCondition(Condition.CONDITION_CONTACT);
                } else {
                    localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                    SmsHelper.sendSMS(mContext, contact.getNumber(), UtilsString.convertProperCase(SmsHelper.replaceMarks(mContext, resultsRecognition.get(0)), sl.getLocale()), true);
                    localRequest.setCondition(Condition.CONDITION_NONE);
                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(mContext, sl, R.string.error_message_length));
                }
                localRequest.execute();
                break;
            case EMAIL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "EMAIL");
                }
                break;
            case EMAIL_CONTENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "EMAIL_CONTENT");
                }
                if (new ai.saiy.android.command.cancel.Cancel(sl, new ai.saiy.android.localisation.SaiyResources(mContext, sl)).detectCancel(resultsRecognition)) {
                    localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                    localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                    localRequest.execute();
                    break;
                }
                commandContactValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                contact = commandContactValues.getContact();
                contact.setGeneric(UtilsString.convertProperCase(SmsHelper.replaceMarks(mContext, resultsRecognition.get(0)), sl.getLocale()));
                commandContactValues.setContact(contact);
                commandContactValues.setConfirmType(ConfirmType.EMAIL_SUBJECT);
                localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                localRequest.setParcelableObject(commandContactValues);
                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, PersonalityResponse.getEmailConfirmation(mContext, sl, contact.getName(), contact.getGeneric()));
                localRequest.setCondition(Condition.CONDITION_CONTACT);
                localRequest.execute();
                break;
            case EMAIL_SUBJECT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "EMAIL_SUBJECT");
                }
                if (new ai.saiy.android.command.cancel.Cancel(sl, new ai.saiy.android.localisation.SaiyResources(mContext, sl)).detectCancel(resultsRecognition)) {
                    localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                    localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                    localRequest.execute();
                    break;
                }
                final String messageProofReadAcknowledge = PersonalityResponse.getMessageProofReadAcknowledge(mContext, sl);
                String utterance;
                if (ai.saiy.android.utils.SPH.getEmailAutoVerbose(mContext) <= 2) {
                    ai.saiy.android.utils.SPH.autoIncreaseEmailAutoVerbose(mContext);
                    utterance = messageProofReadAcknowledge + ". " + PersonalityResponse.getEmailProofReadVerbose(mContext, sl);
                } else {
                    utterance = messageProofReadAcknowledge;
                }
                localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                commandContactValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, !UtilsContact.sendEmail(mContext, commandContactValues.getContact().getEmailAddress(), UtilsString.convertProperCase(resultsRecognition.get(0), sl.getLocale()), commandContactValues.getContact().getGeneric(), false, null) ? "There was a problem opening your email application." : utterance);
                localRequest.execute();
                break;
            default:
                break;
        }
    }
}
