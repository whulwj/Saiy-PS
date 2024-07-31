package ai.saiy.android.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.google.common.collect.Lists;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.accessibility.BlockedApplications;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.tts.helper.SpeechPriority;
import ai.saiy.android.utils.MyLog;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NLService extends NotificationListenerService {
    private static final int MAX_PREVIOUS_CONTENT = 30;
    private static final Object messageLock = new Object();
    private static final Object hangoutLock = new Object();
    private static final Object whatsAppLock = new Object();
    private static final Object lock = new Object();

    private long previousCommandTime;
    private SupportedLanguage sl;
    private String blocked_input_method;
    private String blocked_location;
    private String is_typing;
    private String an_unknown_application;
    private volatile boolean isAnnounceNotificationRequired;
    private volatile BlockedApplications blockedApplications;
    private ArrayList<String> messages;
    private ArrayList<String> hangoutTickerText;
    private ArrayList<String> whatsAppContent;
    private long then;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = NLService.class.getSimpleName();
    private String utterance = null;
    private final Pattern saiy = Pattern.compile("ai\\.saiy\\.android", Pattern.CASE_INSENSITIVE);
    private final Pattern phone = Pattern.compile("com\\.android\\.phone", Pattern.CASE_INSENSITIVE);
    private final Pattern googleDialer = Pattern.compile("com\\.google\\.android\\.dialer", Pattern.CASE_INSENSITIVE);

    private void examineBundle(@Nullable Bundle bundle) {
        MyLog.d(CLS_NAME, "examineBundle");
        if (bundle == null) {
            MyLog.w(CLS_NAME, "examineBundle: bundle null");
            return;
        }
        for (String key : bundle.keySet()) {
            MyLog.v(CLS_NAME, "examineBundle: " + key + " ~ " + bundle.get(key));
        }
    }

    private void handleNotificationRemoved(final StatusBarNotification statusBarNotification) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "handleNotificationRemoved");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isAnnounceNotificationRequired) {
                    if (NLService.DEBUG) {
                        MyLog.d(CLS_NAME, "handleNotificationRemoved: not required");
                    }
                    return;
                }
                if (!ai.saiy.android.utils.SPH.getSelfAwareEnabled(getApplicationContext())) {
                    if (NLService.DEBUG) {
                        MyLog.d(CLS_NAME, "handleNotificationRemoved: self aware disabled by user");
                    }
                    return;
                }
                if (NLService.DEBUG) {
                    MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: getId: " + statusBarNotification.getId());
                    MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: getPostTime: " + statusBarNotification.getPostTime());
                    MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: getTag: " + statusBarNotification.getTag());
                    MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: getPackageName: " + statusBarNotification.getPackageName());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: getKey: " + statusBarNotification.getKey());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: getGroupKey: " + statusBarNotification.getGroupKey());
                        final UserHandle user = statusBarNotification.getUser();
                        if (user != null) {
                            MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: userHandle: " + user);
                        } else {
                            MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: userHandle: null");
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        MyLog.d(CLS_NAME, "onNotificationRemoved: sbn: getOverrideGroupKey: " + statusBarNotification.getOverrideGroupKey());
                    }
                }
                if (ai.saiy.android.utils.UtilsString.notNaked(statusBarNotification.getPackageName())) {
                    final String packageName = statusBarNotification.getPackageName();
                    switch (packageName) {
                        case Installed.PACKAGE_ANDROID_MESSAGING:
                        case Installed.PACKAGE_GOOGLE_MESSAGE:
                            if (NLService.DEBUG) {
                                MyLog.d(CLS_NAME, "onNotificationRemoved: MESSAGING: purging array");
                            }
                            synchronized (messageLock) {
                                messages.clear();
                            }
                            break;
                        case Installed.PACKAGE_GOOGLE_HANGOUT:
                            if (NLService.DEBUG) {
                                MyLog.d(CLS_NAME, "onNotificationRemoved: PACKAGE_HANGOUTS: purging array");
                            }
                            synchronized (hangoutLock) {
                                hangoutTickerText.clear();
                            }
                            break;
                        case Installed.PACKAGE_WHATSAPP:
                            if (NLService.DEBUG) {
                                MyLog.d(CLS_NAME, "onNotificationRemoved: PACKAGE_WHATSAPP: purging array");
                            }
                            synchronized (whatsAppLock) {
                                whatsAppContent.clear();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }).start();
    }

    private void updateServiceInfo(boolean condition) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "updateServiceInfo");
        }
        this.blockedApplications = ai.saiy.android.accessibility.BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
        if (isAnnounceNotificationRequired != condition) {
            this.isAnnounceNotificationRequired = condition;
        } else if (DEBUG) {
            MyLog.d(CLS_NAME, "updateServiceInfo: no change");
        }
    }

    private boolean commandDelaySufficient(long postTime) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "commandDelaySufficient");
        }
        final long delay = postTime - 5000;
        if (DEBUG) {
            MyLog.d(CLS_NAME, "commandDelaySufficient: delay: " + delay);
            MyLog.d(CLS_NAME, "commandDelaySufficient: previousCommandTime: " + previousCommandTime);
        }
        return delay > previousCommandTime;
    }

    private boolean isRestrictedContent(String content) {
        ArrayList<String> arrayList;
        synchronized (lock) {
            if (ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(content).find() || !ai.saiy.android.utils.UtilsString.regexCheck(content)) {
                return true;
            }
            if (blockedApplications != null) {
                if (ai.saiy.android.utils.UtilsString.notNaked(blockedApplications.getText())) {
                    arrayList = Lists.newArrayList(com.google.common.base.Splitter.on(XMLResultsHandler.SEP_COMMA).trimResults().split(blockedApplications.getText()));
                    arrayList.removeAll(Collections.singleton(null));
                    arrayList.removeAll(Collections.singleton(""));
                } else {
                    arrayList = new ArrayList<>(2);
                }
                arrayList.add(blocked_input_method);
                arrayList.add(blocked_location);
                for (String restrictedContent : arrayList) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "isRestrictedContent: " + restrictedContent);
                    }
                    if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(content, restrictedContent)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void handleNotificationPosted(final StatusBarNotification statusBarNotification) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onNotificationPosted");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ai.saiy.android.utils.SPH.getSelfAwareEnabled(getApplicationContext())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "handleNotificationPosted: self aware disabled by user");
                    }
                    return;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNotificationPosted: DrivingProfileHelper.areProfileNotificationsEnabled: " + ai.saiy.android.command.driving.DrivingProfileHelper.isAnnounceNotificationsEnabled(getApplicationContext()));
                    MyLog.i(CLS_NAME, "onNotificationPosted: getAnnounceNotifications: " + ai.saiy.android.utils.SPH.getAnnounceNotifications(getApplicationContext()));
                    MyLog.i(CLS_NAME, "onNotificationPosted: QuietTimesHelper.canProceed: " + ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext()));
                    MyLog.i(CLS_NAME, "onNotificationPosted: getSuppressNotificationsSecure: " + ai.saiy.android.utils.SPH.getAnnounceNotificationsSecure(getApplicationContext()));
                }
                boolean isAnnounceNotificationRequired = false;
                if (ai.saiy.android.command.driving.DrivingProfileHelper.isAnnounceNotificationsEnabled(getApplicationContext())) {
                    isAnnounceNotificationRequired = true;
                } else if (ai.saiy.android.utils.SPH.getAnnounceNotifications(getApplicationContext())) {
                    if (ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext())) {
                        if (!ai.saiy.android.device.UtilsDevice.isDeviceLocked(getApplicationContext())) {
                            isAnnounceNotificationRequired = true;
                        } else if (!ai.saiy.android.utils.SPH.getAnnounceNotificationsSecure(getApplicationContext())) {
                            isAnnounceNotificationRequired = true;
                        }
                    }
                }
                updateServiceInfo(isAnnounceNotificationRequired);
                if (!isAnnounceNotificationRequired) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onNotificationPosted: not required");
                    }
                    return;
                }

                final Notification notification = statusBarNotification.getNotification();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNotificationPosted: sbn: getId: " + statusBarNotification.getId());
                    MyLog.i(CLS_NAME, "onNotificationPosted: sbn: getPostTime: " + statusBarNotification.getPostTime());
                    MyLog.i(CLS_NAME, "onNotificationPosted: sbn: getTag: " + statusBarNotification.getTag());
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        MyLog.i(CLS_NAME, "onNotificationPosted: sbn: getKey: " + statusBarNotification.getKey());
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                            MyLog.i(CLS_NAME, "onNotificationPosted: sbn: getGroupKey: " + statusBarNotification.getGroupKey());
                            MyLog.i(CLS_NAME, "onNotificationPosted: sbn: userHandle: " + statusBarNotification.getUser());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                MyLog.i(CLS_NAME, "onNotificationPosted: sbn: getOverrideGroupKey: " + statusBarNotification.getOverrideGroupKey());
                            }
                        }
                    }
                }
                if (notification == null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "notification null");
                    }
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    examineBundle(notification.extras);
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNotificationPosted: notification: " + notification);
                }
                if (!ai.saiy.android.utils.UtilsString.notNaked(statusBarNotification.getPackageName())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "package name naked");
                    }
                    return;
                } else if (isPackageRestrictedPackage(statusBarNotification.getPackageName())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "package restricted");
                    }
                    return;
                }

                final Pair<Boolean, String> appNamePair = ai.saiy.android.applications.UtilsApplication.getAppNameFromPackage(getApplicationContext(), statusBarNotification.getPackageName());
                final String applicationName = (appNamePair.first? appNamePair.second : an_unknown_application);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "applicationName: " + applicationName);
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ticker: " + ((notification.tickerText == null)? "" : notification.tickerText));
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "no bundle");
                    }
                    return;
                } else if (!ai.saiy.android.utils.UtilsBundle.notNaked(notification.extras)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "bundle naked");
                    }
                    return;
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "EXTRA_TITLE: " + notification.extras.getCharSequence(Notification.EXTRA_TITLE, ""));
                    MyLog.i(CLS_NAME, "EXTRA_TEXT: " + notification.extras.getCharSequence(Notification.EXTRA_TEXT, ""));
                    MyLog.i(CLS_NAME, "EXTRA_SUB_TEXT: " + notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT, ""));
                    MyLog.i(CLS_NAME, "EXTRA_INFO_TEXT: " + notification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT, ""));
                    MyLog.i(CLS_NAME, "EXTRA_SUBSTITUTE_APP_NAME: " + notification.extras.getCharSequence("android.substName"/*Notification.EXTRA_SUBSTITUTE_APP_NAME*/, ""));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            MyLog.i(CLS_NAME, "EXTRA_CONVERSATION_TITLE: " + notification.extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE, ""));
                        }
                        MyLog.i(CLS_NAME, "EXTRA_BIG_TEXT: " + notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT, ""));
                    }
                    MyLog.i(CLS_NAME, "EXTRA_TITLE_BIG: " + notification.extras.getCharSequence(Notification.EXTRA_TITLE_BIG, ""));
                    MyLog.i(CLS_NAME, "EXTRA_PROGRESS_INDETERMINATE: " + notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false));
                    MyLog.i(CLS_NAME, "EXTRA_PROGRESS: " + notification.extras.getInt(Notification.EXTRA_PROGRESS, 0));
                    MyLog.i(CLS_NAME, "EXTRA_PROGRESS_MAX: " + notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0));
                    final CharSequence[] notificationTextLines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                    if (ai.saiy.android.utils.UtilsList.notNaked(notificationTextLines)) {
                        for (CharSequence charSequence : notificationTextLines) {
                            MyLog.i(CLS_NAME, "EXTRA_TEXT_LINES: line: " + charSequence);
                        }
                    } else {
                        MyLog.i(CLS_NAME, "EXTRA_TEXT_LINES: naked");
                    }
                    final String[] notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                    if (ai.saiy.android.utils.UtilsList.notNaked(notificationPeople)) {
                        for (String charSequence : notificationPeople) {
                            MyLog.i(CLS_NAME, "EXTRA_PEOPLE: person: " + charSequence);
                        }
                    } else {
                        MyLog.i(CLS_NAME, "EXTRA_PEOPLE: naked");
                    }
                    final String[] applications = notification.extras.getStringArray("android.foregroundApps"/*Notification.EXTRA_FOREGROUND_APPS*/);
                    if (ai.saiy.android.utils.UtilsList.notNaked(applications)) {
                        for (String app : applications) {
                            MyLog.i(CLS_NAME, "EXTRA_FOREGROUND_APPS: app: " + app);
                        }
                    } else {
                        MyLog.i(CLS_NAME, "EXTRA_FOREGROUND_APPS: naked");
                    }
                }
                if (notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)
                        || notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0) > 0) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "progress notification: ignoring");
                    }
                    return;
                }
                final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(getApplicationContext(), sl);
                String utterance = "";
                String title;
                String notificationText;
                CharSequence[] notificationTextLines;
                String[] notificationPeople;
                switch (statusBarNotification.getPackageName()) {
                    case Installed.PACKAGE_GMAIL:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_GMAIL");
                        }
                        if (!ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_GMAIL: ticker naked: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                        if (!ai.saiy.android.utils.UtilsList.notNaked(notificationPeople)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_GMAIL: notificationPeople naked: discarding");
                            }
                            sr.reset();
                            return;
                        }

                        String emailAddress = notificationPeople[notificationPeople.length - 1].replaceFirst("mailto:", "").trim();
                        String contactName;
                        if (ai.saiy.android.utils.UtilsString.notNaked(emailAddress)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_GMAIL: emailAddress: " + emailAddress);
                            }
                            if (ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissionsNR(getApplicationContext())) {
                                contactName = new ai.saiy.android.contacts.ContactHelper().getNameFromEmail(getApplicationContext(), emailAddress);
                                if (ai.saiy.android.utils.UtilsString.notNaked(contactName)) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_GMAIL: contactName: " + contactName);
                                    }
                                } else {
                                    contactName = emailAddress;
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_GMAIL: contactName: unknown");
                                    }
                                }
                            } else {
                                if (ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_GMAIL: contactName: " + notification.tickerText);
                                    }
                                    contactName = notification.tickerText.toString();
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_GMAIL: contactName: unknown");
                                    }
                                    contactName = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_GMAIL: contactName: naked");
                            }
                            contactName = "an unknown sender";
                        }
                        utterance = "You've received a new " + applicationName + ". " + "From, " + contactName;
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "PACKAGE_GMAIL: " + utterance);
                        }
                        break;
                    case Installed.PACKAGE_ANDROID_MESSAGING:
                    case Installed.PACKAGE_GOOGLE_MESSAGE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING|PACKAGE_GOOGLE_MESSENGER");
                        }
                        notificationTextLines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                        if (ai.saiy.android.utils.UtilsList.notNaked(notificationTextLines)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: multiple message: discarding");
                            }
                            sr.reset();
                            return;
                        } else {
                            notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                            if (ai.saiy.android.utils.UtilsString.notNaked(notificationText) && matchWithPreviousMessage(notificationText)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: previous content: discarding");
                                }
                                sr.reset();
                                return;
                            }
                            synchronized (messageLock) {
                                if (messages.size() > MAX_PREVIOUS_CONTENT) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: purging array");
                                    }
                                    messages.clear();
                                }
                                messages.add(notificationText);
                            }
                            notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                            String smsContactName = "an unknown sender";
                            if (!ai.saiy.android.utils.UtilsList.notNaked(notificationPeople) || notificationPeople.length != 1) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: unknown contact");
                                }
                                title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                                if (ai.saiy.android.utils.UtilsString.notNaked(title)) {
                                    smsContactName = title;
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: single known contact");
                                }
                                if (ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissionsNR(getApplicationContext())) {
                                    smsContactName = new ai.saiy.android.contacts.ContactHelper().getNameFromUri(getApplicationContext(), android.net.Uri.parse(notificationPeople[0]));
                                }
                                if (ai.saiy.android.utils.UtilsString.notNaked(smsContactName)) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: smsContactName: " + smsContactName);
                                    }
                                } else {
                                    smsContactName = "an unknown sender";
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: smsContactName: naked");
                                    }
                                    title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                                    if (ai.saiy.android.utils.UtilsString.notNaked(title)) {
                                        smsContactName = title;
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: smsContactName: " + smsContactName);
                                        }
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: EXTRA_TITLE: naked");
                                        }
                                    }
                                }
                            }

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: announceSMSContent: " + ai.saiy.android.utils.SPH.getAnnounceNotificationsSMS(getApplicationContext()));
                            }
                            if (ai.saiy.android.utils.SPH.getAnnounceNotificationsSMS(getApplicationContext())) {
                                if (ai.saiy.android.utils.UtilsString.notNaked(notificationText)) {
                                    utterance = sr.getString(R.string.structured_sms_1) + " " + smsContactName + " " + sr.getString(R.string.saying) + ". " + notificationText;
                                } else {
                                    utterance = sr.getString(R.string.structured_sms_1) + " " + smsContactName;
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: EXTRA_TEXT: naked");
                                    }
                                }
                            } else {
                                utterance = sr.getString(R.string.structured_sms_1) + " " + smsContactName;
                            }
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: " + utterance);
                            }
                        }
                        break;
                    case Installed.PACKAGE_SNAPCHAT:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_SNAPCHAT");
                        }
                        if (!ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_SNAPCHAT: ticker naked: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        if (notification.tickerText.toString().contains(is_typing)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_SNAPCHAT: is typing: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        utterance = "New Snapchat. " + notification.tickerText;
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "PACKAGE_SNAPCHAT: " + utterance);
                        }
                        break;
                    case Installed.PACKAGE_EBAY:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_EBAY");
                        }
                        title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                        notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                        if (ai.saiy.android.utils.UtilsString.notNaked(notificationText) && ai.saiy.android.utils.UtilsString.notNaked(title)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_EBAY: using title & text");
                            }
                            utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + title + ". " + notificationText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(notificationText) && ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_EBAY: using ticker & text");
                            }
                            utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + notification.tickerText + ". " + notificationText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_EBAY: using ticker");
                            }
                            utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + notification.tickerText;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_EBAY: content naked: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        break;
                    case "android":
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID");
                        }
                        title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                        notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                        if (ai.saiy.android.utils.UtilsString.notNaked(title) && (title.contains(getString(R.string.app_name)) || title.contains("Gmail"))) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID: title contains Saiy/Gmail: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        if (ai.saiy.android.utils.UtilsString.notNaked(notificationText) && (notificationText.contains(getString(R.string.app_name)) || notificationText.contains("Gmail"))) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID: text contains Saiy/Gmail: discarding");
                            }
                            sr.reset();
                            return;
                        }

                        if (ai.saiy.android.utils.UtilsString.notNaked(notificationText) && notificationText.contains("Vocalizer")) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID: text contains Vocalizer: discarding");
                            }
                            sr.reset();
                            return;
                        }

                        if (ai.saiy.android.utils.UtilsString.notNaked(notificationText) && ai.saiy.android.utils.UtilsString.notNaked(title)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID: using title & text");
                            }
                            utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + title + ". " + notificationText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(notificationText) && ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID: using ticker & text");
                            }
                            utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + notification.tickerText + ". " + notificationText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID: using ticker");
                            }
                            utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + notification.tickerText;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID: content naked: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "PACKAGE_ANDROID: " + utterance);
                        }
                        break;
                    case Installed.PACKAGE_WHATSAPP:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP");
                        }
                        if (!ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: ticker naked: discarding");
                            }
                            return;
                        } else if (notification.tickerText.toString().matches("WhatsApp")) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: ticker WhatsApp: discarding");
                            }
                            return;
                        }

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: announceWhatsAppContent: " + ai.saiy.android.utils.SPH.getAnnounceNotificationsWhatsapp(getApplicationContext()));
                        }
                        if (ai.saiy.android.utils.SPH.getAnnounceNotificationsWhatsapp(getApplicationContext())) {
                            notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                            notificationTextLines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                            notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                            if (DEBUG) {
                                if (!ai.saiy.android.utils.UtilsList.notNaked(notificationPeople)) {
                                    MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: notificationPeople: naked");
                                }
                            }
                            String content = null;
                            boolean haveContent = false;
                            if (ai.saiy.android.utils.UtilsList.notNaked(notificationTextLines) && ai.saiy.android.utils.UtilsList.notNaked(notificationPeople)) {
                                content = notificationTextLines[notificationTextLines.length - 1].toString();
                                haveContent = true;
                            } else {
                                if (ai.saiy.android.utils.UtilsList.notNaked(notificationTextLines) && ai.saiy.android.utils.UtilsString.notNaked(notificationText)) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: notificationTextLines & notificationText");
                                    }
                                    for (CharSequence charSequence: notificationTextLines) {
                                        if (notificationText.matches(Pattern.quote(charSequence.toString()))) {
                                            content = charSequence.toString();
                                            haveContent = true;
                                            break;
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        if (!ai.saiy.android.utils.UtilsList.notNaked(notificationTextLines)) {
                                            MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: notificationTextLines: naked");
                                        }
                                    }

                                    if (ai.saiy.android.utils.UtilsString.notNaked(notificationText)) {
                                        content = notificationText;
                                        haveContent = true;
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: notificationText: naked");
                                        }
                                        utterance = "Notification from " + applicationName + ". " + notification.tickerText;
                                    }
                                }
                            }

                            if (haveContent) {
                                if (matchWithPreviousWhatsApp(content)) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: previous content: discarding");
                                    }
                                    sr.reset();
                                    return;
                                } else {
                                    synchronized (whatsAppLock) {
                                        if (whatsAppContent.size() > MAX_PREVIOUS_CONTENT) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: purging array");
                                            }
                                            whatsAppContent.clear();
                                        }
                                        whatsAppContent.add(content);
                                    }
                                    utterance = "Notification from " + applicationName + ". " + notification.tickerText + ". " + content;
                                }
                            }
                        } else {
                            utterance = "Notification from " + applicationName + ". " + notification.tickerText;
                        }

                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "PACKAGE_WHATSAPP: " + utterance);
                        }
                        break;
                    case Installed.PACKAGE_GOOGLE_HANGOUT:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS");
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS: announceHangoutContent: " + ai.saiy.android.utils.SPH.getAnnounceNotificationsHangouts(getApplicationContext()));
                        }
                        if (ai.saiy.android.utils.SPH.getAnnounceNotificationsHangouts(getApplicationContext())) {
                            if (!ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString()) || matchWithPreviousHangout(notification.tickerText.toString())) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS: ticker naked or previous: discarding");
                                }
                                sr.reset();
                                return;
                            }
                            utterance = "You've received a Hangout from " + notification.tickerText;
                        } else {
                            title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(title) || matchWithPreviousHangout(title)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS: notificationTitle naked or previous: discarding");
                                }
                                sr.reset();
                                return;
                            }
                            if (title.contains(":")) {
                                String[] separated = title.split(":", 2);
                                if (separated.length <= 1) {
                                    utterance = "You've received a Hangout from, " + title;
                                } else {
                                    utterance = "You've received a Hangout in, " + separated[1].trim() + ". From, " + title;
                                }
                            } else {
                                utterance = "You've received a Hangout from, " + title;
                            }

                            synchronized (hangoutLock) {
                                if (hangoutTickerText.size() > MAX_PREVIOUS_CONTENT) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS: purging array");
                                    }
                                    hangoutTickerText.clear();
                                }
                                hangoutTickerText.add(notification.tickerText.toString());
                            }
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "PACKAGE_HANGOUTS: " + utterance);
                            }
                        }
                        break;
                    case Installed.PACKAGE_TWITTER:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_TWITTER");
                        }
                        if (!ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_TWITTER: ticker naked: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        utterance = "Notification from Twitter. " + notification.tickerText;
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "PACKAGE_TWITTER: " + utterance);
                        }
                        break;
                    case Installed.PACKAGE_FACEBOOK:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_FACEBOOK");
                        }
                        notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                        if (ai.saiy.android.utils.UtilsString.notNaked(notificationText)) {
                            utterance = "Notification from " + applicationName + ". " + notificationText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            utterance = "Notification from " + applicationName + ". " + notification.tickerText;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_FACEBOOK: no content: discarding");
                                MyLog.i(CLS_NAME, "PACKAGE_FACEBOOK: can't structure: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "PACKAGE_FACEBOOK: " + utterance);
                        }
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_DEFAULT");
                        }
                        title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                        notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                        if (ai.saiy.android.utils.UtilsString.notNaked(title) && ai.saiy.android.utils.UtilsString.notNaked(notificationText)) {
                            utterance = "Notification from " + applicationName + ". " + title + ". " + notificationText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(title) && ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            utterance = "Notification from " + applicationName + ". " + title + ". " + notification.tickerText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(notification.tickerText.toString())) {
                            utterance = "Notification from " + applicationName + ". " + notification.tickerText;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(title)) {
                            utterance = "Notification from " + applicationName + ". " + title;
                        } else if (ai.saiy.android.utils.UtilsString.notNaked(notificationText)) {
                            utterance = "Notification from " + applicationName + ". " + notificationText;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_DEFAULT: no content: discarding");
                                MyLog.i(CLS_NAME, "PACKAGE_DEFAULT: can't structure: discarding");
                            }
                            sr.reset();
                            return;
                        }

                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "PACKAGE_FACEBOOK: " + utterance);
                        }
                        break;
                }

                sr.reset();
                if (isRestrictedContent(utterance)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "restrictedContent: true");
                    }
                } else {
                    if (commandDelaySufficient(statusBarNotification.getPostTime())) {
                        if (commandPreviousMatches(utterance)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "commandPreviousMatches: true");
                            }
                            return;
                        }
                        previousCommandTime = statusBarNotification.getPostTime();
                        NLService.this.utterance = utterance;
                        final ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(getApplicationContext());
                        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, ai.saiy.android.utils.SPH.getVRLocale(getApplicationContext()), ai.saiy.android.utils.SPH.getTTSLocale(getApplicationContext()), utterance);
                        localRequest.setSpeechPriority(SpeechPriority.PRIORITY_NOTIFICATION);
                        localRequest.execute();
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "commandDelaySufficient: false");
                        }
                    }
                }
            }
        }).start();
    }

    private boolean matchWithPreviousMessage(String str) {
        boolean isFound = false;
        synchronized (messageLock) {
            messages.removeAll(Collections.singleton(null));
            messages.removeAll(Collections.singleton(""));
            for (String previousContent : messages) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "previousContent: " + previousContent);
                }
                if (previousContent.matches(Pattern.quote(str))) {
                    isFound = true;
                    break;
                }
            }
        }
        return isFound;
    }

    private boolean matchWithPreviousWhatsApp(String str) {
        boolean isFound = false;
        synchronized (whatsAppLock) {
            whatsAppContent.removeAll(Collections.singleton(null));
            whatsAppContent.removeAll(Collections.singleton(""));
            for (String previousContent : whatsAppContent) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "previousContent: " + previousContent);
                }
                if (previousContent.matches(Pattern.quote(str))) {
                    isFound = true;
                    break;
                }
            }
        }
        return isFound;
    }

    private boolean isDelayTooShort() {
        return System.currentTimeMillis() - then > (15 * 1000);
    }

    private boolean matchWithPreviousHangout(String str) {
        boolean isFound = false;
        synchronized (hangoutLock) {
            hangoutTickerText.removeAll(Collections.singleton(null));
            hangoutTickerText.removeAll(Collections.singleton(""));
            for (String previousContent : hangoutTickerText) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "previousContent: " + previousContent);
                }
                if (previousContent.matches(Pattern.quote(str))) {
                    isFound = true;
                    break;
                }
            }
        }
        return isFound;
    }

    private boolean isPackageRestrictedPackage(String str) {
        if (blockedApplications != null) {
            List<ApplicationBasic> applicationArray = blockedApplications.getApplicationArray();
            if (ai.saiy.android.utils.UtilsList.notNaked(applicationArray)) {
                for (ApplicationBasic applicationBasic : applicationArray) {
                    String packageName = applicationBasic.getPackageName();
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "isPackageRestrictedPackage: " + packageName + " ~ " + str);
                    }
                    if (ai.saiy.android.utils.UtilsString.notNaked(packageName) && applicationBasic.getPackageName().matches(str)) {
                        return true;
                    }
                }
            }
        }
        return saiy.matcher(str).matches() || googleDialer.matcher(str).matches() || phone.matcher(str).matches();
    }

    private boolean commandPreviousMatches(String str) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "commandPreviousMatches");
        }
        return utterance != null && !ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(utterance).find() && !ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(str).find() && ai.saiy.android.utils.UtilsString.regexCheck(utterance) && ai.saiy.android.utils.UtilsString.regexCheck(str) && utterance.matches(str);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onCreate");
        }
        this.then = System.currentTimeMillis();
        this.sl = SupportedLanguage.getSupportedLanguage(ai.saiy.android.utils.SPH.getVRLocale(getApplicationContext()));
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(getApplicationContext(), sl);
        this.blocked_input_method = sr.getString(R.string.blocked_input_method);
        this.blocked_location = sr.getString(R.string.blocked_location);
        this.is_typing = sr.getString(R.string.is_typing);
        this.an_unknown_application = sr.getString(R.string.an_unknown_application);
        sr.reset();
        this.isAnnounceNotificationRequired = ai.saiy.android.command.driving.DrivingProfileHelper.isAnnounceNotificationsEnabled(getApplicationContext()) || (ai.saiy.android.utils.SPH.getAnnounceNotifications(getApplicationContext()) && ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext()) && !(ai.saiy.android.device.UtilsDevice.isDeviceLocked(getApplicationContext()) && ai.saiy.android.utils.SPH.getAnnounceNotificationsSecure(getApplicationContext())));
        this.blockedApplications = ai.saiy.android.accessibility.BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
        this.messages = new ArrayList<>();
        this.hangoutTickerText = new ArrayList<>();
        this.whatsAppContent = new ArrayList<>();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if (isDelayTooShort()) {
            StatusBarNotification clone = statusBarNotification.clone();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                super.onNotificationPosted(statusBarNotification);
            }
            handleNotificationPosted(clone);
            return;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onNotificationPosted: delay too short");
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            super.onNotificationPosted(statusBarNotification);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        if (isDelayTooShort()) {
            StatusBarNotification clone = statusBarNotification.clone();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                super.onNotificationRemoved(statusBarNotification);
            }
            handleNotificationRemoved(clone);
            return;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onNotificationRemoved: delay too short");
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            super.onNotificationRemoved(statusBarNotification);
        }
    }
}
