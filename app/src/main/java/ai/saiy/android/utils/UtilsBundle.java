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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *      Copyright 2012 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 *      Licensed to the Apache Software Foundation (ASF) under one or more
 *      contributor license agreements.  See the NOTICE file distributed with
 *      this work for additional information regarding copyright ownership.
 *      The ASF licenses this file to You under the Apache License, Version 2.0
 *      (the "License"); you may not use this file except in compliance with
 *      the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package ai.saiy.android.utils;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.webkit.URLUtil;

import androidx.annotation.Nullable;

import com.google.common.collect.Lists;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Created by benrandall76@gmail.com on 23/04/2016.
 */
public class UtilsBundle {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsBundle.class.getSimpleName();

    public static final String KEY_URI_PREFIX = "URI:";
    /**
     * Prevent instantiation
     */
    public UtilsBundle() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static boolean notNaked(@Nullable final Bundle bundle) {
        return bundle != null && !bundle.isEmpty();
    }

    /**
     * Scrubs Intents for private serializable subclasses in the Intent extras. If the Intent's extras contain
     * a private serializable subclass, the Bundle is cleared. The Bundle will not be set to null. If the
     * Bundle is null, has no extras, or the extras do not contain a private serializable subclass, the Bundle
     * is not mutated.
     *
     * @param intent {@code Intent} to scrub. This parameter may be mutated if scrubbing is necessary. This
     *               parameter may be null.
     * @return true if the Intent was scrubbed, false if the Intent was not modified.
     */
    public static boolean isSuspicious(@Nullable final Intent intent) {
        return intent != null && isSuspicious(intent.getExtras());
    }

    /**
     * Scrubs Bundles for private serializable subclasses in the extras. If the Bundle's extras contain a
     * private serializable subclass, the Bundle is cleared. If the Bundle is null, has no extras, or the
     * extras do not contain a private serializable subclass, the Bundle is not mutated.
     *
     * @param bundle {@code Bundle} to scrub. This parameter may be mutated if scrubbing is necessary. This
     *               parameter may be null.
     * @return true if the Bundle was scrubbed, false if the Bundle was not modified.
     */
    public static boolean isSuspicious(@Nullable final Bundle bundle) {

        if (bundle != null) {

        /*
         * Note: This is a hack to work around a private serializable classloader attack
         */
            try {
                // if a private serializable exists, this will throw an exception
                bundle.containsKey(null);
            } catch (final Exception e) {
                bundle.clear();
                return true;
            }
        }

        return false;
    }

    public static Bundle stringExtra(String str) {
        if (!UtilsString.notNaked(str)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stringExtra: naked");
            }
            return Bundle.EMPTY;
        }
        final ArrayList<String> arrayList = Lists.newArrayList(com.google.common.base.Splitter.on(XMLResultsHandler.SEP_COMMA).trimResults().split(str));
        arrayList.removeAll(Collections.singleton(null));
        arrayList.removeAll(Collections.singleton(""));
        if (!UtilsList.notNaked(arrayList)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stringExtra: no content");
            }
            return Bundle.EMPTY;
        }
        final Bundle bundle = new Bundle();
        for (String stringExtra : arrayList) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stringExtra: " + stringExtra);
            }
            if (!stringExtra.contains(":")) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stringExtra: delimiter missing");
                }
                continue;
            }
            final String[] split = stringExtra.split(":", 2);
            final int length = split.length;
            if (length != 2) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "extractedArrayLength: incorrect" + length);
                }
                continue;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "extractedArrayLength: " + length);
                for (int i = 0; i < length; i++) {
                    MyLog.i(CLS_NAME, "extracted: " + i + " : " + split[i]);
                }
            }
            final String key = split[0].trim();
            final String value = split[1].trim();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "key: " + key);
                MyLog.i(CLS_NAME, "value: " + value);
            }
            if (!UtilsString.notNaked(key) || !UtilsString.notNaked(value)) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "key or value naked" + length);
                }
                continue;
            }
            if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(value)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "not numeric: " + value);
                }
                if (!URLUtil.isValidUrl(value)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "not Uri: " + value);
                    }
                    try {
                        bundle.putBoolean(key, org.apache.commons.lang3.BooleanUtils.toBoolean(value, String.valueOf(true), String.valueOf(false)));
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "resolved to: boolean: " + value);
                        }
                    } catch (IllegalArgumentException e) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "toBoolean: IllegalArgumentException");
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "not boolean: " + value);
                        }
                        if (value.matches("null")) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "string representation of null");
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "resolved to: string: " + value);
                            }
                            bundle.putString(key, value);
                        }
                    }
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolved to: Uri: " + value);
                    bundle.putString(KEY_URI_PREFIX + key, value);
                }
            } else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(value, "F")) {
                try {
                    bundle.putFloat(key, Float.parseFloat(value));
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: float: " + value);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseFloat: NumberFormatException");
                    }
                }
            } else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(value, "L")) {
                try {
                    bundle.putLong(key, Long.parseLong(value.substring(0, value.length() - 1)));
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: long: " + value);
                    }
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseLong: IndexOutOfBoundsException");
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseLong: NumberFormatException");
                    }
                }
            } else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(value, "D")) {
                try {
                    bundle.putDouble(key, Double.parseDouble(value));
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: double: " + value);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseDouble: NumberFormatException");
                    }
                }
            } else {
                try {
                    bundle.putInt(key, Integer.parseInt(value));
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: int: " + value);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseInt: NumberFormatException");
                    }
                    try {
                        bundle.putDouble(key, Double.parseDouble(value));
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "resolved to: double: " + value);
                        }
                    } catch (NumberFormatException numberFormatException) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "parseDouble: NumberFormatException");
                        }
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "isCreatable: no number created");
                        }
                    }
                }
            }
        }
        return bundle;
    }

    public static boolean stringExtrasToBundle(String str) {
        if (!UtilsString.notNaked(str)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stringExtrasToBundle: naked");
            }
            return false;
        }
        final ArrayList<String> arrayList = Lists.newArrayList(com.google.common.base.Splitter.on(XMLResultsHandler.SEP_COMMA).trimResults().split(str));
        arrayList.removeAll(Collections.singleton(null));
        arrayList.removeAll(Collections.singleton(""));
        if (!UtilsList.notNaked(arrayList)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stringExtrasToBundle: no content");
            }
            return false;
        }
        for (String stringExtra : arrayList) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stringExtra: " + stringExtra);
            }
            if (!stringExtra.contains(":")) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stringExtra: delimiter missing");
                }
                return false;
            }
            final String[] split = stringExtra.split(":", 2);
            final int length = split.length;
            if (length != 2) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "extractedArrayLength: incorrect" + length);
                }
                return false;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "extractedArrayLength: " + length);
                for (int i = 0; i < length; i++) {
                    MyLog.i(CLS_NAME, "extracted: " + i + " : " + split[i]);
                }
            }
            final String key = split[0].trim();
            final String value = split[1].trim();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "key: " + key);
                MyLog.i(CLS_NAME, "value: " + value);
            }
            if (!UtilsString.notNaked(key) || !UtilsString.notNaked(value)) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "key or value naked" + length);
                }
                return false;
            }
            if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(value)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "not numeric: " + value);
                }
                if (!URLUtil.isValidUrl(value)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "not Uri: " + value);
                    }
                    try {
                        org.apache.commons.lang3.BooleanUtils.toBoolean(value, String.valueOf(true), String.valueOf(false));
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "resolved to: boolean: " + value);
                        }
                    } catch (IllegalArgumentException e) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "toBoolean: IllegalArgumentException");
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "not boolean: " + value);
                        }
                        if (value.matches("null")) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "string representation of null");
                            }
                            return false;
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "resolved to: string: " + value);
                        }
                    }
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolved to: Uri: " + value);
                }
            } else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(value, "F")) {
                try {
                    Float.parseFloat(value);
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: float: " + value);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseFloat: NumberFormatException");
                    }
                    return false;
                }
            } else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(value, "L")) {
                try {
                    Long.parseLong(value.substring(0, value.length() - 1));
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: long: " + value);
                    }
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseLong: IndexOutOfBoundsException");
                    }
                    return false;
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseLong: NumberFormatException");
                    }
                    return false;
                }
            } else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(value, "D")) {
                try {
                    Double.parseDouble(value);
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: double: " + value);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseDouble: NumberFormatException");
                    }
                    return false;
                }
            } else {
                try {
                    Integer.parseInt(value);
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resolved to: int: " + value);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "parseInt: NumberFormatException");
                    }
                    try {
                        Double.parseDouble(value);
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "resolved to: double: " + value);
                        }
                    } catch (NumberFormatException numberFormatException) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "parseDouble: NumberFormatException");
                        }
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "isCreatable: no number created");
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * For debugging the intent extras
     *
     * @param bundle containing potential extras
     */
    public static void examineBundle(@Nullable final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineBundle");

            if (bundle != null) {
                final Set<String> keys = bundle.keySet();
                //noinspection Convert2streamapi
                for (final String key : keys) {
                    MyLog.v(CLS_NAME, "examineBundle: " + key + " ~ " + bundle.get(key));
                }
            }
        }
    }
}
