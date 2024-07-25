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

package ai.saiy.android.custom;

import android.content.Context;

import ai.saiy.android.R;

/**
 * Class the holds the Custom Command Constants (=CCC)
 * <p/>
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public enum CCC {
    CUSTOM_SPEECH,
    CUSTOM_DISPLAY_CONTACT,
    CUSTOM_TASKER_TASK,
    CUSTOM_ACTIVITY,
    CUSTOM_CALL_CONTACT,
    CUSTOM_LAUNCH_APPLICATION,
    CUSTOM_LAUNCH_SHORTCUT,
    CUSTOM_SEARCHABLE,
    CUSTOM_INTENT_SERVICE,
    CUSTOM_IOT_CONNECTION,
    CUSTOM_SEND_INTENT,
    CUSTOM_HTTP,
    CUSTOM_CAST,
    CUSTOM_AUTOMATE_FLOW;

    public static String getReadableName(Context context, CCC ccc, String str) {
        switch (ccc) {
            case CUSTOM_SPEECH:
                return context.getString(R.string.title_custom_speech);
            case CUSTOM_DISPLAY_CONTACT:
                return context.getString(R.string.title_display_contact);
            case CUSTOM_TASKER_TASK:
                return context.getString(R.string.title_tasker_task);
            case CUSTOM_ACTIVITY:
                return context.getString(R.string.title_launch_activity);
            case CUSTOM_CALL_CONTACT:
                return context.getString(R.string.title_call_contact);
            case CUSTOM_LAUNCH_APPLICATION:
                return context.getString(R.string.title_launch_application);
            case CUSTOM_LAUNCH_SHORTCUT:
                return context.getString(R.string.title_application_shortcut);
            case CUSTOM_SEARCHABLE:
                return context.getString(R.string.title_searchable_application);
            case CUSTOM_INTENT_SERVICE:
                return context.getString(R.string.title_external_command_, str);
            case CUSTOM_IOT_CONNECTION:
                return context.getString(R.string.title_iot_connection);
            case CUSTOM_SEND_INTENT:
                return context.getString(R.string.title_send_intent);
            case CUSTOM_HTTP:
                return context.getString(R.string.title_http_request);
            case CUSTOM_CAST:
                return context.getString(R.string.title_cast);
            case CUSTOM_AUTOMATE_FLOW:
                return context.getString(R.string.title_automate_flow);
            default:
                return context.getString(R.string.menu_custom_command);
        }
    }
}
