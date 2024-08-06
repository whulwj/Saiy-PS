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

package ai.saiy.android.command.helper;

import androidx.annotation.NonNull;

/**
 * Class (Command Constants) that lists all possible commands as enum constants. In the scheme of
 * things, using enums over integers here is of little concern - less concern than type safety and
 * the potential overhead of an equivalent implementation.
 * <p/>
 * The ordinal value of these enums should not be used as they are not guaranteed for future releases.
 * For logging purposes, use {@link Enum#name()}
 * <p/>
 * Created by benrandall76@gmail.com on 09/02/2016.
 */
public enum CC {

    /*
     * Inbuilt
     */
    COMMAND_UNKNOWN(false, false),
    COMMAND_CANCEL(false, false),
    COMMAND_SPELL(false, false),
    COMMAND_TRANSLATE(true, false),
    COMMAND_PARDON(false, false),
    COMMAND_USER_NAME(false, false),
    COMMAND_BATTERY(false, false),
    COMMAND_SONG_RECOGNITION(false, false),
    COMMAND_WOLFRAM_ALPHA(true, false),
    COMMAND_TASKER(false, true),
    COMMAND_EMOTION(true, true),
    COMMAND_HOTWORD(false, false),
    COMMAND_VOICE_IDENTIFY(true, false),
    COMMAND_CONTACT(false, true),
    COMMAND_NAVIGATION(false, false),
    COMMAND_DATE(false, false),
    COMMAND_TIME(false, false),
    COMMAND_HARDWARE(false, false),
    COMMAND_TOAST(false, false),
    COMMAND_CLIPBOARD(false, false),
    COMMAND_SETTINGS(false, false),
    COMMAND_MUSIC(false, false),
    COMMAND_HOME(false, false),
    COMMAND_SOMERSAULT(false, false),
    COMMAND_ORIENTATION(false, false),
    COMMAND_REDIAL(false, true),
    COMMAND_CALL_BACK(false, true),
    COMMAND_SHUTDOWN(false, true),
    COMMAND_RESTART(false, true),
    COMMAND_REMEMBER(false, false),
    COMMAND_UNINSTALL(false, false),
    COMMAND_DEFINE(true, false),
    COMMAND_APPLICATION_SETTINGS(false, false),
    COMMAND_CALCULATE(false, false),
    COMMAND_KILL(false, true),
    COMMAND_LAUNCH(false, false),
    COMMAND_LOCATION(true, false),
    COMMAND_PARKED_VEHICLE(false, false),
    COMMAND_LOCATE_VEHICLE(false, true),
    COMMAND_FOREGROUND(false, false),
    COMMAND_AUDIO(false, false),
    COMMAND_HOROSCOPE(true, false),
    COMMAND_AGENDA(false, true),
    COMMAND_WEATHER(true, false),
    COMMAND_STOCK_QUOTE(true, false),
    COMMAND_NOTE(false, false),
    COMMAND_SEARCH(false, false),
    COMMAND_ALARM(false, false),
    COMMAND_CALENDAR(false, false),
    COMMAND_TIMER(false, false),
    COMMAND_SMS(false, true),
    COMMAND_SUPERUSER(false, true),
    COMMAND_WEB(true, true),
    COMMAND_EASTER_EGG(false, false),
    COMMAND_JOKE(false, false),
    COMMAND_HELP(false, false),
    COMMAND_SHOW(false, false),
    COMMAND_CHAT_BOT(true, false),
    COMMAND_NOTIFICATIONS(false, true),
    COMMAND_DRIVING(false, true),
    COMMAND_FACEBOOK(true, true),
    COMMAND_TWITTER(true, true),
    COMMAND_FOURSQUARE(true, true),
    COMMAND_TAXI(false, true),
    COMMAND_FLOAT_COMMANDS(false, false),
    COMMAND_CARD(false, false),
    COMMAND_DICE(false, false),
    COMMAND_COIN(false, false),
    COMMAND_DONATE(true, false),
    COMMAND_ALEXA(true, true),

    /*
     * Custom
     */
    COMMAND_USER_CUSTOM(false, false),

    /*
     * Errors
     */
    COMMAND_EMPTY_ARRAY(false, false),
    COMMAND_SOMETHING_WEIRD(false, false);

    private final boolean requiresNetwork;
    private final boolean isSecure;

    /**
     * Hardcoded parameter that denotes if the command can be processed without a network connection.
     *
     * @param requiresNetwork true if a network connection is required, false otherwise
     * @param isSecure        true if the command must be handled securely, false otherwise
     */
    CC(final boolean requiresNetwork, final boolean isSecure) {
        this.requiresNetwork = requiresNetwork;
        this.isSecure = isSecure;
    }

    public boolean requiresNetwork() {
        return requiresNetwork;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public static boolean isSecure(@NonNull final CC cc) {
        return cc.isSecure();
    }

    public static boolean requiresNetwork(@NonNull final CC cc) {
        return cc.requiresNetwork();
    }
}
