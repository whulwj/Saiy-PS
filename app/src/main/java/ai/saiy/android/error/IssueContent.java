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

package ai.saiy.android.error;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import ai.saiy.android.ui.activity.ActivityIssue;

/**
 * Holder to provide information to display in {@link ActivityIssue}
 * <p/>
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public class IssueContent implements Parcelable {
    private final int issueConstant;
    private String issueText;

    public IssueContent(final int issueConstant) {
        this.issueConstant = issueConstant;
    }

    public static final Creator<IssueContent> CREATOR = new Creator<IssueContent>() {
        @Override
        public IssueContent createFromParcel(Parcel in) {
            final IssueContent issueContent = new IssueContent(in.readInt());
            issueContent.issueText = in.readString();
            return issueContent;
        }

        @Override
        public IssueContent[] newArray(int size) {
            return new IssueContent[size];
        }
    };

    public void setIssueText(@NonNull final String issueText) {
        this.issueText = issueText;
    }

    public int getIssueConstant() {
        return issueConstant;
    }

    public String getIssueText() {
        return issueText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(issueConstant);
        dest.writeString(issueText);
    }
}
