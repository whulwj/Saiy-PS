package ai.saiy.android.ui.components;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.Application;

public class UIApplicationPickerAdapter extends RecyclerView.Adapter<UIApplicationPickerAdapter.ViewHolder> {
    private final ArrayList<Application> mObjects;
    private final View.OnClickListener onClickListener;
    private final String autoPlay;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView appName;
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

    public UIApplicationPickerAdapter(ArrayList<Application> arrayList, View.OnClickListener onClickListener, String str) {
        this.mObjects = arrayList;
        this.onClickListener = onClickListener;
        this.autoPlay = str;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_ui_application, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Application application = mObjects.get(position);
        final String action = application.getAction();
        if (action == null || !action.matches(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            viewHolder.appName.setText(application.getLabel());
        } else {
            viewHolder.appName.setText(String.format("%s%s", application.getLabel(), autoPlay));
        }
        viewHolder.appIcon.setImageDrawable(application.getIcon());
        viewHolder.setBoundPosition(position);
        viewHolder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }
}
