package ai.saiy.android.location;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.rxjava3.core.SingleEmitter;

final class LocationCallbackAdapter {
//    private static final boolean DEBUG = MyLog.DEBUG;
//    private static final String CLS_NAME = "LocationCallback";
    private final Context mContext;
    private final CopyOnWriteArrayList<SingleEmitter<LocalLocation>> mSingleEmitters = new CopyOnWriteArrayList<>();

    LocationCallbackAdapter(Context context) {
        this.mContext = context;
    }

    public void addEmitter(@NonNull SingleEmitter<LocalLocation> emitter) {
        this.mSingleEmitters.addIfAbsent(emitter);
/*        if (DEBUG) {
            MyLog.d(CLS_NAME, "addEmit:" + emitter.hashCode());
        }*/
    }

    public boolean removeEmitter(@NonNull SingleEmitter<LocalLocation> emitter) {
/*        if (DEBUG) {
            MyLog.d(CLS_NAME, "removeEmit:" + emitter.hashCode());
        }*/
        if (this.mSingleEmitters.remove(emitter)) {
            return this.mSingleEmitters.isEmpty();
        }
        return false;
    }

    public void onLocationChanged(@NonNull Location location) {
        final LocalLocation localLocation = new LocalLocation(location);
        LocationRepository.setLastLocation(mContext, localLocation);
        if (!mSingleEmitters.isEmpty()) {
            final Iterator<SingleEmitter<LocalLocation>> emitterIterator = mSingleEmitters.iterator();
            SingleEmitter<LocalLocation> emitter;
            final ArrayList<SingleEmitter<LocalLocation>> disposedList = new ArrayList<>();
            while (emitterIterator.hasNext()) {
                emitter = emitterIterator.next();
                if (emitter.isDisposed()) {
                    disposedList.add(emitter);
                    continue;
                }
                emitter.onSuccess(localLocation);
            }
            if (!disposedList.isEmpty()) {
                mSingleEmitters.removeAll(disposedList);
            }
        }
    }
}
