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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal;

import androidx.annotation.NonNull;

/**
 * Created by benrandall76@gmail.com on 14/08/2016.
 */

public class AnalysisResult {

    private long analysisTime;
    private String recordingId;
    private String description;

    public AnalysisResult() {
    }

    public AnalysisResult(@NonNull final String recordingId, final long analysisTime, @NonNull final String description) {
        this.recordingId = recordingId;
        this.analysisTime = analysisTime;
        this.description = description;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(final String recordingId) {
        this.recordingId = recordingId;
    }

    public long getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(final long analysisTime) {
        this.analysisTime = analysisTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
