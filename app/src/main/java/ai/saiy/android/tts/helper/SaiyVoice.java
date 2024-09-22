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

package ai.saiy.android.tts.helper;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import android.speech.tts.Voice;

import androidx.annotation.NonNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import ai.saiy.android.tts.attributes.Gender;

/**
 * Created by benrandall76@gmail.com on 19/08/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class SaiyVoice extends Voice {

    private String engine;
    private Gender gender = Gender.UNDEFINED;

    public SaiyVoice(@NonNull final Voice voice) {
        super(voice.getName(), voice.getLocale(), voice.getQuality(), voice.getLatency(), voice.isNetworkConnectionRequired(),
                voice.getFeatures());
    }

    public static Set<SaiyVoice> getSaiyVoices(@NonNull final Set<Voice> voiceSet, @NonNull final String initialisedEngine) {

        final Set<SaiyVoice> saiyVoiceSet = new HashSet<>(voiceSet.size());

        if (initialisedEngine.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
            final TTSDefaults.Google[] googleList = TTSDefaults.Google.values();

            SaiyVoice saiyVoice;
            String voicePattern;
            for (final Voice voice : voiceSet) {
                saiyVoice = new SaiyVoice(voice);
                saiyVoice.setEngine(initialisedEngine);

                voicePattern = Pattern.quote(voice.getName());
                for (final TTSDefaults.Google g : googleList) {
                    if (g.getVoiceName().matches(voicePattern)) {
                        saiyVoice.setGender(g.getGender());
                        break;
                    }
                }

                saiyVoiceSet.add(saiyVoice);
            }
        } else {

            SaiyVoice saiyVoice;
            for (final Voice voice : voiceSet) {
                saiyVoice = new SaiyVoice(voice);
                saiyVoice.setEngine(initialisedEngine);
                saiyVoice.setGender(Gender.getGenderFromVoiceName(voice.getName()));
                saiyVoiceSet.add(saiyVoice);
            }
        }

        return saiyVoiceSet;
    }

    public static final Creator<SaiyVoice> CREATOR = new Creator<SaiyVoice>() {
        @Override
        public SaiyVoice createFromParcel(Parcel parcel) {
            final Voice voice = Voice.CREATOR.createFromParcel(parcel);
            final SaiyVoice saiyVoice = new SaiyVoice(voice);
            saiyVoice.engine = parcel.readString();
            saiyVoice.gender = parcel.readParcelable(Gender.class.getClassLoader());
            return saiyVoice;
        }

        @Override
        public SaiyVoice[] newArray(int size) {
            return new SaiyVoice[size];
        }
    };

    public Gender getGender() {
        return gender;
    }

    public void setGender(@NonNull final Gender gender) {
        this.gender = gender;
    }

    public void setGender(@NonNull final String voiceName) {

        if (this.engine != null) {
            if (TTSDefaults.pTTS_PKG_NAME_GOOGLE.matcher(this.engine).matches()) {
                this.gender = TTSDefaults.Google.getGender(voiceName);
            } else {
                this.gender = Gender.getGenderFromVoiceName(voiceName);
            }
        } else {
            this.gender = Gender.UNDEFINED;
        }
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(@NonNull final String engine) {
        this.engine = engine;
    }

    public static class VoiceComparator implements Comparator<Voice> {
        @Override
        public int compare(final Voice v1, final Voice v2) {
            return v1.getLocale().toString().compareTo(v2.getLocale().toString());
        }
    }

    public static class SaiyVoiceComparator implements Comparator<SaiyVoice> {
        @Override
        public int compare(final SaiyVoice v1, final SaiyVoice v2) {
            return v2.getQuality() - v1.getQuality();
        }
    }

    @Override
    public @NonNull String toString() {
        return "SaiyVoice[Name: " + getName() +
                ", locale: " + getLocale() +
                ", quality: " + getQuality() +
                ", latency: " + getLatency() +
                ", requiresNetwork: " + isNetworkConnectionRequired() +
                ", features: " + getFeatures().toString() +
                ", engine: " + engine +
                ", gender: " + gender.name() +
                "]";
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(engine);
        dest.writeParcelable(gender, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SaiyVoice saiyVoice = (SaiyVoice) o;
        return Objects.equals(engine, saiyVoice.engine) && gender == saiyVoice.gender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), engine, gender);
    }
}
