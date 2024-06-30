package ai.saiy.android.command.horoscope;

public class CommandHoroscopeValues {
    private Sign sign;
    private String name;

    public enum Sign {
        UNKNOWN(null),
        CAPRICORN("Capricorn"),
        AQUARIUS("Aquarius"),
        PISCES("Pisces"),
        ARIES("Aries"),
        TAURUS("Taurus"),
        GEMINI("Gemini"),
        CANCER("Cancer"),
        LEO("Leo"),
        VIRGO("Virgo"),
        SCORPIO("Scorpio"),
        SAGITTARIUS("Sagittarius"),
        LIBRA("Libra");

        private final String name;

        Sign(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    public Sign getSign() {
        return this.sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }

    public void setName(String str) {
        this.name = str;
    }
}
