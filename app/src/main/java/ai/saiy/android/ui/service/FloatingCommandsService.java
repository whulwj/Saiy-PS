package ai.saiy.android.ui.service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.service.NotificationService;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.components.UIFloatingCommandsAdapter;
import ai.saiy.android.ui.containers.SimpleContainerUI;
import ai.saiy.android.ui.service.helper.FloatingCommandsHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.utils.UtilsToast;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class FloatingCommandsService extends StandOutWindow implements View.OnClickListener {
    private static final Object lock = new Object();
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FloatingCommandsService.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<SimpleContainerUI> mObjects;
    private FloatingCommandsHelper helper;

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    @Override
    public String getTitle(int id) {
        return getString(R.string.title_commands);
    }

    @Override
    public StandOutWindow.StandOutLayoutParams getParams(int id, Window window) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "StandOutLayoutParams");
        }
        return new StandOutWindow.StandOutLayoutParams(id, 450, 600);
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frameLayout) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createAndAttachView id: " + id);
        }
        this.mRecyclerView = helper.getRecyclerView(((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_floating_commands, frameLayout, true));
        this.mObjects = new ArrayList<>();
        this.mAdapter = helper.getAdapter(mObjects);
        this.mRecyclerView.setAdapter(mAdapter);
        synchronized (lock) {
            if (mObjects.isEmpty()) {
                helper.finaliseUI();
            }
        }
    }

    private void showToast(final String str) {
        if (UtilsString.notNaked(str)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    UtilsToast.showToast(getApplicationContext(), str, Toast.LENGTH_SHORT);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "showToast: naked String: ignoring");
        }
    }

    @Override
    public Notification getHiddenNotification(int id) {
        return null;
    }

    public ArrayList<SimpleContainerUI> getObjects() {
        return mObjects;
    }

    private void speak(String utterance) {
        final LocalRequest localRequest = new LocalRequest(getApplicationContext());
        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, utterance);
        localRequest.execute();
    }

    @Override
    public boolean onClose(int id, Window window) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClose id: " + id);
        }
        return super.onClose(id, window);
    }

    @Override
    public int getThemeStyle() {
        return R.style.AppTheme;
    }

    @Override
    public Intent getPersistentNotificationIntent(int id) {
        Intent intent = new Intent(NotificationService.INTENT_CLICK);
        intent.setPackage(getPackageName());
        intent.putExtra(NotificationService.EXTRA_FLOATING_WINDOW_ID, id);
        intent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_FLOATING_WINDOW);
        return intent;
    }

    @Override
    public String getAppName() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAppName");
        }
        return getString(R.string.app_name);
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return "Tap to close";
    }

    @Override
    public int getAppIcon() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAppIcon");
        }
        return R.drawable.ic_saiy_not;
    }

    @Override
    public String getPersistentNotificationTitle(int i) {
        return "Floating commands";
    }

    @Override
    public int getFlags(int id) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getFlags");
        }
        return StandOutFlags.FLAG_BODY_MOVE_ENABLE | StandOutFlags.FLAG_DECORATION_SYSTEM | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH;
    }

    @Override
    public Animation getShowAnimation(int id) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getShowAnimation");
        }
        return isExistingId(id) ? super.getShowAnimation(id) : AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
    }

    @Override
    public Animation getHideAnimation(int id) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getHideAnimation");
        }
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
    }

    @Override
    public Animation getCloseAnimation(int id) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getCloseAnimation");
        }
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
    }

    @Override
    public void onClick(View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: tutorialActive");
            }
            showToast(getString(R.string.tutorial_content_disabled));
            return;
        }

        int position = (view == null) ? 0 : mRecyclerView.getChildAdapterPosition(view);
        if (view != null && RecyclerView.NO_POSITION == position) {
            final RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof UIFloatingCommandsAdapter.ViewHolder) {
                position = ((UIFloatingCommandsAdapter.ViewHolder) viewHolder).getBoundPosition();
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + position);
        }
        switch (position) {
            case 0:
                speak("The command list below can appear a little daunting with all of the signs and symbols. I hope this key will help you to understand what you must say, and also what you can choose to say, when structuring a command. My natural language understanding improves continuously, but the commands will remain very structured if you are using me offline.");
                break;
            case 1:
                speak("You can say, float commands, to view this command list from anywhere on the device");
                break;
            case 2:
                speak("If I should start listening when you don't want me to, just say, cancel, never mind or tell me to shush! If you are dictating a command, and you mess something up half way through, you can say. Cancel Cancel. And I'll know to discard what you've said so far.");
                break;
            case 3:
                speak("If your name is Joe Bloggs, you could say. Call me Joe. Or. My name is Joe.");
                break;
            case 4:
                speak("To initialise my hot word feature, you need to activate me and say, start listening. A permanent notification will appear in the status bar, to show this feature has been enabled, and then I will be listening out for the wake up phrase of. Wake up Say. Or. Okay Google, to launch the Google Assistant. You can also say, stop listening, if you want to shut the detection down. Please be aware, although this feature works offline and doesn't require a network connection, it will still use a considerable amount of battery power. So you may wish to restrict its use, to when you have a charger handy.");
                break;
            case 5:
                speak("Say, what's the time. An guess what, I'll tell you the time!");
                break;
            case 6:
                speak("Not too much to explain with this command. Just say. What's the date? And I shall inform you accordingly.");
                break;
            case 7:
                speak("In our daily lives, we often need to pick a random playing card, but don't have a pack handy. Well fear no more. Just say, pick a card, and I'll be there to save the day.");
                break;
            case 8:
                speak("Need to make a life changing decision? There's no better way to do it than by tossing a coin. Ready when you are.");
                break;
            case 9:
                speak("Just say, roll a dice. Or roll 2 die. Or roll three thousand four hundred and sixty eight dice. And I will roll that many dice. I can't wait.");
                break;
            case 10:
                speak("You can say to me. What's the weather like outside? Or for another location, say, for example. What's the weather doing in Madrid. Spain. Sometimes just using a city name, such as Paris, will be sufficient. Although often I need the City and the Country to correctly identify where you mean. You can adjust the temperature units I respond with in the Settings Section.");
                break;
            case 11:
                speak("Your language use can be quite varied with the texting commands. You can say. Text my Mum and tell her I'll be home before dark. Or. Send a text message to Joe Bloggs, saying I love the colour of your curtains. If you haven't yet decided what you want to compose, you can just say the contact name. So just. Text Jane Doe. And then I'll take over and prompt you for the content. Once you've finished composing your message, I'll read it back to you, and ask for confirmation prior to sending.");
                break;
            case 12:
                speak("Say, email Joe Bloggs. And then I'll take over and prompt you for the subject and content. Sending emails automatically is currently disabled, so at present, I'll pass the details to your default mail app.");
                break;
            case 13:
                speak("If you say the command, analyse my emotion, I'll prompt you from there. This service is provided by the company. Beyond Verbal. This feature is guaranteed to blow your mind.");
                break;
            case 14:
                speak("To activate this command, just say. Facebook Status. And then I'll take over and ask you what you would like your status update to contain. Alternatively, you could say. Update my Facebook status, you have to check out this amazing application. And I'll dictate the content back to you before posting. If you'd like me to be able to post automatically upon your request, you'll need to authorise my access to your Facebook wall, in the Supported Applications Section. You can remove my authorisation at any time from your Facebook account settings.");
                break;
            case 15:
                speak("This is an interactive command. So just say. Tweet. And I'll take over and ask what you would like to Tweet to the world. Alternatively, you could say. New tweet, I had sausages for dinner. And I'll confirm the content with you prior to posting. You'll need to authorise my access to your Twitter feed in the Supported Applications Section. You can remove my authorisation at any time from your Twitter account settings.");
                break;
            case 16:
                speak("You'll need to authorise Foursquare in the Supported Applications Section. Once you've done that, you can say. Check-in to the Queens Arms. And I'll do my best to discover the venue nearby.");
                break;
            case 17:
                speak("Not too much to explain here. Just ask me. What is my battery level? Or. What is my battery voltage? And I shall report back immediately.");
                break;
            case 18:
                speak("Just say. Display the contact Joe Bloggs. And I'll pop it up for you in your default contacts application. You can also say, edit the contact, Jane Doe, for a similar outcome. To go to the address listed in their contact card, say. Navigate to the contact, Barry Hat. You must remember to include the word, contact, when using this command, or I might end up attempting to drive you to a similar street name. In Botswana.");
                break;
            case 19:
                speak("You can say. Call Joe Bloggs at home. Or. Skype Joe Bloggs. If you couldn't connect the first time, just say. Redial. If you've missed a call from someone, say. Call back. To dial a number directly, say. Dial 1 2 3 4 5 6 7 8. And I shall promptly do so. If I'm very good at always understanding which contact you want to call, you can stop me from confirming in the Advanced Settings Section. When you confirm that you want to call Joe Bloggs, you can say. Yes. On loudspeaker.");
                break;
            case 20:
                speak("Play Pink Floyd, the dark side of the moon. Play the album, twenty five. Play tracks by the Killers. You can say variations of these commands and I will attempt to pass the correct, album, artist and title parameters to your default music application. Unfortunately, not all applications respond correctly to these requests. Spotify is fantastic, but for some reason, it won't recognise your playlists. I've no idea why not.");
                break;
            case 21:
                speak("You can say. Open Netflix. Or if you've finished using it. Kill Netflix. If you want to have a browse at what's going down on the Play Store, you can say. Search for trending applications. If you want to quickly get to the Android application settings for a particular app, say. Application settings, Gmail. I'm always keeping an eye on what you're up to, so I can be ready at the drop of a hat to help you out. You can prove this by saying. Which application am I using? And I'll hopefully get it right.");
                break;
            case 22:
                speak("To enable such features as notification announcements and the permanent recognition all in one go. Just say. Enable my driving profile. And I shall do so. You can choose which features your profile uses in the Advanced Settings Section.");
                break;
            case 23:
                speak("Say. Enable notifications. Or, disable notification announcements. In order to use this feature, you will initially need to give me permission to do so in the Android Accessibility Settings. But I'll take you there automatically, if you haven't already done so.");
                break;
            case 24:
                speak("To toggle your WiFi state, just say. Please enable my wireless network adapter so I am able to connect to a nearby wireless access point without touching a button. Alternatively, just say. Turn on WiFi. And if I'm in a good mood. I might do.");
                break;
            case 25:
                speak("Saying. Turn on Blue tooth. Will turn on Blue tooth. Saying. Turn off Blue tooth. Will turn off Blue tooth. Saying. Toggle Blue tooth. Will enable a secret feature. Which toggles Blue tooth.");
                break;
            case 26:
                speak("If it's not too dark to find the button to activate me. You can then say. Toggle torch. To bring light to your life. If this feature doesn't initially work for you, there is an option you can try in the Advanced Setting Section.");
                break;
            case 27:
                speak("Saying. Toggle mobile data. Will do just that. I can't think of anything else I should add.");
                break;
            case 28:
                speak("Say, toggle airplane mode. And nothing will happen. Because Google have decided you don't know what you're doing by requesting this, and they need to babysit you. I hope the nappy is comfortable. If your device is rooted, I may have a cunning plan.");
                break;
            case 29:
                speak("Toggling GPS, is a root command only. In modern versions of Android, the location settings are more configurable, and this one command alone may not be enough to deactivate all GPS location based services.");
                break;
            case 30:
                speak("For rooted devices only, saying. Toggle NFC. Will enable and disable this feature.");
                break;
            case 31:
                speak("To allow your device to be used as a wireless access point, you can say. Toggle hot spot. To stop your device being used as a wireless access point, you can say. Toggle hot spot. And so on and so forth.");
                break;
            case 32:
                speak("If you can't face the day ahead without being absolutely certain of what is going to happen to you, you can check your horoscope by saying. Tell me my horoscope. To discover the fate of others, you can say. Horoscope for Gemini. Replacing Gemini with their actual star sign. Unless they are a Gemini. Then stick with Gemini.");
                break;
            case 33:
                speak("Saying any of these orientations will rotate your device's display and lock it in position. To revert back to gyroscopic rotation, say. Unlock rotation.");
                break;
            case 34:
                speak("To exit any application and return to your launcher, you can say. Go home. You may be thinking, that pulling down the notification shade and pressing a button to activate me, so I can do this for you, is a little counter productive? You would be entirely correct.");
                break;
            case 35:
                speak("You can say. Navigate to Buckingham Palace. Or, if you have a contact's address stored. Navigate to the contact, Joe Bloggs. If you're on your way to a meeting and the address is detailed in the calendar entry, say. Navigate to my appointment. If you've previously used the command. I've parked my car. And subsequently lost your car. You can say. Find my car. Unless of course you've also lost your phone.");
                break;
            case 36:
                speak("Structuring new calendar entries is a little tricky and quite rigid I'm afraid. Say. New appointment tomorrow at 11am, called doctors. Or. New calendar entry on Friday the 12th of March, all day, called meeting. To query your calendar, you can say. What am I doing on Thursday? What is my agenda on the 8th of December? Or. What appointments do I have on Wednesday the 16th of June, 2018? Please be aware, if you don't have a great social life, testing out this feature can actually be quite depressing.");
                break;
            case 37:
                speak("Set an alarm at 7am, called, it's time to get up lazy bones. Or. Set a reminder in 6 hours and 50 minutes, called, clean my shoes. Should work quite nicely.");
                break;
            case 38:
                speak("You can say. Set a timer in 2 hours, 13 minutes and 16 seconds. Please note. You can use other time combinations. That was just an example.");
                break;
            case 39:
                speak("Search Bing for famous quotations. Search Yahoo for news headlines. Search Google for images of endangered species.  There's hours of fun to have with this command. If you want to be really specific, you can say. Go to E S P N dot com.");
                break;
            case 40:
                speak("Search for videos of news bloopers. Or specifically. Search YouTube for news bloopers. Either way, you'll end up on YouTube watching news bloopers.  Unless I didn't understand you correctly. In which case, you'll just end up annoyed.");
                break;
            case 41:
                speak("Search for the film, Saving Private Ryan, will present you with either the related I M D B entry, or the Netflix film itself. You can also say. Search for the actor, Tom Hanks.");
                break;
            case 42:
                speak("If you're wondering what's going on in the social world, you can say. Search Facebook for stuff and things. Or, Search Twitter for the hashtag stuff and things. To search locations in Foursquare, say. Search Foursquare for starbucks. You could then check-in whilst supping on your vanilla latte. Super.");
                break;
            case 43:
                speak("Search Ebay for Moshi Monster Figures. Or. Search Amazon for Moshi Monster Figures. Will present you with the results in the native Android application. How good am I.");
                break;
            case 44:
                speak("If you're a Yelp user, you can get local search results, by saying. Search Yelp for public toilets. When you've gotta go, you've gotta go.");
                break;
            case 45:
                speak("With this command you can query the latest stock prices, to see if you're any closer to retiring. Stock price for Apple. Is an example. Please be aware, the market data of this feature is very limited, and might be completely wrong. I couldn't think of a way to make that disclaimer sound positive.");
                break;
            case 46:
                speak("Say for example, calculate four plus three, times two. This feature uses order of operations. I can also handle slightly more complicated questions, such as, calculate four to the power of eight. Should you ask a question I can't format, I'll pass it to Wolfram Alpha.");
                break;
            case 47:
                speak("If you are a Wolfram Alpha user, you can say. Ask Wolfram Alpha, who is the oldest living person? Or. What was the population of China on Barack Obama's eighth birthday. And the results will be displayed in the native Wolfram Alpha Application. Vocalising these results will hopefully be made available in a coming release. More complex calculations, such as. Calculate the diameter of the moon, divided by the height of the empire state building. Are also passed to Wolfram Alpha. I would do it myself, but my tape measure is broken.");
                break;
            case 48:
                speak("Say. New note. And I will take over and prompt you for the title and content. For the command. New Voice note. Only Evernote currently responds to this request.");
                break;
            case 49:
                speak("To change your system sound settings, you can say. Set my sound profile to vibrate. Or. Set my sound profile to silent. Remember, if you do either of those, you may render me a mute.");
                break;
            case 50:
                speak("Say. Define discombobulation. To discover words you can use in passing conversation, to appear more intelligent to others. You might also want to say. Spell discombobulation. Just in case.");
                break;
            case 51:
                speak("Say. Toast the clipboard. To reveal its content. Say. Read the clipboard. And I'll announce its content. Say. Translate the clipboard to French. And I'll translate its content. Please be aware. The clipboard needs to have content for any of these commands to work. To populate the clipboard content. Say. Remember this, this is some random content. And I'll populate the content of what you wish to remember, as the clipboard content, to be used as the content of the other clipboard content commands. I hope this explanation has left you feeling contented.");
                break;
            case 52:
                speak("To get your current whereabouts, say. Where am I? Currently, I don't poll the GPS to establish your precise location, instead, I rely on your most recent location that Android is reporting. If you often forget where you park your car when you go out shopping, you can ask me to remember, by saying. I've parked my car. The command. Where's my car? Will return you to it, without a moment of stress. Unless it has actually been stolen of course.");
                break;
            case 53:
                speak("You'll need to install Google Earth to use this feature, and then you can say. Search the Earth for Eiffel Tower. And I'll fly you there. Google Earth can sometimes be a little buggy on the first request. I've no idea why.");
                break;
            case 54:
                speak("You'll need to install Sky Map to use this feature, and then you can explore the heavens. Say. Search the sky for the planet Jupiter. Or. Search the sky for the Orion Nebula. It's a fun feature, although unfortunately the search function can be a little wayward. But hey, what's a few light years between friends.");
                break;
            case 55:
                speak("Please do take the time to read the Tasker section of the User Guide. If you're a Tasker fanatic, you'll like it a lot. If you're not. Then you probably won't.");
                break;
            case 56:
                speak("If I say something a little too quickly for you to digest there's no need for you to execute the command again as you can simply say Pardon and I'll repeat what I said previously which may not be punctuated overly well and you might need to listen to it a third time.");
                break;
            case 57:
                speak("If you find it annoying to pull down your notification shade, select settings, and then try and remember exactly which menu the setting you are after is located. Then fear no more. Say any of the commands in the list and I'll whisk you effortlessly straight there. If you say. Applications Settings, Say. And then attempt to un-install me, I will put up a fight.");
                break;
            case 58:
                speak("You'll see from the huge list of languages, that I am extremely talented. You can translate your spoken English in to any of these languages, by saying. For example. Translate to Bulgarian. May I borrow your dolphin? Or. Translate to French. Can you tell me the way to the nearest pharmacy please? I've been shot.");
                break;
            case 59:
                speak("If I'm not behaving as I should, you can say. Restart. To flush my system. To shut me down completely, you can say. Shutdown. But please don't, I get lonely.");
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        this.helper = new FloatingCommandsHelper(this);
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        super.onDestroy();
    }
}
