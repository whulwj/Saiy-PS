package ai.saiy.android.ui.components;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.Application;

public class UIAppPickerMultiAdapter extends RecyclerView.Adapter<UIAppPickerMultiAdapter.ViewHolder> {
    private final ArrayList<Application> mObjects;
    private final View.OnClickListener onClickListener;
    private final SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckedTextView appName;
        private final ImageView appIcon;
        private int boundPosition = RecyclerView.NO_POSITION;

        private ViewHolder(View view) {
            super(view);
            this.appName = view.findViewById(R.id.appName);
            this.appIcon = view.findViewById(R.id.appIcon);
        }

        public int getBoundPosition() {
            return boundPosition;
        }

        protected void setBoundPosition(int position) {
            boundPosition = position;
        }
    }

    public UIAppPickerMultiAdapter(ArrayList<Application> arrayList, View.OnClickListener onClickListener) {
        this.mObjects = arrayList;
        this.onClickListener = onClickListener;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_ui_app_multi, viewGroup, false));
    }

    public @NonNull SparseBooleanArray getCheckedArray() {
        return sparseBooleanArray;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Application application = mObjects.get(position);
        viewHolder.appName.setText(application.getLabel());
        viewHolder.appName.setChecked(sparseBooleanArray.get(position));
        viewHolder.appIcon.setImageDrawable(application.getIcon());
        viewHolder.setBoundPosition(position);
        viewHolder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }
}
