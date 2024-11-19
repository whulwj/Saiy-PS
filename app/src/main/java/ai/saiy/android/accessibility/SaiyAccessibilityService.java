/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.util.Pair;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.Lists;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.driving.DrivingProfileHelper;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.tts.helper.SpeechPriority;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to handle accessibility service events.
 * Created by benrandall76@gmail.com on 03/08/2016.
 */

public class SaiyAccessibilityService extends AccessibilityService {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyAccessibilityService.class.getSimpleName();
    public static final String EXTRA_START_COMMAND_KEY = "start_command_key";
    private static final int DEFAULT = 0;
    private static final int GMAIL = 1;
    private static final int TEXT_MESSAGE = 2;

    private static final long COMMAND_UPDATE_DELAY = 5000L;
    private static final long UPDATE_TIMEOUT = 250L;
    private long previousCommandTime;
    private String previousCommandProcessed = null;

    private final Pattern saiy = Pattern.compile("ai\\.saiy\\.android", Pattern.CASE_INSENSITIVE);
    private final Pattern phone = Pattern.compile("com\\.android\\.phone", Pattern.CASE_INSENSITIVE);
    private final Pattern googleDialer = Pattern.compile("com\\.google\\.android\\.dialer", Pattern.CASE_INSENSITIVE);
    private final Pattern dialer = Pattern.compile("com\\.android\\.dialer", Pattern.CASE_INSENSITIVE);

    private SupportedLanguage sl;
    private String blockedInputMethod;
    private String blockedLocation;
    private String newMessages;
    private String anUnknownApplication;
    private boolean initAnnounceNotifications;
    private boolean initIgnoreRestrictedContent;
    private BlockedApplications blockedApplications;

    private static final boolean EXTRA_VERBOSE = false;

    @Override
    public void onCreate() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(getApplicationContext()));
        final SaiyResources sr = new SaiyResources(getApplicationContext(), sl);
        this.blockedInputMethod = sr.getString(R.string.blocked_input_method);
        this.blockedLocation = sr.getString(R.string.blocked_location);
        this.newMessages = sr.getString(R.string.new_messages);
        this.anUnknownApplication = sr.getString(R.string.an_unknown_application);
        sr.reset();
    }

    @Override
    protected void onServiceConnected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onServiceConnected");
        }

        initAnnounceNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && (DrivingProfileHelper.isAnnounceNotificationsEnabled(getApplicationContext()) || (SPH.getAnnounceNotifications(getApplicationContext()) && ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext()) && !(ai.saiy.android.device.UtilsDevice.isDeviceLocked(getApplicationContext()) && SPH.getAnnounceNotificationsSecure(getApplicationContext()))));
        blockedApplications = BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
        initIgnoreRestrictedContent = SPH.getIgnoreRestrictedContent(getApplicationContext());

        setDynamicContent();
    }

    /**
     * Set the content this service should be receiving
     */
    private void setDynamicContent() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setDynamicContent: announceNotifications: " + initAnnounceNotifications);
            MyLog.i(CLS_NAME, "setDynamicContent: ignoreRestrictedContent: " + initIgnoreRestrictedContent);
        }

        if (!initAnnounceNotifications) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDynamicContent: none required: finishing");
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDynamicContent: updating content");
            }

            final AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
            serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
            serviceInfo.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            serviceInfo.notificationTimeout = UPDATE_TIMEOUT;
            serviceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
            setServiceInfo(serviceInfo);
        }
    }

    /**
     * Check if we need to update the content we are receiving by comparing the current content values to the ones checked
     * on the most recent accessibility event.
     *
     * @param announceNotifications if we should be announcing notification content
     * @param ignoreRestrictedContent if we should ignore restricted content
     */
    private void updateServiceInfo(final boolean announceNotifications, final boolean ignoreRestrictedContent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateServiceInfo");
        }

        if (initAnnounceNotifications != announceNotifications) {
            initAnnounceNotifications = announceNotifications;
            initIgnoreRestrictedContent = ignoreRestrictedContent;
            setDynamicContent();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "updateServiceInfo: no change");
            }
        }
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onAccessibilityEvent");
        }
        if (!SPH.getSelfAwareEnabled(getApplicationContext())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccessibilityEvent: self aware disabled by user");
            }
            return;
        }

        updateServiceInfo(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && (DrivingProfileHelper.isAnnounceNotificationsEnabled(getApplicationContext()) || (SPH.getAnnounceNotifications(getApplicationContext()) && ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext()) && !(ai.saiy.android.device.UtilsDevice.isDeviceLocked(getApplicationContext()) && SPH.getAnnounceNotificationsSecure(getApplicationContext())))), SPH.getIgnoreRestrictedContent(getApplicationContext()));

        if (!initAnnounceNotifications) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccessibilityEvent: not required");
            }
            return;
        }

        if (event != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccessibilityEvent: contentDesc: " + event.getContentDescription());
                getEventType(event.getEventType());
            }
            AccessibilityNodeInfo source = null;
            if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                if (initAnnounceNotifications) {

                    final Parcelable parcelable = event.getParcelableData();

                    if (parcelable != null) {

                        if (parcelable instanceof Notification) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "fast instance of Notification: continuing");
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "fast not instance of Notification");
                            }
                            return;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "fast parcelable null");
                        }
                        return;
                    }

                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: fast not announcing notifications");
                    }
                    return;
                }
            }

            if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                final String packageName = event.getPackageName().toString();
                if (!UtilsString.notNaked(packageName)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "package name naked");
                    }
                    return;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "packageName: " + packageName);
                }
                if (isPackageRestricted(packageName)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "package restricted");
                    }
                    return;
                }
                String str;
                boolean noEventText;
                String eventText = null;
                final Pair<Boolean, String> appNameFromPackage = UtilsApplication.getAppNameFromPackage(getApplicationContext(), packageName);
                String applicationName = appNameFromPackage.first ? appNameFromPackage.second : anUnknownApplication;
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "applicationName: " + applicationName);
                }
                List<CharSequence> eventTextList = event.getText();
                if (UtilsList.notNaked(eventTextList)) {
                    StringBuilder sb = new StringBuilder();
                    for (CharSequence charSequence : eventTextList) {
                        if (charSequence != null) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "charSequence: " + charSequence);
                            }
                            sb.append(XMLResultsHandler.SEP_SPACE);
                            sb.append(charSequence);
                        }
                    }
                    str = sb.toString().trim();
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "eventTextList naked");
                    }
                    str = null;
                }
                if (UtilsString.notNaked(str)) {
                    eventText = str;
                    noEventText = false;
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "eventText naked");
                    }
                    if (initIgnoreRestrictedContent) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "eventText naked: ignoring restricted");
                        }
                    } else {
                        eventText = getString(R.string.announce_not_no_content);
                    }
                    noEventText = true;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "eventText: " + eventText);
                }
                if (!UtilsString.notNaked(eventText) || (!noEventText && isRestrictedContent(eventText))) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "content restricted");
                    }
                    return;
                }

                String utterance;
                SaiyResources sr = new SaiyResources(getApplicationContext(), sl);
                String quote = Pattern.quote(eventText);
                switch (getPackageType(packageName)) {
                    case GMAIL:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "GMAIL");
                        }
                        if (quote.contains(newMessages)) {
                            utterance = sr.getString(R.string.structured_email_1) + ". " + sr.getString(R.string.structured_email_2) + XMLResultsHandler.SEP_SPACE + eventText.replace(newMessages, "").trim() + XMLResultsHandler.SEP_SPACE + sr.getString(R.string.structured_email_3);
                        } else {
                            utterance = sr.getString(R.string.structured_email_4) + XMLResultsHandler.SEP_SPACE + eventText;
                        }
                        break;
                    case TEXT_MESSAGE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "TEXT_MESSAGE");
                        }
                        final boolean announceSMS = SPH.getAnnounceNotificationsSMS(getApplicationContext());
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "TEXT_MESSAGE: announce content: " + announceSMS);
                        }
                        if (quote.contains(":")) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "attempting to separate");
                            }
                            String[] split = eventText.split(":", 2);
                            if (split.length > 1) {
                                String senderName = split[0].trim();
                                String smsContent = split[1].trim();
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "senderName: " + senderName);
                                    MyLog.i(CLS_NAME, "smsContent: " + smsContent);
                                }
                                utterance = UtilsString.notNaked(senderName) ? announceSMS ? sr.getString(R.string.structured_sms_1) + XMLResultsHandler.SEP_SPACE + senderName + XMLResultsHandler.SEP_SPACE + sr.getString(R.string.saying) + ". " + smsContent : sr.getString(R.string.structured_sms_1) + XMLResultsHandler.SEP_SPACE + senderName : announceSMS ? sr.getString(R.string.structured_sms_1a) + ". " + eventText : sr.getString(R.string.structured_sms_1a);
                            } else {
                                utterance = announceSMS ? sr.getString(R.string.structured_sms_1a) + ". " + eventText : sr.getString(R.string.structured_sms_1a);
                            }
                        } else if (announceSMS) {
                            utterance = sr.getString(R.string.structured_sms_1a) + ". " + eventText;
                        } else {
                            utterance = sr.getString(R.string.structured_sms_1a);
                        }
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "DEFAULT");
                        }
                        utterance = sr.getString(R.string.structured_default) + XMLResultsHandler.SEP_SPACE + applicationName + ". " + eventText;
                        break;
                }
                sr.reset();
                if (!commandDelaySufficient(event.getEventTime())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandDelaySufficient: false");
                    }
                } else if (commandPreviousMatches(utterance)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousMatches: true");
                    }
                } else {
                    this.previousCommandTime = event.getEventTime();
                    this.previousCommandProcessed = utterance;
                    final LocalRequest localRequest = new LocalRequest(getApplicationContext());
                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, SPH.getVRLocale(getApplicationContext()), SPH.getTTSLocale(getApplicationContext()), utterance);
                    localRequest.setSpeechPriority(SpeechPriority.PRIORITY_NOTIFICATION);
                    localRequest.execute();
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: not interested in type");
                }

                if (EXTRA_VERBOSE) {
                    source = event.getSource();
                    if (source != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted contentDesc: " + source.getContentDescription());
                        }
                        if (source.getText() != null) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted text: " + source.getText().toString());
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted text: null");
                            }
                        }

                        final int childCount = source.getChildCount();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted childCount: " + childCount);
                        }

                        if (childCount > 0) {

                            for (int i = 0; i < childCount; i++) {

                                final String childText = examineChild(source.getChild(i));

                                if (childText != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted child text: " + childText);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            try {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: recycling source");
                }
                if (source != null) {
                    source.recycle();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onAccessibilityEvent: IllegalStateException source recycle");
                    e.printStackTrace();
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccessibilityEvent: event null");
            }
        }
    }

    /**
     * Check if the previous command was actioned within the {@link #COMMAND_UPDATE_DELAY}
     *
     * @param currentTime the time of the current {@link AccessibilityEvent}
     * @return true if the delay is sufficient to proceed, false otherwise
     */
    private boolean commandDelaySufficient(final long currentTime) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandDelaySufficient");
        }

        final long delay = (currentTime - COMMAND_UPDATE_DELAY);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandDelaySufficient: delay: " + delay);
            MyLog.i(CLS_NAME, "commandDelaySufficient: previousCommandTime: " + previousCommandTime);
        }

        return delay > previousCommandTime;
    }

    /**
     * Check if the previous command/text matches the previous text we processed
     *
     * @param text the current text
     * @return true if the text matches the previous text we processed, false otherwise.
     */
    private boolean commandPreviousMatches(@NonNull final String text) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandPreviousMatches");
        }

        return previousCommandProcessed != null && !ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(previousCommandProcessed).find() && !ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(text).find() && ai.saiy.android.utils.UtilsString.regexCheck(previousCommandProcessed) && ai.saiy.android.utils.UtilsString.regexCheck(text) && previousCommandProcessed.matches(text);
    }

    private int getPackageType(String packageName) {
        if (packageName.matches(Installed.PACKAGE_GMAIL)) {
            return GMAIL;
        }
        if (packageName.matches(Installed.PACKAGE_ANDROID_MESSAGING) || packageName.matches(Installed.PACKAGE_GOOGLE_MESSAGE)) {
            return TEXT_MESSAGE;
        }
        final String defaultSMSPackage = ai.saiy.android.command.sms.SmsHelper.getDefaultSMSPackage(getApplicationContext());
        if (!ai.saiy.android.utils.UtilsString.notNaked(defaultSMSPackage) || !packageName.matches(defaultSMSPackage)) {
            return DEFAULT;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "canStructure default SMS provider");
        }
        return TEXT_MESSAGE;
    }

    private boolean isPackageRestricted(String name) {
        if (blockedApplications != null) {
            List<ApplicationBasic> applicationArray = blockedApplications.getApplicationArray();
            if (UtilsList.notNaked(applicationArray)) {
                String packageName;
                for (ApplicationBasic applicationBasic : applicationArray) {
                    packageName = applicationBasic.getPackageName();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "isPackageRestrictedPackage: " + packageName + " ~ " + name);
                    }
                    if (UtilsString.notNaked(packageName) && applicationBasic.getPackageName().matches(name)) {
                        return true;
                    }
                }
            }
        }
        return saiy.matcher(name).matches() || dialer.matcher(name).matches() || phone.matcher(name).matches() || googleDialer.matcher(name).matches();
    }

    private boolean isRestrictedContent(String str) {
        ArrayList<String> arrayList;
        if (ai.saiy.android.nlu.local.Profanity.pProfanity.matcher(str).find() || !ai.saiy.android.utils.UtilsString.regexCheck(str)) {
            return true;
        }
        if (blockedApplications != null) {
            if (ai.saiy.android.utils.UtilsString.notNaked(blockedApplications.getText())) {
                arrayList = Lists.newArrayList(com.google.common.base.Splitter.on(XMLResultsHandler.SEP_COMMA).trimResults().split(blockedApplications.getText()));
                arrayList.removeAll(Collections.singleton(null));
                arrayList.removeAll(Collections.singleton(""));
            } else {
                arrayList = new ArrayList<>();
            }
            arrayList.add(blockedInputMethod);
            arrayList.add(blockedLocation);
            for (String restrictedContent : arrayList) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isRestrictedContent: " + restrictedContent);
                }
                if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(str, restrictedContent)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Recursively examine the {@link AccessibilityNodeInfo} object
     *
     * @param parent the {@link AccessibilityNodeInfo} parent object
     * @return the extracted text or null if no text was contained in the child objects
     */
    private String examineChild(@Nullable final AccessibilityNodeInfo parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineChild");
        }

        if (parent != null) {

            for (int i = 0; i < parent.getChildCount(); i++) {

                final AccessibilityNodeInfo nodeInfo = parent.getChild(i);

                if (nodeInfo != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "examineChild: nodeInfo: getClassName: " + nodeInfo.getClassName());
                        MyLog.i(CLS_NAME, "examineChild: nodeInfo: contentDesc: " + nodeInfo.getContentDescription());
                    }

                    if (nodeInfo.getText() != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "examineChild: have text: returning: " + nodeInfo.getText().toString());
                        }
                        return nodeInfo.getText().toString();
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "examineChild: text: null: recurse");
                        }

                        final int childCount = nodeInfo.getChildCount();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "examineChild: childCount: " + childCount);
                        }

                        if (childCount > 0) {

                            final String text = examineChild(nodeInfo);

                            if (text != null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "examineChild: have recursive text: returning: " + text);
                                }
                                return text;
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "examineChild: recursive text: null");
                                }
                            }
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "examineChild: nodeInfo null");
                    }
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "examineChild: parent null");
            }
        }

        return null;
    }

    /**
     * Check the event type for debugging
     *
     * @param eventType the Accessibility event type
     * @return the Accessibility event type
     */
    private int getEventType(final int eventType) {

        switch (eventType) {

            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_ANNOUNCEMENT");
                }
                break;
            case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_ASSIST_READING_CONTEXT");
                }
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_GESTURE_DETECTION_END");
                }
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_GESTURE_DETECTION_START");
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_NOTIFICATION_STATE_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_EXPLORATION_GESTURE_END");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_EXPLORATION_GESTURE_START");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_INTERACTION_END");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_INTERACTION_START");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_CLICKED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_CONTEXT_CLICKED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_FOCUSED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_HOVER_ENTER");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_HOVER_EXIT");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_LONG_CLICKED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_SCROLLED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_SELECTED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_TEXT_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_TEXT_SELECTION_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY");
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_WINDOW_CONTENT_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_WINDOW_STATE_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_WINDOWS_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPES_ALL_MASK:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPES_ALL_MASK");
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onAccessibilityEvent: default");
                }
                break;
        }

        return eventType;
    }

    @Override
    public void onInterrupt() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onInterrupt");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStartCommand");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || intent == null || !intent.hasExtra(EXTRA_START_COMMAND_KEY)) {
            initAnnounceNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && (DrivingProfileHelper.isAnnounceNotificationsEnabled(getApplicationContext()) || (SPH.getAnnounceNotifications(getApplicationContext()) && ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext()) && !(ai.saiy.android.device.UtilsDevice.isDeviceLocked(getApplicationContext()) && SPH.getAnnounceNotificationsSecure(getApplicationContext()))));
            blockedApplications = BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
            initIgnoreRestrictedContent = SPH.getIgnoreRestrictedContent(getApplicationContext());
            setDynamicContent();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onStartCommand: disabling");
            }
            disableSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }
}
