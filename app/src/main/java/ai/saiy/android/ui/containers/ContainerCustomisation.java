/*
 * Copyright (c) 2017. Saiy® Ltd. All Rights Reserved.
 *
 * Unauthorised copying of this file, via any medium is strictly prohibited. Proprietary and confidential
 */

package ai.saiy.android.ui.containers;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import ai.saiy.android.custom.Custom;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class ContainerCustomisation implements Parcelable {

    private final Custom custom;
    private String serialised;

    private String title;
    private String subtitle;

    private int iconMain;
    private int iconExtra;

    private long rowId;


    /**
     * Constructor
     *
     * @param custom     the {@link Custom} type
     * @param serialised the serialised string of the customisation
     * @param title      of the element
     * @param subtitle   of the element
     * @param rowId      of the element
     * @param iconMain   main icon
     * @param iconExtra  secondary icon
     */
    public ContainerCustomisation(@NonNull final Custom custom, @NonNull final String serialised, @NonNull final String title,
                                  @NonNull final String subtitle, final long rowId, final int iconMain, final int iconExtra) {
        this.custom = custom;
        this.serialised = serialised;
        this.title = title;
        this.subtitle = subtitle;
        this.rowId = rowId;
        this.iconMain = iconMain;
        this.iconExtra = iconExtra;
    }

    public static final Creator<ContainerCustomisation> CREATOR = new Creator<ContainerCustomisation>() {
        @Override
        public ContainerCustomisation createFromParcel(Parcel in) {
            final int index = in.readByte();
            Custom custom = Custom.CUSTOM_INTRO;
            for (Custom item: Custom.values()) {
                if (index == item.ordinal()) {
                    custom = item;
                    break;
                }
            }
            return new ContainerCustomisation(custom, in.readString(), in.readString(), in.readString(),
                    in.readLong(), in.readInt(), in.readInt());
        }

        @Override
        public ContainerCustomisation[] newArray(int size) {
            return new ContainerCustomisation[size];
        }
    };

    public long getRowId() {
        return rowId;
    }

    public Custom getCustom() {
        return custom;
    }

    public String getSerialised() {
        return serialised;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getIconMain() {
        return iconMain;
    }

    public int getIconExtra() {
        return iconExtra;
    }

    public void setIconExtra(final int iconExtra) {
        this.iconExtra = iconExtra;
    }

    public void setIconMain(final int iconMain) {
        this.iconMain = iconMain;
    }

    public void setRowId(final long rowId) {
        this.rowId = rowId;
    }

    public void setSerialised(final String serialised) {
        this.serialised = serialised;
    }

    public void setSubtitle(final String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) custom.ordinal());
        dest.writeString(serialised);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeLong(rowId);
        dest.writeInt(iconMain);
        dest.writeInt(iconExtra);
    }
}
