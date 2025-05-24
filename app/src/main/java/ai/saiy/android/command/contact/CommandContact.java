package ai.saiy.android.command.contact;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.sms.SmsHelper;
import ai.saiy.android.command.call.CallHelper;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.command.navigation.UtilsNavigation;
import ai.saiy.android.contacts.UtilsContact;
import ai.saiy.android.database.helper.DatabaseHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandContact {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandContact.class.getSimpleName();

    private Choice getChoice(ArrayList<Choice> choices, int type) {
        for (Choice next : choices) {
            if (next.getType() == type && next.isPrimary()) {
                return next;
            }
        }
        return choices.get(0);
    }

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, CommandRequest cr) {
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        outcome.setCondition(Condition.CONDITION_NONE);
        final ai.saiy.android.contacts.ContactHelper contactHelper = new ai.saiy.android.contacts.ContactHelper();
        final CommandContactValues commandContactValues = new Contact(supportedLanguage).sort(context, voiceData);
        if (commandContactValues.getCallType() == CommandContactValues.CallType.NUMBER) {
            outcome.setOutcome(Outcome.SUCCESS);
            if (ai.saiy.android.permissions.PermissionHelper.checkTelephonyGroupPermissions(context, cr.getBundle())) {
                final String vdTrimmed = commandContactValues.getVoiceDataTrimmed().get(0);
                outcome.setUtterance(context.getString(R.string.dialing) + XMLResultsHandler.SEP_SPACE + vdTrimmed.replaceAll(".(?=.)", "$0 ").trim());
                commandContactValues.setRequiredNumber(vdTrimmed);
                new ContactHelper(context, commandContactValues, supportedLanguage, SPH.getVRLocale(context), SPH.getTTSLocale(context)).perform(false);
            } else {
                outcome.setUtterance(SaiyRequestParams.SILENCE);
                CallHelper.dialNumber(context, commandContactValues.getVoiceDataTrimmed().get(0));
            }
            return outcome;
        }
        if (ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissions(context, cr.getBundle())) {
            final boolean precise = commandContactValues.getType() == CommandContactValues.Type.EMAIL || commandContactValues.getType() == CommandContactValues.Type.TEXT;
            if (DEBUG) {
                MyLog.i(CLS_NAME, "using precision: " + precise);
            }
            final ArrayList<ai.saiy.android.contacts.Contact> contacts = contactHelper.getContactFromName(context, supportedLanguage.getLocale(), commandContactValues.getVoiceDataTrimmed(), precise);
            if (UtilsList.notNaked(contacts)) {
                Pair<Boolean, ai.saiy.android.contacts.Contact> contactPair;
                ai.saiy.android.contacts.Contact contact;
                String confirmationUtterance;
                switch (commandContactValues.getType()) {
                    case DISPLAY:
                        contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.NONE, ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM);
                        if (!contactPair.first) {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                        } else {
                            contact = contactPair.second;
                            if (!UtilsContact.displayContact(context, contact.getID())) {
                                outcome.setOutcome(Outcome.FAILURE);
                                outcome.setUtterance(context.getString(R.string.error_loading_contact));
                            } else {
                                outcome.setOutcome(Outcome.SUCCESS);
                                outcome.setUtterance(context.getString(R.string.displayed) + XMLResultsHandler.SEP_SPACE + contact.getName());
                            }
                        }
                        break;
                    case EDIT:
                        contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
                        if (!contactPair.first) {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                        } else {
                            contact = contactPair.second;
                            if (!UtilsContact.displayContact(context, contact.getID())) {
                                outcome.setOutcome(Outcome.FAILURE);
                                outcome.setUtterance(context.getString(R.string.error_loading_contact));
                            } else {
                                outcome.setOutcome(Outcome.SUCCESS);
                                outcome.setUtterance(context.getString(R.string.editing) + XMLResultsHandler.SEP_SPACE + contact.getName());
                            }
                        }
                        break;
                    case CALL:
                        if (ai.saiy.android.permissions.PermissionHelper.checkTelephonyGroupPermissions(context, cr.getBundle())) {
                            final boolean needCallConfirmation = SPH.getCallConfirmation(context);
                            switch (commandContactValues.getCallType()) {
                                case UNKNOWN:
                                    ArrayList<Choice> choices = contactHelper.getChoices(context, contacts);
                                    if (choices.isEmpty()) {
                                        outcome.setOutcome(Outcome.SUCCESS);
                                        outcome.setUtterance(contacts.get(0).getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.error_no_numbers));
                                        UtilsContact.displayContact(context, contacts.get(0).getID());
                                    } else {
                                        outcome.setOutcome(Outcome.SUCCESS);
                                        commandContactValues.setConfirmType(ContactConfirm.ConfirmType.CALL_TYPE);
                                        final @Choice.CombinedType int combinedType = Choice.getCombinedType(choices);
                                        final String name = contacts.get(0).getName();
                                        String where;
                                        final String callConfirmation = PersonalityResponse.getCallConfirmation(context, supportedLanguage) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.call) + XMLResultsHandler.SEP_SPACE + name;
                                        switch (combinedType) {
                                            case Choice.MOBILE:
                                                where = context.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mobile);
                                                commandContactValues.setConfirmType(ContactConfirm.ConfirmType.CALL_CONFIRM);
                                                commandContactValues.setCallType(CommandContactValues.CallType.MOBILE);
                                                commandContactValues.setActionUtterance(context.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + name + XMLResultsHandler.SEP_SPACE + context.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mobile));
                                                contacts.get(0).setNumber(getChoice(choices, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).getNumber());
                                                break;
                                            case Choice.HOME:
                                                where = context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home);
                                                commandContactValues.setConfirmType(ContactConfirm.ConfirmType.CALL_CONFIRM);
                                                commandContactValues.setCallType(CommandContactValues.CallType.HOME);
                                                commandContactValues.setActionUtterance(context.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + name + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home));
                                                contacts.get(0).setNumber(getChoice(choices, ContactsContract.CommonDataKinds.Phone.TYPE_HOME).getNumber());
                                                break;
                                            case Choice.WORK:
                                                where = context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work);
                                                commandContactValues.setConfirmType(ContactConfirm.ConfirmType.CALL_CONFIRM);
                                                commandContactValues.setCallType(CommandContactValues.CallType.WORK);
                                                commandContactValues.setActionUtterance(context.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + name + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work));
                                                contacts.get(0).setNumber(getChoice(choices, ContactsContract.CommonDataKinds.Phone.TYPE_WORK).getNumber());
                                                break;
                                            case Choice.MOBILE_OR_HOME:
                                                where = context.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mobile) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.or) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home);
                                                break;
                                            case Choice.MOBILE_OR_WORK:
                                                where = context.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mobile) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.or) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work);
                                                break;
                                            case Choice.MOBILE_OR_HOME_OR_WORK:
                                                where = context.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mobile) + ", " + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.or) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work);
                                                break;
                                            case Choice.HOME_OR_WORK:
                                                where = context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.or) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work);
                                                break;
                                            default:
                                                where = "";
                                                break;
                                        }
                                        commandContactValues.setChoiceArray(choices);
                                        commandContactValues.setContact(contacts.get(0));
                                        outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                        outcome.setCondition(Condition.CONDITION_CONTACT);
                                        confirmationUtterance = callConfirmation + XMLResultsHandler.SEP_SPACE + where;
                                        outcome.setUtterance(confirmationUtterance);
                                        commandContactValues.setConfirmationUtterance(confirmationUtterance);
                                    }
                                    break;
                                case HOME:
                                    contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.NUMBER, 1);
                                    contact = contactPair.second;
                                    if (!contactPair.first) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        if (contact == null) {
                                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                        } else {
                                            outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.text_default) + context.getString(R.string.home) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.number) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                            UtilsContact.displayContact(context, contact.getID());
                                        }
                                    } else {
                                        outcome.setOutcome(Outcome.SUCCESS);
                                        commandContactValues.setContact(contact);
                                        String actionUtterance = context.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home);
                                        if (!needCallConfirmation) {
                                            outcome.setUtterance(actionUtterance);
                                            new ContactHelper(context, commandContactValues, supportedLanguage, SPH.getVRLocale(context), SPH.getTTSLocale(context)).perform(false);
                                        } else {
                                            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                            outcome.setCondition(Condition.CONDITION_CONTACT);
                                            confirmationUtterance = PersonalityResponse.getCallConfirmation(context, supportedLanguage) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.call) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home);
                                            outcome.setUtterance(confirmationUtterance);
                                            commandContactValues.setConfirmationUtterance(confirmationUtterance);
                                            commandContactValues.setActionUtterance(actionUtterance);
                                        }
                                    }
                                    break;
                                case WORK:
                                    contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.NUMBER, 3);
                                    contact = contactPair.second;
                                    if (!contactPair.first) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        if (contact == null) {
                                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                        } else {
                                            outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.text_default) + context.getString(R.string.work) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.number) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                            UtilsContact.displayContact(context, contact.getID());
                                        }
                                    } else {
                                        outcome.setOutcome(Outcome.SUCCESS);
                                        commandContactValues.setContact(contact);
                                        String actionUtterance = context.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work);
                                        if (!needCallConfirmation) {
                                            outcome.setUtterance(actionUtterance);
                                            new ContactHelper(context, commandContactValues, supportedLanguage, SPH.getVRLocale(context), SPH.getTTSLocale(context)).perform(false);
                                        } else {
                                            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                            outcome.setCondition(Condition.CONDITION_CONTACT);
                                            confirmationUtterance = PersonalityResponse.getCallConfirmation(context, supportedLanguage) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.call) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work);
                                            outcome.setUtterance(confirmationUtterance);
                                            commandContactValues.setConfirmationUtterance(confirmationUtterance);
                                            commandContactValues.setActionUtterance(actionUtterance);
                                        }
                                    }
                                    break;
                                case MOBILE:
                                    contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.NUMBER, 2);
                                    contact = contactPair.second;
                                    if (!contactPair.first) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        if (contact == null) {
                                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                        } else {
                                            outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.text_default) + context.getString(R.string.mobile) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.number) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                            UtilsContact.displayContact(context, contact.getID());
                                        }
                                    } else {
                                        outcome.setOutcome(Outcome.SUCCESS);
                                        commandContactValues.setContact(contact);
                                        String actionUtterance = context.getString(R.string.calling) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mobile);
                                        if (!needCallConfirmation) {
                                            outcome.setUtterance(actionUtterance);
                                            new ContactHelper(context, commandContactValues, supportedLanguage, SPH.getVRLocale(context), SPH.getTTSLocale(context)).perform(false);
                                        } else {
                                            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                            outcome.setCondition(Condition.CONDITION_CONTACT);
                                            confirmationUtterance = PersonalityResponse.getCallConfirmation(context, supportedLanguage) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.call) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.on) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.their) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mobile);
                                            outcome.setUtterance(confirmationUtterance);
                                            commandContactValues.setConfirmationUtterance(confirmationUtterance);
                                            commandContactValues.setActionUtterance(actionUtterance);
                                        }
                                    }
                                    break;
                                case SKYPE:
                                    if (Installed.isPackageInstalled(context, Installed.PACKAGE_SKYPE)) {
                                        contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.IM, 3);
                                        contact = contactPair.second;
                                        if (!contactPair.first) {
                                            outcome.setOutcome(Outcome.FAILURE);
                                            if (contact == null) {
                                                outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                            } else {
                                                outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.text_default) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.skype_handle) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                                UtilsContact.displayContact(context, contact.getID());
                                            }
                                        } else {
                                            outcome.setOutcome(Outcome.SUCCESS);
                                            commandContactValues.setContact(contact);
                                            String actionUtterance = context.getString(R.string.skyping) + XMLResultsHandler.SEP_SPACE + contact.getName();
                                            if (!needCallConfirmation) {
                                                outcome.setUtterance(actionUtterance);
                                                new ContactHelper(context, commandContactValues, supportedLanguage, SPH.getVRLocale(context), SPH.getTTSLocale(context)).perform(false);
                                            } else {
                                                outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                                outcome.setCondition(Condition.CONDITION_CONTACT);
                                                confirmationUtterance = PersonalityResponse.getCallConfirmation(context, supportedLanguage) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.skype) + XMLResultsHandler.SEP_SPACE + contact.getName();
                                                outcome.setUtterance(confirmationUtterance);
                                                commandContactValues.setConfirmationUtterance(confirmationUtterance);
                                                commandContactValues.setActionUtterance(actionUtterance);
                                            }
                                        }
                                    } else {
                                        Install.showInstallLink(context, Installed.PACKAGE_SKYPE);
                                        outcome.setUtterance(PersonalityResponse.getSkypeInstallError(context, supportedLanguage));
                                        outcome.setOutcome(Outcome.FAILURE);
                                        return outcome;
                                    }
                            }
                        } else {
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(SaiyRequestParams.SILENCE);
                            return outcome;
                        }
                        break;
                    case NAVIGATE:
                        if (Installed.isPackageInstalled(context, Installed.PACKAGE_GOOGLE_MAPS)) {
                            switch (commandContactValues.getNavigationType()) {
                                case UNKNOWN:
                                    contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.ADDRESS, ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM);
                                    contact = contactPair.second;
                                    if (!(Boolean) contactPair.first) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        if (contact == null) {
                                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                        } else {
                                            outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.an) + context.getString(R.string.address) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                            UtilsContact.displayContact(context, contact.getID());
                                        }
                                    } else {
                                        if (!UtilsNavigation.navigateToAddress(context, contact.getAddress())) {
                                            outcome.setOutcome(Outcome.FAILURE);
                                            outcome.setUtterance(context.getString(R.string.error_navigation));
                                        } else {
                                            outcome.setOutcome(Outcome.SUCCESS);
                                            outcome.setUtterance(context.getString(R.string.navigating) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.to) + XMLResultsHandler.SEP_SPACE + contact.getName());
                                        }
                                    }
                                    break;
                                case HOME:
                                    contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.ADDRESS, 1);
                                    contact = contactPair.second;
                                    if (!contactPair.first) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        if (contact == null) {
                                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                        } else {
                                            outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.text_default) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.address) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                            UtilsContact.displayContact(context, contact.getID());
                                        }
                                    } else {
                                        if (!UtilsNavigation.navigateToAddress(context, contact.getAddress())) {
                                            outcome.setOutcome(Outcome.FAILURE);
                                            outcome.setUtterance(context.getString(R.string.error_navigation));
                                        } else {
                                            outcome.setOutcome(Outcome.SUCCESS);
                                            outcome.setUtterance(context.getString(R.string.navigating) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.to) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.home));
                                        }
                                    }
                                    break;
                                case WORK:
                                    contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.ADDRESS, 2);
                                    contact = contactPair.second;
                                    if (!contactPair.first) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        if (contact == null) {
                                            outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                        } else {
                                            outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.text_default) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.address) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                            UtilsContact.displayContact(context, contact.getID());
                                        }
                                    } else {
                                        outcome.setOutcome(Outcome.SUCCESS);
                                        outcome.setUtterance(context.getString(R.string.navigating) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.to) + XMLResultsHandler.SEP_SPACE + contact.getName() + XMLResultsHandler.SEP_SPACE + context.getString(R.string.at) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.work));
                                        UtilsNavigation.navigateToAddress(context, contact.getAddress());
                                    }
                                    break;
                            }
                        } else {
                            Install.showInstallLink(context, Installed.PACKAGE_GOOGLE_MAPS);
                            outcome.setUtterance(PersonalityResponse.getNavigationInstallError(context, supportedLanguage));
                            outcome.setOutcome(Outcome.FAILURE);
                            return outcome;
                        }
                        break;
                    case TEXT:
                        if (!ai.saiy.android.permissions.PermissionHelper.checkSMSSendPermissions(context, cr.getBundle())) {
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(SaiyRequestParams.SILENCE);
                        } else {
                            contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.NUMBER, 2);
                            contact = contactPair.second;
                            if (!contactPair.first) {
                                outcome.setOutcome(Outcome.FAILURE);
                                if (contact == null) {
                                    outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                                } else {
                                    outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.text_default) + context.getString(R.string.mobile) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.number) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                    UtilsContact.displayContact(context, contact.getID());
                                }
                            } else {
                                outcome.setOutcome(Outcome.SUCCESS);
                                if (!UtilsString.notNaked(contact.getGeneric())) {
                                    outcome.setUtterance(PersonalityResponse.getMessageContentRequest(context, supportedLanguage, contact.getName()));
                                    outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                    outcome.setCondition(Condition.CONDITION_CONTACT);
                                    commandContactValues.setConfirmType(ContactConfirm.ConfirmType.TEXT_CONTENT);
                                    commandContactValues.setRequiredNumber(contact.getNumber());
                                    commandContactValues.setContact(contact);
                                } else {
                                    String messageBody = getRawSms(context, supportedLanguage, contact.getGeneric());
                                    if (!messageBody.isEmpty()) {
                                        if (!SmsHelper.isLengthWithinMax(messageBody)) {
                                            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                                            outcome.setCondition(Condition.CONDITION_NONE);
                                            outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.error_message_length));
                                            SmsHelper.sendSMS(context, contact.getNumber(), UtilsString.convertProperCase(SmsHelper.replaceMarks(context, messageBody), supportedLanguage.getLocale()), true);
                                        } else {
                                            contact.setGeneric(UtilsString.convertProperCase(SmsHelper.replaceMarks(context, messageBody), supportedLanguage.getLocale()));
                                            commandContactValues.setContact(contact);
                                            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                            outcome.setCondition(Condition.CONDITION_CONTACT);
                                            commandContactValues.setConfirmType(ContactConfirm.ConfirmType.TEXT);
                                            outcome.setUtterance(PersonalityResponse.getSmsConfirmation(context, supportedLanguage, contact.getName(), contact.getGeneric()));
                                            commandContactValues.setRequiredNumber(contact.getNumber());
                                        }
                                    } else {
                                        outcome.setUtterance(PersonalityResponse.getMessageContentRequest(context, supportedLanguage, contact.getName()));
                                        outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                        outcome.setCondition(Condition.CONDITION_CONTACT);
                                        commandContactValues.setConfirmType(ContactConfirm.ConfirmType.TEXT_CONTENT);
                                        commandContactValues.setRequiredNumber(contact.getNumber());
                                        commandContactValues.setContact(contact);
                                    }
                                }
                            }
                        }
                        break;
                    case EMAIL:
                        contactPair = contactHelper.getContact(context, contacts, ai.saiy.android.contacts.Contact.Weighting.EMAIL, ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM);
                        contact = contactPair.second;
                        if (!contactPair.first) {
                            outcome.setOutcome(Outcome.FAILURE);
                            if (contact == null) {
                                outcome.setUtterance(context.getString(R.string.error_detecting_contact));
                            } else {
                                outcome.setUtterance(PersonalityResponse.getContactMissingData(context, supportedLanguage, contact.getName()) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.an) + context.getString(R.string.email) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.address) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.listed));
                                UtilsContact.displayContact(context, contact.getID());
                            }
                        } else {
                            outcome.setOutcome(Outcome.SUCCESS);
                            if (!UtilsString.notNaked(contact.getGeneric())) {
                                outcome.setUtterance(PersonalityResponse.getMessageContentRequest(context, supportedLanguage, contact.getName()));
                                outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                outcome.setCondition(Condition.CONDITION_CONTACT);
                                commandContactValues.setConfirmType(ContactConfirm.ConfirmType.EMAIL_CONTENT);
                                commandContactValues.setRequiredNumber(contact.getEmailAddress());
                                commandContactValues.setContact(contact);
                            } else {
                                String emailBody = getRawEmail(context, supportedLanguage, contact.getGeneric());
                                if (emailBody.isEmpty()) {
                                    outcome.setUtterance(PersonalityResponse.getMessageContentRequest(context, supportedLanguage, contact.getName()));
                                    outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                    outcome.setCondition(Condition.CONDITION_CONTACT);
                                    commandContactValues.setConfirmType(ContactConfirm.ConfirmType.EMAIL_CONTENT);
                                    commandContactValues.setRequiredNumber(contact.getEmailAddress());
                                    commandContactValues.setContact(contact);
                                } else {
                                    contact.setGeneric(UtilsString.convertProperCase(SmsHelper.replaceMarks(context, emailBody), supportedLanguage.getLocale()));
                                    commandContactValues.setContact(contact);
                                    outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                    outcome.setCondition(Condition.CONDITION_CONTACT);
                                    commandContactValues.setConfirmType(ContactConfirm.ConfirmType.EMAIL_SUBJECT);
                                    outcome.setUtterance(PersonalityResponse.getEmailConfirmation(context, supportedLanguage, contact.getName(), contact.getGeneric()));
                                    commandContactValues.setRequiredNumber(contact.getEmailAddress());
                                }
                            }
                        }
                        break;
                }
                outcome.setExtra(commandContactValues);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "no contacts or contact match");
                }
                String utterance;
                final int unknownContactVerbose = SPH.getUnknownContactVerbose(context);
                if (unknownContactVerbose >= 3) {
                    utterance = PersonalityResponse.getContactNotDetectedError(context, supportedLanguage);
                } else if (unknownContactVerbose == 0) {
                    SPH.autoIncreaseUnknownContactVerbose(context);
                    utterance = PersonalityResponse.getContactNotDetectedError(context, supportedLanguage) + ". " + ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.contact_not_detected_verbose);
                } else {
                    SPH.autoIncreaseUnknownContactVerbose(context);
                    utterance = PersonalityResponse.getContactNotDetectedError(context, supportedLanguage) + ". " + PersonalityResponse.getContactNotDetectedExtra(context, supportedLanguage);
                }
                outcome.setOutcome(Outcome.FAILURE);
                outcome.setUtterance(utterance);
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "missing contact permission");
            }
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(SaiyRequestParams.SILENCE);
        }
        if (outcome.getOutcome() == Outcome.FAILURE) {
            new DatabaseHelper().deleteContacts(context);
        }
        return outcome;
    }

    private static @NonNull String getRawSms(Context context, SupportedLanguage supportedLanguage, String generic) {
        String result = generic;
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        final String text = sr.getString(R.string.text);
        final String text_message = sr.getString(R.string.text_message);
        final String sms = sr.getString(R.string.sms);
        final String sms_message = sr.getString(R.string.sms_message);
        final String an = sr.getString(R.string.an);
        final String to_say = sr.getString(R.string.to_say);
        final String that_says = sr.getString(R.string.that_says);
        final String say = sr.getString(R.string.say);
        final String saying = sr.getString(R.string.saying);
        final String tell_him = sr.getString(R.string.tell_him);
        final String tell_her = sr.getString(R.string.tell_her);
        final String tell_them = sr.getString(R.string.tell_them);
        final String telling_him = sr.getString(R.string.telling_him);
        final String telling_her = sr.getString(R.string.telling_her);
        final String telling_them = sr.getString(R.string.telling_them);
        final String tell = sr.getString(R.string.tell);
        final String to_tell_him = sr.getString(R.string.to_tell_him);
        final String to_tell_her = sr.getString(R.string.to_tell_her);
        final String to_tell_them = sr.getString(R.string.to_tell_them);
        final String to_tell = sr.getString(R.string.to_tell);
        final String an_sms = sr.getString(R.string.an_sms);
        final String an_sms_message = sr.getString(R.string.an_sms_message);
        final String a_message = sr.getString(R.string.a_message);
        final String a_text = sr.getString(R.string.a_text);
        final String text_default = sr.getString(R.string.text_default);
        final String attacks_message = sr.getString(R.string.attacks_message);
        final String and = sr.getString(R.string.and);
        final String his = sr.getString(R.string.his);
        final String her = sr.getString(R.string.her);
        final String their = sr.getString(R.string.their);
        final String there = sr.getString(R.string.there);
        final String on = sr.getString(R.string.on);
        final String mobile = sr.getString(R.string.mobile);
        sr.reset();
        if (result.startsWith(attacks_message + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(attacks_message, "").trim();
        }
        if (result.startsWith(text + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(text_message, "").trim().replaceFirst(text, "").trim();
        }
        if (result.startsWith(sms + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(sms_message, "").trim().replaceFirst(sms, "").trim();
        }
        if (result.startsWith(text_default + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(text_default + XMLResultsHandler.SEP_SPACE + text_message, "").trim().replaceFirst(a_text, "").trim().replaceFirst(a_message, "").trim().replaceFirst(tell_them, "").trim().replaceFirst(tell_her, "").trim().replaceFirst(tell_him, "").trim();
        }
        if (result.startsWith(an + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(an_sms_message, "").trim().replaceFirst(an_sms, "").trim().replaceFirst(tell_them, "").trim().replaceFirst(tell_her, "").trim().replaceFirst(tell_him, "").trim();
        }
        if (result.startsWith(on + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(on + XMLResultsHandler.SEP_SPACE + his, "").trim().replaceFirst(on + XMLResultsHandler.SEP_SPACE + her, "").trim().replaceFirst(on + XMLResultsHandler.SEP_SPACE + their, "").trim().replaceFirst(on + XMLResultsHandler.SEP_SPACE + there, "").trim();
        }
        if (result.startsWith(mobile + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(mobile, "").trim();
        }
        if (result.startsWith(to_tell)) {
            result = result.replaceFirst(to_tell_them, "").trim().replaceFirst(to_tell_her, "").trim().replaceFirst(to_tell_him, "").trim();
        }
        if (result.startsWith(and + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(and, "").trim();
        }
        if (result.startsWith(tell)) {
            result = result.replaceFirst(telling_them, "").trim().replaceFirst(telling_her, "").trim().replaceFirst(telling_him, "").trim().replaceFirst(tell_them, "").trim().replaceFirst(tell_her, "").trim().replaceFirst(tell_him, "").trim();
        }
        if (result.startsWith(saying + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(saying, "").trim();
        }
        if (result.startsWith(say + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(say, "").trim();
        }
        if (result.startsWith(that_says + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(that_says, "").trim();
        }
        if (result.startsWith(to_say + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(to_say, "").trim();
        }
        return result;
    }

    private static @NonNull String getRawEmail(Context context, SupportedLanguage supportedLanguage, String generic) {
        String result = generic;
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        String email = sr.getString(R.string.email);
        String email_message = sr.getString(R.string.email_message);
        String an = sr.getString(R.string.an);
        String to_say = sr.getString(R.string.to_say);
        String that_says = sr.getString(R.string.that_says);
        String say = sr.getString(R.string.say);
        String saying = sr.getString(R.string.saying);
        String tell_him = sr.getString(R.string.tell_him);
        String tell_her = sr.getString(R.string.tell_her);
        String tell_them = sr.getString(R.string.tell_them);
        String telling_him = sr.getString(R.string.telling_him);
        String telling_her = sr.getString(R.string.telling_her);
        String telling_them = sr.getString(R.string.telling_them);
        String tell = sr.getString(R.string.tell);
        String to_tell_him = sr.getString(R.string.to_tell_him);
        String to_tell_her = sr.getString(R.string.to_tell_her);
        String to_tell_them = sr.getString(R.string.to_tell_them);
        String to_tell = sr.getString(R.string.to_tell);
        String b53 = sr.getString(R.string.an_email);
        String an_email = sr.getString(R.string.an_email_message);
        String and = sr.getString(R.string.and);
        sr.reset();
        if (result.startsWith(email + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(email_message, "").trim().replaceFirst(email, "").trim();
        }
        if (result.startsWith(an + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(an_email, "").trim().replaceFirst(b53, "").trim().replaceFirst(tell_them, "").trim().replaceFirst(tell_her, "").trim().replaceFirst(tell_him, "").trim();
        }
        if (result.startsWith(to_tell)) {
            result = result.replaceFirst(to_tell_them, "").trim().replaceFirst(to_tell_her, "").trim().replaceFirst(to_tell_him, "").trim();
        }
        if (result.startsWith(and + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(and, "").trim();
        }
        if (result.startsWith(tell)) {
            result = result.replaceFirst(telling_them, "").trim().replaceFirst(telling_her, "").trim().replaceFirst(telling_him, "").trim().replaceFirst(tell_them, "").trim().replaceFirst(tell_her, "").trim().replaceFirst(tell_him, "").trim();
        }
        if (result.startsWith(saying + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(saying, "").trim();
        }
        if (result.startsWith(say + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(say, "").trim();
        }
        if (result.startsWith(that_says + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(that_says, "").trim();
        }
        if (result.startsWith(to_say + XMLResultsHandler.SEP_SPACE)) {
            result = result.replaceFirst(to_say, "").trim();
        }
        return result;
    }
}
