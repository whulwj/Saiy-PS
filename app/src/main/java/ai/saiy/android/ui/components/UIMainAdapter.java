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

package ai.saiy.android.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.containers.ContainerUI;

/**
 * Utility class for the main user interface layout
 * <p>
 * Created by benrandall76@gmail.com on 19/07/2016.
 */

public class UIMainAdapter extends RecyclerView.Adapter<UIMainAdapter.ViewHolder> {

    private final ArrayList<ContainerUI> mObjects;
    private final View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener;

    /**
     * Constructor
     *
     * @param mObjects            list of {@link ContainerUI} elements
     * @param onClickListener     the listener of the parent
     * @param onLongClickListener the long click listener of the parent
     */
    public UIMainAdapter(@NonNull final ArrayList<ContainerUI> mObjects,
                         @NonNull final View.OnClickListener onClickListener,
                         @NonNull final View.OnLongClickListener onLongClickListener) {
        this.mObjects = mObjects;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    /**
     * Set the contents of the view holder
     */
    @SuppressWarnings("WeakerAccess")
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout itemContainer;
        private final TextView title;
        private final TextView subtitle;
        private final ImageView iconMain;
        private final ImageView iconExtra;

        /**
         * Constructor
         *
         * @param view containing our layout items
         */
        private ViewHolder(@NonNull final View view) {
            super(view);

            itemContainer = (LinearLayout) view.findViewById(R.id.itemContainer);
            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
            iconMain = (ImageView) view.findViewById(R.id.iconMain);
            iconExtra = (ImageView) view.findViewById(R.id.iconExtra);
        }
    }

    @Override
    public @NonNull UIMainAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(
                LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.layout_item_ui_main, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.title.setText(mObjects.get(position).getTitle());
        holder.subtitle.setText(mObjects.get(position).getSubtitle());
        holder.iconMain.setImageResource(mObjects.get(position).getIconMain());
        holder.iconExtra.setImageResource(mObjects.get(position).getIconExtra());
        holder.itemContainer.setOnClickListener(onClickListener);
        holder.itemContainer.setOnLongClickListener(onLongClickListener);
        holder.itemContainer.setTag(position);

    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }
}
