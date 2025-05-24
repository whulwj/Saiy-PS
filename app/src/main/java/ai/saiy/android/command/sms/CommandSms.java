package ai.saiy.android.command.sms;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Collections;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.command.time.TimeHelper;
import ai.saiy.android.contacts.Contact;
import ai.saiy.android.contacts.ContactHelper;
import ai.saiy.android.database.helper.DatabaseHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandSms {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandSms.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, this.then);
        }
        return outcome;
    }

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        if (!ai.saiy.android.permissions.PermissionHelper.checkSMSReadPermissions(context, cr.getBundle())) {
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(SaiyRequestParams.SILENCE);
            return returnOutcome(outcome);
        }
        if (!ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissions(context, cr.getBundle())) {
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(SaiyRequestParams.SILENCE);
            return returnOutcome(outcome);
        }
        final ArrayList<String> senders = new ArrayList<>();
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            senders.addAll(new Sms(supportedLanguage).sort(context, voiceData));
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        final ContactHelper contactHelper = new ContactHelper();
        final SmsHelper smsHelper = new SmsHelper();
        if (!UtilsList.notNaked(senders)) {
            Message message = smsHelper.getMostRecentMessage(context);
            if (message == null) {
                outcome.setUtterance(context.getString(R.string.sms_error_4));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            }
            if (!UtilsString.notNaked(message.getBody())) {
                outcome.setUtterance(context.getString(R.string.sms_error_1));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getBody : " + message.getBody());
                MyLog.i(CLS_NAME, "getAddress : " + message.getAddress());
                MyLog.i(CLS_NAME, "getPerson : " + message.getPerson());
                MyLog.i(CLS_NAME, "getDate : " + message.getDate());
                MyLog.i(CLS_NAME, "isRead : " + message.isRead());
            }
            final StringBuilder sb = new StringBuilder();
            sb.append(PersonalityResponse.getSmsResponsePart1(context, supportedLanguage, new TimeHelper().getDate(context, supportedLanguage, message.getDate()), new TimeHelper().getTime(context, message.getDate())));
            sb.append(", ");
            sb.append(context.getString(R.string.from));
            sb.append(XMLResultsHandler.SEP_SPACE);
            final String sender = smsHelper.resolveSender(context, message.getPerson(), message.getAddress());
            if (UtilsString.notNaked(sender)) {
                sb.append(sender);
                sb.append(", ");
            } else {
                sb.append(context.getString(R.string.an_unknown_sender));
                sb.append(", ");
                new DatabaseHelper().deleteContacts(context);
            }
            sb.append(context.getString(R.string.saying));
            sb.append(". ");
            sb.append(message.getBody());
            outcome.setUtterance(sb.toString());
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(outcome);
        }

        final ArrayList<Contact> contacts = contactHelper.getContactFromName(context, supportedLanguage.getLocale(), senders, false);
        if (!UtilsList.notNaked(contacts)) {
            Message message = smsHelper.getMostRecentMessageByName(context, senders);
            if (message == null) {
                outcome.setUtterance(PersonalityResponse.getContactNotDetectedError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                new DatabaseHelper().deleteContacts(context);
                return returnOutcome(outcome);
            }
            if (!UtilsString.notNaked(message.getBody())) {
                outcome.setUtterance(context.getString(R.string.sms_error_1));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getBody : " + message.getBody());
                MyLog.i(CLS_NAME, "getAddress : " + message.getAddress());
                MyLog.i(CLS_NAME, "getPerson : " + message.getPerson());
                MyLog.i(CLS_NAME, "getDate : " + message.getDate());
                MyLog.i(CLS_NAME, "isRead : " + message.isRead());
            }
            outcome.setUtterance(PersonalityResponse.getSmsContactResponsePart1(context, supportedLanguage, message.getAddress()) + ", " + PersonalityResponse.getContactResponsePart2(context, supportedLanguage, new TimeHelper().getDate(context, supportedLanguage, message.getDate()), new TimeHelper().getTime(context, message.getDate())) + ", " + context.getString(R.string.saying) + ". " + message.getBody());
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(outcome);
        }
        if (DEBUG) {
            for (Contact contact : contacts) {
                MyLog.i(CLS_NAME, "contact name: " + contact.getName());
                MyLog.i(CLS_NAME, "contact id: " + contact.getID());
                MyLog.i(CLS_NAME, "contact has number: " + contact.hasPhoneNumber());
            }
        }
        final Pair<Boolean, Contact> contactPair = contactHelper.getContact(context, contacts, Contact.Weighting.NUMBER, ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM);
        if (!contactPair.first) {
            Message message = smsHelper.getMostRecentMessageByName(context, senders);
            if (message == null) {
                outcome.setUtterance(context.getString(R.string.sms_error_5));
                outcome.setOutcome(Outcome.FAILURE);
                new DatabaseHelper().deleteContacts(context);
                return returnOutcome(outcome);
            }
            if (!UtilsString.notNaked(message.getBody())) {
                outcome.setUtterance(context.getString(R.string.sms_error_1));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getBody : " + message.getBody());
                MyLog.i(CLS_NAME, "getAddress : " + message.getAddress());
                MyLog.i(CLS_NAME, "getPerson : " + message.getPerson());
                MyLog.i(CLS_NAME, "getDate : " + message.getDate());
                MyLog.i(CLS_NAME, "isRead : " + message.isRead());
            }
            outcome.setUtterance(PersonalityResponse.getSmsContactResponsePart1(context, supportedLanguage, message.getAddress()) + ", " + PersonalityResponse.getContactResponsePart2(context, supportedLanguage, new TimeHelper().getDate(context, supportedLanguage, message.getDate()), new TimeHelper().getTime(context, message.getDate())) + ", " + context.getString(R.string.saying) + ". " + message.getBody());
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(outcome);
        }
        final Contact contact = contactPair.second;
        final ArrayList<String> rawIDs = contactHelper.getRawIdArray(context, contact.getName());
        if (!UtilsList.notNaked(rawIDs)) {
            outcome.setUtterance(PersonalityResponse.getNoContactForSms(context, supportedLanguage, contact.getName()));
            outcome.setOutcome(Outcome.FAILURE);
            new DatabaseHelper().deleteContacts(context);
            return returnOutcome(outcome);
        }
        Message message;
        if (ai.saiy.android.utils.SPH.getSmsIdFix(context)) {
            String normalisedNumber = contactHelper.getNormalisedNumber(context, contact.getNumber());
            if (UtilsString.notNaked(normalisedNumber)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "normalisedNumber : " + normalisedNumber);
                }
                rawIDs.clear();
                message = smsHelper.getMostRecentMessageByNumber(context, Collections.singletonList(normalisedNumber));
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "normalisedNumber naked");
                }
                message = null;
            }
        } else {
            message = smsHelper.getMostRecentContactMessage(context, rawIDs);
        }
        if (message == null || !UtilsString.notNaked(message.getBody())) {
            message = smsHelper.getMostRecentMessageByName(context, senders);
        }
        if (message == null) {
            outcome.setUtterance(PersonalityResponse.getNoRecordForSms(context, supportedLanguage, contact.getName()));
            outcome.setOutcome(Outcome.FAILURE);
            new DatabaseHelper().deleteContacts(context);
            return returnOutcome(outcome);
        }
        if (!UtilsString.notNaked(message.getBody())) {
            outcome.setUtterance(context.getString(R.string.sms_error_1));
            outcome.setOutcome(Outcome.FAILURE);
            new DatabaseHelper().deleteContacts(context);
            return returnOutcome(outcome);
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getBody : " + message.getBody());
            MyLog.i(CLS_NAME, "getAddress : " + message.getAddress());
            MyLog.i(CLS_NAME, "getPerson : " + message.getPerson());
            MyLog.i(CLS_NAME, "getDate : " + message.getDate());
            MyLog.i(CLS_NAME, "isRead : " + message.isRead());
        }
        outcome.setUtterance(PersonalityResponse.getSmsContactResponsePart1(context, supportedLanguage, contact.getName()) + ", " + PersonalityResponse.getContactResponsePart2(context, supportedLanguage, new TimeHelper().getDate(context, supportedLanguage, message.getDate()), new TimeHelper().getTime(context, message.getDate())) + ", " + context.getString(R.string.saying) + ". " + message.getBody());
        outcome.setOutcome(Outcome.SUCCESS);
        return returnOutcome(outcome);
    }
}
