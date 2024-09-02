package ai.saiy.android.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.containers.ContainerUI;

public class UIApplicationsAdapter extends RecyclerView.Adapter<UIApplicationsAdapter.ViewHolder> {
    private final ArrayList<ContainerUI> mObjects;
    private final View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;
        private final ImageView iconMain;
        private final ImageView iconExtra;
        private int boundPosition = RecyclerView.NO_POSITION;

        private ViewHolder(View view) {
            super(view);
            this.title = view.findViewById(R.id.title);
            this.subtitle = view.findViewById(R.id.subtitle);
            this.iconMain = view.findViewById(R.id.iconMain);
            this.iconExtra = view.findViewById(R.id.iconExtra);
        }

        public int getBoundPosition() {
            return boundPosition;
        }

        protected void setBoundPosition(int position) {
            boundPosition = position;
        }
    }

    public UIApplicationsAdapter(ArrayList<ContainerUI> arrayList, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        this.mObjects = arrayList;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_ui_main, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.title.setText(this.mObjects.get(position).getTitle());
        viewHolder.subtitle.setText(this.mObjects.get(position).getSubtitle());
        viewHolder.iconMain.setImageResource(this.mObjects.get(position).getIconMain());
        int iconExtra = this.mObjects.get(position).getIconExtra();
        viewHolder.iconExtra.setImageResource(iconExtra);
        if (iconExtra != R.drawable.chevron) {
            viewHolder.iconExtra.setContentDescription(viewHolder.itemView.getContext().getString(R.string.acs_ui_main_switch));
        }
        viewHolder.setBoundPosition(position);
        viewHolder.itemView.setOnClickListener(this.onClickListener);
        viewHolder.itemView.setOnLongClickListener(this.onLongClickListener);
    }

    @Override
    public int getItemCount() {
        return this.mObjects.size();
    }
}
