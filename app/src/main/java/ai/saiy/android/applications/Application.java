package ai.saiy.android.applications;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class Application {
    private CharSequence label;
    private String packageName;
    private Drawable icon;
    private String action;

    public Application(CharSequence label, @NonNull String packageName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setAction(String str) {
        this.action = str;
    }

    public CharSequence getLabel() {
        return this.label;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getAction() {
        return this.action;
    }
}
