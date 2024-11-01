package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.amazon.TokenHelper;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.thirdparty.tasker.TaskerIntent;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIApplicationsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentApplications;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FragmentApplicationsHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentApplicationsHelper.class.getSimpleName();
    private static final int CHECK = R.drawable.ic_check;

    private final FragmentApplications parentFragment;

    public FragmentApplicationsHelper(FragmentApplications fragment) {
        this.parentFragment = fragment;
    }

    private String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    private ArrayList<ContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getUIComponents");
        }
        ArrayList<ContainerUI> arrayList = new ArrayList<>();
        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_facebook));
        boolean packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_FACEBOOK);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_facebook);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_link_facebook));
        containerUI.setSubtitle(getString(R.string.menu_tap_authorise));
        containerUI.setIconMain(R.drawable.ic_login);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_twitter));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_TWITTER);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_twitter);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_link_twitter));
        containerUI.setSubtitle(getString(R.string.menu_tap_authorise));
        containerUI.setIconMain(R.drawable.ic_login);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_alexa));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_ALEXA);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_alexa);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_link_alexa));
        containerUI.setSubtitle(TokenHelper.hasToken(getApplicationContext()) ? getString(R.string.menu_tap_deauthorise) : getString(R.string.menu_tap_authorise));
        containerUI.setIconMain(R.drawable.ic_login);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_tasker));
        final boolean isTaskerInstalled = TaskerIntent.taskerInstalled(getApplicationContext());
        containerUI.setSubtitle(isTaskerInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(isTaskerInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_tasker);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_link_tasker));
        containerUI.setSubtitle(getString(R.string.menu_tap_authorise));
        containerUI.setIconMain(R.drawable.ic_login);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_linkedin));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_LINKED_IN);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_linkedin);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_link_linkedin));
        containerUI.setSubtitle(getString(R.string.menu_tap_authorise));
        containerUI.setIconMain(R.drawable.ic_login);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_foursquare));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_FOUR_SQUARED);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_foursquare);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_link_foursquare));
        containerUI.setSubtitle(getString(R.string.menu_tap_authorise));
        containerUI.setIconMain(R.drawable.ic_login);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_wolfram_alpha));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_WOLFRAM_ALPHA);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_wolfram_alpha);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_shazam));
        boolean c2 = ai.saiy.android.applications.Installed.shazamInstalled(getApplicationContext());
        containerUI.setSubtitle(c2 ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(c2 ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_shazam);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_soundhound));
        boolean d = ai.saiy.android.applications.Installed.soundHoundInstalled(getApplicationContext());
        containerUI.setSubtitle(d ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(d ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_image_unknown);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_trackid));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_TRACK_ID);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_image_unknown);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_google_sound_search));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_SOUND_SEARCH);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_google_play);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_ebay));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_EBAY);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_ebay);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_amazon));
        boolean k = ai.saiy.android.applications.Installed.amazonInstalled(getApplicationContext());
        containerUI.setSubtitle(k ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(k ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_amazon);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_spotify));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_SPOTIFY_MUSIC);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_spotify);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_deezer));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_DEEZER);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_image_unknown);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_play_music));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_MUSIC);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_image_unknown);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_tidal));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_ASPIRO_TINDER);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_tidal);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_amazon_music));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_AMAZON_MUSIC);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_amazon);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_netflix));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_NETFLIX);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_netflix);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_imdb));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_IMDB);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_imdb);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_youtube));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_YOUTUBE);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_youtube);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_google_maps));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_MAPS);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_google_maps);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_google_earth));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_EARTH);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_google_earth);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_google_sky_map));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_SKY);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_image_unknown);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_google_translate));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_TRANSLATE);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_google_translate);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_yelp));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_YELP);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_yelp);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_uber));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_UBER);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_uber);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_dropbox));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_DROPBOX);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_dropbox);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_evernote));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_EVERNOTE);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_evernote);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_box));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_BOX);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_box);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_drive));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_DOCS);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_google_drive);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_keep));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_KEEP);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_image_unknown);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_automate));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_AUTOMATE);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled ? FragmentApplicationsHelper.CHECK : FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_image_unknown);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_google_play_store));
        packageInstalled = ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_STORE);
        containerUI.setSubtitle(packageInstalled ? getString(R.string.title_installed) : getString(R.string.menu_tap_install));
        containerUI.setIconExtra(packageInstalled? FragmentApplicationsHelper.CHECK:FragmentHome.CHEVRON);
        containerUI.setIconMain(R.drawable.ic_google_play);
        arrayList.add(containerUI);
        return arrayList;
    }

    private Context getApplicationContext() {
        return this.parentFragment.getApplicationContext();
    }

    public UIApplicationsAdapter getAdapter(ArrayList<ContainerUI> arrayList) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getAdapter");
        }
        return new UIApplicationsAdapter(arrayList, getParent(), getParent());
    }

    /**
     * Get the recycler view for this fragment
     *
     * @param parent the view parent
     * @return the {@link RecyclerView}
     */
    public RecyclerView getRecyclerView(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getRecyclerView");
        }
        RecyclerView recyclerView = parent.findViewById(R.id.layout_common_fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));
        return recyclerView;
    }

    public void finaliseUI() {
        Schedulers.single().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentApplicationsHelper.this.getUIComponents();
                if (FragmentApplicationsHelper.this.getParent().isActive()) {
                    FragmentApplicationsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FragmentApplicationsHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentApplicationsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentApplicationsHelper.this.getParent().getObjects().size());
                        }
                    });
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                }
            }
        }, getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)? FragmentHome.DRAWER_CLOSE_DELAY : 0, TimeUnit.MILLISECONDS);
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "makeToast: " + text);
        }
        if (getParent().isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    public void checkTaskerInstallation() {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.thirdparty.tasker.TaskerHelper taskerHelper = new ai.saiy.android.thirdparty.tasker.TaskerHelper();
                Pair<Boolean, String> taskerPair = taskerHelper.isTaskerInstalled(FragmentApplicationsHelper.this.getApplicationContext());
                if (!taskerPair.first) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "tasker not installed");
                    }
                    Install.showInstallLink(FragmentApplicationsHelper.this.getApplicationContext(), Installed.PACKAGE_TASKER_MARKET);
                    return;
                }
                String packageName = taskerPair.second;
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "tasker installed: " + packageName);
                }
                Pair<Boolean, Boolean> taskerStatusPair = taskerHelper.canInteract(FragmentApplicationsHelper.this.getApplicationContext());
                if (!taskerStatusPair.first) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "tasker disabled");
                    }
                    ai.saiy.android.applications.UtilsApplication.launchAppFromPackageName(FragmentApplicationsHelper.this.getApplicationContext(), packageName);
                    FragmentApplicationsHelper.this.toast(FragmentApplicationsHelper.this.getString(R.string.error_tasker_enable), Toast.LENGTH_SHORT);
                    return;
                }
                if (!taskerStatusPair.second) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "tasker external access required");
                    }
                    taskerHelper.showTaskerExternalAccess(FragmentApplicationsHelper.this.getApplicationContext());
                    FragmentApplicationsHelper.this.getParentActivity().speak(R.string.error_tasker_external_access, LocalRequest.ACTION_SPEAK_ONLY);
                    return;
                }
                if (taskerHelper.receiverExists(FragmentApplicationsHelper.this.getApplicationContext())) {
                    FragmentApplicationsHelper.this.toast(FragmentApplicationsHelper.this.getString(R.string.content_tasker_connection_success), Toast.LENGTH_SHORT);
                    return;
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "tasker no receiver");
                    MyLog.i(CLS_NAME, "tasker no receiver: install from unknown: " + ai.saiy.android.thirdparty.tasker.TaskerHelper.isUnknownSourceInstallAllowed(FragmentApplicationsHelper.this.getApplicationContext()));
                }
                Locale vrLocale = SPH.getVRLocale(FragmentApplicationsHelper.this.getApplicationContext());
                SupportedLanguage supportedLanguage = SupportedLanguage.getSupportedLanguage(vrLocale);
                ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(FragmentApplicationsHelper.this.getApplicationContext(), supportedLanguage);
                String utterance;
                if (ai.saiy.android.thirdparty.tasker.TaskerHelper.isUnknownSourceInstallAllowed(FragmentApplicationsHelper.this.getApplicationContext())) {
                    SPH.setCheckReinstallationNeeded(FragmentApplicationsHelper.this.getApplicationContext(), true);
                    utterance = sr.getString(R.string.content_tasker_reinstall_1) + XMLResultsHandler.SEP_SPACE + sr.getString(R.string.content_tasker_reinstall_7);
                } else {
                    FragmentApplicationsHelper.this.getParent().setAttemptingReinstallation(true);
                    SPH.setCheckUnknownSourcesSettingNeeded(FragmentApplicationsHelper.this.getApplicationContext(), true);
                    utterance = sr.getString(R.string.content_tasker_reinstall_1) + XMLResultsHandler.SEP_SPACE + sr.getString(R.string.content_tasker_reinstall_2);
                }
                sr.reset();
                ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(FragmentApplicationsHelper.this.getApplicationContext());
                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, supportedLanguage, vrLocale, SPH.getTTSLocale(FragmentApplicationsHelper.this.getApplicationContext()), utterance);
                localRequest.setCondition(Condition.CONDITION_CHECK_REINSTALLATION);
                localRequest.execute();
            }
        });
    }

    public ActivityHome getParentActivity() {
        return this.parentFragment.getParentActivity();
    }

    public FragmentApplications getParent() {
        return this.parentFragment;
    }
}
