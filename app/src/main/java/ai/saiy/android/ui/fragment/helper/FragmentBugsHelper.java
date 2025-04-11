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

package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIBugsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentBugs;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentBugsHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentHomeHelper.class.getSimpleName();

    private final FragmentBugs parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentBugsHelper(@NonNull final FragmentBugs parentFragment) {
        this.parentFragment = parentFragment;
    }

    /**
     * Get the components for this fragment
     *
     * @return a list of {@link ContainerUI} elements
     */
    private ArrayList<ContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUIComponents");
        }

        final ArrayList<ContainerUI> mObjects = new ArrayList<>();

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle("Bugs bugs bugs!");
        containerUI.setSubtitle("Saiy has been completely rebuilt from its previous incarnation as utter! So I'm afraid it's going to be a little buggy to begin with… \n\nThe good news is that I've open sourced the code, so I hope to have the expertise of many developers to help solve any issues quickly. If you'd like to get involved, head over to the Development Section!\n\nTap for the Development Section.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Ads are still showing!");
        containerUI.setSubtitle("If you've donated to remove ads and they are still appearing across the application, firstly, thank you for your donation! Secondly, apologies… Sometimes Google doesn't send back the correct result when the app attempts to verify your purchase. If this problem persists for more than 24 hours, please do email me on billing@saiy.ai with your purchase confirmation number.\n\nKilling the application and opening it again, can sometimes fix the issue.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Can I import my utter! commands?");
        containerUI.setSubtitle("I'm afraid not… Due to the vast differences in the complexity of the two apps and how custom commands function, it wasn't possible to migrate between the two, without opening the door to many errors. Forgive me…");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Recognition times out too quickly!");
        containerUI.setSubtitle("I know!! Google removed the option to stipulate a longer 'pause for breath' timeout a while ago and it desperately needs to be reinstated. Until then, take a deep breath before you compose anything lengthy…");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("I don't get a chance to speak!");
        containerUI.setSubtitle("I know!!! As well as the above, Google also removed the 'consider silence' timeout, so until they reinstate that, think of the recognition beep as a starting pistol… and be quick out of the blocks.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Alexa issues!");
        containerUI.setSubtitle("Lagging: I'm trying to resolve why the Alexa Service doesn't perform well with pre-recorded audio. To work around this, just say the command 'Ask Alexa' and then ask your question when Saiy prompts you. Alternatively, use the Alexa Action Button on the permanent notification, which can be enabled in the Settings.\n\nCereProc: There is an issue with CereProc Voices Engines attempting to play the audio response from Alexa. To work around this, simply make sure you have an alternative Voice Engine installed, such as Google TTS, with an English voice pack - and Saiy will use that behind the scenes.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_offline_recognition));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_offline_recognition));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_no_speech));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_no_speech));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_delay_speech));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_delay_speech));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("The recognition results are rubbish!?");
        containerUI.setSubtitle("This is often reported by users after an update to the Google Now/Assistant Application. Uninstall the update in your Android Applications Settings, reboot, then install the updates from the Play Store\n\nIf you are a new user, it is very often reported that the recognition improves dramatically after a small amount of usage.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Saiy uses a lot of memory!");
        containerUI.setSubtitle("The application works as a Service and Android allocates a higher amount of memory for this type of implementation, usually around 20mb. In addition, the application binds to both the Google Recognition Service and a Voice Engine. The Android Running Applications screen will show you the combined total of the three. \n\nPlease do remember that memory is not proportional to battery usage! In fact, it can be quite the opposite. Retaining objects in the memory prevents the same objects from being reloaded, over and over again, which, guess what - will drain your battery! \n\nThere is of course a happy medium and by default, the application releases its bindings to the voice and recognition services after 15 minutes of inactivity. Should you feel the need, you can lower (or increase) this timescale in the Superuser Settings. Just to reiterate, if Android needs to reclaim the memory, it will do so, and the application is designed to work harmoniously with such situations. If you leave the application dormant for 24 hours, you will note it will not even appear in the battery consumption screen at all. It's something I've worked hard on, as a necessity for the application to function in this unique way…\n\nTo flush the memory the application is using at any time, you can say the command 'restart'.\n\nTap for the Superuser Settings");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Saiy kills my battery!");
        containerUI.setSubtitle("If you are using the hotword detection, you should expect high battery usage, as it is permanently recording short audio bursts. Considering switching this off when it's no longer needed, or you step too far away from a charger…\n\nWhen you are initially setting the application up with your customisations, you may notice a high level of drain whilst you configure and test your commands. \n\nRecording audio, using the network for recognition and a voice engine to announce them, are all resource intensive functions. Once you have set the application up as you wish, it will use minimal resource whilst lying 'dormant' in the background.\n\nIf the Saiy Accessibility Service is enabled, to announce your notifications or interact with the Google Assistant, this can also cause a small, but continuous drain.\n\nTap for the Android Accessibility Settings.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("The hotword doesn't work?");
        containerUI.setSubtitle("This is an experimental feature and it's a little hit and miss to say the least. The phonetic detection needs a great deal of improvement and you may indeed find repeating the hotword five or six times does actually trigger it… As the implementation is open source, I'll be asking the developers to me help improve it.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Can I customise the hotword?");
        containerUI.setSubtitle("Not yet. But don't fear, this is high on my priority list! The hotword library and code I'm using is open source, so I hope the developers will jump in and help me out to get this up and running as soon as possible.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Okay Google doesn't work?");
        containerUI.setSubtitle("This is caused by a bug in various releases of the Google app and the Google Now Launcher. If you go to the Superuser Settings, you'll see an option to apply an 'Okay Google Fix' which I hope will resolve the issue in the short term.\n\n• Tap for Superuser Settings.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("SMS messages incorrect or not detected?");
        containerUI.setSubtitle("Some Android versions have an odd mapping between the contact and SMS tables. If you go to the Superuser Settings, you'll see an option to apply an 'SMS ID Fix' which will use a different method to find messages from specific contacts.\n\n• Tap for Superuser Settings.");;
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("The Recognizer is busy?");
        containerUI.setSubtitle("This is caused by a bug in various releases of the Google app and the Google Now Launcher. If you go to the Superuser Settings, you'll see an option to apply a 'Recognizer Busy Fix' which I hope will resolve the issue in the short term.\n\nIn order for the fix to take effect, you may need to kill the Google app.\n\n• Tap for Superuser Settings. Tap and hold to murder Google!");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("It just beeps twice quickly?");
        containerUI.setSubtitle("This is a variation of the issue directly above. Please follow the instructions there, but instead enable the Superuser option 'Double Beep Fix'");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Driving profile woes");
        containerUI.setSubtitle("I've had reports that the hotword detection will shutdown successfully, but then leave a notification to say the Driving Profile is still active and vice versa. I've not managed to get to the bottom of this yet! If you think you can replicate it happening under certain circumstances, please do use the Development Section to send me a bug report!\n\nOnce you've finished your journey, there will be a delay before the feature automatically turns off. This delay is so it doesn't deactivate every time you stop at traffic lights! It should be four or five minutes tops.\n\n• Tap for the Development Section.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Saiy shuts down when I swipe");
        containerUI.setSubtitle("If you find that Saiy is shutting down when you clear it from your recent apps list, this could be due to a number of reasons:\n\nIf you're running Android 4.X then I'm afraid it's a known bug and there is no cure :(\n\nSome device manufacturers, such as Huawei and Xiaomi, altered Android to deliberately behave in this way. To prevent this from happening, you'll need to either add Saiy as a 'Protect App' or use 'lock' under your recent apps list. \n\nUnfortunately, where such features and options are located varies from device to device, so you might need to search for your solution on the tinterweb.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Toggling mobile data fails");
        containerUI.setSubtitle("Google couldn't make up their mind as to whether to allow non System apps to control this feature or not. Therefore, it will work for some Android versions just fine. For others, I've managed to get it working using a 'hack'. For the remainder, I'm afraid it's bad news… \n\nThis will be available as a root command in a coming release.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Saiy can't answer calls");
        containerUI.setSubtitle("Doh! Trying to get this to work on all Android versions has given me grey hairs. Now my code is open source, I'm hoping some smart developer will come along and show me how it's done properly, to work for all…\n\nIf you're running Android Oreo, there is an issue with the text to speech engine being muted by the System. That really doesn't help…");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Nothing happens when I open Saiy?");
        containerUI.setSubtitle("Saiy does not provide an in-app-experience (yet). \n\nDepending on your device manufacturer and Android version, it can be activated in any one of the following ways:\n\n• Tapping the permanent notification\n• Long-press-home (Select search icon)\n• Long-press-home (6.0+ default 'assistant')\n• Quick settings tile (7.0+)\n• Launcher shortcut\n• Hotword (wake-up Saiy)\n\nYou can activate Saiy anywhere on the device, whilst using any application.\n\n• Tap for Assistant Settings. Tap and hold for Tile Settings.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("There's a problem with my TTS Engine?");
        containerUI.setSubtitle("When activated, Saiy gives your Text to Speech Engine 15 seconds to sort itself out and speak. If it doesn't manage it within this timescale, this error is raised. \n\nOn devices with lower hardware specifications, the Google Text to Speech Engine can take this long, or even longer, to initialise…Unfortunately, if this issue persists, you may need to switch to a lower quality voice engine.\n\nHave a read of 'there's a delay before speech' above, to see if you can improve the situation by following those instructions.\n\nTap for Text to Speech Settings");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Saiy doesn't recognise my commands!?");
        containerUI.setSubtitle("The speech recognition is currently powered by Google. Saiy uses advanced algorithms to analyse every sentence and individual word returned in an attempt to piece together your command - but if Google is returning gibberish, then I'm afraid the application doesn't stand a chance…\n\nHave a read above of 'the recognition results are rubbish' to see if any suggestions there help.\n\nDon't forget, you can create and tweak commands from the Customisation Section, using words and phrases that Google always returns correctly for you.\n\nForgive me, but I'll also have to ask you nicely to check the command list to make sure you are structuring the phrases correctly…\n\nIf you think the application should be responding to the natural language you are using, please do submit your suggestion in the Development Section.\n\nTap for the Customisation Section. Tap and hold for the Development Section.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Sometimes Saiy doesn't reply?");
        containerUI.setSubtitle("If you are using the Google Text to Speech network synthesised voice, an error occurring during the request is denoted by silence… Very aptly, I have no words…");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Edited text messages are blank?");
        containerUI.setSubtitle("Some manufacturers have changed they way they are handling this. In the Superuser Section, there is an option 'sms body fix'. Try toggling this and hopefully your content will begin to appear.\n\nTap for the Superuser Settings");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Error communicating with Tasker?");
        containerUI.setSubtitle("This is an Android install order issue where permissions are not correctly detected by the application installed second. The only solution to this is to reinstall Saiy and then Tasker will acknowledge the permissions. \n\nIf you click on 'Link Tasker' in the Supported Application Settings, Saiy should guide you through this process, but it all else fails, you will need to manually reinstall the application… *sigh*\n\nTap for the Supported Application Section");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("My Bluetooth headset doesn't work?");
        containerUI.setSubtitle("It would be a fantastic idea if nearly all headsets worked in a completely different way and then I could waste hours of my life trying to get them all to behave correctly. \n\nSo, if your headset isn't working please use the 'generic feedback' option in the Development Section to send me the make and model and I'll see what I can do.\n\nIn the Android Voice Search Settings, there are options to allow requests from bluetooth and wired headsets. This could possibly help your issue.\n\nTap for Voice Search Settings. Tap and hold for the Development Section");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("My wired headset doesn't work?");
        containerUI.setSubtitle("Google had a bright idea to let you choose which application handles the button press on your headset when the display is off, by showing you a confirmation dialog. \n\nUnfortunately, as the display is off, you can't see it. And when you turn on your display, the dialog disappears.\n\nNow I know what you're thinking, but stop. We all make mistakes. Some more than others admittedly… Fortunately, there's a wired headset fix in the Superuser Settings.\n\nTap for the Superuser Settings.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Saiy can't pronounce my name right!");
        containerUI.setSubtitle("In the Customisation Section, you can enter the phonetic spelling of your name. Hopefully after a couple of attempts, you'll get the voice engine pronouncing something near enough!\n\nTap for the Customisation Section");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Why is there a permanent notification?");
        containerUI.setSubtitle("Saiy runs in the background on your device and the permanent notification is required to make the Android System aware that it is 'important'. Without the notification, Android would continuously kill the process when managing memory. This often used to occur mid-command on early versions, so the permanent notification became a necessary permanent fixture…\n\nThe notification itself should be hidden from the status bar, unless you are using the hotword detection or the driving profile. It becomes visible under these circumstances to remind you to deactivate it when the time is right!\n\nI'm currently searching for a work-around to mimic this behaviour on Android Oreo, which has become super strict about ongoing processes. Therefore you're stuck for now with the notification in the status bar too :(");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Sometimes I think I'm a Snowman?");
        containerUI.setSubtitle("This has been reported by a large number of users. I'm currently working on a fix.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Saiy is slow at processing commands");
        containerUI.setSubtitle("There are a few reasons why this could be happening:\n\n• A poor network connection\n\nIf there is a delay in the recognition results, Saiy will display a 'fetching' notification, which remains in the notification bar until the results are returned from the Google Servers. The time between the end of your voice instruction and the notification changing to 'computing' is a reflection of your network connection speed.\n\nIf you are using a WiFi network, you should expect the voice results to be returned almost instantaneously after you finish speaking and all device based commands to be resolved within two tenths of a second from there. If this isn't your experience, please don't hesitate to report this a bug in the Development Section, so I can investigate the cause further.\n\nAs well a delay for the voice recognition, many commands will also need to make additional network calls to complete your requests.\n\n• Your device has low hardware specifications\n\nWhen Saiy attempts to resolve your command, it will work your device very hard to do this as quickly and 'efficiently' as possible. Unfortunately, some older and cheaper Android devices simply aren't equipped to do this in milliseconds, which is the timescale that is aimed for. \n\nTo avoid potential criticism of the of the application, certain devices with low RAM and CPU power really should be excluded as incompatible. I didn't want to do this though, as even the oldest of devices can be supercharged into a beast. Check out the XDA Developers website, if you're not sure what I mean. So, all devices are remain welcome, but please note this 'disclaimer'! Don't forget, rebooting your device can often clear out the cobwebs!\n\n• You've found a bug!\n\nOk, I admit it, there is a possibility I might have done something wrong… If the application fails to process your command in the 'standard' way, it often falls back to alternative methods, which are slower. Please do drop me a quick bug report in the Development Section and I'll investigate.\n\n• You are too popular!\n\nIf you are experiencing lag on contact based commands, it could well be because you simply have an awful lot of them and the comparison algorithms are consequently taking too long. Again, please consider dropping me a bug report and I'll see what I can do.\n\nTap for the Development Section");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Why are features disabled?");
        containerUI.setSubtitle("Features such as accessing the Wolfram Alpha knowledge base, translations, voice footprint, emotional analysis, definitions, weather conditions, stock prices etc aren't free for me to access, so until a 'premium' strategy is devised, they remain on a limited quota basis.\n\nUsing the donations that some users make, to pay the way for others to use the features for free, doesn't really seem fair on those that donate! I continue to ponder…\n\nAll root features are currently disabled, purely so I can focus on fixing any issues with the core commands. Don't worry though, they will be back!");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Skype ID's aren't detected?");
        containerUI.setSubtitle("In order for Saiy to detect the contact's Skype ID, you need to make sure this is stored correctly within their contact card. It should be entered under the 'IM' field heading, with the label option set to 'Skype'.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Can I ask Saiy any question I like?");
        containerUI.setSubtitle("Such knowledge-based responses will rely heavily on Wolfram Alpha, which is disabled currently due to the reasons above. In the Settings Section, if you set the 'unknown commands' option to Google, then anything the application doesn't understand, will be passed to the Google Assistant to have a go!\n\nTap for the Settings Section");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Will Saiy be multilingual?");
        containerUI.setSubtitle("It will indeed! I'll be entirely reliant on the help of my users to achieve this. There is a link in the Development Section if you'd like to get involved. Thanks in advance!\n\nTap for the Development Section");
        mObjects.add(containerUI);

        return mObjects;
    }

    /**
     * Get the recycler view for this fragment
     *
     * @param parent the view parent
     * @return the {@link RecyclerView}
     */
    public RecyclerView getRecyclerView(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }

        final RecyclerView mRecyclerView = (RecyclerView)
                parent.findViewById(R.id.layout_bugs_fragment_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        return mRecyclerView;
    }

    /**
     * Get the Edit Text for this fragment
     *
     * @param parent the view parent
     * @return the {@link EditText}
     */
    public EditText getEditText(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getEditText");
        }

        final EditText editText = (EditText) parent.findViewById(R.id.etCommand);
        editText.setOnEditorActionListener(getParent());
        editText.setImeActionLabel(getParent().getString(R.string.menu_run), EditorInfo.IME_ACTION_GO);
        return editText;
    }

    /**
     * Get the Image Button for this fragment
     *
     * @param parent the view parent
     * @return the {@link ImageButton}
     */
    public ImageButton getImageButton(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getImageButton");
        }
        final ImageButton imageButton = (ImageButton) parent.findViewById(R.id.ibRun);
        imageButton.setOnClickListener(getParent());
        return imageButton;
    }

    /**
     * Get the adapter for this fragment
     *
     * @param mObjects list of {@link ContainerUI} elements
     * @return the {@link UIBugsAdapter}
     */
    public UIBugsAdapter getAdapter(@NonNull final ArrayList<ContainerUI> mObjects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIBugsAdapter(mObjects, getParent(), getParent());
    }

    /**
     * Update the parent fragment with the UI components. If the drawer is not open in the parent
     * Activity, we can assume this method is called as a result of the back button being pressed, or
     * the first initialisation of the application - neither of which require a delay.
     */
    public void finaliseUI() {
        Schedulers.single().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentBugsHelper.this.getUIComponents();
                if (FragmentBugsHelper.this.getParent().isActive()) {

                    FragmentBugsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        FragmentBugsHelper.this.getParent().getObjects().addAll(tempArray);
                        FragmentBugsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentBugsHelper.this.getParent().getObjects().size());
                    }});

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        }, getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)? FragmentHome.DRAWER_CLOSE_DELAY : 0, TimeUnit.MILLISECONDS);
    }

    public void showLatestBugsDialog(ArrayList<String> bugs) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_latest_bugs)
                .setMessage(R.string.content_override_secure)
                .setIcon(R.drawable.ic_bug)
                .setItems(bugs.toArray(new String[0]), null)
                .setPositiveButton(R.string.menu_squash_them, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showLatestBugsDialog: onPositive");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showLatestBugsDialog: onCancel");
                        }
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this
     * fragment helper will never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */

    public ActivityHome getParentActivity() {
        return parentFragment.getParentActivity();
    }

    /**
     * Utility method to return the parent fragment this helper is helping.
     *
     * @return the parent fragment
     */
    public FragmentBugs getParent() {
        return parentFragment;
    }
}
