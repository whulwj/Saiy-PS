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

public class UIApplicationsAdapter extends RecyclerView.Adapter<UIApplicationsAdapter.ViewHolder> {
    private final ArrayList<ContainerUI> mObjects;
    private final View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout itemContainer;

        private final TextView title;
        private final TextView subtitle;
        private final ImageView iconMain;
        private final ImageView iconExtra;

        private ViewHolder(View view) {
            super(view);
            this.itemContainer = (LinearLayout) view.findViewById(R.id.itemContainer);
            this.title = (TextView) view.findViewById(R.id.title);
            this.subtitle = (TextView) view.findViewById(R.id.subtitle);
            this.iconMain = (ImageView) view.findViewById(R.id.iconMain);
            this.iconExtra = (ImageView) view.findViewById(R.id.iconExtra);
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
    public void onBindViewHolder(ViewHolder aVar, int position) {
        aVar.title.setText(this.mObjects.get(position).getTitle());
        aVar.subtitle.setText(this.mObjects.get(position).getSubtitle());
        aVar.iconMain.setImageResource(this.mObjects.get(position).getIconMain());
        int iconExtra = this.mObjects.get(position).getIconExtra();
        aVar.iconExtra.setImageResource(iconExtra);
        if (iconExtra != R.drawable.chevron) {
            aVar.iconExtra.setContentDescription(aVar.itemView.getContext().getString(R.string.acs_ui_main_switch));
        }
        aVar.itemContainer.setOnClickListener(this.onClickListener);
        aVar.itemContainer.setOnLongClickListener(this.onLongClickListener);
        aVar.itemContainer.setTag(position);
    }

    @Override
    public int getItemCount() {
        return this.mObjects.size();
    }
}
