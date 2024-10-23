package ai.saiy.android.files;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

/**
 * Caching version of a [DocumentFile].
 * <p>
 * A [DocumentFile] will perform a lookup (via the system [ContentResolver]), whenever a
 * property is referenced. This means that a request for [DocumentFile.lastModified] is a *lot*
 * slower than one would expect.
 * <p>
 * To improve performance in the app, where we want to be able to sort a list of [DocumentFile]s
 * by lastModified, we wrap it like this so the value is only looked up once.
 */
public final class CachingDocumentFile {
    private final DocumentFile documentFile;
    private long lastModified;

    public CachingDocumentFile(@NonNull DocumentFile documentFile) {
        this.documentFile = documentFile;
    }

    public long getLastModified() {
        if (lastModified == 0) {
            lastModified = documentFile.lastModified();
        }
        return lastModified;
    }

    public @NonNull Uri getUri() {
        return documentFile.getUri();
    }
}
