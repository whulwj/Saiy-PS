package ai.saiy.android.ui.service.helper;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import ai.saiy.android.R;
import ai.saiy.android.ui.components.UIFloatingCommandsAdapter;
import ai.saiy.android.ui.containers.SimpleContainerUI;
import ai.saiy.android.ui.service.FloatingCommandsService;
import ai.saiy.android.utils.MyLog;

public class FloatingCommandsHelper {

    private final boolean DEBUG = MyLog.DEBUG;

    private final String CLS_NAME = FloatingCommandsHelper.class.getSimpleName();
    private static final String LINE_SEPARATOR = "\n";

    private final FloatingCommandsService service;

    public FloatingCommandsHelper(FloatingCommandsService floatingCommandsService) {
        this.service = floatingCommandsService;
    }

    private String getString(@StringRes int resId) {
        return getService().getString(resId);
    }

    private ArrayList<SimpleContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUIComponents");
        }
        ArrayList<SimpleContainerUI> arrayList = new ArrayList<>();
        SimpleContainerUI simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_key));
        simpleContainerUI.setContent(getString(R.string.command_key_content_1) + LINE_SEPARATOR + getString(R.string.command_key_content_2) + LINE_SEPARATOR + getString(R.string.command_key_content_3) + LINE_SEPARATOR + getString(R.string.command_key_content_4) + LINE_SEPARATOR + getString(R.string.command_key_content_5) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_key_content_6) + LINE_SEPARATOR + getString(R.string.command_key_content_7) + LINE_SEPARATOR + getString(R.string.command_key_content_8));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_commands));
        simpleContainerUI.setContent(getString(R.string.command_commands_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_cancel));
        simpleContainerUI.setContent(getString(R.string.command_cancel_content_1) + LINE_SEPARATOR + getString(R.string.command_cancel_content_2) + LINE_SEPARATOR + getString(R.string.command_cancel_content_3) + LINE_SEPARATOR + getString(R.string.command_cancel_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_name));
        simpleContainerUI.setContent(getString(R.string.command_name_content_1) + LINE_SEPARATOR + getString(R.string.command_name_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_hotword));
        simpleContainerUI.setContent(getString(R.string.command_hotword_content_1) + LINE_SEPARATOR + getString(R.string.command_hotword_content_2) + LINE_SEPARATOR + getString(R.string.command_hotword_content_3) + LINE_SEPARATOR + getString(R.string.command_hotword_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_time));
        simpleContainerUI.setContent(getString(R.string.command_time_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_date));
        simpleContainerUI.setContent(getString(R.string.command_date_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_card));
        simpleContainerUI.setContent(getString(R.string.command_card_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_coin));
        simpleContainerUI.setContent(getString(R.string.command_coin_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_dice));
        simpleContainerUI.setContent(getString(R.string.command_dice_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_weather));
        simpleContainerUI.setContent(getString(R.string.command_weather_content_1) + LINE_SEPARATOR + getString(R.string.command_weather_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_sms));
        simpleContainerUI.setContent(getString(R.string.command_sms_content_1) + LINE_SEPARATOR + getString(R.string.command_sms_content_2) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_sms_content_3) + LINE_SEPARATOR + getString(R.string.command_sms_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_email));
        simpleContainerUI.setContent(getString(R.string.command_email_content_1) + LINE_SEPARATOR + getString(R.string.command_email_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_emotion));
        simpleContainerUI.setContent(getString(R.string.command_emotion_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_facebook));
        simpleContainerUI.setContent(getString(R.string.command_facebook_content_1) + LINE_SEPARATOR + getString(R.string.command_facebook_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_twitter));
        simpleContainerUI.setContent(getString(R.string.command_twitter_content_1) + LINE_SEPARATOR + getString(R.string.command_twitter_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_foursquare));
        simpleContainerUI.setContent(getString(R.string.command_foursquare_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_battery));
        simpleContainerUI.setContent(getString(R.string.command_battery_content_1) + LINE_SEPARATOR + getString(R.string.command_battery_content_2) + LINE_SEPARATOR + getString(R.string.command_battery_content_3) + LINE_SEPARATOR + getString(R.string.command_battery_content_4) + LINE_SEPARATOR + getString(R.string.command_battery_content_5) + LINE_SEPARATOR + getString(R.string.command_battery_content_6));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_contact));
        simpleContainerUI.setContent(getString(R.string.command_contact_content_1) + LINE_SEPARATOR + getString(R.string.command_contact_content_2) + LINE_SEPARATOR + getString(R.string.command_contact_content_3));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_call));
        simpleContainerUI.setContent(getString(R.string.command_call_content_1) + LINE_SEPARATOR + getString(R.string.command_call_content_2) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_call_content_3) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_call_content_4) + LINE_SEPARATOR + getString(R.string.command_call_content_5) + LINE_SEPARATOR + getString(R.string.command_call_content_6));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_music));
        simpleContainerUI.setContent(getString(R.string.command_music_content_1) + LINE_SEPARATOR + getString(R.string.command_music_content_2) + LINE_SEPARATOR + getString(R.string.command_music_content_3) + LINE_SEPARATOR + getString(R.string.command_music_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_app));
        simpleContainerUI.setContent(getString(R.string.command_app_content_1) + LINE_SEPARATOR + getString(R.string.command_app_content_2) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_app_content_4) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_app_content_3) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_app_content_5) + LINE_SEPARATOR + getString(R.string.command_app_content_6) + LINE_SEPARATOR + getString(R.string.command_app_content_7) + LINE_SEPARATOR + getString(R.string.command_app_content_8) + LINE_SEPARATOR + getString(R.string.command_app_content_9) + LINE_SEPARATOR + getString(R.string.command_app_content_10) + LINE_SEPARATOR + getString(R.string.command_app_content_11) + LINE_SEPARATOR + getString(R.string.command_app_content_12) + LINE_SEPARATOR + getString(R.string.command_app_content_13) + LINE_SEPARATOR + getString(R.string.command_app_content_14));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_driving));
        simpleContainerUI.setContent(getString(R.string.command_driving_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_notifications));
        simpleContainerUI.setContent(getString(R.string.command_notifications_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_wifi));
        simpleContainerUI.setContent(getString(R.string.command_wifi_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_bluetooth));
        simpleContainerUI.setContent(getString(R.string.command_bluetooth_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_torch));
        simpleContainerUI.setContent(getString(R.string.command_torch_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_mobile_data));
        simpleContainerUI.setContent(getString(R.string.command_mobile_data_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_airplane_mode));
        simpleContainerUI.setContent(getString(R.string.command_airplane_mode_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_gps));
        simpleContainerUI.setContent(getString(R.string.command_gps_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_nfc));
        simpleContainerUI.setContent(getString(R.string.command_nfc_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_hotspot));
        simpleContainerUI.setContent(getString(R.string.command_hotspot_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_horoscope));
        simpleContainerUI.setContent(getString(R.string.command_horoscope_content_1) + LINE_SEPARATOR + getString(R.string.command_horoscope_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_orientation));
        simpleContainerUI.setContent(getString(R.string.command_orientation_content_1) + LINE_SEPARATOR + getString(R.string.command_orientation_content_2) + LINE_SEPARATOR + getString(R.string.command_orientation_content_3) + LINE_SEPARATOR + getString(R.string.command_orientation_content_4) + LINE_SEPARATOR + getString(R.string.command_orientation_content_5) + LINE_SEPARATOR + getString(R.string.command_orientation_content_6) + LINE_SEPARATOR + getString(R.string.command_orientation_content_7));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_home));
        simpleContainerUI.setContent(getString(R.string.command_home_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_navigation));
        simpleContainerUI.setContent(getString(R.string.command_navigation_content_1) + LINE_SEPARATOR + getString(R.string.command_navigation_content_2) + LINE_SEPARATOR + getString(R.string.command_navigation_content_3) + LINE_SEPARATOR + getString(R.string.command_navigation_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_calendar));
        simpleContainerUI.setContent(getString(R.string.command_calendar_content_1) + LINE_SEPARATOR + getString(R.string.command_calendar_content_2) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_calendar_content_3) + LINE_SEPARATOR + getString(R.string.command_calendar_content_4) + LINE_SEPARATOR + getString(R.string.command_calendar_content_5));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_alarm));
        simpleContainerUI.setContent(getString(R.string.command_alarm_content_1) + LINE_SEPARATOR + getString(R.string.command_alarm_content_2) + LINE_SEPARATOR + getString(R.string.command_alarm_content_3) + LINE_SEPARATOR + getString(R.string.command_alarm_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_timer));
        simpleContainerUI.setContent(getString(R.string.command_timer_content_1) + LINE_SEPARATOR + getString(R.string.command_timer_content_2) + LINE_SEPARATOR + getString(R.string.command_timer_content_3) + LINE_SEPARATOR + getString(R.string.command_timer_content_4) + LINE_SEPARATOR + getString(R.string.command_timer_content_5));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_web_search));
        simpleContainerUI.setContent(getString(R.string.command_web_search_content_1) + LINE_SEPARATOR + getString(R.string.command_web_search_content_2) + LINE_SEPARATOR + getString(R.string.command_web_search_content_3) + LINE_SEPARATOR + getString(R.string.command_web_search_content_4) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_web_search_content_5));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_video_search));
        simpleContainerUI.setContent(getString(R.string.command_video_search_content_1) + LINE_SEPARATOR + getString(R.string.command_video_search_content_2) + LINE_SEPARATOR + getString(R.string.command_video_search_content_3));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_movie_search));
        simpleContainerUI.setContent(getString(R.string.command_movie_search_content_1) + LINE_SEPARATOR + getString(R.string.command_movie_search_content_2) + LINE_SEPARATOR + getString(R.string.command_movie_search_content_3) + LINE_SEPARATOR + getString(R.string.command_movie_search_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_social_search));
        simpleContainerUI.setContent(getString(R.string.command_social_search_content_1) + LINE_SEPARATOR + getString(R.string.command_social_search_content_2) + LINE_SEPARATOR + getString(R.string.command_social_search_content_3));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_product_search));
        simpleContainerUI.setContent(getString(R.string.command_product_search_content_1) + LINE_SEPARATOR + getString(R.string.command_product_search_content_2) + LINE_SEPARATOR + getString(R.string.command_product_search_content_3));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_local_search));
        simpleContainerUI.setContent(getString(R.string.command_local_search_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_financial));
        simpleContainerUI.setContent(getString(R.string.command_financial_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_calculate));
        simpleContainerUI.setContent(getString(R.string.command_calculate_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_knowledge));
        simpleContainerUI.setContent(getString(R.string.command_knowledge_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_notes));
        simpleContainerUI.setContent(getString(R.string.command_notes_content_1) + LINE_SEPARATOR + getString(R.string.command_notes_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_volume));
        simpleContainerUI.setContent(getString(R.string.command_volume_content_1) + LINE_SEPARATOR + getString(R.string.command_volume_content_2) + LINE_SEPARATOR + getString(R.string.command_volume_content_3) + LINE_SEPARATOR + getString(R.string.command_volume_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_define));
        simpleContainerUI.setContent(getString(R.string.command_define_content_1) + LINE_SEPARATOR + getString(R.string.command_define_content_2));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_clipboard));
        simpleContainerUI.setContent(getString(R.string.command_clipboard_content_1) + LINE_SEPARATOR + getString(R.string.command_clipboard_content_2) + LINE_SEPARATOR + getString(R.string.command_clipboard_content_3) + LINE_SEPARATOR + getString(R.string.command_clipboard_content_4));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_location));
        simpleContainerUI.setContent(getString(R.string.command_location_content_1) + LINE_SEPARATOR + getString(R.string.command_location_content_2) + LINE_SEPARATOR + getString(R.string.command_location_content_3));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_earth));
        simpleContainerUI.setContent(getString(R.string.command_earth_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_sky));
        simpleContainerUI.setContent(getString(R.string.command_sky_content_1) + LINE_SEPARATOR + getString(R.string.command_sky_content_2) + LINE_SEPARATOR + getString(R.string.command_sky_content_3));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_tasker));
        simpleContainerUI.setContent(getString(R.string.command_tasker_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_repeat));
        simpleContainerUI.setContent(getString(R.string.command_repeat_content_1));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_device_settings));
        simpleContainerUI.setContent(getString(R.string.command_device_settings_content_1) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_2) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_3) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_4) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_5) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_6) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_7) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_8) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_9) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_10) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_11) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_12) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_13) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_14) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_15) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_16) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_17) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_18) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_19) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_20) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_21) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_22) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_23) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_24) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_25) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_26) + LINE_SEPARATOR + getString(R.string.command_device_settings_content_27) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_device_settings_content_28));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_translate));
        simpleContainerUI.setContent(getString(R.string.command_translate_content_1) + LINE_SEPARATOR + getString(R.string.command_translate_content_2) + LINE_SEPARATOR + LINE_SEPARATOR + getString(R.string.command_translate_content_3) + LINE_SEPARATOR + getString(R.string.command_translate_content_4) + LINE_SEPARATOR + getString(R.string.command_translate_content_5) + LINE_SEPARATOR + getString(R.string.command_translate_content_6) + LINE_SEPARATOR + getString(R.string.command_translate_content_7) + LINE_SEPARATOR + getString(R.string.command_translate_content_8) + LINE_SEPARATOR + getString(R.string.command_translate_content_9) + LINE_SEPARATOR + getString(R.string.command_translate_content_10) + LINE_SEPARATOR + getString(R.string.command_translate_content_11) + LINE_SEPARATOR + getString(R.string.command_translate_content_12) + LINE_SEPARATOR + getString(R.string.command_translate_content_13) + LINE_SEPARATOR + getString(R.string.command_translate_content_14) + LINE_SEPARATOR + getString(R.string.command_translate_content_15) + LINE_SEPARATOR + getString(R.string.command_translate_content_16) + LINE_SEPARATOR + getString(R.string.command_translate_content_17) + LINE_SEPARATOR + getString(R.string.command_translate_content_18) + LINE_SEPARATOR + getString(R.string.command_translate_content_20) + LINE_SEPARATOR + getString(R.string.command_translate_content_21) + LINE_SEPARATOR + getString(R.string.command_translate_content_22) + LINE_SEPARATOR + getString(R.string.command_translate_content_23) + LINE_SEPARATOR + getString(R.string.command_translate_content_24) + LINE_SEPARATOR + getString(R.string.command_translate_content_25) + LINE_SEPARATOR + getString(R.string.command_translate_content_26) + LINE_SEPARATOR + getString(R.string.command_translate_content_27) + LINE_SEPARATOR + getString(R.string.command_translate_content_28) + LINE_SEPARATOR + getString(R.string.command_translate_content_29) + LINE_SEPARATOR + getString(R.string.command_translate_content_30) + LINE_SEPARATOR + getString(R.string.command_translate_content_31) + LINE_SEPARATOR + getString(R.string.command_translate_content_32) + LINE_SEPARATOR + getString(R.string.command_translate_content_33) + LINE_SEPARATOR + getString(R.string.command_translate_content_34) + LINE_SEPARATOR + getString(R.string.command_translate_content_35) + LINE_SEPARATOR + getString(R.string.command_translate_content_36) + LINE_SEPARATOR + getString(R.string.command_translate_content_37) + LINE_SEPARATOR + getString(R.string.command_translate_content_38) + LINE_SEPARATOR + getString(R.string.command_translate_content_39) + LINE_SEPARATOR + getString(R.string.command_translate_content_40) + LINE_SEPARATOR + getString(R.string.command_translate_content_41) + LINE_SEPARATOR + getString(R.string.command_translate_content_42) + LINE_SEPARATOR + getString(R.string.command_translate_content_43) + LINE_SEPARATOR + getString(R.string.command_translate_content_44) + LINE_SEPARATOR + getString(R.string.command_translate_content_45) + LINE_SEPARATOR + getString(R.string.command_translate_content_46) + LINE_SEPARATOR + getString(R.string.command_translate_content_47) + LINE_SEPARATOR + getString(R.string.command_translate_content_48) + LINE_SEPARATOR + getString(R.string.command_translate_content_49) + LINE_SEPARATOR + getString(R.string.command_translate_content_50) + LINE_SEPARATOR + getString(R.string.command_translate_content_51) + LINE_SEPARATOR + getString(R.string.command_translate_content_52) + LINE_SEPARATOR + getString(R.string.command_translate_content_53) + LINE_SEPARATOR + getString(R.string.command_translate_content_54) + LINE_SEPARATOR + getString(R.string.command_translate_content_55) + LINE_SEPARATOR + getString(R.string.command_translate_content_56) + LINE_SEPARATOR + getString(R.string.command_translate_content_57) + LINE_SEPARATOR + getString(R.string.command_translate_content_58) + LINE_SEPARATOR + getString(R.string.command_translate_content_59) + LINE_SEPARATOR + getString(R.string.command_translate_content_19));
        arrayList.add(simpleContainerUI);

        simpleContainerUI = new SimpleContainerUI();
        simpleContainerUI.setTitle(getString(R.string.command_maintenance));
        simpleContainerUI.setContent(getString(R.string.command_maintenance_content_1) + LINE_SEPARATOR + getString(R.string.command_maintenance_content_2));
        arrayList.add(simpleContainerUI);
        return arrayList;
    }

    private FloatingCommandsService getService() {
        return this.service;
    }

    public UIFloatingCommandsAdapter getAdapter(ArrayList<SimpleContainerUI> arrayList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIFloatingCommandsAdapter(arrayList, getService());
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
        final RecyclerView recyclerView = parent.findViewById(R.id.layout_floating_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getService()));
        recyclerView.addItemDecoration(new ai.saiy.android.ui.components.DividerItemDecoration(getService(), null));
        return recyclerView;
    }

    public void finaliseUI() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<SimpleContainerUI> tempArray = FloatingCommandsHelper.this.getUIComponents();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        FloatingCommandsHelper.this.getService().getObjects().addAll(tempArray);
                        FloatingCommandsHelper.this.getService().getAdapter().notifyItemRangeInserted(0, FloatingCommandsHelper.this.getService().getObjects().size());
                    }
                });
            }
        });
    }
}
