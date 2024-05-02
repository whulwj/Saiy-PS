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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Helper class to serialise the JSON response from Beyond Verbal
 * <p>
 * Created by benrandall76@gmail.com on 09/06/2016.
 */
public class Emotions {

    public static final String SUCCESS = "success";

    @SerializedName("result")
    private final Result result;

    @SerializedName("status")
    private final String status;

    @SerializedName("recordingId")
    private String recordingId;

    public Emotions(final String recordingId, final Result result, final String status) {
        this.recordingId = recordingId;
        this.result = result;
        this.status = status;
    }

    public void setRecordingId(@NonNull final String recordingId) {
        this.recordingId = recordingId;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public Result getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }
}
