package ai.saiy.android.command.contact;

import android.content.Context;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Contact_en {
    private static String mobile;
    private static String work;
    private static String office;
    private static String home;
    private static String the;
    private static String number;
    private static String his;
    private static String her;
    private static String their;
    private static String there;
    private static String on;
    private static String my;
    private static String at;
    private static String to;
    private static String via;
    private static String by;
    private static String contacts;
    private static String address;
    private static String location;
    private static String send_a_message_to;
    private static String send_a_message;
    private static String send_an;
    private static String send_a;
    private static String send_message_to;
    private static String compose_a_message_to;
    private static String compose_a_message;
    private static String compose_an;
    private static String compose_a;
    private static String compose_message_to;
    private static String text_message_to;
    private static String text_message;
    private static String text_to;
    private static String email_message_to;
    private static String email_message;

    private static String email_to;
    private static String sms_message_to;
    private static String sms_message;
    private static String sms_to;
    private static String message_to;
    private static String contact;
    private static String call;
    private static String dial;
    private static String ring;
    private static String display;
    private static String edit;
    private static String navigate;
    private static String text;
    private static String message;
    private static String email;
    private static String skype;
    private static String me;
    private static String text_to_speech;
    private static String send;
    private static String sms;
    private static String compose;
    private static String callback;
    private static String call_back;
    private static String call_black;
    private static String call_bak;
    private static String call_bag;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Contact_en.class.getSimpleName();

    public Contact_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (contact == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandContactValues sortContact(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandContactValues commandContactValues = new CommandContactValues();
        final ArrayList<String> voiceDataTrimmed = new ArrayList<>();
        ListIterator<String> listIterator = voiceData.listIterator();
        while (listIterator.hasNext()) {
            if (!ai.saiy.android.utils.UtilsString.regexCheck(listIterator.next())) {
                listIterator.remove();
            }
        }
        if (contact == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if ((vdLower.startsWith(call) && !vdLower.startsWith(call + XMLResultsHandler.SEP_SPACE + me + XMLResultsHandler.SEP_SPACE) && !vdLower.startsWith(call_back) && !vdLower.startsWith(callback) && !vdLower.matches(call_black) && !vdLower.matches(call_bak) && !vdLower.matches(call_bag)) || vdLower.startsWith(dial) || vdLower.startsWith(ring)) {
                if (commandContactValues.getType() == CommandContactValues.Type.UNKNOWN) {
                    commandContactValues.setType(CommandContactValues.Type.CALL);
                }
                if (vdLower.contains(mobile)) {
                    if (commandContactValues.getCallType() == CommandContactValues.CallType.UNKNOWN) {
                        commandContactValues.setCallType(CommandContactValues.CallType.MOBILE);
                    }
                    voiceDataTrimmed.add(trimForMobile(vdLower));
                } else if (vdLower.contains(office) || vdLower.contains(work)) {
                    if (commandContactValues.getCallType() == CommandContactValues.CallType.UNKNOWN) {
                        commandContactValues.setCallType(CommandContactValues.CallType.WORK);
                    }
                    voiceDataTrimmed.add(trimForWork(vdLower));
                } else if (vdLower.contains(home)) {
                    if (commandContactValues.getCallType() == CommandContactValues.CallType.UNKNOWN) {
                        commandContactValues.setCallType(CommandContactValues.CallType.HOME);
                    }
                    voiceDataTrimmed.add(trimForHome(vdLower));
                } else {
                    final String trimmedForCall = trimForCall(vdLower);
                    final String trimmed = trimmedForCall.replaceAll("o", "0").replaceAll(XMLResultsHandler.SEP_HYPHEN, "").trim();
                    if (trimmed.matches("[0-9 ]+")) {
                        if (commandContactValues.getCallType() == CommandContactValues.CallType.UNKNOWN) {
                            commandContactValues.setCallType(CommandContactValues.CallType.NUMBER);
                            voiceDataTrimmed.clear();
                            voiceDataTrimmed.add(trimmed);
                            break;
                        }
                    } else if (!trimmedForCall.isEmpty() && commandContactValues.getCallType() == CommandContactValues.CallType.UNKNOWN) {
                        voiceDataTrimmed.add(trimmedForCall);
                    }
                }
            } else if (vdLower.startsWith(display)) {
                if (commandContactValues.getType() == CommandContactValues.Type.UNKNOWN) {
                    commandContactValues.setType(CommandContactValues.Type.DISPLAY);
                }
                voiceDataTrimmed.add(trimForDisplay(vdLower));
            } else if (vdLower.startsWith(navigate) && vdLower.contains(contact)) {
                if (commandContactValues.getType() == CommandContactValues.Type.UNKNOWN) {
                    commandContactValues.setType(CommandContactValues.Type.NAVIGATE);
                }
                String trimmedForNavigate = trimForNavigate(vdLower);
                if (trimmedForNavigate.contains(work) || trimmedForNavigate.contains(office)) {
                    if (commandContactValues.getNavigationType() == CommandContactValues.NavigationType.UNKNOWN) {
                        commandContactValues.setNavigationType(CommandContactValues.NavigationType.WORK);
                    }
                    trimmedForNavigate = trimmedForNavigate.replaceAll("\\b" + work + "\\b\\s*", "").trim().replaceAll("\\b" + office + "\\b\\s*", "").trim();
                } else if (trimmedForNavigate.contains(home)) {
                    if (commandContactValues.getNavigationType() == CommandContactValues.NavigationType.UNKNOWN) {
                        commandContactValues.setNavigationType(CommandContactValues.NavigationType.HOME);
                    }
                    trimmedForNavigate = trimmedForNavigate.replaceAll("\\b" + home + "\\b\\s*", "").trim();
                }
                voiceDataTrimmed.add(trimmedForNavigate);
            } else if ((vdLower.startsWith(text) && !vdLower.startsWith(text_to_speech)) || vdLower.startsWith(sms) || ((vdLower.startsWith(message) && !vdLower.contains(email)) || ((vdLower.startsWith(send) && !vdLower.contains(email) && (vdLower.contains(text) || vdLower.contains(sms) || vdLower.contains(message))) || (vdLower.startsWith(compose) && !vdLower.contains(email) && (vdLower.contains(text) || vdLower.contains(sms) || vdLower.contains(message)))))) {
                if (commandContactValues.getType() == CommandContactValues.Type.UNKNOWN) {
                    commandContactValues.setType(CommandContactValues.Type.TEXT);
                }
                if (vdLower.startsWith(send)) {
                    vdLower = trimForSend(vdLower);
                }
                if (vdLower.startsWith(compose)) {
                    vdLower = trimForCompose(vdLower);
                }
                if (vdLower.startsWith(via)) {
                    vdLower = vdLower.replaceFirst(via, "").trim();
                }
                if (vdLower.startsWith(by)) {
                    vdLower = vdLower.replaceFirst(by, "").trim();
                }
                if (vdLower.startsWith(text)) {
                    vdLower = trimForText(vdLower);
                }
                if (vdLower.startsWith(sms)) {
                    vdLower = trimForSms(vdLower);
                }
                if (vdLower.startsWith(message)) {
                    vdLower = vdLower.replaceFirst(message_to, "").trim().replaceFirst(message, "").trim();
                }
                if (vdLower.startsWith(via)) {
                    vdLower = vdLower.replaceFirst(via, "").trim();
                }
                if (vdLower.startsWith(by)) {
                    vdLower = vdLower.replaceFirst(by, "").trim();
                }
                if (vdLower.startsWith(my)) {
                    vdLower = vdLower.replaceFirst("\\b" + my + "\\b\\s*", "").trim();
                }
                if (vdLower.startsWith(the)) {
                    vdLower = vdLower.replaceFirst("\\b" + the + "\\b\\s*", "").trim();
                }
                voiceDataTrimmed.add(vdLower.replaceFirst("\\b" + contacts + "\\b\\s*", "").trim().replaceFirst("\\b" + contact + "\\b\\s*", "").trim());
            } else if (vdLower.startsWith(email) || ((vdLower.startsWith(message) && vdLower.contains(email)) || ((vdLower.startsWith(send) && vdLower.contains(email)) || (vdLower.startsWith(compose) && vdLower.contains(email))))) {
                if (commandContactValues.getType() == CommandContactValues.Type.UNKNOWN) {
                    commandContactValues.setType(CommandContactValues.Type.EMAIL);
                }
                if (vdLower.startsWith(send)) {
                    vdLower = trimForSend(vdLower);
                }
                if (vdLower.startsWith(compose)) {
                    vdLower = trimForCompose(vdLower);
                }
                if (vdLower.startsWith(via)) {
                    vdLower = vdLower.replaceFirst(via, "").trim();
                }
                if (vdLower.startsWith(by)) {
                    vdLower = vdLower.replaceFirst(by, "").trim();
                }
                if (vdLower.startsWith(email)) {
                    vdLower = trimForEmail(vdLower);
                }
                if (vdLower.startsWith(message)) {
                    vdLower = vdLower.replaceFirst(message_to, "").trim().replaceFirst(message, "").trim();
                }
                if (vdLower.startsWith(via)) {
                    vdLower = vdLower.replaceFirst(via, "").trim();
                }
                if (vdLower.startsWith(by)) {
                    vdLower = vdLower.replaceFirst(by, "").trim();
                }
                if (vdLower.startsWith(my)) {
                    vdLower = vdLower.replaceFirst("\\b" + my + "\\b\\s*", "").trim();
                }
                if (vdLower.startsWith(the)) {
                    vdLower = vdLower.replaceFirst("\\b" + the + "\\b\\s*", "").trim();
                }
                voiceDataTrimmed.add(vdLower.replaceFirst("\\b" + contacts + "\\b\\s*", "").trim().replaceFirst("\\b" + contact + "\\b\\s*", "").trim());
            } else if (vdLower.startsWith(skype)) {
                if (commandContactValues.getType() == CommandContactValues.Type.UNKNOWN) {
                    commandContactValues.setType(CommandContactValues.Type.CALL);
                }
                if (commandContactValues.getCallType() == CommandContactValues.CallType.UNKNOWN) {
                    commandContactValues.setCallType(CommandContactValues.CallType.SKYPE);
                }
                voiceDataTrimmed.add(trimForSkype(vdLower));
            } else if (vdLower.startsWith(edit)) {
                if (commandContactValues.getType() == CommandContactValues.Type.UNKNOWN) {
                    commandContactValues.setType(CommandContactValues.Type.EDIT);
                }
                voiceDataTrimmed.add(trimForEdit(vdLower));
            }
        }
        commandContactValues.setVoiceData(voiceData);
        commandContactValues.setVoiceDataTrimmed(voiceDataTrimmed);
        if (DEBUG) {
            for (String string : voiceDataTrimmed) {
                MyLog.d("vdTrimmed: ", string);
            }
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandContactValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        contact = sr.getString(R.string.contact);
        call = sr.getString(R.string.call);
        dial = sr.getString(R.string.dial);
        ring = sr.getString(R.string.ring);
        display = sr.getString(R.string.display);
        edit = sr.getString(R.string.edit);
        navigate = sr.getString(R.string.navigate);
        text = sr.getString(R.string.text);
        message = sr.getString(R.string.message);
        email = sr.getString(R.string.email);
        skype = sr.getString(R.string.skype);
        me = sr.getString(R.string.me);
        text_to_speech = sr.getString(R.string.text_to_speech);
        send = sr.getString(R.string.send);
        sms = sr.getString(R.string.sms);
        compose = sr.getString(R.string.compose);
        callback = sr.getString(R.string.callback);
        call_back = sr.getString(R.string.call_back);
        call_black = sr.getString(R.string.call_black);
        call_bak = sr.getString(R.string.call_bak);
        call_bag = sr.getString(R.string.call_bag);
        mobile = sr.getString(R.string.mobile);
        work = sr.getString(R.string.work);
        office = sr.getString(R.string.office);
        home = sr.getString(R.string.home);
        the = sr.getString(R.string.the);
        number = sr.getString(R.string.number);
        his = sr.getString(R.string.his);
        her = sr.getString(R.string.her);
        their = sr.getString(R.string.their);
        there = sr.getString(R.string.there);
        on = sr.getString(R.string.on);
        my = sr.getString(R.string.my);
        at = sr.getString(R.string.at);
        to = sr.getString(R.string.to);
        via = sr.getString(R.string.via);
        by = sr.getString(R.string.by);
        contacts = sr.getString(R.string.contacts);
        address = sr.getString(R.string.address);
        location = sr.getString(R.string.location);
        send_a_message_to = sr.getString(R.string.send_a_message_to);
        send_a_message = sr.getString(R.string.send_a_message);
        send_an = sr.getString(R.string.send_an);
        send_a = sr.getString(R.string.send_a);
        send_message_to = sr.getString(R.string.send_message_to);
        compose_a_message_to = sr.getString(R.string.compose_a_message_to);
        compose_a_message = sr.getString(R.string.compose_a_message);
        compose_an = sr.getString(R.string.compose_an);
        compose_a = sr.getString(R.string.compose_a);
        compose_message_to = sr.getString(R.string.compose_message_to);
        text_message_to = sr.getString(R.string.text_message_to);
        text_message = sr.getString(R.string.text_message);
        text_to = sr.getString(R.string.text_to);
        email_message_to = sr.getString(R.string.email_message_to);
        email_message = sr.getString(R.string.email_message);
        email_to = sr.getString(R.string.email_to);
        sms_message_to = sr.getString(R.string.sms_message_to);
        sms_message = sr.getString(R.string.sms_message);
        sms_to = sr.getString(R.string.sms_to);
        message_to = sr.getString(R.string.message_to);
    }

    private static String trimForMobile(String str) {
        return str.replaceAll("\\b" + mobile + "\\b\\s*", "").trim().replaceAll("\\b" + call + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + his + "\\b\\s*", "").trim().replaceAll("\\b" + her + "\\b\\s*", "").trim().replaceAll("\\b" + their + "\\b\\s*", "").trim().replaceAll("\\b" + there + "\\b\\s*", "").trim().replaceAll("\\b" + on + "\\b\\s*", "").trim().replaceAll("\\b" + my + "\\b\\s*", "").trim().replaceAll("\\b" + at + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim();
    }

    private static String trimForWork(String str) {
        return str.replaceAll("\\b" + work + "\\b\\s*", "").trim().replaceAll("\\b" + call + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + his + "\\b\\s*", "").trim().replaceAll("\\b" + her + "\\b\\s*", "").trim().replaceAll("\\b" + their + "\\b\\s*", "").trim().replaceAll("\\b" + there + "\\b\\s*", "").trim().replaceAll("\\b" + on + "\\b\\s*", "").trim().replaceAll("\\b" + my + "\\b\\s*", "").trim().replaceAll("\\b" + at + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim();
    }

    private static String trimForHome(String str) {
        return str.replaceAll("\\b" + home + "\\b\\s*", "").trim().replaceAll("\\b" + call + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + his + "\\b\\s*", "").trim().replaceAll("\\b" + her + "\\b\\s*", "").trim().replaceAll("\\b" + their + "\\b\\s*", "").trim().replaceAll("\\b" + there + "\\b\\s*", "").trim().replaceAll("\\b" + on + "\\b\\s*", "").trim().replaceAll("\\b" + my + "\\b\\s*", "").trim().replaceAll("\\b" + at + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim();
    }

    private static String trimForCall(String str) {
        return str.replaceAll("\\b" + call + "\\b\\s*", "").trim().replaceAll("\\b" + ring + "\\b\\s*", "").trim().replaceAll("\\b" + dial + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + number + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim();
    }

    private static String trimForDisplay(String str) {
        return str.replaceAll("\\b" + display + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim();
    }

    private static String trimForNavigate(String str) {
        return str.replaceAll("\\b" + navigate + "\\b\\s*", "").trim().replaceAll("\\b" + to + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + his + "\\b\\s*", "").trim().replaceAll("\\b" + her + "\\b\\s*", "").trim().replaceAll("\\b" + their + "\\b\\s*", "").trim().replaceAll("\\b" + there + "\\b\\s*", "").trim().replaceAll("\\b" + on + "\\b\\s*", "").trim().replaceAll("\\b" + my + "\\b\\s*", "").trim().replaceAll("\\b" + at + "\\b\\s*", "").trim().replaceAll("\\b" + address + "\\b\\s*", "").trim().replaceAll("\\b" + location + "\\b\\s*", "").trim();
    }

    private static String trimForSend(String str) {
        return str.replaceFirst(send_a_message_to, "").trim().replaceFirst(send_a_message, "").trim().replaceFirst(send_an, "").trim().replaceFirst(send_a, "").trim().replaceFirst(send_message_to, "").trim().replaceFirst(send, "").trim();
    }

    private static String trimForCompose(String str) {
        return str.replaceFirst(compose_a_message_to, "").trim().replaceFirst(compose_a_message, "").trim().replaceFirst(compose_an, "").trim().replaceFirst(compose_a, "").trim().replaceFirst(compose_message_to, "").trim().replaceFirst(compose, "").trim();
    }

    private static String trimForText(String str) {
        return str.replaceFirst(text_message_to, "").trim().replaceFirst(text_message, "").trim().replaceFirst(text_to, "").trim().replaceFirst("\\b" + text + "\\b\\s*", "").trim();
    }

    private static String trimForSms(String str) {
        return str.replaceFirst(sms_message_to, "").trim().replaceFirst(sms_message, "").trim().replaceFirst(sms_to, "").trim().replaceFirst(sms, "").trim();
    }

    private static String trimForEmail(String str) {
        return str.replaceFirst(email_message_to, "").trim().replaceFirst(email_message, "").trim().replaceFirst(email_to, "").trim().replaceFirst("\\b" + email + "\\b\\s*", "").trim();
    }

    private static String trimForSkype(String str) {
        return str.replaceAll("\\b" + skype + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim();
    }

    private static String trimForEdit(String str) {
        return str.replaceAll("\\b" + edit + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + contacts + "\\b\\s*", "").trim().replaceAll("\\b" + contact + "\\b\\s*", "").trim();
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (!vdLower.startsWith(call_back) && !vdLower.startsWith(callback) && !vdLower.matches(call_black) && !vdLower.matches(call_bak) && !vdLower.matches(call_bag) && ((vdLower.startsWith(display) && vdLower.contains(contact)) || ((vdLower.startsWith(edit) && vdLower.contains(contact)) || ((vdLower.startsWith(navigate) && vdLower.contains(contact)) || ((vdLower.startsWith(call) && !vdLower.startsWith(call + XMLResultsHandler.SEP_SPACE + me + XMLResultsHandler.SEP_SPACE)) || vdLower.startsWith(ring) || vdLower.startsWith(dial) || ((vdLower.startsWith(text) && !vdLower.startsWith(text_to_speech)) || vdLower.startsWith(email) || vdLower.startsWith(message) || vdLower.startsWith(skype) || ((vdLower.startsWith(send) && (vdLower.contains(text) || vdLower.contains(sms) || vdLower.contains(message) || vdLower.contains(email))) || (vdLower.startsWith(compose) && (vdLower.contains(text) || vdLower.contains(sms) || vdLower.contains(message) || vdLower.contains(email)))))))))) {
                    toReturn.add(new Pair<>(CC.COMMAND_CONTACT, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "contact: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
