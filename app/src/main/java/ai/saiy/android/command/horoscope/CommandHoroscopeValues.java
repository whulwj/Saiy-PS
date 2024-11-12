package ai.saiy.android.command.horoscope;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import ai.saiy.android.utils.UtilsString;

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

        /**
         * Get the {@link CommandHoroscopeValues.Sign} from the string equivalent
         *
         * @param name the string
         * @return the equivalent {@link CommandHoroscopeValues.Sign}
         */
        public static CommandHoroscopeValues.Sign getSign(@Nullable final String name) {
            if (UtilsString.notNaked(name)) {
                if (StringUtils.containsIgnoreCase(CAPRICORN.name(), name)) {
                    return CAPRICORN;
                } else if (StringUtils.containsIgnoreCase(AQUARIUS.name(), name)) {
                    return AQUARIUS;
                } else if (StringUtils.containsIgnoreCase(PISCES.name(), name)) {
                    return PISCES;
                } else if (StringUtils.containsIgnoreCase(ARIES.name(), name)) {
                    return ARIES;
                } else if (StringUtils.containsIgnoreCase(TAURUS.name(), name)) {
                    return TAURUS;
                } else if (StringUtils.containsIgnoreCase(GEMINI.name(), name)) {
                    return GEMINI;
                } else if (StringUtils.containsIgnoreCase(CANCER.name(), name)) {
                    return CANCER;
                } else if (StringUtils.containsIgnoreCase(LEO.name(), name)) {
                    return LEO;
                } else if (StringUtils.containsIgnoreCase(VIRGO.name(), name)) {
                    return VIRGO;
                } else if (StringUtils.containsIgnoreCase(SCORPIO.name(), name)) {
                    return SCORPIO;
                } else if (StringUtils.containsIgnoreCase(SAGITTARIUS.name(), name)) {
                    return SAGITTARIUS;
                } else if (StringUtils.containsIgnoreCase(LIBRA.name(), name)) {
                    return LIBRA;
                }
            }

            return UNKNOWN;
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
