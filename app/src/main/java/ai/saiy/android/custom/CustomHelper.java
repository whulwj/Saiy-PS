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
import android.content.Intent;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.database.callable.DBCustomCommandCallable;
import ai.saiy.android.database.callable.DBCustomNicknameCallable;
import ai.saiy.android.database.callable.DBCustomPhraseCallable;
import ai.saiy.android.database.callable.DBCustomReplacementCallable;
import ai.saiy.android.ui.containers.ContainerCustomisation;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class CustomHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CustomHelper.class.getSimpleName();

    public static final int CHEVRON_RESOURCE_ID = R.drawable.chevron;
    public static final int CUSTOM_NICKNAME_RESOURCE_ID = R.drawable.ic_account_switch;
    public static final int CUSTOM_PHRASE_RESOURCE_ID = R.drawable.ic_format_quote;
    public static final int CUSTOM_COMMAND_RESOURCE_ID = R.drawable.ic_shape_plus;
    public static final int CUSTOM_REPLACEMENT_RESOURCE_ID = R.drawable.ic_swap_horizontal;

    private static final Object lock = new Object();

    public CustomHelperHolder getCustomisationHolder(@NonNull final Context ctx) {
        synchronized (lock) {
            final long then = System.nanoTime();
            ArrayList<CustomNickname> customNicknameArray = null;
            ArrayList<CustomPhrase> customPhraseArray = null;
            ArrayList<CustomCommandContainer> customCommandContainerArray = null;
            ArrayList<CustomReplacement> customReplacementArray = null;

            final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            try {
                final Future<ArrayList<CustomNickname>> customNicknameFuture = executorService.submit(new DBCustomNicknameCallable(ctx));
                final Future<ArrayList<CustomPhrase>> customPhraseFuture = executorService.submit(new DBCustomPhraseCallable(ctx));
                final Future<ArrayList<CustomCommandContainer>> customCommandContainerFuture = executorService.submit(new DBCustomCommandCallable(ctx));
                final Future<ArrayList<CustomReplacement>> customReplacementFuture = executorService.submit(new DBCustomReplacementCallable(ctx));
                customNicknameArray = customNicknameFuture.get();
                customPhraseArray = customPhraseFuture.get();
                customCommandContainerArray = customCommandContainerFuture.get();
                customReplacementArray = customReplacementFuture.get();
            } catch (final ExecutionException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: ExecutionException");
                    e.printStackTrace();
                }
            } catch (final CancellationException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: CancellationException");
                    e.printStackTrace();
                }
            } catch (final InterruptedException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: InterruptedException");
                    e.printStackTrace();
                }
            } finally {
                executorService.shutdown();
            }

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, "callables", then);
            }

            final CustomHelperHolder holder = new CustomHelperHolder();
            setCustomNickname(holder, ctx, customNicknameArray);
            setCustomPhrase(holder, ctx, customPhraseArray);
            setCustomCommandContainer(holder, ctx, customCommandContainerArray);
            setCustomReplacementArray(holder, ctx, customReplacementArray);

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }

            return holder;
        }
    }

    private void setCustomPhrase(@NonNull CustomHelperHolder holder, @NonNull final Context ctx,
                                 @Nullable final ArrayList<CustomPhrase> objectArray) {
        boolean hasPhrase = false;
        if (objectArray != null) {
            holder.setCustomPhraseArray(objectArray);
            hasPhrase = true;
        }
        SPH.setHasPhrase(ctx, hasPhrase);
    }

    private void setCustomCommandContainer(@NonNull CustomHelperHolder holder, @NonNull final Context ctx,
                                           @Nullable final ArrayList<CustomCommandContainer> objectArray) {
        boolean hasCustomisation = false;
        if (objectArray != null) {
            holder.setCustomCommandArray(objectArray);
            hasCustomisation = true;
        }
        SPH.setHasCustomisation(ctx, hasCustomisation);
    }

    private void setCustomReplacementArray(@NonNull CustomHelperHolder holder, @NonNull final Context ctx,
                                           @Nullable final ArrayList<CustomReplacement> objectArray) {
        boolean hasReplacement = false;
        if (objectArray != null) {
            holder.setCustomReplacementArray(objectArray);
            hasReplacement = true;
        }
        SPH.setHasReplacement(ctx, hasReplacement);
    }

    private void setCustomNickname(@NonNull CustomHelperHolder holder, @NonNull final Context ctx,
                                   @Nullable final ArrayList<CustomNickname> objectArray) {
        boolean hasNickname = false;
        if (objectArray != null) {
            holder.setCustomNicknameArray(objectArray);
            hasNickname = true;
        }
        SPH.setHasNickname(ctx, hasNickname);
    }

    public ArrayList<ContainerCustomisation> getCustomisations(@NonNull final Context ctx) {
        synchronized (lock) {
            final long then = System.nanoTime();
            final ArrayList<ContainerCustomisation> containerCustomisationArray = new ArrayList<>();
            final ArrayList<CustomNickname> customNicknameArray = new ArrayList<>();
            final ArrayList<CustomPhrase> customPhraseArray = new ArrayList<>();
            final ArrayList<CustomCommandContainer> customCommandContainerArray = new ArrayList<>();
            final ArrayList<CustomReplacement> customReplacementArray = new ArrayList<>();

            final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            try {
                final Future<ArrayList<CustomNickname>> customNicknameFuture = executorService.submit(new DBCustomNicknameCallable(ctx));
                final Future<ArrayList<CustomPhrase>> customPhraseFuture = executorService.submit(new DBCustomPhraseCallable(ctx));
                final Future<ArrayList<CustomCommandContainer>> customCommandContainerFuture = executorService.submit(new DBCustomCommandCallable(ctx));
                final Future<ArrayList<CustomReplacement>> customReplacementFuture = executorService.submit(new DBCustomReplacementCallable(ctx));
                customNicknameArray.addAll(customNicknameFuture.get());
                customPhraseArray.addAll(customPhraseFuture.get());
                customCommandContainerArray.addAll(customCommandContainerFuture.get());
                customReplacementArray.addAll(customReplacementFuture.get());
            } catch (final ExecutionException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: ExecutionException");
                    e.printStackTrace();
                }
            } catch (final CancellationException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: CancellationException");
                    e.printStackTrace();
                }
            } catch (final InterruptedException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: InterruptedException");
                    e.printStackTrace();
                }
            } finally {
                executorService.shutdown();
            }

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, "callables", then);
            }

            for (CustomNickname customNickname : customNicknameArray) {
                containerCustomisationArray.add(new ContainerCustomisation(Custom.CUSTOM_NICKNAME, customNickname.getSerialised(), customNickname.getNickname(), customNickname.getContactName(), customNickname.getRowId(), CUSTOM_NICKNAME_RESOURCE_ID, CHEVRON_RESOURCE_ID));
            }
            for (CustomPhrase customPhrase : customPhraseArray) {
                containerCustomisationArray.add(new ContainerCustomisation(Custom.CUSTOM_PHRASE, customPhrase.getSerialised(), customPhrase.getKeyphrase(), customPhrase.getResponse(), customPhrase.getRowId(), CUSTOM_PHRASE_RESOURCE_ID, CHEVRON_RESOURCE_ID));
            }
            if (UtilsList.notNaked(customCommandContainerArray)) {
                final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

                ContainerCustomisation containerCustomisation;
                CustomCommandContainer customCommandContainer;
                CustomCommand customCommand;
                String serialised;
                String extra = null;
                String label;
                Intent remoteIntent = null;

                final int size = customCommandContainerArray.size();

                for (int i = 0; i < size; i++) {
                    customCommandContainer = customCommandContainerArray.get(i);

                    serialised = customCommandContainer.getSerialised();
                    customCommand = gson.fromJson(serialised, CustomCommand.class);

                    if (customCommand.getCustomAction() == CCC.CUSTOM_INTENT_SERVICE) {

                        try {
                            remoteIntent = Intent.parseUri(customCommand.getIntent(), 0);
                        } catch (final URISyntaxException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "remoteIntent.parseUri: URISyntaxException");
                                e.printStackTrace();
                            }
                        } catch (final NullPointerException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "remoteIntent.parseUri: NullPointerException");
                                e.printStackTrace();
                            }
                        }

                        if (remoteIntent != null) {

                            final Pair<Boolean, String> pair = UtilsApplication.getAppNameFromPackage(ctx,
                                    remoteIntent.getPackage());

                            if (pair.first) {
                                extra = pair.second;
                            } else {
                                extra = remoteIntent.getPackage();
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "remoteIntent null");
                            }
                            extra = ctx.getString(R.string.an_unknown_application);
                        }

                        label = CCC.getReadableName(ctx, customCommand.getCustomAction(), extra);

                    } else {
                        label = CCC.getReadableName(ctx, customCommand.getCustomAction(), extra);
                    }

                    containerCustomisation = new ContainerCustomisation(Custom.CUSTOM_COMMAND,
                            serialised,
                            customCommandContainer.getKeyphrase(),
                            label,
                            customCommandContainer.getRowId(),
                            CUSTOM_COMMAND_RESOURCE_ID, CHEVRON_RESOURCE_ID);

                    containerCustomisationArray.add(containerCustomisation);
                }
            }
            for (CustomReplacement customReplacement : customReplacementArray) {
                containerCustomisationArray.add(new ContainerCustomisation(Custom.CUSTOM_REPLACEMENT, customReplacement.getSerialised(), customReplacement.getKeyphrase(), customReplacement.getReplacement(), customReplacement.getRowId(), CUSTOM_REPLACEMENT_RESOURCE_ID, CHEVRON_RESOURCE_ID));
            }

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }

            return containerCustomisationArray;
        }
    }
}
