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
import ai.saiy.android.ui.containers.ContainerCustomisation;

public class UIEditCustomisationAdapter extends RecyclerView.Adapter<UIEditCustomisationAdapter.ViewHolder> {
    private final ArrayList<ContainerCustomisation> mObjects;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(this.mObjects.get(position).getTitle());
        holder.subtitle.setText(this.mObjects.get(position).getSubtitle());
        holder.iconMain.setImageResource(this.mObjects.get(position).getIconMain());
        holder.iconExtra.setImageResource(this.mObjects.get(position).getIconExtra());

        holder.setBoundPosition(position);
        holder.itemView.setOnClickListener(onClickListener);
        holder.itemView.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public int getItemCount() {
        return this.mObjects.size();
    }
}
