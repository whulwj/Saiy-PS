package ai.saiy.android.command.music;

public class CommandMusicValues {
    private Type type;
    private String query;

    public enum Type {
        UNKNOWN,
        ALBUM,
        ARTIST,
        PLAYLIST,
        RADIO,
        GENRE
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setQuery(String str) {
        this.query = str;
    }

    public String getQuery() {
        return this.query;
    }
}
