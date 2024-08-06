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
import ai.saiy.android.ui.containers.ContainerCustomisation;

public class UIEditCustomisationAdapter extends RecyclerView.Adapter<UIEditCustomisationAdapter.ViewHolder> {
    private final ArrayList<ContainerCustomisation> mObjects;
    private final View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener;

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final LinearLayout itemContainer;

        private final TextView title;
        private final TextView subtitle;
        private final ImageView iconMain;
        private final ImageView iconExtra;

        private ViewHolder(View view) {
            super(view);
            this.itemContainer = view.findViewById(R.id.itemContainer);
            this.title = view.findViewById(R.id.title);
            this.subtitle = view.findViewById(R.id.subtitle);
            this.iconMain = view.findViewById(R.id.iconMain);
            this.iconExtra = view.findViewById(R.id.iconExtra);
            this.itemContainer.setOnClickListener(this);
            this.itemContainer.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            view.setTag(getAdapterPosition());
            UIEditCustomisationAdapter.this.onClickListener.onClick(view);
        }

        @Override
        public boolean onLongClick(View view) {
            view.setTag(getAdapterPosition());
            return UIEditCustomisationAdapter.this.onLongClickListener.onLongClick(view);
        }
    }

    public UIEditCustomisationAdapter(ArrayList<ContainerCustomisation> objects, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        this.mObjects = objects;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_edit_customisation, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder aVar, int position) {
        aVar.title.setText(this.mObjects.get(position).getTitle());
        aVar.subtitle.setText(this.mObjects.get(position).getSubtitle());
        aVar.iconMain.setImageResource(this.mObjects.get(position).getIconMain());
        aVar.iconExtra.setImageResource(this.mObjects.get(position).getIconExtra());
        aVar.itemContainer.setTag(position);
    }

    @Override
    public int getItemCount() {
        return this.mObjects.size();
    }
}
