package ai.saiy.android.service;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
    private static final String CLS_NAME = NLService.class.getSimpleName();
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleNotificationRemoved(final StatusBarNotification statusBarNotification) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "handleNotificationRemoved");
        }
        Schedulers.computation().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (!isAnnounceNotificationRequired) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "handleNotificationRemoved: not required");
                    }
                    return;
                }
                if (!SPH.getSelfAwareEnabled(getApplicationContext())) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "handleNotificationRemoved: self aware disabled by user");
                    }
                    return;
                }
                if (DEBUG) {
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
                if (UtilsString.notNaked(statusBarNotification.getPackageName())) {
                    final String packageName = statusBarNotification.getPackageName();
                    switch (packageName) {
                        case Installed.PACKAGE_ANDROID_MESSAGING:
                        case Installed.PACKAGE_GOOGLE_MESSAGE:
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "onNotificationRemoved: MESSAGING: purging array");
                            }
                            synchronized (NLService.messageLock) {
                                messages.clear();
                            }
                            break;
                        case Installed.PACKAGE_GOOGLE_HANGOUT:
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "onNotificationRemoved: PACKAGE_HANGOUTS: purging array");
                            }
                            synchronized (NLService.hangoutLock) {
                                hangoutTickerText.clear();
                            }
                            break;
                        case Installed.PACKAGE_WHATSAPP:
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "onNotificationRemoved: PACKAGE_WHATSAPP: purging array");
                            }
                            synchronized (NLService.whatsAppLock) {
                                whatsAppContent.clear();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        });
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
        synchronized (NLService.lock) {
            if (ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(content).find() || !UtilsString.regexCheck(content)) {
                return true;
            }
            if (blockedApplications != null) {
                if (UtilsString.notNaked(blockedApplications.getText())) {
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleNotificationPosted(final StatusBarNotification statusBarNotification) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onNotificationPosted");
        }
        Schedulers.computation().scheduleDirect((new NotificationPostedTask(NLService.this, statusBarNotification)));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class NotificationPostedTask implements Runnable {
        private final NLService nlService;
        private final StatusBarNotification statusBarNotification;
        NotificationPostedTask(NLService nlService, StatusBarNotification sbn) {
            this.nlService = nlService;
            this.statusBarNotification = sbn;
        }

        @Override
        public void run() {
            final Context context = nlService.getApplicationContext();
            if (!SPH.getSelfAwareEnabled(context)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "handleNotificationPosted: self aware disabled by user");
                }
                return;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onNotificationPosted: DrivingProfileHelper.isProfileNotificationsEnabled: " + ai.saiy.android.command.driving.DrivingProfileHelper.isProfileNotificationsEnabled(context));
                MyLog.i(CLS_NAME, "onNotificationPosted: getAnnounceNotifications: " + SPH.getAnnounceNotifications(context));
                MyLog.i(CLS_NAME, "onNotificationPosted: QuietTimesHelper.canProceed: " + ai.saiy.android.quiet.QuietTimeHelper.canProceed(context));
                MyLog.i(CLS_NAME, "onNotificationPosted: getSuppressNotificationsSecure: " + SPH.getAnnounceNotificationsSecure(context));
            }
            boolean isAnnounceNotificationRequired = false;
            if (ai.saiy.android.command.driving.DrivingProfileHelper.isProfileNotificationsEnabled(context)) {
                isAnnounceNotificationRequired = true;
            } else if (SPH.getAnnounceNotifications(context)) {
                if (ai.saiy.android.quiet.QuietTimeHelper.canProceed(context)) {
                    if (!ai.saiy.android.device.UtilsDevice.isDeviceLocked(context)) {
                        isAnnounceNotificationRequired = true;
                    } else if (!SPH.getAnnounceNotificationsSecure(context)) {
                        isAnnounceNotificationRequired = true;
                    }
                }
            }
            nlService.updateServiceInfo(isAnnounceNotificationRequired);
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
                nlService.examineBundle(notification.extras);
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onNotificationPosted: notification: " + notification);
            }
            if (!UtilsString.notNaked(statusBarNotification.getPackageName())) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "package name naked");
                }
                return;
            } else if (nlService.isPackageRestrictedPackage(statusBarNotification.getPackageName())) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "package restricted");
                }
                return;
            }

            final Pair<Boolean, String> appNamePair = ai.saiy.android.applications.UtilsApplication.getAppNameFromPackage(context, statusBarNotification.getPackageName());
            final String applicationName = (appNamePair.first? appNamePair.second : nlService.an_unknown_application);
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
                if (UtilsList.notNaked(notificationTextLines)) {
                    for (CharSequence charSequence : notificationTextLines) {
                        MyLog.i(CLS_NAME, "EXTRA_TEXT_LINES: line: " + charSequence);
                    }
                } else {
                    MyLog.i(CLS_NAME, "EXTRA_TEXT_LINES: naked");
                }
                final String[] notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                if (UtilsList.notNaked(notificationPeople)) {
                    for (String charSequence : notificationPeople) {
                        MyLog.i(CLS_NAME, "EXTRA_PEOPLE: person: " + charSequence);
                    }
                } else {
                    MyLog.i(CLS_NAME, "EXTRA_PEOPLE: naked");
                }
                final String[] applications = notification.extras.getStringArray("android.foregroundApps"/*Notification.EXTRA_FOREGROUND_APPS*/);
                if (UtilsList.notNaked(applications)) {
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
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, nlService.sl);
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
                    if (!UtilsString.notNaked(notification.tickerText.toString())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_GMAIL: ticker naked: discarding");
                        }
                        sr.reset();
                        return;
                    }
                    notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                    if (!UtilsList.notNaked(notificationPeople)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_GMAIL: notificationPeople naked: discarding");
                        }
                        sr.reset();
                        return;
                    }

                    String emailAddress = notificationPeople[notificationPeople.length - 1].replaceFirst("mailto:", "").trim();
                    String contactName;
                    if (UtilsString.notNaked(emailAddress)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_GMAIL: emailAddress: " + emailAddress);
                        }
                        if (ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissionsNR(context)) {
                            contactName = new ai.saiy.android.contacts.ContactHelper().getNameFromEmail(context, emailAddress);
                            if (UtilsString.notNaked(contactName)) {
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
                            if (UtilsString.notNaked(notification.tickerText.toString())) {
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
                    if (UtilsList.notNaked(notificationTextLines)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: multiple message: discarding");
                        }
                        sr.reset();
                        return;
                    } else {
                        notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                        if (UtilsString.notNaked(notificationText) && nlService.matchWithPreviousMessage(notificationText)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: previous content: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        synchronized (NLService.messageLock) {
                            if (nlService.messages.size() > MAX_PREVIOUS_CONTENT) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: purging array");
                                }
                                nlService.messages.clear();
                            }
                            nlService.messages.add(notificationText);
                        }
                        notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                        String smsContactName = "an unknown sender";
                        if (!UtilsList.notNaked(notificationPeople) || notificationPeople.length != 1) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: unknown contact");
                            }
                            title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                            if (UtilsString.notNaked(title)) {
                                smsContactName = title;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: single known contact");
                            }
                            if (ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissionsNR(context)) {
                                smsContactName = new ai.saiy.android.contacts.ContactHelper().getNameFromUri(context, android.net.Uri.parse(notificationPeople[0]));
                            }
                            if (UtilsString.notNaked(smsContactName)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: smsContactName: " + smsContactName);
                                }
                            } else {
                                smsContactName = "an unknown sender";
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: smsContactName: naked");
                                }
                                title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                                if (UtilsString.notNaked(title)) {
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
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID_MESSAGING: announceSMSContent: " + SPH.getAnnounceNotificationsSMS(context));
                        }
                        if (SPH.getAnnounceNotificationsSMS(context)) {
                            if (UtilsString.notNaked(notificationText)) {
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
                    if (!UtilsString.notNaked(notification.tickerText.toString())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_SNAPCHAT: ticker naked: discarding");
                        }
                        sr.reset();
                        return;
                    }
                    if (notification.tickerText.toString().contains(nlService.is_typing)) {
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
                    if (UtilsString.notNaked(notificationText) && UtilsString.notNaked(title)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_EBAY: using title & text");
                        }
                        utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + title + ". " + notificationText;
                    } else if (UtilsString.notNaked(notificationText) && UtilsString.notNaked(notification.tickerText.toString())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_EBAY: using ticker & text");
                        }
                        utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + notification.tickerText + ". " + notificationText;
                    } else if (UtilsString.notNaked(notification.tickerText.toString())) {
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
                    if (UtilsString.notNaked(title) && (title.contains(context.getString(R.string.app_name)) || title.contains("Gmail"))) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID: title contains Saiy/Gmail: discarding");
                        }
                        sr.reset();
                        return;
                    }
                    if (UtilsString.notNaked(notificationText) && (notificationText.contains(context.getString(R.string.app_name)) || notificationText.contains("Gmail"))) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID: text contains Saiy/Gmail: discarding");
                        }
                        sr.reset();
                        return;
                    }

                    if (UtilsString.notNaked(notificationText) && notificationText.contains("Vocalizer")) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID: text contains Vocalizer: discarding");
                        }
                        sr.reset();
                        return;
                    }

                    if (UtilsString.notNaked(notificationText) && UtilsString.notNaked(title)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID: using title & text");
                        }
                        utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + title + ". " + notificationText;
                    } else if (UtilsString.notNaked(notificationText) && UtilsString.notNaked(notification.tickerText.toString())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PACKAGE_ANDROID: using ticker & text");
                        }
                        utterance = "Notification from " + statusBarNotification.getPackageName() + ". " + notification.tickerText + ". " + notificationText;
                    } else if (UtilsString.notNaked(notification.tickerText.toString())) {
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
                    if (!UtilsString.notNaked(notification.tickerText.toString())) {
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
                        MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: announceWhatsAppContent: " + SPH.getAnnounceNotificationsWhatsapp(context));
                    }
                    if (SPH.getAnnounceNotificationsWhatsapp(context)) {
                        notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
                        notificationTextLines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                        notificationPeople = notification.extras.getStringArray(Notification.EXTRA_PEOPLE);
                        if (DEBUG) {
                            if (!UtilsList.notNaked(notificationPeople)) {
                                MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: notificationPeople: naked");
                            }
                        }
                        String content = null;
                        boolean haveContent = false;
                        if (UtilsList.notNaked(notificationTextLines) && UtilsList.notNaked(notificationPeople)) {
                            content = notificationTextLines[notificationTextLines.length - 1].toString();
                            haveContent = true;
                        } else {
                            if (UtilsList.notNaked(notificationTextLines) && UtilsString.notNaked(notificationText)) {
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
                                    if (!UtilsList.notNaked(notificationTextLines)) {
                                        MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: notificationTextLines: naked");
                                    }
                                }

                                if (UtilsString.notNaked(notificationText)) {
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
                            if (nlService.matchWithPreviousWhatsApp(content)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: previous content: discarding");
                                }
                                sr.reset();
                                return;
                            } else {
                                synchronized (NLService.whatsAppLock) {
                                    if (nlService.whatsAppContent.size() > MAX_PREVIOUS_CONTENT) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "PACKAGE_WHATSAPP: purging array");
                                        }
                                        nlService.whatsAppContent.clear();
                                    }
                                    nlService.whatsAppContent.add(content);
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
                        MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS: announceHangoutContent: " + SPH.getAnnounceNotificationsHangouts(context));
                    }
                    if (SPH.getAnnounceNotificationsHangouts(context)) {
                        if (!UtilsString.notNaked(notification.tickerText.toString()) || nlService.matchWithPreviousHangout(notification.tickerText.toString())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS: ticker naked or previous: discarding");
                            }
                            sr.reset();
                            return;
                        }
                        utterance = "You've received a Hangout from " + notification.tickerText;
                    } else {
                        title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
                        if (!UtilsString.notNaked(title) || nlService.matchWithPreviousHangout(title)) {
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

                        synchronized (NLService.hangoutLock) {
                            if (nlService.hangoutTickerText.size() > MAX_PREVIOUS_CONTENT) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "PACKAGE_HANGOUTS: purging array");
                                }
                                nlService.hangoutTickerText.clear();
                            }
                            nlService.hangoutTickerText.add(notification.tickerText.toString());
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
                    if (!UtilsString.notNaked(notification.tickerText.toString())) {
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
                    if (UtilsString.notNaked(notificationText)) {
                        utterance = "Notification from " + applicationName + ". " + notificationText;
                    } else if (UtilsString.notNaked(notification.tickerText.toString())) {
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
                    if (UtilsString.notNaked(title) && UtilsString.notNaked(notificationText)) {
                        utterance = "Notification from " + applicationName + ". " + title + ". " + notificationText;
                    } else if (UtilsString.notNaked(title) && UtilsString.notNaked(notification.tickerText.toString())) {
                        utterance = "Notification from " + applicationName + ". " + title + ". " + notification.tickerText;
                    } else if (UtilsString.notNaked(notification.tickerText.toString())) {
                        utterance = "Notification from " + applicationName + ". " + notification.tickerText;
                    } else if (UtilsString.notNaked(title)) {
                        utterance = "Notification from " + applicationName + ". " + title;
                    } else if (UtilsString.notNaked(notificationText)) {
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
            if (nlService.isRestrictedContent(utterance)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "restrictedContent: true");
                }
            } else {
                if (nlService.commandDelaySufficient(statusBarNotification.getPostTime())) {
                    if (nlService.commandPreviousMatches(utterance)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "commandPreviousMatches: true");
                        }
                        return;
                    }
                    nlService.previousCommandTime = statusBarNotification.getPostTime();
                    nlService.utterance = utterance;
                    final ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(context);
                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, nlService.sl, SPH.getVRLocale(context), SPH.getTTSLocale(context), utterance);
                    localRequest.setSpeechPriority(SpeechPriority.PRIORITY_NOTIFICATION);
                    localRequest.execute();
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "commandDelaySufficient: false");
                    }
                }
            }
        }
    }

    private boolean matchWithPreviousMessage(String str) {
        boolean isFound = false;
        synchronized (NLService.messageLock) {
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
        synchronized (NLService.whatsAppLock) {
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
        synchronized (NLService.hangoutLock) {
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
            if (UtilsList.notNaked(applicationArray)) {
                for (ApplicationBasic applicationBasic : applicationArray) {
                    String packageName = applicationBasic.getPackageName();
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "isPackageRestrictedPackage: " + packageName + " ~ " + str);
                    }
                    if (UtilsString.notNaked(packageName) && applicationBasic.getPackageName().matches(str)) {
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
        return utterance != null && !ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(utterance).find() && !ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(str).find() && UtilsString.regexCheck(utterance) && UtilsString.regexCheck(str) && utterance.matches(str);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onCreate");
        }
        this.then = System.currentTimeMillis();
        this.sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(getApplicationContext()));
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(getApplicationContext(), sl);
        this.blocked_input_method = sr.getString(R.string.blocked_input_method);
        this.blocked_location = sr.getString(R.string.blocked_location);
        this.is_typing = sr.getString(R.string.is_typing);
        this.an_unknown_application = sr.getString(R.string.an_unknown_application);
        sr.reset();
        this.isAnnounceNotificationRequired = ai.saiy.android.command.driving.DrivingProfileHelper.isProfileNotificationsEnabled(getApplicationContext()) || (SPH.getAnnounceNotifications(getApplicationContext()) && ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext()) && !(ai.saiy.android.device.UtilsDevice.isDeviceLocked(getApplicationContext()) && SPH.getAnnounceNotificationsSecure(getApplicationContext())));
        this.blockedApplications = ai.saiy.android.accessibility.BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
        this.messages = new ArrayList<>();
        this.hangoutTickerText = new ArrayList<>();
        this.whatsAppContent = new ArrayList<>();
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
