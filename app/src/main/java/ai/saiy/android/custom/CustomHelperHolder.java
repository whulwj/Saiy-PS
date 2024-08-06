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

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class CustomHelperHolder {

    private ArrayList<CustomCommandContainer> customCommandArray;
    private ArrayList<CustomNickname> customNicknameArray;
    private ArrayList<CustomReplacement> customReplacementArray;
    private ArrayList<CustomPhrase> cetCustomPhraseArray;

    public CustomHelperHolder() {
    }

    public CustomHelperHolder(@NonNull final ArrayList<CustomCommandContainer> customCommandArray) {
        this.customCommandArray = customCommandArray;
    }

    public void setCustomCommandArray(@NonNull final ArrayList<CustomCommandContainer> customCommandArray) {
        this.customCommandArray = customCommandArray;
    }

    public ArrayList<CustomCommandContainer> getCustomCommandArray() {
        return customCommandArray != null ? customCommandArray : new ArrayList<>();
    }

    public ArrayList<CustomNickname> getCustomNicknameArray() {
        return this.customNicknameArray != null ? this.customNicknameArray : new ArrayList<>();
    }

    public void setCustomNicknameArray(ArrayList<CustomNickname> arrayList) {
        this.customNicknameArray = arrayList;
    }

    public ArrayList<CustomPhrase> getCustomPhraseArray() {
        return this.cetCustomPhraseArray != null ? this.cetCustomPhraseArray : new ArrayList<>();
    }

    public void setCustomPhraseArray(ArrayList<CustomPhrase> arrayList) {
        this.cetCustomPhraseArray = arrayList;
    }

    public ArrayList<CustomReplacement> getCustomReplacementArray() {
        return this.customReplacementArray != null ? this.customReplacementArray : new ArrayList<>();
    }

    public void setCustomReplacementArray(ArrayList<CustomReplacement> arrayList) {
        this.customReplacementArray = arrayList;
    }
}
