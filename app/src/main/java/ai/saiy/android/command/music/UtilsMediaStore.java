package ai.saiy.android.command.music;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import ai.saiy.android.utils.MyLog;

public class UtilsMediaStore {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsMediaStore.class.getSimpleName();

    public static boolean playMusicFromSearch(@NonNull Context context, CommandMusicValues.Type type, @NonNull String query) {
        final Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switch (type) {
            case ALBUM:
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
                intent.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, query);
                break;
            case ARTIST:
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
                intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, query);
                break;
            case PLAYLIST:
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.putExtra(MediaStore.EXTRA_MEDIA_PLAYLIST, query);
                } else {
                    intent.putExtra("android.intent.extra.playlist", query);
                }
                break;
            case GENRE:
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.putExtra(MediaStore.EXTRA_MEDIA_GENRE, query);
                } else {
                    intent.putExtra("android.intent.extra.genre", query);
                }
                break;
            case RADIO:
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)? ContentResolver.ANY_CURSOR_ITEM_TYPE : ContentResolver.CURSOR_ITEM_BASE_TYPE + "/*");
                break;
            default:
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)? ContentResolver.ANY_CURSOR_ITEM_TYPE : ContentResolver.CURSOR_ITEM_BASE_TYPE + "/*");
                intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, query);
                break;
        }
        intent.putExtra(SearchManager.QUERY, query);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "playMusicFromSearch ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "playMusicFromSearch Exception");
                e.printStackTrace();
            }
        }
        return false;
    }
}
