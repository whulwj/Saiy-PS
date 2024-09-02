package ai.saiy.android.ui.components;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.containers.ContainerCustomisation;

public class UIExportCustomisationAdapter extends RecyclerView.Adapter<UIExportCustomisationAdapter.ViewHolder> {
    private final ArrayList<ContainerCustomisation> mObjects;
    private final View.OnClickListener onClickListener;
    private final SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;
        private final ImageView iconMain;
        private final CheckBox cbExport;
        private int boundPosition = RecyclerView.NO_POSITION;

        private ViewHolder(View view) {
            super(view);
            this.title = view.findViewById(R.id.title);
            this.subtitle = view.findViewById(R.id.subtitle);
            this.iconMain = view.findViewById(R.id.iconMain);
            this.cbExport = view.findViewById(R.id.cbExport);
        }

        public int getBoundPosition() {
            return boundPosition;
        }

        protected void setBoundPosition(int position) {
            boundPosition = position;
        }
    }

    public UIExportCustomisationAdapter(ArrayList<ContainerCustomisation> objects, View.OnClickListener onClickListener) {
        this.mObjects = objects;
        this.onClickListener = onClickListener;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_export_customisation, viewGroup, false));
    }

    public SparseBooleanArray getCheckedArray() {
        return this.sparseBooleanArray;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(this.mObjects.get(position).getTitle());
        holder.subtitle.setText(this.mObjects.get(position).getSubtitle());
        holder.iconMain.setImageResource(this.mObjects.get(position).getIconMain());
        holder.cbExport.setChecked(this.sparseBooleanArray.get(position));
        holder.setBoundPosition(position);
        holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return this.mObjects.size();
    }
}
