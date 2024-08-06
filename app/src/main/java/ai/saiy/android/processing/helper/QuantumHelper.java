/*
 * Copyright (c) 2017. Saiy® Ltd. All Rights Reserved.
 *
 * Unauthorised copying of this file, via any medium is strictly prohibited. Proprietary and confidential
 */

package ai.saiy.android.processing.helper;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.custom.CustomCommandHelper;
import ai.saiy.android.custom.CustomHelper;
import ai.saiy.android.custom.CustomHelperHolder;
import ai.saiy.android.custom.CustomNicknameHelper;
import ai.saiy.android.custom.CustomPhraseHelper;
import ai.saiy.android.custom.CustomReplacementHelper;
import ai.saiy.android.custom.CustomResolver;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.Profanity;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class QuantumHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = QuantumHelper.class.getSimpleName();

    public CustomResolver resolve(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                  @NonNull final CommandRequest cr) {

        final long then = System.nanoTime();

        final CustomHelperHolder holder = new CustomHelper().getCustomisationHolder(ctx);

        final CustomResolver resolver = new CustomResolver();
        final CustomCommandHelper cch = new CustomCommandHelper();

        final ArrayList<String> manipulatedVoiceData = new CustomNicknameHelper().replace(new CustomReplacementHelper().replace(new Profanity(ctx, cr.getResultsArray(), sl).remove(), holder.getCustomReplacementArray()), holder.getCustomNicknameArray());
        final Pair<Boolean, ai.saiy.android.custom.CustomCommand> haveCustomPhrasePair = new CustomPhraseHelper().haveCustomPhrase(ctx, cr, manipulatedVoiceData, holder.getCustomPhraseArray());
        resolver.setCustom(haveCustomPhrasePair.first);
        if (resolver.isCustom()) {
            cch.setCustomCommand(haveCustomPhrasePair.second);
        } else {
            resolver.setCustom(cch.isCustomCommand(ctx, manipulatedVoiceData, sl, holder.getCustomCommandArray()));
        }
        resolver.setCustomCommandHelper(cch);
        resolver.setVoiceData(manipulatedVoiceData);

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return resolver;


    }
}
