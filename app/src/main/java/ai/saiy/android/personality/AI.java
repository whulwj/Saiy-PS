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

package ai.saiy.android.personality;

import android.content.Context;
import android.util.Pair;

import com.facebook.share.model.ShareLinkContent;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ai.saiy.android.R;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.user.SaiyAccount;
import ai.saiy.android.user.SaiyAccountHelper;
import ai.saiy.android.user.SaiyAccountList;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * The more features of the application that are interacted with, the more the 'AI Value' increases,
 * which is shown to the user in the permanent notification.
 * <p>
 * The AI Level is based on very little that is Artificially Intelligent....
 * <p>
 * Created by benrandall76@gmail.com on 22/03/2016.
 */
public class AI {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AI.class.getSimpleName();

    // Placeholder value
    public static double AI_LEVEL = 0.24;

    /**
     * Get the current AI Level to display
     *
     * @param context the application context
     * @return the AI Level
     */
    public static String getAILevel(Context context) {
        return new BigDecimal(Double.toString(calculateAI(context))).setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * Check how much of the application's functionality the user has used and is using. More = higher.
     *
     * @param context the application context
     * @return the AI Level
     */
    private static double calculateAI(Context context) {
        final long then = System.nanoTime();
        final double aiLevel = AI_LEVEL + scoreOfUserName(context) + scoreOfPhrase(context) + scoreOfNickname(context) + scoreOfReplacement(context) + scoreOfCustomisation(context) + scoreOfTaskerVariables(context) + scoreOfCustomIntro(context) + scoreOfRunDiagnostics(context) + scoreOfDefaultTTSVoice(context) + scoreOfCommandUnknownAction(context) + scoreOfAccount(context) + scoreOfFacebook() + scoreOfTwitter(context) + scoreOfTasker(context) + scoreOfUsage(context);
        if (DEBUG) {
            ai.saiy.android.utils.MyLog.i(CLS_NAME, "aiLevel: " + new BigDecimal(Double.toString(aiLevel)).setScale(2, RoundingMode.HALF_UP).toString());
            ai.saiy.android.utils.MyLog.getElapsed(CLS_NAME, "calculateAI", then);
        }
        return aiLevel;
    }

    private static double scoreOfUserName(Context context) {
        if (ai.saiy.android.utils.SPH.getUserName(context) == null || ai.saiy.android.utils.SPH.getUserName(context).matches(context.getString(R.string.master))) {
            return 0;
        }
        return 0.23d;
    }

    private static double scoreOfPhrase(Context context) {
        if (ai.saiy.android.utils.SPH.hasPhrase(context)) {
            return 0.23d;
        }
        return 0;
    }

    private static double scoreOfNickname(Context context) {
        if (ai.saiy.android.utils.SPH.hasNickname(context)) {
            return 0.23d;
        }
        return 0;
    }

    private static double scoreOfReplacement(Context context) {
        if (ai.saiy.android.utils.SPH.hasReplacement(context)) {
            return 0.23d;
        }
        return 0;
    }

    private static double scoreOfCustomisation(Context context) {
        if (ai.saiy.android.utils.SPH.hasCustomisation(context)) {
            return 0.36d;
        }
        return 0;
    }

    private static double scoreOfTaskerVariables(Context context) {
        if (ai.saiy.android.utils.SPH.hasTaskerVariables(context)) {
            return 0.3d;
        }
        return 0;
    }

    private static double scoreOfCustomIntro(Context context) {
        if (ai.saiy.android.utils.SPH.getCustomIntro(context) != null) {
            return 0.23d;
        }
        return 0;
    }

    private static double scoreOfRunDiagnostics(Context context) {
        if (ai.saiy.android.utils.SPH.getRunDiagnostics(context)) {
            return 0.23d;
        }
        return 0;
    }

    private static double scoreOfDefaultTTSVoice(Context context) {
        if (ai.saiy.android.utils.SPH.getDefaultTTSVoice(context) != null) {
            return 0.23d;
        }
        return 0;
    }

    private static double scoreOfCommandUnknownAction(Context context) {
        if (ai.saiy.android.utils.SPH.getCommandUnknownAction(context) != Unknown.UNKNOWN_STATE) {
            return 0.23d;
        }
        return 0;
    }

    private static double scoreOfAccount(Context context) {
        final SaiyAccountList saiyAccountList = SaiyAccountHelper.getAccounts(context);
        if (saiyAccountList == null || saiyAccountList.size() <= 0) {
            return 0;
        }
        final SaiyAccount saiyAccount = saiyAccountList.getSaiyAccountList().get(0);
        if (saiyAccount == null) {
            return 0;
        }
        final ai.saiy.android.cognitive.identity.provider.microsoft.containers.ProfileItem profileItem = saiyAccount.getProfileItem();
        if (profileItem == null || !UtilsString.notNaked(profileItem.getId())) {
            return 0;
        }
        return 0.39d;
    }

    private static double scoreOfFacebook() {
        if (new com.facebook.share.ShareApi(new ShareLinkContent.Builder().build()).canShare()) {
            return 0.3d;
        }
        return 0;
    }

    private static double scoreOfTwitter(Context context) {
        if (UtilsString.notNaked(ai.saiy.android.utils.SPH.getTwitterSecret(context)) && UtilsString.notNaked(ai.saiy.android.utils.SPH.getTwitterToken(context))) {
            return 0.3d;
        }
        return 0;
    }

    private static double scoreOfTasker(Context context) {
        final Pair<Boolean, Boolean> taskerStatusPair = new ai.saiy.android.thirdparty.tasker.TaskerHelper().canInteract(context);
        if (taskerStatusPair.first && taskerStatusPair.second) {
            return 0.3d;
        }
        return 0;
    }

    private static double scoreOfUsage(Context context) {
        if (ai.saiy.android.utils.SPH.getUsedIncrement(context) > 250) {
            return 0.99d;
        }
        return 0;
    }
}
