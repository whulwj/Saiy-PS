package ai.saiy.android.ui.components;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final LinearLayout itemContainer;

        private final TextView title;
        private final TextView subtitle;
        private final ImageView iconMain;
        private final CheckBox cbExport;

        private ViewHolder(View view) {
            super(view);
            this.itemContainer = view.findViewById(R.id.itemContainer);
            this.title = view.findViewById(R.id.title);
            this.subtitle = view.findViewById(R.id.subtitle);
            this.iconMain = view.findViewById(R.id.iconMain);
            this.cbExport = view.findViewById(R.id.cbExport);
            this.itemContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            view.setTag(getAdapterPosition());
            UIExportCustomisationAdapter.this.onClickListener.onClick(view);
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
    public void onBindViewHolder(ViewHolder aVar, int position) {
        aVar.title.setText(this.mObjects.get(position).getTitle());
        aVar.subtitle.setText(this.mObjects.get(position).getSubtitle());
        aVar.iconMain.setImageResource(this.mObjects.get(position).getIconMain());
        aVar.cbExport.setChecked(this.sparseBooleanArray.get(position));
        aVar.itemContainer.setTag(position);
    }

    @Override
    public int getItemCount() {
        return this.mObjects.size();
    }
}
