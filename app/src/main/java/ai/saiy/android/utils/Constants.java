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

package ai.saiy.android.utils;

import android.content.res.Resources;

/**
 * A collection of constants that are not suitable to store in a resource file.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public final class Constants {

    /**
     * Prevent instantiation
     */
    public Constants() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final String SAIY = "Saiy";
    public static final String SAIY_WEB_URL = "http://saiy.ai";
    public static final String SAIY_PRIVACY_URL = "https://sites.google.com/view/privacy-policy-saiy";
    public static final String SAIY_TOU_URL = "http://saiy.ai/terms.html";
    public static final String SAIY_BILLING_EMAIL = "billing@saiy.ai";
    public static final String SAIY_ENQUIRIES_EMAIL = "commercial@saiy.ai";
    public static final String SAIY_FEEDBACK_EMAIL = "feedback@saiy.ai";
    public static final String SAIY_GITHUB_URL = "https://github.com/whulwj/Saiy-PS/";
    public static final String SAIY_TWITTER_HANDLE = "http://twitter.com/brandall76";
    public static final String SAIY_GOOGLE_PLUS_URL = "https://plus.google.com/100131487913427971091";
    public static final String SAIY_XDA_URL = "http://forum.xda-developers.com/showthread.php?t=1508195";

    public static final String DEFAULT_FILE_PREFIX = "default_file";
    public static final String DEFAULT_AUDIO_FILE_SUFFIX = "wav";
    public static final String DEFAULT_AUDIO_FILE_PREFIX = "default_audio_file";
    public static final String DEFAULT_TEMP_FILE_SUFFIX = "txt";
    public static final String DEFAULT_TEMP_FILE_PREFIX = "default_temp_file";
    public static final String OGG_AUDIO_FILE_SUFFIX = "ogg";
    public static final String MP3_AUDIO_FILE_SUFFIX = "mp3";

    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_HEAD = "HEAD";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";

    public static final String STUDIO_AHREMARK_WEB_URL = "http://www.studioahremark.com/";
    public static final String SCHLAPA_WEB_URL = "https://schlapa.net";

    public static final String LICENSE_URL_GOOGLE_PLAY_SERVICES = "https://developers.google.com/android/guides/overview";
    public static final String LICENSE_URL_GSON = "https://github.com/google/gson";
    public static final String LICENSE_URL_VOLLEY = "https://github.com/google/volley";
    public static final String LICENSE_URL_KAAREL_KALJURAND = "https://github.com/Kaljurand/speechutils";
    public static final String LICENSE_URL_MICROSOFT_TRANSLATOR = "https://github.com/boatmeme/microsoft-translator-java-api";
    public static final String LICENSE_URL_APACHE_COMMONS = "https://commons.apache.org";
    public static final String LICENSE_URL_SIMMETRICS = "https://github.com/Simmetrics/simmetrics";
    public static final String LICENSE_URL_NUANCE_SPEECHKIT = "https://github.com/NuanceDev/speechkit-android";
    public static final String LICENSE_URL_GUAVA = "https://github.com/google/guava";
    public static final String LICENSE_URL_MICROSOFT_COGNITIVE = "https://www.microsoft.com/cognitive-services";
    public static final String LICENSE_URL_DIALOG_FLOW = "https://github.com/googleapis/google-cloud-java/tree/main/java-dialogflow";
    public static final String LICENSE_URL_SIMPLE_XML = "https://github.com/ngallagher/simplexml";
    public static final String LICENSE_MATERIAL_ICONS = "https://github.com/Templarian/MaterialDesign";
    public static final String LICENSE_POCKETSPHINX = "https://github.com/cmusphinx/pocketsphinx-android";
    public static final String LICENSE_SOUND_BIBLE = "http://soundbible.com";

    public static final String USER_GUIDE_BASIC = "http://forum.xda-developers.com/showpost.php?p=26804173&postcount=1043";
    public static final String USER_CUSTOM_COMMANDS = "http://forum.xda-developers.com/showpost.php?p=26883467&postcount=1050";
    public static final String USER_CUSTOM_REPLACEMENTS = "http://forum.xda-developers.com/showpost.php?p=33882082&postcount=2047";
    public static final String USER_SOUND_EFFECTS = "http://forum.xda-developers.com/showpost.php?p=33877549&postcount=2042";
    public static final String USER_TASKER = "http://forum.xda-developers.com/showpost.php?p=34339449&postcount=2155";
    public static final String USER_TROUBLESHOOTING = "http://forum.xda-developers.com/showpost.php?p=25228934&postcount=659";
    public static final String USER_COMING_SOON = "http://forum.xda-developers.com/showpost.php?p=25666528&postcount=755";

    public static final String PERMISSION_CONTROL_SAIY = "ai.saiy.android.permission.CONTROL_SAIY";

    public static final int FUSED_LOCATION_PROVIDER = 1;
    public static final int DEFAULT_LOCATION_PROVIDER = 2;

    public static final String SEP_COMMA = ",";
    public static final String SEP_HYPHEN = "-";
    public static final String SEP_SPACE = " ";
}
