package ai.saiy.android.ui.components;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.Application;
import ai.saiy.android.utils.MyLog;

public class UIActivityPickerAdapter extends BaseExpandableListAdapter {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = UIActivityPickerAdapter.class.getSimpleName();

    private final ArrayList<Application> mObjects;
    private final PackageManager packageManager;
    private final LayoutInflater layoutInflater;

    private static class ChildViewHolder {
        TextView activityName;

        private ChildViewHolder() {
        }
    }

    private static class GroupViewHolder {
        ImageView appIcon;
        TextView appName;

        private GroupViewHolder() {
        }
    }

    public UIActivityPickerAdapter(PackageManager packageManager, LayoutInflater layoutInflater, ArrayList<Application> objects) {
        this.mObjects = objects;
        this.packageManager = packageManager;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public ActivityInfo getChild(int groupPosition, int childPosition) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getChild");
        }
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(mObjects.get(groupPosition).getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo.activities != null) {
                return packageInfo.activities[childPosition];
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getChild returning null");
            }
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getChildView");
        }
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_item_ui_act_exp, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.activityName = convertView.findViewById(R.id.actName);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        final ActivityInfo child = getChild(groupPosition, childPosition);
        if (childViewHolder.activityName != null && child != null) {
            childViewHolder.activityName.setText(String.format("%s(%s)", child.loadLabel(packageManager), child.name.replace(child.packageName, "")));
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(mObjects.get(groupPosition).getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo.activities != null) {
                return packageInfo.activities.length;
            }
            return 0;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mObjects.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mObjects.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_item_ui_app_exp, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.appIcon = convertView.findViewById(R.id.appIcon);
            groupViewHolder.appName = convertView.findViewById(R.id.appName);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
       final Application application = mObjects.get(groupPosition);
        if (groupViewHolder.appIcon != null) {
            groupViewHolder.appIcon.setImageDrawable(application.getIcon());
        }
        if (groupViewHolder.appName != null) {
            groupViewHolder.appName.setText(application.getLabel());
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
