package ai.saiy.android.recognition.provider.Amazon;

public enum VRLanguageAmazon {
    ENGLISH_US("en-US"),
    ENGLISH_CA("en-CA"),
    ENGLISH_UK("en-GB"),
    ENGLISH_AU("en-AU"),
    ENGLISH_IN("en-IN"),
    GERMAN("de-DE"),
    JAPANESE("ja-JP");

    private final String localeString;

    VRLanguageAmazon(String localeString) {
        this.localeString = localeString;
    }

    public String getLocaleString() {
        return this.localeString;
    }
}
