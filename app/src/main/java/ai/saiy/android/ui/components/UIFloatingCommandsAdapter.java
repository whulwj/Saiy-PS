package ai.saiy.android.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.containers.SimpleContainerUI;

public class UIFloatingCommandsAdapter extends RecyclerView.Adapter<UIFloatingCommandsAdapter.ViewHolder> {
    private final ArrayList<SimpleContainerUI> mObjects;

    private final View.OnClickListener onClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvContent;
        private int boundPosition = RecyclerView.NO_POSITION;

        private ViewHolder(View view) {
            super(view);
            this.tvTitle = view.findViewById(R.id.tvCommandTitle);
            this.tvContent = view.findViewById(R.id.tvCommandContent);
        }

        public int getBoundPosition() {
            return boundPosition;
        }

        protected void setBoundPosition(int position) {
            boundPosition = position;
        }
    }

    public UIFloatingCommandsAdapter(ArrayList<SimpleContainerUI> arrayList, View.OnClickListener onClickListener) {
        this.mObjects = arrayList;
        this.onClickListener = onClickListener;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_floating_command, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.tvTitle.setText(this.mObjects.get(position).getTitle());
        viewHolder.tvContent.setText(this.mObjects.get(position).getContent());
        viewHolder.itemView.setOnClickListener(this.onClickListener);
        viewHolder.setBoundPosition(position);
    }

    @Override
    public int getItemCount() {
        return this.mObjects.size();
    }
}
