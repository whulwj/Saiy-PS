package ai.saiy.android.contacts;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;

public class ContentObserverAdapter extends ContentObserver {
    public ContentObserverAdapter(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }

    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri) {
        super.onChange(selfChange, uri);
    }
}
