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

package ai.saiy.android.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import ai.saiy.android.R;
import ai.saiy.android.applications.Install;
import ai.saiy.android.firebase.UserFirebaseListener;
import ai.saiy.android.firebase.UtilsFirebase;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.tts.helper.SpeechPriority;
import ai.saiy.android.ui.activity.helper.ActivityHomeHelper;
import ai.saiy.android.ui.fragment.FragmentAbout;
import ai.saiy.android.ui.fragment.FragmentAdvancedSettings;
import ai.saiy.android.ui.fragment.FragmentApplications;
import ai.saiy.android.ui.fragment.FragmentCommands;
import ai.saiy.android.ui.fragment.FragmentCustomisation;
import ai.saiy.android.ui.fragment.FragmentDevelopment;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.ui.fragment.FragmentSettings;
import ai.saiy.android.ui.fragment.FragmentSuperUser;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.UtilsAuth;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsString;
import me.drakeet.support.toast.ToastCompat;

/**
 * Main activity class that handles the fragment management
 * <p>
 * Created by benrandall76@gmail.com on 23/08/2016.
 */

public class ActivityHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener,
        UserFirebaseListener {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityHome.class.getSimpleName();

    private static final long ARBITRARY_WAIT = 2000L;
    private static final long VIBRATE_MIN = 40L;

    public static final int ANIMATION_NONE = 0;
    public static final int ANIMATION_FADE = 1;
    public static final int ANIMATION_FADE_DELAYED = 2;

    public static final int INDEX_FRAGMENT_HOME = 0;
    public static final int INDEX_FRAGMENT_SETTINGS = 1;
    public static final int INDEX_FRAGMENT_CUSTOMISATION = 2;
    public static final int INDEX_FRAGMENT_ADVANCED_SETTINGS = 3;
    public static final int INDEX_FRAGMENT_SUPER_USER = 4;
    public static final int INDEX_FRAGMENT_ABOUT = 5;
    public static final int INDEX_FRAGMENT_BUGS = 6;
    public static final int INDEX_FRAGMENT_DEVELOPMENT = 7;
    public static final int INDEX_FRAGMENT_SUPPORTED_APPS = 8;
    public static final int INDEX_FRAGMENT_COMMANDS = 10;
    public static final int INDEX_FRAGMENT_DIAGNOSTICS = 11;
    public static final int INDEX_FRAGMENT_EDIT_CUSTOMISATION = 12;
    public static final int INDEX_FRAGMENT_EXPORT_CUSTOMISATION = 13;

    public static final int MENU_INDEX_HOME = 0;
    public static final int MENU_INDEX_SETTINGS = 1;
    public static final int MENU_INDEX_CUSTOMISATION = 2;
    public static final int MENU_INDEX_ADVANCED_SETTINGS = 3;
    public static final int MENU_INDEX_SUPER_USER = 4;
    public static final int MENU_INDEX_DEVELOPMENT = 5;
    public static final int MENU_INDEX_SUPPORTED_APPS = 6;
    public static final int MENU_INDEX_ABOUT = 7;

    public static final int INDEX_DIALOG_USER_GUIDE = 1;

    public static final String FRAGMENT_INDEX = "fragment_index";
    public static final String DIALOG_INDEX = "dialog_index";

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private NavigationView navigationView;
    private Menu menu;

    private final ActivityHomeHelper helper = new ActivityHomeHelper();
    private FirebaseAuth firebaseAuth;
    private volatile boolean isUserSignedIn;
    private volatile boolean havePersisted;
    private final AtomicInteger signInCount = new AtomicInteger();
    private final FirebaseAuth.AuthStateListener mAuth = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            final com.google.firebase.auth.FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAuthStateChanged: firebaseUser null");
                }
                ActivityHome.this.signInAnonymously();
                return;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAuthStateChanged: firebaseUser signed in: " + firebaseUser.getUid());
                MyLog.i(CLS_NAME, "onAuthStateChanged: firebaseUser anonymous: " + firebaseUser.isAnonymous());
            }
            ActivityHome.this.isUserSignedIn = true;
            if (firebaseUser.isAnonymous()) {
                SPH.setFirebaseAnonymousUid(getApplicationContext(), firebaseUser.getUid());
            } else {
                SPH.setFirebaseUid(getApplicationContext(), firebaseUser.getUid());
            }
            if (ActivityHome.this.havePersisted) {
                return;
            }
            ActivityHome.this.havePersisted = true;
            ActivityHome.this.persistFirebase();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_layout);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        final Pair<Boolean, Integer> intentPair = startFragment(getIntent());

        if (intentPair.first) {
            final Pair<Fragment, String> fragmentPair = getFragmentAndTag(intentPair.second);
            doFragmentReplaceTransaction(fragmentPair.first, fragmentPair.second, ANIMATION_NONE);
        } else {
            doFragmentReplaceTransaction(FragmentHome.newInstance(null), String.valueOf(INDEX_FRAGMENT_HOME),
                    ANIMATION_NONE);
        }

        setupUI();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Give the UI some time to set up before we start work on any user interaction processes
     */
    @Override
    public void onAttachedToWindow() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onAttachedToWindow");
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runStartConfiguration();
                        }
                    });
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onAttachedToWindow:  NullPointerException");
                        e.printStackTrace();
                    }
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onAttachedToWindow:  Exception");
                        e.printStackTrace();
                    }
                }
            }
        }, ARBITRARY_WAIT);
    }

    /**
     * Check to see if we need to interact with the user
     */
    public void runStartConfiguration() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runStartConfiguration");
        }

        if (acceptedDisclaimer()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "acceptedDisclaimer");
            }

            if (hasSelectedLanguage()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "hasSelectedLanguage");
                }

                if (seenDeveloperNote()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "seenDeveloperNote");
                    }

                    if (seenWhatsNew()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "seenWhatsNew: all done");
                        }

                        checkTTSInstallation();
                    } else {
                        showWhatsNew();
                    }
                } else {
                    showDeveloperNote();
                }
            } else {
                showLanguageSelector();
            }
        } else {
            showDisclaimer();
        }
    }

    /**
     * Check if the user has seen the most recent what's new message
     *
     * @return true if the message has been seen, false otherwise
     */
    private boolean seenWhatsNew() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "seenWhatsNew");
        }
        return SPH.getWhatsNew(getApplicationContext());
    }

    /**
     * Check if the user has seen the latest developer note
     *
     * @return true if the note has been seen, false otherwise
     */
    private boolean seenDeveloperNote() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "seenDeveloperNote");
        }
        return SPH.getDeveloperNote(getApplicationContext());
    }

    /**
     * Check if the user has chosen their supported language
     *
     * @return true if a language has been selected, false otherwise
     */
    private boolean hasSelectedLanguage() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hasSelectedLanguage");
        }
        return true;
    }

    /**
     * Check if the user has accepted the application disclaimer
     *
     * @return true if the disclaimer has been accepted, false otherwise
     */
    private boolean acceptedDisclaimer() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "acceptedDisclaimer");
        }
        return SPH.getAcceptedDisclaimer(getApplicationContext());
    }

    private void checkTTSInstallation() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkTTSInstallation");
        }
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        if (ai.saiy.android.utils.UtilsList.notNaked(getPackageManager().queryIntentActivities(intent, PackageManager.GET_META_DATA))) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkTTSInstallation: all ok");
            }
            SelfAwareHelper.startSelfAwareIfRequired(getApplicationContext());
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "checkTTSInstallation: no engine");
            }
            helper.showNoTTSDialog(this);
        }
    }

    /**
     * Self explanatory utility
     */
    private void setupUI() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupUI");
        }

        setupToolbar();
        setupDrawer();
        setupNavigation();

        if (Global.PROJECT_ID.equals("GCP_PROJECT_ID")) {
            Toast.makeText(this, "Please update the GCP_PROJECT_ID in strings.xml",
                    Toast.LENGTH_LONG).show();
//            finish();
            return;
        }
        checkPermissions();
        helper.checkAppRestrictionsStatus(this);
    }

    /**
     * Show the user the what's new message
     */
    protected void showWhatsNew() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showWhatsNew");
        }
        helper.showWhatsNew(this);
    }

    /**
     * Show the user the developer note
     */
    protected void showDeveloperNote() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showDeveloperNote");
        }
        helper.showDeveloperNote(this);
    }

    /**
     * We need to get the user to select a supported language
     */
    public void showLanguageSelector() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showLanguageSelector");
        }
        helper.showLanguageSelector(this);
    }

    /**
     * Show the user the disclaimer dialog
     */
    protected void showDisclaimer() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showDisclaimer");
        }
        helper.showDisclaimer(this);
    }

    /**
     * Self explanatory utility
     */
    private void setupToolbar() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupToolbar");
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    /**
     * Self explanatory utility
     */
    private void setupDrawer() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupDrawer");
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Self explanatory utility
     */
    private void setupNavigation() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupNavigation");
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void checkPermissions() {
        int permissionsCount = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)? 5:4;
        final boolean isAcceptableToRequestNotificationPermission = helper.isAcceptableToRequestNotificationPermission();
        if (isAcceptableToRequestNotificationPermission) {
            ++permissionsCount;
        }
        final String[] permissions = new String[permissionsCount];
        permissions[0] = Manifest.permission.INTERNET;
        permissions[1] = Manifest.permission.RECORD_AUDIO;
        permissions[2] = Manifest.permission.READ_EXTERNAL_STORAGE;
        permissions[3] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[4] = Manifest.permission.ACTIVITY_RECOGNITION;
        }
        if (isAcceptableToRequestNotificationPermission) {
            permissions[5] = "android.permission.POST_NOTIFICATIONS"/*android.Manifest.permission.POST_NOTIFICATIONS*/;
        }

        mChatPermissionRequest.launch(permissions);
    }

    /**
     * Get the Fragment and its Tag
     *
     * @param fragmentIndex the fragment index constant
     * @return an {@link Pair} with the first parameter containing the {@link Fragment} and the second the tag
     */
    private Pair<Fragment, String> getFragmentAndTag(final int fragmentIndex) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getFragmentAndTag");
        }

        switch (fragmentIndex) {

            case INDEX_FRAGMENT_HOME:
                return new Pair<>((Fragment) FragmentHome.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_HOME));
            case INDEX_FRAGMENT_SETTINGS:
                return new Pair<>((Fragment) FragmentSettings.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_SETTINGS));
            case INDEX_FRAGMENT_CUSTOMISATION:
                return new Pair<>((Fragment) FragmentCustomisation.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_CUSTOMISATION));
            case INDEX_FRAGMENT_ADVANCED_SETTINGS:
                return new Pair<>((Fragment) FragmentAdvancedSettings.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_ADVANCED_SETTINGS));
            case INDEX_FRAGMENT_SUPER_USER:
                return new Pair<>((Fragment) FragmentSuperUser.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_SUPER_USER));
            case INDEX_FRAGMENT_ABOUT:
                return new Pair<>((Fragment) FragmentAbout.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_ABOUT));
            case INDEX_FRAGMENT_DEVELOPMENT:
                return new Pair<>(FragmentDevelopment.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_DEVELOPMENT));
            case INDEX_FRAGMENT_SUPPORTED_APPS:
                return new Pair<>(FragmentApplications.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_SUPPORTED_APPS));
            case INDEX_FRAGMENT_COMMANDS:
                return new Pair<>(FragmentCommands.newInstance(null), String.valueOf(INDEX_FRAGMENT_COMMANDS));
            default:
                return new Pair<>((Fragment) FragmentHome.newInstance(null),
                        String.valueOf(INDEX_FRAGMENT_HOME));
        }
    }

    /**
     * Check if an intent received contains extras to start a specific fragment
     *
     * @param intent the intent to examine
     * @return a {@link Pair} with the first parameter denoting success and the second the fragment index constant
     */
    private Pair<Boolean, Integer> startFragment(@Nullable final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startFragment");
        }

        if (intent != null) {

            final Bundle bundle = intent.getExtras();

            if (UtilsBundle.notNaked(bundle)) {
                if (bundle.containsKey(FRAGMENT_INDEX)) {
                    return new Pair<>(true, bundle.getInt(FRAGMENT_INDEX, INDEX_FRAGMENT_HOME));
                }
            }
        }

        return new Pair<>(false, null);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onNewIntent");
        }

        final Pair<Boolean, Integer> intentPair = startFragment(intent);

        if (intentPair.first) {
            final Pair<Fragment, String> fragmentPair = getFragmentAndTag(intentPair.second);
            doFragmentReplaceTransaction(fragmentPair.first, fragmentPair.second, ANIMATION_NONE);
        }
        if (intent.hasExtra(ActivityHome.DIALOG_INDEX)) {
            if (intent.getIntExtra(ActivityHome.DIALOG_INDEX, 0) == INDEX_DIALOG_USER_GUIDE) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNewIntent: INDEX_DIALOG_USER_GUIDE");
                }
                helper.showUserGuideDialog(this);
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBackPressed");
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {

                try {

                    switch (Integer.valueOf(getSupportFragmentManager().findFragmentById(R.id.fragmentContent).getTag())) {

                        case INDEX_FRAGMENT_HOME:
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "onBackPressed: INDEX_FRAGMENT_HOME");
                            }
                            super.onBackPressed();
                            break;
                        default:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onBackPressed: default");
                            }

                            doFragmentReplaceTransaction(FragmentHome.newInstance(null), String.valueOf(INDEX_FRAGMENT_HOME),
                                    ANIMATION_FADE);
                            navigationView.setCheckedItem(R.id.nav_home);
                            navigationView.getMenu().getItem(INDEX_FRAGMENT_HOME).setChecked(true);
                            break;
                    }

                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onBackPressed: NullPointerException");
                        e.printStackTrace();
                    }
                    super.onBackPressed();
                } catch (final NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onBackPressed: NumberFormatException");
                        e.printStackTrace();
                    }
                    super.onBackPressed();
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onBackPressed: have back stack");
                }

                final String backStackTag = getSupportFragmentManager().getBackStackEntryAt(0).getName();
                final int navId = getNavIdFromTag(backStackTag);
                updateNavigationView(navId);
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(
                        R.id.fragmentContent)).commit();
                getSupportFragmentManager().popBackStack(backStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateOptionsMenu");
        }
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPrepareOptionsMenu");
        }
        if ((SPH.getSelfAwareEnabled(getApplicationContext()))) {
            menu.findItem(R.id.action_power).setIcon(R.drawable.ic_power);
        } else {
            menu.findItem(R.id.action_power).setIcon(R.drawable.ic_power_rt);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContent);
        if (!(fragment instanceof FragmentHome)) {
            menu.findItem(R.id.action_power).setVisible(false);
            menu.findItem(R.id.action_mic).setVisible(false);
        } else {
            menu.findItem(R.id.action_power).setVisible(true);
            menu.findItem(R.id.action_mic).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onOptionsItemSelected");
        }
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onOptionsItemSelected: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return super.onOptionsItemSelected(item);
        }
        if (R.id.action_power == item.getItemId()) {
            if (SPH.getSelfAwareEnabled(getApplicationContext())) {
                SPH.setSelfAwareEnabled(getApplicationContext(), false);
                toast(getString(R.string.will_shutdown_on_exit), Toast.LENGTH_SHORT);
                item.setIcon(R.drawable.ic_power_rt);
            } else {
                toast(getString(R.string.menu_enabled), Toast.LENGTH_SHORT);
                vibrate();
                SPH.setSelfAwareEnabled(getApplicationContext(), true);
                item.setIcon(R.drawable.ic_power);
            }
            return true;
        } else if (R.id.action_mic == item.getItemId()) {
            if (SPH.isFirstForMicroPhone(getApplicationContext())) {
                SPH.markMicroPhoneAccession(getApplicationContext());
                if (SPH.getUsedIncrement(getApplicationContext()) >= SPH.TRESS) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onOptionsItemSelected: run tutorial: false: but usage ok");
                    }
                    final LocalRequest localRequest = new LocalRequest(getApplicationContext());
                    localRequest.prepareIntro();
                    localRequest.execute();
                    return true;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onOptionsItemSelected: suggesting tutorial");
                }
                helper.showStartTutorialDialog(this);
                return true;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onOptionsItemSelected: run tutorial");
            }
            final LocalRequest localRequest = new LocalRequest(getApplicationContext());
            localRequest.prepareIntro();
            localRequest.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isFragmentLoading(@NonNull final String tag) {
        return getSupportFragmentManager().findFragmentById(R.id.fragmentContent).getTag().matches(tag);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onNavigationItemSelected");
        }

        drawer.closeDrawer(GravityCompat.START);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final int currentTag = Integer.parseInt(ActivityHome.this.getSupportFragmentManager().findFragmentById(
                        R.id.fragmentContent).getTag());
                final int id = item.getItemId();
                boolean proceed;

                switch (currentTag) {

                    case INDEX_FRAGMENT_HOME:
                        proceed = (id != R.id.nav_home);
                        break;
                    case INDEX_FRAGMENT_SETTINGS:
                        proceed = (id != R.id.nav_settings);
                        break;
                    case INDEX_FRAGMENT_CUSTOMISATION:
                        proceed = (id != R.id.nav_customisation);
                        break;
                    case INDEX_FRAGMENT_ADVANCED_SETTINGS:
                        proceed = (id != R.id.nav_advanced_settings);
                        break;
                    case INDEX_FRAGMENT_SUPER_USER:
                        proceed = (id != R.id.nav_super_user);
                        break;
                    case INDEX_FRAGMENT_ABOUT:
                        proceed = (id != R.id.nav_about);
                        break;
                    case INDEX_FRAGMENT_BUGS:
                        proceed = true;
                        break;
                    case INDEX_FRAGMENT_DEVELOPMENT:
                        proceed = (id != R.id.nav_development);
                        break;
                    case INDEX_FRAGMENT_SUPPORTED_APPS:
                        proceed = (id != R.id.nav_supported_apps);
                        break;
                    case INDEX_FRAGMENT_COMMANDS:
                        proceed = true;
                        break;
                    case INDEX_FRAGMENT_DIAGNOSTICS:
                        proceed = true;
                        break;
                    case INDEX_FRAGMENT_EDIT_CUSTOMISATION:
                        proceed = true;
                        break;
                    case INDEX_FRAGMENT_EXPORT_CUSTOMISATION:
                        proceed = true;
                        break;
                    default:
                        proceed = false;
                        break;
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNavigationItemSelected: proceeding: " + proceed);
                }

                if (proceed) {

                    String tag = null;
                    Fragment fragment = null;
                    if (R.id.nav_home == id) {
                        fragment = FragmentHome.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_HOME);
                    } else if (R.id.nav_settings == id) {
                        fragment = FragmentSettings.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_SETTINGS);
                    } else if (R.id.nav_customisation == id) {
                        fragment = FragmentCustomisation.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_CUSTOMISATION);
                    } else if (R.id.nav_advanced_settings == id) {
                        fragment = FragmentAdvancedSettings.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_ADVANCED_SETTINGS);
                    } else if (R.id.nav_super_user == id) {
                        fragment = FragmentSuperUser.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_SUPER_USER);
                    } else if (R.id.nav_development == id) {
                        fragment = FragmentDevelopment.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_DEVELOPMENT);
                    } else if (R.id.nav_supported_apps == id) {
                        fragment = FragmentApplications.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_SUPPORTED_APPS);
                    } else if (R.id.nav_about == id) {
                        fragment = FragmentAbout.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_ABOUT);
                    } else if (R.id.nav_share == id) {
                        if (!ExecuteIntent.shareIntent(getApplicationContext(),
                                Install.getSaiyInstallLink(getApplicationContext()))) {
                            toast(getString(R.string.error_no_application), Toast.LENGTH_LONG);
                        }
                    } else if (R.id.nav_feedback == id) {
                        if (!ExecuteIntent.sendDeveloperEmail(getApplicationContext())) {
                            toast(getString(R.string.error_no_application), Toast.LENGTH_LONG);
                        }
                    } else if (R.id.nav_twitter == id) {
                        ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_TWITTER_HANDLE);
                    } else if (R.id.nav_google_plus == id) {
                        ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_GOOGLE_PLUS_URL);
                    } else if (R.id.nav_forum == id) {
                        ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_XDA_URL);
                    } else {
                        fragment = FragmentHome.newInstance(null);
                        tag = String.valueOf(INDEX_FRAGMENT_HOME);
                    }

                    if (fragment != null) {

                        final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

                        if (backStackCount > 0) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onNavigationItemSelected: backStackCount: " + backStackCount);
                            }

                            final String backStackTag = getSupportFragmentManager()
                                    .getBackStackEntryAt(backStackCount - 1).getName();

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onNavigationItemSelected: comparing backStackTag: " + backStackTag
                                        + " ~ tag: " + tag);
                            }

                            if (backStackTag.matches(tag)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onNavigationItemSelected: tags match");
                                }

                                final int navId = getNavIdFromTag(backStackTag);
                                updateNavigationView(navId);

                                ActivityHome.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityHome.this.getSupportFragmentManager().beginTransaction().remove(ActivityHome.this.getSupportFragmentManager()
                                                .findFragmentById(R.id.fragmentContent)).commit();

                                        ActivityHome.this.getSupportFragmentManager().popBackStack(backStackTag,
                                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    }
                                });

                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onNavigationItemSelected: tags different: removing stack");
                                }

                                // TODO - remove unwanted fragment transaction from back stack. Unable to find a
                                // TODO - way to do this without full fragment lifecycle being called.

                                doFragmentReplaceTransaction(fragment, tag, ANIMATION_FADE_DELAYED);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onNavigationItemSelected: backStackCount 0");
                            }

                            ActivityHome.this.doFragmentReplaceTransaction(fragment, tag, ANIMATION_FADE_DELAYED);
                        }
                    }
                }
            }
        });

        return true;
    }

    /**
     * Helper method to replace the current fragment
     *
     * @param fragment to transact
     * @param tag      of the fragment
     */
    public void doFragmentReplaceTransaction(@NonNull final Fragment fragment, @NonNull final String tag,
                                             final int fade) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final int navId = getNavIdFromTag(tag);

                ActivityHome.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ActivityHome.this.updateNavigationView(navId);

                        if (menu != null) {
                            menu.findItem(R.id.action_power).setVisible(Integer.parseInt(tag) == INDEX_FRAGMENT_HOME);
                            menu.findItem(R.id.action_mic).setVisible(Integer.parseInt(tag) == INDEX_FRAGMENT_HOME);
                        }

                        switch (fade) {

                            case ANIMATION_NONE:
                                ActivityHome.this.getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragmentContent, fragment, tag).commit();
                                break;
                            case ANIMATION_FADE:
                                ActivityHome.this.getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.fade_in_slow, R.anim.none)
                                        .replace(R.id.fragmentContent, fragment, tag).commit();
                                break;
                            case ANIMATION_FADE_DELAYED:
                                ActivityHome.this.getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.fade_in_slow_delayed, R.anim.none)
                                        .replace(R.id.fragmentContent, fragment, tag).commit();
                                break;
                        }
                    }
                });
            }
        });
    }

    /**
     * Helper method to replace the current fragment
     *
     * @param fragment to transact
     * @param tag      of the fragment
     */
    public void doFragmentAddTransaction(@NonNull final Fragment fragment, @NonNull final String tag,
                                         final int fade, final int backStackTag) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final int navId = ActivityHome.this.getNavIdFromTag(tag);

                ActivityHome.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ActivityHome.this.updateNavigationView(navId);

                        if (menu != null) {
                            menu.findItem(R.id.action_power).setVisible(Integer.parseInt(tag) == INDEX_FRAGMENT_HOME);
                            menu.findItem(R.id.action_mic).setVisible(Integer.parseInt(tag) == INDEX_FRAGMENT_HOME);
                        }

                        switch (fade) {

                            case ANIMATION_NONE:
                                ActivityHome.this.getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragmentContent, fragment, tag)
                                        .addToBackStack(String.valueOf(backStackTag)).commit();
                                break;
                            case ANIMATION_FADE:
                                ActivityHome.this.getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.fade_in_slow, R.anim.none)
                                        .replace(R.id.fragmentContent, fragment, tag)
                                        .addToBackStack(String.valueOf(backStackTag)).commit();
                                break;
                            case ANIMATION_FADE_DELAYED:
                                ActivityHome.this.getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.fade_in_slow_delayed, R.anim.none)
                                        .replace(R.id.fragmentContent, fragment, tag)
                                        .addToBackStack(String.valueOf(backStackTag)).commit();
                                break;
                        }
                    }
                });
            }
        });
    }

    private void updateNavigationView(final int navId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateNavigationView: " + navId);
        }

        if (isActive()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Fragment fragment = ActivityHome.this.getSupportFragmentManager().findFragmentById(R.id.fragmentContent);
                    if (navId == MENU_INDEX_HOME) {
                        if (fragment == null) {
                            navigationView.getMenu().getItem(MENU_INDEX_HOME).setChecked(false);
                            return;
                        }
                        switch (Integer.valueOf(fragment.getTag())) {

                            case INDEX_FRAGMENT_HOME:
                                navigationView.getMenu().getItem(MENU_INDEX_HOME).setChecked(false);
                                break;
                            case INDEX_FRAGMENT_SETTINGS:
                                navigationView.getMenu().getItem(MENU_INDEX_SETTINGS).setChecked(false);
                                break;
                            case INDEX_FRAGMENT_CUSTOMISATION:
                                navigationView.getMenu().getItem(MENU_INDEX_CUSTOMISATION).setChecked(false);
                                break;
                            case INDEX_FRAGMENT_ADVANCED_SETTINGS:
                                navigationView.getMenu().getItem(MENU_INDEX_ADVANCED_SETTINGS).setChecked(false);
                                break;
                            case INDEX_FRAGMENT_SUPER_USER:
                                navigationView.getMenu().getItem(MENU_INDEX_SUPER_USER).setChecked(false);
                                break;
                            case INDEX_FRAGMENT_ABOUT:
                                navigationView.getMenu().getItem(MENU_INDEX_ABOUT).setChecked(false);
                                break;
                            case INDEX_FRAGMENT_DEVELOPMENT:
                                navigationView.getMenu().getItem(MENU_INDEX_DEVELOPMENT).setChecked(false);
                                break;
                            case INDEX_FRAGMENT_SUPPORTED_APPS:
                                navigationView.getMenu().getItem(MENU_INDEX_SUPPORTED_APPS).setChecked(false);
                                break;
                        }

                    } else {
                        if (R.id.nav_home == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_HOME).setChecked(true);
                        } else if (R.id.nav_settings == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_SETTINGS).setChecked(true);
                        } else if (R.id.nav_customisation == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_CUSTOMISATION).setChecked(true);
                        } else if (R.id.nav_advanced_settings == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_ADVANCED_SETTINGS).setChecked(true);
                        } else if (R.id.nav_super_user == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_SUPER_USER).setChecked(true);
                        } else if (R.id.nav_development == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_DEVELOPMENT).setChecked(true);
                        } else if (R.id.nav_supported_apps == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_SUPPORTED_APPS).setChecked(true);
                        } else if (R.id.nav_about == navId) {
                            navigationView.getMenu().getItem(MENU_INDEX_ABOUT).setChecked(true);
                        }

                        navigationView.setCheckedItem(navId);
                    }
                }
            });
        }
    }

    /**
     * Get the navigation id corresponding to the Fragment's tag
     *
     * @param tag of the Fragment
     * @return the navigation id
     */
    private int getNavIdFromTag(@NonNull final String tag) {

        switch (Integer.parseInt(tag)) {

            case INDEX_FRAGMENT_HOME:
                return R.id.nav_home;
            case INDEX_FRAGMENT_SETTINGS:
                return R.id.nav_settings;
            case INDEX_FRAGMENT_CUSTOMISATION:
                return R.id.nav_customisation;
            case INDEX_FRAGMENT_ADVANCED_SETTINGS:
                return R.id.nav_advanced_settings;
            case INDEX_FRAGMENT_SUPER_USER:
                return R.id.nav_super_user;
            case INDEX_FRAGMENT_ABOUT:
                return R.id.nav_about;
           case INDEX_FRAGMENT_DEVELOPMENT:
                return R.id.nav_development;
            case INDEX_FRAGMENT_SUPPORTED_APPS:
                return R.id.nav_supported_apps;
            default:
                return 0;
        }
    }

    /**
     * Expose the {@link DrawerLayout}
     *
     * @return the layout
     */
    public DrawerLayout getDrawer() {
        return this.drawer;
    }

    public void startTutorial() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContent);
        if (fragment instanceof FragmentHome && isActive()) {
            runOnUiThread(new Runnable() {
                public void run() {
                    ((FragmentHome) fragment).onClick(null);
                }
            });
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "startTutorial: fragment detached");
        }
    }

    /**
     * Utility method to show or hide the progress bar
     *
     * @param visible true to show, false to hide
     */
    public void showProgress(final boolean visible) {
        if (isActive()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }
    }

    /**
     * Utility method to vocalise any UI content or descriptions
     *
     * @param resId  to the String to speak
     * @param action one of {@link LocalRequest#ACTION_SPEAK_ONLY} {@link LocalRequest#ACTION_SPEAK_LISTEN}
     */
    public void speak(final int resId, final int action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(ActivityHome.this.getApplicationContext()));
                ActivityHome.this.speak(SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, resId), action);
            }
        }).start();
    }

    /**
     * Utility method to vocalise any UI content or descriptions
     *
     * @param utterance to the String to speak
     * @param action    one of {@link LocalRequest#ACTION_SPEAK_ONLY} {@link LocalRequest#ACTION_SPEAK_LISTEN}
     */
    public void speak(@NonNull final String utterance, final int action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final LocalRequest request = new LocalRequest(ActivityHome.this.getApplicationContext());
                request.prepareDefault(action, utterance);
                request.execute();
            }
        }).start();
    }

    /**
     * Utility method to toast making sure it's on the main thread
     *
     * @param text   to toast
     * @param duration one of {@link Toast#LENGTH_SHORT} {@link Toast#LENGTH_LONG}
     */
    public void toast(@Nullable final String text, final int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }

        if (UtilsString.notNaked(text)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastCompat.makeText(ActivityHome.this.getApplicationContext(), text, duration).show();
                }
            });
        }
    }

    /**
     * Utility method to show a snack bar object
     *
     * @param view     the parent view to attach
     * @param text     to show
     * @param toAction the action
     * @param length   one of {@link Snackbar#LENGTH_SHORT} {@link Snackbar#LENGTH_LONG}
     *                 {@link Snackbar#LENGTH_INDEFINITE}
     */
    public void snack(@Nullable final View view, @Nullable final String text, final int length, @Nullable final String toAction,
                      @Nullable final View.OnClickListener onClickListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "snack: " + text);
        }

        if (view != null && UtilsString.notNaked(text)) {

            if (UtilsString.notNaked(toAction)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(view, text, length).setAction(toAction, onClickListener).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(view, text, length).show();
                    }
                });
            }
        }
    }

    /**
     * Utility method to provide haptic feedback
     */
    public void vibrate() {
        if (SPH.getVibrateCondition(getApplicationContext())) {
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VIBRATE_MIN);
        }
    }

    /**
     * Called whenever the contents of the back stack change.
     */
    @Override
    public void onBackStackChanged() {

        final int count = getSupportFragmentManager().getBackStackEntryCount();

        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBackStackChanged: count: " + count);
        }

        if (count > 0) {
            switch (Integer.valueOf(getSupportFragmentManager().getBackStackEntryAt(count - 1).getName())) {

                case INDEX_FRAGMENT_HOME:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_HOME");
                    }
                    break;
                case INDEX_FRAGMENT_SETTINGS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_SETTINGS");
                    }
                    break;
                case INDEX_FRAGMENT_CUSTOMISATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_CUSTOMISATION");
                    }
                    break;
                case INDEX_FRAGMENT_ADVANCED_SETTINGS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_ADVANCED_SETTINGS");
                    }
                    break;
                case INDEX_FRAGMENT_SUPER_USER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_SUPER_USER");
                    }
                    break;
                case INDEX_FRAGMENT_ABOUT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_ABOUT");
                    }
                    break;
                case INDEX_FRAGMENT_BUGS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_BUGS");
                    }
                    break;
                case INDEX_FRAGMENT_DEVELOPMENT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_DEVELOPMENT");
                    }
                    break;
                case INDEX_FRAGMENT_SUPPORTED_APPS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_SUPPORTED_APPS");
                    }
                    break;
                case INDEX_FRAGMENT_COMMANDS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_COMMANDS");
                    }
                    break;
                case INDEX_FRAGMENT_DIAGNOSTICS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_DIAGNOSTICS");
                    }
                    break;
                case INDEX_FRAGMENT_EDIT_CUSTOMISATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_EDIT_CUSTOMISATION");
                    }
                    break;
                case INDEX_FRAGMENT_EXPORT_CUSTOMISATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onBackStackChanged: INDEX_FRAGMENT_EXPORT_CUSTOMISATION");
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onBackStackChanged: default");
                    }
                    break;
            }
        }
    }

    public boolean isActive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !isDestroyed() && !isFinishing();
        } else {
            return !isFinishing();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPause");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.firebaseAuth != null) {
            this.firebaseAuth.removeAuthStateListener(mAuth);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.firebaseAuth != null) {
            this.firebaseAuth.addAuthStateListener(mAuth);
        } else if (DEBUG) {
            MyLog.e(CLS_NAME, "mAuth null");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResume");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRequestPermissionsResult");
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContent);
        if ((fragment instanceof FragmentHome) && isActive()) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }

        if (Global.isInVoiceTutorial()) {
            Global.setVoiceTutorialState(getApplicationContext(), false);
            Bundle bundle = new Bundle();
            bundle.putInt(LocalRequest.EXTRA_SPEECH_PRIORITY, SpeechPriority.PRIORITY_TUTORIAL);
            bundle.putBoolean(LocalRequest.EXTRA_PREVENT_RECOGNITION, true);
            new ai.saiy.android.service.helper.LocalRequest(getApplicationContext(), bundle).execute();
        }
        if (!SPH.getSelfAwareEnabled(getApplicationContext()) &&
                SelfAwareHelper.selfAwareRunning(getApplicationContext())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onDestroy: stopping service");
            }
            SelfAwareHelper.stopService(getApplicationContext());
        }

        System.gc();
    }

    private final ActivityResultLauncher<String[]> mChatPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
    });

    private void persistFirebase() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "persistFirebase");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true);
/*                final com.google.firebase.database.DatabaseReference databaseReference = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read");
                databaseReference.child("bing").child("translate").keepSynced(true);
                databaseReference.child("bing").child("speaker_recognition").keepSynced(true);
                databaseReference.child("provider").child("translation").keepSynced(true);
                databaseReference.child("provider").child("weather").keepSynced(true);
                databaseReference.child("bugs").child("known_bugs").keepSynced(true);
                databaseReference.child("google").child("translate").keepSynced(true);
                databaseReference.child("google").child("geo").keepSynced(true);
                databaseReference.child("open_weather_map").keepSynced(true);
                databaseReference.child("twitter").keepSynced(true);
                databaseReference.child("weather_online").keepSynced(true);
                databaseReference.child("wordnik").keepSynced(true);
                databaseReference.child("beyond_verbal").keepSynced(true);
                databaseReference.child("foursquare").keepSynced(true);
                databaseReference.child("version").keepSynced(true);
                databaseReference.child("iap").keepSynced(true);*/
                final ai.saiy.android.firebase.UserFirebase userFirebase = UtilsFirebase.getUserFirebase(getApplicationContext());
                if (userFirebase != null) {
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read_write").child("users").child(userFirebase.getUid()).keepSynced(true);
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "persistFirebase: userFirebase null");
                }
                new UserFirebaseHelper().isAdFree(getApplication(), ActivityHome.this);
            }
        }).start();
    }

    /**
     * function to sign in to Firebase Anonymously
     */
    private void signInAnonymously() {
        firebaseAuth.signInAnonymously().addOnCompleteListener(Executors.newSingleThreadExecutor(), new com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "signInAnonymously: onComplete: " + task.isSuccessful());
                }
                if (task.isSuccessful()) {
                    // Sign in success
                    ActivityHome.this.isUserSignedIn = true;
                    UtilsAuth.getFirebaseInstanceId();
                    return;
                }
                if (DEBUG) {
                    task.getException().printStackTrace();
                }
                // If sign in fails, try it again later.
                if (ActivityHome.this.signInCount.incrementAndGet() < 4) {
                    try {
                        Thread.sleep(ActivityHome.this.signInCount.get() * 5000);
                    } catch (InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "signInAnonymously InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public boolean userSignedIn() {
        return this.isUserSignedIn;
    }

    @Override
    public void onDetermineAdFree(boolean isAddFree) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDetermineAdFree: " + isAddFree);
        }
    }
}

